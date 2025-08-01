package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportChannelEntity;
import com.melli.wallet.domain.slave.entity.ReportChannelRoleEntity;
import com.melli.wallet.domain.slave.entity.ReportRoleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Class Name: ReportProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Repository
public interface ReportChannelRoleRepository extends CrudRepository<ReportChannelRoleEntity, Long> {
    ReportChannelRoleEntity findByChannelEntityAndRoleEntity(ReportChannelEntity channelEntity, ReportRoleEntity role);
} 