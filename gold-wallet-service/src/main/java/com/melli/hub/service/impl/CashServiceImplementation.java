package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.*;
import com.melli.hub.domain.response.cash.CashInResponse;
import com.melli.hub.domain.response.cash.CashInTrackResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.*;
import com.melli.hub.util.Utility;
import com.melli.hub.utils.Helper;
import com.melli.hub.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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


    @Override
    public CashInResponse cashIn(ChannelEntity channelEntity, String nationalCode, String uniqueIdentifier, String amount, String refNumber, String signData, String dataForCheckInVerify, String accountNumber, String additionalData, String ip) throws InternalServiceException {
        RrnEntity rrnEntity = rrnService.findByUid(uniqueIdentifier);

        return redisLockService.runAfterLock(uniqueIdentifier, this.getClass(), ()->{
            log.info("cashIn: Start checking uniqueness of traceId({}) ...", rrnEntity.getId());
            requestService.checkTraceIdIsUnique(rrnEntity.getId(), new CashInRequestEntity());
            log.info("cashIn: Checking uniqueness of traceId({}), is finished.", rrnEntity.getId());

            securityService.checkSign(channelEntity, signData, dataForCheckInVerify);

            log.info("start checking existence of traceId({}) ...", uniqueIdentifier);
            rrnService.checkRrn(uniqueIdentifier, channelEntity);
            log.info("finish checking existence of traceId({})", uniqueIdentifier);

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
            requestService.save(cashInRequestEntity);
            cashInRequestEntity.setRefNumber(refNumber);
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
