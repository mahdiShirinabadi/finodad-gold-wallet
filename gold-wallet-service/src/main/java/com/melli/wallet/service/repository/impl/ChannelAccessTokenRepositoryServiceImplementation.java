package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.entity.ChannelAccessTokenEntity;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.persistence.ChannelAccessTokenRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.ChannelAccessTokenRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.utils.RedisLockService;
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
public class ChannelAccessTokenRepositoryServiceImplementation implements ChannelAccessTokenRepositoryService {

    private final ChannelAccessTokenRepository channelAccessTokenRepository;
    private final RedisLockService redisLockService;


    @Override
    public ChannelAccessTokenEntity findTopByRefreshTokenEndTimeIsnUll(String refreshToken) throws InternalServiceException {
        return channelAccessTokenRepository.findTopByRefreshTokenAndEndTimeIsNull(refreshToken).orElseThrow(()->{
            log.error("refreshToken ({}) not exist", refreshToken);
            return new InternalServiceException("refreshToken not found", StatusRepositoryService.REFRESH_TOKEN_NOT_FOUND, HttpStatus.UNAUTHORIZED);
        });
    }


    @Override
    public ChannelAccessTokenEntity findTopByChannelEntityAndEndTimeIsnUll(ChannelEntity channelEntity) {
        return channelAccessTokenRepository.findTopByChannelEntityAndEndTimeIsNull(channelEntity);
    }

    @Override
    public void save(ChannelAccessTokenEntity channelAccessTokenEntity) throws InternalServiceException {

        String key = channelAccessTokenEntity.getChannelEntity().getUsername();

        redisLockService.runAfterLock(key, this.getClass(), ()->{
            List<ChannelAccessTokenEntity> channelAccessTokenEntityList = channelAccessTokenRepository.findAllByChannelEntityAndEndTimeIsNull(channelAccessTokenEntity.getChannelEntity());
            channelAccessTokenEntityList.forEach(p -> {
                p.setEndTime(new Date());
                channelAccessTokenRepository.save(p);
            });
            channelAccessTokenEntity.setCreatedBy(channelAccessTokenEntity.getChannelEntity().getUsername());
            channelAccessTokenEntity.setCreatedAt(new Date());
            channelAccessTokenRepository.save(channelAccessTokenEntity);
            return null;
        }, key);
    }

    @Override
    public List<ChannelAccessTokenEntity> findAllByChannelEntityAndEndTimeIsNull(ChannelEntity channelEntity) {
        return channelAccessTokenRepository.findAll();
    }

}
