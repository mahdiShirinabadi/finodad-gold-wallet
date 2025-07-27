package com.melli.wallet.domain.response.purchase;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Class Name: PurchaseResponse
 * Author: Mahdi Shirinabadi
 * Date: 1/27/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseTrackResponse {

    @Schema(name = NamingProperty.PURCHASE_TRACK_OBJECT_LIST)
    @JsonProperty(NamingProperty.PURCHASE_TRACK_OBJECT_LIST)
    private List<PurchaseTrackObject> purchaseTrackObjectList;
}
