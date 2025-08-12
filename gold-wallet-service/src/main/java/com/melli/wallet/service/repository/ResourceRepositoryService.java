package com.melli.wallet.service.repository;

import com.melli.wallet.domain.master.entity.ResourceEntity;

public interface ResourceRepositoryService {
    String LOGOUT = "LOGOUT";
    String WALLET_CREATE = "WALLET_CREATE";
    String WALLET_DEACTIVATE = "WALLET_DEACTIVATE";
    String WALLET_DELETE = "WALLET_DELETE";
    String WALLET_ACTIVE = "WALLET_ACTIVE";
    String WALLET_INFO = "WALLET_INFO";
    String GENERATE_CASH_IN_UNIQUE_IDENTIFIER = "GENERATE_CASH_IN_UNIQUE_IDENTIFIER";
    String GENERATE_PURCHASE_UNIQUE_IDENTIFIER = "GENERATE_PURCHASE_UNIQUE_IDENTIFIER";
    String MERCHANT_LIST = "MERCHANT_LIST";
    String CASH_IN = "CASH_IN";
    String CASH_OUT = "CASH_OUT";
    String PHYSICAL_CASH_OUT = "PHYSICAL_CASH_OUT";
    String P2P = "P2P";
    String BUY = "BUY";
    String BUY_DIRECT = "BUY_DIRECT";
    String SELL = "SELL";
    String SETTING_LIST = "SETTING_LIST";
    String LIMITATION_LIST = "LIMITATION_LIST";
    String LIMITATION_MANAGE = "LIMITATION_MANAGE";
    String MERCHANT_BALANCE = "MERCHANT_BALANCE";
    String MERCHANT_INCREASE_BALANCE = "MERCHANT_INCREASE_BALANCE";
    String MERCHANT_DECREASE_BALANCE = "MERCHANT_DECREASE_BALANCE";

    ResourceEntity getRequestType(String name);

}
