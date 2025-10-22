package com.melli.wallet.domain.response.stock;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.domain.response.base.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Class Name: StockHistoryListResponse
 * Author: AI Assistant
 * Date: 1/26/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StockHistoryListResponse extends PageResponse {

    @Schema(name = NamingProperty.LIST)
    @JsonProperty(NamingProperty.LIST)
    private List<StockHistoryObject> list;
}
