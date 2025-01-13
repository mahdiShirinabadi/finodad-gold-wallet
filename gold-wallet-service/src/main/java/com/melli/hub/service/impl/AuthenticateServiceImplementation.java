package com.melli.hub.service.impl;

import com.melli.hub.domain.enumaration.VerificationCodeEnum;
import com.melli.hub.domain.master.entity.ProfileAccessTokenEntity;
import com.melli.hub.domain.master.entity.ProfileEntity;
import com.melli.hub.domain.response.login.LoginResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.*;
import com.melli.hub.util.Utility;
import com.melli.hub.util.Validator;
import com.melli.hub.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Class Name: AuthenticateServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/6/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class AuthenticateServiceImplementation implements AuthenticateService {

    private final ProfileService profileService;
    private final Helper helper;
    private final SecurityService securityService;
    private final VerificationCodeService verificationCodeService;
    private final SettingService settingService;
    private final MessageService messageService;
    private final ProfileAccessTokenService profileAccessTokenService;
    private final Environment environment;

    @Override
    public LoginResponse login(String nationalCode, String deviceName, String additionalData, String ip, boolean isAfter, Map<String, String> accessTokenMap, Map<String, String> refreshTokenMap) throws InternalServiceException {

        ProfileEntity profileEntity = profileService.findByNationalCode(nationalCode);


        if (helper.notInAllowedList(profileEntity.getValidIp(), ip)) {
            log.error("ip ({}), not exist in valid ip list ({})", ip, profileEntity.getValidIp());
            throw new InternalServiceException("ip (" + ip + ", not exist in valid ip list ( " + profileEntity.getValidIp() + ")", StatusService.INVALID_IP_ADDRESS, HttpStatus.FORBIDDEN);
        }

        if (securityService.isBlock(profileEntity)) {
            log.info("channel ({}) is blocked !!!", nationalCode);
            throw new InternalServiceException("channel (" + nationalCode + ") is blocked", StatusService.PROFILE_IS_BLOCKED, HttpStatus.FORBIDDEN);
        }

        if(Boolean.TRUE.equals(profileEntity.getTowFactorAuthentication())){
            log.info("twoFactor authentication for nationalCode ({}) is active", nationalCode);
            //send otp to user for authentication
            int numberGenerated = Helper.generateRandomNumber();
            String randomNumber = Utility.pad(String.valueOf(numberGenerated), Integer.parseInt(settingService.getSetting(SettingService.LENGTH_OTP).getValue()));//generate otp
            log.info("generate random number ({}) for nationalCode ({})", randomNumber, nationalCode);
            verificationCodeService.save(nationalCode, randomNumber, VerificationCodeEnum.LOGIN);
            String template = settingService.getSetting(SettingService.SMS_OTP_TEMPLATE).getValue();
            messageService.send(String.format(template, randomNumber), profileEntity.getMobile());
            return helper.fillLoginResponse(profileEntity, null, Long.valueOf("0"), null, Long.valueOf("0"));
        }

        ProfileAccessTokenEntity profileAccessTokenEntity = saveProfileAccessTokenEntity(ip, profileEntity, accessTokenMap, refreshTokenMap, deviceName, additionalData);
        securityService.resetFailLoginCount(profileEntity);
        return helper.fillLoginResponse(profileEntity, profileAccessTokenEntity.getAccessToken(), profileAccessTokenEntity.getAccessTokenExpireTime().getTime(),
                profileAccessTokenEntity.getRefreshToken(), profileAccessTokenEntity.getRefreshTokenExpireTime().getTime());
    }

    @Override
    public LoginResponse generateRefreshToken(String refreshToken, String nationalCode, String deviceName, String additionalData, String ip, boolean isAfter, Map<String, String> accessTokenMap, Map<String, String> refreshTokenMap) throws InternalServiceException {
        ProfileAccessTokenEntity profileAccessTokenEntityOld = profileAccessTokenService.findTopByRefreshTokenEndTimeIsnUll(refreshToken);

        if (!profileAccessTokenEntityOld.getProfileEntity().getUsername().equalsIgnoreCase(nationalCode)) {
            log.error("nationalCode refreshToken ({}) not same nationalCode ({})", nationalCode, profileAccessTokenEntityOld.getProfileEntity().getNationalCode());
            throw new InternalServiceException("Unauthorized access to resources", StatusService.REFRESH_TOKEN_NOT_BELONG_TO_PROFILE, HttpStatus.UNAUTHORIZED);
        }

        if (profileAccessTokenEntityOld.getRefreshTokenExpireTime().before(new Date())) {
            profileAccessTokenEntityOld.setEndTime(new Date());
            profileAccessTokenService.save(profileAccessTokenEntityOld);
            throw new InternalServiceException("refresh token is expire", StatusService.REFRESH_TOKEN_IS_EXPIRE, HttpStatus.UNAUTHORIZED);
        }


        log.info("start generate token for username ({}), Ip ({})...", nationalCode, ip);
        ProfileAccessTokenEntity profileAccessTokenEntity = saveProfileAccessTokenEntity(ip, profileAccessTokenEntityOld.getProfileEntity(), accessTokenMap, refreshTokenMap, deviceName, additionalData);
        log.info("success generate token for username ({}), Ip ({})", nationalCode, ip);
        securityService.resetFailLoginCount(profileAccessTokenEntityOld.getProfileEntity());
        return helper.fillLoginResponse(profileAccessTokenEntityOld.getProfileEntity(), profileAccessTokenEntity.getAccessToken(), profileAccessTokenEntity.getAccessTokenExpireTime().getTime(),
                profileAccessTokenEntity.getRefreshToken(), profileAccessTokenEntity.getRefreshTokenExpireTime().getTime());
    }

    @Override
    public void resentOtp(String nationalCode, String ip) throws InternalServiceException {
        ProfileEntity profileEntity = profileService.findByNationalCode(nationalCode);
        log.info("start call method ({}) send otp user with email ({}), from Ip ({})", Utility.getCallerMethodName(),nationalCode, ip);
        int numberGenerated = Helper.generateRandomNumber();
        String randomNumber = Utility.pad(String.valueOf(numberGenerated), Integer.parseInt(settingService.getSetting(SettingService.LENGTH_OTP).getValue()));//generate otp
        log.info("generate random number ({}) for nationalCode ({})", randomNumber, nationalCode);
        verificationCodeService.save(nationalCode, randomNumber, VerificationCodeEnum.LOGIN);
        String template = settingService.getSetting(SettingService.SMS_OTP_TEMPLATE).getValue();
        messageService.send(String.format(template, randomNumber), profileEntity.getMobile());
    }

    @Override
    public LoginResponse validateOtp(String nationalCode, String deviceName, String additionalData, String ip, boolean isAfter, Map<String, String> accessTokenMap, Map<String, String> refreshTokenMap, String otp) throws InternalServiceException {
        ProfileEntity profileEntity = profileService.findByNationalCode(nationalCode);
        if (helper.notInAllowedList(profileEntity.getValidIp(), ip)) {
            log.error("ip ({}), not exist in valid ip list ({})", ip, profileEntity.getValidIp());
            throw new InternalServiceException("ip (" + ip + ", not exist in valid ip list ( " + profileEntity.getValidIp() + ")", StatusService.INVALID_IP_ADDRESS, HttpStatus.FORBIDDEN);
        }

        Optional<String> envorinmentOptional = Arrays.stream(environment.getActiveProfiles()).findAny();

        if (envorinmentOptional.isPresent() && (envorinmentOptional.get().equalsIgnoreCase("test") || envorinmentOptional.get().equalsIgnoreCase("staging") || envorinmentOptional.get().equalsIgnoreCase("dev"))) {
            log.info("this is a not production env and accept any code!!");
            if(!nationalCode.substring(0,5).equalsIgnoreCase(otp)){
                log.error("code ({}) for nationalCode ({}) is not found", otp, profileEntity.getUsername());
                throw new InternalServiceException("otp (" + otp + ") for mobile (" + profileEntity.getUsername() + ") is not found", StatusService.OTP_NOT_FOUND, HttpStatus.OK, null);
            }
        } else {
            verificationCodeService.findFirstByProfileAndCodeAndStatusAndType(nationalCode, otp, VerificationCodeEnum.LOGIN);
        }

        ProfileAccessTokenEntity profileAccessTokenEntityOld = profileAccessTokenService.findTopByProfileEntityAndEndTimeIsnUll(profileEntity);
        if (profileAccessTokenEntityOld != null && !Validator.isNull(profileAccessTokenEntityOld.getAccessToken()) && isAfter) {
            log.info("token for profile ({}) is not expired and we will return current token", profileAccessTokenEntityOld.getProfileEntity().getNationalCode());
            return helper.fillLoginResponse(profileEntity, profileAccessTokenEntityOld.getAccessToken(), profileAccessTokenEntityOld.getAccessTokenExpireTime().getTime(),
                    profileAccessTokenEntityOld.getRefreshToken(), profileAccessTokenEntityOld.getRefreshTokenExpireTime().getTime());
        }
        ProfileAccessTokenEntity profileAccessTokenEntity = saveProfileAccessTokenEntity(ip, profileEntity, accessTokenMap, refreshTokenMap, deviceName, additionalData);
        securityService.resetFailLoginCount(profileEntity);
        return helper.fillLoginResponse(profileEntity, profileAccessTokenEntity.getAccessToken(), profileAccessTokenEntity.getAccessTokenExpireTime().getTime(),
                profileAccessTokenEntity.getRefreshToken(), profileAccessTokenEntity.getRefreshTokenExpireTime().getTime());
    }

    @Override
    public void logout(ProfileEntity profileEntity) throws InternalServiceException {
        List<ProfileAccessTokenEntity> profileAccessTokenEntityList = profileAccessTokenService.findAllByProfileEntityAndEndTimeIsNull(profileEntity);
        for (ProfileAccessTokenEntity profileAccessTokenEntity : profileAccessTokenEntityList) {
            profileAccessTokenEntity.setEndTime(profileEntity.getEndTime());
            profileAccessTokenService.save(profileAccessTokenEntity);
        }
    }


    private ProfileAccessTokenEntity saveProfileAccessTokenEntity(String ip, ProfileEntity profileEntity, Map<String, String> accessTokenMap, Map<String,String> refreshTokenMap, String deviceName, String additionalData) throws InternalServiceException {
        log.info("start generate token for username ({}), Ip ({})...", profileEntity.getNationalCode(), ip);
        ProfileAccessTokenEntity profileAccessTokenEntity = new ProfileAccessTokenEntity();
        profileAccessTokenEntity.setProfileEntity(profileEntity);
        profileAccessTokenEntity.setAccessToken(accessTokenMap.get("accessToken"));
        profileAccessTokenEntity.setAccessTokenExpireTime(new Date(Long.parseLong(accessTokenMap.get("expireTime"))));
        profileAccessTokenEntity.setRefreshToken(refreshTokenMap.get("refreshToken"));
        profileAccessTokenEntity.setRefreshTokenExpireTime(new Date(Long.parseLong(refreshTokenMap.get("expireTime"))));
        profileAccessTokenEntity.setDeviceName(deviceName);
        profileAccessTokenEntity.setIp(ip);
        profileAccessTokenEntity.setAdditionalData(additionalData);
        profileAccessTokenService.save(profileAccessTokenEntity);
        log.info("success generate token for username ({}), Ip ({})", profileEntity.getNationalCode(), ip);
        return profileAccessTokenEntity;
    }
}
