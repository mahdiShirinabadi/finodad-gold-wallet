package com.melli.hub.service;

import com.melli.hub.exception.InternalServiceException;

public interface SadadService {

    Boolean shahkar(ShahkarInfoEntity shahkarInfoEntity) throws InternalServiceException;

    void sendSms(String message, String mobile) throws InternalServiceException;
}
