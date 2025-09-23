package com.melli.wallet.domain.request.merchant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

/**
 * Class Name: MerchantBalanceRequest
 * Author: Mahdi Shirinabadi
 * Date: 6/7/2025
 */
@Getter
@Setter
public class MerchantUpdateRequest {

    @NumberValidation(label = NamingProperty.STATUS)
    @JsonProperty(NamingProperty.STATUS)
    private String status;

    @NumberValidation(label = NamingProperty.MERCHANT_ID)
    @JsonProperty(NamingProperty.MERCHANT_ID)
    private String merchantId;
} 