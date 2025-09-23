package com.melli.wallet.domain.request.wallet.giftcard;

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
public class GiftCardGenerateUuidRequestJson {

    @NumberValidation(allowDecimal = true, label = NamingProperty.QUANTITY)
    @Schema(name = NamingProperty.QUANTITY, description = "مقدار", example = "0.001")
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @NationalCodeValidation(label = NamingProperty.NATIONAL_CODE)
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "کد ملی ")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @StringValidation(label = NamingProperty.WALLET_ACCOUNT_NUMBER)
    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = "شماره حساب کیف ")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String accountNumber;

    @StringValidation(label = NamingProperty.CURRENCY)
    @Schema(name = NamingProperty.CURRENCY, description = "ارز")
    @JsonProperty(NamingProperty.CURRENCY)
    private String currency;
}
