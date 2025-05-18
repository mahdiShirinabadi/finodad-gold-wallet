package com.melli.wallet.domain.response.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Name: WalletAccountCurrencyObject
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
@Setter
@Getter
@ToString
public class WalletAccountTypeObject {

    @Schema(name = NamingProperty.ID)
    @JsonProperty(NamingProperty.ID)
    private String id;

    @Schema(name = NamingProperty.NAME)
    @JsonProperty(NamingProperty.NAME)
    private String name;

    @Schema(name = NamingProperty.ADDITIONAL_DATA)
    @JsonProperty(NamingProperty.ADDITIONAL_DATA)
    private String additionalData;

    @Schema(name = NamingProperty.DESCRIPTION)
    @JsonProperty(NamingProperty.DESCRIPTION)
    private String description;
}
