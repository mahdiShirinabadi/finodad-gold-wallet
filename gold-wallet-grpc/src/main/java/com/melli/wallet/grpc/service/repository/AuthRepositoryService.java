package com.melli.wallet.grpc.service.repository;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.exception.InternalServiceException;

public interface AuthRepositoryService {
    
    /**
     * Login with username and password
     * @param username username
     * @param password password
     * @param clientIp client IP address
     * @return LoginResponse with tokens
     * @throws InternalServiceException if login fails
     */
    LoginResponse login(String username, String password, String clientIp) throws InternalServiceException;
    
    /**
     * Refresh access token using refresh token
     * @param username username
     * @param refreshToken refresh token
     * @param clientIp client IP address
     * @return LoginResponse with new tokens
     * @throws InternalServiceException if refresh fails
     */
    LoginResponse refreshToken(String username, String refreshToken, String clientIp) throws InternalServiceException;
    
    /**
     * Logout user
     * @param channelEntity channel entity
     * @param clientIp client IP address
     * @throws InternalServiceException if logout fails
     */
    void logout(ChannelEntity channelEntity, String clientIp) throws InternalServiceException;
}
