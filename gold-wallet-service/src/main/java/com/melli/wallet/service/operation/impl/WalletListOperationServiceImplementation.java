package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.response.panel.WalletAccountCurrencyListResponse;
import com.melli.wallet.domain.response.panel.WalletAccountTypeListResponse;
import com.melli.wallet.domain.response.panel.WalletLevelListResponse;
import com.melli.wallet.domain.response.panel.WalletTypeListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.helper.WalletMapperHelper;
import com.melli.wallet.service.operation.WalletListOperationService;
import com.melli.wallet.service.repository.WalletAccountCurrencyRepositoryService;
import com.melli.wallet.service.repository.WalletAccountTypeRepositoryService;
import com.melli.wallet.service.repository.WalletLevelRepositoryService;
import com.melli.wallet.service.repository.WalletTypeRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Class Name: WalletListOperationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class WalletListOperationServiceImplementation implements WalletListOperationService {

    private final WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService;
    private final WalletLevelRepositoryService walletLevelRepositoryService;
    private final WalletTypeRepositoryService walletTypeRepositoryService;
    private final WalletAccountTypeRepositoryService walletAccountTypeRepositoryService;
    private final WalletMapperHelper walletMapperHelper;

    @Override
    public WalletAccountCurrencyListResponse getWalletAccountCurrencyList() throws InternalServiceException {
        log.info("start getWalletAccountCurrencyList");
        
        WalletAccountCurrencyListResponse response = new WalletAccountCurrencyListResponse();
        response.setWalletAccountCurrencies(walletMapperHelper.mapToWalletAccountCurrencyObjectList(
                walletAccountCurrencyRepositoryService.getAll()
        ));
        
        log.info("finished getWalletAccountCurrencyList with {} items", response.getWalletAccountCurrencies().size());
        return response;
    }

    @Override
    public WalletLevelListResponse getWalletLevelList() throws InternalServiceException {
        log.info("start getWalletLevelList");
        
        WalletLevelListResponse response = new WalletLevelListResponse();
        response.setWalletLevels(walletMapperHelper.mapToWalletLevelObjectList(
                walletLevelRepositoryService.getAll()
        ));
        
        log.info("finished getWalletLevelList with {} items", response.getWalletLevels().size());
        return response;
    }

    @Override
    public WalletTypeListResponse getWalletTypeList() throws InternalServiceException {
        log.info("start getWalletTypeList");
        
        WalletTypeListResponse response = new WalletTypeListResponse();
        response.setWalletTypes(walletMapperHelper.mapToWalletTypeObjectList(
                walletTypeRepositoryService.getAll()
        ));
        
        log.info("finished getWalletTypeList with {} items", response.getWalletTypes().size());
        return response;
    }

    @Override
    public WalletAccountTypeListResponse getWalletAccountTypeList() throws InternalServiceException {
        log.info("start getWalletAccountTypeList");
        
        WalletAccountTypeListResponse response = new WalletAccountTypeListResponse();
        response.setWalletAccountTypes(walletMapperHelper.mapToWalletAccountTypeObjectList(
                walletAccountTypeRepositoryService.getAll()
        ));
        
        log.info("finished getWalletAccountTypeList with {} items", response.getWalletAccountTypes().size());
        return response;
    }
}
