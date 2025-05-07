package com.melli.wallet.domain.response.cash;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CashOutTrackResponse {
    private long  id;
    private String nationalCode;
    private String refNumber;
    private long amount;
    private String uniqueIdentifier;
    private int result;
    private String description;
    private String walletAccountNumber;
    private String createTime;
    private long createTimeTimestamp;
}
