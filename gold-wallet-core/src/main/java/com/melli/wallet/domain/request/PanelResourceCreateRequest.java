package com.melli.wallet.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.bool.BooleanValidation;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PanelResourceCreateRequest {

    @StringValidation(label = "نام دسترسی")
    private String name;

    @StringValidation(label = "نام فارسی دسترسی")
    @JsonProperty(NamingProperty.FARSI_NAME)
    private String faName;

    @NumberValidation
    private int display;

}