package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.ProfileEntity;
import com.melli.hub.domain.master.entity.ProfileRoleEntity;
import com.melli.hub.domain.master.entity.RoleEntity;
import com.melli.hub.domain.master.persistence.ProfileRoleRepository;
import com.melli.hub.domain.master.persistence.RoleRepository;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.RoleService;
import com.melli.hub.service.StatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Class Name: RoleServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/5/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class RoleServiceImplementation implements RoleService {

    private final ProfileRoleRepository profileRoleRepository;
    private final RoleRepository roleRepository;


    @Override
    public void addProfileToRole(ProfileEntity profileEntity, String roleName) throws InternalServiceException {
        RoleEntity roleEntity = roleRepository.findByName(roleName).orElseThrow(() -> new InternalServiceException("role name not found", StatusService.ROLE_NAME_NOT_FOUND, HttpStatus.OK));
        ProfileRoleEntity profileRoleEntity = new ProfileRoleEntity();
        profileRoleEntity.setRoleEntity(roleEntity);
        profileRoleEntity.setProfileEntity(profileEntity);
        profileRoleEntity.setCreatedAt(new Date());
        profileRoleEntity.setCreatedBy(profileEntity.getUsername());
        profileRoleRepository.save(profileRoleEntity);
    }

    @Override
    @Transactional
    public void removeProfileFromRole(ProfileEntity profileEntity, String roleName) throws InternalServiceException {
        RoleEntity roleEntity = roleRepository.findByName(roleName).orElseThrow(() -> new InternalServiceException("role name not found", StatusService.ROLE_NAME_NOT_FOUND, HttpStatus.OK));
        ProfileRoleEntity profileRoleEntity = profileRoleRepository.findByProfileEntityAndRoleEntity(profileEntity, roleEntity);
        profileRoleRepository.delete(profileRoleEntity);
    }

}
