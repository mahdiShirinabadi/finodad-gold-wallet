package com.melli.wallet.service.operation;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.exception.InternalServiceException;

import java.util.Map;

/**
 * Class Name: AuthenticateService
 * Author: Mahdi Shirinabadi
 * Date: 1/6/2025
 */
public interface AuthenticateOperationService {

    LoginResponse login(String username, String ip, Map<String, String> accessToken, Map<String, String> refreshToken) throws InternalServiceException;
    LoginResponse generateRefreshToken(String refreshToken, String nationalCode, String ip, Map<String, String> accessTokenMap, Map<String, String> refreshTokenMap) throws InternalServiceException;
    void logout(ChannelEntity channelEntity) throws InternalServiceException;
}
