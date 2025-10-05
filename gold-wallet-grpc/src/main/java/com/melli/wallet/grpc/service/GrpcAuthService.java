package com.melli.wallet.grpc.service;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.grpc.service.repository.AuthRepositoryService;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.config.RequestContext;
import com.melli.wallet.grpc.exception.GrpcErrorHandler;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * Class Name: GrpcAuthService
 * Author: Mahdi Shirinabadi
 * Date: 6/7/2025
 */
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcAuthService extends AuthenticationServiceGrpc.AuthenticationServiceImplBase {

    private final AuthRepositoryService authRepositoryService;
    private final GrpcErrorHandler exceptionHandler;

    @Override
    public void login(LoginRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String clientIp = RequestContext.getClientIp();
            log.info("Starting login process for username: {} from IP: {}", request.getUsername(), clientIp);

            // Validate input
            if (request.getUsername().trim().isEmpty() || request.getPassword().trim().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Username and password are required").asRuntimeException());
                return;
            }

            // Call auth service
            LoginResponse loginResponse = authRepositoryService.login(
                    request.getUsername(),
                    request.getPassword(),
                    clientIp
            );

            // Build gRPC response
            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setLoginResponse(convertToGrpcLoginResponse(loginResponse))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
            log.info("Login successful for username: {}", request.getUsername());
            
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "GrpcAuthService/Login");
        } catch (Exception e) {
            log.error("Unexpected error in login: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }

    @Override
    public void refreshToken(RefreshTokenRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String clientIp = RequestContext.getClientIp();
            log.info("Starting refresh token process for username: {} from IP: {}", request.getUsername(), clientIp);

            // Validate input
            if (request.getUsername().trim().isEmpty() || request.getRefreshToken().trim().isEmpty()) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Username and refresh token are required").asRuntimeException());
                return;
            }

            // Call auth service
            LoginResponse loginResponse = authRepositoryService.refreshToken(
                    request.getUsername(),
                    request.getRefreshToken(),
                    clientIp
            );

            // Build gRPC response
            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setLoginResponse(convertToGrpcLoginResponse(loginResponse))
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
            log.info("Token refresh successful for username: {}", request.getUsername());
            
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "GrpcAuthService/RefreshToken");
        } catch (Exception e) {
            log.error("Unexpected error in refresh token: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }

    @Override
    public void logout(LogoutRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            ChannelEntity channelEntity = RequestContext.getChannelEntity();
            String clientIp = RequestContext.getClientIp();
            
            if (channelEntity == null) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription("No authenticated user found").asRuntimeException());
                return;
            }

            log.info("Starting logout process for username: {} from IP: {}", channelEntity.getUsername(), clientIp);

            // Call auth service
            authRepositoryService.logout(channelEntity, clientIp);

            // Build gRPC response
            BaseResponseGrpc grpcResponse = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setEmpty(Empty.newBuilder().build())
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
            log.info("Logout successful for username: {}", channelEntity.getUsername());
            
        } catch (InternalServiceException e) {
            exceptionHandler.handleException(e, responseObserver, "GrpcAuthService/Logout");
        } catch (Exception e) {
            log.error("Unexpected error in logout: {}", e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
        }
    }

    private LoginResponseGrpc convertToGrpcLoginResponse(LoginResponse loginResponse) {
        LoginResponseGrpc.Builder builder = LoginResponseGrpc.newBuilder();
        
        if (loginResponse.getChannelObject() != null) {
            ChannelObjectGrpc channelObject = ChannelObjectGrpc.newBuilder()
                    .setFirstName(loginResponse.getChannelObject().getFirstName())
                    .setLastName(loginResponse.getChannelObject().getLastName())
                    .setUsername(loginResponse.getChannelObject().getUsername())
                    .setMobile(loginResponse.getChannelObject().getMobile())
                    .build();
            builder.setChannelObject(channelObject);
        }
        
        if (loginResponse.getAccessTokenObject() != null) {
            TokenObjectGrpc accessToken = TokenObjectGrpc.newBuilder()
                    .setToken(loginResponse.getAccessTokenObject().getToken())
                    .setExpireTime(loginResponse.getAccessTokenObject().getExpireTime())
                    .build();
            builder.setAccessTokenObject(accessToken);
        }
        
        if (loginResponse.getRefreshTokenObject() != null) {
            TokenObjectGrpc refreshToken = TokenObjectGrpc.newBuilder()
                    .setToken(loginResponse.getRefreshTokenObject().getToken())
                    .setExpireTime(loginResponse.getRefreshTokenObject().getExpireTime())
                    .build();
            builder.setRefreshTokenObject(refreshToken);
        }
        
        return builder.build();
    }
}
