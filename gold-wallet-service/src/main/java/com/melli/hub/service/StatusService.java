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
    int CHANNEL_IS_BLOCKED = 12;
    int INVALID_IP_ADDRESS = 13;
    int TOO_MANY_REQUESTS = 14;
    int STATUS_NOT_FOUND = 15;
    int ERROR_IN_GET_SHAHKAR = 16;
    int MOBILE_AND_NATIONAL_CODE_NOT_MATCH = 17;

    int ERROR_IN_SEND_SMS = 18;
    int TOO_MANY_OTP = 22;
    int DUPLICATE_CHANNEL_NAME = 23;
    int CHANNEL_NOT_FOUND = 24;
    int WALLET_NOT_FOUND = 25;
    int WALLET_ACCOUNT_CURRENCY_NOT_FOUND = 26;
    int WALLET_ACCOUNT_TYPE_NOT_FOUND = 27;
    int WALLET_TYPE_NOT_FOUND = 28;
    int INVALID_SIGN = 29;
    int WALLET_NOT_CREATE_SUCCESS = 30;
    int UUID_NOT_FOUND = 31;
    int DUPLICATE_UUID = 32;
    int REF_NUMBER_USED_BEFORE = 33;
    int WALLET_IS_NOT_ACTIVE = 34;
    int WALLET_ACCOUNT_NOT_FOUND = 35;
    int WALLET_ACCOUNT_IS_NOT_ACTIVE = 36;
    int AMOUNT_LESS_THAN_MIN = 37;
    int SETTING_NOT_FOUND = 38;
    int ACCOUNT_DONT_PERMISSION_FOR_CASH_IN = 39;
    int AMOUNT_BIGGER_THAN_MAX = 40;
    int BALANCE_MORE_THAN_STANDARD = 41;
    int WALLET_EXCEEDED_AMOUNT_LIMITATION = 42;
    int BALANCE_IS_NOT_ENOUGH = 43;
    int RECORD_NOT_FOUND = 44;

    int REFRESH_TOKEN_NOT_BELONG_TO_PROFILE = 898;
    int REFRESH_TOKEN_NOT_FOUND = 899;
    int REFRESH_TOKEN_IS_EXPIRE = 900;
    int ERROR_IN_LOCK = 988;
    int TIMEOUT = 998;
    int GENERAL_ERROR = 999;
    int MERCHANT_IS_NOT_EXIST = 45;


    void init();

    StatusEntity findByCode(String code);

    void clearCache();

    StatusEntity findById(long id) throws InternalServiceException;

    StatusEntity findByPersianDescription(String persianDescription);
}
