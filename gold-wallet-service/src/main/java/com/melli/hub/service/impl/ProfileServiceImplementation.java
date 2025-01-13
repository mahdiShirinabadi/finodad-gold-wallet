package com.melli.hub.service.impl;

import com.melli.hub.domain.master.persistence.ProfileAccessTokenRepository;
import com.melli.hub.domain.master.persistence.ProfileRepository;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Class Name: ProfileServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class ProfileServiceImplementation implements ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileAccessTokenRepository profileAccessTokenRepository;

    @Override
    public ProfileEntity findByNationalCode(String nationalCode)  {
        log.info("start findByUsername for username({})", nationalCode);
        return profileRepository.findByNationalCode(nationalCode);
    }

    @Override
    public void ensureNationalCodeUnique(String nationalCode) throws InternalServiceException {
        ProfileEntity profileEntity = profileRepository.findByNationalCode(nationalCode);
        if(profileEntity != null) {
            log.error("profile with nationalCode {} already exists", nationalCode);
            throw new InternalServiceException("profile with nationalCode is exist", StatusServiceImplementation.NATIONAL_CODE_EXIT, HttpStatus.OK);
        }
    }

    @Override
    public ProfileEntity ensureNationalCodeExist(String nationalCode) throws InternalServiceException {
        ProfileEntity profileEntity = profileRepository.findByNationalCode(nationalCode);
        if(profileEntity == null) {
            log.error("profile with nationalCode {} not exists", nationalCode);
            throw new InternalServiceException("profile with nationalCode is not exist", StatusServiceImplementation.GENERAL_ERROR, HttpStatus.OK);
        }
        return profileEntity;
    }

    @Override
    public ProfileEntity ensureNationalCodeAndMobileNumberExist(String nationalCode, String mobileNumber) throws InternalServiceException {

        ProfileEntity profileEntity = profileRepository.findByNationalCode(nationalCode);

        if(profileEntity == null) {
            log.error("profile with nationalCode {} not exists", nationalCode);
            throw new InternalServiceException("profile with nationalCode is not exist", StatusServiceImplementation.PROFILE_NOT_FOUND, HttpStatus.OK);
        }

        if(!profileEntity.getMobile().equalsIgnoreCase(mobileNumber)) {
            log.error("profile with nationalCode {}, mobileNumber ({}) not exists", nationalCode, mobileNumber);
            throw new InternalServiceException("profile with nationalCode is not exist", StatusServiceImplementation.PROFILE_NOT_FOUND, HttpStatus.OK);
        }
        return profileEntity;
    }

    @Override
    public void save(ProfileEntity profileEntity) {
        profileRepository.save(profileEntity);
    }
}
