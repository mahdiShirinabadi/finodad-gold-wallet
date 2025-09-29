package com.melli.wallet.domain.response.collateral;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Class Name: CollateralResponse
 * Author: Mahdi Shirinabadi
 * Date: 5/14/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CollateralResponse {

    @Schema(name = NamingProperty.LIST)
    @JsonProperty(NamingProperty.LIST)
    private List<CollateralObject> collateralObjectList;

}
