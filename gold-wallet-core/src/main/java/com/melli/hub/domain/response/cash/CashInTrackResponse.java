package com.melli.hub.domain.response.cash;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CashInTrackResponse {
    private long  id;
    private String nationalCode;
    private String refNumber;
    private long amount;
    private String uniqueIdentifier;
    private int result;
    private String description;
    private String accountNumber;
    private String createTime;
    private long createTimeTimestamp;
}
