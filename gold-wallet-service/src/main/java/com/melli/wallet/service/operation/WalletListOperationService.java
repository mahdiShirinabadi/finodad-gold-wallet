package com.melli.wallet.service.operation;

import com.melli.wallet.domain.response.panel.WalletAccountCurrencyListResponse;
import com.melli.wallet.domain.response.panel.WalletAccountTypeListResponse;
import com.melli.wallet.domain.response.panel.WalletLevelListResponse;
import com.melli.wallet.domain.response.panel.WalletTypeListResponse;
import com.melli.wallet.exception.InternalServiceException;

/**
 * Class Name: WalletListOperationService
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
public interface WalletListOperationService {

    WalletAccountCurrencyListResponse getWalletAccountCurrencyList() throws InternalServiceException;
    
    WalletLevelListResponse getWalletLevelList() throws InternalServiceException;
    
    WalletTypeListResponse getWalletTypeList() throws InternalServiceException;
    
    WalletAccountTypeListResponse getWalletAccountTypeList() throws InternalServiceException;
}
