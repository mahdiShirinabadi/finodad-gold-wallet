package com.melli.wallet.domain.response.collateral;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Class Name: MerchantResponse
 * Author: Mahdi Shirinabadi
 * Date: 5/14/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CollateralObject {

    @Schema(name = NamingProperty.ID)
    @JsonProperty(NamingProperty.ID)
    private String id;

    @Schema(name = NamingProperty.NAME)
    @JsonProperty(NamingProperty.NAME)
    private String name;

    @Schema(name = NamingProperty.LOGO)
    @JsonProperty(NamingProperty.LOGO)
    private String logo;

    @Schema(name = NamingProperty.STATUS)
    @JsonProperty(NamingProperty.STATUS)
    private String status;

    @Schema(name = NamingProperty.IBAN)
    @JsonProperty(NamingProperty.IBAN)
    private String iban;
}
