package com.melli.wallet.web.panel;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import com.melli.wallet.domain.enumaration.ResourceDefinition;
import com.melli.wallet.domain.master.entity.CollateralEntity;
import com.melli.wallet.domain.master.entity.CollateralWalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.persistence.CollateralWalletAccountCurrencyRepository;
import com.melli.wallet.domain.request.PanelBaseSearchJson;
import com.melli.wallet.domain.request.setup.PanelCollateralCreateRequestJson;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.collateral.CollateralListResponse;
import com.melli.wallet.domain.response.collateral.CollateralResponse;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.operation.CollateralOperationService;
import com.melli.wallet.service.operation.WalletOperationalService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.web.WebController;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Class Name: PanelLimitationController
 * Author: Mahdi Shirinabadi
 * Date: 7/21/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = {"/api/v1/panel/collateral"})
@Validated
@Log4j2
public class PanelCollateralController extends WebController {

    private final RequestContext requestContext;
    private final WalletRepositoryService walletRepositoryService;
    private final CollateralRepositoryService collateralRepositoryService;
    private final CollateralOperationService collateralOperationService;
    private final WalletOperationalService walletOperationalService;
    private final WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService;
    private final CollateralWalletAccountCurrencyRepository collateralWalletAccountCurrencyRepository;





    @Timed(description = "Time taken to update limitation general")
    @PostMapping(path = "/create", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "Update general limitation (value and pattern only)")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("Create collateral wallet")
    public ResponseEntity<BaseResponse<String>> createCollateralWallet(@Valid @RequestBody PanelCollateralCreateRequestJson requestJson) throws InternalServiceException {

        CreateWalletResponse createWalletResponse = walletOperationalService.createWallet(requestContext.getChannelEntity(), requestJson.getMobileNumber(), requestJson.getEconomicCode(), WalletTypeRepositoryService.COLLATERAL,
                List.of(WalletAccountCurrencyRepositoryService.GOLD, WalletAccountCurrencyRepositoryService.RIAL),
                List.of(WalletAccountTypeRepositoryService.NORMAL));
        CollateralEntity collateralEntity = new CollateralEntity();
        collateralEntity.setName(requestJson.getName());
        collateralEntity.setDescription("create collateral");
        collateralEntity.setMobile(requestJson.getMobileNumber());
        collateralEntity.setEconomicalCode(requestJson.getEconomicCode());
        collateralEntity.setLogo("");
        collateralEntity.setWalletEntity(walletRepositoryService.findById(Long.parseLong(createWalletResponse.getWalletId())));
        collateralEntity.setIban(requestJson.getIban());
        collateralEntity.setStatus(1);
        collateralEntity.setCreatedBy(requestContext.getChannelEntity().getUsername());
        collateralEntity.setCreatedAt(new Date());
        collateralRepositoryService.save(collateralEntity);

        CollateralWalletAccountCurrencyEntity goldEntity = new CollateralWalletAccountCurrencyEntity();
        goldEntity.setCreatedBy(requestContext.getChannelEntity().getUsername());
        goldEntity.setCreatedAt(new Date());
        goldEntity.setCollateralEntity(collateralEntity);
        goldEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.GOLD));
        collateralWalletAccountCurrencyRepository.save(goldEntity);

        CollateralWalletAccountCurrencyEntity rialEntity = new CollateralWalletAccountCurrencyEntity();
        rialEntity.setCreatedBy(requestContext.getChannelEntity().getUsername());
        rialEntity.setCreatedAt(new Date());
        rialEntity.setCollateralEntity(collateralEntity);
        rialEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.RIAL));
        collateralWalletAccountCurrencyRepository.save(rialEntity);

        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, String.valueOf(collateralEntity.getId())));
    }


    @Timed(description = "Time taken to list collateral")
    @GetMapping(path = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "لیست شرکتهای خواهان وثیقه ")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("Get collateral list")
    public ResponseEntity<BaseResponse<CollateralResponse>> list(@Valid @StringValidation @RequestParam("currency") String currency) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call getMerchant in username ===> {}, currency ===> {}, from ip ===> {}", username, currency, channelIp);
        CollateralResponse response = collateralRepositoryService.get(requestContext.getChannelEntity(), currency);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @Timed(description = "Time taken to report collateral")
    @PostMapping(path = "/create/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "گزارش تراکنش", description =
            """
                            {
                              "map": {
                                "collateralId": "1",
                                "nationalCode": "0000000000",
                                "fromTime":"1403/01/01",
                                "toTime":"1403/09/01",
                                "page": "0",
                                "size": "10",
                                "orderBy": "id",
                                "sort": "asc"
                              }
                            }
                     """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("Generate transaction report")
    public ResponseEntity<BaseResponse<CollateralListResponse>> createList(@RequestBody @Validated PanelBaseSearchJson request) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call list response in username ===> {},from ip ===> {}", username, channelIp);
        CollateralListResponse response = collateralOperationService.list(requestContext.getChannelEntity(), request.getMap());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }




    @Timed(description = "Time taken to balance collateral")
    @GetMapping(path = "/balance", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "گزارش تراکنش", description =
            """
                            {
                              "map": {
                                "collateralId": "1",
                                "fromTime":"1403/01/01",
                                "toTime":"1403/09/01",
                                "page": "0",
                                "size": "10",
                                "orderBy": "id",
                                "sort": "asc"
                              }
                            }
                     """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("Get merchant balance")
    public ResponseEntity<BaseResponse<WalletBalanceResponse>> getBalance(@Valid @NumberValidation @RequestParam("collateralId") String merchantId) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call balance getMerchant in username ===> {}, merchantId ===> {}, from ip ===> {}", username, merchantId, channelIp);
        WalletBalanceResponse response = collateralOperationService.getBalance(requestContext.getChannelEntity(), merchantId);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }


    @Timed(description = "Time taken to report collateral")
    @PostMapping(path = "/report", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "گزارش تراکنش", description =
            """
                            {
                              "map": {
                                "collateralId": "1",
                                "fromTime":"1403/01/01",
                                "toTime":"1403/09/01",
                                "page": "0",
                                "size": "10",
                                "orderBy": "id",
                                "sort": "asc"
                              }
                            }
                     """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("Generate transaction report")
    public ResponseEntity<BaseResponse<ReportTransactionResponse>> report(@RequestBody @Validated PanelBaseSearchJson request) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call report transaction in username ===> {},from ip ===> {}", username, channelIp);
        ReportTransactionResponse response = collateralOperationService.report(requestContext.getChannelEntity(), request.getMap());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }






}
