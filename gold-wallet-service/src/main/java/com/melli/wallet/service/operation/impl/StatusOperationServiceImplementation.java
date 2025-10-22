package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.StatusEntity;
import com.melli.wallet.domain.response.status.StatusListResponse;
import com.melli.wallet.domain.response.status.StatusObject;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.StatusOperationService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Class Name: StatusOperationServiceImplementation
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Implementation of status operations
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class StatusOperationServiceImplementation implements StatusOperationService {

    private final StatusRepositoryService statusRepositoryService;
    private final Helper helper;
    private final RedisLockService redisLockService;

    @Override
    public StatusListResponse list(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException {
        log.info("start list statuses for channel: {} with criteria: {}", channelEntity.getUsername(), searchCriteria);

        // Get pagination settings
        Pageable pageable = getPageableConfig(searchCriteria);

        // Get statuses with specification and pagination
        Page<StatusEntity> statusPage = statusRepositoryService.findAllWithSpecification(searchCriteria, pageable);

        // Convert to response objects
        List<StatusObject> statusObjectList = statusPage.getContent().stream()
                .map(this::convertToStatusObject)
                .toList();

        log.info("finish list statuses, found {} statuses", statusObjectList.size());

        return helper.fillStatusListResponse(statusObjectList, statusPage);
    }

    @Override
    public void create(ChannelEntity channelEntity, String code, String persianDescription) throws InternalServiceException {
        log.info("start create status with code: {} for channel: {}", code, channelEntity.getUsername());

        redisLockService.runAfterLock(code, this.getClass(),()->{
            // Check if status with same code already exists
            StatusEntity existingStatus = statusRepositoryService.findByCode(code);
            if (existingStatus != null) {
                log.error("Status with code {} already exists", code);
                throw new InternalServiceException("Status with this code already exists", StatusRepositoryService.STATUS_NOT_FOUND, HttpStatus.OK);
            }

            // Create new status entity
            StatusEntity statusEntity = new StatusEntity();
            statusEntity.setCode(code);
            statusEntity.setPersianDescription(persianDescription);

            // Save status
            statusRepositoryService.createStatus(statusEntity);

            log.info("finish create status with code: {}", code);
            return null;
        }, code);
    }

    @Override
    public void update(ChannelEntity channelEntity, String id, String code, String persianDescription) throws InternalServiceException {
        log.info("start update status with id: {} for channel: {}", id, channelEntity.getUsername());

        // Find existing status
        StatusEntity statusEntity = statusRepositoryService.findById(Long.parseLong(id));

        // Update status fields
        statusEntity.setCode(code);
        statusEntity.setPersianDescription(persianDescription);

        // Save updated status
        statusRepositoryService.updateStatus(statusEntity);

        log.info("finish update status with id: {}", id);
    }

    private Pageable getPageableConfig(Map<String, String> searchCriteria) {
        int page = Integer.parseInt(searchCriteria.getOrDefault("page", "0"));
        int size = Integer.parseInt(searchCriteria.getOrDefault("size", "10"));
        return PageRequest.of(page, size);
    }

    private StatusObject convertToStatusObject(StatusEntity statusEntity) {
        StatusObject statusObject = new StatusObject();
        statusObject.setId(statusEntity.getId());
        statusObject.setCode(statusEntity.getCode());
        statusObject.setPersianDescription(statusEntity.getPersianDescription());
        statusObject.setAdditionalData(statusEntity.getAdditionalData());
        statusObject.setCreatedAt(statusEntity.getCreatedAt());
        statusObject.setUpdatedAt(statusEntity.getUpdatedAt());
        return statusObject;
    }
}
