package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.ShahkarInfoEntity;
import com.melli.wallet.exception.InternalServiceException;

public interface SadadOperationService {

    Boolean shahkar(ShahkarInfoEntity shahkarInfoEntity) throws InternalServiceException;

    void sendSms(String message, String mobile) throws InternalServiceException;
}
