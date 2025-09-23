package com.melli.wallet.domain.response.giftcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Name: GiftCardUuidResponse
 * Author: Mahdi Shirinabadi
 * Date: 9/3/2025
 */
@Setter
@Getter
@ToString
public class GiftCardResponse {

    @Schema(name = NamingProperty.ACTIVE_CODE)
    @JsonProperty(NamingProperty.ACTIVE_CODE)
    private String activeCode;

    @Schema(name = NamingProperty.QUANTITY)
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @Schema(name = NamingProperty.CURRENCY)
    @JsonProperty(NamingProperty.CURRENCY)
    private String currency;

    @Schema(name = NamingProperty.EXPIRE_TIME)
    @JsonProperty(NamingProperty.EXPIRE_TIME)
    private String expireTime;

    @Schema(name = NamingProperty.EXPIRE_TIME_TIME_STAMP)
    @JsonProperty(NamingProperty.EXPIRE_TIME_TIME_STAMP)
    private Long expireTimeTimeStamp;
}
