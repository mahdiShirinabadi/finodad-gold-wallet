package com.melli.hub.web;

import com.melli.hub.domain.request.login.SignUpOtpRequestJson;
import com.melli.hub.domain.request.login.SignUpPasswordRequestJson;
import com.melli.hub.domain.request.login.SignUpShahkarRequestJson;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.login.SendOtpRegisterResponse;
import com.melli.hub.domain.response.login.SendShahkarRegisterResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.RegistrationService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@RequestMapping("/api/v1/profile/registration")
@Validated
@Log4j2
public class RegistrationEndPoint extends WebEndPoint{

    private final RegistrationService registrationService;
    private final PasswordEncoder passwordEncoder;


    @Timed(description = "time taken to sendOtp profile")
    @Operation(summary = "ثبت نام حساب کاربری - ارسال رمز یکبار مصرف (مرحله اول)")
    @PostMapping(value = "/sendOtp")
    public ResponseEntity<BaseResponse<SendOtpRegisterResponse>> sendOtp(@Valid @RequestBody SignUpOtpRequestJson requestJson, HttpServletRequest httpRequest) throws InternalServiceException {
        log.info("start send otp with data ({})", requestJson.toString());
        SendOtpRegisterResponse response = registrationService.sendOtp(requestJson.getNationalCode(), requestJson.getMobileNumber(), getIP(httpRequest));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @Timed(description = "time taken to checkShahkar profile")
    @Operation(summary = "ثبت نام حساب کاربری -چک کردن شاهکار(مرحله دوم)")
    @PostMapping(value = "/checkShahkar", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<BaseResponse<SendShahkarRegisterResponse>> validateOtp(@Valid @RequestBody SignUpShahkarRequestJson requestJson, HttpServletRequest httpRequest) throws InternalServiceException {
        log.info("start check shahkar with data ({})", requestJson.toString());
        SendShahkarRegisterResponse response = registrationService.checkShahkar(requestJson.getNationalCode(), requestJson.getTempUuid(), requestJson.getOtp(), getIP(httpRequest));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }


    @Timed(description = "time taken to register profile")
    @Operation(summary = "ثبت نام حساب کاربری(مرحله آخر)")
    @PostMapping(value = "/register")
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> register(@Valid @RequestBody SignUpPasswordRequestJson requestJson, HttpServletRequest httpRequest) throws InternalServiceException {
        log.info("start register with data ({})", requestJson.toString());
        registrationService.register(requestJson.getNationalCode(), requestJson.getTempUuid(), requestJson.getPassword(), getIP(httpRequest), requestJson.getRepeatPassword(), passwordEncoder);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
    }
}
