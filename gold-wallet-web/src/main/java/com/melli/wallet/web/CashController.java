package com.melli.wallet.web;

import com.melli.wallet.domain.dto.CashOutObjectDTO;
import com.melli.wallet.domain.dto.ChargeObjectDTO;
import com.melli.wallet.domain.request.wallet.CashGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.CashInWalletRequestJson;
import com.melli.wallet.domain.request.wallet.CashOutWalletRequestJson;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.domain.response.cash.CashOutResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.*;
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
@RequestMapping("/api/v1/cash")
@Validated
@Log4j2
public class CashController extends WebController {

    private final RequestContext requestContext;
    private final CashInService cashInService;
    private final CashOutService cashOutService;
    private final SecurityService securityService;

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "/generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد شناسه یکتا")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_IN +"\")")
    public ResponseEntity<BaseResponse<UuidResponse>> generateUuid(@Valid @RequestBody CashGenerateUuidRequestJson requestJson) throws InternalServiceException {
        log.info("start call uuid nationalCode ===> {}", requestJson.getNationalCode());
        UuidResponse response = cashInService.generateUuid(requestContext.getChannelEntity(), requestJson.getNationalCode(), requestJson.getAmount(), requestJson.getAccountNumber());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    @Timed(description = "CashEndPoint.cashIn")
    @PostMapping(path = "/charge", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "افزایش موجودی کیف پول")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_IN +"\")")
    public ResponseEntity<BaseResponse<CashInResponse>> cashIn(@Valid @RequestBody CashInWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();

        securityService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());

        log.info("start call cashIn in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        CashInResponse cashInResponse = cashInService.charge(new ChargeObjectDTO(requestContext.getChannelEntity(), requestJson.getNationalCode(),  requestJson.getUniqueIdentifier(),requestJson.getAmount(), requestJson.getReferenceNumber(),
               requestJson.getAccountNumber(), requestJson.getAdditionalData(), requestContext.getClientIp(), requestJson.getCashInType()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }

    @Timed(description = "CashEndPoint.inquiry")
    @GetMapping(path = "/charge/inquiry", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "پیگیری افزایش موجودی کیف پول")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_IN +"\")")
    public ResponseEntity<BaseResponse<CashInTrackResponse>> inquiryCashIn(@Valid @RequestParam("uniqueIdentifier") String uniqueIdentifier) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call inquiry cashIn in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, uniqueIdentifier, channelIp);
        CashInTrackResponse cashInResponse = cashInService.inquiry(requestContext.getChannelEntity(), uniqueIdentifier, requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }


    @Timed(description = "CashEndPoint.cashOut")
    @PostMapping(path = "/cashOut", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "برداشت وجه")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_OUT +"\")")
    public ResponseEntity<BaseResponse<CashOutResponse>> cashOut(@Valid @RequestBody CashOutWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        securityService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());

        log.info("start call cashOut in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        CashOutResponse cashOutResponse = cashOutService.withdrawal(new CashOutObjectDTO(requestContext.getChannelEntity(), requestJson.getNationalCode(),
                requestJson.getUniqueIdentifier(),requestJson.getAmount(), requestJson.getIban(),
                requestJson.getAccountNumber(), requestJson.getAdditionalData(), requestContext.getClientIp()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashOutResponse));
    }

    @Timed(description = "CashEndPoint.cashOut.inquiry")
    @GetMapping(path = "/cashOut/inquiry", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "پیگیری برداشت وجه")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_OUT +"\")")
    public ResponseEntity<BaseResponse<CashInTrackResponse>> inquiryCashOut(@Valid @RequestParam("uniqueIdentifier") String uniqueIdentifier) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call inquiry cashOut in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, uniqueIdentifier, channelIp);
        CashInTrackResponse cashInResponse = cashInService.inquiry(requestContext.getChannelEntity(), uniqueIdentifier, requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }


}
