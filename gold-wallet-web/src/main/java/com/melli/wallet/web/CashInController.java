package com.melli.wallet.web;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.dto.ChargeObjectDTO;
import com.melli.wallet.domain.request.wallet.cash.CashGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.cash.CashInWalletRequestJson;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.operation.CashInOperationService;
import com.melli.wallet.domain.enumaration.ResourceDefinition;
import com.melli.wallet.service.operation.SecurityOperationService;
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

/**
 * Class Name: CashEndPoint
 * Author: Mahdi Shirinabadi
 * Date: 1/20/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cashIn")
@Validated
@Log4j2
public class CashInController extends WebController {

    private final RequestContext requestContext;
    private final CashInOperationService cashInOperationService;
    private final SecurityOperationService securityOperationService;

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "/generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد شناسه یکتا")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.CASH_IN_AUTH + "')")
    @LogExecutionTime("Generate cash in UUID")
    public ResponseEntity<BaseResponse<UuidResponse>> generateUuid(@Valid @RequestBody CashGenerateUuidRequestJson requestJson) throws InternalServiceException {
        log.info("start call cashIn uuid nationalCode ===> {}", requestJson.getNationalCode());
        UuidResponse response = cashInOperationService.generateUuid(requestContext.getChannelEntity(), requestJson.getNationalCode(), requestJson.getAmount(), requestJson.getAccountNumber());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    @Timed(description = "CashEndPoint.cashIn")
    @PostMapping(path = "/charge", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "افزایش موجودی کیف پول")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.CASH_IN_AUTH + "')")
    @LogExecutionTime("Cash in charge")
    public ResponseEntity<BaseResponse<CashInResponse>> cashIn(@Valid @RequestBody CashInWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();

        securityOperationService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());

        log.info("start call cashIn in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        CashInResponse cashInResponse = cashInOperationService.charge(new ChargeObjectDTO(requestContext.getChannelEntity(), requestJson.getNationalCode(),  requestJson.getUniqueIdentifier(),requestJson.getAmount(), requestJson.getReferenceNumber(),
               requestJson.getAccountNumber(), requestJson.getAdditionalData(), requestContext.getClientIp(), requestJson.getCashInType()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }

    @Timed(description = "CashEndPoint.inquiry")
    @GetMapping(path = "/inquiry", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "پیگیری افزایش موجودی کیف پول")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.CASH_IN_AUTH + "')")
    @LogExecutionTime("Cash in inquiry")
    public ResponseEntity<BaseResponse<CashInTrackResponse>> inquiryCashIn(@Valid @RequestParam("uniqueIdentifier") String uniqueIdentifier) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call inquiry cashIn in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, uniqueIdentifier, channelIp);
        CashInTrackResponse cashInResponse = cashInOperationService.inquiry(requestContext.getChannelEntity(), uniqueIdentifier, requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }
}
