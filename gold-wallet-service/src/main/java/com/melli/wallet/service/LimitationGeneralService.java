package com.melli.wallet.service;


import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.LimitationGeneralEntity;
import com.melli.wallet.domain.master.entity.SettingGeneralEntity;
import com.melli.wallet.domain.response.limitation.GeneralLimitationListResponse;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface LimitationGeneralService {

    default Pageable getPageableConfig(SettingGeneralService settingService, Integer page, Integer size) {
        SettingGeneralEntity settingPage = settingService.getSetting(SettingGeneralService.SETTLE_DEFAULT_PAGE);
        SettingGeneralEntity settingSize = settingService.getSetting(SettingGeneralService.SETTLE_DEFAULT_SIZE);
        return PageRequest.of(page == null ? Integer.parseInt(settingPage.getValue()) : page, size == null ? Integer.parseInt(settingSize.getValue()) : size);
    }
    //cashIn
    String ENABLE_CASH_IN = "ENABLE_CASH_IN";
    String MIN_AMOUNT_CASH_IN = "MIN_AMOUNT_CASH_IN";
    String MAX_AMOUNT_CASH_IN = "MAX_AMOUNT_CASH_IN";
    String MAX_WALLET_BALANCE = "MAX_WALLET_BALANCE";
    String MAX_WALLET_AMOUNT_DAILY_CASH_IN = "MAX_WALLET_AMOUNT_DAILY_CASH_IN";

    //cashout
    String ENABLE_CASH_OUT = "ENABLE_CASH_OUT";
    String MIN_AMOUNT_CASH_OUT = "MIN_AMOUNT_CASH_OUT";
    String MAX_AMOUNT_CASH_OUT = "MAX_AMOUNT_CASH_OUT";
    String MAX_WALLET_AMOUNT_DAILY_CASH_OUT = "MAX_WALLET_AMOUNT_DAILY_CASH_OUT";
    //buy
    String MAX_DAILY_PRICE_BUY = "MAX_DAILY_PRICE_BUY";
    String MAX_DAILY_COUNT_BUY = "MAX_DAILY_COUNT_BUY";
    String MAX_MONTHLY_PRICE_BUY = "MAX_MONTHLY_PRICE_BUY";
    String MAX_MONTHLY_COUNT_BUY = "MAX_MONTHLY_COUNT_BUY";
    String MIN_PRICE_BUY = "MIN_PRICE_BUY";
    String MAX_PRICE_BUY = "MAX_PRICE_BUY";

    //sell
    String MAX_DAILY_QUANTITY_SELL = "MAX_DAILY_QUANTITY_SELL";
    String MAX_DAILY_COUNT_SELL = "MAX_DAILY_COUNT_SELL";
    String MAX_MONTHLY_QUANTITY_SELL = "MAX_MONTHLY_QUANTITY_SELL";
    String MAX_MONTHLY_COUNT_SELL = "MAX_MONTHLY_COUNT_SELL";
    String MIN_QUANTITY_SELL = "MIN_QUANTITY_SELL";
    String MAX_QUANTITY_SELL = "MAX_QUANTITY_SELL";

    List<LimitationGeneralEntity> getLimitationGeneralEntities() throws InternalServiceException;
    LimitationGeneralEntity getSetting(String name);
    LimitationGeneralEntity getById(Long id) throws InternalServiceException;
    public void clearCache();
    void save(LimitationGeneralEntity setting) throws InternalServiceException;
    GeneralLimitationListResponse getGeneralLimitationList(ChannelEntity channelEntity, Map<String, String> mapParameter) throws InternalServiceException;

}
