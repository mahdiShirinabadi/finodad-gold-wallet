package com.melli.wallet.domain.response.cash;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
public class CashInResponse {

    private String nationalCode;
    private String balance;
    private String uniqueIdentifier;
    private String accountNumber;
}
