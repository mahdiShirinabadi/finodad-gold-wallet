package com.melli.wallet.domain.response.cash;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Class Name: PhysicalCashOutTotalQuantityResponse
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Response for total quantity calculation of physical cash out transactions
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalCashOutTotalQuantityResponse {

    @Schema(description = "Total quantity of all physical cash out transactions")
    @JsonProperty("totalQuantity")
    private BigDecimal totalQuantity;

    @Schema(description = "Request type identifier")
    @JsonProperty("requestType")
    private String requestType;
}
