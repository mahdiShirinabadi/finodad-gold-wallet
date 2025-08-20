package com.melli.wallet.domain.response.panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.domain.response.wallet.WalletTypeObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Class Name: WalletTypeListResponse
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
@Setter
@Getter
@ToString
public class WalletTypeListResponse {

    @Schema(name = "walletTypes")
    @JsonProperty("walletTypes")
    private List<WalletTypeObject> walletTypes;
}
