package com.melli.wallet.domain.response.cash;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SettlementObject {

    @Schema(name = NamingProperty.ID)
    @JsonProperty(NamingProperty.ID)
    private long  id;

    @Schema(name = NamingProperty.REFERENCE_NUMBER)
    @JsonProperty(NamingProperty.REFERENCE_NUMBER)
    private String refNumber;

    @Schema(name = NamingProperty.CHANNEL_RESPONSE)
    @JsonProperty(NamingProperty.CHANNEL_RESPONSE)
    private String channelResponse;

    @Schema(name = NamingProperty.QUANTITY)
    @JsonProperty(NamingProperty.QUANTITY)
    private long amount;

    @Schema(name = NamingProperty.CREATE_TIME)
    @JsonProperty(NamingProperty.CREATE_TIME)
    private String createTime;

    @Schema(name = NamingProperty.CREATE_TIMESTAMP)
    @JsonProperty(NamingProperty.CREATE_TIMESTAMP)
    private long createTimeTimestamp;


}
