package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.ChannelAccessTokenEntity;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Repository
public interface ChannelAccessTokenRepository extends CrudRepository<ChannelAccessTokenEntity, Long> {
    Optional<ChannelAccessTokenEntity> findTopByRefreshTokenAndEndTimeIsNull(String refreshToken);
    ChannelAccessTokenEntity findTopByChannelEntityAndRefreshTokenAndEndTimeIsNull(ChannelEntity channelEntity, String refreshToken);
    ChannelAccessTokenEntity findTopByChannelEntityAndEndTimeIsNull(ChannelEntity channelEntity);
    List<ChannelAccessTokenEntity> findAllByChannelEntityAndEndTimeIsNull(ChannelEntity channelEntity);
    List<ChannelAccessTokenEntity> findAll();
}
