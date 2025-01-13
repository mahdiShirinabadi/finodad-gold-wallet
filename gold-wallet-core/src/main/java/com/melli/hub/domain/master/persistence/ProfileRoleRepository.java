package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.ProfileEntity;
import com.melli.hub.domain.master.entity.ProfileRoleEntity;
import com.melli.hub.domain.master.entity.RoleEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ProfileRoleRepository extends CrudRepository<ProfileRoleEntity, Long> {
    ProfileRoleEntity findByProfileEntityAndRoleEntity(ProfileEntity profile, RoleEntity role);
}
