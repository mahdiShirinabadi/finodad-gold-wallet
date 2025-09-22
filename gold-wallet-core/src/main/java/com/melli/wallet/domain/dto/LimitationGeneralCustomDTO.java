package com.melli.wallet.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Name: SettingGeneralCustomDTOP
 * Author: Mahdi Shirinabadi
 * Date: 4/16/2025
 */
@Setter
@Getter
@Builder
@ToString
public class LimitationGeneralCustomDTO {
    private String id;
    private String settingGeneralEntityId;
    private String walletLevelEntityId;
    private String walletAccountTypeEntityId;
    private String walletAccountCurrencyEntityId;
    private String walletTypeEntityId;
    private String channelEntityId;
    private String additionalData;
}
