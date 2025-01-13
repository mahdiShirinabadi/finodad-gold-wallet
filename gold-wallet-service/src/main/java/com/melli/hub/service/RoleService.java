package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ProfileEntity;
import com.melli.hub.exception.InternalServiceException;

/**
 * Class Name: RoleService
 * Author: Mahdi Shirinabadi
 * Date: 1/5/2025
 */
public interface RoleService {

    String WEB_PROFILE = "WEB_PROFILE";

    void addProfileToRole(ProfileEntity profileEntity, String roleName) throws InternalServiceException;
    void removeProfileFromRole(ProfileEntity profileEntity, String roleName) throws InternalServiceException;
}
