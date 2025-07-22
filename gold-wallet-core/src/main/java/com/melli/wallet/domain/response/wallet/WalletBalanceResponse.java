package com.melli.wallet.domain.response.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Class Name: WalletBalanceResponse
 * Author: Mahdi Shirinabadi
 * Date: 7/22/2025
 */
@Setter
@Getter
@ToString
public class WalletBalanceResponse {

    @Schema(name = NamingProperty.WALLET_ACCOUNT_OBJECT_LIST)
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_OBJECT_LIST)
    private List<WalletAccountObject> walletAccountObjectList;
}
