package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.RoleEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
}
