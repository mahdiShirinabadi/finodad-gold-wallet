package com.melli.wallet.domain.request.limitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;

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

    @Schema(name = NamingProperty.ID, required = true)
    @JsonProperty(NamingProperty.ID)
    @NotNull(message = "General limitation ID is required")
    @NumberValidation(label = "شناسه محدودیت عمومی")
    private String limitationGeneralId;

    @Schema(name = NamingProperty.VALUE, required = true)
    @JsonProperty(NamingProperty.VALUE)
    @NotBlank(message = "Limitation value is required")
    @StringValidation(label = "مقدار محدودیت")
    private String value;

    @Schema(name = NamingProperty.ADDITIONAL_DATA)
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    @StringValidation(label = "اطلاعات اضافی")
    private String additionalData;

    @Schema(name = "walletLevelId")
    @JsonProperty("walletLevelId")
    @NumberValidation(label = "شناسه سطح کیف پول")
    private String walletLevelId;

    @Schema(name = "walletAccountTypeId")
    @JsonProperty("walletAccountTypeId")
    @NumberValidation(label = "شناسه نوع حساب کیف پول")
    private String walletAccountTypeId;

    @Schema(name = "walletAccountCurrencyId")
    @JsonProperty("walletAccountCurrencyId")
    @NumberValidation(label = "شناسه ارز حساب کیف پول")
    private String walletAccountCurrencyId;

    @Schema(name = "walletTypeId")
    @JsonProperty("walletTypeId")
    @NumberValidation(label = "شناسه نوع کیف پول")
    private String walletTypeId;

    @Schema(name = "channelId")
    @JsonProperty("channelId")
    @NumberValidation(label = "شناسه کانال")
    private String channelId;
} 