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
public class GiftCardProcessObjectDTO {

    private ChannelEntity channelEntity;
    private String uniqueIdentifier;
    private String quantity;
    private BigDecimal commission;
    private String commissionType;
    private String nationalCode;
    private String accountNumber;
    private String destinationNationalCode;
    private String ip;
    private String additionalData;
}
