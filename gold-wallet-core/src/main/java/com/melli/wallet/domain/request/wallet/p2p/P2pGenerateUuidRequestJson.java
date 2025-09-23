package com.melli.wallet.domain.request.wallet.p2p;

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
public class P2pGenerateUuidRequestJson {

    @NumberValidation(allowDecimal = true, label = NamingProperty.QUANTITY)
    @Schema(name = NamingProperty.QUANTITY, description = "مقدار", example = "0.001")
    @JsonProperty(NamingProperty.QUANTITY)
    private String quantity;

    @StringValidation(label = NamingProperty.NATIONAL_CODE)
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "کد ملی مبدا")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @StringValidation(label = NamingProperty.WALLET_ACCOUNT_NUMBER)
    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER, description = "شماره حساب کیف مبدا")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String accountNumber;

    @StringValidation(label = NamingProperty.DEST_WALLET_ACCOUNT_NUMBER)
    @Schema(name = NamingProperty.DEST_WALLET_ACCOUNT_NUMBER, description = "شماره حساب کیف")
    @JsonProperty(NamingProperty.DEST_WALLET_ACCOUNT_NUMBER)
    private String destAccountNumber;
}
