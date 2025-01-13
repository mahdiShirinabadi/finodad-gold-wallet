package com.melli.hub.service;

import com.melli.hub.exception.InternalServiceException;

public interface MessageService {
    void send(String message, String mobile) throws InternalServiceException;
}
