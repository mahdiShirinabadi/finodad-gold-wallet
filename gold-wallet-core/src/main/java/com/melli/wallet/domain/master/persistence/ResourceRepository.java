package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.ResourceEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Repository
public interface ResourceRepository extends CrudRepository<ResourceEntity, Long> {
    List<ResourceEntity> findAll();
    ResourceEntity findByName(String name);
}
