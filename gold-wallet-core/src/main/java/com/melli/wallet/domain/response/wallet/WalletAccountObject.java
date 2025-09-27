package com.melli.wallet.domain.response.wallet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class WalletAccountObject {

    @Schema(name = NamingProperty.WALLET_ACCOUNT_TYPE_OBJECT)
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_TYPE_OBJECT)
    private WalletAccountTypeObject walletAccountTypeObject;

    @Schema(name = NamingProperty.WALLET_ACCOUNT_CURRENCY_OBJECT)
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_CURRENCY_OBJECT)
    private WalletAccountCurrencyObject walletAccountCurrencyObject;

    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER)
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String accountNumber;

    @Schema(name = NamingProperty.BALANCE)
    @JsonProperty(NamingProperty.BALANCE)
    private String balance;

    @Schema(name = NamingProperty.AVAILABLE_BALANCE)
    @JsonProperty(NamingProperty.AVAILABLE_BALANCE)
    private String availableBalance;

    @Schema(name = NamingProperty.STATUS)
    @JsonProperty(NamingProperty.STATUS)
    private String status;

    @Schema(name = NamingProperty.STATUS_DESCRIPTION)
    @JsonProperty(NamingProperty.STATUS_DESCRIPTION)
    private String statusDescription;
}
