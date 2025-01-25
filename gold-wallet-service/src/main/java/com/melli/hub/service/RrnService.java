package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.master.entity.RrnEntity;
import com.melli.hub.exception.InternalServiceException;

public interface RrnService {

    String generateTraceId(String nationalCode, ChannelEntity channelEntity) throws InternalServiceException;

    RrnEntity findByUid(String uid) throws InternalServiceException;

    RrnEntity checkRrn(String uid, ChannelEntity channelEntity) throws InternalServiceException;

    RrnEntity findRrnById(long id)throws InternalServiceException;
}
