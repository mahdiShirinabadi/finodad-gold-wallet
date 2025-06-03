package com.melli.wallet.grpc.service;

import com.melli.wallet.domain.master.entity.ChannelAccessTokenEntity;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.grpc.*;
import com.melli.wallet.grpc.exception.GrpcExceptionHandler;
import com.melli.wallet.grpc.security.JwtTokenUtil;
import com.melli.wallet.service.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * Class Name: AithServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 6/3/2025
 */
@GrpcService
@RequiredArgsConstructor
@Log4j2
public class GrpcAuthServiceImplementation extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthenticationManager authenticationManager;
    private final ChannelService channelService;
    private final JwtTokenUtil jwtTokenUtil;
    private final SecurityService securityService;
    private final ChannelAccessTokenService channelAccessTokenService;
    private final SettingGeneralService settingGeneralService;
    private final AuthenticateService authenticateService;
    private final GrpcExceptionHandler exceptionHandler;

    @Override
    public void login(LoginRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String username = request.getUsername();
            String password = request.getPassword();
            log.info("Start login for username: {}", username);

            authenticate(username, password);
            boolean isAfter = checkExpiration(channelService.findByUsername(username));
            Map<String, String> accessToken = jwtTokenUtil.generateToken(username, Long.parseLong(settingGeneralService.getSetting(SettingGeneralService.DURATION_ACCESS_TOKEN_PROFILE).getValue()));
            Map<String, String> refreshToken = jwtTokenUtil.generateRefreshToken(username, Long.parseLong(settingGeneralService.getSetting(SettingGeneralService.DURATION_REFRESH_TOKEN_PROFILE).getValue()));

            LoginResponse loginResponse = authenticateService.login(username, ThreadContext.get("ip"), isAfter, accessToken, refreshToken);
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setLoginResponse(convertToProtoLoginResponse(loginResponse))
                    .setTrackingId(ThreadContext.get("uuid"))
                    .setDoTime(new Date().toString())
                    .setDoTimestamp(System.currentTimeMillis())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            exceptionHandler.handleException(ex, responseObserver, "AuthService/Login");
        }
    }

    @Override
    public void refreshToken(RefreshTokenRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String username = request.getUsername();
            String refreshToken = request.getRefreshToken();
            log.info("Start refreshToken for username: {}", username);

            boolean isAfter = checkExpiration(channelService.findByUsername(username));
            Map<String, String> accessToken = jwtTokenUtil.generateToken(username, Long.parseLong(settingGeneralService.getSetting(SettingGeneralService.DURATION_ACCESS_TOKEN_PROFILE).getValue()));
            Map<String, String> newRefreshToken = jwtTokenUtil.generateRefreshToken(username, Long.parseLong(settingGeneralService.getSetting(SettingGeneralService.DURATION_REFRESH_TOKEN_PROFILE).getValue()));

            LoginResponse loginResponse = authenticateService.generateRefreshToken(refreshToken, username, ThreadContext.get("ip"), isAfter, accessToken, newRefreshToken);
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setLoginResponse(convertToProtoLoginResponse(loginResponse))
                    .setTrackingId(ThreadContext.get("uuid"))
                    .setDoTime(new Date().toString())
                    .setDoTimestamp(System.currentTimeMillis())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            exceptionHandler.handleException(ex, responseObserver, "AuthService/RefreshToken");
        }
    }

    @Override
    public void logout(LogoutRequestGrpc request, StreamObserver<BaseResponseGrpc> responseObserver) {
        try {
            String username = ThreadContext.get("username");
            log.info("Start logout for username: {}, ip: {}", username, ThreadContext.get("ip"));

            authenticateService.logout(channelService.findByUsername(username));
            BaseResponseGrpc response = BaseResponseGrpc.newBuilder()
                    .setSuccess(true)
                    .setEmpty(Empty.newBuilder().build())
                    .setTrackingId(ThreadContext.get("uuid"))
                    .setDoTime(new Date().toString())
                    .setDoTimestamp(System.currentTimeMillis())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            exceptionHandler.handleException(ex, responseObserver, "AuthService/Logout");
        }
    }

    private void authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, username + "M@hd!" + password));
            log.info("Success authenticate for username: {}", username);
        } catch (BadCredentialsException ex) {
            log.error("Failed authenticate for username: {}, error: {}", username, ex.getMessage());
            securityService.increaseFailLogin(channelService.findByUsername(username));
            throw new BadCredentialsException("INVALID_CREDENTIALS for username: " + username, ex);
        }
    }

    private boolean checkExpiration(ChannelEntity channelEntity) {
        log.info("Start check expiration for username: {}", channelEntity.getUsername());
        ChannelAccessTokenEntity tokenEntity = channelAccessTokenService.findTopByChannelEntityAndEndTimeIsnUll(channelEntity);
        if (tokenEntity != null && tokenEntity.getAccessToken() != null) {
            try {
                return jwtTokenUtil.getExpirationDateFromToken(tokenEntity.getAccessToken()).after(new Date());
            } catch (Exception ex) {
                log.error("Failed check expiration for username: {}, error: {}", channelEntity.getUsername(), ex.getMessage());
            }
        }
        return false;
    }

    private LoginResponseGrpc convertToProtoLoginResponse(LoginResponse loginResponse) {
        // Implement conversion logic from LoginResponse to WalletProto.LoginResponse
        // This involves mapping ChannelObject, TokenObject, etc.
        return LoginResponseGrpc.newBuilder()
                .setChannelObject(ChannelObjectGrpc.newBuilder()
                        .setFirstName(loginResponse.getChannelObject().getFirstName())
                        .setLastName(loginResponse.getChannelObject().getLastName())
                        .setUsername(loginResponse.getChannelObject().getUsername())
                        .setMobile(loginResponse.getChannelObject().getMobile())
                        .build())
                .setAccessTokenObject(TokenObjectGrpc.newBuilder()
                        .setToken(loginResponse.getAccessTokenObject().getToken())
                        .setExpireTime(loginResponse.getAccessTokenObject().getExpireTime())
                        .build())
                .setRefreshTokenObject(TokenObjectGrpc.newBuilder()
                        .setToken(loginResponse.getRefreshTokenObject().getToken())
                        .setExpireTime(loginResponse.getRefreshTokenObject().getExpireTime())
                        .build())
                .build();
    }
}
