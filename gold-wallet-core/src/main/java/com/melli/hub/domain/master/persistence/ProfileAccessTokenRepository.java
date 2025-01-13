package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.ChannelAccessTokenEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ProfileAccessTokenRepository extends CrudRepository<ChannelAccessTokenEntity, Long> {
    Optional<ChannelAccessTokenEntity> findTopByRefreshTokenAndEndTimeIsNull(String refreshToken);
    ChannelAccessTokenEntity findTopByProfileEntityAndRefreshTokenAndEndTimeIsNull(ProfileEntity profileEntity, String refreshToken);
    ChannelAccessTokenEntity findTopByProfileEntityAndEndTimeIsNull(ProfileEntity profileEntity);
    List<ChannelAccessTokenEntity> findAllByProfileEntityAndEndTimeIsNull(ProfileEntity profileEntity);
}
