package com.melli.hub.service.impl;

import com.melli.hub.domain.enumaration.VerificationCodeEnum;
import com.melli.hub.domain.master.persistence.VerificationCodeRepository;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.SettingService;
import com.melli.hub.service.StatusService;
import com.melli.hub.service.VerificationCodeService;
import com.melli.hub.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Log4j2
@RequiredArgsConstructor
public class VerificationCodeServiceImplementation implements VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final SettingService settingService;

    @Override
    public VerificationCodeEntity findFirstByProfileAndCodeAndStatusAndType(String nationalCode, String code, VerificationCodeEnum verificationCodeEnum) throws InternalServiceException {
        log.info("start find code ({}) for nationalCode ({})", code, nationalCode);
        VerificationCodeEntity verificationCode = verificationCodeRepository.findByNationalCodeAndCodeAndStatusAndVerificationCodeEnumOrderByIdDesc(nationalCode, code, VerificationCodeService.STATUS_CREATE, verificationCodeEnum);

        if(verificationCode == null){
            log.error("code ({}) for nationalCode ({}) is not found", code, nationalCode);
            throw new InternalServiceException("otp code (" + code + ") for nationalCode (" + nationalCode + ") is not found", StatusService.OTP_NOT_FOUND, HttpStatus.OK, null);
        }

        if(verificationCode.getExpireTime().getTime() < new Date().getTime()){
            log.error("code ({}) for nationalCode ({}) is expire at time ({})", code, nationalCode, verificationCode.getExpireTime());
            throw new InternalServiceException("code (" + code + ") for nationalCode (" + nationalCode + ") is not found", StatusService.OTP_EXPIRE,HttpStatus.OK, null);
        }
        log.info("success find code ({}) for mobile ({})", code, nationalCode);
        verificationCode.setStatus(VerificationCodeService.STATUS_USED);
        verificationCodeRepository.save(verificationCode);
        log.info("update find code ({}) for mobile ({}) to status used", code, nationalCode);
        return verificationCode;
    }

    @Override
    public VerificationCodeEntity findLastByProfileAndStatusAndType(String nationalCode, String code, VerificationCodeEnum verificationCodeEnum) throws InternalServiceException {
        log.info("start find otp code ({}) for nationalCode ({})", code, nationalCode);
        return verificationCodeRepository.findByNationalCodeAndStatusAndVerificationCodeEnumOrderByIdDesc(nationalCode, VerificationCodeService.STATUS_CREATE, verificationCodeEnum);
    }

    @Override
    public void checkTooManySendOtp(String nationalCode, int status, VerificationCodeEnum verificationCodeEnum) throws InternalServiceException {
        log.info("start to find last time for send Oto for nationalCode ({})", nationalCode);
        VerificationCodeEntity verificationCode = verificationCodeRepository.findByNationalCodeAndStatusAndVerificationCodeEnumOrderByIdDesc(nationalCode, status, verificationCodeEnum);
        if(verificationCode != null && new Date().before(verificationCode.getExpireTime())){
            log.error("too many request for generate otp for nationalCode ({})", nationalCode);
            throw new InternalServiceException("too many request for send otp", StatusService.TOO_MANY_OTP, HttpStatus.OK, null);
        }
    }

    @Override
    public VerificationCodeEntity save(String nationalCode, String code, VerificationCodeEnum verificationCodeEnum) {
        log.info("start save verification code ({}) for nationalCode ({})", code, nationalCode);
        VerificationCodeEntity verificationCode = new VerificationCodeEntity();
        verificationCode.setVerificationCodeEnum(verificationCodeEnum);
        verificationCode.setCode(code);
        verificationCode.setCreatedAt(new Date());
        verificationCode.setCreatedBy(nationalCode);
        verificationCode.setNationalCode(nationalCode);
        verificationCode.setStatus(VerificationCodeService.STATUS_CREATE);
        verificationCode.setExpireTime(DateUtils.getNNextMinutes(new Date(),Integer.parseInt(settingService.getSetting(SettingService.MAX_OTP_EXPIRE_TIME_MINUTES).getValue())));
        verificationCodeRepository.save(verificationCode);
        log.info("finish save verification code ({}) for nationalCode ({})", code, nationalCode);
        return verificationCode;
    }

    @Override
    public VerificationCodeEntity getByOtpCodeAndStatus(String nationalCode, String code, int status, VerificationCodeEnum verificationCodeEnum) throws InternalServiceException {
        VerificationCodeEntity verificationCode = verificationCodeRepository.findByNationalCodeAndCodeAndStatusAndVerificationCodeEnumOrderByIdDesc(nationalCode, code, status, verificationCodeEnum);
        if(verificationCode == null){
            log.error("code ({}) for nationalCode ({}) is not found", code, nationalCode);
            throw new InternalServiceException("code (" + code + ") for nationalCode (" + nationalCode + ") is not found", StatusService.OTP_NOT_FOUND, HttpStatus.OK, null);
        }
        return verificationCode;
    }

    @Override
    @Transactional
    @Async
    public void deleteAll(String nationalCode, VerificationCodeEnum verificationCodeEnum) throws InternalServiceException {
        log.info("start delete all verification code for nationalCode ({})", nationalCode);
        verificationCodeRepository.deleteAllByNationalCodeAndVerificationCodeEnum(nationalCode, verificationCodeEnum);
    }
}

