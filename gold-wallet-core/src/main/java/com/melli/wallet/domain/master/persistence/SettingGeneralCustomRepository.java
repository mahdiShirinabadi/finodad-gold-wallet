package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingGeneralCustomRepository extends CrudRepository<SettingGeneralCustomEntity, Long> {

    List<SettingGeneralCustomEntity> findBySettingGeneralEntityAndEndTimeIsNull(SettingGeneralEntity settingGeneralEntity);
    Page<SettingGeneralCustomEntity> findAll(Specification<SettingGeneralCustomEntity> spec, Pageable pageable);
}
