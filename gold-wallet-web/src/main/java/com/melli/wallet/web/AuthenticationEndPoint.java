package com.melli.wallet.web;

import com.melli.wallet.domain.master.entity.ChannelAccessTokenEntity;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.request.login.LoginRequestJson;
import com.melli.wallet.domain.request.login.RefreshTokenRequestJson;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.JwtTokenUtil;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.*;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

/**
 * Class Name: AuthenticationEndPoint
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Validated
@Log4j2
public class AuthenticationEndPoint extends WebEndPoint {

    private final RequestContext context;
    private final AuthenticationManager authenticationManager;
    private final ChannelService channelService;
    private final JwtTokenUtil jwtTokenUtil;
    private final SecurityService securityService;
    private final ChannelAccessTokenService channelAccessTokenService;
    private final SettingGeneralService settingGeneralService;
    private final AuthenticateService authenticateService;


    @Operation(summary = "ورود به حساب کاربری")
    @Timed(description = "time taken to login profile")
    @PostMapping(value = "/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequestJson loginJson, HttpServletRequest httpRequest) throws InternalServiceException {
        try {
            authenticate(loginJson.getUsername(), loginJson.getPassword());
            boolean isAfter = checkExpiration(channelService.findByUsername(loginJson.getUsername()));
            Map<String, String> accessToken = jwtTokenUtil.generateToken(loginJson.getUsername(), Long.parseLong(settingGeneralService.getSetting(SettingGeneralService.DURATION_ACCESS_TOKEN_PROFILE).getValue()));
            Map<String, String> refreshToken = jwtTokenUtil.generateRefreshToken(loginJson.getUsername(), Long.parseLong(settingGeneralService.getSetting(SettingGeneralService.DURATION_REFRESH_TOKEN_PROFILE).getValue()));
            return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, authenticateService.login(loginJson.getUsername(), getIP(httpRequest), isAfter, accessToken, refreshToken)));
        } catch (InternalServiceException ex) {
            log.error("failed in login with InternalServiceException ({})", ex.getMessage());
            throw ex;
        } catch (BadCredentialsException ex) {
            log.error("failed in login with BadCredentialsException ({})", ex.getMessage());
            ChannelEntity profileEntity = channelService.findByUsername(loginJson.getUsername());
            securityService.increaseFailLogin(profileEntity);
            throw new InternalServiceException("invalid username password", StatusService.INVALID_USERNAME_PASSWORD, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            log.error("failed in login with Exception ({})", ex.getMessage());
            throw new InternalServiceException("general error", StatusService.GENERAL_ERROR, HttpStatus.OK);
        }
    }


    @Timed(description = "time taken to checkShahkar profile")
    @Operation(summary = "تولید دوباره accessToken")
    @PostMapping(value = "/refresh", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BaseResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequestJson requestJson, HttpServletRequest httpRequest) throws InternalServiceException {
        log.info("start refreshToken with data ({})", requestJson.toString());
        boolean isAfter = checkExpiration(channelService.findByUsername(requestJson.getUsername()));
        Map<String, String> accessToken = jwtTokenUtil.generateToken(requestJson.getUsername(), Long.parseLong(settingGeneralService.getSetting(SettingGeneralService.DURATION_ACCESS_TOKEN_PROFILE).getValue()));
        Map<String, String> refreshToken = jwtTokenUtil.generateRefreshToken(requestJson.getUsername(), Long.parseLong(settingGeneralService.getSetting(SettingGeneralService.DURATION_REFRESH_TOKEN_PROFILE).getValue()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, authenticateService.generateRefreshToken(requestJson.getRefreshToken(), requestJson.getUsername(), getIP(httpRequest), isAfter, accessToken, refreshToken)));
    }

    private void authenticate(String username, String password) {
        try {
            log.info("start authenticate for username ({})...", username);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, username + "M@hd!" + password));
            log.info("success authenticate for username ({})", username);
        } catch (DisabledException ex) {
            log.error("failed authenticate for username ({}), with DisabledException ({})", username, ex.getMessage());
            throw new BadCredentialsException("USER_DISABLED", ex);
        } catch (BadCredentialsException ex) {
            log.error("failed authenticate for username ({}), with BadCredentialsException ({})", username, ex.getMessage());
            securityService.increaseFailLogin(channelService.findByUsername(username));
            throw new BadCredentialsException("INVALID_CREDENTIALS for username (" + username + ")", ex);
        }
    }

    private boolean checkExpiration(ChannelEntity channelEntity) {
        log.info("start check expiration for channelEntity ({})...", channelEntity.getUsername());
        ChannelAccessTokenEntity channelAccessTokenEntity = channelAccessTokenService.findTopByChannelEntityAndEndTimeIsnUll(channelEntity);
        if (channelAccessTokenEntity != null && channelAccessTokenEntity.getAccessToken() != null) {
            try {
                return jwtTokenUtil.getExpirationDateFromToken(channelAccessTokenEntity.getAccessToken()).after(new Date());
            } catch (Exception ex) {
                log.error("failed check expiration for channel ({}), token is expired with error ({})",
                        channelEntity.getUsername(), ex.getMessage());
            }
        }
        return false;
    }

    @Timed(description = "time taken to logout profile")
    @PostMapping(path = "/logout", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAuthority(\"" + ResourceService.LOGOUT + "\")")
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "خروج از حساب")
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> logout() throws InternalServiceException {
        log.info("start logout channel by ({}), ip({})", context.getChannelEntity().getUsername(), context.getClientIp());
        authenticateService.logout(context.getChannelEntity());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
    }
}
