package com.melli.wallet.sync;

import com.melli.wallet.domain.master.entity.ResourceEntity;
import com.melli.wallet.domain.master.entity.RoleEntity;
import com.melli.wallet.domain.master.persistence.ResourceRepository;
import com.melli.wallet.domain.master.persistence.RoleResourceRepository;
import com.melli.wallet.domain.master.persistence.ChannelRepository;
import com.melli.wallet.domain.master.persistence.ChannelRoleRepository;
import com.melli.wallet.domain.master.entity.RoleResourceEntity;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.ChannelRoleEntity;
import com.melli.wallet.domain.enumaration.ResourceDefinition;
import com.melli.wallet.service.repository.RoleRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service to automatically sync resources from constants to database
 * and assign them to GOD role on application startup
 * Uses reflection to automatically discover all resource constants
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class ResourceSyncService {

    private final ResourceRepository resourceRepository;
    private final RoleRepositoryService roleRepositoryService;
    private final RoleResourceRepository roleResourceRepository;
    private final ChannelRepository channelRepository;
    private final ChannelRoleRepository channelRoleRepository;

    /**
     * Get Persian description from existing resource or from ResourceDefinition
     */
    private String getPersianDescription(String resourceName, Map<String, ResourceEntity> existingResourceMap) {
        // First try to get from existing resource in database
        ResourceEntity existingResource = existingResourceMap.get(resourceName);
        if (existingResource != null && existingResource.getFaName() != null && !existingResource.getFaName().trim().isEmpty()) {
            return existingResource.getFaName();
        }
        
        // If not found in database, get from ResourceDefinition
        return ResourceDefinition.getPersianDescription(resourceName);
    }

    /**
     * Sync resources on application startup using reflection
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncResourcesOnStartup() {
        log.info("Starting resource synchronization using reflection...");
        
        try {
            // Get or create GOD role
            RoleEntity godRole = roleRepositoryService.findByName(RoleRepositoryService.GOD_ROLE).orElse(null);
            if (godRole == null) {
                log.info("GOD role not found. Creating GOD role...");
                godRole = createGodRole();
                log.info("GOD role created successfully with ID: {}", godRole.getId());
            }

            // Get existing resources from database
            List<ResourceEntity> existingResources = resourceRepository.findAll();
            Map<String, ResourceEntity> existingResourceMap = existingResources.stream()
                    .collect(Collectors.toMap(ResourceEntity::getName, resource -> resource));

            // Get existing role-resource assignments for GOD role
            final long godRoleId = godRole.getId();
            List<RoleResourceEntity> existingRoleResources = roleResourceRepository.findAll().stream()
                    .filter(rr -> rr.getRoleEntity().getId() == godRoleId)
                    .toList();
            Map<Long, RoleResourceEntity> existingRoleResourceMap = existingRoleResources.stream()
                    .collect(Collectors.toMap(rr -> rr.getResourceEntity().getId(), rr -> rr));

            // Get all resource constants using reflection
            List<String> resourceNames = getResourceNamesFromReflection();
            
            int newResourcesCount = 0;
            int newAssignmentsCount = 0;

            // Process each resource name
            for (String resourceName : resourceNames) {
                ResourceEntity resource = existingResourceMap.get(resourceName);
                
                // Create resource if it doesn't exist
                if (resource == null) {
                    String description = getPersianDescription(resourceName, existingResourceMap);
                    resource = createResource(resourceName, description);
                    existingResourceMap.put(resourceName, resource);
                    newResourcesCount++;
                    log.info("Created new resource: {}", resourceName);
                }

                // Assign to GOD role if not already assigned
                if (!existingRoleResourceMap.containsKey(resource.getId())) {
                    assignResourceToGodRole(resource, godRole);
                    existingRoleResourceMap.put(resource.getId(), null); // Mark as assigned
                    newAssignmentsCount++;
                    log.info("Assigned resource to GOD role: {}", resourceName);
                }
            }

            log.info("Resource synchronization completed. New resources: {}, New assignments: {}", 
                    newResourcesCount, newAssignmentsCount);

            // Assign GOD role to admin user if exists
            assignGodRoleToAdmin(godRole);

        } catch (Exception e) {
            log.error("Error during resource synchronization", e);
        }
    }

    /**
     * Get all resource names directly from ResourceDefinition enum
     */
    private List<String> getResourceNamesFromReflection() {
        List<String> resourceNames = new ArrayList<>();
        
        try {
            // Get all enum values from ResourceDefinition
            ResourceDefinition[] enumValues = ResourceDefinition.values();
            
            for (ResourceDefinition resource : enumValues) {
                String resourceName = resource.getName();
                resourceNames.add(resourceName);
                log.info("Discovered resource from enum: {}", resourceName);
            }
            
            log.info("Discovered {} resources from ResourceDefinition enum", resourceNames.size());
            
        } catch (Exception e) {
            log.error("Error discovering resources from ResourceDefinition enum", e);
        }
        
        return resourceNames;
    }

    /**
     * Create a new resource
     */
    private ResourceEntity createResource(String resourceName, String description) {
        ResourceEntity resource = new ResourceEntity();
        resource.setName(resourceName);
        resource.setFaName(description);
        resource.setDisplay(1);
        resource.setCreatedBy("system");
        resource.setCreatedAt(new Date());
        
        return resourceRepository.save(resource);
    }

    /**
     * Create GOD role
     */
    private RoleEntity createGodRole() {
        RoleEntity godRole = new RoleEntity();
        godRole.setName(RoleRepositoryService.GOD_ROLE);
        godRole.setPersianDescription("دسترسی کامل به تمام امکانات");
        godRole.setAdditionalData("GOD_PERMISSION");
        godRole.setCreatedBy("system");
        godRole.setCreatedAt(new Date());
        
        return roleRepositoryService.save(godRole);
    }

    /**
     * Assign resource to GOD role
     */
    private void assignResourceToGodRole(ResourceEntity resource, RoleEntity godRole) {
        RoleResourceEntity roleResource = new RoleResourceEntity();
        roleResource.setRoleEntity(godRole);
        roleResource.setResourceEntity(resource);
        roleResource.setCreatedBy("system");
        roleResource.setCreatedAt(new Date());
        
        roleResourceRepository.save(roleResource);
    }

    /**
     * Assign GOD role to admin user if exists
     */
    private void assignGodRoleToAdmin(RoleEntity godRole) {
        try {
            // Find admin user
            ChannelEntity adminChannel = channelRepository.findByUsername("admin");
            if (adminChannel == null) {
                log.info("Admin user not found in database. Skipping GOD role assignment to admin.");
                return;
            }

            // Check if GOD role is already assigned to admin
            ChannelRoleEntity existingChannelRole = channelRoleRepository.findByChannelEntityAndRoleEntity(adminChannel, godRole);
            if (existingChannelRole != null) {
                log.info("GOD role is already assigned to admin user. Skipping assignment.");
                return;
            }

            // Assign GOD role to admin user
            ChannelRoleEntity channelRole = new ChannelRoleEntity();
            channelRole.setChannelEntity(adminChannel);
            channelRole.setRoleEntity(godRole);
            channelRole.setCreatedBy("system");
            channelRole.setCreatedAt(new Date());
            
            channelRoleRepository.save(channelRole);
            log.info("Successfully assigned GOD role to admin user with ID: {}", adminChannel.getId());

        } catch (Exception e) {
            log.error("Error assigning GOD role to admin user", e);
        }
    }


}
