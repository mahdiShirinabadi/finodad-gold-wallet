package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.SettingGeneralEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingGeneralRepository extends CrudRepository<SettingGeneralEntity, Long> {

    SettingGeneralEntity findByNameAndEndTimeIsNull(String name);

    Page<SettingGeneralEntity> findAll(Specification<SettingGeneralEntity> spec, Pageable pageable);
}
