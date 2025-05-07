package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.RefnumberRepository;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.domain.response.cash.CashOutTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;

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
    private final WalletLimitationService walletLimitationService;
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
    public CashInResponse withdrawal(ChannelEntity channelEntity, String nationalCode, String uniqueIdentifier, String amount, String refNumber, String signData, String dataForCheckInVerify, String accountNumber, String additionalData, String ip) throws InternalServiceException {
        return null;
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
