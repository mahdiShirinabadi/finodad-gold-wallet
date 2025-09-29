package com.melli.wallet.domain.response.collateral;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Name: CreateCollateralResponse
 * Author: Mahdi Shirinabadi
 * Date: 9/27/2025
 */
@Setter
@Getter
@ToString
public class CollateralCreateTrackObject {

    @Schema(name = NamingProperty.ID)
    @JsonProperty(NamingProperty.ID)
    private String id;

    @Schema(name = NamingProperty.UNIQUE_IDENTIFIER)
    @JsonProperty(NamingProperty.UNIQUE_IDENTIFIER)
    private String uniqueIdentifier;

    @Schema(name = NamingProperty.COLLATERAL_CODE)
    @JsonProperty(NamingProperty.COLLATERAL_CODE)
    private String collateralCode;

    @Schema(name = NamingProperty.QUANTITY)
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @Schema(name = NamingProperty.FINAL_QUANTITY_BLOCK)
    @JsonProperty(NamingProperty.FINAL_QUANTITY_BLOCK)
    private String finalQuantityBlock;

    @Schema(name = NamingProperty.COMMISSION)
    @JsonProperty(NamingProperty.COMMISSION)
    private String commission;

    @Schema(name = NamingProperty.NATIONAL_CODE)
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @Schema(name = NamingProperty.STATUS)
    @JsonProperty(NamingProperty.STATUS)
    private String status;

    @Schema(name = NamingProperty.STATUS_DESCRIPTION)
    @JsonProperty(NamingProperty.STATUS_DESCRIPTION)
    private String statusDescription;

    @Schema(name = NamingProperty.ADDITIONAL_DATA)
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    private String additionalData;

    @Schema(name = NamingProperty.CURRENCY)
    @JsonProperty(NamingProperty.CURRENCY)
    private String currency;

    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER)
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String walletAccountNumber;

    @Schema(name = NamingProperty.CHANNEL_NAME)
    @JsonProperty(NamingProperty.CHANNEL_NAME)
    private String channelName;

    @Schema(name = NamingProperty.CREATE_TIME)
    @JsonProperty(NamingProperty.CREATE_TIME)
    private String createTime;

    @Schema(name = NamingProperty.CREATE_TIMESTAMP)
    @JsonProperty(NamingProperty.CREATE_TIMESTAMP)
    private Long createTimeTimestamp;

    @Schema(name = NamingProperty.RESULT)
    @JsonProperty(NamingProperty.RESULT)
    private String result;


    @Schema(name = NamingProperty.DESCRIPTION)
    @JsonProperty(NamingProperty.DESCRIPTION)
    private String description;
}
