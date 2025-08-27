package com.melli.wallet.domain.response.panel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melli.wallet.NamingProperty;
import com.melli.wallet.domain.response.wallet.WalletAccountCurrencyObject;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountTypeObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * Class Name: CustomerListResponse
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerListResponse {
    
    private List<CustomerObject> customers;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerObject {
        private CustomerAccountObject wallet;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerAccountObject {
        @Schema(name = NamingProperty.ID)
        @JsonProperty(NamingProperty.ID)
        private String walletId;

        @Schema(name = NamingProperty.MOBILE_NUMBER)
        @JsonProperty(NamingProperty.MOBILE_NUMBER)
        private String mobile;

        @Schema(name = NamingProperty.NATIONAL_CODE)
        @JsonProperty(NamingProperty.NATIONAL_CODE)
        private String nationalCode;

        @Schema(name = NamingProperty.STATUS)
        @JsonProperty(NamingProperty.STATUS)
        private String status;

        @Schema(name = NamingProperty.CREATE_TIME)
        @JsonProperty(NamingProperty.CREATE_TIME)
        private String createTime;

        @Schema(name = NamingProperty.WALLET_ACCOUNT_OBJECT_LIST)
        @JsonProperty(NamingProperty.WALLET_ACCOUNT_OBJECT_LIST)
        private List<WalletAccountNameObject> walletAccountObjectList;
    }

    @Setter
    @Getter
    @ToString
    public static class WalletAccountNameObject {

        @Schema(name = NamingProperty.WALLET_ACCOUNT_TYPE_NAME)
        @JsonProperty(NamingProperty.WALLET_ACCOUNT_TYPE_NAME)
        private String walletAccountTypeName;

        @Schema(name = NamingProperty.WALLET_ACCOUNT_CURRENCY_NAME)
        @JsonProperty(NamingProperty.WALLET_ACCOUNT_CURRENCY_NAME)
        private String walletAccountCurrencyName;

        @Schema(name = NamingProperty.WALLET_ACCOUNT_NUMBER)
        @JsonProperty(NamingProperty.WALLET_ACCOUNT_NUMBER)
        private String accountNumber;

        @Schema(name = NamingProperty.BALANCE)
        @JsonProperty(NamingProperty.BALANCE)
        private String balance;

        @Schema(name = NamingProperty.STATUS)
        @JsonProperty(NamingProperty.STATUS)
        private String status;
    }
}
