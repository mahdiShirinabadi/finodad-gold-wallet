package com.melli.wallet.service.repository;


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

    default Pageable getPageableConfig(SettingGeneralRepositoryService settingService, Integer page, Integer size) {
        SettingGeneralEntity settingPage = settingService.getSetting(SettingGeneralRepositoryService.SETTLE_DEFAULT_PAGE);
        SettingGeneralEntity settingSize = settingService.getSetting(SettingGeneralRepositoryService.SETTLE_DEFAULT_SIZE);
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
    String ENABLE_PHYSICAL_CASH_OUT = "ENABLE_PHYSICAL_CASH_OUT";
    String MIN_AMOUNT_CASH_OUT = "MIN_AMOUNT_CASH_OUT";
    String MAX_AMOUNT_CASH_OUT = "MAX_AMOUNT_CASH_OUT";
    String MAX_WALLET_AMOUNT_DAILY_CASH_OUT = "MAX_WALLET_AMOUNT_DAILY_CASH_OUT";

    String MIN_QUANTITY_PHYSICAL_CASH_OUT = "MIN_QUANTITY_PHYSICAL_CASH_OUT";
    String MAX_QUANTITY_PHYSICAL_CASH_OUT = "MAX_QUANTITY_PHYSICAL_CASH_OUT";
    String MAX_WALLET_QUANTITY_DAILY_PHYSICAL_CASH_OUT = "MAX_WALLET_QUANTITY_DAILY_PHYSICAL_CASH_OUT";

    //buy
    String MAX_DAILY_QUANTITY_BUY = "MAX_DAILY_QUANTITY_BUY";
    String MAX_DAILY_COUNT_BUY = "MAX_DAILY_COUNT_BUY";
    String MONTHLY_VALIDATION_CHECK_BUY = "MONTHLY_VALIDATION_CHECK_BUY";
    String MAX_MONTHLY_QUANTITY_BUY = "MAX_MONTHLY_QUANTITY_BUY";
    String MAX_MONTHLY_COUNT_BUY = "MAX_MONTHLY_COUNT_BUY";
    String MIN_QUANTITY_BUY = "MIN_QUANTITY_BUY";
    String MAX_QUANTITY_BUY = "MAX_QUANTITY_BUY";

    //sell
    String MAX_DAILY_QUANTITY_SELL = "MAX_DAILY_QUANTITY_SELL";
    String MAX_DAILY_COUNT_SELL = "MAX_DAILY_COUNT_SELL";
    String MONTHLY_VALIDATION_CHECK_SELL = "MONTHLY_VALIDATION_CHECK_SELL";
    String MAX_MONTHLY_QUANTITY_SELL = "MAX_MONTHLY_QUANTITY_SELL";
    String MAX_MONTHLY_COUNT_SELL = "MAX_MONTHLY_COUNT_SELL";
    String MIN_QUANTITY_SELL = "MIN_QUANTITY_SELL";
    String MAX_QUANTITY_SELL = "MAX_QUANTITY_SELL";

    //p2p
    String MAX_DAILY_QUANTITY_P2P = "MAX_DAILY_QUANTITY_P2P";
    String MAX_DAILY_COUNT_P2P = "MAX_DAILY_COUNT_P2P";
    String MIN_QUANTITY_P2P = "MIN_QUANTITY_P2P";
    String MAX_QUANTITY_P2P = "MAX_QUANTITY_P2P";

    //giftCard
    String MAX_DAILY_QUANTITY_GIFT_CARD = "MAX_DAILY_QUANTITY_GIFT_CARD";
    String MAX_DAILY_COUNT_GIFT_CARD = "MAX_DAILY_COUNT_GIFT_CARD";
    String MAX_DAILY_QUANTITY_PAYMENT_GIFT_CARD = "MAX_DAILY_QUANTITY_PAYMENT_GIFT_CARD";
    String MAX_DAILY_COUNT_PAYMENT_GIFT_CARD = "MAX_DAILY_COUNT_PAYMENT_GIFT_CARD";
    String MIN_QUANTITY_GIFT_CARD = "MIN_QUANTITY_GIFT_CARD";
    String MAX_QUANTITY_GIFT_CARD = "MAX_QUANTITY_GIFT_CARD";
    String ENABLE_GIFT_CARD = "ENABLE_GIFT_CARD";

    List<LimitationGeneralEntity> getLimitationGeneralEntities() throws InternalServiceException;
    LimitationGeneralEntity getSetting(String name);
    LimitationGeneralEntity getById(Long id) throws InternalServiceException;
    public void clearCache();
    void save(LimitationGeneralEntity setting) throws InternalServiceException;
    GeneralLimitationListResponse getGeneralLimitationList(ChannelEntity channelEntity, Map<String, String> mapParameter) throws InternalServiceException;

}
