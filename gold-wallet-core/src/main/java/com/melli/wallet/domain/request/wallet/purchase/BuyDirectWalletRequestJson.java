package com.melli.wallet.domain.request.wallet.purchase;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.national_code.NationalCodeValidation;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import com.melli.wallet.domain.request.wallet.CommissionObject;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BuyDirectWalletRequestJson {

    @StringValidation(label = NamingProperty.UNIQUE_IDENTIFIER)
    @Schema(name = NamingProperty.UNIQUE_IDENTIFIER, description = "شناسه یکتا")
    @JsonProperty(NamingProperty.UNIQUE_IDENTIFIER)
    private String uniqueIdentifier;

    @NumberValidation(allowDecimal = true, minDecimalValue = "0.00001", label = NamingProperty.QUANTITY)
    @Schema(name = NamingProperty.QUANTITY, description = "مقدار طلا", example = "1.1")
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @NumberValidation(label = NamingProperty.PRICE)
    @Schema(name = NamingProperty.PRICE, description = " مبلغ بدون کارمزد به ریال", example = "1000")
    @JsonProperty(NamingProperty.PRICE)
    private String price;

    @Schema(name = NamingProperty.COMMISSION_OBJECT, description = "کارمزد")
    @JsonProperty(NamingProperty.COMMISSION_OBJECT)
    private CommissionObject commissionObject;

    @NationalCodeValidation(label = NamingProperty.NATIONAL_CODE)
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "کدملی")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @StringValidation(label = NamingProperty.CURRENCY)
    @Schema(name = NamingProperty.CURRENCY, description = "نوع واحد (مثال: GOLD,RIAL, SILVER, PLATINUM)", example = "GOLD")
    @JsonProperty(NamingProperty.CURRENCY)
    private String currency;

    @NumberValidation(label = NamingProperty.MERCHANT_ID)
    @Schema(name = NamingProperty.MERCHANT_ID , description = "شناسه پذیرنده")
    @JsonProperty(NamingProperty.MERCHANT_ID)
    private String merchantId;

    @StringValidation(label = NamingProperty.WALLET_ACCOUNT_NUMBER)
    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = "شماره حساب")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String walletAccountNumber;

    @Schema(name = NamingProperty.ADDITIONAL_DATA, description = "اطلاعات تکمیلی")
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    private String additionalData;

    @StringValidation(label = NamingProperty.REFERENCE_NUMBER)
    @Schema(name = NamingProperty.REFERENCE_NUMBER, description = "شماره مرجع تراکنش")
    @JsonProperty(NamingProperty.REFERENCE_NUMBER)
    private String refNumber;

    @Schema(name = NamingProperty.SIGN, description = "عبارت امضا شده")
    @JsonProperty(NamingProperty.SIGN)
    private String sign;

    @Hidden
    public String getDataString() {
        return uniqueIdentifier + "|" + merchantId + "|" + price + "|" + nationalCode;
    }
}
