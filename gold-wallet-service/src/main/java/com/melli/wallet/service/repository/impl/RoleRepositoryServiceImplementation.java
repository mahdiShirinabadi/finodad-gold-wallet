package com.melli.wallet.service.repository.impl;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.ChannelRoleEntity;
import com.melli.wallet.domain.master.entity.RoleEntity;
import com.melli.wallet.domain.master.persistence.ChannelRoleRepository;
import com.melli.wallet.domain.master.persistence.RoleRepository;
import com.melli.wallet.domain.request.panel.RoleCreateRequestJson;
import com.melli.wallet.domain.request.panel.RoleUpdateRequestJson;
import com.melli.wallet.domain.response.panel.RoleDetailResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.RoleRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
 * Class Name: RoleServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/5/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class RoleRepositoryServiceImplementation implements RoleRepositoryService {

    private final ChannelRoleRepository channelRoleRepository;
    private final RoleRepository roleRepository;


    @Override
    public void addChannelToRole(ChannelEntity channelEntity, String roleName) throws InternalServiceException {
        RoleEntity roleEntity = roleRepository.findByName(roleName).orElseThrow(() -> new InternalServiceException("role name not found", StatusRepositoryService.ROLE_NAME_NOT_FOUND, HttpStatus.OK));
        ChannelRoleEntity channelRoleEntity = new ChannelRoleEntity();
        channelRoleEntity.setRoleEntity(roleEntity);
        channelRoleEntity.setChannelEntity(channelEntity);
        channelRoleEntity.setCreatedAt(new Date());
        channelRoleEntity.setCreatedBy(channelEntity.getUsername());
        channelRoleRepository.save(channelRoleEntity);
    }

    @Override
    @Transactional
    public void removeChannelFromRole(ChannelEntity channelEntity, String roleName) throws InternalServiceException {
        RoleEntity roleEntity = roleRepository.findByName(roleName).orElseThrow(() -> new InternalServiceException("role name not found", StatusRepositoryService.ROLE_NAME_NOT_FOUND, HttpStatus.OK));
        ChannelRoleEntity channelRoleEntity = channelRoleRepository.findByChannelEntityAndRoleEntity(channelEntity, roleEntity);
        channelRoleRepository.delete(channelRoleEntity);
    }

    @Override
    public RoleEntity findById(Long roleId) throws InternalServiceException {
        return roleRepository.findById(roleId).orElseThrow(()-> new InternalServiceException(StatusRepositoryService.ROLE_NOT_FOUND));
    }

    @Override
    public Optional<RoleEntity> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public RoleEntity save(RoleEntity roleEntity) {
        return roleRepository.save(roleEntity);
    }

    @Override
    @Transactional
    @LogExecutionTime("Create role")
    public RoleDetailResponse createRole(RoleCreateRequestJson requestJson, String createdBy) throws InternalServiceException {
        // Check if role with same name already exists
        Optional<RoleEntity> existingRole = roleRepository.findByName(requestJson.getName());
        if (existingRole.isPresent()) {
            throw new InternalServiceException(StatusRepositoryService.ROLE_ALREADY_EXISTS);
        }

        // Create new role
        RoleEntity role = new RoleEntity();
        role.setName(requestJson.getName());
        role.setPersianDescription(requestJson.getPersianDescription());
        role.setAdditionalData(requestJson.getAdditionalData());
        role.setEndTime(requestJson.getEndTime());
        role.setCreatedBy(createdBy);
        role.setCreatedAt(new Date());

        RoleEntity savedRole = roleRepository.save(role);

        return mapToRoleDetailResponse(savedRole);
    }

    @Override
    @Transactional
    @LogExecutionTime("Update role")
    public RoleDetailResponse updateRole(Long roleId, RoleUpdateRequestJson requestJson, String updatedBy) throws InternalServiceException {
        // Check if role exists
        RoleEntity role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            throw new InternalServiceException(StatusRepositoryService.ROLE_NOT_FOUND);
        }

        // Check if name is being changed and if new name already exists
        if (!role.getName().equals(requestJson.getName())) {
            Optional<RoleEntity> existingRole = roleRepository.findByName(requestJson.getName());
            if (existingRole.isPresent()) {
                throw new InternalServiceException(StatusRepositoryService.ROLE_ALREADY_EXISTS);
            }
        }

        // Update role
        role.setName(requestJson.getName());
        role.setPersianDescription(requestJson.getPersianDescription());
        role.setAdditionalData(requestJson.getAdditionalData());
        role.setEndTime(requestJson.getEndTime());
        role.setUpdatedBy(updatedBy);
        role.setUpdatedAt(new Date());

        RoleEntity savedRole = roleRepository.save(role);

        return mapToRoleDetailResponse(savedRole);
    }

    @Override
    @Transactional
    @LogExecutionTime("Delete role")
    public void deleteRole(Long roleId) throws InternalServiceException {
        // Check if role exists
        RoleEntity role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            throw new InternalServiceException(StatusRepositoryService.ROLE_NOT_FOUND);
        }

        // Check if role is in use (has resources or channel assignments)
        if (role.getResources() != null && !role.getResources().isEmpty()) {
            throw new InternalServiceException(StatusRepositoryService.ROLE_IN_USE);
        }

        roleRepository.delete(role);
    }

    @Override
    @LogExecutionTime("Get role by ID")
    public RoleDetailResponse getRoleById(Long roleId) throws InternalServiceException {
        RoleEntity role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            throw new InternalServiceException(StatusRepositoryService.ROLE_NOT_FOUND);
        }

        return mapToRoleDetailResponse(role);
    }

    @Override
    @LogExecutionTime("List roles")
    public Page<RoleDetailResponse> listRoles(Pageable pageable) throws InternalServiceException {
        Page<RoleEntity> roles = roleRepository.findAll(pageable);

        return roles.map(this::mapToRoleDetailResponse);
    }

    private RoleDetailResponse mapToRoleDetailResponse(RoleEntity role) {
        return RoleDetailResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .persianDescription(role.getPersianDescription())
                .additionalData(role.getAdditionalData())
                .endTime(role.getEndTime())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

}
