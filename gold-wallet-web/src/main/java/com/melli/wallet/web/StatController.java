package com.melli.wallet.web;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.enumaration.ResourceDefinition;
import com.melli.wallet.domain.request.PanelBaseSearchJson;
import com.melli.wallet.domain.request.stat.StatRegenerateRequest;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.stat.StatBuyListResponse;
import com.melli.wallet.domain.response.stat.StatSellListResponse;
import com.melli.wallet.domain.response.stat.StatWalletListResponse;
import com.melli.wallet.domain.response.stat.StatPerson2PersonListResponse;
import com.melli.wallet.domain.response.stat.StatPhysicalCashOutListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.JobService;
import com.melli.wallet.service.operation.StatOperationService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Class Name: StatController
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Panel controller for statistics management operations
 */
@RestController
@RequestMapping("/api/v1/panel/stat")
@RequiredArgsConstructor
@Validated
@Log4j2
@Tag(name = "Statistics", description = "Statistics management endpoints")
public class StatController {

    private final RequestContext requestContext;
    private final StatOperationService statOperationService;
    private final JobService jobService;

    @Timed(description = "Time taken to get buy statistics list")
    @PostMapping(path = "/buy/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "لیست آمار خرید", description =
            """
                            {
                              "map": {
                                "page": "0",
                                "size": "10",
                                "orderBy": "id",
                                "sort": "desc",
                                "channelId": "1",
                                "currencyId": "1",
                                "merchantId": "1",
                                "result": "SUCCESS",
                                "persianCalcDate": "1403/01/01",
                                "fromDate": "2024-01-01",
                                "toDate": "2024-12-31",
                                "minAmount": "1000",
                                "maxAmount": "10000",
                                "minCount": "1",
                                "maxCount": "100"
                              }
                            }
                     """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STAT_LIST_AUTH + "')")
    @LogExecutionTime("Get buy statistics list")
    public ResponseEntity<BaseResponse<StatBuyListResponse>> getBuyStatistics(@RequestBody @Validated PanelBaseSearchJson request) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call buy statistics list in username ===> {}, from ip ===> {}", username, channelIp);
        StatBuyListResponse response = statOperationService.getBuyStatistics(requestContext.getChannelEntity(), request.getMap());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @Timed(description = "Time taken to get sell statistics list")
    @PostMapping(path = "/sell/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "لیست آمار فروش", description =
            """
                            {
                              "map": {
                                "page": "0",
                                "size": "10",
                                "orderBy": "id",
                                "sort": "desc",
                                "channelId": "1",
                                "currencyId": "1",
                                "merchantId": "1",
                                "result": "SUCCESS",
                                "persianCalcDate": "1403/01/01",
                                "fromDate": "2024-01-01",
                                "toDate": "2024-12-31",
                                "minAmount": "1000",
                                "maxAmount": "10000",
                                "minCount": "1",
                                "maxCount": "100"
                              }
                            }
                     """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STAT_LIST_AUTH + "')")
    @LogExecutionTime("Get sell statistics list")
    public ResponseEntity<BaseResponse<StatSellListResponse>> getSellStatistics(@RequestBody @Validated PanelBaseSearchJson request) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call sell statistics list in username ===> {}, from ip ===> {}", username, channelIp);
        StatSellListResponse response = statOperationService.getSellStatistics(requestContext.getChannelEntity(), request.getMap());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @Timed(description = "Time taken to get wallet statistics list")
    @PostMapping(path = "/wallet/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "لیست آمار کیف پول", description =
            """
                            {
                              "map": {
                                "page": "0",
                                "size": "10",
                                "orderBy": "id",
                                "sort": "desc",
                                "channelId": "1",
                                "persianCalcDate": "1403/01/01",
                                "fromDate": "2024-01-01",
                                "toDate": "2024-12-31",
                                "minCount": "1",
                                "maxCount": "100"
                              }
                            }
                     """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STAT_LIST_AUTH + "')")
    @LogExecutionTime("Get wallet statistics list")
    public ResponseEntity<BaseResponse<StatWalletListResponse>> getWalletStatistics(@RequestBody @Validated PanelBaseSearchJson request) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call wallet statistics list in username ===> {}, from ip ===> {}", username, channelIp);
        StatWalletListResponse response = statOperationService.getWalletStatistics(requestContext.getChannelEntity(), request.getMap());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @Timed(description = "Time taken to get person2person statistics list")
    @PostMapping(path = "/person2person/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "لیست آمار انتقال شخص به شخص", description =
            """
                            {
                              "map": {
                                "page": "0",
                                "size": "10",
                                "orderBy": "id",
                                "sort": "desc",
                                "channelId": "1",
                                "currencyId": "1",
                                "result": "SUCCESS",
                                "persianCalcDate": "1403/01/01",
                                "fromDate": "2024-01-01",
                                "toDate": "2024-12-31",
                                "minAmount": "1000",
                                "maxAmount": "10000",
                                "minCount": "1",
                                "maxCount": "100"
                              }
                            }
                     """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STAT_LIST_AUTH + "')")
    @LogExecutionTime("Get person2person statistics list")
    public ResponseEntity<BaseResponse<StatPerson2PersonListResponse>> getPerson2PersonStatistics(@RequestBody @Validated PanelBaseSearchJson request) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call person2person statistics list in username ===> {}, from ip ===> {}", username, channelIp);
        StatPerson2PersonListResponse response = statOperationService.getPerson2PersonStatistics(requestContext.getChannelEntity(), request.getMap());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @Timed(description = "Time taken to get physical cash out statistics list")
    @PostMapping(path = "/physical-cash-out/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "لیست آمار برداشت نقدی", description =
            """
                            {
                              "map": {
                                "page": "0",
                                "size": "10",
                                "orderBy": "id",
                                "sort": "desc",
                                "channelId": "1",
                                "currencyId": "1",
                                "result": "SUCCESS",
                                "persianCalcDate": "1403/01/01",
                                "fromDate": "2024-01-01",
                                "toDate": "2024-12-31",
                                "minAmount": "1000",
                                "maxAmount": "10000",
                                "minCount": "1",
                                "maxCount": "100"
                              }
                            }
                     """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STAT_LIST_AUTH + "')")
    @LogExecutionTime("Get physical cash out statistics list")
    public ResponseEntity<BaseResponse<StatPhysicalCashOutListResponse>> getPhysicalCashOutStatistics(@RequestBody @Validated PanelBaseSearchJson request) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call physical cash out statistics list in username ===> {}, from ip ===> {}", username, channelIp);
        StatPhysicalCashOutListResponse response = statOperationService.getPhysicalCashOutStatistics(requestContext.getChannelEntity(), request.getMap());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @Timed(description = "Time taken to regenerate statistics")
    @PostMapping(path = "/regenerate", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "بازتولید آمار")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STAT_MANAGE_AUTH + "')")
    @LogExecutionTime("Regenerate statistics")
    public ResponseEntity<BaseResponse<Object>> regenerateStatistics(@Valid @RequestBody StatRegenerateRequest requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call regenerate statistics in username ===> {}, statType ===> {}, fromDate ===> {}, toDate ===> {}, from ip ===> {}", 
                username, requestJson.getStatType(), requestJson.getFromDate(), requestJson.getToDate(), channelIp);
        
        try {
            switch (requestJson.getStatType().toLowerCase()) {
                case "buy":
                    jobService.generateBuyStatistics(requestJson.getFromDate(), requestJson.getToDate(), "API");
                    break;
                case "sell":
                    jobService.generateSellStatistics(requestJson.getFromDate(), requestJson.getToDate(), "API");
                    break;
                case "wallet":
                    jobService.generateWalletStatistics(requestJson.getFromDate(), requestJson.getToDate(), "API");
                    break;
                case "person2person":
                    jobService.generatePerson2PersonStatistics(requestJson.getFromDate(), requestJson.getToDate(), "API");
                    break;
                case "physical-cash-out":
                    jobService.generatePhysicalCashOutStatistics(requestJson.getFromDate(), requestJson.getToDate(), "API");
                    break;
                default:
                    throw new InternalServiceException("Invalid stat type. Valid types: buy, sell, wallet, person2person, physical-cash-out", StatusRepositoryService.REQUEST_TYPE_NOT_FOUND, HttpStatus.OK);
            }
            
            log.info("finish regenerate statistics for type: {}", requestJson.getStatType());
            return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
            
        } catch (InternalServiceException e) {
            log.error("Error regenerating statistics: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error regenerating statistics: {}", e.getMessage(), e);
            throw new InternalServiceException("Unexpected error occurred", StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
        }
    }
}