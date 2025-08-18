package com.melli.wallet.service.repository.impl;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.master.entity.ResourceEntity;
import com.melli.wallet.domain.master.persistence.ResourceRepository;
import com.melli.wallet.domain.request.panel.ResourceCreateRequestJson;
import com.melli.wallet.domain.request.panel.ResourceUpdateRequestJson;
import com.melli.wallet.domain.response.panel.ResourceDetailResponse;
import com.melli.wallet.domain.response.panel.ResourceListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.ResourceRepositoryService;
import com.melli.wallet.service.repository.SettingGeneralRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ResourceRepositoryServiceImplementation implements ResourceRepositoryService {

    private final ResourceRepository resourceRepository;
    private final Helper helper;

    @Override
    public ResourceEntity getRequestType(String name) {
        return resourceRepository.findByName(name);
    }

    @Override
    public List<ResourceEntity> findAllByIds(List<Long> ids) {
        return resourceRepository.findAllByIdIn(ids);
    }

    @Override
    @Transactional
    @LogExecutionTime("Create resource")
    public ResourceDetailResponse createResource(ResourceCreateRequestJson requestJson, String createdBy) throws InternalServiceException {
        // Check if resource with same name already exists
        ResourceEntity existingResource = resourceRepository.findByName(requestJson.getName());
        if (existingResource != null) {
            throw new InternalServiceException(StatusRepositoryService.RESOURCE_ALREADY_EXISTS);
        }

        // Create new resource
        ResourceEntity resource = new ResourceEntity();
        resource.setName(requestJson.getName());
        resource.setFaName(requestJson.getFaName());
        resource.setDisplay(requestJson.getDisplay());
        resource.setCreatedBy(createdBy);
        resource.setCreatedAt(new Date());

        ResourceEntity savedResource = resourceRepository.save(resource);

        return mapToResourceDetailResponse(savedResource);
    }

    @Override
    @Transactional
    @LogExecutionTime("Update resource")
    public ResourceDetailResponse updateResource(Long resourceId, ResourceUpdateRequestJson requestJson, String updatedBy) throws InternalServiceException {
        // Check if resource exists
        ResourceEntity resource = resourceRepository.findById(resourceId).orElse(null);
        if (resource == null) {
            throw new InternalServiceException(StatusRepositoryService.RESOURCE_NOT_FOUND);
        }

        // Check if name is being changed and if new name already exists
        if (!resource.getName().equals(requestJson.getName())) {
            ResourceEntity existingResource = resourceRepository.findByName(requestJson.getName());
            if (existingResource != null) {
                throw new InternalServiceException(StatusRepositoryService.RESOURCE_ALREADY_EXISTS);
            }
        }

        // Update resource
        resource.setName(requestJson.getName());
        resource.setFaName(requestJson.getFaName());
        resource.setDisplay(requestJson.getDisplay());
        resource.setUpdatedBy(updatedBy);
        resource.setUpdatedAt(new Date());

        ResourceEntity savedResource = resourceRepository.save(resource);

        return mapToResourceDetailResponse(savedResource);
    }

    @Override
    @Transactional
    @LogExecutionTime("Delete resource")
    public void deleteResource(Long resourceId) throws InternalServiceException {
        // Check if resource exists
        ResourceEntity resource = resourceRepository.findById(resourceId).orElse(null);
        if (resource == null) {
            throw new InternalServiceException(StatusRepositoryService.RESOURCE_NOT_FOUND);
        }

        // Check if resource is in use (assigned to roles)
        if (resource.getRoles() != null && !resource.getRoles().isEmpty()) {
            throw new InternalServiceException(StatusRepositoryService.RESOURCE_IN_USE);
        }

        resourceRepository.delete(resource);
    }

    @Override
    @LogExecutionTime("Get resource by ID")
    public ResourceDetailResponse getResourceById(Long resourceId) throws InternalServiceException {
        ResourceEntity resource = resourceRepository.findById(resourceId).orElse(null);
        if (resource == null) {
            throw new InternalServiceException(StatusRepositoryService.RESOURCE_NOT_FOUND);
        }

        return mapToResourceDetailResponse(resource);
    }

    @Override
    @LogExecutionTime("List resources")
    public Page<ResourceListResponse> listResources(Pageable pageable) throws InternalServiceException {
        List<ResourceEntity> resources = resourceRepository.findAll();

        // Convert to Page manually since ResourceRepository doesn't support Pageable
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), resources.size());

        List<ResourceEntity> pageContent = resources.subList(start, end);
        List<ResourceListResponse> responseList = pageContent.stream()
                .map(this::mapToResourceListResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(
                responseList,
                pageable,
                resources.size()
        );
    }

    private ResourceDetailResponse mapToResourceDetailResponse(ResourceEntity resource) {
        return ResourceDetailResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .faName(resource.getFaName())
                .display(resource.getDisplay())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
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
