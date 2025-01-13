package com.melli.hub.service;

import com.melli.hub.domain.master.entity.TempRegisterProfileEntity;
import com.melli.hub.exception.InternalServiceException;

/**
 * Class Name: TempRegisterProfileService
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface TempRegisterProfileService {
    TempRegisterProfileEntity findByTempUuidAndNationalCode(String tempUuid, String nationalCode)  throws InternalServiceException;
    void save(TempRegisterProfileEntity tempRegisterProfileEntity);
    void deleteByNationalCode(String nationalCode) throws InternalServiceException;
}
