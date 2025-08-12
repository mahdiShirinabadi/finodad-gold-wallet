package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.RequestTypeEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import com.melli.wallet.exception.InternalServiceException;

public interface RrnRepositoryService {

    RrnEntity generateTraceId(String nationalCode, ChannelEntity channelEntity, RequestTypeEntity requestTypeEntity, String accountNumber, String amount) throws InternalServiceException;

    RrnEntity findByUid(String uid) throws InternalServiceException;

    RrnEntity checkRrn(String uid, ChannelEntity channelEntity, RequestTypeEntity requestTypeEntity, String amount, String accountNumber) throws InternalServiceException;

    RrnEntity findRrnById(long id)throws InternalServiceException;
}
