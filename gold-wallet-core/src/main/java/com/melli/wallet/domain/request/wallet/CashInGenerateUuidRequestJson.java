package com.melli.wallet.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.annotation.number.NumberValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CashInGenerateUuidRequestJson {
    @Schema(name = "nationalCode", description = "کدملی", example = "0063360993")
    @JsonProperty("nationalCode")
    private String nationalCode;

    @NumberValidation
    @Schema(name = "amount", description = "مبلغ به ریال", example = "1000")
    @JsonProperty("amount")
    private String amount;

    @Schema(name = "accountNumber", description = "شماره حساب")
    @JsonProperty("accountNumber")
    private String accountNumber;
}
