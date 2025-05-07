package com.melli.wallet.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.annotation.fund_type.PurchaseTypeValidation;
import com.melli.wallet.annotation.number.NumberValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class PurchaseGenerateUuidRequestJson {
    @Schema(name = "nationalCode", description = "کدملی", example = "0063360993")
    @JsonProperty("nationalCode")
    private String nationalCode;

    @NumberValidation
    @Schema(name = "price", description = "مبلغ به ریال", example = "1000")
    @JsonProperty("price")
    private String price;

    @Schema(name = "walletAccountNumber", description = "شماره حساب")
    @JsonProperty("walletAccountNumber")
    private String accountNumber;

    @PurchaseTypeValidation(label = "نوع تراکنش")
    @Schema(name = "purchaseType", description = "نوع تراکنش", allowableValues = {"SELL","BUY"})
    @JsonProperty("purchaseType")
    private String purchaseType;
}
