package com.melli.wallet.domain.response.cash;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CashOutTrackResponse {

    @Schema(name = NamingProperty.ID)
    @JsonProperty(NamingProperty.ID)
    private long  id;

    @Schema(name = NamingProperty.NATIONAL_CODE)
    @JsonProperty(NamingProperty.NATIONAL_CODE)
    private String nationalCode;

    @Schema(name = NamingProperty.PRICE)
    @JsonProperty(NamingProperty.PRICE)
    private long price;

    @Schema(name = NamingProperty.UNIQUE_IDENTIFIER)
    @JsonProperty(NamingProperty.UNIQUE_IDENTIFIER)
    private String uniqueIdentifier;

    @Schema(name = NamingProperty.RESULT)
    @JsonProperty(NamingProperty.RESULT)
    private int result;

    @Schema(name = NamingProperty.SETTLEMENT_STATUS)
    @JsonProperty(NamingProperty.SETTLEMENT_STATUS)
    private String settlementStatus;

    @Schema(name = NamingProperty.SETTLEMENT_STATUS_DESCRIPTION)
    @JsonProperty(NamingProperty.SETTLEMENT_STATUS_DESCRIPTION)
    private String settlementStatusDescription;

    @Schema(name = NamingProperty.DESCRIPTION)
    @JsonProperty(NamingProperty.DESCRIPTION)
    private String description;

    @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER)
    @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
    private String walletAccountNumber;

    @Schema(name = NamingProperty.CREATE_TIME)
    @JsonProperty(NamingProperty.CREATE_TIME)
    private String createTime;

    @Schema(name = NamingProperty.CREATE_TIMESTAMP)
    @JsonProperty(NamingProperty.CREATE_TIMESTAMP)
    private long createTimeTimestamp;

    @Schema(name = NamingProperty.SETTLEMENT_OBJECT)
    @JsonProperty(NamingProperty.SETTLEMENT_OBJECT)
    private SettlementObject settlementObject;

}
