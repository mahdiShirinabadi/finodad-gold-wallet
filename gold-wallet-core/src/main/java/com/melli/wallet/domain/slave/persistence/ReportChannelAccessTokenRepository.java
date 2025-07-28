package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportChannelAccessTokenEntity;
import com.melli.wallet.domain.slave.entity.ReportChannelEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Class Name: ReportProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Repository
public interface ReportChannelAccessTokenRepository extends CrudRepository<ReportChannelAccessTokenEntity, Long> {
    Optional<ReportChannelAccessTokenEntity> findTopByRefreshTokenAndEndTimeIsNull(String refreshToken);
    ReportChannelAccessTokenEntity findTopByChannelEntityAndRefreshTokenAndEndTimeIsNull(ReportChannelEntity channelEntity, String refreshToken);
    ReportChannelAccessTokenEntity findTopByChannelEntityAndEndTimeIsNull(ReportChannelEntity channelEntity);
    List<ReportChannelAccessTokenEntity> findAllByChannelEntityAndEndTimeIsNull(ReportChannelEntity channelEntity);
    List<ReportChannelAccessTokenEntity> findAll();
} 