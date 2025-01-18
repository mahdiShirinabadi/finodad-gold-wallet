package com.melli.hub.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ActiveWalletRequestJson {

    @Schema(name = "id", description = "شناسه کیف پول", example = "1")
    @JsonProperty("id")
    private String id;
}
