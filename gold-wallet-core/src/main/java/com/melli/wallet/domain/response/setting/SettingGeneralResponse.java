package com.melli.wallet.domain.response.setting;

import com.melli.wallet.domain.response.base.PageResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountCurrencyObject;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.domain.response.wallet.WalletAccountTypeObject;
import com.melli.wallet.domain.response.wallet.WalletLevelObject;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Class Name: Setting
 * Author: Mahdi Shirinabadi
 * Date: 4/19/2025
 */
@Setter
@Getter
public class SettingGeneralResponse extends PageResponse {
    private List<SettingGeneralObject> settingGeneralObjectList;
}
