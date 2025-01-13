package com.melli.hub.service;


import com.melli.hub.domain.master.entity.StatusEntity;
import com.melli.hub.exception.InternalServiceException;

public interface StatusService {

    int CREATE = -1;
    int SUCCESSFUL = 0;
    int INPUT_PARAMETER_NOT_VALID = 1;
    int PASSWORD_NOT_MATCH = 2;
    int ROLE_NAME_NOT_FOUND = 3;
    int FUND_NOT_FOUND = 4;
    int NATIONAL_CODE_NOT_FOUND = 5;
    int NATIONAL_CODE_EXIT = 6;
    int INVALID_USERNAME_PASSWORD = 7;
    int USER_NOT_PERMISSION = 8;
    int TOKEN_NOT_VALID = 9;
    int PROFILE_NOT_FOUND = 10;
    int EXPIRE_TIME_FOR_UPDATE_PASSWORD = 11;
    int PROFILE_IS_BLOCKED = 12;
    int INVALID_IP_ADDRESS = 13;
    int TOO_MANY_REQUESTS = 14;
    int STATUS_NOT_FOUND = 15;
    int ERROR_IN_GET_SHAHKAR = 16;
    int MOBILE_AND_NATIONAL_CODE_NOT_MATCH = 17;

    int ERROR_IN_SEND_SMS = 18;
    int REGISTER_TIME_IS_EXPIRE = 19;
    int OTP_NOT_FOUND = 20;
    int OTP_EXPIRE = 21;
    int TOO_MANY_OTP = 22;

    int REFRESH_TOKEN_NOT_BELONG_TO_PROFILE = 898;
    int REFRESH_TOKEN_NOT_FOUND = 899;
    int REFRESH_TOKEN_IS_EXPIRE = 900;
    int ERROR_IN_LOCK = 988;
    int TIMEOUT = 998;
    int GENERAL_ERROR = 999;


    void init();

    StatusEntity findByCode(String code);

    void clearCache();

    StatusEntity findById(long id) throws InternalServiceException;

    StatusEntity findByPersianDescription(String persianDescription);
}
