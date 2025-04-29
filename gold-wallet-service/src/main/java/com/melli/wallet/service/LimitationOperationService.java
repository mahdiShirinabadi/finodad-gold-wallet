package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.limitation.LimitationListResponse;
import com.melli.wallet.exception.InternalServiceException;

/**
 * Class Name: LimitationOperationService
 * Author: Mahdi Shirinabadi
 * Date: 4/21/2025
 */
public interface LimitationOperationService {
    String getValue(ChannelEntity channelEntity, String limitationName, String accountNumber, String nationalCode, String ip) throws InternalServiceException;
    LimitationListResponse getAll() throws InternalServiceException;
}
