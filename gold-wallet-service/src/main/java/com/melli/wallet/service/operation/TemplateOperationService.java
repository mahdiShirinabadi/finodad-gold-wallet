package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.template.TemplateListResponse;
import com.melli.wallet.exception.InternalServiceException;

import java.util.Map;

/**
 * Class Name: TemplateOperationService
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Service interface for template operations
 */
public interface TemplateOperationService {

    TemplateListResponse list(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException;

    void create(ChannelEntity channelEntity, String name, String value) throws InternalServiceException;

    void update(ChannelEntity channelEntity, String id, String name, String value) throws InternalServiceException;
}
