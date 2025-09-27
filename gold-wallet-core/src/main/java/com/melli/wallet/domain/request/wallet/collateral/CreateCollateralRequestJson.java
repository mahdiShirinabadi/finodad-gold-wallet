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
public class CreateCollateralRequestJson {

    @StringValidation
    @Schema(name = NamingProperty.UNIQUE_IDENTIFIER, description = "شناسه یکتا")
    @JsonProperty(NamingProperty.UNIQUE_IDENTIFIER)
    private String uniqueIdentifier;

    @NumberValidation(allowDecimal = true, label = NamingProperty.QUANTITY)
    @Schema(name = NamingProperty.QUANTITY, description = "تعداد", example = "1000")
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @NumberValidation
    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = "شماره حساب کیف پول", example = "")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String accountNumber;

    @NumberValidation
    @Schema(name = NamingProperty.DESCRIPTION, description = "تعداد", example = "1000")
    @JsonProperty(NamingProperty.DESCRIPTION)
    private String description;


    @Schema(name = NamingProperty.SIGN, description = "uniqueIdentifier|quantity|accountNumber")
    @JsonProperty(NamingProperty.SIGN)
    private String sign;

    @Schema(name = NamingProperty.COMMISSION_OBJECT, description = "کارمزد")
    @JsonProperty(NamingProperty.COMMISSION_OBJECT)
    private CommissionObject commissionObject;

    @Hidden
    public String getDataString() {
        return uniqueIdentifier + "|" + quantity + "|" + accountNumber;
    }

}
