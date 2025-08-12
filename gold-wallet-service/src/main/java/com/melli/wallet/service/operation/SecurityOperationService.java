package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.exception.InternalServiceException;


public interface SecurityOperationService {
    void checkSign(ChannelEntity channelEntity, String sign, String data) throws InternalServiceException;

    void resetFailLoginCount(ChannelEntity channelEntity) throws InternalServiceException;

    boolean isBlock(ChannelEntity channelEntity);

    void increaseFailLogin(ChannelEntity channelEntity);

}
