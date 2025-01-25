package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.response.UuidResponse;
import com.melli.hub.exception.InternalServiceException;

/**
 * Class Name: GeneralService
 * Author: Mahdi Shirinabadi
 * Date: 1/21/2025
 */
public interface GeneralService {

    UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode) throws InternalServiceException;
}
