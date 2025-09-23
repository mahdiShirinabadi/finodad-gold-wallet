package com.melli.wallet.domain.request.wallet.giftcard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
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
public class GiftCardProcessRequestJson {

    @NumberValidation(allowDecimal = true, label = NamingProperty.QUANTITY)
    @Schema(name = NamingProperty.QUANTITY, description = "مقدار", example = "0.001")
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @StringValidation(label = NamingProperty.NATIONAL_CODE)
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "کد ملی مبدا")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @StringValidation(label = NamingProperty.WALLET_ACCOUNT_NUMBER)
    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = "شماره حساب کیف مبدا")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String accountNumber;

    @Schema(name = NamingProperty.DEST_NATIONAL_CODE, description = "برای امنیت بیشتر کد ملی مقصد")
    @JsonProperty(NamingProperty.DEST_NATIONAL_CODE)
    private String destinationNationalCode;

    @StringValidation(label = NamingProperty.ADDITIONAL_DATA)
    @Schema(name = NamingProperty.ADDITIONAL_DATA, description = "اطلاعات تکمیلی")
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    private String additionalData;


    @StringValidation(label = NamingProperty.UNIQUE_IDENTIFIER)
    @Schema(name = NamingProperty.UNIQUE_IDENTIFIER, description = "کد پیگیری")
    @JsonProperty(NamingProperty.UNIQUE_IDENTIFIER)
    private String uniqueIdentifier;

    @Schema(name = NamingProperty.SIGN, description = "امضا دیجیتال")
    @JsonProperty(NamingProperty.SIGN)
    private String sign;

    @Schema(name = NamingProperty.COMMISSION_OBJECT, description = "کارمزد")
    @JsonProperty(NamingProperty.COMMISSION_OBJECT)
    private CommissionObject commissionObject;

    @StringValidation(label = NamingProperty.CURRENCY)
    @Schema(name = NamingProperty.CURRENCY, description = "نوع ارز (مثال: GOLD, SILVER, PLATINUM)", example = "GOLD")
    @JsonProperty(NamingProperty.CURRENCY)
    private String currency;

    @Hidden
    public String getDataString() {
        return uniqueIdentifier + "|" + quantity + "|" + nationalCode;
    }
}
