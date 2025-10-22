package com.melli.wallet.domain.request.template;

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
public class TemplateUpdateRequestJson {

    @NumberValidation(label = NamingProperty.ID)
    @Schema(name = NamingProperty.ID, description = "شناسه قالب", example = "1")
    @JsonProperty(NamingProperty.ID)
    private String id;

    @StringValidation(label = NamingProperty.NAME)
    @Schema(name = NamingProperty.NAME, description = "نام قالب", example = "SMS_OTP")
    @JsonProperty(NamingProperty.NAME)
    private String name;

    @StringValidation(label = NamingProperty.VALUE)
    @Schema(name = NamingProperty.VALUE, description = "مقدار قالب", example = "کد تایید شما: {code}")
    @JsonProperty(NamingProperty.VALUE)
    private String value;
}
