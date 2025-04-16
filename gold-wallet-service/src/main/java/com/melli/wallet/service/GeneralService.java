package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.exception.InternalServiceException;

/**
 * Class Name: GeneralService
 * Author: Mahdi Shirinabadi
 * Date: 1/21/2025
 */
public interface GeneralService {

    UuidResponse generateUuid(ChannelEntity channelEntity, String nationalCode) throws InternalServiceException;
}
