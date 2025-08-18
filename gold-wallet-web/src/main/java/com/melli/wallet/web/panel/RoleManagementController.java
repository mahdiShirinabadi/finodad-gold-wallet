package com.melli.wallet.web.panel;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.master.entity.ResourceEntity;
import com.melli.wallet.domain.request.panel.RoleCreateRequestJson;
import com.melli.wallet.domain.request.panel.RoleResourceRequestJson;
import com.melli.wallet.domain.request.panel.ResourceCreateRequestJson;
import com.melli.wallet.domain.request.panel.ChannelRoleRequestJson;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.panel.RoleDetailResponse;
import com.melli.wallet.domain.response.panel.ResourceListResponse;
import com.melli.wallet.domain.response.panel.ResourceDetailResponse;
import com.melli.wallet.domain.response.panel.ChannelListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.operation.RoleResourceOperationService;
import com.melli.wallet.service.operation.ChannelRoleOperationService;
import com.melli.wallet.service.repository.ResourceRepositoryService;
import com.melli.wallet.service.repository.RoleRepositoryService;
import com.melli.wallet.web.WebController;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;


/**
 * Role Management Controller
 * Provides operations for managing roles, resources, and their relationships
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = {"/api/v1/panel/role-management"})
@Validated
@Log4j2
@Tag(name = "Role Management", description = "Role and Resource Management APIs")
public class RoleManagementController extends WebController {

    private final RequestContext requestContext;
    private final RoleResourceOperationService roleResourceOperationService;
    private final ChannelRoleOperationService channelRoleOperationService;
    private final RoleRepositoryService roleRepositoryService;
    private final ResourceRepositoryService resourceRepositoryService;

    // ==================== ROLE CRUD OPERATIONS ====================

    /**
     * 1. Add Role
     */
    @Timed(description = "Time taken to create role")
    @PostMapping(path = "/role/create", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "Create a new role")
    @PreAuthorize("hasAuthority(\"" + ResourceRepositoryService.ROLE_MANAGE + "\")")
    @LogExecutionTime("Create role")
    public ResponseEntity<BaseResponse<RoleDetailResponse>> addRole(
            @Valid @RequestBody RoleCreateRequestJson requestJson) throws InternalServiceException {

        RoleDetailResponse response = roleRepositoryService.createRole(requestJson, requestContext.getChannelEntity().getUsername());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>(true, response));
    }

    /**
     * 7. Role List
     */
    @Timed(description = "Time taken to list roles")
    @GetMapping(path = "/role/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "Get paginated list of roles")
    @PreAuthorize("hasAuthority(\"" + ResourceRepositoryService.ROLE_MANAGE + "\")")
    @LogExecutionTime("List roles")
    public ResponseEntity<BaseResponse<Page<RoleDetailResponse>>> roleList(Pageable pageable) throws InternalServiceException {

        Page<RoleDetailResponse> response = roleRepositoryService.listRoles(pageable);
        
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    // ==================== RESOURCE CRUD OPERATIONS ====================

    /**
     * 2. Add Resource
     */
    @Timed(description = "Time taken to create resource")
    @PostMapping(path = "/resource/create", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "Create a new resource")
    @PreAuthorize("hasAuthority(\"" + ResourceRepositoryService.RESOURCE_MANAGE + "\")")
    @LogExecutionTime("Create resource")
    public ResponseEntity<BaseResponse<ResourceDetailResponse>> addResource(
            @Valid @RequestBody ResourceCreateRequestJson requestJson) throws InternalServiceException {

        ResourceDetailResponse response = resourceRepositoryService.createResource(requestJson, requestContext.getChannelEntity().getUsername());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>(true, response));
    }

    /**
     * 6. Resource List
     */
    @Timed(description = "Time taken to list resources")
    @GetMapping(path = "/resource/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "Get paginated list of resources")
    @PreAuthorize("hasAuthority(\"" + ResourceRepositoryService.RESOURCE_MANAGE + "\")")
    @LogExecutionTime("List resources")
    public ResponseEntity<BaseResponse<Page<ResourceListResponse>>> resourceList(Pageable pageable) throws InternalServiceException {

        Page<ResourceListResponse> response = resourceRepositoryService.listResources(pageable);
        
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }


    /**
     * Add resources to a role (keep existing resources and add new ones)
     */
    @Timed(description = "Time taken to add resources to role")
    @PostMapping(path = "/add-resources", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "Add resources to a role (keep existing resources)")
    @PreAuthorize("hasAuthority(\"" + ResourceRepositoryService.ROLE_MANAGE + "\")")
    @LogExecutionTime("Add resources to role")
    public ResponseEntity<BaseResponse<String>> addResourcesToRole(
            @Valid @RequestBody RoleResourceRequestJson requestJson) throws InternalServiceException {

        roleResourceOperationService.addResourcesToRole(requestJson.getRoleId(), requestJson.getResourceIds(), requestContext.getChannelEntity().getUsername());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse<>(true, "Resources added to role successfully"));
    }


    /**
     * Get all resources for a role
     */
    @Timed(description = "Time taken to get role resources")
    @GetMapping(path = "/{roleId}/resources", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "Get all resources for a role")
    @PreAuthorize("hasAuthority(\"" + ResourceRepositoryService.ROLE_MANAGE + "\")")
    @LogExecutionTime("Get role resources")
    public ResponseEntity<BaseResponse<List<ResourceListResponse>>> getRoleResources(@PathVariable Long roleId) throws InternalServiceException {

        Set<ResourceEntity> resources = roleResourceOperationService.getRoleResources(roleId);
        List<ResourceListResponse> response = resources.stream()
                .map(this::mapToResourceListResponse)
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    // ==================== ROLE-CHANNEL MANAGEMENT ====================

    /**
     * 5. Assign Role To Channel
     */
    @Timed(description = "Time taken to assign role to channel")
    @PostMapping(path = "/assign-role-to-channel", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "Assign a role to a channel")
    @PreAuthorize("hasAuthority(\"" + ResourceRepositoryService.CHANNEL_MANAGE + "\")")
    @LogExecutionTime("Assign role to channel")
    public ResponseEntity<BaseResponse<String>> assignRoleToChannel(
            @Valid @RequestBody ChannelRoleRequestJson requestJson) throws InternalServiceException {

        channelRoleOperationService.assignRoleToChannel(
                requestJson.getRoleId(), 
                requestJson.getChannelId(), 
                requestContext.getChannelEntity().getUsername());
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse<>(true, "Role assigned to channel successfully"));
    }



    // ==================== CHANNEL OPERATIONS ====================

    /**
     * 4. Channel List
     */
    @Timed(description = "Time taken to list channels")
    @GetMapping(path = "/channel/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "Get list of channels")
    @PreAuthorize("hasAuthority(\"" + ResourceRepositoryService.CHANNEL_MANAGE + "\")")
    @LogExecutionTime("List channels")
    public ResponseEntity<BaseResponse<List<ChannelListResponse>>> channelList() throws InternalServiceException {
        List<ChannelListResponse> response = channelRoleOperationService.getChannelList();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse<>(true, response));
    }


    private ResourceListResponse mapToResourceListResponse(ResourceEntity resource) {
        return ResourceListResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .faName(resource.getFaName())
                .display(resource.getDisplay())
                .build();
    }
}
