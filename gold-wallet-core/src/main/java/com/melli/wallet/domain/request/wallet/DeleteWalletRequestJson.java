package com.melli.wallet.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DeleteWalletRequestJson {

    @Schema(name = NamingProperty.ID, description = "شناسه کیف پول", example = "1")
    @JsonProperty(NamingProperty.ID)
    private String id;
}
