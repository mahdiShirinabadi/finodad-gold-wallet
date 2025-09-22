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
public class GiftCardPaymentObjectDTO {

    private ChannelEntity channelEntity;
    private String giftCardUniqueCode;
    private String quantity;
    private String currency;
    private String nationalCode;
    private String ip;
}
