package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.ResourceEntity;
import com.melli.wallet.domain.request.panel.ResourceCreateRequestJson;
import com.melli.wallet.domain.request.panel.ResourceUpdateRequestJson;
import com.melli.wallet.domain.response.panel.ResourceDetailResponse;
import com.melli.wallet.domain.response.panel.ResourceListResponse;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ResourceRepositoryService {

    ResourceEntity getRequestType(String name);

    List<ResourceEntity> findAllByIds(List<Long> ids);

    /**
     * Create a new resource
     */
    ResourceDetailResponse createResource(ResourceCreateRequestJson requestJson, String createdBy) throws InternalServiceException;

    /**
     * Update an existing resource
     */
    ResourceDetailResponse updateResource(Long resourceId, ResourceUpdateRequestJson requestJson, String updatedBy) throws InternalServiceException;

    /**
     * Delete a resource
     */
    void deleteResource(Long resourceId) throws InternalServiceException;

    /**
     * Get resource by ID
     */
    ResourceDetailResponse getResourceById(Long resourceId) throws InternalServiceException;

    /**
     * Get paginated list of resources
     */
    Page<ResourceListResponse> listResources(Pageable pageable) throws InternalServiceException;

}
