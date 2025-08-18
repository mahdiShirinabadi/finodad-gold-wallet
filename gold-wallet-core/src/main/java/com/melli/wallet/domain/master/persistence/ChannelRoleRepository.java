package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.ChannelRoleEntity;
import com.melli.wallet.domain.master.entity.RoleEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Repository
public interface ChannelRoleRepository extends CrudRepository<ChannelRoleEntity, Long> {
    ChannelRoleEntity findByChannelEntityAndRoleEntity(ChannelEntity channelEntity, RoleEntity role);

    @Query("SELECT cr FROM ChannelRoleEntity cr WHERE cr.channelEntity.id = :channelId AND cr.roleEntity.id = :roleId")
    Optional<ChannelRoleEntity> findByChannelIdAndRoleId(@Param("channelId") Long channelId, @Param("roleId") Long roleId);

    @Query("SELECT cr FROM ChannelRoleEntity cr WHERE cr.channelEntity.id = :channelId")
    List<ChannelRoleEntity> findByChannelId(@Param("channelId") Long channelId);

    @Query("SELECT cr FROM ChannelRoleEntity cr WHERE cr.roleEntity.id = :roleId")
    List<ChannelRoleEntity> findByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT cr FROM ChannelRoleEntity cr INNER JOIN FETCH cr.channelEntity INNER JOIN FETCH cr.roleEntity")
    List<ChannelRoleEntity> findAllWithChannelAndRole();
}
