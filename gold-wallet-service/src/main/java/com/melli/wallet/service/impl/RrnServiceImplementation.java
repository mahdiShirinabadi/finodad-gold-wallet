package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import com.melli.wallet.domain.master.persistence.RrnRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.RrnService;
import com.melli.wallet.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Class Name: RrnServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/21/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class RrnServiceImplementation implements RrnService {

    private final RrnRepository rrnRepository;

    @Override
    public RrnEntity generateTraceId(String nationalCode, ChannelEntity channelEntity) throws InternalServiceException {

        log.info("start generate traceId ===> mobile({}), channel({})", nationalCode, channelEntity.getUsername() );
        try{
            RrnEntity rrnEntity = new RrnEntity();
            rrnEntity.setChannel(channelEntity);
            rrnEntity.setNationalCode(nationalCode);
            rrnEntity.setCreatedAt(new Date());
            rrnEntity.setCreatedBy(channelEntity.getUsername());
            rrnEntity = rrnRepository.save(rrnEntity);
            RrnEntity rrnEntityUuid = rrnRepository.findById(rrnEntity.getId());
            return rrnEntityUuid;
        }catch (Exception e){
            log.error("error in save traceId, and get error ===> ({})", e.getMessage());
            throw new InternalServiceException("error in generate traceId ===> " + e.getMessage(), StatusService.GENERAL_ERROR, HttpStatus.OK);
        }
    }

    @Override
    public RrnEntity findByUid(String uuid) throws InternalServiceException {
        RrnEntity rrn = rrnRepository.findByUuid(uuid);
        if(rrn == null){
            throw new InternalServiceException("rrn ===> " + uuid + " not exist in table !!! ", StatusService.UUID_NOT_FOUND, HttpStatus.OK);
        }
        return rrn;
    }

    @Override
    public RrnEntity checkRrn(String uuid, ChannelEntity channelEntity) throws InternalServiceException {
        RrnEntity rrn = findByUid(uuid);
        if(rrn.getChannel().getId() != channelEntity.getId()){
            log.error("channelId of user and traceId, are not the same !!!");
            throw new InternalServiceException("channelId of user and traceId, are not the same !!!", StatusService.UUID_NOT_FOUND, HttpStatus.OK);
        }
        return rrn;
    }

    @Override
    public RrnEntity findRrnById(long id) throws InternalServiceException {
        RrnEntity rrn = rrnRepository.findById(id);
        if(rrn == null){
            throw new InternalServiceException("rrn ===> " + id + " not exist in table !!! ", StatusService.UUID_NOT_FOUND, HttpStatus.OK);
        }
        return rrn;
    }
}
