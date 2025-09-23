package com.melli.wallet.domain.request.wallet.giftcard;

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
public class PaymentGiftCardRequestJson {

    @StringValidation(label = NamingProperty.GIFT_CARD_ACTIVE_CODE)
    @Schema(name = NamingProperty.GIFT_CARD_ACTIVE_CODE, description = "مقدار", example = "0.001")
    @JsonProperty(NamingProperty.GIFT_CARD_ACTIVE_CODE)
    private String giftCardUniqueCode;

    @Schema(name = NamingProperty.QUANTITY, description = "مقدار", example = "0.001")
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @StringValidation(label = NamingProperty.CURRENCY)
    @Schema(name = NamingProperty.CURRENCY, description = "نوع ارز (مثال: GOLD, SILVER, PLATINUM)", example = "GOLD")
    @JsonProperty(NamingProperty.CURRENCY)
    private String currency;

    @Schema(name = NamingProperty.NATIONAL_CODE, description = " کد ملی فعال کننده")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = " شماره حساب فعال کننده")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String accountNumber;

    @Schema(name = NamingProperty.ADDITIONAL_DATA, description = "اطلاعات تکمیلی")
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    private String additionalData;

}
