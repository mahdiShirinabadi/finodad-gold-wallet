package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.master.entity.RrnEntity;
import com.melli.hub.domain.response.cash.CashInResponse;
import com.melli.hub.domain.response.cash.CashInTrackResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.CashService;
import com.melli.hub.service.RrnService;
import com.melli.hub.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

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
    private final RequestSE


    @Override
    public CashInResponse cashIn(ChannelEntity channel, String nationalCode, String uniqueIdentifier, String amount, String refNumber, String signData, String dataForCheckInVerify, String accountNumber, String additionalData, String ip) throws InternalServiceException {
        RrnEntity rrnEntity = rrnService.findByUid(uniqueIdentifier);

        return redisLockService.runAfterLock(uniqueIdentifier, this.getClass(), ()->{
            log.info("Start checking uniqueness of traceId({}) ...", uniqueIdentifier);

        }, uniqueIdentifier);

        return null;
    }

    @Override
    public CashInTrackResponse cashInTrack(String uid, String channelIp) throws InternalServiceException {
        return null;
    }
}
