package com.melli.wallet.service;


import com.melli.wallet.domain.master.entity.LimitationGeneralEntity;
import com.melli.wallet.domain.master.entity.SettingGeneralEntity;
import com.melli.wallet.exception.InternalServiceException;

import java.util.List;

public interface LimitationGeneralService {
    String ENABLE_CASH_IN = "ENABLE_CASH_IN";
    String MIN_AMOUNT_CASH_IN = "MIN_AMOUNT_CASH_IN";
    String MAX_AMOUNT_CASH_IN = "MAX_AMOUNT_CASH_IN";
    String MAX_WALLET_BALANCE = "MAX_WALLET_BALANCE";
    String MAX_WALLET_AMOUNT_DAILY_CASH_IN = "MAX_WALLET_AMOUNT_DAILY_CASH_IN";

    List<LimitationGeneralEntity> getLimitationGeneralEntities() throws InternalServiceException;
    LimitationGeneralEntity getSetting(String name);
    public void clearCache();
    void save(LimitationGeneralEntity setting) throws InternalServiceException;
}
