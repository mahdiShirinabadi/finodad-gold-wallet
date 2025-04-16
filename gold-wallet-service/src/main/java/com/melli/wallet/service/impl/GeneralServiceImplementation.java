package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.GeneralService;
import com.melli.wallet.service.RrnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Class Name: GeneralServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/21/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class GeneralServiceImplementation implements GeneralService {

    private final RrnService rrnService;

    @Override
    public UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode) throws InternalServiceException {
        try {
            log.info("start generate traceId, username ===> ({}), nationalCode ({})", channelEntity.getUsername(), nationalCode);
            RrnEntity rrnEntity = rrnService.generateTraceId(nationalCode, channelEntity);
            log.info("finish traceId ===> {}, username ({}), nationalCode ({})", rrnEntity.getUuid(), channelEntity.getUsername(), nationalCode);
            return new UuidResponse(rrnEntity.getUuid());
        } catch (InternalServiceException e) {
            log.error("error in generate traceId with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }
}
