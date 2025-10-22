package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.TemplateEntity;
import com.melli.wallet.domain.response.template.TemplateListResponse;
import com.melli.wallet.domain.response.template.TemplateObject;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.TemplateOperationService;
import com.melli.wallet.service.repository.SettingGeneralRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.service.repository.TemplateRepositoryService;
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
 * Class Name: TemplateOperationServiceImplementation
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Implementation of template operations
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class TemplateOperationServiceImplementation implements TemplateOperationService {

    private final TemplateRepositoryService templateRepositoryService;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;
    private final Helper helper;
    private final RedisLockService redisLockService;

    @Override
    public TemplateListResponse list(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException {
        log.info("start list templates for channel: {} with criteria: {}", channelEntity.getUsername(), searchCriteria);

        // Get pagination settings
        Pageable pageable = getPageableConfig(searchCriteria);

        // Get templates with specification and pagination
        Page<TemplateEntity> templatePage = templateRepositoryService.findAllWithSpecification(searchCriteria, pageable);

        // Convert to response objects
        List<TemplateObject> templateObjectList = templatePage.getContent().stream()
                .map(this::convertToTemplateObject)
                .toList();

        log.info("finish list templates, found {} templates", templateObjectList.size());

        return helper.fillTemplateListResponse(templateObjectList, templatePage);
    }

    @Override
    public void create(ChannelEntity channelEntity, String name, String value) throws InternalServiceException {
        log.info("start create template with name: {} for channel: {}", name, channelEntity.getUsername());

        redisLockService.runAfterLock(name, this.getClass(),()->{
            TemplateEntity currentTemplate = templateRepositoryService.findByName(name);
            if(currentTemplate != null){
                log.error("template already exists for name: {}", name);
                throw new InternalServiceException("template name not unique", StatusRepositoryService.TEMPLATE_WITH_NAME_EXIST, HttpStatus.OK);
            }

            // Create new template entity
            TemplateEntity templateEntity = new TemplateEntity();
            templateEntity.setName(name);
            templateEntity.setValue(value);

            // Save template
            templateRepositoryService.createTemplate(templateEntity);

            log.info("finish create template with name: {}", name);
            return null;
        }, name);
    }

    @Override
    public void update(ChannelEntity channelEntity, String id, String name, String value) throws InternalServiceException {
        log.info("start update template with id: {} for channel: {}", id, channelEntity.getUsername());

        // Find existing template
        TemplateEntity templateEntity = templateRepositoryService.findById(Long.parseLong(id));

        // Update template fields
        templateEntity.setName(name);
        templateEntity.setValue(value);

        // Save updated template
        templateRepositoryService.updateTemplate(templateEntity);

        log.info("finish update template with id: {}", id);
    }

    private Pageable getPageableConfig(Map<String, String> searchCriteria) {
        int page = Integer.parseInt(searchCriteria.getOrDefault("page", "0"));
        int size = Integer.parseInt(searchCriteria.getOrDefault("size", "10"));
        return PageRequest.of(page, size);
    }

    private TemplateObject convertToTemplateObject(TemplateEntity templateEntity) {
        TemplateObject templateObject = new TemplateObject();
        templateObject.setId(templateEntity.getId());
        templateObject.setName(templateEntity.getName());
        templateObject.setValue(templateEntity.getValue());
        templateObject.setCreatedAt(templateEntity.getCreatedAt());
        templateObject.setUpdatedAt(templateEntity.getUpdatedAt());
        return templateObject;
    }
}
