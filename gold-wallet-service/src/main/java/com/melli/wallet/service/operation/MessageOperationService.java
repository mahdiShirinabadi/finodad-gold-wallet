package com.melli.wallet.service.operation;

import com.melli.wallet.exception.InternalServiceException;

public interface MessageOperationService {
    void send(String message, String mobile) throws InternalServiceException;
}
