package com.melli.wallet.domain.response.merchant;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Class Name: MerchantBalanceCalculationResponse
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Response for merchant balance calculation from transactions
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MerchantBalanceCalculationResponse {

    @Schema(description = "Calculated balance from merchant transactions")
    @JsonProperty("balance")
    private BigDecimal balance;

    @Schema(description = "Merchant ID")
    @JsonProperty("merchantId")
    private String merchantId;
}
