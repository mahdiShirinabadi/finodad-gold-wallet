package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.master.entity.ChannelRoleEntity;
import com.melli.hub.domain.master.entity.RoleEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ChannelRoleRepository extends CrudRepository<ChannelRoleEntity, Long> {
    ChannelRoleEntity findByChannelEntityAndRoleEntity(ChannelEntity channelEntity, RoleEntity role);
}
