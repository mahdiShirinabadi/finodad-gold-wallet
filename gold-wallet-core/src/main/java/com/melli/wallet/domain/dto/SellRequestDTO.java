package com.melli.wallet.domain.dto;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import lombok.*;

/**
 * Class Name: BuyRequestDTO
 * Author: Mahdi Shirinabadi
 * Date: 5/6/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SellRequestDTO {
    private ChannelEntity channel;
    private String uniqueIdentifier;
    private float amount;
    private long price;
    private String walletAccountNumber;
    private String additionalData;
    private String merchantId;
    private String nationalCode;
    private float commission;
    private String currency;
    private String ip;
    private String commissionCurrency;
}
