package com.melli.wallet.domain.dto;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import lombok.*;

import java.math.BigDecimal;

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
public class BuyRequestDTO {
    private ChannelEntity channel;
    private String uniqueIdentifier;
    private BigDecimal amount;
    private long price;
    private String walletAccountNumber;
    private String additionalData;
    private String merchantId;
    private String nationalCode;
    private BigDecimal commission;
    private String currency;
    private String ip;
    private String refNumber;
    private String commissionType;
}
