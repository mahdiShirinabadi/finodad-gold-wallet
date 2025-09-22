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
public class PaymentGiftCardRequestJson {

    @NumberValidation(allowDecimal = true)
    @Schema(name = NamingProperty.GIFT_CARD_UNIQUE_CODE, description = "مقدار", example = "0.001")
    @JsonProperty(NamingProperty.GIFT_CARD_UNIQUE_CODE)
    private String giftCardUniqueCode;

    @Schema(name = NamingProperty.QUANTITY, description = "مقدار", example = "0.001")
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @StringValidation(label = "نوع ارز")
    @Schema(name = NamingProperty.CURRENCY, description = "نوع ارز (مثال: GOLD, SILVER, PLATINUM)", example = "GOLD")
    @JsonProperty(NamingProperty.CURRENCY)
    private String currency;

    @Schema(name = NamingProperty.NATIONAL_CODE, description = "برای امنیت بیشتر کد ملی مقصد")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @Schema(name = NamingProperty.ADDITIONAL_DATA, description = "اطلاعات تکمیلی")
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    private String additionalData;

}
