package com.melli.wallet.service;

import com.melli.wallet.domain.master.entity.MerchantEntity;


public interface MerchantService {

    int ACTIVE = 1;
    int DISABLED = 2;
    MerchantEntity findById(int id);
    void save(MerchantEntity merchant);
    void clearAllCache();
}
