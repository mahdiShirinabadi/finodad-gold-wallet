package com.melli.hub.domain.response.purchase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class Name: PurchaseResponse
 * Author: Mahdi Shirinabadi
 * Date: 1/27/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
public class PurchaseResponse {

    private String nationalCode;
    private String balance;
    private String amount;
    private String uniqueIdentifier;
    private String type;
    private String accountNumber;

}
