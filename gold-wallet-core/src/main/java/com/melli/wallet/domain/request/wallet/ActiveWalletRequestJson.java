package com.melli.wallet.domain.request.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.number.NumberValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ActiveWalletRequestJson {

    @NumberValidation(label = NamingProperty.ID)
    @Schema(name = NamingProperty.ID, description = "شناسه کیف پول", example = "1")
    @JsonProperty(value = NamingProperty.ID)
    private String id;
}
