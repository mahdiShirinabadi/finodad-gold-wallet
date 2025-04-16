package com.melli.wallet.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CashInWalletRequestJson {

    @StringValidation
    @Schema(name="uniqueIdentifier", description = "شناسه یکتا")
    @JsonProperty("uniqueIdentifier")
    private String uniqueIdentifier;

    @StringValidation
    @Schema(name="referenceNumber",description = "شماره مرجع")
    @JsonProperty("referenceNumber")
    private String referenceNumber;

    @NumberValidation
    @Schema(name = "amount", description = "مبلغ به ریال", example = "1000")
    @JsonProperty("amount")
    private String amount;

    @NationalCodeValidation(label = "کد ملی")
    @Schema(name = "nationalCode", description = "کدملی")
    @JsonProperty("nationalCode")
    private String nationalCode;

    @Schema(name = "additionalData", description = "اطلاعات تکمیلی")
    @JsonProperty("additionalData")
    private String additionalData;

    @Schema(name = "accountNumber", description = "شماره حساب")
    @JsonProperty("accountNumber")
    private String accountNumber;


    @Schema(name = "sign", description = "امضا دیجیتال")
    @JsonProperty("sign")
    private String sign;

    @Hidden
    public String getDataString() {
        return uniqueIdentifier + "|" + referenceNumber + "|" + amount + "|" + nationalCode;
    }
}
