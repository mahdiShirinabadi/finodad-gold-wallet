package com.melli.wallet.domain.response.stock;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Class Name: StockObject
 * Author: Mahdi Shirinabadi
 * Date: 9/15/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StockObject {

    @Schema(name = NamingProperty.ID)
    @JsonProperty(NamingProperty.ID)
    private String id;

    @Schema(name = NamingProperty.BALANCE)
    @JsonProperty(NamingProperty.BALANCE)
    private String balance;

    @Schema(name = NamingProperty.CODE)
    @JsonProperty(NamingProperty.CODE)
    private String code;


}
