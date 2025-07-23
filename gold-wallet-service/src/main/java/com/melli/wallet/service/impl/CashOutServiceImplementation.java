package com.melli.wallet.service.impl;

import com.melli.wallet.domain.dto.CashOutObjectDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.RefnumberRepository;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.cash.CashOutResponse;
import com.melli.wallet.domain.response.cash.CashOutTrackResponse;
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

/**
 * Class Name: CashOutServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 5/3/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class CashOutServiceImplementation implements CashOutService {

    private final RedisLockService redisLockService;
    private final RrnService rrnService;
    private final RequestService requestService;
    private final SecurityService securityService;
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
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public CashOutResponse withdrawal(CashOutObjectDTO cashOutObjectDTO) throws InternalServiceException {

        RequestTypeEntity requestTypeEntity = requestTypeService.getRequestType(RequestTypeService.CASH_OUT);
        RrnEntity rrnEntity = rrnService.findByUid(cashOutObjectDTO.getUniqueIdentifier());

        return redisLockService.runAfterLock(cashOutObjectDTO.getAccountNumber(), this.getClass(), () -> {
            log.info("start checking existence of traceId({}) ...", cashOutObjectDTO.getUniqueIdentifier());
            rrnService.checkRrn(cashOutObjectDTO.getUniqueIdentifier(), cashOutObjectDTO.getChannel(), requestTypeEntity, String.valueOf(cashOutObjectDTO.getAmount()), cashOutObjectDTO.getAccountNumber());
            log.info("finish checking existence of traceId({})", cashOutObjectDTO.getUniqueIdentifier());

            requestService.findCashOutDuplicateWithRrnId(rrnEntity.getId());

            WalletAccountEntity walletAccountEntity = helper.checkWalletAndWalletAccountForNormalUser(walletService, rrnEntity.getNationalCode(), walletAccountService, cashOutObjectDTO.getAccountNumber());
            walletCashLimitationService.checkCashOutLimitation(cashOutObjectDTO.getChannel(), walletAccountEntity.getWalletEntity(), BigDecimal.valueOf(Long.parseLong(cashOutObjectDTO.getAmount())), walletAccountEntity);

            CashOutRequestEntity cashOutRequestEntity = new CashOutRequestEntity();
            cashOutRequestEntity.setAmount(Long.parseLong(cashOutObjectDTO.getAmount()));
            cashOutRequestEntity.setIban(cashOutObjectDTO.getIban());
            cashOutRequestEntity.setWalletAccountEntity(walletAccountEntity);
            cashOutRequestEntity.setRrnEntity(rrnEntity);
            cashOutRequestEntity.setAdditionalData(cashOutObjectDTO.getAdditionalData());
            cashOutRequestEntity.setChannel(cashOutObjectDTO.getChannel());
            cashOutRequestEntity.setResult(StatusService.CREATE);
            cashOutRequestEntity.setChannelIp(cashOutObjectDTO.getIp());
            cashOutRequestEntity.setRequestTypeEntity(requestTypeEntity);
            cashOutRequestEntity.setCreatedBy(cashOutObjectDTO.getChannel().getUsername());
            cashOutRequestEntity.setCreatedAt(new Date());
            try {
                requestService.save(cashOutRequestEntity);
            } catch (Exception ex) {
                log.error("error in save cashOut with message ({})", ex.getMessage());
                throw new InternalServiceException("error in save cashIn", StatusService.GENERAL_ERROR, HttpStatus.OK);
            }
            cashOutRequestEntity.setResult(StatusService.SUCCESSFUL);
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
            String templateMessage = templateService.getTemplate(TemplateService.CASH_OUT);
            transaction.setDescription(messageResolverService.resolve(templateMessage, model));
            transactionService.insertWithdraw(transaction);
            log.info("balance for walletAccount ===> {} update successful", walletAccountEntity.getAccountNumber());

            requestService.save(cashOutRequestEntity);

            log.info("Start updating CashOutLimitation for walletAccount ({})", walletAccountEntity.getAccountNumber());
            walletCashLimitationService.updateCashOutLimitation(walletAccountEntity, BigDecimal.valueOf(Long.parseLong(cashOutObjectDTO.getAmount())));
            log.info("updating CashOutLimitation for walletAccount ({}) is finished.", walletAccountEntity.getAccountNumber());

            BigDecimal walletAccountServiceBalance = walletAccountService.getBalance(walletAccountEntity.getId());

            return helper.fillCashOutResponse(cashOutObjectDTO.getNationalCode(), rrnEntity.getUuid(), String.valueOf(walletAccountServiceBalance), walletAccountEntity.getAccountNumber());
        }, cashOutObjectDTO.getUniqueIdentifier());
    }

    @Override
    public CashOutTrackResponse inquiry(ChannelEntity channelEntity, String uuid, String channelIp) throws InternalServiceException {
        RequestTypeEntity requestTypeEntity = requestTypeService.getRequestType(RequestTypeService.CASH_OUT);
        RrnEntity rrnEntity = rrnService.findByUid(uuid);
        rrnService.checkRrn(uuid, channelEntity, requestTypeEntity,"","");
        CashOutRequestEntity cashOutRequestEntity = requestService.findCashOutWithRrnId(rrnEntity.getId());
        return helper.fillCashOutTrackResponse(cashOutRequestEntity, statusService);
    }
}
