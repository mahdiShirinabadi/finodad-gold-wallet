package com.melli.hub.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.hub.annotation.national_code.NationalCodeValidation;
import com.melli.hub.annotation.number.NumberValidation;
import com.melli.hub.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class PurchaseWalletRequestJson {

    @StringValidation
    @Schema(name = "شناسه یکتا")
    @JsonProperty("uniqueIdentifier")
    private String uniqueIdentifier;

    @NumberValidation
    @Schema(name = "amount", description = "مقدار طلا", example = "1.1")
    @JsonProperty("amount")
    private String amount;

    @NumberValidation
    @Schema(name = "price", description = "مبلغ به ریال", example = "1000")
    @JsonProperty("price")
    private String price;

    @NationalCodeValidation(label = "کد ملی")
    @Schema(name = "nationalCode", description = "کدملی")
    @JsonProperty("nationalCode")
    private String nationalCode;

    @NationalCodeValidation(label = "شماره پیگیری")
    @Schema(name = "referenceNumber", description = "شماره پیگیری")
    @JsonProperty("referenceNumber")
    private String referenceNumber;


    @Schema(name = "merchantId" , description = "شناسه پذیرنده")
    @JsonProperty("merchantId")
    private String merchantId;

    @Schema(name = "accountNumber", description = "شماره حساب")
    @JsonProperty("accountNumber")
    private String accountNumber;

    @Schema(name = "additionalData", description = "اطلاعات تکمیلی")
    @JsonProperty("additionalData")
    private String additionalData;





    @Schema(name = "purchaseType" , description = "SELL/BUY")
    @JsonProperty("purchaseType")
    private String purchaseType;


    @Schema(name = "sign", description = "عبارت امضا شده")
    @JsonProperty("sign")
    private String sign;

    public String getDataString() {
        return uniqueIdentifier + "|" + merchantId + "|" + amount + "|" + nationalCode;
    }
}
