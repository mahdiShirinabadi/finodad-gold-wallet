package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.BalanceDTO;
import com.melli.wallet.domain.dto.ChargeObjectDTO;
import com.melli.wallet.domain.enumaration.CashInPaymentTypeEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.RefnumberRepository;
import com.melli.wallet.domain.redis.RefNumberRedis;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.MessageResolverOperationService;
import com.melli.wallet.service.operation.WalletCashLimitationOperationService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.service.operation.CashInOperationService;
import com.melli.wallet.util.Utility;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Class Name: CashServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/21/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class CashInOperationServiceImplementation implements CashInOperationService {

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
    private final RefnumberRepository refnumberRepository;
    private final RedisLockRegistry redisLockRegistry;


    @Override
    public UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String amount, String accountNumber) throws InternalServiceException {
        try {

            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, nationalCode, walletAccountRepositoryService, accountNumber);
            walletCashLimitationOperationService.checkCashInLimitation(channelEntity, walletAccountEntity.getWalletEntity(), BigDecimal.valueOf(Long.parseLong(amount)), walletAccountEntity);
            log.info("start generate traceId, username ===> ({}), nationalCode ({})", channelEntity.getUsername(), nationalCode);
            RrnEntity rrnEntity = rrnRepositoryService.generateTraceId(nationalCode, channelEntity, requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CASH_IN), accountNumber, amount);
            log.info("finish traceId ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), channelEntity.getUsername(), nationalCode);
            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in generate traceId with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CashInResponse charge(ChargeObjectDTO chargeObjectDTO) throws InternalServiceException {

        log.info("=== CASH IN CHARGE OPERATION START ===");
        log.info("Input parameters - uniqueIdentifier: {}, accountNumber: {}, amount: {}, refNumber: {}, nationalCode: {}", 
            chargeObjectDTO.getUniqueIdentifier(), chargeObjectDTO.getAccountNumber(), 
            chargeObjectDTO.getAmount(), chargeObjectDTO.getRefNumber(), chargeObjectDTO.getNationalCode());

        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CASH_IN);
        log.debug("Request type retrieved - type: {}", requestTypeEntity.getName());
        
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(chargeObjectDTO.getUniqueIdentifier());
        log.debug("RRN entity found - rrnId: {}, uuid: {}", rrnEntity.getId(), rrnEntity.getUuid());

        log.info("Starting Redis lock acquisition for account: {}", chargeObjectDTO.getAccountNumber());
        return redisLockService.runWithLockUntilCommit(chargeObjectDTO.getAccountNumber(), this.getClass(), () -> {
            log.info("=== LOCK ACQUIRED - STARTING CASH IN CRITICAL SECTION ===");
            log.info("start checking existence of traceId({}) ...", chargeObjectDTO.getUniqueIdentifier());
            rrnRepositoryService.checkRrn(chargeObjectDTO.getUniqueIdentifier(), chargeObjectDTO.getChannel(), requestTypeEntity, String.valueOf(chargeObjectDTO.getAmount()), chargeObjectDTO.getAccountNumber());
            log.info("finish checking existence of traceId({})", chargeObjectDTO.getUniqueIdentifier());

            log.debug("Checking for duplicate cash in requests with rrnId: {}", rrnEntity.getId());
            requestRepositoryService.findCashInDuplicateWithRrnId(rrnEntity.getId());
            log.debug("No duplicate cash in requests found");

            log.info("=== REF NUMBER VALIDATION START ===");
            log.debug("Acquiring lock for ref number validation - refNumber: {}", chargeObjectDTO.getRefNumber());
            Lock refNumberLock = redisLockRegistry.obtain(chargeObjectDTO.getRefNumber());
            boolean lockSuccess = false;
            try {
                log.debug("Attempting to acquire ref number lock with timeout: 5 seconds");
                lockSuccess = refNumberLock.tryLock(5, TimeUnit.SECONDS);
                if (!lockSuccess) {
                    log.error("Failed to acquire lock for ref_number: {} - timeout reached", chargeObjectDTO.getRefNumber());
                    throw new InternalServiceException("Unable to acquire lock for ref_number", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
                }
                log.info("Ref number lock acquired successfully - refNumber: {}", chargeObjectDTO.getRefNumber());

                log.debug("Checking ref number uniqueness - refNumber: {}", chargeObjectDTO.getRefNumber());
                Optional<RefNumberRedis> refnumberCheck = refnumberRepository.findById(chargeObjectDTO.getRefNumber());
                if (refnumberCheck.isPresent()) {
                    log.error("Ref number already exists - refNumber: {}", chargeObjectDTO.getRefNumber());
                    throw new InternalServiceException("rer number is duplicate", StatusRepositoryService.REF_NUMBER_USED_BEFORE, HttpStatus.OK);
                } else {
                    log.info("Ref number is unique - saving to Redis - refNumber: {}", chargeObjectDTO.getRefNumber());
                    RefNumberRedis refNumberRedis = new RefNumberRedis();
                    refNumberRedis.setId(chargeObjectDTO.getRefNumber());
                    refnumberRepository.save(refNumberRedis);
                    log.info("Ref number saved successfully to Redis - refNumber: {}", chargeObjectDTO.getRefNumber());
                }
            } catch (Exception ex) {
                log.error("Ref number validation failed - refNumber: {}, error: {}", chargeObjectDTO.getRefNumber(), ex.getMessage());
                throw new InternalServiceException("ref number is duplicate", StatusRepositoryService.REF_NUMBER_USED_BEFORE, HttpStatus.OK);
            } finally {
                if (lockSuccess) {
                    log.debug("Releasing ref number lock - refNumber: {}", chargeObjectDTO.getRefNumber());
                    refNumberLock.unlock();
                    log.info("Ref number lock released successfully");
                }
            }
            log.info("=== REF NUMBER VALIDATION COMPLETED ===");

            log.debug("Checking for successful cash in by ref number - refNumber: {}", chargeObjectDTO.getRefNumber());
            requestRepositoryService.findSuccessCashInByRefNumber(chargeObjectDTO.getRefNumber());
            log.debug("No successful cash in found with this ref number");

            log.debug("Validating wallet and account for nationalCode: {}, accountNumber: {}", 
                rrnEntity.getNationalCode(), chargeObjectDTO.getAccountNumber());
            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletRepositoryService, rrnEntity.getNationalCode(), walletAccountRepositoryService, chargeObjectDTO.getAccountNumber());
            log.info("Wallet account validated - accountId: {}, accountNumber: {}", 
                walletAccountEntity.getId(), walletAccountEntity.getAccountNumber());

            log.debug("Retrieving current balance for accountId: {}", walletAccountEntity.getId());
            BalanceDTO balanceBefore = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            log.info("Current balance - real: {}, available: {}", 
                balanceBefore.getRealBalance(), balanceBefore.getAvailableBalance());

            BigDecimal chargeAmount = BigDecimal.valueOf(Long.parseLong(chargeObjectDTO.getAmount()));
            log.debug("Checking cash in limitations - amount: {}, accountId: {}", chargeAmount, walletAccountEntity.getId());
            walletCashLimitationOperationService.checkCashInLimitation(chargeObjectDTO.getChannel(), walletAccountEntity.getWalletEntity(), chargeAmount, walletAccountEntity);
            log.info("Cash in limitation check passed - amount: {}", chargeAmount);

            log.debug("Creating cash in request entity");
            CashInRequestEntity cashInRequestEntity = new CashInRequestEntity();
            cashInRequestEntity.setAmount(Long.parseLong(chargeObjectDTO.getAmount()));
            cashInRequestEntity.setRefNumber(chargeObjectDTO.getRefNumber());
            cashInRequestEntity.setWalletAccount(walletAccountEntity);
            cashInRequestEntity.setRrnEntity(rrnEntity);
            cashInRequestEntity.setAdditionalData(chargeObjectDTO.getAdditionalData());
            cashInRequestEntity.setChannel(chargeObjectDTO.getChannel());
            cashInRequestEntity.setResult(StatusRepositoryService.CREATE);
            cashInRequestEntity.setChannelIp(chargeObjectDTO.getIp());
            cashInRequestEntity.setRequestTypeEntity(requestTypeEntity);
            cashInRequestEntity.setCreatedBy(chargeObjectDTO.getChannel().getUsername());
            cashInRequestEntity.setCreatedAt(new Date());
            cashInRequestEntity.setRefNumber(chargeObjectDTO.getRefNumber());
            cashInRequestEntity.setCashInPaymentTypeEnum(CashInPaymentTypeEnum.valueOf(chargeObjectDTO.getCashInPaymentType()));
            log.info("Cash in request entity created - amount: {}, refNumber: {}, paymentType: {}, channel: {}", 
                cashInRequestEntity.getAmount(), cashInRequestEntity.getRefNumber(), 
                cashInRequestEntity.getCashInPaymentTypeEnum(), cashInRequestEntity.getChannel().getUsername());
            log.info("=== SAVING CASH IN REQUEST ===");
            try {
                log.debug("Saving cash in request entity to database - amount: {}, refNumber: {}", 
                    cashInRequestEntity.getAmount(), cashInRequestEntity.getRefNumber());
                requestRepositoryService.save(cashInRequestEntity);
                log.info("Cash in request entity saved successfully - requestId: {}", cashInRequestEntity.getId());
            } catch (Exception ex) {
                log.error("Failed to save cash in request - cleaning up ref number - refNumber: {}, error: {}", 
                    chargeObjectDTO.getRefNumber(), ex.getMessage());
                refnumberRepository.deleteById(chargeObjectDTO.getRefNumber());
                log.error("error in save cashIn with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save cashIn", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
            }
            
            cashInRequestEntity.setResult(StatusRepositoryService.SUCCESSFUL);
            cashInRequestEntity.setAdditionalData(chargeObjectDTO.getAdditionalData());
            log.info("Cash in request status set to SUCCESSFUL");

            log.info("=== CREATING TRANSACTION ===");
            TransactionEntity transaction = new TransactionEntity();
            transaction.setRrnEntity(rrnEntity);
            transaction.setAmount(BigDecimal.valueOf(Long.parseLong(chargeObjectDTO.getAmount())));
            transaction.setWalletAccountEntity(walletAccountEntity);
            transaction.setAdditionalData(chargeObjectDTO.getAdditionalData());
            transaction.setRequestTypeId(cashInRequestEntity.getRequestTypeEntity().getId());
            log.debug("Transaction entity created - amount: {}, accountId: {}", 
                transaction.getAmount(), transaction.getWalletAccountEntity().getId());

            log.debug("Preparing transaction template model");
            Map<String, Object> model = new HashMap<>();
            model.put("amount", Utility.addComma(Long.parseLong(chargeObjectDTO.getAmount())));
            model.put("traceId", String.valueOf(rrnEntity.getId()));
            model.put("additionalData", chargeObjectDTO.getAdditionalData());
            log.debug("Template model prepared - amount: {}, traceId: {}", 
                model.get("amount"), model.get("traceId"));

            String templateMessage = templateRepositoryService.getTemplate(TemplateRepositoryService.CASH_IN);
            transaction.setDescription(messageResolverOperationService.resolve(templateMessage, model));
            log.debug("Transaction description resolved - template: {}", templateMessage);

            log.debug("Executing deposit transaction - amount: {}", transaction.getAmount());
            transactionRepositoryService.insertDeposit(transaction);
            log.info("Deposit transaction executed successfully - transactionId: {}, amount: {}", 
                transaction.getId(), transaction.getAmount());
            log.info("balance for walletAccount ===> {} update successful", walletAccountEntity.getAccountNumber());

            log.debug("Saving final cash in request with updated status");
            requestRepositoryService.save(cashInRequestEntity);
            log.info("Final cash in request saved successfully");

            log.info("=== UPDATING CASH IN LIMITATIONS ===");
            log.info("Start updating CashInLimitation for walletAccount ({})", walletAccountEntity.getAccountNumber());
            walletCashLimitationOperationService.updateCashInLimitation(walletAccountEntity, BigDecimal.valueOf(Long.parseLong(chargeObjectDTO.getAmount())));
            log.info("updating CashInLimitation for walletAccount ({}) is finished.", walletAccountEntity.getAccountNumber());

            log.info("=== RETRIEVING FINAL BALANCE ===");
            BalanceDTO walletAccountServiceBalance = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
            log.info("Final balance after charge - real: {}, available: {}", 
                walletAccountServiceBalance.getRealBalance(), walletAccountServiceBalance.getAvailableBalance());

            BigDecimal actualCharge = walletAccountServiceBalance.getRealBalance().subtract(balanceBefore.getRealBalance());
            log.info("Actual charge amount: {} (expected: {})", actualCharge, chargeAmount);

            log.info("=== CREATING RESPONSE ===");
            CashInResponse response = helper.fillCashInResponse(chargeObjectDTO.getNationalCode(), rrnEntity.getUuid(), String.valueOf(walletAccountServiceBalance.getAvailableBalance()), walletAccountEntity.getAccountNumber(), String.valueOf(walletAccountServiceBalance.getRealBalance()));
            log.info("Response created - nationalCode: {}, uuid: {}, availableBalance: {}, accountNumber: {}", 
                response.getNationalCode(), response.getUniqueIdentifier(), response.getBalance(), response.getWalletAccountNumber());

            log.info("=== CASH IN CHARGE OPERATION COMPLETED SUCCESSFULLY ===");
            return response;
        }, chargeObjectDTO.getUniqueIdentifier());
    }

    @Override
    public CashInTrackResponse inquiry(ChannelEntity channelEntity, String uuid, String channelIp) throws InternalServiceException {
        RequestTypeEntity requestTypeEntity = requestTypeRepositoryService.getRequestType(RequestTypeRepositoryService.CASH_IN);
        RrnEntity rrnEntity = rrnRepositoryService.findByUid(uuid);
        rrnRepositoryService.checkRrn(uuid, channelEntity, requestTypeEntity, "", "");
        CashInRequestEntity cashInRequestEntity = requestRepositoryService.findCashInWithRrnId(rrnEntity.getId());
        return helper.fillCashInTrackResponse(cashInRequestEntity, statusRepositoryService);
    }
}
