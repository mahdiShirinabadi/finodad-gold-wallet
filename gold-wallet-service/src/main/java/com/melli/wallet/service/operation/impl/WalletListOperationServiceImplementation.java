package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.response.panel.CustomerListResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountTypeObject;
import com.melli.wallet.domain.response.wallet.WalletAccountCurrencyObject;
import com.melli.wallet.domain.response.panel.WalletAccountCurrencyListResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.domain.response.panel.WalletAccountTypeListResponse;
import com.melli.wallet.domain.response.panel.WalletLevelListResponse;
import com.melli.wallet.domain.response.panel.WalletTypeListResponse;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.slave.entity.ReportWalletEntity;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.helper.WalletMapperHelper;
import com.melli.wallet.service.operation.WalletListOperationService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.util.date.DateUtils;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final WalletRepositoryService walletRepositoryService;
    private final WalletAccountRepositoryService walletAccountRepositoryService;
    private final WalletMapperHelper walletMapperHelper;
    private final Helper helper;
    private final SettingGeneralRepositoryService settingService;

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


    @Override
    public CustomerListResponse getCustomerListEfficient(Map<String, String> mapParameter) throws InternalServiceException {
        log.info("start getCustomerListEfficient with mapParameter: {}", mapParameter);
        
        if (mapParameter == null) {
            mapParameter = new HashMap<>();
        }

        Pageable pageable = helper.getPageableConfig(
                settingService,
                Integer.parseInt(Optional.ofNullable(mapParameter.get("page")).orElse("0")),
                Integer.parseInt(Optional.ofNullable(mapParameter.get("size")).orElse("10")));
        
        // Extract search parameters
        String nationalCode = mapParameter.get("nationalCode");
        String mobile = mapParameter.get("mobile");
        String fromTime = mapParameter.get("fromTime");
        String toTime = mapParameter.get("toTime");
        
        // Step 1: Get paginated wallets with filters
        Page<ReportWalletEntity> walletPage = walletRepositoryService.findWalletsWithFiltersAndPagination(
                "ACTIVE", nationalCode, mobile, fromTime, toTime, pageable);
        
        List<ReportWalletEntity> wallets = walletPage.getContent();
        
        if (wallets.isEmpty()) {
            // Return empty response
            CustomerListResponse response = new CustomerListResponse();
            response.setCustomers(new ArrayList<>());
            response.setTotalElements(0);
            response.setTotalPages(0);
            response.setCurrentPage(pageable.getPageNumber());
            response.setPageSize(pageable.getPageSize());
            return response;
        }
        
        // Step 2: Extract wallet IDs for batch account fetching
        List<Long> walletIds = wallets.stream()
                .map(ReportWalletEntity::getId)
                .toList();

        // Step 3: Fetch all accounts for these wallets in a single query
        List<Object[]> walletAccountDetails = walletAccountRepositoryService.findAccountDetailsByWalletIds(walletIds);
        
        // Step 4: Group account details by wallet ID
        Map<Long, List<Object[]>> accountsByWalletId = walletAccountDetails.stream()
                .collect(Collectors.groupingBy(account -> (Long) account[4])); // walletId is at index 4
        
        // Step 5: Create customer objects with nested accounts
        List<CustomerListResponse.CustomerObject> customers = new ArrayList<>();
        
        for (ReportWalletEntity wallet : wallets) {
            List<Object[]> walletAccounts = accountsByWalletId.getOrDefault(wallet.getId(), new ArrayList<>());
            
            // Create customer object
            CustomerListResponse.CustomerObject customer = new CustomerListResponse.CustomerObject();
            
            // Map wallet to CreateWalletResponse
            CustomerListResponse.CustomerAccountObject walletResponse = new CustomerListResponse.CustomerAccountObject();
            walletResponse.setWalletId(String.valueOf(wallet.getId()));
            walletResponse.setMobile(wallet.getMobile());
            walletResponse.setNationalCode(wallet.getNationalCode());
            walletResponse.setStatus(wallet.getStatus().name());
            walletResponse.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, wallet.getCreatedAt(), "yyyy/MM/dd HH:mm:ss", false));
            
            // Map wallet accounts from Object[] array
            List<CustomerListResponse.WalletAccountNameObject> walletAccountObjects = walletAccounts.stream()
                    .map(account -> {
                        CustomerListResponse.WalletAccountNameObject accountObject = new CustomerListResponse.WalletAccountNameObject();
                        accountObject.setAccountNumber((String) account[1]); // accountNumber
                        accountObject.setStatus((String) account[2]); // accountStatus
                        accountObject.setBalance(account[3] != null ? account[3].toString() : "0"); // balance
                        accountObject.setWalletAccountTypeName((String) account[5]);
                        accountObject.setWalletAccountCurrencyName((String) account[6]);
                        return accountObject;
                    })
                    .toList();
            walletResponse.setWalletAccountObjectList(walletAccountObjects);
            customer.setWallet(walletResponse);
            customers.add(customer);
        }
        
        CustomerListResponse response = new CustomerListResponse();
        response.setCustomers(customers);
        response.setTotalElements(walletPage.getTotalElements());
        response.setTotalPages(walletPage.getTotalPages());
        response.setCurrentPage(walletPage.getNumber());
        response.setPageSize(walletPage.getSize());
        
        log.info("finished getCustomerListEfficient with {} customers", customers.size());
        return response;
    }
}
