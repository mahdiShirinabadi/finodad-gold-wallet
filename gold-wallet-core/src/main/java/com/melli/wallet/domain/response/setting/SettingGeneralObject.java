package com.melli.wallet.domain.response.setting;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Name: SettingObject
 * Author: Mahdi Shirinabadi
 * Date: 4/19/2025
 */
@Setter
@Getter
@ToString
public class SettingGeneralObject {
    private String id;
    private String name;
    private String value;
    private String pattern;
    private String additionalData;
    private String createTime;
    private String createBy;
    private String updateTime;
    private String updateBy;
}
