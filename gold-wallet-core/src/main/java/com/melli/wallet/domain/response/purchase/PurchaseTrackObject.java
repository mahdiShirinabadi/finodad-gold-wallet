package com.melli.wallet.domain.response.purchase;

import lombok.*;

/**
 * Class Name: PurchaseResponse
 * Author: Mahdi Shirinabadi
 * Date: 1/27/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseTrackObject {

    private String nationalCode;
    private String balance;
    private String amount;
    private String price;
    private String uniqueIdentifier;
    private String type;
    private String accountNumber;
    private String result;
    private String description;
    private String channelName;
    private String createTime;
    private Long createTimeTimestamp;

}
