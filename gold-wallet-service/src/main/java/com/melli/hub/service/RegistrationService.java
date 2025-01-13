package com.melli.hub.service;

import com.melli.hub.domain.response.login.ForgetPasswordCheckOtpResponse;
import com.melli.hub.domain.response.login.ForgetPasswordProfileResponse;
import com.melli.hub.domain.response.login.SendOtpRegisterResponse;
import com.melli.hub.domain.response.login.SendShahkarRegisterResponse;
import com.melli.hub.exception.InternalServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Class Name: AuthenticationService
 * Author: Mahdi Shirinabadi
 * Date: 1/4/2025
 */
public interface RegistrationService {

    ForgetPasswordProfileResponse forgetPassword(String nationalCode, String mobileNumber) throws InternalServiceException;
    ForgetPasswordCheckOtpResponse forgetPasswordCheckOtp(String otp, String username, String registerHash) throws InternalServiceException;
    void forgetPasswordSetPassword(String otp, String username, String password, String newPassword,String registerHash) throws InternalServiceException;


    SendOtpRegisterResponse sendOtp(String nationalCode, String mobileNumber, String ip) throws InternalServiceException;
    SendShahkarRegisterResponse checkShahkar(String nationalCode, String tempUuid, String otp, String ip) throws InternalServiceException;
    void register(String nationalCode, String tempUuid, String password, String ip, String repeatPassword, PasswordEncoder bcryptEncoder) throws InternalServiceException;
    SendOtpRegisterResponse resendOtp(String tempUuid, String nationalCode, String mobile) throws InternalServiceException;
}
