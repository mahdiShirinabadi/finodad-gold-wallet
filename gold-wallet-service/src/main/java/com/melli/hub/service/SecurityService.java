package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.exception.InternalServiceException;


public interface SecurityService {
    void checkSign(ChannelEntity channelEntity, String sign, String data) throws InternalServiceException;

    void resetFailLoginCount(ChannelEntity channelEntity) throws InternalServiceException;

    boolean isBlock(ChannelEntity channelEntity);

    void increaseFailLogin(ChannelEntity channelEntity);

}
