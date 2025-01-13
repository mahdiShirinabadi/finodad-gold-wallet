package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.SettingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends CrudRepository<SettingEntity, Long> {

    SettingEntity findByNameAndEndTimeIsNull(String name);

    Page<SettingEntity> findAll(Specification<SettingEntity> spec, Pageable pageable);
}
