package com.melli.wallet.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.national_code.NationalCodeValidation;
import com.melli.wallet.annotation.number.NumberValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CashInGenerateUuidRequestJson {
    @NationalCodeValidation(label = "کد ملی")
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "کدملی", example = "0063360993")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @NumberValidation(label = "مبلغ")
    @Schema(name = NamingProperty.QUANTITY, description = "مبلغ به ریال", example = "1000")
    @JsonProperty(NamingProperty.QUANTITY)
    private String amount;

    @NumberValidation(label = "شماره حساب")
    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = "شماره حساب")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String accountNumber;
}
