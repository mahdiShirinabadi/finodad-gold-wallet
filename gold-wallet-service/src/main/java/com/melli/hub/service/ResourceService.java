package com.melli.hub.service;

import com.melli.hub.domain.master.entity.ResourceEntity;

public interface ResourceService {
    String LOGOUT = "LOGOUT";
    String WALLET_CREATE = "WALLET_CREATE";
    String WALLET_DEACTIVATE = "WALLET_DEACTIVATE";
    String WALLET_DELETE = "WALLET_DELETE";
    String WALLET_ACTIVE = "WALLET_ACTIVE";
    String GENERATE_UNIQUE_IDENTIFIER = "GENERATE_UNIQUE_IDENTIFIER";
    String CASH_IN = "CASH_IN";
    String PURCHASE = "PURCHASE";

    ResourceEntity getRequestType(String name);

}
