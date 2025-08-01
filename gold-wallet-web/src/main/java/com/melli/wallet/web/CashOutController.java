package com.melli.wallet.web;

import com.melli.wallet.domain.dto.CashOutObjectDTO;
import com.melli.wallet.domain.request.wallet.CashGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.CashOutWalletRequestJson;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.cash.CashOutResponse;
import com.melli.wallet.domain.response.cash.CashOutTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.CashOutService;
import com.melli.wallet.service.ResourceService;
import com.melli.wallet.service.SecurityService;
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
@RequestMapping("/api/v1/cashOut")
@Validated
@Log4j2
public class CashOutController extends WebController {

    private final RequestContext requestContext;
    private final CashOutService cashOutService;
    private final SecurityService securityService;

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "/generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد شناسه یکتا")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_OUT +"\")")
    public ResponseEntity<BaseResponse<UuidResponse>> cashOutGenerateUuid(@Valid @RequestBody CashGenerateUuidRequestJson requestJson) throws InternalServiceException {
        log.info("start call uuid nationalCode ===> {}", requestJson.getNationalCode());
        UuidResponse response = cashOutService.generateUuid(requestContext.getChannelEntity(), requestJson.getNationalCode(), requestJson.getAmount(), requestJson.getAccountNumber());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }


    @Timed(description = "CashEndPoint.cashOut")
    @PostMapping(path = "/withdraw", produces = {MediaType.APPLICATION_JSON_VALUE})
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
    @GetMapping(path = "/inquiry", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "پیگیری برداشت وجه")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_OUT +"\")")
    public ResponseEntity<BaseResponse<CashOutTrackResponse>> inquiryCashOut(@Valid @RequestParam("uniqueIdentifier") String uniqueIdentifier) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call inquiry cashOut in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, uniqueIdentifier, channelIp);
        CashOutTrackResponse response = cashOutService.inquiry(requestContext.getChannelEntity(), uniqueIdentifier, requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }


}
