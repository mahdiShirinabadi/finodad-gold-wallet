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
public class CreateCollateralResponse {

    @Schema(name = NamingProperty.COLLATERAL_CODE)
    @JsonProperty(NamingProperty.COLLATERAL_CODE)
    private String collateralCode;

    @Schema(name = NamingProperty.QUANTITY)
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @Schema(name = NamingProperty.NATIONAL_CODE)
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;
}
