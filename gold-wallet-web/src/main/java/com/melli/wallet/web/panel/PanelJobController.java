package com.melli.wallet.web.panel;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.domain.enumaration.ResourceDefinition;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.JobService;
import com.melli.wallet.service.operation.SettlementService;
import com.melli.wallet.web.WebController;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Class Name: PanelJobController
 * Author: AI Assistant
 * Date: 1/26/2025
 * Description: Panel controller for job execution operations
 */
@RestController
@RequestMapping("/api/v1/panel/job")
@RequiredArgsConstructor
@Validated
@Log4j2
public class PanelJobController extends WebController {

    private final RequestContext requestContext;
    private final JobService jobService;
    private final SettlementService settlementService;

    @Timed(description = "Time taken to execute buy statistics job")
    @PostMapping(path = "/buy-statistics", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "اجرای جاب آمار خرید", description =
            """
                    {
                      "fromDate": "1403/01/01",  // Optional, Persian date format
                      "toDate": "1403/01/31"     // Optional, Persian date format
                    }
            """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STAT_MANAGE_AUTH + "')")
    @LogExecutionTime("Execute buy statistics job")
    public ResponseEntity<BaseResponse<String>> executeBuyStatisticsJob(
            @RequestParam String fromDate,
            @RequestParam String toDate) throws InternalServiceException {
        log.info("start execute buy statistics job by operator ({}), ip ({}), fromDate ({}), toDate ({})",
                requestContext.getChannelEntity().getUsername(), requestContext.getClientIp(), fromDate, toDate);
        jobService.generateBuyStatistics(fromDate, toDate, requestContext.getChannelEntity().getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "Buy statistics job executed successfully"));
    }

    @Timed(description = "Time taken to execute sell statistics job")
    @PostMapping(path = "/sell-statistics", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "اجرای جاب آمار فروش", description =
            """
                    {
                      "fromDate": "1403/01/01",  // Optional, Persian date format
                      "toDate": "1403/01/31"     // Optional, Persian date format
                    }
            """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STAT_MANAGE_AUTH + "')")
    @LogExecutionTime("Execute sell statistics job")
    public ResponseEntity<BaseResponse<String>> executeSellStatisticsJob(
            @RequestParam String fromDate,
            @RequestParam String toDate) throws InternalServiceException {

        log.info("start execute sell statistics job by operator ({}), ip ({}), fromDate ({}), toDate ({})",
                requestContext.getChannelEntity().getUsername(), requestContext.getClientIp(), fromDate, toDate);
        jobService.generateSellStatistics(fromDate, toDate, requestContext.getChannelEntity().getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "Sell statistics job executed successfully"));
    }

    @Timed(description = "Time taken to execute wallet statistics job")
    @PostMapping(path = "/wallet-statistics", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "اجرای جاب آمار کیف پول", description =
            """
                    {
                      "fromDate": "1403/01/01",  // Optional, Persian date format
                      "toDate": "1403/01/31"     // Optional, Persian date format
                    }
            """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STAT_MANAGE_AUTH + "')")
    @LogExecutionTime("Execute wallet statistics job")
    public ResponseEntity<BaseResponse<String>> executeWalletStatisticsJob(
            @RequestParam String fromDate,
            @RequestParam String toDate) throws InternalServiceException {
        log.info("start execute wallet statistics job by operator ({}), ip ({}), fromDate ({}), toDate ({})",
                requestContext.getChannelEntity().getUsername(), requestContext.getClientIp(), fromDate, toDate);
        jobService.generateWalletStatistics(fromDate, toDate, requestContext.getChannelEntity().getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "Wallet statistics job executed successfully"));
    }

    @Timed(description = "Time taken to execute person2person statistics job")
    @PostMapping(path = "/person2person-statistics", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "اجرای جاب آمار نقل و انتقال", description =
            """
                    {
                      "fromDate": "1403/01/01",  // Optional, Persian date format
                      "toDate": "1403/01/31"     // Optional, Persian date format
                    }
            """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STAT_MANAGE_AUTH + "')")
    @LogExecutionTime("Execute person2person statistics job")
    public ResponseEntity<BaseResponse<String>> executePerson2PersonStatisticsJob(
            @RequestParam String fromDate,
            @RequestParam String toDate) throws InternalServiceException {
        log.info("start execute person2person statistics job by operator ({}), ip ({}), fromDate ({}), toDate ({})",
                requestContext.getChannelEntity().getUsername(), requestContext.getClientIp(), fromDate, toDate);
        jobService.generatePerson2PersonStatistics(fromDate, toDate, requestContext.getChannelEntity().getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "Person2Person statistics job executed successfully"));
    }

    @Timed(description = "Time taken to execute physical cash out statistics job")
    @PostMapping(path = "/physical-cashout-statistics", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "اجرای جاب آمار برداشت فیزیکی", description =
            """
                    {
                      "fromDate": "1403/01/01",  // Optional, Persian date format
                      "toDate": "1403/01/31"     // Optional, Persian date format
                    }
            """)
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STAT_MANAGE_AUTH + "')")
    @LogExecutionTime("Execute physical cash out statistics job")
    public ResponseEntity<BaseResponse<String>> executePhysicalCashOutStatisticsJob(
            @RequestParam String fromDate,
            @RequestParam String toDate) throws InternalServiceException {
        log.info("start execute physical cash out statistics job by operator ({}), ip ({}), fromDate ({}), toDate ({})",
                requestContext.getChannelEntity().getUsername(), requestContext.getClientIp(), fromDate, toDate);
        jobService.generatePhysicalCashOutStatistics(fromDate, toDate, requestContext.getChannelEntity().getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "Physical cash out statistics job executed successfully"));
    }

    @Timed(description = "Time taken to execute batch settlement job")
    @PostMapping(path = "/batch-settlement", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "اجرای جاب تسویه دسته‌ای")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STAT_MANAGE_AUTH + "')")
    @LogExecutionTime("Execute batch settlement job")
    public ResponseEntity<BaseResponse<String>> executeBatchSettlementJob() throws InternalServiceException {
        log.info("start execute batch settlement job by operator ({}), ip ({})",
                requestContext.getChannelEntity().getUsername(), requestContext.getClientIp());
        jobService.batchSettlement();
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "Batch settlement job executed successfully"));
    }

    @Timed(description = "Time taken to execute batch settlement job")
    @PostMapping(path = "/settlement", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "اجرای  تسویه ")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.STAT_MANAGE_AUTH + "')")
    @LogExecutionTime("Execute batch settlement job")
    public ResponseEntity<BaseResponse<String>> executeSettlementById(@Valid @NumberValidation @RequestParam("id") long id) throws InternalServiceException {
        log.info("start execute settlement by operator ({}), ip ({}), id ({})",
                requestContext.getChannelEntity().getUsername(), requestContext.getClientIp(), id);
        settlementService.settlementById(id);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "settlement job successfully"));
    }


}

