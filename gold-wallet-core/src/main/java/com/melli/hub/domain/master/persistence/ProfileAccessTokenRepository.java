package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.ProfileAccessTokenEntity;
import com.melli.hub.domain.master.entity.ProfileEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ProfileAccessTokenRepository extends CrudRepository<ProfileAccessTokenEntity, Long> {
    Optional<ProfileAccessTokenEntity> findTopByRefreshTokenAndEndTimeIsNull(String refreshToken);
    ProfileAccessTokenEntity findTopByProfileEntityAndRefreshTokenAndEndTimeIsNull(ProfileEntity profileEntity, String refreshToken);
    ProfileAccessTokenEntity findTopByProfileEntityAndEndTimeIsNull(ProfileEntity profileEntity);
    List<ProfileAccessTokenEntity> findAllByProfileEntityAndEndTimeIsNull(ProfileEntity profileEntity);
}
