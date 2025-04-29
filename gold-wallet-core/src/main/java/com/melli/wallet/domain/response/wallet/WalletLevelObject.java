package com.melli.wallet.domain.response.wallet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Name: WalletLevelObject
 * Author: Mahdi Shirinabadi
 * Date: 4/19/2025
 */
@Setter
@Getter
@ToString
public class WalletLevelObject {
    private String id;
    private String name;
    private String additionalData;
}
