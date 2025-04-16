package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.ChannelRoleEntity;
import com.melli.wallet.domain.master.entity.RoleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Repository
public interface ChannelRoleRepository extends CrudRepository<ChannelRoleEntity, Long> {
    ChannelRoleEntity findByChannelEntityAndRoleEntity(ChannelEntity channelEntity, RoleEntity role);
}
