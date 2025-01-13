package com.melli.hub.service;

import com.melli.hub.exception.InternalServiceException;

/**
 * Class Name: ProfileService
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface ProfileService {
    ProfileEntity findByNationalCode(String nationalCode);
    void ensureNationalCodeUnique(String nationalCode) throws InternalServiceException;
    ProfileEntity ensureNationalCodeExist(String nationalCode) throws InternalServiceException;
    ProfileEntity ensureNationalCodeAndMobileNumberExist(String nationalCode, String mobileNumber) throws InternalServiceException;
    void save(ProfileEntity profileEntity);
}
