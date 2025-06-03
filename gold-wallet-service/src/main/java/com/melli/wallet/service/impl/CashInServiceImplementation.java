package com.melli.wallet.service.impl;

import com.melli.wallet.domain.dto.ChargeObjectDTO;
import com.melli.wallet.domain.enumaration.CashInPaymentTypeEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.RefnumberRepository;
import com.melli.wallet.domain.redis.RefNumberRedis;
import com.melli.wallet.domain.response.UuidResponse;
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
public class CashInServiceImplementation implements CashInService {

    private final RedisLockService redisLockService;
    private final RrnService rrnService;
    private final RequestService requestService;
    private final Helper helper;
    private final WalletService walletService;
    private final WalletAccountService walletAccountService;
    private final WalletCashLimitationService walletCashLimitationService;
    private final RequestTypeService requestTypeService;
    private final TemplateService templateService;
    private final TransactionService transactionService;
    private final MessageResolverService messageResolverService;
    private final StatusService statusService;
    private final RefnumberRepository refnumberRepository;
    private final RedisLockRegistry redisLockRegistry;


    @Override
    public UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode, String amount, String accountNumber) throws InternalServiceException {
        try {

            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletService,nationalCode, walletAccountService, accountNumber);
            walletCashLimitationService.checkCashInLimitation(channelEntity, walletAccountEntity.getWalletEntity(), BigDecimal.valueOf(Long.parseLong(amount)), walletAccountEntity);
            log.info("start generate traceId, username ===> ({}), nationalCode ({})", channelEntity.getUsername(), nationalCode);
            RrnEntity rrnEntity = rrnService.generateTraceId(nationalCode, channelEntity, requestTypeService.getRequestType(RequestTypeService.CASH_IN), accountNumber, amount);
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

        RequestTypeEntity requestTypeEntity = requestTypeService.getRequestType(RequestTypeService.CASH_IN);
        RrnEntity rrnEntity = rrnService.findByUid(chargeObjectDTO.getUniqueIdentifier());

        return redisLockService.runAfterLock(chargeObjectDTO.getAccountNumber(), this.getClass(), () -> {
            log.info("start checking existence of traceId({}) ...", chargeObjectDTO.getUniqueIdentifier());
            rrnService.checkRrn(chargeObjectDTO.getUniqueIdentifier(), chargeObjectDTO.getChannel(), requestTypeEntity, String.valueOf(chargeObjectDTO.getAmount()), chargeObjectDTO.getAccountNumber());
            log.info("finish checking existence of traceId({})", chargeObjectDTO.getUniqueIdentifier());

            requestService.findCashInDuplicateWithRrnId(rrnEntity.getId());

            Lock refNumberLock = redisLockRegistry.obtain(chargeObjectDTO.getRefNumber());
            boolean lockSuccess = false;
            try {
                lockSuccess = refNumberLock.tryLock(5, TimeUnit.SECONDS);
                if (!lockSuccess) {
                    log.error("Failed to acquire lock for ref_number: {}", chargeObjectDTO.getRefNumber());
                    throw new InternalServiceException("Unable to acquire lock for ref_number", StatusService.GENERAL_ERROR, HttpStatus.OK);
                }

                Optional<RefNumberRedis> refnumberCheck = refnumberRepository.findById(chargeObjectDTO.getRefNumber());
                if (refnumberCheck.isPresent()) {
                    log.error("ref number ({}) is duplicated", chargeObjectDTO.getRefNumber());
                    throw new InternalServiceException("rer number is duplicate", StatusService.REF_NUMBER_USED_BEFORE, HttpStatus.OK);
                } else {
                    RefNumberRedis refNumberRedis = new RefNumberRedis();
                    refNumberRedis.setId(chargeObjectDTO.getRefNumber());
                    refnumberRepository.save(refNumberRedis);
                }
            } catch (Exception ex) {
                log.error("ref number ({}) is duplicated or system can not be lock", chargeObjectDTO.getRefNumber());
                throw new InternalServiceException("ref number is duplicate", StatusService.REF_NUMBER_USED_BEFORE, HttpStatus.OK);
            } finally {
                if (lockSuccess) {
                    refNumberLock.unlock();
                }
            }

            requestService.findSuccessCashInByRefNumber(chargeObjectDTO.getRefNumber());

            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletService, rrnEntity.getNationalCode(), walletAccountService, chargeObjectDTO.getAccountNumber());
            walletCashLimitationService.checkCashInLimitation(chargeObjectDTO.getChannel(), walletAccountEntity.getWalletEntity(),BigDecimal.valueOf(Long.parseLong(chargeObjectDTO.getAmount())), walletAccountEntity);

            CashInRequestEntity cashInRequestEntity = new CashInRequestEntity();
            cashInRequestEntity.setAmount(Long.parseLong(chargeObjectDTO.getAmount()));
            cashInRequestEntity.setRefNumber(chargeObjectDTO.getRefNumber());
            cashInRequestEntity.setWalletAccount(walletAccountEntity);
            cashInRequestEntity.setRrnEntity(rrnEntity);
            cashInRequestEntity.setAdditionalData(chargeObjectDTO.getAdditionalData());
            cashInRequestEntity.setChannel(chargeObjectDTO.getChannel());
            cashInRequestEntity.setResult(StatusService.CREATE);
            cashInRequestEntity.setChannelIp(chargeObjectDTO.getIp());
            cashInRequestEntity.setRequestTypeEntity(requestTypeEntity);
            cashInRequestEntity.setCreatedBy(chargeObjectDTO.getChannel().getUsername());
            cashInRequestEntity.setCreatedAt(new Date());
            cashInRequestEntity.setRefNumber(chargeObjectDTO.getRefNumber());
            cashInRequestEntity.setCashInPaymentTypeEnum(CashInPaymentTypeEnum.valueOf(chargeObjectDTO.getCashInPaymentType()));
            try {
                requestService.save(cashInRequestEntity);
            } catch (Exception ex) {
                refnumberRepository.deleteById(chargeObjectDTO.getRefNumber());
                log.error("error in save cashIn with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save cashIn", StatusService.GENERAL_ERROR, HttpStatus.OK);
            }
            cashInRequestEntity.setResult(StatusService.SUCCESSFUL);
            cashInRequestEntity.setAdditionalData(chargeObjectDTO.getAdditionalData());

            TransactionEntity transaction = new TransactionEntity();
            transaction.setAmount(BigDecimal.valueOf(Long.parseLong(chargeObjectDTO.getAmount())));
            transaction.setWalletAccountEntity(walletAccountEntity);
            transaction.setAdditionalData(chargeObjectDTO.getAdditionalData());
            transaction.setRequestTypeId(cashInRequestEntity.getRequestTypeEntity().getId());

            Map<String, Object> model = new HashMap<>();
            model.put("amount", Utility.addComma(Long.parseLong(chargeObjectDTO.getAmount())));
            model.put("traceId", String.valueOf(rrnEntity.getId()));
            model.put("additionalData", chargeObjectDTO.getAdditionalData());
            String templateMessage = templateService.getTemplate(TemplateService.CASH_IN);
            transaction.setDescription(messageResolverService.resolve(templateMessage, model));
            transactionService.insertDeposit(transaction);
            log.info("balance for walletAccount ===> {} update successful", walletAccountEntity.getAccountNumber());

            requestService.save(cashInRequestEntity);

            log.info("Start updating CashInLimitation for walletAccount ({})", walletAccountEntity.getAccountNumber());
            walletCashLimitationService.updateCashInLimitation(walletAccountEntity, BigDecimal.valueOf(Long.parseLong(chargeObjectDTO.getAmount())));
            log.info("updating CashInLimitation for walletAccount ({}) is finished.", walletAccountEntity.getAccountNumber());

            BigDecimal walletAccountServiceBalance = walletAccountService.getBalance(walletAccountEntity.getId());

            return helper.fillCashInResponse(chargeObjectDTO.getNationalCode(), rrnEntity.getUuid(), String.valueOf(walletAccountServiceBalance), walletAccountEntity.getAccountNumber());
        }, chargeObjectDTO.getUniqueIdentifier());
    }

    @Override
    public CashInTrackResponse inquiry(ChannelEntity channelEntity, String uuid, String channelIp) throws InternalServiceException {
        RequestTypeEntity requestTypeEntity = requestTypeService.getRequestType(RequestTypeService.CASH_IN);
        RrnEntity rrnEntity = rrnService.findByUid(uuid);
        rrnService.checkRrn(uuid, channelEntity, requestTypeEntity,"","");
        CashInRequestEntity cashInRequestEntity = requestService.findCashInWithRrnId(rrnEntity.getId());
        return helper.fillCashInTrackResponse(cashInRequestEntity, statusService);
    }
}
