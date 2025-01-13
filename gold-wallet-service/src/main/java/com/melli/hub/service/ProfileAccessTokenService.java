package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ProfileAccessTokenEntity;
import com.melli.hub.domain.master.entity.ProfileEntity;
import com.melli.hub.exception.InternalServiceException;

import java.util.List;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ProfileAccessTokenService {
    ProfileAccessTokenEntity findTopByRefreshTokenEndTimeIsnUll(String refreshToken) throws InternalServiceException;
    ProfileAccessTokenEntity findTopByProfileEntityAndRefreshTokenEndTimeIsnUll(ProfileEntity profileEntity, String refreshToken);
    ProfileAccessTokenEntity findTopByProfileEntityAndEndTimeIsnUll(ProfileEntity profileEntity);
    void save(ProfileAccessTokenEntity profileAccessTokenEntity) throws InternalServiceException;
    List<ProfileAccessTokenEntity> findAllByProfileEntityAndEndTimeIsNull(ProfileEntity profileEntity);
}
