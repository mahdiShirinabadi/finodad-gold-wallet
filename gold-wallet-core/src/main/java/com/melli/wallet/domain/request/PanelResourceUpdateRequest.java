package com.melli.wallet.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PanelResourceUpdateRequest {

    @NumberValidation(label = NamingProperty.ID)
    @Schema(name = NamingProperty.ID, description = "شناسه")
    @JsonProperty(NamingProperty.ID)
    private String id;

    @StringValidation(label = NamingProperty.NAME)
    @JsonProperty(NamingProperty.NAME)
    private String name;

    @StringValidation(label = NamingProperty.FARSI_NAME)
    @JsonProperty(NamingProperty.FARSI_NAME)
    private String faName;

    @NumberValidation
    private int display;
}
