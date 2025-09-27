package com.melli.wallet.domain.request.wallet.collateral;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.national_code.NationalCodeValidation;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Class Name: UniqueIdentifierCollateralRequestJson
 * Author: Mahdi Shirinabadi
 * Date: 9/27/2025
 */
@Setter
@Getter
public class CreateUniqueIdentifierCollateralRequestJson {

    @NationalCodeValidation(label =  NamingProperty.NATIONAL_CODE)
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "کد ملی")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @NumberValidation(allowNegative = true, label =  NamingProperty.QUANTITY)
    @Schema(name = NamingProperty.QUANTITY, description = "تعداد", example = "1000")
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @StringValidation(label =  NamingProperty.CURRENCY)
    @Schema(name = NamingProperty.CURRENCY, description = "واحد", example = "GOLD")
    @JsonProperty(NamingProperty.CURRENCY)
    private String currency;

    @StringValidation(label =  NamingProperty.WALLET_ACCOUNT_NUMBER)
    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = "شماره حساب کیف پول", example = "GHT56")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String walletAccountNumber;

}
