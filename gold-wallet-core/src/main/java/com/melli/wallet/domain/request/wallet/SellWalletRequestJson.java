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
public class
SellWalletRequestJson {

    @StringValidation
    @Schema(name = NamingProperty.UNIQUE_IDENTIFIER, description = "شناسه یکتا")
    @JsonProperty(NamingProperty.UNIQUE_IDENTIFIER)
    private String uniqueIdentifier;

    @NumberValidation(allowDecimal = true, minDecimalValue = "0.00001")
    @Schema(name = NamingProperty.QUANTITY, description = "مقدار طلا", example = "1.1")
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @NumberValidation
    @Schema(name = NamingProperty.PRICE, description = "مبلغ با کارمزد به ریال", example = "1000")
    @JsonProperty(NamingProperty.PRICE)
    private String price;


    @Schema(name = NamingProperty.COMMISSION_OBJECT, description = "کارمزد")
    @JsonProperty(NamingProperty.COMMISSION_OBJECT)
    private CommissionObject commissionObject;

    @NationalCodeValidation(label = "کد ملی")
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "کدملی")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @StringValidation(label ="شماره شبا", minLength = "26", maxLength = "26")
    @Schema(name = NamingProperty.IBAN, description = "شماره شبا")
    @JsonProperty(NamingProperty.IBAN)
    private String iban;

    @StringValidation(label = "نوع ارز")
    @Schema(name = NamingProperty.CURRENCY, description = "نوع ارز (مثال: GOLD, SILVER, PLATINUM)", example = "GOLD")
    @JsonProperty(NamingProperty.CURRENCY)
    private String currency;

    @Schema(name = NamingProperty.MERCHANT_ID, description = "شناسه پذیرنده")
    @JsonProperty(NamingProperty.MERCHANT_ID)
    private String merchantId;

    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = "شماره حساب")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String walletAccountNumber;

    @Schema(name = NamingProperty.ADDITIONAL_DATA, description = "اطلاعات تکمیلی")
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    private String additionalData;


    @Schema(name = NamingProperty.SIGN, description = "عبارت امضا شده")
    @JsonProperty(NamingProperty.SIGN)
    private String sign;

    @Hidden
    public String getDataString() {
        return uniqueIdentifier + "|" + merchantId + "|" + quantity + "|" + nationalCode;
    }
}
