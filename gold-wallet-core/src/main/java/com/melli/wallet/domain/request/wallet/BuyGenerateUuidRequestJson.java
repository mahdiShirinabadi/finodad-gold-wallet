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
public class BuyGenerateUuidRequestJson {
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "کدملی", example = "0063360993")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @NumberValidation
    @Schema(name = NamingProperty.PRICE, description = "مبلغ به ریال", example = "1000")
    @JsonProperty(NamingProperty.PRICE)
    private String price;

    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = "شماره حساب")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String accountNumber;

    @Schema(name = NamingProperty.MERCHANT_ID, description = "اطلاعات پذیرنده")
    @JsonProperty(NamingProperty.MERCHANT_ID)
    private String merchantId;

    @NumberValidation(allowDecimal = true)
    @Schema(name = NamingProperty.QUANTITY, description = "گرم ", example = "1.1")
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @StringValidation(label = "نوع واحد")
    @Schema(name = NamingProperty.CURRENCY, description = "نوع واحد (مثال: GOLD,RIAL, SILVER, PLATINUM)", example = "GOLD")
    @JsonProperty(NamingProperty.CURRENCY)
    private String currency;

}
