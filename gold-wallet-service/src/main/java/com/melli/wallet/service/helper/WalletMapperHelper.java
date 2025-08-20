package com.melli.wallet.service.helper;

import com.melli.wallet.domain.master.entity.WalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.entity.WalletAccountTypeEntity;
import com.melli.wallet.domain.master.entity.WalletLevelEntity;
import com.melli.wallet.domain.master.entity.WalletTypeEntity;
import com.melli.wallet.domain.response.wallet.WalletAccountCurrencyObject;
import com.melli.wallet.domain.response.wallet.WalletAccountTypeObject;
import com.melli.wallet.domain.response.wallet.WalletLevelObject;
import com.melli.wallet.domain.response.wallet.WalletTypeObject;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class Name: WalletMapperHelper
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
@Component
public class WalletMapperHelper {

    public List<WalletAccountCurrencyObject> mapToWalletAccountCurrencyObjectList(List<WalletAccountCurrencyEntity> entities) {
        return entities.stream()
                .map(this::mapToWalletAccountCurrencyObject)
                .collect(Collectors.toList());
    }

    public List<WalletLevelObject> mapToWalletLevelObjectList(List<WalletLevelEntity> entities) {
        return entities.stream()
                .map(this::mapToWalletLevelObject)
                .collect(Collectors.toList());
    }

    public List<WalletTypeObject> mapToWalletTypeObjectList(List<WalletTypeEntity> entities) {
        return entities.stream()
                .map(this::mapToWalletTypeObject)
                .collect(Collectors.toList());
    }

    public List<WalletAccountTypeObject> mapToWalletAccountTypeObjectList(List<WalletAccountTypeEntity> entities) {
        return entities.stream()
                .map(this::mapToWalletAccountTypeObject)
                .collect(Collectors.toList());
    }

    private WalletAccountCurrencyObject mapToWalletAccountCurrencyObject(WalletAccountCurrencyEntity entity) {
        WalletAccountCurrencyObject object = new WalletAccountCurrencyObject();
        object.setId(String.valueOf(entity.getId()));
        object.setName(entity.getName());
        object.setSuffix(entity.getSuffix());
        object.setAdditionalData(entity.getAdditionalData());
        object.setDescription(entity.getDescription());
        return object;
    }

    private WalletLevelObject mapToWalletLevelObject(WalletLevelEntity entity) {
        WalletLevelObject object = new WalletLevelObject();
        object.setId(String.valueOf(entity.getId()));
        object.setName(entity.getName());
        // WalletLevelEntity doesn't have additionalData field
        return object;
    }

    private WalletTypeObject mapToWalletTypeObject(WalletTypeEntity entity) {
        WalletTypeObject object = new WalletTypeObject();
        object.setId(String.valueOf(entity.getId()));
        object.setName(entity.getName());
        // WalletTypeEntity doesn't have additionalData or description fields
        return object;
    }

    private WalletAccountTypeObject mapToWalletAccountTypeObject(WalletAccountTypeEntity entity) {
        WalletAccountTypeObject object = new WalletAccountTypeObject();
        object.setId(String.valueOf(entity.getId()));
        object.setName(entity.getName());
        object.setAdditionalData(entity.getAdditionalData());
        object.setDescription(entity.getDescription());
        return object;
    }
}
