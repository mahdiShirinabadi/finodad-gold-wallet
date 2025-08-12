package com.melli.wallet.service.repository.impl;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.ChannelRoleEntity;
import com.melli.wallet.domain.master.entity.RoleEntity;
import com.melli.wallet.domain.master.persistence.ChannelRoleRepository;
import com.melli.wallet.domain.master.persistence.RoleRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.RoleRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
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

}
