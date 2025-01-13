package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.ResourceEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Class Name: ProfileAccessToken
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ResourceRepository extends CrudRepository<ResourceEntity, Long> {
    List<ResourceEntity> findAll();
    ResourceEntity findByName(String name);
}
