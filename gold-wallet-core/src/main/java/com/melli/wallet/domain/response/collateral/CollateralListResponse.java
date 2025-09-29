package com.melli.wallet.domain.response.collateral;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.domain.response.base.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Class Name: CollateralListResponse
 * Author: Mahdi Shirinabadi
 * Date: 9/29/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CollateralListResponse extends PageResponse {

    @Schema(name = NamingProperty.LIST)
    @JsonProperty(NamingProperty.LIST)
    private List<CollateralCreateTrackObject> collateralCreateTrackObjectList;
}
