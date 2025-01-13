package com.melli.hub.service;


import com.melli.hub.domain.enumaration.VerificationCodeEnum;
import com.melli.hub.domain.master.entity.VerificationCodeEntity;
import com.melli.hub.exception.InternalServiceException;

public interface VerificationCodeService {
    int STATUS_CREATE = 0;
    int STATUS_USED = 1;

    VerificationCodeEntity findFirstByProfileAndCodeAndStatusAndType(String nationalCode, String code, VerificationCodeEnum verificationCodeEnum) throws InternalServiceException;
    VerificationCodeEntity findLastByProfileAndStatusAndType(String nationalCode, String status, VerificationCodeEnum verificationCodeEnum) throws InternalServiceException;
    void checkTooManySendOtp(String nationalCode, int status, VerificationCodeEnum verificationCodeEnum) throws InternalServiceException;
    VerificationCodeEntity save(String nationalCode, String code, VerificationCodeEnum verificationCodeEnum);
    VerificationCodeEntity getByOtpCodeAndStatus(String nationalCode, String code, int status, VerificationCodeEnum verificationCodeEnum) throws InternalServiceException;
    void deleteAll(String nationalCode, VerificationCodeEnum verificationCodeEnum) throws InternalServiceException;
}
