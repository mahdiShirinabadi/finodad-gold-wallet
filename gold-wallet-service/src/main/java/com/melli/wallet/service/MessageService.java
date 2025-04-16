package com.melli.wallet.service;

import com.melli.wallet.exception.InternalServiceException;

public interface MessageService {
    void send(String message, String mobile) throws InternalServiceException;
}
