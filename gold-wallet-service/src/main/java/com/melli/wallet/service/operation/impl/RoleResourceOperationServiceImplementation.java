package com.melli.wallet.service.operation.impl;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.master.entity.RoleEntity;
import com.melli.wallet.domain.master.entity.ResourceEntity;
import com.melli.wallet.domain.master.persistence.RoleRepository;
import com.melli.wallet.domain.master.persistence.ResourceRepository;
import com.melli.wallet.domain.master.persistence.RoleResourceRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.RoleResourceOperationService;
import com.melli.wallet.service.repository.RoleRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class RoleResourceOperationServiceImplementation implements RoleResourceOperationService {
    
    private final RoleRepositoryService roleRepositoryService;
    private final ResourceRepository resourceRepository;
    private final RoleResourceRepository roleResourceRepository;


    @Override
    @Transactional
    @LogExecutionTime("Add resources to role")
    public void addResourcesToRole(Long roleId, List<Long> resourceIds, String updatedBy) throws InternalServiceException {
        log.info("Starting addResourcesToRole - roleId: {}, resourceIds: {}, updatedBy: {}", roleId, resourceIds, updatedBy);
        
        try {
            // Check if role exists
            RoleEntity role = roleRepositoryService.findById(roleId);
            log.info("Role found - roleId: {}, roleName: {}", roleId, role.getName());
            
            // Check if all resources exist
            List<ResourceEntity> resources = new java.util.ArrayList<>();
            resourceRepository.findAllById(resourceIds).forEach(resources::add);
            if (resources.size() != resourceIds.size()) {
                log.error("Resource not found - requested: {}, found: {}", resourceIds.size(), resources.size());
                throw new InternalServiceException(StatusRepositoryService.RESOURCE_NOT_FOUND);
            }
            log.info("All resources found - resourceCount: {}", resources.size());
            
            // First, clear all existing resources for this role
            log.info("Clearing existing resources for roleId: {}", roleId);
            roleResourceRepository.deleteRoleResources(role.getId());
            
            // Then, assign all new resources
            log.info("Assigning new resources to roleId: {} - resourceCount: {}", roleId, resources.size());
            roleResourceRepository.updateRoleResources(role.getId(), new HashSet<>(resources));
            
            log.info("Successfully completed addResourcesToRole - roleId: {}, resourceCount: {}", roleId, resources.size());
            
        } catch (InternalServiceException e) {
            log.error("InternalServiceException in addResourcesToRole - roleId: {}, error: {}", roleId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in addResourcesToRole - roleId: {}, error: {}", roleId, e.getMessage(), e);
            throw new InternalServiceException(StatusRepositoryService.INTERNAL_ERROR);
        }
    }
    

    
    @Override
    @LogExecutionTime("Get role with resources")
    public RoleEntity getRoleWithResources(Long roleId) throws InternalServiceException {
        log.info("Starting getRoleWithResources - roleId: {}", roleId);
        
        try {
            RoleEntity role = roleResourceRepository.findRoleWithResources(roleId);
            if (role == null) {
                log.error("Role not found - roleId: {}", roleId);
                throw new InternalServiceException(StatusRepositoryService.ROLE_NOT_FOUND);
            }
            
            log.info("Successfully completed getRoleWithResources - roleId: {}, roleName: {}, resourceCount: {}", 
                    roleId, role.getName(), role.getResources() != null ? role.getResources().size() : 0);
            return role;
            
        } catch (InternalServiceException e) {
            log.error("InternalServiceException in getRoleWithResources - roleId: {}, error: {}", roleId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in getRoleWithResources - roleId: {}, error: {}", roleId, e.getMessage(), e);
            throw new InternalServiceException(StatusRepositoryService.INTERNAL_ERROR);
        }
    }
    
    @Override
    @LogExecutionTime("Get role resources")
    public Set<ResourceEntity> getRoleResources(Long roleId) throws InternalServiceException {
        log.info("Starting getRoleResources - roleId: {}", roleId);
        
        try {
            // Check if role exists
            RoleEntity role = roleRepositoryService.findById(roleId);
            log.info("Role found - roleId: {}, roleName: {}", roleId, role.getName());
            
            Set<ResourceEntity> resources = roleResourceRepository.findResourcesByRoleId(role.getId());
            log.info("Successfully completed getRoleResources - roleId: {}, resourceCount: {}", roleId, resources.size());
            return resources;
            
        } catch (InternalServiceException e) {
            log.error("InternalServiceException in getRoleResources - roleId: {}, error: {}", roleId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in getRoleResources - roleId: {}, error: {}", roleId, e.getMessage(), e);
            throw new InternalServiceException(StatusRepositoryService.INTERNAL_ERROR);
        }
    }
}
