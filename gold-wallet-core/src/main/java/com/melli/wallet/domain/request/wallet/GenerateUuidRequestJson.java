package com.melli.wallet.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class GenerateUuidRequestJson {
    @Schema(name = "nationalCode", description = "کدملی", example = "0063360993")
    @JsonProperty("nationalCode")
    private String nationalCode;
}
