package com.melli.wallet.domain.response.stock;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;

/**
 * Class Name: StockHistoryObject
 * Author: AI Assistant
 * Date: 1/26/2025
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StockHistoryObject {

    @Schema(name = NamingProperty.ID)
    @JsonProperty(NamingProperty.ID)
    private String id;

    @Schema(name = NamingProperty.STOCK_ID)
    @JsonProperty(NamingProperty.STOCK_ID)
    private String stockId;

    @Schema(name = NamingProperty.TRANSACTION_ID)
    @JsonProperty(NamingProperty.TRANSACTION_ID)
    private String transactionId;

    @Schema(name = NamingProperty.AMOUNT)
    @JsonProperty(NamingProperty.AMOUNT)
    private String amount;

    @Schema(name = NamingProperty.TYPE)
    @JsonProperty(NamingProperty.TYPE)
    private String type;

    @Schema(name = NamingProperty.BALANCE)
    @JsonProperty(NamingProperty.BALANCE)
    private String balance;

    @Schema(name = NamingProperty.CREATE_TIME)
    @JsonProperty(NamingProperty.CREATE_TIME)
    private Date createdAt;

    @Schema(name = NamingProperty.UPDATE_TIME)
    @JsonProperty(NamingProperty.UPDATE_TIME)
    private Date updatedAt;
}
