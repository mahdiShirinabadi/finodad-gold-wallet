package com.melli.wallet.web;

import com.melli.wallet.domain.request.wallet.CashInGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.CashInWalletRequestJson;
import com.melli.wallet.domain.request.wallet.GenerateUuidRequestJson;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.*;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/v1/cash")
@Validated
@Log4j2
public class CashController extends WebController {

    private final RequestContext requestContext;
    private final CashService cashService;

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد شناسه یکتا")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_IN +"\")")
    public ResponseEntity<BaseResponse<UuidResponse>> generateUuid(@RequestBody CashInGenerateUuidRequestJson requestJson) throws InternalServiceException {
        log.info("start call uuid nationalCode ===> {}", requestJson.getNationalCode());
        UuidResponse response = cashService.generateUuid(requestContext.getChannelEntity(), requestJson.getNationalCode(), requestJson.getAmount(), requestJson.getAccountNumber());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    @Timed(description = "CashEndPoint.cashIn")
    @PostMapping(path = "/cashIn", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "افزایش موجودی کیف پول")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_IN +"\")")
    public ResponseEntity<BaseResponse<CashInResponse>> cashIn(@RequestBody CashInWalletRequestJson cashInWalletRequestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call cashIn in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, cashInWalletRequestJson.getNationalCode(), channelIp);
        CashInResponse cashInResponse = cashService.cashIn(requestContext.getChannelEntity(), cashInWalletRequestJson.getNationalCode(),  cashInWalletRequestJson.getUniqueIdentifier(),cashInWalletRequestJson.getAmount(), cashInWalletRequestJson.getReferenceNumber(),
                cashInWalletRequestJson.getSign(), cashInWalletRequestJson.getDataString(), cashInWalletRequestJson.getAccountNumber(), cashInWalletRequestJson.getAdditionalData(), requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }

    @Timed(description = "CashEndPoint.inquiry")
    @GetMapping(path = "/inquiry/{uniqueIdentifier}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "پیگیری افزایش موجودی کیف پول")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_IN +"\")")
    public ResponseEntity<BaseResponse<CashInTrackResponse>> inquiryCashIn(@PathVariable("uniqueIdentifier") String uniqueIdentifier) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call inquiry cashIn in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, uniqueIdentifier, channelIp);
        CashInTrackResponse cashInResponse = cashService.cashInTrack(requestContext.getChannelEntity(), uniqueIdentifier, requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }



}
