package com.melli.wallet.domain.dto;

import com.melli.wallet.domain.master.entity.*;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Class Name: SettingGeneralCustomDTOP
 * Author: Mahdi Shirinabadi
 * Date: 4/16/2025
 */
@Setter
@Getter
@Builder
@ToString
public class SettingGeneralCustomDTO {
    private String id;
    private String settingGeneralEntityId;
    private String walletLevelEntityId;
    private String walletAccountTypeEntityId;
    private String walletAccountCurrencyEntityId;
    private String walletTypeEntityId;
    private String channelEntityId;
}
