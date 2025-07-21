package com.melli.wallet.domain.response.limitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Name: GeneralCustomLimitationObject
 * Author: Mahdi Shirinabadi
 * Date: 1/20/2025
 */
@Setter
@Getter
@ToString
public class GeneralCustomLimitationObject {

    @Schema(name = NamingProperty.ID)
    @JsonProperty(NamingProperty.ID)
    private String id;

    @Schema(name = NamingProperty.LIMITATION_GENERAL_ID)
    @JsonProperty(NamingProperty.LIMITATION_GENERAL_ID)
    private String limitationGeneralId;

    @Schema(name = NamingProperty.LIMITATION_GENERAL_NAME)
    @JsonProperty(NamingProperty.LIMITATION_GENERAL_NAME)
    private String limitationGeneralName;

    private GeneralLimitationObject generalLimitationObject;

    @Schema(name = NamingProperty.VALUE)
    @JsonProperty(NamingProperty.VALUE)
    private String value;

    @Schema(name = NamingProperty.ADDITIONAL_DATA)
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    private String additionalData;

    @Schema(name = NamingProperty.WALLET_LEVEL_ID)
    @JsonProperty(NamingProperty.WALLET_LEVEL_ID)
    private String walletLevelId;

    @Schema(name = NamingProperty.WALLET_LEVEL_NAME)
    @JsonProperty(NamingProperty.WALLET_LEVEL_NAME)
    private String walletLevelName;

    @Schema(name = NamingProperty.WALLET_ACCOUNT_TYPE_ID)
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_TYPE_ID)
    private String walletAccountTypeId;

    @Schema(name = NamingProperty.WALLET_ACCOUNT_TYPE_NAME)
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_TYPE_NAME)
    private String walletAccountTypeName;

    @Schema(name = NamingProperty.WALLET_ACCOUNT_CURRENCY_ID)
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_CURRENCY_ID)
    private String walletAccountCurrencyId;

    @Schema(name = NamingProperty.WALLET_ACCOUNT_CURRENCY_NAME)
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_CURRENCY_NAME)
    private String walletAccountCurrencyName;

    @Schema(name = NamingProperty.WALLET_TYPE_ID)
    @JsonProperty(NamingProperty.WALLET_TYPE_ID)
    private String walletTypeId;

    @Schema(name = NamingProperty.WALLET_TYPE_NAME)
    @JsonProperty(NamingProperty.WALLET_TYPE_NAME)
    private String walletTypeName;

    @Schema(name = NamingProperty.CHANNEL_ID)
    @JsonProperty(NamingProperty.CHANNEL_ID)
    private String channelId;

    @Schema(name = NamingProperty.CHANNEL_NAME)
    @JsonProperty(NamingProperty.CHANNEL_NAME)
    private String channelName;

    @Schema(name = NamingProperty.CREATE_TIME)
    @JsonProperty(NamingProperty.CREATE_TIME)
    private String createTime;

    @Schema(name = NamingProperty.CREATE_BY)
    @JsonProperty(NamingProperty.CREATE_BY)
    private String createBy;

    @Schema(name = NamingProperty.END_TIME)
    @JsonProperty(NamingProperty.END_TIME)
    private String endTime;
} 