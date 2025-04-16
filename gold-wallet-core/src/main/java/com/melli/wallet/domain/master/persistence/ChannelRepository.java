package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Repository
public interface ChannelRepository extends CrudRepository<ChannelEntity, Long> {
    ChannelEntity findByUsername(String username);
}
