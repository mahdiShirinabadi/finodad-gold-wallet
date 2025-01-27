package com.melli.hub.service;

import com.melli.hub.domain.master.entity.MerchantEntity;


public interface MerchantService {

    int ACTIVE = 1;
    int DISABLED = 2;
    MerchantEntity findById(int id);
    void save(MerchantEntity merchant);
    void clearAllCache();
}
