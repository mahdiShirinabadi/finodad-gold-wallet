package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.status.StatusListResponse;
import com.melli.wallet.exception.InternalServiceException;

import java.util.Map;

/**
 * Class Name: StatusOperationService
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Service interface for status operations
 */
public interface StatusOperationService {

    StatusListResponse list(ChannelEntity channelEntity, Map<String, String> searchCriteria) throws InternalServiceException;

    void create(ChannelEntity channelEntity, String code, String persianDescription) throws InternalServiceException;

    void update(ChannelEntity channelEntity, String id, String code, String persianDescription) throws InternalServiceException;
}
