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
@NoArgsConstructor
@AllArgsConstructor
public class StockCurrencyObject {

    @Schema(name = NamingProperty.BALANCE)
    @JsonProperty(NamingProperty.BALANCE)
    private String balance;

    @Schema(name = NamingProperty.WALLET_ACCOUNT_CURRENCY_NAME)
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_TYPE_NAME)
    private String walletAccountCurrencyName;

}
