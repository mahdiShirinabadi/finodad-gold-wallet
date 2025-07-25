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
public class ChargeObjectDTO {

    private ChannelEntity channel;
    private String nationalCode;
    private String uniqueIdentifier;
    private String amount;
    private String refNumber;
    private String accountNumber;
    private String additionalData;
    private String ip;
    private String cashInPaymentType;
}
