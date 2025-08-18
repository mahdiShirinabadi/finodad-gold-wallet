package com.melli.wallet.service.operation.impl;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.master.entity.RoleEntity;
import com.melli.wallet.domain.master.entity.ResourceEntity;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.ChannelRoleEntity;
import com.melli.wallet.domain.master.persistence.ChannelRoleRepository;
import com.melli.wallet.domain.request.panel.RoleCreateRequestJson;
import com.melli.wallet.domain.request.panel.RoleUpdateRequestJson;
import com.melli.wallet.domain.request.panel.ResourceCreateRequestJson;
import com.melli.wallet.domain.request.panel.ResourceUpdateRequestJson;
import com.melli.wallet.domain.response.panel.RoleDetailResponse;
import com.melli.wallet.domain.response.panel.ResourceDetailResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.RoleManagementOperationService;
import com.melli.wallet.service.operation.RoleResourceOperationService;
import com.melli.wallet.service.repository.ChannelRepositoryService;
import com.melli.wallet.service.repository.ResourceRepositoryService;
import com.melli.wallet.service.repository.RoleRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class RoleManagementOperationServiceImplementation implements RoleManagementOperationService {
    
    private final RoleRepositoryService roleRepositoryService;
    private final ResourceRepositoryService resourceRepositoryService;
    private final ChannelRepositoryService channelRepositoryService;
    private final ChannelRoleRepository channelRoleRepository;
    private final RoleResourceOperationService roleResourceOperationService;

    // Role CRUD operations
    @Override
    @LogExecutionTime("Create role")
    public RoleDetailResponse createRole(RoleCreateRequestJson requestJson, String createdBy) throws InternalServiceException {
        return roleRepositoryService.createRole(requestJson, createdBy);
    }
    
    @Override
    @LogExecutionTime("Update role")
    public RoleDetailResponse updateRole(Long roleId, RoleUpdateRequestJson requestJson, String updatedBy) throws InternalServiceException {
        return roleRepositoryService.updateRole(roleId, requestJson, updatedBy);
    }
    
    @Override
    @LogExecutionTime("Delete role")
    public void deleteRole(Long roleId) throws InternalServiceException {
        roleRepositoryService.deleteRole(roleId);
    }
    
    // Resource CRUD operations
    @Override
    @LogExecutionTime("Create resource")
    public ResourceDetailResponse createResource(ResourceCreateRequestJson requestJson, String createdBy) throws InternalServiceException {
        return resourceRepositoryService.createResource(requestJson, createdBy);
    }
    
    @Override
    @LogExecutionTime("Update resource")
    public ResourceDetailResponse updateResource(Long resourceId, ResourceUpdateRequestJson requestJson, String updatedBy) throws InternalServiceException {
        return resourceRepositoryService.updateResource(resourceId, requestJson, updatedBy);
    }
    
    @Override
    @LogExecutionTime("Delete resource")
    public void deleteResource(Long resourceId) throws InternalServiceException {
        resourceRepositoryService.deleteResource(resourceId);
    }
    
    // Role-Resource management
    @Override
    @Transactional
    @LogExecutionTime("Assign resources to role")
    public void assignResourcesToRole(Long roleId, List<Long> resourceIds, String updatedBy) throws InternalServiceException {
        // Get role entity from repository
        RoleEntity role = roleRepositoryService.findById(roleId);

        // Get resources from repository
        List<ResourceEntity> resources = resourceRepositoryService.findAllByIds(resourceIds);
        if (resources.size() != resourceIds.size()) {
            throw new InternalServiceException(StatusRepositoryService.RESOURCE_NOT_FOUND);
        }
        
        // Clear existing resources and assign new ones
        role.setResources(new HashSet<>(resources));
        roleResourceOperationService.addResourcesToRole(String.valueOf(roleId), resourceIds.stream().map(String::valueOf).toList(), updatedBy);
    }


    // Role-Channel management
    @Override
    @Transactional
    @LogExecutionTime("Assign role to channel")
    public void assignRoleToChannel(Long roleId, Long channelId, String createdBy) throws InternalServiceException {
        // Get role entity from repository
        RoleEntity role = roleRepositoryService.findById(roleId);
        
        // Get channel and validate it exists
        ChannelEntity channel = channelRepositoryService.findById(channelId);
        if (channel == null) {
            throw new InternalServiceException(StatusRepositoryService.CHANNEL_NOT_FOUND);
        }
        
        // Check if role is already assigned to channel
        ChannelRoleEntity existingAssignment = channelRoleRepository.findByChannelEntityAndRoleEntity(channel, role);
        if (existingAssignment != null) {
            throw new InternalServiceException(StatusRepositoryService.ROLE_ALREADY_ASSIGNED);
        }
        
        // Create new channel-role assignment
        ChannelRoleEntity channelRole = new ChannelRoleEntity();
        channelRole.setRoleEntity(role);
        channelRole.setChannelEntity(channel);
        channelRoleRepository.save(channelRole);
    }
    

    // Get operations
    @Override
    @LogExecutionTime("Get role with resources")
    public RoleEntity getRoleWithResources(Long roleId) throws InternalServiceException {
        return roleRepositoryService.findById(roleId);
    }
    
    @Override
    @LogExecutionTime("Get role resources")
    public Set<ResourceEntity> getRoleResources(Long roleId) throws InternalServiceException {
        RoleEntity role = roleRepositoryService.findById(roleId);
        return role.getResources();
    }
}
