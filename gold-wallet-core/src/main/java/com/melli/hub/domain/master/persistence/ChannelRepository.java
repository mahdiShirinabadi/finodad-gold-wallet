package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.ChannelEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ChannelRepository extends CrudRepository<ChannelEntity, Long> {
    ChannelEntity findByUsername(String username);
}
