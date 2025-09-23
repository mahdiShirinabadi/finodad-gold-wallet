package com.melli.wallet.domain.request.limitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Class Name: CreateLimitationGeneralCustomRequestJson
 * Author: Mahdi Shirinabadi
 * Date: 1/20/2025
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateLimitationGeneralCustomRequestJson {

    @Schema(name = NamingProperty.LIMITATION_GENERAL_ID)
    @JsonProperty(NamingProperty.LIMITATION_GENERAL_ID)
    @NotNull(message = "General limitation ID is required")
    @NumberValidation(label = NamingProperty.LIMITATION_GENERAL_ID)
    private String limitationGeneralId;

    @Schema(name = NamingProperty.VALUE, required = true)
    @JsonProperty(NamingProperty.VALUE)
    @NotBlank(message = "Limitation value is required")
    @StringValidation(label = NamingProperty.VALUE)
    private String value;

    @Schema(name = NamingProperty.ADDITIONAL_DATA)
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    @StringValidation(label = NamingProperty.ADDITIONAL_DATA)
    private String additionalData;

    @Schema(name = "walletLevelId")
    @JsonProperty("walletLevelId")
    @NumberValidation(label = "walletLevelId")
    private String walletLevelId;

    @Schema(name = "walletAccountTypeId")
    @JsonProperty("walletAccountTypeId")
    @NumberValidation(label = "walletAccountTypeId")
    private String walletAccountTypeId;

    @Schema(name = "walletAccountCurrencyId")
    @JsonProperty("walletAccountCurrencyId")
    @NumberValidation(label = "walletAccountCurrencyId")
    private String walletAccountCurrencyId;

    @Schema(name = "walletTypeId")
    @JsonProperty("walletTypeId")
    @NumberValidation(label = "walletTypeId")
    private String walletTypeId;

    @Schema(name = "channelId")
    @JsonProperty("channelId")
    @NumberValidation(label = "channelId")
    private String channelId;
} 