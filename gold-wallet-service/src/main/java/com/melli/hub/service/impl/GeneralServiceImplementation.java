package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.master.persistence.ChannelRepository;
import com.melli.hub.domain.response.UuidResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.GeneralService;
import com.melli.hub.service.RrnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.uid;

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
            String uuid = rrnService.generateTraceId(nationalCode, channelEntity);
            log.info("finish traceId ===> {}, username ({}), nationalCode ({})", uuid, channelEntity.getUsername(), nationalCode);
            return new UuidResponse(uuid);
        } catch (InternalServiceException e) {
            log.error("error in generate traceId with info ===> username ({}), nationalCode ({}) error ===> ({})", channelEntity.getUsername(), nationalCode, e.getMessage());
            throw e;
        }
    }
}
