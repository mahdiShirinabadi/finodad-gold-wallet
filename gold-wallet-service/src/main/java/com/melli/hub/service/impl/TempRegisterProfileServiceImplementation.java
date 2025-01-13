package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.TempRegisterProfileEntity;
import com.melli.hub.domain.master.persistence.TempRegisterProfileRepository;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.StatusService;
import com.melli.hub.service.TempRegisterProfileService;
import com.melli.hub.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class Name: TempRegisterProfileServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class TempRegisterProfileServiceImplementation implements TempRegisterProfileService {
    private final TempRegisterProfileRepository tempRegisterProfileRepository;

    @Override
    public TempRegisterProfileEntity findByTempUuidAndNationalCode(String tempUuid, String nationalCode) throws InternalServiceException {
        TempRegisterProfileEntity tempRegisterProfileEntity = tempRegisterProfileRepository.findTopByTempUuidAndNationalCode(tempUuid, nationalCode);
        if(tempRegisterProfileEntity == null) {
            log.error("tempuuid ({}) with nationalCode ({}) not found", tempUuid, nationalCode);
            throw new InternalServiceException("tempUuid for nationalCode not found", StatusService.GENERAL_ERROR, HttpStatus.FORBIDDEN);
        }
        return tempRegisterProfileEntity;
    }

    @Override
    public void save(TempRegisterProfileEntity tempRegisterProfileEntity) {
        tempRegisterProfileRepository.save(tempRegisterProfileEntity);
    }

    @Override
    @Async
    @Transactional
    public void deleteByNationalCode(String nationalCode) throws InternalServiceException {
        log.info("delete all record for nationalCode {}", nationalCode);
        tempRegisterProfileRepository.deleteAllByNationalCode(nationalCode);
    }
}
