package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ShahkarInfoEntity;
import com.melli.wallet.exception.InternalServiceException;

public interface SadadService {

    Boolean shahkar(ShahkarInfoEntity shahkarInfoEntity) throws InternalServiceException;

    void sendSms(String message, String mobile) throws InternalServiceException;
}
