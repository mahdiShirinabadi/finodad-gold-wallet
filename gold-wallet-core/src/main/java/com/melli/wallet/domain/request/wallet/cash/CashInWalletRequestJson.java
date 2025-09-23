package com.melli.wallet.domain.request.wallet.cash;

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
public class CashInWalletRequestJson {

    @StringValidation
    @Schema(name = NamingProperty.UNIQUE_IDENTIFIER, description = "شناسه یکتا")
    @JsonProperty(NamingProperty.UNIQUE_IDENTIFIER)
    private String uniqueIdentifier;

    @StringValidation
    @Schema(name = NamingProperty.REFERENCE_NUMBER, description = "شماره مرجع")
    @JsonProperty(NamingProperty.REFERENCE_NUMBER)
    private String referenceNumber;

    @NumberValidation
    @Schema(name = NamingProperty.AMOUNT, description = "مبلغ به ریال", example = "1000")
    @JsonProperty(NamingProperty.AMOUNT)
    private String amount;

    @NationalCodeValidation(label = NamingProperty.NATIONAL_CODE)
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "کدملی")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @Schema(name = NamingProperty.ADDITIONAL_DATA, description = "اطلاعات تکمیلی")
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    private String additionalData;

    @Schema(name = NamingProperty.CASH_IN_TYPE, description = "نوع شارژ (IPG,ACCOUNT_TO_ACCOUNT)")
    @JsonProperty(NamingProperty.CASH_IN_TYPE)
    private String cashInType;

    @StringValidation(label = NamingProperty.WALLET_ACCOUNT_NUMBER)
    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = "شماره حساب کیف پول")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String accountNumber;


    @Schema(name = NamingProperty.SIGN, description = "امضا دیجیتال")
    @JsonProperty(NamingProperty.SIGN)
    private String sign;

    @Hidden
    public String getDataString() {
        return uniqueIdentifier + "|" + referenceNumber + "|" + amount + "|" + nationalCode;
    }
}
