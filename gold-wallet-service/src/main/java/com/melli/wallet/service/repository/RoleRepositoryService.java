package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.RoleEntity;
import com.melli.wallet.domain.request.panel.RoleCreateRequestJson;
import com.melli.wallet.domain.request.panel.RoleUpdateRequestJson;
import com.melli.wallet.domain.response.panel.PanelRoleListResponse;
import com.melli.wallet.domain.response.panel.PanelRoleObject;
import com.melli.wallet.domain.response.panel.RoleDetailResponse;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Class Name: RoleService
 * Author: Mahdi Shirinabadi
 * Date: 1/5/2025
 */
public interface RoleRepositoryService {

    String WEB_PROFILE = "WEB_PROFILE";

    void addChannelToRole(ChannelEntity channelEntity, String roleName) throws InternalServiceException;
    void removeChannelFromRole(ChannelEntity channelEntity, String roleName) throws InternalServiceException;

    RoleEntity findById(Long roleId) throws InternalServiceException;
    /**
     * Create a new role
     */
    RoleDetailResponse createRole(RoleCreateRequestJson requestJson, String createdBy) throws InternalServiceException;

    /**
     * Update an existing role
     */
    RoleDetailResponse updateRole(Long roleId, RoleUpdateRequestJson requestJson, String updatedBy) throws InternalServiceException;

    /**
     * Delete a role
     */
    void deleteRole(Long roleId) throws InternalServiceException;

    /**
     * Get role by ID
     */
    RoleDetailResponse getRoleById(Long roleId) throws InternalServiceException;

    /**
     * Get paginated list of roles
     */
    Page<RoleDetailResponse> listRoles(Pageable pageable) throws InternalServiceException;
}
