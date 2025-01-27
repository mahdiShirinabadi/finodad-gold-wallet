package com.melli.hub.service;


import com.melli.hub.domain.master.entity.SettingGeneralEntity;
import com.melli.hub.exception.InternalServiceException;

public interface SettingGeneralService {
    String MAX_WRONG_PASSWORD_FOR_PROFILE = "MAX_WRONG_PASSWORD_FOR_PROFILE";
    String DURATION_ACCESS_TOKEN_PROFILE = "DURATION_ACCESS_TOKEN_PROFILE";
    String DURATION_REFRESH_TOKEN_PROFILE = "DURATION_REFRESH_TOKEN_PROFILE";
    String MAX_OTP_EXPIRE_TIME_MINUTES = "MAX_OTP_EXPIRE_TIME_MINUTES";
    String MAX_REGISTER_EXPIRE_TIME_MINUTES = "MAX_REGISTER_EXPIRE_TIME_MINUTES";
    String LENGTH_OTP = "LENGTH_OTP";
    String SMS_OTP_TEMPLATE = "SMS_OTP_TEMPLATE";
    String NEW_PASSWORD_TEMPLATE = "NEW_PASSWORD_TEMPLATE";
    String MOBILE_FOR_GOT_ALERT = "MOBILE_FOR_GOT_ALERT";
    String SMS_SEND_ALERT = "SMS_SEND_ALERT";
    String ENABLE_CASH_IN = "ENABLE_CASH_IN";
    String MIN_AMOUNT_CASH_IN = "MIN_AMOUNT_CASH_IN";
    String MAX_AMOUNT_CASH_IN = "MAX_AMOUNT_CASH_IN";
    String MAX_WALLET_BALANCE = "MAX_WALLET_BALANCE";


    SettingGeneralEntity getSetting(String name);
    public void clearCache();
    void save(SettingGeneralEntity setting);
}
