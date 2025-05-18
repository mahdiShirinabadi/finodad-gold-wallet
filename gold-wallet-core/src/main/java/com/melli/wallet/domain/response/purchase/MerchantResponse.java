package com.melli.wallet.domain.response.purchase;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

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
public class MerchantResponse {

    @Schema(name = NamingProperty.MERCHANT_OBJECT_LIST)
    @JsonProperty(NamingProperty.MERCHANT_OBJECT_LIST)
    private List<MerchantObject> merchantObjectList;

}
