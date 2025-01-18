package com.melli.hub.domain.response.wallet;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Name: WalletAccountCurrencyObject
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
@Setter
@Getter
@ToString
public class WalletAccountCurrencyObject {

    private String id;
    private String name;
    private String suffix;
    private String additionalData;
    private String description;
}
