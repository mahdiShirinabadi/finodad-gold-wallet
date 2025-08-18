package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.RoleEntity;
import com.melli.wallet.domain.master.entity.ResourceEntity;
import com.melli.wallet.domain.request.panel.RoleCreateRequestJson;
import com.melli.wallet.domain.request.panel.RoleUpdateRequestJson;
import com.melli.wallet.domain.request.panel.ResourceCreateRequestJson;
import com.melli.wallet.domain.request.panel.ResourceUpdateRequestJson;
import com.melli.wallet.domain.response.panel.RoleDetailResponse;
import com.melli.wallet.domain.response.panel.ResourceDetailResponse;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface RoleManagementOperationService {
    
    // Role CRUD operations
    RoleDetailResponse createRole(RoleCreateRequestJson requestJson, String createdBy) throws InternalServiceException;
    
    RoleDetailResponse updateRole(Long roleId, RoleUpdateRequestJson requestJson, String updatedBy) throws InternalServiceException;
    
    void deleteRole(Long roleId) throws InternalServiceException;
    
    // Resource CRUD operations
    ResourceDetailResponse createResource(ResourceCreateRequestJson requestJson, String createdBy) throws InternalServiceException;
    
    ResourceDetailResponse updateResource(Long resourceId, ResourceUpdateRequestJson requestJson, String updatedBy) throws InternalServiceException;
    
    void deleteResource(Long resourceId) throws InternalServiceException;
    
    // Role-Resource management
    @Transactional
    void assignResourcesToRole(Long roleId, List<Long> resourceIds, String updatedBy) throws InternalServiceException;
    
    // Role-Channel management
    @Transactional
    void assignRoleToChannel(Long roleId, Long channelId, String createdBy) throws InternalServiceException;

    // Get operations
    RoleEntity getRoleWithResources(Long roleId) throws InternalServiceException;
    
    Set<ResourceEntity> getRoleResources(Long roleId) throws InternalServiceException;
}
