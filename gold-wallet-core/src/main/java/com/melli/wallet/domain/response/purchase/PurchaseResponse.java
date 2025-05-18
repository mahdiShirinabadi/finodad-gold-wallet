package com.melli.wallet.domain.response.purchase;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Class Name: PurchaseResponse
 * Author: Mahdi Shirinabadi
 * Date: 1/27/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseResponse {

    @Schema(name = NamingProperty.NATIONAL_CODE)
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

/*    @Schema(name = NamingProperty.BALANCE)
    @JsonProperty(NamingProperty.BALANCE)
    private String balance;*/

    @Schema(name = NamingProperty.QUANTITY)
    @JsonProperty(NamingProperty.QUANTITY)
    private String amount;

    @Schema(name = NamingProperty.PRICE)
    @JsonProperty(NamingProperty.PRICE)
    private String price;

    @Schema(name = NamingProperty.UNIQUE_IDENTIFIER)
    @JsonProperty(NamingProperty.UNIQUE_IDENTIFIER)
    private String uniqueIdentifier;

    @Schema(name = NamingProperty.PURCHASE_TYPE)
    @JsonProperty(NamingProperty.PURCHASE_TYPE)
    private String type;

    @Schema(name = NamingProperty.CHANNEL_NAME)
    @JsonProperty(NamingProperty.CHANNEL_NAME)
    private String channelName;

    @Schema(name = NamingProperty.CREATE_TIME)
    @JsonProperty(NamingProperty.CREATE_TIME)
    private String createTime;

    @Schema(name = NamingProperty.CREATE_TIMESTAMP)
    @JsonProperty(NamingProperty.CREATE_TIMESTAMP)
    private Long createTimeTimestamp;
}
