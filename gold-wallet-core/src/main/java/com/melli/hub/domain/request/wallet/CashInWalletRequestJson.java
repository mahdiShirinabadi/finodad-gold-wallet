package com.melli.hub.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.hub.annotation.national_code.NationalCodeValidation;
import com.melli.hub.annotation.number.NumberValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CashInWalletRequestJson {

    @Schema(name = "شناسه یکتا")
    @JsonProperty("uniqueIdentifier")
    private String uniqueIdentifier;

    @Schema(name = "شماره مرجع")
    @JsonProperty("referenceNumber")
    private String referenceNumber;

    @Schema(name = "amount", description = "مبلغ به ریال", example = "1000")
    @JsonProperty("amount")
    private String amount;

    @NationalCodeValidation(label = "کد ملی")
    @Schema(name = "nationalCode", description = "کدملی")
    @JsonProperty("nationalCode")
    private String nationalCode;

    @NationalCodeValidation(label = "کد ملی")
    @Schema(name = "additionalData", description = "کدملی")
    @JsonProperty("additionalData")
    private String additionalData;


    @Schema(name = "sign", description = "کدملی")
    @JsonProperty("sign")
    private String sign;

    public String getDataString() {
        return uniqueIdentifier + "|" + referenceNumber + "|" + amount + "|" + nationalCode;
    }
}
