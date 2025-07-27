package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportChannelEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Class Name: ReportProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Repository
public interface ReportChannelRepository extends CrudRepository<ReportChannelEntity, Long> {
    ReportChannelEntity findByUsername(String username);
} 