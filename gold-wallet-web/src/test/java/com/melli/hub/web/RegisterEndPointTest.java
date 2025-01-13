package com.melli.hub.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.melli.hub.GoldApplicationTests;
import com.melli.hub.domain.enumaration.ProfileStatusEnum;
import com.melli.hub.domain.enumaration.VerificationCodeEnum;
import com.melli.hub.domain.master.entity.ProfileEntity;
import com.melli.hub.domain.master.entity.SettingEntity;
import com.melli.hub.domain.master.entity.VerificationCodeEntity;
import com.melli.hub.domain.request.login.*;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.login.*;
import com.melli.hub.service.*;
import com.melli.hub.util.Utility;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.ObjectUtils;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Log4j2
@DisplayName("RegisterEndPointTest End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RegisterEndPointTest extends GoldApplicationTests {
    private static final String BASE_URL_REGISTRATION = "/api/v1/profile/registration";
    private static final String BASE_URL_FORGET_PASSWORD = "/api/v1/profile/forgetPassword";
    private static final String BASE_URL_AUTHENTICATION = "/api/v1/profile/authentication";

    private static final String VALID_NATIONAL_CODE = "0077847660";
    private static final String VALID_NATIONAL_CODE_2 = "2980511481";
    private static final String VALID_MOBILE_NUMBER = "09124162337";
    private static final String VALID_MOBILE_NUMBER_NOT_MATCH = "09124162338";

    private static final String INVALID_NATIONAL_CODE = "007784766";
    private static final String INVALID_NATIONAL_CODE_WITH_SUCCESS_FORMAT = "0077847661";
    private static final String INVALID_MOBILE_NUMBER = "0912416233";

    private static final String SIMPLE_PASSWORD_ONLY_NUMBER = "12345678";
    private static final String SIMPLE_PASSWORD_ONLY_UPPER = "12345ABC";
    private static final String SIMPLE_PASSWORD_ONLY_LOWER = "12345abc";
    private static final String CORRECT_PASSWORD = "AliMahdi1254";

    private static MockMvc mockMvc;
    private static String token;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ProfileBlockService profileBlockService;

    @Autowired
    private SecurityService securityService;

    @Test
    @Order(1)
    @DisplayName("Initiate...")
    void initial() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);
        boolean success = setupDB();
        Assert.assertTrue(success);
    }


    //region SenOtp
    @Test
    @Order(2)
    @DisplayName("register send otp success")
    void registration_send_otp_success() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        BaseResponse<SendOtpRegisterResponse> response = registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.REGISTER);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());

    }

    @Test
    @Order(3)
    @DisplayName("register send otp invalid nationalCode fail")
    void registration_send_otp_invalid_nationalCode() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = INVALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        BaseResponse<SendOtpRegisterResponse> response = registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false, "1.1.1.1");
        log.info("start send Otp registration test with response ({})", response);
    }

    @Test
    @Order(4)
    @DisplayName("register send otp invalid mobileNumber fail ")
    void registration_send_otp_invalid_mobile() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = INVALID_MOBILE_NUMBER;
        BaseResponse<SendOtpRegisterResponse> response = registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false, "1.1.1.1");
        log.info("start send Otp registration test with response ({})", response);
    }


    @Test
    @Order(5)
    @DisplayName("register send otp to many request")
    void registration_send_otp_to_many_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE_2;
        String mobileNumber = VALID_MOBILE_NUMBER;
        registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.TOO_MANY_OTP, false, "1.1.1.1");
        registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.TOO_MANY_OTP, false, "1.1.1.1");
    }

    @Test
    @Order(6)
    @DisplayName("register send otp to many request")
    void registration_send_otp_to_invalid_national_code() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = INVALID_NATIONAL_CODE_WITH_SUCCESS_FORMAT;
        String mobileNumber = VALID_MOBILE_NUMBER;
        registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false, "1.1.1.1");
    }

    //endregion


    //region validOtpAndShahkar
    @Test
    @Order(20)
    @DisplayName("valid otp and shahkar success")
    void registration_shahkar_valid_otp_success() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        verificationCodeService.deleteAll(nationalCode, VerificationCodeEnum.REGISTER);
        BaseResponse<SendOtpRegisterResponse> responseSendOtp = registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.REGISTER);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        BaseResponse<SendShahkarRegisterResponse> response = registerValidateShahkarAndOtp(nationalCode, mobileNumber, verificationCodeEntity.getCode(), responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());
    }

    @Test
    @Order(21)
    @DisplayName("shahkar not match fail")
    void registration_shahkar_no_match_valid_otp_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER_NOT_MATCH;
        verificationCodeService.deleteAll(nationalCode, VerificationCodeEnum.REGISTER);
        BaseResponse<SendOtpRegisterResponse> responseSendOtp = registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.REGISTER);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        BaseResponse<SendShahkarRegisterResponse> response = registerValidateShahkarAndOtp(nationalCode, mobileNumber, verificationCodeEntity.getCode(), responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.MOBILE_AND_NATIONAL_CODE_NOT_MATCH, false, "1.1.1.1");
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());
    }


    @Test
    @Order(22)
    @DisplayName("not found otp- fail")
    void registration_shahkar_not_found_otp_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER_NOT_MATCH;
        verificationCodeService.deleteAll(nationalCode, VerificationCodeEnum.REGISTER);
        BaseResponse<SendOtpRegisterResponse> responseSendOtp = registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.REGISTER);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        BaseResponse<SendShahkarRegisterResponse> response = registerValidateShahkarAndOtp(nationalCode, mobileNumber, "1111", responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.OTP_NOT_FOUND, false, "1.1.1.1");
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());
    }


    @Test
    @Order(23)
    @DisplayName("otp expire fail")
    void registration_shahkar_expire_otp_fail() throws Exception {
        log.info("start send Otp registration test");

        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER_NOT_MATCH;
        verificationCodeService.deleteAll(nationalCode, VerificationCodeEnum.REGISTER);

        //set setting to zero
        SettingEntity settingEntity = settingService.getSetting(SettingService.MAX_OTP_EXPIRE_TIME_MINUTES);
        String maxExpireTimeDefault = settingEntity.getValue();
        settingEntity.setValue("0");
        settingService.save(settingEntity);
        settingService.clearCache();

        BaseResponse<SendOtpRegisterResponse> responseSendOtp = registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.REGISTER);

        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }

        BaseResponse<SendShahkarRegisterResponse> response = registerValidateShahkarAndOtp(nationalCode, mobileNumber, verificationCodeEntity.getCode(), responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.OTP_EXPIRE, false, "1.1.1.1");
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());
        settingEntity.setValue(maxExpireTimeDefault);
        settingService.save(settingEntity);
        settingService.clearCache();
    }

    @Test
    @Order(24)
    @DisplayName("temp uui not valid fail")
    void registration_shahkar_temp_uuid_not_valid_fail() throws Exception {
        log.info("start send Otp registration test");

        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER_NOT_MATCH;
        verificationCodeService.deleteAll(nationalCode, VerificationCodeEnum.REGISTER);

        //set setting to zero
        BaseResponse<SendOtpRegisterResponse> responseSendOtp = registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.REGISTER);

        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }

        BaseResponse<SendShahkarRegisterResponse> response = registerValidateShahkarAndOtp(nationalCode, mobileNumber, verificationCodeEntity.getCode(), responseSendOtp.getData().getTempUuid().concat("1"), HttpStatus.FORBIDDEN, StatusService.GENERAL_ERROR, false, "1.1.1.1");
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());
    }

    //endregion


    //region register
    @Test
    @Order(30)
    @DisplayName("register password not valid fail")
    void registration_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        verificationCodeService.deleteAll(nationalCode, VerificationCodeEnum.REGISTER);
        BaseResponse<SendOtpRegisterResponse> responseSendOtp = registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.REGISTER);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        BaseResponse<SendShahkarRegisterResponse> response = registerValidateShahkarAndOtp(nationalCode, mobileNumber, verificationCodeEntity.getCode(), responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());

        register(nationalCode, mobileNumber, CORRECT_PASSWORD, "123323", responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.PASSWORD_NOT_MATCH, false, "1.1.1.1");
        register(nationalCode, mobileNumber, SIMPLE_PASSWORD_ONLY_NUMBER, SIMPLE_PASSWORD_ONLY_NUMBER, responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false, "1.1.1.1");
        register(nationalCode, mobileNumber, SIMPLE_PASSWORD_ONLY_LOWER, SIMPLE_PASSWORD_ONLY_LOWER, responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false, "1.1.1.1");
        register(nationalCode, mobileNumber, SIMPLE_PASSWORD_ONLY_UPPER, SIMPLE_PASSWORD_ONLY_UPPER, responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false, "1.1.1.1");
    }


    @Test
    @Order(31)
    @DisplayName("register temp uuid not valid fail")
    void registration_temp_uuid_not_valid_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        verificationCodeService.deleteAll(nationalCode, VerificationCodeEnum.REGISTER);
        BaseResponse<SendOtpRegisterResponse> responseSendOtp = registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.REGISTER);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        BaseResponse<SendShahkarRegisterResponse> response = registerValidateShahkarAndOtp(nationalCode, mobileNumber, verificationCodeEntity.getCode(), responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());

        register(nationalCode, mobileNumber, CORRECT_PASSWORD, CORRECT_PASSWORD, responseSendOtp.getData().getTempUuid() + "1", HttpStatus.FORBIDDEN, StatusService.GENERAL_ERROR, false, "1.1.1.1");
    }

    @Test
    @Order(32)
    @DisplayName("register - user not pass shahkar- fail")
    void registration_user_not_pass_shahkar_step_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        verificationCodeService.deleteAll(nationalCode, VerificationCodeEnum.REGISTER);
        BaseResponse<SendOtpRegisterResponse> responseSendOtp = registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.REGISTER);
        if (verificationCodeEntity == null) {
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        register(nationalCode, mobileNumber, CORRECT_PASSWORD, CORRECT_PASSWORD, responseSendOtp.getData().getTempUuid() + "1", HttpStatus.FORBIDDEN, StatusService.GENERAL_ERROR, false, "1.1.1.1");
    }


    @Test
    @Order(33)
    @DisplayName("register - register time expire- fail")
    void registration_register_time_expire_fail() throws Exception {
        log.info("start send Otp registration test");

        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;

        verificationCodeService.deleteAll(nationalCode, VerificationCodeEnum.REGISTER);

        //set setting to zero
        SettingEntity settingEntity = settingService.getSetting(SettingService.MAX_REGISTER_EXPIRE_TIME_MINUTES);
        String maxExpireTimeDefault = settingEntity.getValue();
        settingEntity.setValue("0");
        settingService.save(settingEntity);
        settingService.clearCache();


        BaseResponse<SendOtpRegisterResponse> responseSendOtp = registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.REGISTER);
        if (verificationCodeEntity == null) {
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }

        registerValidateShahkarAndOtp(nationalCode, mobileNumber, verificationCodeEntity.getCode(), responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");

        register(nationalCode, mobileNumber, CORRECT_PASSWORD, CORRECT_PASSWORD, responseSendOtp.getData().getTempUuid(), HttpStatus.FORBIDDEN, StatusService.REGISTER_TIME_IS_EXPIRE, false, "1.1.1.1");
        settingEntity.setValue(maxExpireTimeDefault);
        settingService.save(settingEntity);
        settingService.clearCache();
    }


    @Test
    @Order(34)
    @DisplayName("register success")
    void registration_success() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        verificationCodeService.deleteAll(nationalCode, VerificationCodeEnum.REGISTER);
        BaseResponse<SendOtpRegisterResponse> responseSendOtp = registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.REGISTER);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        BaseResponse<SendShahkarRegisterResponse> response = registerValidateShahkarAndOtp(nationalCode, mobileNumber, verificationCodeEntity.getCode(), responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());

        register(nationalCode, mobileNumber, CORRECT_PASSWORD, CORRECT_PASSWORD, responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
    }


    @Test
    @Order(35)
    @DisplayName("register national code exist")
    void registration_nationalcode_exist_fail() throws Exception {
        log.info("start send Otp registration test");
        registerSendOtp(VALID_NATIONAL_CODE, VALID_MOBILE_NUMBER, HttpStatus.OK, StatusService.NATIONAL_CODE_EXIT, false, "1.1.1.1");
    }


    //endregion

    //region SenOtp
    @Test
    @Order(40)
    @DisplayName("forgetPassword- success")
    void forget_password_success() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        BaseResponse<ForgetPasswordProfileResponse> response = changePasswordFirstStep(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.FORGET_PASSWORD);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());
    }


    @Test
    @Order(41)
    @DisplayName("forgetPassword- nationalCode not found -fail")
    void forget_password_nationalCode_not_found_fail() throws Exception {
        log.info("start send Otp registration test");
        changePasswordFirstStep(VALID_NATIONAL_CODE_2, VALID_MOBILE_NUMBER, HttpStatus.OK, StatusService.PROFILE_NOT_FOUND, false, "1.1.1.1");
    }

    @Test
    @Order(42)
    @DisplayName("forgetPassword-too many request- fail")
    void forget_password_too_many_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        verificationCodeService.deleteAll(nationalCode, VerificationCodeEnum.FORGET_PASSWORD);
        BaseResponse<ForgetPasswordProfileResponse> response = changePasswordFirstStep(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.FORGET_PASSWORD);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());
        changePasswordFirstStep(nationalCode, mobileNumber, HttpStatus.OK, StatusService.TOO_MANY_OTP, false, "1.1.1.1");
    }


    //endregion


    //region checkOtp
    @Test
    @Order(50)
    @DisplayName("not found otp- fail")
    void forget_password_not_found_otp_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        verificationCodeService.deleteAll(nationalCode, VerificationCodeEnum.FORGET_PASSWORD);

        BaseResponse<ForgetPasswordProfileResponse> responseFirstStep = changePasswordFirstStep(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");

        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.FORGET_PASSWORD);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        changePasswordCheckOtp(nationalCode, mobileNumber, responseFirstStep.getData().getRegisterHash(), "11111", HttpStatus.OK, StatusService.OTP_NOT_FOUND, false, "1.1.1.1");
    }


    @Test
    @Order(51)
    @DisplayName("otp expire fail")
    void forget_password_expire_otp_fail() throws Exception {
        log.info("start send Otp registration test");

        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        verificationCodeService.deleteAll(nationalCode, VerificationCodeEnum.FORGET_PASSWORD);

        //set setting to zero
        SettingEntity settingEntity = settingService.getSetting(SettingService.MAX_OTP_EXPIRE_TIME_MINUTES);
        String maxExpireTimeDefault = settingEntity.getValue();
        settingEntity.setValue("0");
        settingService.save(settingEntity);
        settingService.clearCache();

        BaseResponse<ForgetPasswordProfileResponse> responseFirstStep = changePasswordFirstStep(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.FORGET_PASSWORD);

        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }

        changePasswordCheckOtp(nationalCode, mobileNumber, responseFirstStep.getData().getRegisterHash(), verificationCodeEntity.getCode(), HttpStatus.OK, StatusService.OTP_EXPIRE, false, "1.1.1.1");
        settingEntity.setValue(maxExpireTimeDefault);
        settingService.save(settingEntity);
        settingService.clearCache();
    }

    @Test
    @Order(60)
    @DisplayName("forgetPassword-complete- success")
    void forget_password_complete_success() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        BaseResponse<ForgetPasswordProfileResponse> response = changePasswordFirstStep(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.FORGET_PASSWORD);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        changePasswordCheckOtp(nationalCode, mobileNumber, response.getData().getRegisterHash(), verificationCodeEntity.getCode(), HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());
        changePasswordUpdatePassword(nationalCode, response.getData().getRegisterHash(), verificationCodeEntity.getCode(), CORRECT_PASSWORD, CORRECT_PASSWORD, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
    }


    @Test
    @Order(61)
    @DisplayName("forgetPassword-change hashValue- fail")
    void forget_password_complete_change_hash_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        BaseResponse<ForgetPasswordProfileResponse> response = changePasswordFirstStep(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.FORGET_PASSWORD);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        changePasswordCheckOtp(nationalCode, mobileNumber, response.getData().getRegisterHash(), verificationCodeEntity.getCode(), HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());
        changePasswordUpdatePassword(nationalCode, response.getData().getRegisterHash().concat("1"), verificationCodeEntity.getCode(), CORRECT_PASSWORD, CORRECT_PASSWORD, HttpStatus.FORBIDDEN, StatusService.GENERAL_ERROR, false, "1.1.1.1");
    }

    @Test
    @Order(62)
    @DisplayName("forgetPassword-change invalid otp- fail")
    void forget_password_complete_invalid_otp_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        BaseResponse<ForgetPasswordProfileResponse> response = changePasswordFirstStep(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.FORGET_PASSWORD);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        changePasswordCheckOtp(nationalCode, mobileNumber, response.getData().getRegisterHash(), verificationCodeEntity.getCode(), HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());
        changePasswordUpdatePassword(nationalCode, response.getData().getRegisterHash(), "1111", CORRECT_PASSWORD, CORRECT_PASSWORD, HttpStatus.OK, StatusService.OTP_NOT_FOUND, false, "1.1.1.1");
    }

    @Test
    @Order(63)
    @DisplayName("forgetPassword-change password not match- fail")
    void forget_password_complete_password_not_match_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;
        String mobileNumber = VALID_MOBILE_NUMBER;
        BaseResponse<ForgetPasswordProfileResponse> response = changePasswordFirstStep(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.FORGET_PASSWORD);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        changePasswordCheckOtp(nationalCode, mobileNumber, response.getData().getRegisterHash(), verificationCodeEntity.getCode(), HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());
        changePasswordUpdatePassword(nationalCode, response.getData().getRegisterHash(), verificationCodeEntity.getCode(), CORRECT_PASSWORD, "123456", HttpStatus.OK, StatusService.PASSWORD_NOT_MATCH, false, "1.1.1.1");
    }


    //login
    @Test
    @Order(100)
    @DisplayName("login- fail")
    void login_user_not_register_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode;
        String password;
        String additionalData = "login fail";
        String deviceName = "test";
        nationalCode = VALID_NATIONAL_CODE_2;
        password = SIMPLE_PASSWORD_ONLY_LOWER;
        login(nationalCode, password, additionalData, deviceName, HttpStatus.UNAUTHORIZED, StatusService.INVALID_USERNAME_PASSWORD, false, "1.1.1.1");
    }


    @Test
    @Order(101)
    @DisplayName("login- success")
    void login_success() throws Exception {
        log.info("start login test");
        String nationalCode;
        String password;
        String additionalData = "login success";
        String deviceName = "test";
        nationalCode = VALID_NATIONAL_CODE;
        password = CORRECT_PASSWORD;
        BaseResponse<LoginResponse> response = login(nationalCode, password, additionalData, deviceName, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
    }


    @Test
    @Order(101)
    @DisplayName("login with 2 factor and only wants login without otp and check generate otp and not generate token - success")
    void login_not_generate_token_wthout_otp_success() throws Exception {
        log.info("start login test");
        String nationalCode;
        String password;
        String additionalData = "login success";
        String deviceName = "test";
        nationalCode = VALID_NATIONAL_CODE;
        password = CORRECT_PASSWORD;

        //set user with 2 factor authentication
        ProfileEntity profileEntity = profileService.findByNationalCode(VALID_NATIONAL_CODE);
        profileEntity.setTowFactorAuthentication(true);
        profileService.save(profileEntity);

        BaseResponse<LoginResponse> response = login(nationalCode, password, additionalData, deviceName, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");

        if (response.getData().getAccessTokenObject() != null && response.getData().getAccessTokenObject().getToken() != null) {
            throw new Exception("access token created for two factor without get otp");
        }

        if (!response.getData().isTwoFactorAuthentication()) {
            throw new Exception("two factor authentication is false and system dont generate token");
        }

        VerificationCodeEntity verificationCode = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.LOGIN);
        if (verificationCode == null) {
            throw new Exception("otp not generated for nationalCode " + nationalCode);
        }

        profileEntity.setTowFactorAuthentication(false);
        profileService.save(profileEntity);
    }


    @Test
    @Order(102)
    @DisplayName("login with 2 factor and only wants login without otp and check generate otp and not generate token - success")
    void login_block_user_after_multi_incorrect_success() throws Exception {
        log.info("start login test");

        String nationalCode;
        String password;
        String additionalData = "login fail";
        String deviceName = "test";
        nationalCode = VALID_NATIONAL_CODE;
        password = SIMPLE_PASSWORD_ONLY_LOWER;

        ProfileEntity profileEntity = profileService.findByNationalCode(nationalCode);
        securityService.resetFailLoginCount(profileEntity);

        SettingEntity settingEntity = settingService.getSetting(SettingService.MAX_WRONG_PASSWORD_FOR_PROFILE);

        for (int i = 0; i < Integer.parseInt(settingEntity.getValue()); i++) {
            log.info("count row ({})", i);
            login(nationalCode, password, additionalData, deviceName, HttpStatus.UNAUTHORIZED, StatusService.INVALID_USERNAME_PASSWORD, false, "1.1.1.1");
        }
        login(nationalCode, CORRECT_PASSWORD, additionalData, deviceName, HttpStatus.FORBIDDEN, StatusService.PROFILE_IS_BLOCKED, false, "1.1.1.1");
        profileEntity.setStatus(ProfileStatusEnum.REGISTER.getText());
        profileService.save(profileEntity);
    }


    @Test
    @Order(110)
    @DisplayName("refreshToken - success")
    void refreshToken_success() throws Exception {
        log.info("start login test");

        String nationalCode;
        String additionalData = "login fail";
        String deviceName = "test";
        nationalCode = VALID_NATIONAL_CODE;


        ProfileEntity profileEntity = profileService.findByNationalCode(nationalCode);
        securityService.resetFailLoginCount(profileEntity);

        BaseResponse<LoginResponse> loginResponseResponse = login(nationalCode, CORRECT_PASSWORD, additionalData, deviceName, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        BaseResponse<LoginResponse> loginResponseResponseRefreshToken = refreshToken(nationalCode, loginResponseResponse.getData().getRefreshTokenObject().getToken(), additionalData, deviceName, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");

        if(loginResponseResponse.getData().getRefreshTokenObject().getToken().equals(loginResponseResponseRefreshToken.getData().getRefreshTokenObject().getToken())){
            throw new Exception("refreshToken is same and it's not correct");
        }
    }


    @Test
    @Order(110)
    @DisplayName("refreshToken- get refreshToken with old refreshToken - fail")
    void refreshToken_old_refresh_token_fail() throws Exception {
        log.info("start login test");

        String nationalCode;
        String additionalData = "old_refresh";
        String deviceName = "test";
        nationalCode = VALID_NATIONAL_CODE;

        ProfileEntity profileEntity = profileService.findByNationalCode(nationalCode);
        securityService.resetFailLoginCount(profileEntity);

        BaseResponse<LoginResponse> loginResponseResponse = login(nationalCode, CORRECT_PASSWORD, additionalData, deviceName, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        refreshToken(nationalCode, loginResponseResponse.getData().getRefreshTokenObject().getToken(), additionalData, deviceName, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        refreshToken(nationalCode, loginResponseResponse.getData().getRefreshTokenObject().getToken(), additionalData, deviceName, HttpStatus.UNAUTHORIZED, StatusService.REFRESH_TOKEN_NOT_FOUND, false, "1.1.1.1");
    }

    @Test
    @Order(111)
    @DisplayName("refreshToken- not belong to nationalCode - fail")
    void refreshToken_not_belog_user_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE_2;
        String mobileNumber = VALID_MOBILE_NUMBER;
        verificationCodeService.deleteAll(nationalCode, VerificationCodeEnum.REGISTER);
        BaseResponse<SendOtpRegisterResponse> responseSendOtp = registerSendOtp(nationalCode, mobileNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        VerificationCodeEntity verificationCodeEntity = verificationCodeService.findLastByProfileAndStatusAndType(nationalCode, String.valueOf(VerificationCodeService.STATUS_CREATE), VerificationCodeEnum.REGISTER);
        if (verificationCodeEntity == null) {
            log.info("verificationCodeEntity is null for nationalCode ({})", nationalCode);
            throw new Exception("verificationCodeEntity is null for nationalCode " + nationalCode);
        }
        BaseResponse<SendShahkarRegisterResponse> response = registerValidateShahkarAndOtp(nationalCode, mobileNumber, verificationCodeEntity.getCode(), responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        log.info("success send Otp registration test with response ({}) and verification code is ({})", response, verificationCodeEntity.getCode());
        register(nationalCode, mobileNumber, CORRECT_PASSWORD, CORRECT_PASSWORD, responseSendOtp.getData().getTempUuid(), HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");

        BaseResponse<LoginResponse> loginResponseResponse = login(nationalCode, CORRECT_PASSWORD, "test111", "test111", HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");

        refreshToken(VALID_NATIONAL_CODE, loginResponseResponse.getData().getRefreshTokenObject().getToken(), "refreshTokenDiffNationalCode", "test111", HttpStatus.UNAUTHORIZED, StatusService.REFRESH_TOKEN_NOT_BELONG_TO_PROFILE, false, "1.1.1.1");

    }


    @Test
    @Order(112)
    @DisplayName("refreshToken- is expire - fail")
    void refreshToken_expire_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;

        SettingEntity settingEntity = settingService.getSetting(SettingService.DURATION_REFRESH_TOKEN_PROFILE);
        String defaultSetting = settingEntity.getValue();
        settingEntity.setValue("1");
        settingService.save(settingEntity);
        settingService.clearCache();

        BaseResponse<LoginResponse> loginResponseResponse = login(nationalCode, CORRECT_PASSWORD, "test111", "test111", HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        Thread.sleep(1001);
        refreshToken(VALID_NATIONAL_CODE, loginResponseResponse.getData().getRefreshTokenObject().getToken(), "refreshTokenDiffNationalCode", "test111", HttpStatus.UNAUTHORIZED, StatusService.REFRESH_TOKEN_IS_EXPIRE, false, "1.1.1.1");
        settingEntity.setValue(defaultSetting);
        settingService.save(settingEntity);
        settingService.clearCache();
    }


    @Test
    @Order(113)
    @DisplayName("accessToken- is expire - login again - success")
    void login_access_token_fail() throws Exception {
        log.info("start send Otp registration test");
        String nationalCode = VALID_NATIONAL_CODE;

        SettingEntity settingEntity = settingService.getSetting(SettingService.DURATION_ACCESS_TOKEN_PROFILE);
        String defaultSetting = settingEntity.getValue();
        settingEntity.setValue("1");
        settingService.save(settingEntity);
        settingService.clearCache();
        login(nationalCode, CORRECT_PASSWORD, "test111", "test111", HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        Thread.sleep(1001);
        login(nationalCode, CORRECT_PASSWORD, "test111", "test111", HttpStatus.OK, StatusService.SUCCESSFUL, true, "1.1.1.1");
        settingEntity.setValue(defaultSetting);
        settingService.save(settingEntity);
        settingService.clearCache();
    }


    private BaseResponse<SendOtpRegisterResponse> register(String nationalCode, String mobileNumber, String password, String repeatPassword, String tempUuid, HttpStatus httpStatus, int errorCode, boolean success, String clientIp) throws Exception {
        SignUpPasswordRequestJson requestJson = new SignUpPasswordRequestJson();
        requestJson.setNationalCode(nationalCode);
        requestJson.setMobileNumber(mobileNumber);
        requestJson.setTempUuid(tempUuid);
        requestJson.setPassword(password);
        requestJson.setRepeatPassword(repeatPassword);

        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, BASE_URL_REGISTRATION + "/register", Utility.mapToJsonOrNull(requestJson), clientIp);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);

        TypeReference<BaseResponse<SendOtpRegisterResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }


    private BaseResponse<SendOtpRegisterResponse> registerSendOtp(String nationalCode, String mobileNumber, HttpStatus httpStatus, int errorCode, boolean success, String clientIp) throws Exception {
        SignUpOtpRequestJson requestJson = new SignUpOtpRequestJson();
        requestJson.setNationalCode(nationalCode);
        requestJson.setMobileNumber(mobileNumber);

        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, BASE_URL_REGISTRATION + "/sendOtp", Utility.mapToJsonOrNull(requestJson), clientIp);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);

        TypeReference<BaseResponse<SendOtpRegisterResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }


    private BaseResponse<SendShahkarRegisterResponse> registerValidateShahkarAndOtp(String nationalCode, String mobileNumber, String otp, String tempUuid, HttpStatus httpStatus, int errorCode, boolean success, String clientIp) throws Exception {
        SignUpShahkarRequestJson requestJson = new SignUpShahkarRequestJson();
        requestJson.setNationalCode(nationalCode);
        requestJson.setMobileNumber(mobileNumber);
        requestJson.setOtp(otp);
        requestJson.setTempUuid(tempUuid);


        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, BASE_URL_REGISTRATION + "/checkShahkar", Utility.mapToJsonOrNull(requestJson), clientIp);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);

        TypeReference<BaseResponse<SendShahkarRegisterResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }


    private BaseResponse<ForgetPasswordProfileResponse> changePasswordFirstStep(String nationalCode, String mobileNumber, HttpStatus httpStatus, int errorCode, boolean success, String clientIp) throws Exception {
        ForgetPasswordRequestJson requestJson = new ForgetPasswordRequestJson();
        requestJson.setNationalCode(nationalCode);
        requestJson.setMobileNumber(mobileNumber);

        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, BASE_URL_FORGET_PASSWORD + "/firstStep", Utility.mapToJsonOrNull(requestJson), clientIp);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);

        TypeReference<BaseResponse<ForgetPasswordProfileResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    private BaseResponse<ForgetPasswordCheckOtpResponse> changePasswordCheckOtp(String nationalCode, String mobileNumber, String registerHash, String otp, HttpStatus httpStatus, int errorCode, boolean success, String clientIp) throws Exception {
        ForgetPasswordOtpRequestJson requestJson = new ForgetPasswordOtpRequestJson();
        requestJson.setNationalCode(nationalCode);
        requestJson.setOtp(mobileNumber);
        requestJson.setHashData(registerHash);
        requestJson.setOtp(otp);

        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, BASE_URL_FORGET_PASSWORD + "/checkOtp", Utility.mapToJsonOrNull(requestJson), clientIp);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);

        TypeReference<BaseResponse<ForgetPasswordCheckOtpResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    private BaseResponse<ForgetPasswordCheckOtpResponse> changePasswordUpdatePassword(String nationalCode, String hashData, String otp, String password, String repeatPassword, HttpStatus httpStatus, int errorCode, boolean success, String clientIp) throws Exception {
        ForgetPasswordUpdatePasswordRequestJson requestJson = new ForgetPasswordUpdatePasswordRequestJson();
        requestJson.setNationalCode(nationalCode);
        requestJson.setHashData(hashData);
        requestJson.setOtp(otp);
        requestJson.setPassword(password);
        requestJson.setRepeatPassword(repeatPassword);

        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, BASE_URL_FORGET_PASSWORD + "/updatePassword", Utility.mapToJsonOrNull(requestJson), clientIp);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);

        TypeReference<BaseResponse<ForgetPasswordCheckOtpResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }


    private BaseResponse<LoginResponse> login(String nationalCode, String password, String additionalData, String deviceName, HttpStatus httpStatus, int errorCode, boolean success, String clientIp) throws Exception {
        LoginRequestJson requestJson = new LoginRequestJson();
        requestJson.setUsername(nationalCode);
        requestJson.setPassword(password);
        requestJson.setAdditionalData(additionalData);
        requestJson.setDeviceName(deviceName);

        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, BASE_URL_AUTHENTICATION + "/login", Utility.mapToJsonOrNull(requestJson), clientIp);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);

        TypeReference<BaseResponse<LoginResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }


    private BaseResponse<LoginResponse> refreshToken(String nationalCode, String refreshToken, String additionalData, String deviceName, HttpStatus httpStatus, int errorCode, boolean success, String clientIp) throws Exception {
        RefreshTokenRequestJson requestJson = new RefreshTokenRequestJson();
        requestJson.setNationalCode(nationalCode);
        requestJson.setRefreshToken(refreshToken);
        requestJson.setAdditionalData(additionalData);
        requestJson.setDeviceName(deviceName);

        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, BASE_URL_AUTHENTICATION + "/refreshToken", Utility.mapToJsonOrNull(requestJson), clientIp);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);

        TypeReference<BaseResponse<LoginResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }


    private BaseResponse<ObjectUtils.Null> resendOtp(String nationalCode, HttpStatus httpStatus, int errorCode, boolean success, String clientIp) throws Exception {
        ResendOtpRequestJson requestJson = new ResendOtpRequestJson();
        requestJson.setNationalCode(nationalCode);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, BASE_URL_AUTHENTICATION + "/resendOtp", Utility.mapToJsonOrNull(requestJson), clientIp);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);

        TypeReference<BaseResponse<ObjectUtils.Null>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }


    private BaseResponse<LoginResponse> checkOtp(String nationalCode, String additionalData, String otp, String password, String deviceName, HttpStatus httpStatus, int errorCode, boolean success, String clientIp) throws Exception {
        LoginWithOtpRequestJson requestJson = new LoginWithOtpRequestJson();
        requestJson.setUsername(nationalCode);
        requestJson.setPassword(password);
        requestJson.setOtp(otp);
        requestJson.setAdditionalData(additionalData);
        requestJson.setDeviceName(deviceName);

        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, BASE_URL_AUTHENTICATION + "/checkOtp", Utility.mapToJsonOrNull(requestJson), clientIp);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);

        TypeReference<BaseResponse<LoginResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }


}