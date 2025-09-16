package com.melli.wallet.domain.response.stock;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.domain.response.base.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Class Name: Setting
 * Author: Mahdi Shirinabadi
 * Date: 4/19/2025
 */
@Setter
@Getter
public class StockCurrencyListResponse extends PageResponse {

    @Schema(name = NamingProperty.LIST)
    @JsonProperty(NamingProperty.LIST)
    private List<StockCurrencyObject> stockCurrencyObjectList;
}
