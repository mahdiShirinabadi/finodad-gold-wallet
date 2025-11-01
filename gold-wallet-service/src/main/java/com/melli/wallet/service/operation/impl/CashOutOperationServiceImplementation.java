package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.BalanceDTO;
import com.melli.wallet.domain.dto.CashOutObjectDTO;
import com.melli.wallet.domain.dto.PhysicalCashOutObjectDTO;
import com.melli.wallet.domain.enumaration.SettlementStepEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.cash.CashOutResponse;
import com.melli.wallet.domain.response.cash.CashOutTrackResponse;
import com.melli.wallet.domain.response.cash.PhysicalCashOutResponse;
import com.melli.wallet.domain.response.cash.PhysicalCashOutTrackResponse;
import com.melli.wallet.domain.response.cash.PhysicalCashOutTotalQuantityResponse;
import com.melli.wallet.domain.slave.entity.ReportCashOutRequestEntity;
import com.melli.wallet.domain.slave.entity.ReportCashoutFundTransferEntity;
import com.melli.wallet.domain.slave.entity.ReportFundTransferAccountToAccountRequestEntity;
import com.melli.wallet.domain.slave.entity.ReportPhysicalCashOutRequestEntity;
import com.melli.wallet.domain.slave.persistence.ReportFundTransferAccountToAccountRepository;
import com.melli.wallet.domain.slave.persistence.ReportPhysicalCashOutRequestRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.*;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.util.Utility;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Class Name: CashOutServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 5/3/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class CashOutOperationServiceImplementation implements CashOutOperationService {

    private final RedisLockService redisLockService;
    private final RrnRepositoryService rrnRepositoryService;
    private final RequestRepositoryService requestRepositoryService;
    private final Helper helper;
    private final WalletRepositoryService walletRepositoryService;
    private final WalletAccountRepositoryService walletAccountRepositoryService;
    private final WalletCashLimitationOperationService walletCashLimitationOperationService;
    private final RequestTypeRepositoryService requestTypeRepositoryService;
    private final TemplateRepositoryService templateRepositoryService;
    private final TransactionRepositoryService transactionRepositoryService;
    private final MessageResolverOperationService messageResolverOperationService;
    private final StatusRepositoryService statusRepositoryService;
    private final StockRepositoryService stockRepositoryService;
    private final ReportPhysicalCashOutRequestRepository reportPhysicalCashOutRequestRepository;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;
    private final SettlementService settlementService;
    private final MerchantRepositoryService merchantRepositoryService;
    private final CashoutFundtransferRepositoryService cashoutFundtransferRepositoryService;

    @Override
    public UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String amount, String accountNumber) throws InternalServiceException {
        try {
            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, nationalCode, walletAccountRepositoryService, accountNumber);
            walletCashLimitationOperationService.checkCashOutLimitation(channelEntity, walletAccountEntity.getWalletEntity(), BigDecimal.valueOf(Long.parseLong(amount)), walletAccountEntity);
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(nationalCode, channelEntity, requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CASH_OUT), accountNumber, amount);
            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in generate traceId with info - username ({}), nationalCode ({}) error ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CashOutResponse
    withdrawal(CashOutObjectDTO cashOutObjectDTO) throws InternalServiceException {

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CASH_OUT);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(cashOutObjectDTO.getUniqueIdentifier());

        // Use runWithLockUntilCommit to hold lock until transaction commits, ensuring other threads see the saved record
        return redisLockService.runWithLockUntilCommit(cashOutObjectDTO.getAccountNumber(), this.getClass(), () -> {
            rrnRepositoryService.checkRrn(cashOutObjectDTO.getUniqueIdentifier(), cashOutObjectDTO.getChannel(), requestTypeEntity, String.valueOf(cashOutObjectDTO.getAmount()), cashOutObjectDTO.getAccountNumber());
            requestRepositoryService.findCashOutDuplicateWithRrnId(rrnEntity.getId());
            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, rrnEntity.getNationalCode(), walletAccountRepositoryService, cashOutObjectDTO.getAccountNumber());
            log.info("Wallet account validated - accountId: {}, accountNumber: {}",
                    walletAccountEntity.getId(), walletAccountEntity.getAccountNumber());
            BalanceDTO balanceBefore = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            log.info("Current balance - real: {}, available: {}",
                    balanceBefore.getRealBalance(), balanceBefore.getAvailableBalance());
            BigDecimal withdrawalAmount = BigDecimal.valueOf(Long.parseLong(cashOutObjectDTO.getAmount()));
            walletCashLimitationOperationService.checkCashOutLimitation(cashOutObjectDTO.getChannel(), walletAccountEntity.getWalletEntity(), withdrawalAmount, walletAccountEntity);
            log.info("Cash out limitation check passed - amount: {}", withdrawalAmount);
            CashOutRequestEntity cashOutRequestEntity = new CashOutRequestEntity();
            cashOutRequestEntity.setAmount(Long.parseLong(cashOutObjectDTO.getAmount()));
            cashOutRequestEntity.setIban(cashOutObjectDTO.getIban());
            cashOutRequestEntity.setWalletAccountEntity(walletAccountEntity);
            cashOutRequestEntity.setRrnEntity(rrnEntity);
            cashOutRequestEntity.setAdditionalData(cashOutObjectDTO.getAdditionalData());
            cashOutRequestEntity.setChannel(cashOutObjectDTO.getChannel());
            cashOutRequestEntity.setResult(StatusRepositoryService.CREATE);
            cashOutRequestEntity.setChannelIp(cashOutObjectDTO.getIp());
            cashOutRequestEntity.setRequestTypeEntity(requestTypeEntity);
            cashOutRequestEntity.setCreatedBy(cashOutObjectDTO.getChannel().getUsername());
            cashOutRequestEntity.setMerchantEntity(merchantRepositoryService.findMerchant(cashOutObjectDTO.getMerchantId()));
            cashOutRequestEntity.setCreatedAt(new Date());
            cashOutRequestEntity.setSettlementStepEnum(SettlementStepEnum.INITIAL);
            log.info("Cash out request entity created - amount: {}, iban: {}, channel: {}",
                    cashOutRequestEntity.getAmount(), cashOutRequestEntity.getIban(), cashOutRequestEntity.getChannel().getUsername());
            try {
                requestRepositoryService.save(cashOutRequestEntity);
                log.info("Cash out request entity saved successfully - requestId: {}", cashOutRequestEntity.getId());
            } catch (Exception ex) {
                log.error("error in save cashOut with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save cashOutRequestEntity", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }

            cashOutRequestEntity.setResult(StatusRepositoryService.SUCCESSFUL);
            cashOutRequestEntity.setAdditionalData(cashOutRequestEntity.getAdditionalData());
            log.info("Cash out request status set to SUCCESSFUL");
            TransactionEntity transaction = new TransactionEntity();
            transaction.setAmount(BigDecimal.valueOf(cashOutRequestEntity.getAmount()));
            transaction.setWalletAccountEntity(walletAccountEntity);
            transaction.setAdditionalData(cashOutRequestEntity.getAdditionalData());
            transaction.setRequestTypeId(cashOutRequestEntity.getRequestTypeEntity().getId());
            transaction.setRrnEntity(rrnEntity);
            Map<String, Object> model = new HashMap<>();
            model.put("amount", Utility.addComma(cashOutRequestEntity.getAmount()));
            model.put("traceId", String.valueOf(rrnEntity.getId()));
            model.put("additionalData", cashOutRequestEntity.getAdditionalData());
            String templateMessage = templateRepositoryService.getTemplate(TemplateRepositoryService.CASH_OUT);
            transaction.setDescription(messageResolverOperationService.resolve(templateMessage, model));
            transactionRepositoryService.insertWithdraw(transaction);
            log.info("Withdrawal transaction executed successfully - transactionId: {}, amount: {}",
                    transaction.getId(), transaction.getAmount());
            log.info("balance for walletAccount {} update successful", walletAccountEntity.getAccountNumber());
            requestRepositoryService.save(cashOutRequestEntity);
            log.info("Final cash out request saved successfully");
            log.info("Start updating CashOutLimitation for walletAccount ({})", walletAccountEntity.getAccountNumber());
            walletCashLimitationOperationService.updateCashOutLimitation(walletAccountEntity, BigDecimal.valueOf(Long.parseLong(cashOutObjectDTO.getAmount())));
            log.info("updating CashOutLimitation for walletAccount ({}) is finished.", walletAccountEntity.getAccountNumber());
            BalanceDTO walletAccountServiceBalance = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            log.info("Final balance after withdrawal - real: {}, available: {}",
                    walletAccountServiceBalance.getRealBalance(), walletAccountServiceBalance.getAvailableBalance());

            BigDecimal actualWithdrawal = balanceBefore.getRealBalance().subtract(walletAccountServiceBalance.getRealBalance());
            log.info("Actual withdrawal amount: {} (expected: {})", actualWithdrawal, withdrawalAmount);

            boolean settlementBatch = Boolean.parseBoolean(settingGeneralRepositoryService.getSetting(SettingGeneralRepositoryService.SETTLEMENT_BATCH).getValue());
            String uuid = UUID.randomUUID().toString();

            if (settlementBatch) {
                log.info("settlementBatch setting is true and stop this method and send to job");
                CashOutResponse response = helper.fillCashOutResponse(walletAccountEntity.getWalletEntity().getNationalCode(),
                        uuid, String.valueOf(walletAccountServiceBalance.getRealBalance()), walletAccountEntity.getAccountNumber(), String.valueOf(walletAccountServiceBalance.getAvailableBalance()));
                log.info("Response created - nationalCode: {}, uuid: {}, availableBalance: {}, accountNumber: {}",
                        response.getNationalCode(), response.getUniqueIdentifier(), response.getAvailableBalance(), response.getWalletAccountNumber());
            }

            log.info("start settlement for uuid ({}) in another thread", rrnEntity.getUuid());
            settlementService.settlement(cashOutRequestEntity);

            return helper.fillCashOutResponse(cashOutObjectDTO.getNationalCode(), rrnEntity.getUuid(), String.valueOf(walletAccountServiceBalance.getRealBalance()), walletAccountEntity.getAccountNumber(), String.valueOf(walletAccountServiceBalance.getAvailableBalance()));
        }, cashOutObjectDTO.getUniqueIdentifier());
    }

    @Override
    public CashOutTrackResponse inquiry(ChannelEntity channelEntity, String uuid, String channelIp) throws InternalServiceException {
        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CASH_OUT);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(uuid);
        rrnRepositoryService.checkRrn(uuid, channelEntity, requestTypeEntity, "", "");
        log.info("RRN validation passed for inquiry");
        ReportCashOutRequestEntity cashOutRequestEntity = requestRepositoryService.findCashOutWithRrnId(rrnEntity.getId());
        log.info("Cash out request found - requestId: {}, amount: {}, status: {}",
                cashOutRequestEntity.getId(), cashOutRequestEntity.getAmount(), cashOutRequestEntity.getResult());

        ReportFundTransferAccountToAccountRequestEntity reportFundTransferAccountToAccountRequestEntity = null;
        if (cashOutRequestEntity.getSettlementStepEnum().equals(SettlementStepEnum.SUCCESS)) {
            List<ReportCashoutFundTransferEntity> cashoutFundtransferRepositoryServiceList = cashoutFundtransferRepositoryService.findAllByCashout(cashOutRequestEntity);
            for (ReportCashoutFundTransferEntity reportCashoutFundTransferEntity : cashoutFundtransferRepositoryServiceList) {
                if (reportCashoutFundTransferEntity.getFundTransferAccountToAccountRequestEntity().getResult() == StatusRepositoryService.SUCCESSFUL) {
                    reportFundTransferAccountToAccountRequestEntity = requestRepositoryService.findFundTransferById(
                            reportCashoutFundTransferEntity.getFundTransferAccountToAccountRequestEntity().getId());
                    break;
                }
            }
        }

        CashOutTrackResponse response = helper.fillCashOutTrackResponse(cashOutRequestEntity, statusRepositoryService, reportFundTransferAccountToAccountRequestEntity);
        log.info("Inquiry response created - status: {}, amount: {}", response.getResult(), response.getPrice());
        return response;
    }


    @Override
    public UuidResponse physicalGenerateUuid(ChannelEntity channelEntity, String nationalCode, String quantity, String accountNumber, String currency) throws InternalServiceException {
        try {
            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, nationalCode, walletAccountRepositoryService, accountNumber);
            walletCashLimitationOperationService.checkPhysicalCashOutLimitation(channelEntity, walletAccountEntity.getWalletEntity(), new BigDecimal(quantity), walletAccountEntity);
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(nationalCode, channelEntity, requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.PHYSICAL_CASH_OUT), accountNumber, quantity);
            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in physicalGenerateUuid traceId with info - username ({}), nationalCode ({}) error ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PhysicalCashOutResponse physicalWithdrawal(PhysicalCashOutObjectDTO physicalCashOutObjectDTO) throws InternalServiceException {

        if (!physicalCashOutObjectDTO.getCurrency().equalsIgnoreCase(physicalCashOutObjectDTO.getCommissionType())) {
            log.error("commission and currency not be same!!!");
            throw new InternalServiceException("commission and currency not be same", StatusRepositoryService.COMMISSION_CURRENCY_NOT_VALID, HttpStatus.OK);
        }

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.PHYSICAL_CASH_OUT);
        WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(physicalCashOutObjectDTO.getChannel(), physicalCashOutObjectDTO.getCommissionType());

        if ((physicalCashOutObjectDTO.getQuantity().subtract(physicalCashOutObjectDTO.getCommission())).compareTo(new BigDecimal("0")) <= 0) {
            log.error("commission ({}) is bigger than quantity ({})", physicalCashOutObjectDTO.getCommission(), physicalCashOutObjectDTO.getQuantity());
            throw new InternalServiceException("commission is bigger than quantity", StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, HttpStatus.OK);
        }


        RrnEntity rrnEntity = rrnRepositoryService.findByUid(physicalCashOutObjectDTO.getUniqueIdentifier());

        // Use runWithLockUntilCommit to hold lock until transaction commits, ensuring other threads see the saved record
        return redisLockService.runWithLockUntilCommit(physicalCashOutObjectDTO.getAccountNumber(), this.getClass(), () -> {
            log.info("start physicalWithdrawal checking existence of traceId({}) ...", physicalCashOutObjectDTO.getUniqueIdentifier());
            rrnRepositoryService.checkRrn(physicalCashOutObjectDTO.getUniqueIdentifier(), physicalCashOutObjectDTO.getChannel(), requestTypeEntity, String.valueOf(physicalCashOutObjectDTO.getQuantity()), physicalCashOutObjectDTO.getAccountNumber());
            log.info("finish physicalWithdrawal checking existence of traceId({})", physicalCashOutObjectDTO.getUniqueIdentifier());

            // Check for duplicate INSIDE the lock - this ensures we see uncommitted records from other threads
            requestRepositoryService.findPhysicalCashOutDuplicateWithRrnId(rrnEntity.getId());

            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, rrnEntity.getNationalCode(), walletAccountRepositoryService, physicalCashOutObjectDTO.getAccountNumber());
            walletCashLimitationOperationService.checkPhysicalCashOutLimitation(physicalCashOutObjectDTO.getChannel(), walletAccountEntity.getWalletEntity(), physicalCashOutObjectDTO.getQuantity(), walletAccountEntity);

            PhysicalCashOutRequestEntity physicalCashOutRequestEntity = new PhysicalCashOutRequestEntity();
            physicalCashOutRequestEntity.setQuantity(physicalCashOutObjectDTO.getQuantity());
            physicalCashOutRequestEntity.setCommission(physicalCashOutObjectDTO.getCommission());
            physicalCashOutRequestEntity.setFinalQuantity(physicalCashOutObjectDTO.getQuantity().add(physicalCashOutObjectDTO.getCommission()));
            physicalCashOutRequestEntity.setWalletAccountEntity(walletAccountEntity);
            physicalCashOutRequestEntity.setRrnEntity(rrnEntity);
            physicalCashOutRequestEntity.setAdditionalData(physicalCashOutObjectDTO.getAdditionalData());
            physicalCashOutRequestEntity.setChannel(physicalCashOutObjectDTO.getChannel());
            physicalCashOutRequestEntity.setResult(StatusRepositoryService.CREATE);
            physicalCashOutRequestEntity.setChannelIp(physicalCashOutObjectDTO.getIp());
            physicalCashOutRequestEntity.setRequestTypeEntity(requestTypeEntity);
            physicalCashOutRequestEntity.setCreatedBy(physicalCashOutObjectDTO.getChannel().getUsername());
            physicalCashOutRequestEntity.setCreatedAt(new Date());
            try {
                requestRepositoryService.save(physicalCashOutRequestEntity);
                log.info("Physical cash out request saved with id: {}, rrnId: {}", physicalCashOutRequestEntity.getId(), physicalCashOutRequestEntity.getRrnEntity().getId());
            } catch (Exception ex) {
                log.error("error in save physicalCashOutRequestEntity with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save cashIn", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
            physicalCashOutRequestEntity.setResult(StatusRepositoryService.SUCCESSFUL);
            physicalCashOutRequestEntity.setAdditionalData(physicalCashOutRequestEntity.getAdditionalData());

            TransactionEntity transaction = new TransactionEntity();
            transaction.setRrnEntity(rrnEntity);
            transaction.setAmount(physicalCashOutRequestEntity.getQuantity().add(physicalCashOutRequestEntity.getCommission()));
            transaction.setWalletAccountEntity(walletAccountEntity);
            transaction.setAdditionalData(physicalCashOutRequestEntity.getAdditionalData());
            transaction.setRequestTypeId(physicalCashOutRequestEntity.getRequestTypeEntity().getId());

            Map<String, Object> model = new HashMap<>();
            model.put("amount", Utility.addComma(physicalCashOutRequestEntity.getQuantity().longValue()));
            model.put("traceId", String.valueOf(rrnEntity.getId()));
            model.put("additionalData", physicalCashOutRequestEntity.getAdditionalData());
            String templateMessage = templateRepositoryService.getTemplate(TemplateRepositoryService.PHYSICAL_CASH_OUT);
            transaction.setDescription(messageResolverOperationService.resolve(templateMessage, model));
            transactionRepositoryService.insertWithdraw(transaction);
            log.info("balance for walletAccount {} update successful", walletAccountEntity.getAccountNumber());

            //commission type must be currency
            if (physicalCashOutObjectDTO.getCommission().compareTo(BigDecimal.valueOf(0L)) > 0) {
                log.info("start sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", physicalCashOutRequestEntity.getRrnEntity().getUuid(), physicalCashOutObjectDTO.getCommission(), physicalCashOutObjectDTO.getNationalCode()
                        , channelCommissionAccount.getId());
                String commissionTemplate = templateRepositoryService.getTemplate(TemplateRepositoryService.COMMISSION);
                Map<String, Object> modelCommission = new HashMap<>();
                model.put("traceId", String.valueOf(physicalCashOutRequestEntity.getRrnEntity().getId()));
                model.put("commission", physicalCashOutRequestEntity.getCommission());
                model.put("requestType", physicalCashOutRequestEntity.getRequestTypeEntity().getFaName());
                TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, physicalCashOutObjectDTO.getCommission(),
                        messageResolverOperationService.resolve(commissionTemplate, modelCommission), physicalCashOutRequestEntity.getAdditionalData(), physicalCashOutRequestEntity.getRequestTypeEntity().getId(), physicalCashOutRequestEntity.getRrnEntity());
                transactionRepositoryService.insertDeposit(commissionDeposit);
                log.info("finish sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", physicalCashOutRequestEntity.getRrnEntity().getId(), physicalCashOutObjectDTO.getCommission(), physicalCashOutObjectDTO.getNationalCode(), commissionDeposit.getId());
            }

            requestRepositoryService.save(physicalCashOutRequestEntity);

            log.info("Start updating physicalCashOutLimitation for walletAccount ({})", walletAccountEntity.getAccountNumber());
            walletCashLimitationOperationService.updatePhysicalCashOutLimitation(walletAccountEntity, physicalCashOutRequestEntity.getFinalQuantity());
            log.info("updating physicalCashOutLimitation for walletAccount ({}) is finished.", walletAccountEntity.getAccountNumber());

            BalanceDTO walletAccountServiceBalance = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            stockRepositoryService.insertWithdraw(transaction);

            return helper.fillPhysicalCashOutResponse(physicalCashOutObjectDTO.getNationalCode(), rrnEntity.getUuid(), String.valueOf(walletAccountServiceBalance.getAvailableBalance()), walletAccountEntity.getAccountNumber());
        }, physicalCashOutObjectDTO.getUniqueIdentifier());
    }

    @Override
    public PhysicalCashOutTrackResponse physicalInquiry(ChannelEntity channelEntity, String uuid, String channelIp) throws InternalServiceException {
        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.PHYSICAL_CASH_OUT);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(uuid);
        rrnRepositoryService.checkRrn(uuid, channelEntity, requestTypeEntity, "", "");
        ReportPhysicalCashOutRequestEntity requestEntity = requestRepositoryService.findPhysicalCashOutWithRrnId(rrnEntity.getId());
        return helper.fillPhysicalCashOutTrackResponse(requestEntity, statusRepositoryService);
    }


    private TransactionEntity createTransaction(WalletAccountEntity account, BigDecimal amount, String description,
                                                String additionalData, long requestTypeId,
                                                RrnEntity rrn) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAmount(amount);
        transaction.setWalletAccountEntity(account);
        transaction.setDescription(description);
        transaction.setAdditionalData(additionalData);
        transaction.setRequestTypeId(requestTypeId);
        transaction.setRrnEntity(rrn);
        return transaction;
    }

    @Override
    public PhysicalCashOutTotalQuantityResponse calculateTotalQuantity(ChannelEntity channelEntity) throws InternalServiceException {
        log.info("start calculateTotalQuantity for physical cash out transactions");

        // Get request type ID for PHYSICAL_CASH_OUT

        // Calculate total quantity directly in database using aggregation
        BigDecimal totalQuantity = reportPhysicalCashOutRequestRepository.calculateTotalQuantity();

        // Handle null result (no transactions found)
        if (totalQuantity == null) {
            totalQuantity = BigDecimal.ZERO;
        }

        log.info("finish calculateTotalQuantity for physical cash out transactions, totalQuantity: {}", totalQuantity);

        return helper.fillPhysicalCashOutTotalQuantityResponse(totalQuantity, RequestTypeRepositoryService.PHYSICAL_CASH_OUT);
    }
}
