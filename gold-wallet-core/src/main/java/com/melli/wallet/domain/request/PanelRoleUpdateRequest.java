package com.melli.wallet.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.number.ListElementsInteger;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PanelRoleUpdateRequest {

    @NumberValidation(label = "شناسه", allowDecimal = false)
    @Schema(name = NamingProperty.ID, description = "شناسه")
    @JsonProperty(NamingProperty.ID)
    private String id;

    @StringValidation(label = "عنوان نقش")
    @Schema(name = NamingProperty.NAME, description = "عنوان نقش")
    @JsonProperty(required = true, value = NamingProperty.NAME)
    private String name;

    @StringValidation(label = "توضیحات نقش")
    @Schema(name = NamingProperty.PERSIAN_DESCRIPTION, description = "توضیحات نقش")
    @JsonProperty(required = true, value = NamingProperty.PERSIAN_DESCRIPTION)
    private String persianDescription;

    @Schema(name = NamingProperty.ADDITIONAL_DATA)
    @JsonProperty(value = NamingProperty.ADDITIONAL_DATA)
    private String additionalData;

    @Schema(name = NamingProperty.RESOURCE_IDS)
    @JsonProperty(value = NamingProperty.RESOURCE_IDS)
    private List<String> resourceIds;
}