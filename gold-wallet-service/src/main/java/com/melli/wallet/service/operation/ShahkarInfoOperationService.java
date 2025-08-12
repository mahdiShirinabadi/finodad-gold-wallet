package com.melli.wallet.service.operation;


import com.melli.wallet.exception.InternalServiceException;

public interface ShahkarInfoOperationService {
    Boolean checkShahkarInfo(String mobileNumber, String nationalCode, boolean isNew) throws InternalServiceException;
}
