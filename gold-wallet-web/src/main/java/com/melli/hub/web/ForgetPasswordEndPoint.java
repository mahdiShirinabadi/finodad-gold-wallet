package com.melli.hub.web;

import com.melli.hub.domain.request.login.ForgetPasswordOtpRequestJson;
import com.melli.hub.domain.request.login.ForgetPasswordRequestJson;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.login.ForgetPasswordCheckOtpResponse;
import com.melli.hub.domain.response.login.ForgetPasswordProfileResponse;
import com.melli.hub.domain.response.login.ForgetPasswordUpdatePasswordRequestJson;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.RegistrationService;
import com.melli.hub.util.Utility;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Class Name: AuthenticationEndPoint
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profile/forgetPassword")
@Validated
@Log4j2
public class ForgetPasswordEndPoint extends WebEndPoint{

    private final RegistrationService registrationService;


    @Operation(summary = "فراموشی رمز عبور", description = "register customer")
    @PostMapping(value = "/firstStep")
    public ResponseEntity<BaseResponse<ForgetPasswordProfileResponse>> forgetPassword(@Valid @RequestBody ForgetPasswordRequestJson requestJson, HttpServletRequest httpRequest) throws InternalServiceException {
        log.info("start call method ({}) reset password user with email ({}) from Ip ({})", Utility.getCallerMethodName(), requestJson.getNationalCode(), getIP(httpRequest));
        ForgetPasswordProfileResponse forgetPasswordProfileResponse = registrationService.forgetPassword(requestJson.getNationalCode(), requestJson.getMobileNumber());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,forgetPasswordProfileResponse));
    }

    @Operation(summary = "فراموشی رمز عبور - بررسی کد یکبار مصرف", description = "register customer")
    @PostMapping(value = "/checkOtp")
    public ResponseEntity<BaseResponse<ForgetPasswordCheckOtpResponse>> forgetPasswordCheckOtp(@Valid @RequestBody ForgetPasswordOtpRequestJson requestJson, HttpServletRequest httpRequest) throws InternalServiceException {
        log.info("start call method ({}) forget password user with nationalCode ({}) from Ip ({})", Utility.getCallerMethodName(), requestJson.getNationalCode(), getIP(httpRequest));
        ForgetPasswordCheckOtpResponse forgetPasswordCheckOtpResponse = registrationService.forgetPasswordCheckOtp(requestJson.getOtp(),requestJson.getNationalCode(), requestJson.getHashData());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, forgetPasswordCheckOtpResponse));
    }

    @Operation(summary = "فراموشی رمز عبور - تعیین رمز عبور ", description = "register customer")
    @PostMapping(value = "/updatePassword")
    public ResponseEntity<BaseResponse<ForgetPasswordCheckOtpResponse>> forgetPasswordCheckOtp(@Valid @RequestBody ForgetPasswordUpdatePasswordRequestJson requestJson, HttpServletRequest httpRequest) throws InternalServiceException {
        log.info("start call method ({}) set password user with nationalCode ({}) from Ip ({})", Utility.getCallerMethodName(), requestJson.getNationalCode(), getIP(httpRequest));
        registrationService.forgetPasswordSetPassword(requestJson.getOtp(),requestJson.getNationalCode(), requestJson.getPassword(), requestJson.getRepeatPassword(), requestJson.getHashData());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
    }
}
