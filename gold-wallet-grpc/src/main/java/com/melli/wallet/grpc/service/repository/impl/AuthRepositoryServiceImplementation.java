package com.melli.wallet.grpc.service.repository.impl;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.exception.InternalServiceException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import com.melli.wallet.grpc.service.repository.AuthRepositoryService;
import com.melli.wallet.service.operation.AuthenticateOperationService;
import com.melli.wallet.service.operation.SecurityOperationService;
import com.melli.wallet.service.repository.ChannelAccessTokenRepositoryService;
import com.melli.wallet.service.repository.ChannelRepositoryService;
import com.melli.wallet.service.repository.SettingGeneralRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthRepositoryServiceImplementation implements AuthRepositoryService {

    private final ChannelRepositoryService channelRepositoryService;
    private final AuthenticationManager authenticationManager;
    private final SecurityOperationService securityOperationService;
    private final SettingGeneralRepositoryService settingGeneralRepositoryService;
    private final AuthenticateOperationService authenticateOperationService;

    @Override
    public LoginResponse login(String username, String password, String clientIp) throws InternalServiceException {
        log.info("Starting login process for username: {} from IP: {}", username, clientIp);

        try {
            authenticate(username, password);
            Map<String, String> accessToken = generateToken(username, Long.parseLong(settingGeneralRepositoryService.getSetting(SettingGeneralRepositoryService.DURATION_ACCESS_TOKEN_PROFILE).getValue()));
            Map<String, String> refreshToken = generateRefreshToken(username, Long.parseLong(settingGeneralRepositoryService.getSetting(SettingGeneralRepositoryService.DURATION_REFRESH_TOKEN_PROFILE).getValue()));
            return authenticateOperationService.login(username, clientIp, accessToken, refreshToken);
        } catch (InternalServiceException ex) {
            log.error("failed in login with InternalServiceException ({})", ex.getMessage());
            throw ex;
        } catch (BadCredentialsException ex) {
            log.error("failed in login with BadCredentialsException ({})", ex.getMessage());
            ChannelEntity profileEntity = channelRepositoryService.findByUsername(username);
            securityOperationService.increaseFailLogin(profileEntity);
            throw new InternalServiceException("invalid username password", StatusRepositoryService.INVALID_USERNAME_PASSWORD, HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            log.error("failed in login with Exception ({})", ex.getMessage());
            throw new InternalServiceException("general error", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
        }
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
            securityOperationService.increaseFailLogin(channelRepositoryService.findByUsername(username));
            throw new BadCredentialsException("INVALID_CREDENTIALS for username (" + username + ")", ex);
        }
    }

    @Override
    public LoginResponse refreshToken(String username, String refreshToken, String clientIp) throws InternalServiceException {
        log.info("Starting refresh token process for username: {} from IP: {}", username, clientIp);
        log.info("start refreshToken with data ({})", refreshToken);
        Map<String, String> accessToken = generateToken(username, Long.parseLong(settingGeneralRepositoryService.getSetting(SettingGeneralRepositoryService.DURATION_ACCESS_TOKEN_PROFILE).getValue()));
        Map<String, String> refreshTokenMap = generateRefreshToken(username, Long.parseLong(settingGeneralRepositoryService.getSetting(SettingGeneralRepositoryService.DURATION_REFRESH_TOKEN_PROFILE).getValue()));
        return authenticateOperationService.generateRefreshToken(refreshToken, username, clientIp, accessToken, refreshTokenMap);

    }

    @Override
    public void logout(ChannelEntity channelEntity, String clientIp) throws InternalServiceException {
        log.info("Starting logout process for username: {} from IP: {}", channelEntity.getUsername(), clientIp);
        log.info("start logout channel by ({}), ip({})", channelEntity, clientIp);
        authenticateOperationService.logout(channelEntity);
    }

    private Map<String, String> generateToken(String username, long expireTimeDuration) {
        Map<String, String> result = new HashMap<>();
        Map<String, Object> claims = new HashMap<>();
        claims.put("generateTime", new Date().getTime());
        String token = doGenerateToken(claims, username, expireTimeDuration);
        result.put("accessToken", token);
        result.put("expireTime", String.valueOf(getExpirationDateFromToken(token).getTime()));
        return result;
    }

    private Map<String, String> generateRefreshToken(String username, long expireTimeDuration) {
        Map<String, String> result = new HashMap<>();
        Map<String, Object> claims = new HashMap<>();
        claims.put("generateTime", new Date().getTime());
        String token = doGenerateToken(claims, username, expireTimeDuration);
        result.put("refreshToken", token);
        result.put("expireTime", String.valueOf(getExpirationDateFromToken(token).getTime()));
        return result;
    }

    private String doGenerateToken(Map<String, Object> claims, String subject, long expireTimeDuration) {
        SecretKey key = Keys.hmacShaKeyFor("fdgljdfgldjfgljotgtoretgmeorwpfimmwmk353459fghytljdslfjdslfjdsljferlj342-09yi45ygq23".getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTimeDuration * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Date getExpirationDateFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor("fdgljdfgldjfgljotgtoretgmeorwpfimmwmk353459fghytljdslfjdslfjdsljferlj342-09yi45ygq23".getBytes(StandardCharsets.UTF_8));
        
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}
