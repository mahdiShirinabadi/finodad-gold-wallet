package com.melli.hub.service.impl;

import com.melli.hub.domain.enumaration.ProfileLevelEnum;
import com.melli.hub.domain.enumaration.ProfileStatusEnum;
import com.melli.hub.domain.enumaration.RegisterProfileStepStatus;
import com.melli.hub.domain.enumaration.VerificationCodeEnum;
import com.melli.hub.domain.response.login.ForgetPasswordCheckOtpResponse;
import com.melli.hub.domain.response.login.ForgetPasswordProfileResponse;
import com.melli.hub.domain.response.login.SendOtpRegisterResponse;
import com.melli.hub.domain.response.login.SendShahkarRegisterResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.*;
import com.melli.hub.util.Utility;
import com.melli.hub.util.date.DateUtils;
import com.melli.hub.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Class Name: AuthenticationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class RegistrationServiceImplementation implements RegistrationService {

    private final TempRegisterProfileService tempRegisterProfileService;
    private final SettingService settingService;
    private final VerificationCodeService verificationCodeService;
    private final MessageService messageService;
    private final Helper helper;
    private final Environment environment;
    private final ShahkarInfoOperationService shahkarInfoOperationService;
    private final ProfileService profileService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;



    @Override
    public ForgetPasswordProfileResponse forgetPassword(String nationalCode, String mobileNumber) throws InternalServiceException {
        ProfileEntity profileEntity = profileService.ensureNationalCodeAndMobileNumberExist(nationalCode, mobileNumber);
        verificationCodeService.checkTooManySendOtp(nationalCode, VerificationCodeService.STATUS_CREATE, VerificationCodeEnum.FORGET_PASSWORD);

        int numberGenerated = Helper.generateRandomNumber();
        String randomNumber = Utility.pad(String.valueOf(numberGenerated), Integer.parseInt(settingService.getSetting(SettingService.LENGTH_OTP).getValue()));//generate otp
        log.info("generate random number ({}) for nationalCode ({})", randomNumber, nationalCode);
        VerificationCodeEntity verificationCode = verificationCodeService.save(nationalCode, randomNumber, VerificationCodeEnum.FORGET_PASSWORD);
        String template = settingService.getSetting(SettingService.SMS_OTP_TEMPLATE).getValue();
        messageService.send(String.format(template, randomNumber), mobileNumber);
        String encodedValue = Helper.generateHashForForgetPassword(passwordEncoder, nationalCode);
        return helper.fillForgetPasswordProfileResponse(profileEntity.getNationalCode() ,verificationCode.getExpireTime().getTime(), profileEntity.getMobile(), encodedValue);
    }


    @Override
    public ForgetPasswordCheckOtpResponse forgetPasswordCheckOtp(String otp, String username, String registerHash) throws InternalServiceException {

        Helper.checkGenerateHashForForgetPassword(passwordEncoder, username, registerHash);

        ProfileEntity profileEntity = profileService.ensureNationalCodeExist(username);
        log.info("start find profileEntity with nationalCode ({}) with id ({})", username, profileEntity.getId());
        Optional<String> envorinmentOptional = Arrays.stream(environment.getActiveProfiles()).findAny();
        if (envorinmentOptional.isPresent() && envorinmentOptional.get().equalsIgnoreCase("staging") || envorinmentOptional.get().equalsIgnoreCase("dev")) {
            log.info("this is a not production env and accept any code!!");
            if(!username.substring(0,5).equalsIgnoreCase(otp)){
                log.error("code ({}) for nationalCode ({}) is not found", otp, profileEntity.getUsername());
                throw new InternalServiceException("otp (" + otp + ") for mobile (" + profileEntity.getUsername() + ") is not found", StatusService.OTP_NOT_FOUND, HttpStatus.OK, null);
            }
        } else {
            verificationCodeService.findFirstByProfileAndCodeAndStatusAndType(username, otp, VerificationCodeEnum.FORGET_PASSWORD);
        }
        return helper.fillForgetPasswordCheckOtpResponse(username);
    }

    @Override
    public void forgetPasswordSetPassword(String otp, String username, String password, String newPassword,String registerHash) throws InternalServiceException {

        Helper.checkGenerateHashForForgetPassword(passwordEncoder, username, registerHash);

        if(!password.equals(newPassword)){
            log.error("password is not same!!!");
            throw new InternalServiceException("profile is not exist", StatusService.PASSWORD_NOT_MATCH, HttpStatus.OK, null);
        }

        ProfileEntity profileEntity = profileService.ensureNationalCodeExist(username);
        VerificationCodeEntity verificationCode = verificationCodeService.getByOtpCodeAndStatus(username, otp, VerificationCodeService.STATUS_USED, VerificationCodeEnum.FORGET_PASSWORD);

        if(!Objects.equals(verificationCode.getVerificationCodeEnum().getStatusName(), VerificationCodeEnum.FORGET_PASSWORD.getStatusName())){
            log.error("profile with nationalCode ({}) exist but otp with type ({}) is not found!!!", username, VerificationCodeEnum.FORGET_PASSWORD.getStatusName());
            throw new InternalServiceException("otp not found", StatusService.OTP_NOT_FOUND, HttpStatus.OK, null);
        }

        //add 2 min for set new password
        Date addTwoMinToExpireTimeOtp = new Date(verificationCode.getExpireTime().getTime() + TimeUnit.MINUTES.toMillis(2));

        if(addTwoMinToExpireTimeOtp.before(new Date())){
            log.info("time for set new password is expire, nationalCode ({}), otp ({}) this request", username, otp);
            throw new InternalServiceException("system can not check valid authorize", StatusService.EXPIRE_TIME_FOR_UPDATE_PASSWORD, HttpStatus.OK, null);
        }

        profileEntity.setPassword(Helper.encodePassword(passwordEncoder, profileEntity.getUsername(), password));
        profileEntity.setUpdatedAt(new Date());
        verificationCodeService.deleteAll(username, VerificationCodeEnum.FORGET_PASSWORD);
        profileService.save(profileEntity);
    }

    @Override
    public SendOtpRegisterResponse sendOtp(String nationalCode, String mobileNumber, String ip) throws InternalServiceException {

        profileService.ensureNationalCodeUnique(nationalCode);
        verificationCodeService.checkTooManySendOtp(nationalCode, VerificationCodeService.STATUS_CREATE, VerificationCodeEnum.REGISTER);

        TempRegisterProfileEntity tempRegisterProfileEntity = new TempRegisterProfileEntity();
        tempRegisterProfileEntity.setNationalCode(nationalCode);
        tempRegisterProfileEntity.setMobile(mobileNumber);
        tempRegisterProfileEntity.setTempUuid(ThreadContext.get("uuid"));
        tempRegisterProfileEntity.setIp(ip);
        tempRegisterProfileEntity.setCreatedAt(new Date());
        tempRegisterProfileEntity.setCreatedBy(nationalCode);
        tempRegisterProfileEntity.setStep(RegisterProfileStepStatus.SEND_OTP);
        tempRegisterProfileEntity.setExpireTime(DateUtils.getNNextMinutes(new Date(),Integer.parseInt(settingService.getSetting(SettingService.MAX_REGISTER_EXPIRE_TIME_MINUTES).getValue())));
        tempRegisterProfileService.save(tempRegisterProfileEntity);


        int numberGenerated = Helper.generateRandomNumber();
        String randomNumber = Utility.pad(String.valueOf(numberGenerated), Integer.parseInt(settingService.getSetting(SettingService.LENGTH_OTP).getValue()));//generate otp
        log.info("generate random number ({}) for nationalCode ({})", randomNumber, nationalCode);
        VerificationCodeEntity verificationCode = verificationCodeService.save(nationalCode, randomNumber, VerificationCodeEnum.REGISTER);
        String template = settingService.getSetting(SettingService.SMS_OTP_TEMPLATE).getValue();
        messageService.send(String.format(template, randomNumber), mobileNumber);
        return helper.fillOtpRegisterResponse(nationalCode, ThreadContext.get("uuid"), verificationCode.getExpireTime().getTime());

    }

    @Override
    public SendShahkarRegisterResponse checkShahkar(String nationalCode, String tempUuid, String otp, String ip) throws InternalServiceException {

        TempRegisterProfileEntity tempRegisterProfileEntity = tempRegisterProfileService.findByTempUuidAndNationalCode(tempUuid, nationalCode);

        Optional<String> profileOptional = Arrays.stream(environment.getActiveProfiles()).findAny();
        if (profileOptional.isPresent() && (profileOptional.get().equalsIgnoreCase("staging") || profileOptional.get().equalsIgnoreCase("dev"))) {
            log.info("this is a not production env and accept any code!!");
            if (!nationalCode.substring(0, 5).equalsIgnoreCase(otp)) {
                log.error("code ({}) for nationalCode ({}) is not found", otp, nationalCode);
                throw new InternalServiceException("otp (" + otp + ") for mobile (" + nationalCode + ") is not found", StatusService.OTP_NOT_FOUND, HttpStatus.OK, null);
            }
        } else {
            verificationCodeService.findFirstByProfileAndCodeAndStatusAndType(nationalCode, otp, VerificationCodeEnum.REGISTER);
            tempRegisterProfileEntity.setStep(RegisterProfileStepStatus.VERIFY_OTP);
            tempRegisterProfileService.save(tempRegisterProfileEntity);
        }

        //check shahkar
        boolean isNew = true;
        if (profileOptional.isPresent() && (profileOptional.get().equalsIgnoreCase("test") || profileOptional.get().equalsIgnoreCase("staging") || profileOptional.get().equalsIgnoreCase("dev"))) {
            isNew = false;
        }

        Boolean shahkarInfo = shahkarInfoOperationService.checkShahkarInfo(tempRegisterProfileEntity.getMobile(), nationalCode, isNew);
        if (Boolean.FALSE.equals(shahkarInfo)) {
            tempRegisterProfileEntity.setStep(RegisterProfileStepStatus.FAIL_SHAHKAR);
            tempRegisterProfileService.save(tempRegisterProfileEntity);
            log.info("shahkar for mobile ({}), nationalCode ({}) is ({})", tempRegisterProfileEntity.getMobile(), nationalCode, shahkarInfo);
            throw new InternalServiceException("shahkar not match", StatusService.MOBILE_AND_NATIONAL_CODE_NOT_MATCH, HttpStatus.OK);
        }

        tempRegisterProfileEntity.setStep(RegisterProfileStepStatus.SHAHKAR);
        tempRegisterProfileService.save(tempRegisterProfileEntity);

        return helper.fillShahkarRegisterResponse(nationalCode, tempUuid);
    }


    @Override
    public SendOtpRegisterResponse resendOtp(String tempUuid, String nationalCode, String mobile) throws InternalServiceException {
        TempRegisterProfileEntity tempRegisterProfileEntity = tempRegisterProfileService.findByTempUuidAndNationalCode(tempUuid, nationalCode);

        tempRegisterProfileEntity.setMobile(mobile);
        tempRegisterProfileService.save(tempRegisterProfileEntity);

        verificationCodeService.checkTooManySendOtp(nationalCode, VerificationCodeService.STATUS_CREATE, VerificationCodeEnum.LOGIN);
        int numberGenerated = helper.generateRandomNumber();
        String randomNumber = Utility.pad(String.valueOf(numberGenerated), Integer.parseInt(settingService.getSetting(SettingService.LENGTH_OTP).getValue()));//generate otp
        log.info("generate random number ({})", randomNumber);
        VerificationCodeEntity verificationCode = verificationCodeService.save(nationalCode, randomNumber, VerificationCodeEnum.LOGIN);
        String template = settingService.getSetting(SettingService.SMS_OTP_TEMPLATE).getValue();
        messageService.send(String.format(template, randomNumber), mobile);
        return helper.fillOtpRegisterResponse(nationalCode, ThreadContext.get("uuid"), verificationCode.getExpireTime().getTime());
    }

    @Override
    public void register(String nationalCode, String tempUuid, String password, String ip, String repeatPassword, PasswordEncoder bcryptEncoder) throws InternalServiceException {

        //check exist tempUuid
        TempRegisterProfileEntity tempRegisterProfileEntity = tempRegisterProfileService.findByTempUuidAndNationalCode(tempUuid, nationalCode);

        if(tempRegisterProfileEntity.getStep() != RegisterProfileStepStatus.SHAHKAR) {
            log.error("tempuuid ({}) with nationalCode ({}) is not valid step ({})", tempUuid, nationalCode, tempRegisterProfileEntity.getStep());
            throw new InternalServiceException("tempUuid for nationalCode not valid step", StatusService.GENERAL_ERROR, HttpStatus.FORBIDDEN);
        }

        if(tempRegisterProfileEntity.getExpireTime().before(new Date())) {
            log.error("tempuuid ({}) with nationalCode ({}) is expire at time ({})", tempUuid, nationalCode, tempRegisterProfileEntity.getExpireTime());
            throw new InternalServiceException("tempUuid for nationalCode not valid step", StatusService.REGISTER_TIME_IS_EXPIRE, HttpStatus.FORBIDDEN);
        }

        if(!password.equals(repeatPassword)){
            log.error("password ({}) with repeat password ({}) is not same!!!", password, repeatPassword);
            throw new InternalServiceException("password is not same", StatusService.PASSWORD_NOT_MATCH, HttpStatus.OK, null);
        }

        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setNationalCode(nationalCode);
        profileEntity.setPassword(Helper.encodePassword(bcryptEncoder, nationalCode, password));
        profileEntity.setMobile(tempRegisterProfileEntity.getMobile());
        profileEntity.setStatus(ProfileStatusEnum.REGISTER.getText());
        profileEntity.setLevel(ProfileLevelEnum.BORONZ.getText());
        profileEntity.setCreatedBy(nationalCode);
        profileEntity.setCreatedAt(new Date());
        profileEntity.setTowFactorAuthentication(false);
        profileService.save(profileEntity);
        roleService.addProfileToRole(profileEntity, RoleService.WEB_PROFILE);
    }
}
