package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.SettingEntity;
import com.melli.hub.domain.master.entity.TempRegisterProfileEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Class Name: TempRegisterProfileRepository
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface TempRegisterProfileRepository extends CrudRepository<TempRegisterProfileEntity, Long> {
    TempRegisterProfileEntity findTopByTempUuidAndNationalCode(String tempUuid, String nationalCode);
    void deleteAllByNationalCode(String nationalCode);
}
