package com.melli.wallet.domain.dto;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import lombok.*;

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
public class SeizeCollateralObjectDTO {
    private ChannelEntity channelEntity;
    private String collateralCode;
    private String nationalCode;
    private String description;
    private String ip;
}
