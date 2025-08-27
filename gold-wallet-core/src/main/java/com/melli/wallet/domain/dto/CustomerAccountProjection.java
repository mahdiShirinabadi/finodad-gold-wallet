package com.melli.wallet.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Projection for efficient customer list queries
 * This combines wallet and account data in a single query
 */
public interface CustomerAccountProjection {

    @JsonProperty("accountId")
    Long getAccountId();
    
    @JsonProperty("accountNumber")
    String getAccountNumber();
    
    @JsonProperty("accountStatus")
    String getAccountStatus();
    
    @JsonProperty("balance")
    String getBalance();
    
    @JsonProperty("accountTypeName")
    String getAccountTypeName();
    
    @JsonProperty("currencyName")
    String getCurrencyName();
}
