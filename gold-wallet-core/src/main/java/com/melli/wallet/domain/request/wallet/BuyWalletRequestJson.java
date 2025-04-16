package com.melli.wallet.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.annotation.national_code.NationalCodeValidation;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BuyWalletRequestJson {

    @StringValidation
    @Schema(name = "شناسه یکتا")
    @JsonProperty("uniqueIdentifier")
    private String uniqueIdentifier;

    @NumberValidation
    @Schema(name = "amount", description = "مقدار طلا", example = "1.1")
    @JsonProperty("amount")
    private String amount;

    @NumberValidation
    @Schema(name = "price", description = "مبلغ با کارمزد به ریال", example = "1000")
    @JsonProperty("price")
    private String price;

    @NumberValidation
    @Schema(name = "commission", description = "کارمزد خرید به ریال", example = "1000")
    @JsonProperty("commission")
    private String commission;

    @NationalCodeValidation(label = "کد ملی")
    @Schema(name = "nationalCode", description = "کدملی")
    @JsonProperty("nationalCode")
    private String nationalCode;

    @StringValidation(label = "نوع ارز")
    @Schema(name = "currency", description = "نوع ارز (مثال: GOLD, SILVER, PLATINUM)", example = "GOLD")
    @JsonProperty("currency")
    private String currency;

    @Schema(name = "merchantId" , description = "شناسه پذیرنده")
    @JsonProperty("merchantId")
    private String merchantId;

    @Schema(name = "walletAccountNumber", description = "شماره حساب")
    @JsonProperty("walletAccountNumber")
    private String walletAccountNumber;

    @Schema(name = "additionalData", description = "اطلاعات تکمیلی")
    @JsonProperty("additionalData")
    private String additionalData;


    @Schema(name = "sign", description = "عبارت امضا شده")
    @JsonProperty("sign")
    private String sign;

    public String getDataString() {
        return uniqueIdentifier + "|" + merchantId + "|" + amount + "|" + nationalCode;
    }
}
