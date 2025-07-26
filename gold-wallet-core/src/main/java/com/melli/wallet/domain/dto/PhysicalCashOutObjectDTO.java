package com.melli.wallet.domain.dto;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import lombok.*;

import java.math.BigDecimal;

/**
 * Class Name: ChargeObjectDTO
 * Author: Mahdi Shirinabadi
 * Date: 5/6/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PhysicalCashOutObjectDTO {

    private ChannelEntity channel;
    private String nationalCode;
    private String uniqueIdentifier;
    private BigDecimal quantity;
    private String accountNumber;
    private String additionalData;
    private String ip;
    private BigDecimal commission;
    private String currency;
    private String commissionType;
}
