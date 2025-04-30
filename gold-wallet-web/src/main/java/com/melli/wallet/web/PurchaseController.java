package com.melli.wallet.web;

import com.melli.wallet.annotation.fund_type.PurchaseTypeValidation;
import com.melli.wallet.domain.request.wallet.BuyWalletRequestJson;
import com.melli.wallet.domain.request.wallet.CashInGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.PurchaseGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.SellWalletRequestJson;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.purchase.PurchaseTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.PurchaseService;
import com.melli.wallet.service.ResourceService;
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
@RequestMapping("/api/v1/purchase")
@Validated
@Log4j2
public class PurchaseController extends WebController {

    private final RequestContext requestContext;
    private final PurchaseService purchaseService;

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد شناسه یکتا")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_IN +"\")")
    public ResponseEntity<BaseResponse<UuidResponse>> generateUuid(@RequestBody PurchaseGenerateUuidRequestJson requestJson) throws InternalServiceException {
        log.info("start call uuid nationalCode ===> {}", requestJson.getNationalCode());
        UuidResponse response = purchaseService.generateUuid(requestContext.getChannelEntity(), requestJson.getNationalCode(), requestJson.getAmount(), requestJson.getAccountNumber(), requestJson.getPurchaseType());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    @Timed(description = "Time taken to inquiry gold amount")
    @PostMapping(path = "/inquiry/{type}/{uniqueIdentifier}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "پیگیری")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_IN +"\")")
    public ResponseEntity<BaseResponse<PurchaseTrackResponse>> sell(@PathVariable("uniqueIdentifier") String uniqueIdentifier, @PathVariable("type") @PurchaseTypeValidation(label = "نوع تراکنش")  String type) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call purchase in username ===> {}, uniqueIdentifier ===> {}, from ip ===> {}", username, uniqueIdentifier, channelIp);
        PurchaseTrackResponse cashInResponse = purchaseService.purchaseTrack(requestContext.getChannelEntity(),uniqueIdentifier, type, requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "/buy", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "خرید")
    @PreAuthorize("hasAuthority(\""+ ResourceService.BUY +"\")")
    public ResponseEntity<BaseResponse<PurchaseResponse>> buy(@RequestBody BuyWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call purchase in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        PurchaseResponse cashInResponse = purchaseService.buy(requestContext.getChannelEntity(), requestJson.getUniqueIdentifier(), requestJson.getAmount(),
                requestJson.getPrice(), requestJson.getWalletAccountNumber(), requestJson.getSign(),  requestJson.getDataString(),requestJson.getAdditionalData(), requestJson.getMerchantId(),
                requestJson.getNationalCode(), requestJson.getCommissionObject().getAmount(), requestJson.getCurrency(), channelIp, requestJson.getCommissionObject().getCurrency());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "/sell", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "فروش")
    @PreAuthorize("hasAuthority(\""+ ResourceService.SELL +"\")")
    public ResponseEntity<BaseResponse<PurchaseResponse>> sell(@RequestBody SellWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call purchase in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        PurchaseResponse cashInResponse = purchaseService.sell(requestContext.getChannelEntity(), requestJson.getUniqueIdentifier(), requestJson.getAmount(),
                requestJson.getPrice(), requestJson.getWalletAccountNumber(), requestJson.getSign(),  requestJson.getDataString(),requestJson.getAdditionalData(), requestJson.getMerchantId(),
                requestJson.getNationalCode(), requestJson.getCommissionObject().getAmount(),requestJson.getCurrency(), channelIp, requestJson.getCommissionObject().getCurrency());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }
}
