package com.melli.wallet.domain.response.collateral;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Class Name: CollateralTrackResponse
 * Author: Mahdi Shirinabadi
 * Date: 9/28/2025
 */
@Setter
@Getter
@ToString
public class CollateralTrackResponse {


    @Schema(name = NamingProperty.CREATE_COLLATERAL)
    @JsonProperty(NamingProperty.CREATE_COLLATERAL)
    private CollateralCreateTrackObject collateralCreateTrackObject;

    @Schema(name = NamingProperty.RELEASE_LIST)
    @JsonProperty(NamingProperty.RELEASE_LIST)
    private List<CollateralReleaseTrackObject> collateralReleaseTrackObject;

    @Schema(name = NamingProperty.INCREASE_LIST)
    @JsonProperty(NamingProperty.INCREASE_LIST)
    private List<CollateralIncreaseTrackObject> collateralIncreaseTrackObject;

}
