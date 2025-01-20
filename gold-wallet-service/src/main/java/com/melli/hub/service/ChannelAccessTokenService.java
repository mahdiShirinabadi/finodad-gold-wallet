package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ChannelAccessTokenEntity;
import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.exception.InternalServiceException;

import java.util.List;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ChannelAccessTokenService {
    ChannelAccessTokenEntity findTopByRefreshTokenEndTimeIsnUll(String refreshToken) throws InternalServiceException;
    ChannelAccessTokenEntity findTopByChannelEntityAndRefreshTokenEndTimeIsnUll(ChannelEntity channelEntity, String refreshToken);
    ChannelAccessTokenEntity findTopByChannelEntityAndEndTimeIsnUll(ChannelEntity channelEntity);
    void save(ChannelAccessTokenEntity channelAccessTokenEntity) throws InternalServiceException;
    List<ChannelAccessTokenEntity> findAllByChannelEntityAndEndTimeIsNull(ChannelEntity channelEntity);
    List<ChannelAccessTokenEntity> findAllByProfileEntityAndEndTimeIsNull(ChannelEntity channelEntity);
}
