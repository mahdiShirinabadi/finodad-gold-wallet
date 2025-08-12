package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.master.entity.ChannelAccessTokenEntity;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.SecurityOperationService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.service.operation.AuthenticateOperationService;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Class Name: AuthenticateServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/6/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class AuthenticateOperationServiceImplementation implements AuthenticateOperationService {

    private final ChannelRepositoryService channelRepositoryService;
    private final Helper helper;
    private final SecurityOperationService securityOperationService;
    private final ChannelAccessTokenRepositoryService channelAccessTokenRepositoryService;

    @Override
    public LoginResponse login(String username, String ip, boolean isAfter, Map<String, String> accessTokenMap, Map<String, String> refreshTokenMap) throws InternalServiceException {

        ChannelEntity channelEntity = channelRepositoryService.findByUsername(username);


        if (helper.notInAllowedList(channelEntity.getIp(), ip)) {
            log.error("ip ({}), not exist in valid ip list ({})", ip, channelEntity.getIp());
            throw new InternalServiceException("ip (" + ip + ", not exist in valid ip list ( " + channelEntity.getIp() + ")", StatusRepositoryService.INVALID_IP_ADDRESS, HttpStatus.FORBIDDEN);
        }

        if (securityOperationService.isBlock(channelEntity)) {
            log.info("channel ({}) is blocked !!!", username);
            throw new InternalServiceException("channel (" + username + ") is blocked", StatusRepositoryService.CHANNEL_IS_BLOCKED, HttpStatus.FORBIDDEN);
        }

        ChannelAccessTokenEntity channelAccessTokenEntity = saveChannelAccessTokenEntity(ip, channelEntity, accessTokenMap, refreshTokenMap);
        securityOperationService.resetFailLoginCount(channelEntity);
        return helper.fillLoginResponse(channelEntity, channelAccessTokenEntity.getAccessToken(), channelAccessTokenEntity.getAccessTokenExpireTime().getTime(),
                channelAccessTokenEntity.getRefreshToken(), channelAccessTokenEntity.getRefreshTokenExpireTime().getTime());
    }

    @Override
    public LoginResponse generateRefreshToken(String refreshToken, String nationalCode, String ip, boolean isAfter, Map<String, String> accessTokenMap, Map<String, String> refreshTokenMap) throws InternalServiceException {
        ChannelAccessTokenEntity channelAccessTokenEntityOld = channelAccessTokenRepositoryService.findTopByRefreshTokenEndTimeIsnUll(refreshToken);

        if (!channelAccessTokenEntityOld.getChannelEntity().getUsername().equalsIgnoreCase(nationalCode)) {
            log.error("username refreshToken ({}) not same username ({})", nationalCode, channelAccessTokenEntityOld.getChannelEntity().getUsername());
            throw new InternalServiceException("Unauthorized access to resources", StatusRepositoryService.REFRESH_TOKEN_NOT_BELONG_TO_PROFILE, HttpStatus.UNAUTHORIZED);
        }

        if (channelAccessTokenEntityOld.getRefreshTokenExpireTime().before(new Date())) {
            channelAccessTokenEntityOld.setEndTime(new Date());
            channelAccessTokenRepositoryService.save(channelAccessTokenEntityOld);
            throw new InternalServiceException("refresh token is expire", StatusRepositoryService.REFRESH_TOKEN_IS_EXPIRE, HttpStatus.UNAUTHORIZED);
        }


        log.info("start generate token for username ({}), Ip ({})...", nationalCode, ip);
        ChannelAccessTokenEntity channelAccessTokenEntity = saveChannelAccessTokenEntity(ip, channelAccessTokenEntityOld.getChannelEntity(), accessTokenMap, refreshTokenMap);
        log.info("success generate token for username ({}), Ip ({})", nationalCode, ip);
        securityOperationService.resetFailLoginCount(channelAccessTokenEntityOld.getChannelEntity());
        return helper.fillLoginResponse(channelAccessTokenEntityOld.getChannelEntity(), channelAccessTokenEntity.getAccessToken(), channelAccessTokenEntity.getAccessTokenExpireTime().getTime(),
                channelAccessTokenEntity.getRefreshToken(), channelAccessTokenEntity.getRefreshTokenExpireTime().getTime());
    }


    @Override
    public void logout(ChannelEntity channelEntity) throws InternalServiceException {
        List<ChannelAccessTokenEntity> channelAccessTokenEntityList = channelAccessTokenRepositoryService.findAllByChannelEntityAndEndTimeIsNull(channelEntity);
        for (ChannelAccessTokenEntity channelAccessTokenEntity : channelAccessTokenEntityList) {
            channelAccessTokenEntity.setEndTime(new Date());
            channelAccessTokenRepositoryService.save(channelAccessTokenEntity);
        }
    }


    private ChannelAccessTokenEntity saveChannelAccessTokenEntity(String ip, ChannelEntity channelEntity, Map<String, String> accessTokenMap, Map<String,String> refreshTokenMap) throws InternalServiceException {
        log.info("start generate token for username ({}), Ip ({})...", channelEntity.getUsername(), ip);
        ChannelAccessTokenEntity channelAccessTokenEntity = new ChannelAccessTokenEntity();
        channelAccessTokenEntity.setChannelEntity(channelEntity);
        channelAccessTokenEntity.setAccessToken(accessTokenMap.get("accessToken"));
        channelAccessTokenEntity.setAccessTokenExpireTime(new Date(Long.parseLong(accessTokenMap.get("expireTime"))));
        channelAccessTokenEntity.setRefreshToken(refreshTokenMap.get("refreshToken"));
        channelAccessTokenEntity.setRefreshTokenExpireTime(new Date(Long.parseLong(refreshTokenMap.get("expireTime"))));
        channelAccessTokenEntity.setIp(ip);
        channelAccessTokenRepositoryService.save(channelAccessTokenEntity);
        log.info("success generate token for username ({}), Ip ({})", channelEntity.getUsername(), ip);
        return channelAccessTokenEntity;
    }
}
