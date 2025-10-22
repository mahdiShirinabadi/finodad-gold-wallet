package com.melli.wallet.domain.request.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class StatusCreateRequestJson {

    @StringValidation(label = NamingProperty.CODE)
    @Schema(name = NamingProperty.CODE, description = "کد وضعیت", example = "SUCCESS")
    @JsonProperty(NamingProperty.CODE)
    private String code;

    @StringValidation(label = NamingProperty.PERSIAN_DESCRIPTION)
    @Schema(name = NamingProperty.PERSIAN_DESCRIPTION, description = "توضیحات فارسی", example = "موفق")
    @JsonProperty(NamingProperty.PERSIAN_DESCRIPTION)
    private String persianDescription;
}
