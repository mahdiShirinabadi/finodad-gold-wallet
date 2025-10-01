package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.BalanceDTO;
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
import com.melli.wallet.service.operation.CashOutOperationService;
import com.melli.wallet.service.operation.MessageResolverOperationService;
import com.melli.wallet.service.operation.WalletCashLimitationOperationService;
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
import java.util.Date;
import java.util.HashMap;
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
    public CashOutResponse
    withdrawal(CashOutObjectDTO cashOutObjectDTO) throws InternalServiceException {

        log.info("=== CASH OUT WITHDRAWAL OPERATION START ===");
        log.info("Input parameters - uniqueIdentifier: {}, accountNumber: {}, amount: {}, iban: {}, nationalCode: {}", 
            cashOutObjectDTO.getUniqueIdentifier(), cashOutObjectDTO.getAccountNumber(), 
            cashOutObjectDTO.getAmount(), cashOutObjectDTO.getIban(), cashOutObjectDTO.getNationalCode());

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CASH_OUT);
        log.debug("Request type retrieved - type: {}", requestTypeEntity.getName());
        
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(cashOutObjectDTO.getUniqueIdentifier());
        log.debug("RRN entity found - rrnId: {}, uuid: {}", rrnEntity.getId(), rrnEntity.getUuid());

        log.info("Starting Redis lock acquisition for account: {}", cashOutObjectDTO.getAccountNumber());
        return redisLockService.runAfterLock(cashOutObjectDTO.getAccountNumber(), this.getClass(), () -> {
            log.info("=== LOCK ACQUIRED - STARTING CASH OUT CRITICAL SECTION ===");
            log.info("start checking existence of traceId({}) ...", cashOutObjectDTO.getUniqueIdentifier());
            rrnRepositoryService.checkRrn(cashOutObjectDTO.getUniqueIdentifier(), cashOutObjectDTO.getChannel(), requestTypeEntity, String.valueOf(cashOutObjectDTO.getAmount()), cashOutObjectDTO.getAccountNumber());
            log.info("finish checking existence of traceId({})", cashOutObjectDTO.getUniqueIdentifier());

            log.debug("Checking for duplicate cash out requests with rrnId: {}", rrnEntity.getId());
            requestRepositoryService.findCashOutDuplicateWithRrnId(rrnEntity.getId());
            log.debug("No duplicate cash out requests found");

            log.debug("Validating wallet and account for nationalCode: {}, accountNumber: {}", 
                rrnEntity.getNationalCode(), cashOutObjectDTO.getAccountNumber());
            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, rrnEntity.getNationalCode(), walletAccountRepositoryService, cashOutObjectDTO.getAccountNumber());
            log.info("Wallet account validated - accountId: {}, accountNumber: {}", 
                walletAccountEntity.getId(), walletAccountEntity.getAccountNumber());

            log.debug("Retrieving current balance for accountId: {}", walletAccountEntity.getId());
            BalanceDTO balanceBefore = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            log.info("Current balance - real: {}, available: {}", 
                balanceBefore.getRealBalance(), balanceBefore.getAvailableBalance());

            BigDecimal withdrawalAmount = BigDecimal.valueOf(Long.parseLong(cashOutObjectDTO.getAmount()));
            log.debug("Checking cash out limitations - amount: {}, accountId: {}", withdrawalAmount, walletAccountEntity.getId());
            walletCashLimitationOperationService.checkCashOutLimitation(cashOutObjectDTO.getChannel(), walletAccountEntity.getWalletEntity(), withdrawalAmount, walletAccountEntity);
            log.info("Cash out limitation check passed - amount: {}", withdrawalAmount);

            log.debug("Creating cash out request entity");
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
            log.info("Cash out request entity created - amount: {}, iban: {}, channel: {}", 
                cashOutRequestEntity.getAmount(), cashOutRequestEntity.getIban(), cashOutRequestEntity.getChannel().getUsername());
            log.info("=== SAVING CASH OUT REQUEST ===");
            try {
                log.debug("Saving cash out request entity to database - amount: {}", cashOutRequestEntity.getAmount());
                requestRepositoryService.save(cashOutRequestEntity);
                log.info("Cash out request entity saved successfully - requestId: {}", cashOutRequestEntity.getId());
            } catch (Exception ex) {
                log.error("error in save cashOut with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save cashIn", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
            
            cashOutRequestEntity.setResult(StatusRepositoryService.SUCCESSFUL);
            cashOutRequestEntity.setAdditionalData(cashOutRequestEntity.getAdditionalData());
            log.info("Cash out request status set to SUCCESSFUL");

            log.info("=== CREATING TRANSACTION ===");
            TransactionEntity transaction = new TransactionEntity();
            transaction.setAmount(BigDecimal.valueOf(cashOutRequestEntity.getAmount()));
            transaction.setWalletAccountEntity(walletAccountEntity);
            transaction.setAdditionalData(cashOutRequestEntity.getAdditionalData());
            transaction.setRequestTypeId(cashOutRequestEntity.getRequestTypeEntity().getId());
            log.debug("Transaction entity created - amount: {}, accountId: {}", 
                transaction.getAmount(), transaction.getWalletAccountEntity().getId());

            log.debug("Preparing transaction template model");
            Map<String, Object> model = new HashMap<>();
            model.put("amount", Utility.addComma(cashOutRequestEntity.getAmount()));
            model.put("traceId", String.valueOf(rrnEntity.getId()));
            model.put("additionalData", cashOutRequestEntity.getAdditionalData());
            log.debug("Template model prepared - amount: {}, traceId: {}", 
                model.get("amount"), model.get("traceId"));

            String templateMessage = templateRepositoryService.getTemplate(TemplateRepositoryService.CASH_OUT);
            transaction.setDescription(messageResolverOperationService.resolve(templateMessage, model));
            log.debug("Transaction description resolved - template: {}", templateMessage);

            log.debug("Executing withdrawal transaction - amount: {}", transaction.getAmount());
            transactionRepositoryService.insertWithdraw(transaction);
            log.info("Withdrawal transaction executed successfully - transactionId: {}, amount: {}", 
                transaction.getId(), transaction.getAmount());
            log.info("balance for walletAccount ===> {} update successful", walletAccountEntity.getAccountNumber());

            log.debug("Saving final cash out request with updated status");
            requestRepositoryService.save(cashOutRequestEntity);
            log.info("Final cash out request saved successfully");

            log.info("=== UPDATING CASH OUT LIMITATIONS ===");
            log.info("Start updating CashOutLimitation for walletAccount ({})", walletAccountEntity.getAccountNumber());
            walletCashLimitationOperationService.updateCashOutLimitation(walletAccountEntity, BigDecimal.valueOf(Long.parseLong(cashOutObjectDTO.getAmount())));
            log.info("updating CashOutLimitation for walletAccount ({}) is finished.", walletAccountEntity.getAccountNumber());

            log.info("=== RETRIEVING FINAL BALANCE ===");
            BalanceDTO walletAccountServiceBalance = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            log.info("Final balance after withdrawal - real: {}, available: {}", 
                walletAccountServiceBalance.getRealBalance(), walletAccountServiceBalance.getAvailableBalance());

            BigDecimal actualWithdrawal = balanceBefore.getRealBalance().subtract(walletAccountServiceBalance.getRealBalance());
            log.info("Actual withdrawal amount: {} (expected: {})", actualWithdrawal, withdrawalAmount);

            log.info("=== CREATING RESPONSE ===");
            CashOutResponse response = helper.fillCashOutResponse(cashOutObjectDTO.getNationalCode(), rrnEntity.getUuid(), String.valueOf(walletAccountServiceBalance.getRealBalance()), walletAccountEntity.getAccountNumber(), String.valueOf(walletAccountServiceBalance.getAvailableBalance()));
            log.info("Response created - nationalCode: {}, uuid: {}, availableBalance: {}, accountNumber: {}", 
                response.getNationalCode(), response.getUniqueIdentifier(), response.getAvailableBalance(), response.getWalletAccountNumber());

            log.info("=== CASH OUT WITHDRAWAL OPERATION COMPLETED SUCCESSFULLY ===");
            return response;
        }, cashOutObjectDTO.getUniqueIdentifier());
    }

    @Override
    public CashOutTrackResponse inquiry(ChannelEntity channelEntity, String uuid, String channelIp) throws InternalServiceException {
        log.info("=== CASH OUT INQUIRY OPERATION START ===");
        log.info("Input parameters - uuid: {}, channel: {}, channelIp: {}", 
            uuid, channelEntity.getUsername(), channelIp);

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CASH_OUT);
        log.debug("Request type retrieved - type: {}", requestTypeEntity.getName());
        
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(uuid);
        log.debug("RRN entity found - rrnId: {}, uuid: {}", rrnEntity.getId(), rrnEntity.getUuid());
        
        log.debug("Validating RRN for inquiry - uuid: {}, channel: {}", uuid, channelEntity.getUsername());
        rrnRepositoryService.checkRrn(uuid, channelEntity, requestTypeEntity,"","");
        log.info("RRN validation passed for inquiry");
        
        log.debug("Retrieving cash out request with rrnId: {}", rrnEntity.getId());
        ReportCashOutRequestEntity cashOutRequestEntity = requestRepositoryService.findCashOutWithRrnId(rrnEntity.getId());
        log.info("Cash out request found - requestId: {}, amount: {}, status: {}", 
            cashOutRequestEntity.getId(), cashOutRequestEntity.getAmount(), cashOutRequestEntity.getResult());
        
        log.info("=== CREATING INQUIRY RESPONSE ===");
        CashOutTrackResponse response = helper.fillCashOutTrackResponse(cashOutRequestEntity, statusRepositoryService);
        log.info("Inquiry response created - status: {}, amount: {}", 
            response.getResult(), response.getAmount());
        
        log.info("=== CASH OUT INQUIRY OPERATION COMPLETED ===");
        return response;
    }


    @Override
    public UuidResponse physicalGenerateUuid(ChannelEntity channelEntity, String nationalCode, String quantity, String accountNumber, String currency) throws InternalServiceException {
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
        WalletAccountEntity channelCommissionAccount = walletAccountRepositoryService.findChannelCommissionAccount(physicalCashOutObjectDTO.getChannel(), physicalCashOutObjectDTO.getCommissionType());

        if((physicalCashOutObjectDTO.getQuantity().subtract(physicalCashOutObjectDTO.getCommission())).compareTo(new BigDecimal("0")) <= 0){
            log.error("commission ({}) is bigger than quantity ({})", physicalCashOutObjectDTO.getCommission(), physicalCashOutObjectDTO.getQuantity());
            throw new InternalServiceException("commission is bigger than quantity", StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, HttpStatus.OK);
        }


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

            BalanceDTO walletAccountServiceBalance = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            stockRepositoryService.insertWithdraw(transaction);

            return helper.fillPhysicalCashOutResponse(physicalCashOutObjectDTO.getNationalCode(), rrnEntity.getUuid(), String.valueOf(walletAccountServiceBalance.getRealBalance()), walletAccountEntity.getAccountNumber());
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
