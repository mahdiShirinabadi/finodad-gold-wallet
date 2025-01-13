package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.ChannelAccessTokenEntity;
import com.melli.hub.domain.master.persistence.ProfileAccessTokenRepository;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.ProfileAccessTokenService;
import com.melli.hub.service.StatusService;
import com.melli.hub.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Class Name: ProfileAccessTokenServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class ProfileAccessTokenServiceImplementation implements ProfileAccessTokenService {

    private final ProfileAccessTokenRepository profileAccessTokenRepository;
    private final RedisLockService redisLockService;


    @Override
    public ChannelAccessTokenEntity findTopByRefreshTokenEndTimeIsnUll(String refreshToken) throws InternalServiceException {
        return profileAccessTokenRepository.findTopByRefreshTokenAndEndTimeIsNull(refreshToken).orElseThrow(()->{
            log.error("refreshToken ({}) not exist", refreshToken);
            return new InternalServiceException("refreshToken not found", StatusService.REFRESH_TOKEN_NOT_FOUND, HttpStatus.UNAUTHORIZED);
        });
    }

    @Override
    public ChannelAccessTokenEntity findTopByProfileEntityAndRefreshTokenEndTimeIsnUll(ProfileEntity profileEntity, String refreshToken) {
        return profileAccessTokenRepository.findTopByProfileEntityAndRefreshTokenAndEndTimeIsNull(profileEntity, refreshToken);
    }

    @Override
    public ChannelAccessTokenEntity findTopByProfileEntityAndEndTimeIsnUll(ProfileEntity profileEntity) {
        return profileAccessTokenRepository.findTopByProfileEntityAndEndTimeIsNull(profileEntity);
    }

    @Override
    public void save(ChannelAccessTokenEntity channelAccessTokenEntity) throws InternalServiceException {

        String key = channelAccessTokenEntity.getProfileEntity().getNationalCode();

        redisLockService.runAfterLock(key, this.getClass(), ()->{
            List<ChannelAccessTokenEntity> channelAccessTokenEntityList = profileAccessTokenRepository.findAllByProfileEntityAndEndTimeIsNull(channelAccessTokenEntity.getProfileEntity());
            channelAccessTokenEntityList.forEach(p -> {
                p.setEndTime(new Date());
                profileAccessTokenRepository.save(p);
            });
            channelAccessTokenEntity.setCreatedBy(channelAccessTokenEntity.getProfileEntity().getUsername());
            channelAccessTokenEntity.setCreatedAt(new Date());
            profileAccessTokenRepository.save(channelAccessTokenEntity);
            return null;
        }, key);
    }

    @Override
    public List<ChannelAccessTokenEntity> findAllByProfileEntityAndEndTimeIsNull(ProfileEntity profileEntity) {
        return profileAccessTokenRepository.findAllByProfileEntityAndEndTimeIsNull(profileEntity);
    }
}
