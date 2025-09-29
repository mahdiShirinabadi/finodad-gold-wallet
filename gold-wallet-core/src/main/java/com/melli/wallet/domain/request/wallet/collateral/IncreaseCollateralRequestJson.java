package com.melli.wallet.domain.request.wallet.collateral;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import com.melli.wallet.domain.request.wallet.CommissionObject;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Class Name: CreateCollateralRequestJson
 * Author: Mahdi Shirinabadi
 * Date: 9/27/2025
 */
@Setter
@Getter
public class IncreaseCollateralRequestJson {

    @StringValidation
    @Schema(name = NamingProperty.COLLATERAL_CODE, description = "کد مسدودی")
    @JsonProperty(NamingProperty.COLLATERAL_CODE)
    private String collateralCode;

    @NumberValidation(allowDecimal = true, label = NamingProperty.QUANTITY)
    @Schema(name = NamingProperty.QUANTITY, description = "تعداد", example = "1000")
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @NumberValidation(allowDecimal = true, label = NamingProperty.NATIONAL_CODE)
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "تعداد", example = "1000")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @Schema(name = NamingProperty.ADDITIONAL_DATA, description = "اطلاعات تکمیلی", example = "1000")
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    private String additionalData;

    @Schema(name = NamingProperty.SIGN, description = "quantity|collateralCode|nationalCode")
    @JsonProperty(NamingProperty.SIGN)
    private String sign;

    @Schema(name = NamingProperty.COMMISSION_OBJECT, description = "کارمزد")
    @JsonProperty(NamingProperty.COMMISSION_OBJECT)
    private CommissionObject commissionObject;

    @Hidden
    public String getDataString() {
        return quantity + "|" + collateralCode + "|" + nationalCode;
    }
}
