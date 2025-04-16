package com.melli.wallet.utils;

import com.melli.wallet.domain.master.entity.WalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.entity.WalletAccountTypeEntity;
import com.melli.wallet.domain.response.wallet.WalletAccountCurrencyObject;
import com.melli.wallet.domain.response.wallet.WalletAccountTypeObject;

public class SubHelper {



    public static WalletAccountTypeObject convertWalletAccountEntityToObject(WalletAccountTypeEntity entity){
        WalletAccountTypeObject walletAccountTypeObject = new WalletAccountTypeObject();
        walletAccountTypeObject.setId(String.valueOf(entity.getId()));
        walletAccountTypeObject.setName(entity.getName());
        walletAccountTypeObject.setAdditionalData(entity.getAdditionalData());
        walletAccountTypeObject.setDescription(entity.getDescription());
        return walletAccountTypeObject;
    }

    public static WalletAccountCurrencyObject convertWalletAccountCurrencyEntityToObject(WalletAccountCurrencyEntity entity){
        WalletAccountCurrencyObject walletAccountCurrencyObject = new WalletAccountCurrencyObject();
        walletAccountCurrencyObject.setId(String.valueOf(entity.getId()));
        walletAccountCurrencyObject.setName(entity.getName());
        walletAccountCurrencyObject.setSuffix(entity.getSuffix());
        walletAccountCurrencyObject.setAdditionalData(entity.getAdditionalData());
        walletAccountCurrencyObject.setDescription(entity.getDescription());
        return walletAccountCurrencyObject;
    }

}
