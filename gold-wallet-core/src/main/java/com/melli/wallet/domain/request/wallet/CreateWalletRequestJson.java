package com.melli.wallet.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CreateWalletRequestJson {

    @Schema(name = "mobile", description = "شماره موبایا 10 رقم میباشد و با 9 شروع میشود", example = "9121234567")
    @JsonProperty("mobile")
    private String mobile;

    @Schema(name = "nationalCode", description = "کدملی", example = "0063360993")
    @JsonProperty("nationalCode")
    private String nationalCode;

}
