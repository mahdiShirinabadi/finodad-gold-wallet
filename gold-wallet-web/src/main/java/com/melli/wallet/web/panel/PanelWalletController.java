package com.melli.wallet.web.panel;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.panel.WalletAccountCurrencyListResponse;
import com.melli.wallet.domain.response.panel.WalletAccountTypeListResponse;
import com.melli.wallet.domain.response.panel.WalletLevelListResponse;
import com.melli.wallet.domain.response.panel.WalletTypeListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.WalletListOperationService;
import com.melli.wallet.service.repository.ResourceRepositoryService;
import com.melli.wallet.service.repository.ResourceDefinition;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class Name: PanelWalletController
 * Author: Mahdi Shirinabadi
 * Date: 8/20/2025
 */
@RestController
@RequestMapping("/api/v1/panel/wallet")
@RequiredArgsConstructor
@Log4j2
public class PanelWalletController {

    private final WalletListOperationService walletListOperationService;

    @Timed(description = "Time taken to get wallet account currency list")
    @GetMapping(path = "/accountCurrency/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "لیست ارزهای حساب کیف پول")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.LIMITATION_MANAGE_AUTH + "')")
    @LogExecutionTime("Get wallet account currency list")
    public ResponseEntity<BaseResponse<WalletAccountCurrencyListResponse>> getWalletAccountCurrencyList() throws InternalServiceException {
        log.info("start getWalletAccountCurrencyList");
        WalletAccountCurrencyListResponse response = walletListOperationService.getWalletAccountCurrencyList();
        return ResponseEntity.ok(new BaseResponse<>(true, response));
    }

    @Timed(description = "Time taken to get wallet level list")
    @GetMapping(path = "/level/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "لیست سطوح کیف پول")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.LIMITATION_MANAGE_AUTH + "')")
    @LogExecutionTime("Get wallet level list")
    public ResponseEntity<BaseResponse<WalletLevelListResponse>> getWalletLevelList() throws InternalServiceException {
        log.info("start getWalletLevelList");
        WalletLevelListResponse response = walletListOperationService.getWalletLevelList();
        return ResponseEntity.ok(new BaseResponse<>(true, response));
    }

    @Timed(description = "Time taken to get wallet type list")
    @GetMapping(path = "/type/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "لیست انواع کیف پول")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.LIMITATION_MANAGE_AUTH + "')")
    @LogExecutionTime("Get wallet type list")
    public ResponseEntity<BaseResponse<WalletTypeListResponse>> getWalletTypeList() throws InternalServiceException {
        log.info("start getWalletTypeList");
        WalletTypeListResponse response = walletListOperationService.getWalletTypeList();
        return ResponseEntity.ok(new BaseResponse<>(true, response));
    }

    @Timed(description = "Time taken to get wallet account type list")
    @GetMapping(path = "/accountType/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "لیست انواع حساب کیف پول")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.LIMITATION_MANAGE_AUTH + "')")
    @LogExecutionTime("Get wallet account type list")
    public ResponseEntity<BaseResponse<WalletAccountTypeListResponse>> getWalletAccountTypeList() throws InternalServiceException {
        log.info("start getWalletAccountTypeList");
        WalletAccountTypeListResponse response = walletListOperationService.getWalletAccountTypeList();
        return ResponseEntity.ok(new BaseResponse<>(true, response));
    }

    @Timed(description = "Time taken to get wallet account type list")
    @GetMapping(path = "/customer/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "لیست انواع حساب کیف پول", description =
            """
                           {
                             "parameterMap": {
                               "id": "44",
                               "nationalCode": "1234567890",
                               "fromTime":"1403/01/01",
                               "toTime":"1403/09/01",
                               "sejamMember":"false"
                               "page": "0",
                               "size": "10",
                               "orderBy": "id",
                               "sort": "asc"
                             }
                           }
                    """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.LIMITATION_MANAGE_AUTH + "')")
    @LogExecutionTime("Get customer account type list")
    public ResponseEntity<BaseResponse<WalletAccountTypeListResponse>> getCutomerList() throws InternalServiceException {
        log.info("start getWalletAccountTypeList");
        WalletAccountTypeListResponse response = walletListOperationService.getWalletAccountTypeList();
        return ResponseEntity.ok(new BaseResponse<>(true, response));
    }
}
