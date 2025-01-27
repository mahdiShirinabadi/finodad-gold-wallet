package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.master.entity.SettingGeneralCustomEntity;
import com.melli.hub.domain.master.entity.SettingGeneralEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingGeneralCustomRepository extends CrudRepository<SettingGeneralCustomEntity, Long> {

    SettingGeneralCustomEntity findBySettingGeneralEntityAndEndTimeIsNull(SettingGeneralEntity settingGeneralEntity);

    List<SettingGeneralCustomEntity> findBySettingGeneralEntityAndChannelEntityAndEndTimeIsNull(SettingGeneralEntity settingGeneralEntity, ChannelEntity channelEntity);

    Page<SettingGeneralEntity> findAll(Specification<SettingGeneralEntity> spec, Pageable pageable);
}
