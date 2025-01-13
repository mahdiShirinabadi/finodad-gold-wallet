package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.ProfileAccessTokenEntity;
import com.melli.hub.domain.master.entity.ProfileEntity;
import com.melli.hub.domain.master.persistence.ProfileAccessTokenRepository;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.ProfileAccessTokenService;
import com.melli.hub.service.StatusService;
import com.melli.hub.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.melli.hub.utils.Constant.SETTING_NAME_CACHE;

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
    public ProfileAccessTokenEntity findTopByRefreshTokenEndTimeIsnUll(String refreshToken) throws InternalServiceException {
        return profileAccessTokenRepository.findTopByRefreshTokenAndEndTimeIsNull(refreshToken).orElseThrow(()->{
            log.error("refreshToken ({}) not exist", refreshToken);
            return new InternalServiceException("refreshToken not found", StatusService.REFRESH_TOKEN_NOT_FOUND, HttpStatus.UNAUTHORIZED);
        });
    }

    @Override
    public ProfileAccessTokenEntity findTopByProfileEntityAndRefreshTokenEndTimeIsnUll(ProfileEntity profileEntity, String refreshToken) {
        return profileAccessTokenRepository.findTopByProfileEntityAndRefreshTokenAndEndTimeIsNull(profileEntity, refreshToken);
    }

    @Override
    public ProfileAccessTokenEntity findTopByProfileEntityAndEndTimeIsnUll(ProfileEntity profileEntity) {
        return profileAccessTokenRepository.findTopByProfileEntityAndEndTimeIsNull(profileEntity);
    }

    @Override
    public void save(ProfileAccessTokenEntity profileAccessTokenEntity) throws InternalServiceException {

        String key = profileAccessTokenEntity.getProfileEntity().getNationalCode();

        redisLockService.runAfterLock(key, this.getClass(), ()->{
            List<ProfileAccessTokenEntity> profileAccessTokenEntityList = profileAccessTokenRepository.findAllByProfileEntityAndEndTimeIsNull(profileAccessTokenEntity.getProfileEntity());
            profileAccessTokenEntityList.forEach(p -> {
                p.setEndTime(new Date());
                profileAccessTokenRepository.save(p);
            });
            profileAccessTokenEntity.setCreatedBy(profileAccessTokenEntity.getProfileEntity().getUsername());
            profileAccessTokenEntity.setCreatedAt(new Date());
            profileAccessTokenRepository.save(profileAccessTokenEntity);
            return null;
        }, key);
    }

    @Override
    public List<ProfileAccessTokenEntity> findAllByProfileEntityAndEndTimeIsNull(ProfileEntity profileEntity) {
        return profileAccessTokenRepository.findAllByProfileEntityAndEndTimeIsNull(profileEntity);
    }
}
