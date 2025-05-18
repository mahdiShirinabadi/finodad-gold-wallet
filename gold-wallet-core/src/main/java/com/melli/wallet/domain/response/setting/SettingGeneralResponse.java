package com.melli.wallet.domain.response.setting;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.domain.response.base.PageResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountCurrencyObject;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.domain.response.wallet.WalletAccountTypeObject;
import com.melli.wallet.domain.response.wallet.WalletLevelObject;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(name = NamingProperty.SETTING_GENERAL_OBJECT_LIST)
    @JsonProperty(NamingProperty.SETTING_GENERAL_OBJECT_LIST)
    private List<SettingGeneralObject> settingGeneralObjectList;
}
