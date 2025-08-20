package com.melli.wallet.domain.response.panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.domain.response.wallet.WalletAccountTypeObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Class Name: WalletAccountTypeListResponse
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
@Setter
@Getter
@ToString
public class WalletAccountTypeListResponse {

    @Schema(name = "walletAccountTypes")
    @JsonProperty("walletAccountTypes")
    private List<WalletAccountTypeObject> walletAccountTypes;
}
