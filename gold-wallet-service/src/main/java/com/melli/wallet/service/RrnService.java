package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import com.melli.wallet.exception.InternalServiceException;

public interface RrnService {

    RrnEntity generateTraceId(String nationalCode, ChannelEntity channelEntity) throws InternalServiceException;

    RrnEntity findByUid(String uid) throws InternalServiceException;

    RrnEntity checkRrn(String uid, ChannelEntity channelEntity) throws InternalServiceException;

    RrnEntity findRrnById(long id)throws InternalServiceException;
}
