package com.melli.wallet.domain.request.wallet.physical;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.national_code.NationalCodeValidation;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class PhysicalCashGenerateUuidRequestJson {
    @NationalCodeValidation(label = NamingProperty.NATIONAL_CODE)
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "کدملی", example = "0063360993")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @NumberValidation(label = NamingProperty.QUANTITY ,allowDecimal = true)
    @Schema(name = NamingProperty.QUANTITY, description = "مقدار طلا", example = "1")
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @StringValidation(label = NamingProperty.WALLET_ACCOUNT_NUMBER)
    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = "شماره حساب")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String accountNumber;

    @StringValidation(label = NamingProperty.CURRENCY)
    @Schema(name = NamingProperty.CURRENCY, description = "واحد")
    @JsonProperty(NamingProperty.CURRENCY)
    private String currency;


}
