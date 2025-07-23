package com.melli.wallet.domain.request.merchant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.Valid;

/**
 * Class Name: MerchantBalanceRequest
 * Author: Mahdi Shirinabadi
 * Date: 6/7/2025
 */
@Getter
@Setter
public class MerchantBalanceRequest {

    @Valid
    @StringValidation(label = "شماره حساب کیف پول")
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String walletAccountNumber;

    @Valid
    @StringValidation(label = "مبلغ")
    @JsonProperty(NamingProperty.AMOUNT)
    private String amount;

    @Valid
    @NumberValidation(label = "شناسه پذیرنده")
    @JsonProperty(NamingProperty.MERCHANT_ID)
    private String merchantId;
} 