package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.ResourceEntity;

public interface ResourceService {
    String LOGOUT = "LOGOUT";
    String WALLET_CREATE = "WALLET_CREATE";
    String WALLET_DEACTIVATE = "WALLET_DEACTIVATE";
    String WALLET_DELETE = "WALLET_DELETE";
    String WALLET_ACTIVE = "WALLET_ACTIVE";
    String WALLET_INFO = "WALLET_INFO";
    String GENERATE_UNIQUE_IDENTIFIER = "GENERATE_UNIQUE_IDENTIFIER";
    String CASH_IN = "CASH_IN";
    String BUY = "BUY";
    String SELL = "SELL";
    String SETTING_LIST = "SETTING_LIST";
    String LIMITATION_LIST = "LIMITATION_LIST";

    ResourceEntity getRequestType(String name);

}
