package com.melli.hub.web;

import com.melli.hub.domain.request.wallet.CashInWalletRequestJson;
import com.melli.hub.domain.request.wallet.PurchaseWalletRequestJson;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.cash.CashInResponse;
import com.melli.hub.domain.response.cash.CashInTrackResponse;
import com.melli.hub.domain.response.purchase.PurchaseResponse;
import com.melli.hub.domain.response.purchase.PurchaseTrackResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.security.RequestContext;
import com.melli.hub.service.CashService;
import com.melli.hub.service.PurchaseService;
import com.melli.hub.service.ResourceService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.service.spi.ServiceException;
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
public class PurchaseEndPoint extends WebEndPoint{

    private final RequestContext requestContext;
    private final PurchaseService purchaseService;

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "/buy", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "خرید")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_IN +"\")")
    public ResponseEntity<BaseResponse<CashInResponse>> buy(@RequestBody PurchaseWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call purchase in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        CashInResponse cashInResponse = purchaseService.purchase(requestContext.getChannelEntity(), requestJson.getNationalCode(),  requestJson.getUniqueIdentifier(),requestJson.getAmount(), requestJson.getReferenceNumber(),
                requestJson.getSign(), requestJson.getDataString(), requestJson.getAccountNumber(), requestJson.getAdditionalData(), requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "/sell", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "خرید")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_IN +"\")")
    public ResponseEntity<BaseResponse<CashInResponse>> sell(@RequestBody PurchaseWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call purchase in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        CashInResponse cashInResponse = purchaseService.purchase(requestContext.getChannelEntity(), requestJson.getNationalCode(),  requestJson.getUniqueIdentifier(),requestJson.getAmount(), requestJson.getReferenceNumber(),
                requestJson.getSign(), requestJson.getDataString(), requestJson.getAccountNumber(), requestJson.getAdditionalData(), requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }

    @Timed(description = "Time taken to purchase verify")
    @PostMapping(path = "/verify", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "تایید سفارش")
    @PreAuthorize("hasAuthority(\""+ ResourceService.PURCHASE +"\")")
    public @ResponseBody ResponseEntity<PurchaseResponse> verify(@Parameter(description="لیست پارامترها",schema=@Schema(implementation = VerifyJson.class))@RequestBody VerifyJson verifyJson) throws ServiceException {

        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannel().getChannelName();

        log.info("start call in username ===> {}, uid ==> {}, from ip ===> {} with data {}", username, verifyJson.getUid(), channelIp, verifyJson);

        PurchaseVerifyResponse purchaseVerifyResponse = purchaseService.verify(requestContext.getChannel(), verifyJson.getUid(), channelIp, verifyJson.getCustomerIp(), verifyJson.getPayCashback(), verifyJson.getCashbackAmount());
        return ResponseEntity.status(HttpStatus.OK).body(purchaseVerifyResponse);
    }


    @Timed(description = "Time taken to purchase reverse")
    @PostMapping(path = "/reverse", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "برگشت خرید")
    @PreAuthorize("hasAuthority(\""+ ResourceService.PURCHASE +"\")")
    public @ResponseBody ResponseEntity<PurchaseResponse> reverse(@Parameter(description="لیست پارامترها",schema=@Schema(implementation = ReverseJson.class))@RequestBody ReverseJson reverseJson) throws ServiceException {

        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannel().getChannelName();

        log.info("start call in username ===> {}, uid ==> {}, from ip ===> {} with data {}", username, reverseJson.getUid(), channelIp, reverseJson);

        PurchaseReverseResponse purchaseReverseResponse = purchaseService.reverse(requestContext.getChannel(), reverseJson.getUid(), channelIp, reverseJson.getCustomerIp());
        return ResponseEntity.status(HttpStatus.OK).body(purchaseReverseResponse);
    }

    @Timed(description = "Time taken to create wallet")
    @GetMapping(path = "/track/pay/{uniqueIdentifier}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "پیگیری افزایش موجودی کیف پول")
    @PreAuthorize("hasAuthority(\""+ ResourceService.PURCHASE +"\")")
    public ResponseEntity<BaseResponse<PurchaseTrackResponse>> createWallet(@PathVariable("uniqueIdentifier") String uniqueIdentifier) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call cashIn in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, uniqueIdentifier, channelIp);
        CashInTrackResponse cashInResponse = cashService.cashInTrack(requestContext.getChannelEntity(), uniqueIdentifier, requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }



}
