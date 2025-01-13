package com.melli.hub.service;


import com.melli.hub.exception.InternalServiceException;

public interface ShahkarInfoOperationService {
    Boolean checkShahkarInfo(String mobileNumber, String nationalCode, boolean isNew) throws InternalServiceException;
}
