package com.melli.wallet.service;


import com.melli.wallet.domain.master.entity.SettingGeneralEntity;

public interface SettingGeneralService {
    String MAX_WRONG_PASSWORD_FOR_PROFILE = "MAX_WRONG_PASSWORD_FOR_PROFILE";
    String DURATION_ACCESS_TOKEN_PROFILE = "DURATION_ACCESS_TOKEN_PROFILE";
    String DURATION_REFRESH_TOKEN_PROFILE = "DURATION_REFRESH_TOKEN_PROFILE";
    String MOBILE_FOR_GOT_ALERT = "MOBILE_FOR_GOT_ALERT";
    String SMS_SEND_ALERT = "SMS_SEND_ALERT";
    String SETTLE_DEFAULT_PAGE = "SETTLE_DEFAULT_PAGE";
    String SETTLE_DEFAULT_SIZE = "SETTLE_DEFAULT_SIZE";


    SettingGeneralEntity getSetting(String name);
    public void clearCache();
    void save(SettingGeneralEntity setting);
}
