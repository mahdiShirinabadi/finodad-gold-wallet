package com.melli.wallet.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class SellGenerateUuidRequestJson {
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "کدملی", example = "0063360993")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @NumberValidation(allowDecimal = true, minDecimalValue = "0.00001")
    @Schema(name = NamingProperty.QUANTITY
            , description = "تعداد", example = "1.1")
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @StringValidation(label = "شماره حساب")
    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = "شماره حساب")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String accountNumber;

    @StringValidation(label = "نوع واحد فروش")
    @Schema(name = NamingProperty.CURRENCY, description = "نوع واحد فروش", allowableValues = {"GOLD","SILVER"})
    @JsonProperty(NamingProperty.CURRENCY)
    private String currency;
}
