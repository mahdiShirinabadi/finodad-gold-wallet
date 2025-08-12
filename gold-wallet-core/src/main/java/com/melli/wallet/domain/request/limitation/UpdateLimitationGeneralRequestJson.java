package com.melli.wallet.domain.request.limitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Class Name: UpdateLimitationGeneralRequestJson
 * Author: Mahdi Shirinabadi
 * Date: 1/20/2025
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateLimitationGeneralRequestJson {

    @Schema(name = NamingProperty.ID)
    @JsonProperty(NamingProperty.ID)
    @NotNull(message = "Limitation ID is required")
    @StringValidation(label = "شناسه محدودیت")
    private String id;

    @Schema(name = NamingProperty.VALUE)
    @JsonProperty(NamingProperty.VALUE)
    @NotBlank(message = "Limitation value is required")
    @StringValidation(label = "مقدار محدودیت")
    private String value;

    @Schema(name = NamingProperty.PATTERN)
    @JsonProperty(NamingProperty.PATTERN)
    @StringValidation(label = "الگوی محدودیت")
    private String pattern;
} 