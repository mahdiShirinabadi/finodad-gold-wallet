package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ChannelAccessTokenEntity;
import com.melli.hub.exception.InternalServiceException;

import java.util.List;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ProfileAccessTokenService {
    ChannelAccessTokenEntity findTopByRefreshTokenEndTimeIsnUll(String refreshToken) throws InternalServiceException;
    ChannelAccessTokenEntity findTopByProfileEntityAndRefreshTokenEndTimeIsnUll(ProfileEntity profileEntity, String refreshToken);
    ChannelAccessTokenEntity findTopByProfileEntityAndEndTimeIsnUll(ProfileEntity profileEntity);
    void save(ChannelAccessTokenEntity channelAccessTokenEntity) throws InternalServiceException;
    List<ChannelAccessTokenEntity> findAllByProfileEntityAndEndTimeIsNull(ProfileEntity profileEntity);
}
