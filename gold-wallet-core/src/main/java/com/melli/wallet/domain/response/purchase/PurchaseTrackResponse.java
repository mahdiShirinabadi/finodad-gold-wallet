package com.melli.wallet.domain.response.purchase;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
public class PurchaseTrackResponse {

    @Schema(name = NamingProperty.PURCHASE_TRACK_OBJECT_LIST)
    @JsonProperty(NamingProperty.PURCHASE_TRACK_OBJECT_LIST)
    private List<PurchaseTrackObject> purchaseTrackObjectList;
}
