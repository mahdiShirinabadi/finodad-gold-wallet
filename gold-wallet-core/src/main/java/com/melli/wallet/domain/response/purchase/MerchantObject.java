package com.melli.wallet.domain.response.purchase;

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
public class MerchantObject {

    @Schema(name = NamingProperty.ID)
    @JsonProperty(NamingProperty.ID)
    private String id;

    @Schema(name = NamingProperty.NAME)
    @JsonProperty(NamingProperty.NAME)
    private String name;

    @Schema(name = NamingProperty.LOGO)
    @JsonProperty(NamingProperty.LOGO)
    private String logo;
}
