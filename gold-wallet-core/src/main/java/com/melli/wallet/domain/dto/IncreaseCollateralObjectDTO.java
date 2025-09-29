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
public class IncreaseCollateralObjectDTO {

    private ChannelEntity channelEntity;
    private String collateralCode;
    private BigDecimal quantity;
    private String nationalCode;
    private String description;
    private BigDecimal commission;
    private String commissionCurrency;
    private String ip;
}
