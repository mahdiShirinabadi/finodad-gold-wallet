package com.melli.wallet.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.national_code.NationalCodeValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CreateWalletRequestJson {

    @Schema(name = NamingProperty.MOBILE_NUMBER, description = "شماره موبایا 10 رقم میباشد و با 9 شروع میشود", example = "9121234567")
    @JsonProperty(NamingProperty.MOBILE_NUMBER)
    private String mobile;

    @NationalCodeValidation
    @Schema(name = NamingProperty.NATIONAL_CODE, description = "کدملی", example = "0063360993")
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

}
