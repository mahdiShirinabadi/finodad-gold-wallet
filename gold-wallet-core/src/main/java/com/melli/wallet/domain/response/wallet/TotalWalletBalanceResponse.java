package com.melli.wallet.domain.response.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Class Name: TotalWalletBalanceResponse
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Response for total wallet balance calculation excluding MERCHANT wallets
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TotalWalletBalanceResponse {

    @Schema(description = "Total balance of all non-merchant wallets")
    @JsonProperty("totalBalance")
    private BigDecimal totalBalance;

    @Schema(description = "Wallet type filter applied")
    @JsonProperty("excludedWalletType")
    private String excludedWalletType;
}
