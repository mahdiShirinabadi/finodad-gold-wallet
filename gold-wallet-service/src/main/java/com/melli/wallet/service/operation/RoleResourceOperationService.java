package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.RoleEntity;
import com.melli.wallet.domain.master.entity.ResourceEntity;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface RoleResourceOperationService {
    

    /**
     * Add resources to role (keep existing resources and add new ones)
     */
    @Transactional
    void addResourcesToRole(String roleId, List<String> resourceIds, String updatedBy) throws InternalServiceException;

    /**
     * Get role with resources
     */
    RoleEntity getRoleWithResources(Long roleId) throws InternalServiceException;
    
    /**
     * Get role resources
     */
    Set<ResourceEntity> getRoleResources(Long roleId) throws InternalServiceException;
}
