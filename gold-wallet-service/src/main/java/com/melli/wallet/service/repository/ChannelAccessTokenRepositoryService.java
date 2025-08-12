package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.ChannelAccessTokenEntity;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.util.List;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ChannelAccessTokenRepositoryService {
    ChannelAccessTokenEntity findTopByRefreshTokenEndTimeIsnUll(String refreshToken) throws InternalServiceException;
    ChannelAccessTokenEntity findTopByChannelEntityAndEndTimeIsnUll(ChannelEntity channelEntity);
    void save(ChannelAccessTokenEntity channelAccessTokenEntity) throws InternalServiceException;
    List<ChannelAccessTokenEntity> findAllByChannelEntityAndEndTimeIsNull(ChannelEntity channelEntity);
}
