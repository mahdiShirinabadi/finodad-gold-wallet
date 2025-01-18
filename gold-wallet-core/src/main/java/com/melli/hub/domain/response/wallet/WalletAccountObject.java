package com.melli.hub.domain.response.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class WalletAccountObject {

    @JsonProperty("walletAccountTypeObject")
    private WalletAccountTypeObject walletAccountTypeObject;

    @JsonProperty("walletAccountCurrencyObject")
    private WalletAccountCurrencyObject walletAccountCurrencyObject;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("balance")
    private String balance;

    @JsonProperty("shareAccountObject")
    private ShareAccountObject shareAccountObject;

    @JsonProperty("status")
    private String status;
}
