package com.melli.hub.service;

import com.melli.hub.exception.InternalServiceException;


public interface SecurityService {

    void checkSign(ProfileEntity profileEntity, String sign, String data) throws InternalServiceException;

    void resetFailLoginCount(ProfileEntity profileEntity) throws InternalServiceException;

    boolean isBlock(ProfileEntity profileEntity);

    void increaseFailLogin(ProfileEntity profileEntity);

}
