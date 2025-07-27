package com.melli.wallet.web;

import com.melli.wallet.domain.dto.PhysicalCashOutObjectDTO;
import com.melli.wallet.domain.request.wallet.PhysicalCashGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.PhysicalCashOutWalletRequestJson;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.cash.PhysicalCashOutResponse;
import com.melli.wallet.domain.response.cash.PhysicalCashOutTrackResponse;
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

import java.math.BigDecimal;

/**
 * Class Name: CashEndPoint
 * Author: Mahdi Shirinabadi
 * Date: 1/20/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/physicalCashOut")
@Validated
@Log4j2
public class PhysicalCashOutController extends WebController {

    private final RequestContext requestContext;
    private final CashOutService cashOutService;
    private final SecurityService securityService;


    @Timed(description = "CashEndPoint.physical.cashOut.generate.uuid")
    @PostMapping(path = "/generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد شناسه یکتا برای دریافت فیزیکی")
    @PreAuthorize("hasAuthority(\""+ ResourceService.PHYSICAL_CASH_OUT +"\")")
    public ResponseEntity<BaseResponse<UuidResponse>> physicalCashOutGenerateUuid(@Valid @RequestBody PhysicalCashGenerateUuidRequestJson requestJson) throws InternalServiceException {
        log.info("start call uuid nationalCode ===> {}", requestJson.getNationalCode());
        UuidResponse response = cashOutService.physicalGenerateUuid(requestContext.getChannelEntity(), requestJson.getNationalCode(), requestJson.getQuantity(), requestJson.getAccountNumber());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }


    @Timed(description = "CashEndPoint.physical.cashOut")
    @PostMapping(path = "/withdrawal", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "برداشت وجه فیزیکی")
    @PreAuthorize("hasAuthority(\""+ ResourceService.PHYSICAL_CASH_OUT +"\")")
    public ResponseEntity<BaseResponse<PhysicalCashOutResponse>> physicalCashOut(@Valid @RequestBody PhysicalCashOutWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        securityService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());
        log.info("start call physicalCashOut in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);

        PhysicalCashOutResponse response = cashOutService.physicalWithdrawal(new PhysicalCashOutObjectDTO(requestContext.getChannelEntity(), requestJson.getNationalCode(),
                requestJson.getUniqueIdentifier(),new BigDecimal(requestJson.getQuantity()),
                requestJson.getAccountNumber(), requestJson.getAdditionalData(), requestContext.getClientIp(), new BigDecimal(requestJson.getCommissionObject().getAmount()),
                requestJson.getCurrency(), requestJson.getCommissionObject().getCurrency()));

        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    @Timed(description = "CashEndPoint.physical.cashOut.inquiry")
    @GetMapping(path = "/inquiry", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "پیگیری برداشت وجه فیزیکی")
    @PreAuthorize("hasAuthority(\""+ ResourceService.PHYSICAL_CASH_OUT +"\")")
    public ResponseEntity<BaseResponse<PhysicalCashOutTrackResponse>> physicalInquiryCashOut(@Valid @RequestParam("uniqueIdentifier") String uniqueIdentifier) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call physicalInquiryCashOut in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, uniqueIdentifier, channelIp);
        PhysicalCashOutTrackResponse response = cashOutService.physicalInquiry(requestContext.getChannelEntity(), uniqueIdentifier, requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }


}
