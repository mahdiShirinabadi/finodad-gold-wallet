package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.RefnumberRepository;
import com.melli.wallet.domain.redis.RefNumberRedis;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
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
public class CashServiceImplementation implements CashService {


    private final RedisLockService redisLockService;
    private final RrnService rrnService;
    private final RequestService requestService;
    private final SecurityService securityService;
    private final Helper helper;
    private final WalletService walletService;
    private final WalletAccountService walletAccountService;
    private final WalletTypeService walletTypeService;
    private final WalletLimitationService walletLimitationService;
    private final RequestTypeService requestTypeService;
    private final TemplateService templateService;
    private final TransactionService transactionService;
    private final MessageResolverService messageResolverService;
    private final StatusService statusService;
    private final RefnumberRepository refnumberRepository;
    private final RedisLockRegistry redisLockRegistry;


    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CashInResponse cashIn(ChannelEntity channelEntity, String nationalCode, String uniqueIdentifier, String amount, String refNumber, String signData, String dataForCheckInVerify, String accountNumber, String additionalData, String ip) throws InternalServiceException {
        RrnEntity rrnEntity = rrnService.findByUid(uniqueIdentifier);
        securityService.checkSign(channelEntity, signData, dataForCheckInVerify);

        return redisLockService.runAfterLock(uniqueIdentifier, this.getClass(), () -> {
            log.info("start checking existence of traceId({}) ...", uniqueIdentifier);
            rrnService.checkRrn(uniqueIdentifier, channelEntity);
            log.info("finish checking existence of traceId({})", uniqueIdentifier);

            Lock refNumberLock = redisLockRegistry.obtain(refNumber);
            boolean lockSuccess = false;
            try {
                lockSuccess = refNumberLock.tryLock(5, TimeUnit.SECONDS);
                if (!lockSuccess) {
                    log.error("Failed to acquire lock for ref_number: {}", refNumber);
                    throw new InternalServiceException("Unable to acquire lock for ref_number", StatusService.GENERAL_ERROR, HttpStatus.OK);
                }

                Optional<RefNumberRedis> refnumberCheck = refnumberRepository.findById(refNumber);
                if (refnumberCheck.isPresent()) {
                    log.error("ref number ({}) is duplicated", refNumber);
                    throw new InternalServiceException("rer number is duplicate", StatusService.REF_NUMBER_USED_BEFORE, HttpStatus.OK);
                } else {
                    RefNumberRedis refNumberRedis = new RefNumberRedis();
                    refNumberRedis.setId(refNumber);
                    refnumberRepository.save(refNumberRedis);
                }

            } catch (Exception ex) {
                log.error("ref number ({}) is duplicated or system can not be lock", refNumber);
                throw new InternalServiceException("ref number is duplicate", StatusService.REF_NUMBER_USED_BEFORE, HttpStatus.OK);
            } finally {
                if (lockSuccess) {
                    refNumberLock.unlock();
                }
            }

            requestService.findSuccessCashInByRefNumber(refNumber);

            Optional<WalletTypeEntity> walletTypeEntity = walletTypeService.getAll().stream().filter(x -> x.getName().equals(WalletTypeService.NORMAL_USER)).findFirst();
            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccount(walletService, rrnEntity.getNationalCode(), walletAccountService, accountNumber, walletTypeEntity.get());
            walletLimitationService.checkCashInLimitation(channelEntity, walletAccountEntity.getWalletEntity(), Long.parseLong(amount), walletAccountEntity);

            CashInRequestEntity cashInRequestEntity = new CashInRequestEntity();
            cashInRequestEntity.setAmount(Long.parseLong(amount));
            cashInRequestEntity.setRefNumber(refNumber);
            cashInRequestEntity.setWalletAccount(walletAccountEntity);
            cashInRequestEntity.setRrnEntity(rrnEntity);
            cashInRequestEntity.setAdditionalData(additionalData);
            cashInRequestEntity.setChannel(channelEntity);
            cashInRequestEntity.setResult(StatusService.CREATE);
            cashInRequestEntity.setChannelIp(ip);
            cashInRequestEntity.setRequestTypeEntity(requestTypeService.getRequestType(RequestTypeService.CASH_IN));
            cashInRequestEntity.setCreatedBy(channelEntity.getUsername());
            cashInRequestEntity.setCreatedAt(new Date());
            cashInRequestEntity.setRefNumber(refNumber);
            try {
                requestService.save(cashInRequestEntity);
            } catch (Exception ex) {
                refnumberRepository.deleteById(refNumber);
                log.error("error in save cashIn with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save cashIn", StatusService.GENERAL_ERROR, HttpStatus.OK);
            }
            cashInRequestEntity.setResult(StatusService.SUCCESSFUL);
            cashInRequestEntity.setAdditionalData(additionalData);

            TransactionEntity transaction = new TransactionEntity();
            transaction.setAmount(Long.parseLong(amount));
            transaction.setWalletAccountEntity(walletAccountEntity);
            transaction.setAdditionalData(additionalData);
            transaction.setRequestTypeId(cashInRequestEntity.getRequestTypeEntity().getId());

            Map<String, Object> model = new HashMap<>();
            model.put("amount", Utility.addComma(Long.parseLong(amount)));
            model.put("traceId", String.valueOf(rrnEntity.getId()));
            model.put("additionalData", additionalData);
            String templateMessage = templateService.getTemplate(TemplateService.CASH_IN);
            transaction.setDescription(messageResolverService.resolve(templateMessage, model));
            transactionService.insertDeposit(transaction);
            log.info("balance for walletAccount ===> {} update successful", walletAccountEntity.getAccountNumber());

            requestService.save(cashInRequestEntity);

            log.info("Start updating CashInLimitation for walletAccount ({})", walletAccountEntity.getAccountNumber());
            walletLimitationService.updateCashInLimitation(walletAccountEntity, Long.parseLong(amount));
            log.info("updating CashInLimitation for walletAccount ({}) is finished.", walletAccountEntity.getAccountNumber());

            long walletAccountServiceBalance = walletAccountService.getBalance(walletAccountEntity.getId());

            return helper.fillCashInResponse(nationalCode, rrnEntity.getUuid(), walletAccountServiceBalance, walletAccountEntity.getAccountNumber());
        }, uniqueIdentifier);
    }

    @Override
    public CashInTrackResponse cashInTrack(ChannelEntity channelEntity, String uuid, String channelIp) throws InternalServiceException {
        RrnEntity rrnEntity = rrnService.findByUid(uuid);
        rrnService.checkRrn(uuid, channelEntity);
        CashInRequestEntity cashInRequestEntity = requestService.findCashInWithRrnId(rrnEntity.getId());
        return helper.fillCashInTrackResponse(cashInRequestEntity, statusService);
    }
}
