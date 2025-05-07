package com.melli.wallet.domain.response.cash;

import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CashOutResponse {

    private String nationalCode;
    private String balance;
    private String uniqueIdentifier;
    private String walletAccountNumber;
}
