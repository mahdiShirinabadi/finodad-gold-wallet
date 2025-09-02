package com.melli.wallet.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.national_code.NationalCodeValidation;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CashOutWalletRequestJson {

    @StringValidation
    @Schema(name = NamingProperty.UNIQUE_IDENTIFIER, description = "شناسه یکتا")
    @JsonProperty(NamingProperty.UNIQUE_IDENTIFIER)
    private String uniqueIdentifier;

    @NumberValidation
    @Schema(name = NamingProperty.AMOUNT, description = "مبلغ به ریال")
    @JsonProperty(NamingProperty.AMOUNT)
    private String amount;

    @StringValidation(minLength = "26", maxLength = "26")
    @Schema(name = NamingProperty.IBAN, description = "شماره شبا مقصد", example = "IR000000000000000000000000")
    @JsonProperty(NamingProperty.IBAN)
    private String iban;

    @NationalCodeValidation(label = "کد ملی")
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "کدملی")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @Schema(name = NamingProperty.ADDITIONAL_DATA, description = "اطلاعات تکمیلی")
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    private String additionalData;

    @StringValidation(label = "شماره حساب کیف پول")
    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = "شماره حساب کیف پول")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String accountNumber;


    @Schema(name = NamingProperty.SIGN, description = "امضا دیجیتال")
    @JsonProperty(NamingProperty.SIGN)
    private String sign;

    @Hidden
    public String getDataString() {
        return uniqueIdentifier + "|" + iban + "|" + amount + "|" + nationalCode;
    }
}
