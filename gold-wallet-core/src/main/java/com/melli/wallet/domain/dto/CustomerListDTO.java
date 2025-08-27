package com.melli.wallet.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for customer list with nested wallet accounts
 * This represents a single customer (wallet) with all their accounts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerListDTO {
    
    @JsonProperty("walletId")
    private Long walletId;
    
    @JsonProperty("nationalCode")
    private String nationalCode;
    
    @JsonProperty("mobile")
    private String mobile;
    
    @JsonProperty("walletStatus")
    private String walletStatus;
    
    @JsonProperty("walletCreateTime")
    private String walletCreateTime;
    
    @JsonProperty("walletAccounts")
    private List<WalletAccountDTO> walletAccounts;
    
    /**
     * Inner DTO for wallet account information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WalletAccountDTO {
        
        @JsonProperty("accountId")
        private Long accountId;
        
        @JsonProperty("accountNumber")
        private String accountNumber;
        
        @JsonProperty("accountStatus")
        private String accountStatus;
        
        @JsonProperty("balance")
        private String balance;
        
        @JsonProperty("accountTypeName")
        private String accountTypeName;
        
        @JsonProperty("currencyName")
        private String currencyName;
    }
}
