package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.CashOutObjectDTO;
import com.melli.wallet.domain.dto.PhysicalCashOutObjectDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.cash.CashOutResponse;
import com.melli.wallet.domain.response.cash.CashOutTrackResponse;
import com.melli.wallet.domain.response.cash.PhysicalCashOutResponse;
import com.melli.wallet.domain.response.cash.PhysicalCashOutTrackResponse;
import com.melli.wallet.domain.slave.entity.ReportCashOutRequestEntity;
import com.melli.wallet.domain.slave.entity.ReportPhysicalCashOutRequestEntity;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.MessageResolverOperationService;
import com.melli.wallet.service.operation.WalletCashLimitationOperationService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.service.operation.CashOutOperationService;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final WalletAccountTypeRepositoryService walletAccountTypeRepositoryService;
    private final StockRepositoryService stockRepositoryService;

    @Override
    public UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String amount, String accountNumber) throws InternalServiceException {
        try {
            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, nationalCode, walletAccountRepositoryService, accountNumber);
            walletCashLimitationOperationService.checkCashOutLimitation(channelEntity, walletAccountEntity.getWalletEntity(), BigDecimal.valueOf(Long.parseLong(amount)), walletAccountEntity);
            log.info("start generate traceId, username ===> ({}), nationalCode ({})", channelEntity.getUsername(), nationalCode);
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(nationalCode, channelEntity, requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CASH_OUT), accountNumber, amount);
            log.info("finish traceId ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), channelEntity.getUsername(), nationalCode);
            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in generate traceId with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CashOutResponse withdrawal(CashOutObjectDTO cashOutObjectDTO) throws InternalServiceException {

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CASH_OUT);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(cashOutObjectDTO.getUniqueIdentifier());

        return redisLockService.runAfterLock(cashOutObjectDTO.getAccountNumber(), this.getClass(), () -> {
            log.info("start checking existence of traceId({}) ...", cashOutObjectDTO.getUniqueIdentifier());
            rrnRepositoryService.checkRrn(cashOutObjectDTO.getUniqueIdentifier(), cashOutObjectDTO.getChannel(), requestTypeEntity, String.valueOf(cashOutObjectDTO.getAmount()), cashOutObjectDTO.getAccountNumber());
            log.info("finish checking existence of traceId({})", cashOutObjectDTO.getUniqueIdentifier());

            requestRepositoryService.findCashOutDuplicateWithRrnId(rrnEntity.getId());

            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, rrnEntity.getNationalCode(), walletAccountRepositoryService, cashOutObjectDTO.getAccountNumber());
            walletCashLimitationOperationService.checkCashOutLimitation(cashOutObjectDTO.getChannel(), walletAccountEntity.getWalletEntity(), BigDecimal.valueOf(Long.parseLong(cashOutObjectDTO.getAmount())), walletAccountEntity);

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
            cashOutRequestEntity.setCreatedAt(new Date());
            try {
                requestRepositoryService.save(cashOutRequestEntity);
            } catch (Exception ex) {
                log.error("error in save cashOut with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save cashIn", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
            cashOutRequestEntity.setResult(StatusRepositoryService.SUCCESSFUL);
            cashOutRequestEntity.setAdditionalData(cashOutRequestEntity.getAdditionalData());

            TransactionEntity transaction = new TransactionEntity();
            transaction.setAmount(BigDecimal.valueOf(cashOutRequestEntity.getAmount()));
            transaction.setWalletAccountEntity(walletAccountEntity);
            transaction.setAdditionalData(cashOutRequestEntity.getAdditionalData());
            transaction.setRequestTypeId(cashOutRequestEntity.getRequestTypeEntity().getId());

            Map<String, Object> model = new HashMap<>();
            model.put("amount", Utility.addComma(cashOutRequestEntity.getAmount()));
            model.put("traceId", String.valueOf(rrnEntity.getId()));
            model.put("additionalData", cashOutRequestEntity.getAdditionalData());
            String templateMessage = templateRepositoryService.getTemplate(TemplateRepositoryService.CASH_OUT);
            transaction.setDescription(messageResolverOperationService.resolve(templateMessage, model));
            transactionRepositoryService.insertWithdraw(transaction);
            log.info("balance for walletAccount ===> {} update successful", walletAccountEntity.getAccountNumber());

            requestRepositoryService.save(cashOutRequestEntity);

            log.info("Start updating CashOutLimitation for walletAccount ({})", walletAccountEntity.getAccountNumber());
            walletCashLimitationOperationService.updateCashOutLimitation(walletAccountEntity, BigDecimal.valueOf(Long.parseLong(cashOutObjectDTO.getAmount())));
            log.info("updating CashOutLimitation for walletAccount ({}) is finished.", walletAccountEntity.getAccountNumber());

            BigDecimal walletAccountServiceBalance = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());

            return helper.fillCashOutResponse(cashOutObjectDTO.getNationalCode(), rrnEntity.getUuid(), String.valueOf(walletAccountServiceBalance), walletAccountEntity.getAccountNumber());
        }, cashOutObjectDTO.getUniqueIdentifier());
    }

    @Override
    public CashOutTrackResponse inquiry(ChannelEntity channelEntity, String uuid, String channelIp) throws InternalServiceException {
        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CASH_OUT);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(uuid);
        rrnRepositoryService.checkRrn(uuid, channelEntity, requestTypeEntity,"","");
        ReportCashOutRequestEntity cashOutRequestEntity = requestRepositoryService.findCashOutWithRrnId(rrnEntity.getId());
        return helper.fillCashOutTrackResponse(cashOutRequestEntity, statusRepositoryService);
    }


    @Override
    public UuidResponse physicalGenerateUuid(ChannelEntity channelEntity, String nationalCode, String quantity, String accountNumber) throws InternalServiceException {
        try {
            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, nationalCode, walletAccountRepositoryService, accountNumber);
            walletCashLimitationOperationService.checkPhysicalCashOutLimitation(channelEntity, walletAccountEntity.getWalletEntity(), new BigDecimal(quantity), walletAccountEntity);
            log.info("start physicalGenerateUuid traceId, username ===> ({}), nationalCode ({})", channelEntity.getUsername(), nationalCode);
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(nationalCode, channelEntity, requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.PHYSICAL_CASH_OUT), accountNumber, quantity);
            log.info("finish physicalGenerateUuid traceId ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), channelEntity.getUsername(), nationalCode);
            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in physicalGenerateUuid traceId with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
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
        WalletAccountEntity channelCommissionAccount = findChannelCommissionAccount(physicalCashOutObjectDTO.getChannel(), physicalCashOutObjectDTO.getCommissionType());


        RrnEntity rrnEntity = rrnRepositoryService.findByUid(physicalCashOutObjectDTO.getUniqueIdentifier());

        return redisLockService.runAfterLock(physicalCashOutObjectDTO.getAccountNumber(), this.getClass(), () -> {
            log.info("start physicalWithdrawal checking existence of traceId({}) ...", physicalCashOutObjectDTO.getUniqueIdentifier());
            rrnRepositoryService.checkRrn(physicalCashOutObjectDTO.getUniqueIdentifier(), physicalCashOutObjectDTO.getChannel(), requestTypeEntity, String.valueOf(physicalCashOutObjectDTO.getQuantity()), physicalCashOutObjectDTO.getAccountNumber());
            log.info("finish physicalWithdrawal checking existence of traceId({})", physicalCashOutObjectDTO.getUniqueIdentifier());

            requestRepositoryService.findPhysicalCashOutDuplicateWithRrnId(rrnEntity.getId());

            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, rrnEntity.getNationalCode(), walletAccountRepositoryService, physicalCashOutObjectDTO.getAccountNumber());
            walletCashLimitationOperationService.checkPhysicalCashOutLimitation(physicalCashOutObjectDTO.getChannel(), walletAccountEntity.getWalletEntity(), physicalCashOutObjectDTO.getQuantity(), walletAccountEntity);

            PhysicalCashOutRequestEntity physicalCashOutRequestEntity = new PhysicalCashOutRequestEntity();
            physicalCashOutRequestEntity.setQuantity(physicalCashOutObjectDTO.getQuantity());
            physicalCashOutRequestEntity.setCommission(physicalCashOutObjectDTO.getCommission());
            physicalCashOutRequestEntity.setFinalQuantity(physicalCashOutObjectDTO.getQuantity().subtract(physicalCashOutObjectDTO.getCommission()));
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
            } catch (Exception ex) {
                log.error("error in save physicalCashOutRequestEntity with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save cashIn", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
            physicalCashOutRequestEntity.setResult(StatusRepositoryService.SUCCESSFUL);
            physicalCashOutRequestEntity.setAdditionalData(physicalCashOutRequestEntity.getAdditionalData());

            TransactionEntity transaction = new TransactionEntity();
            transaction.setRrnEntity(rrnEntity);
            transaction.setAmount(physicalCashOutRequestEntity.getQuantity());
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
            log.info("balance for walletAccount ===> {} update successful", walletAccountEntity.getAccountNumber());

            //commission type must be currency
            if (physicalCashOutObjectDTO.getCommission().compareTo(BigDecimal.valueOf(0L)) > 0) {
                log.info("start sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}), walletAccountId ({})", physicalCashOutRequestEntity.getRrnEntity().getUuid(), physicalCashOutObjectDTO.getCommission(), physicalCashOutObjectDTO.getNationalCode()
                        , channelCommissionAccount.getId());
                TransactionEntity commissionDeposit = createTransaction(channelCommissionAccount, physicalCashOutObjectDTO.getCommission(),
                        messageResolverOperationService.resolve(templateMessage, model), physicalCashOutRequestEntity.getAdditionalData(), physicalCashOutRequestEntity.getRequestTypeEntity().getId(), physicalCashOutRequestEntity.getRrnEntity());
                transactionRepositoryService.insertDeposit(commissionDeposit);
                log.info("finish sell transaction for uniqueIdentifier ({}), commission ({}) for deposit commission from nationalCode ({}) with transactionId ({})", physicalCashOutRequestEntity.getRrnEntity().getId(), physicalCashOutObjectDTO.getCommission(), physicalCashOutObjectDTO.getNationalCode(), commissionDeposit.getId());
            }

            requestRepositoryService.save(physicalCashOutRequestEntity);

            log.info("Start updating physicalCashOutLimitation for walletAccount ({})", walletAccountEntity.getAccountNumber());
            walletCashLimitationOperationService.updatePhysicalCashOutLimitation(walletAccountEntity, physicalCashOutRequestEntity.getFinalQuantity());
            log.info("updating physicalCashOutLimitation for walletAccount ({}) is finished.", walletAccountEntity.getAccountNumber());

            BigDecimal walletAccountServiceBalance = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            stockRepositoryService.insertWithdraw(transaction);

            return helper.fillPhysicalCashOutResponse(physicalCashOutObjectDTO.getNationalCode(), rrnEntity.getUuid(), String.valueOf(walletAccountServiceBalance), walletAccountEntity.getAccountNumber());
        }, physicalCashOutObjectDTO.getUniqueIdentifier());
    }

    @Override
    public PhysicalCashOutTrackResponse physicalInquiry(ChannelEntity channelEntity, String uuid, String channelIp) throws InternalServiceException {
        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.PHYSICAL_CASH_OUT);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(uuid);
        rrnRepositoryService.checkRrn(uuid, channelEntity, requestTypeEntity,"","");
        ReportPhysicalCashOutRequestEntity requestEntity = requestRepositoryService.findPhysicalCashOutWithRrnId(rrnEntity.getId());
        return helper.fillPhysicalCashOutTrackResponse(requestEntity, statusRepositoryService);
    }


    private WalletAccountEntity findChannelCommissionAccount(ChannelEntity channel, String walletAccountTypeName) throws InternalServiceException {

        List<WalletAccountEntity> accounts = walletAccountRepositoryService.findByWallet(channel.getWalletEntity());
        if (accounts.isEmpty()) {
            log.error("No wallet accounts found for channel {}", channel.getUsername());
            throw new InternalServiceException("na wallet account found for channel", StatusRepositoryService.CHANNEL_WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }

        WalletAccountTypeEntity wageType = walletAccountTypeRepositoryService.findByNameManaged(WalletAccountTypeRepositoryService.WAGE);
        if (wageType == null) {
            log.error("Wallet account type wage not found for channel {}", channel.getUsername());
            throw new InternalServiceException("Wallet account type wage not found", StatusRepositoryService.WALLET_ACCOUNT_TYPE_NOT_FOUND, HttpStatus.OK);
        }

        return accounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equalsIgnoreCase(walletAccountTypeName)
                        && x.getWalletAccountTypeEntity().getName().equalsIgnoreCase(wageType.getName())).findFirst().orElseThrow(() -> {
                    log.error("Commission account not found for channel {}", channel.getUsername());
                    return new InternalServiceException("Commission account not found", StatusRepositoryService.CHANNEL_WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
                });
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
}
