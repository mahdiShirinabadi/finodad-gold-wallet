package com.melli.wallet.web;

import com.melli.wallet.annotation.fund_type.PurchaseTypeValidation;
import com.melli.wallet.domain.dto.BuyRequestDTO;
import com.melli.wallet.domain.dto.SellRequestDTO;
import com.melli.wallet.domain.request.wallet.*;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.purchase.PurchaseTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.PurchaseService;
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
@RequestMapping("/api/v1/purchase")
@Validated
@Log4j2
public class PurchaseController extends WebController {

    private final RequestContext requestContext;
    private final PurchaseService purchaseService;
    private final SecurityService securityService;

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "/buy/generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد شناسه یکتا")
    @PreAuthorize("hasAuthority(\""+ ResourceService.GENERATE_PURCHASE_UNIQUE_IDENTIFIER +"\")")
    public ResponseEntity<BaseResponse<UuidResponse>> generateBuyUuid(@Valid @RequestBody BuyGenerateUuidRequestJson requestJson) throws InternalServiceException {
        log.info("start call buy uuid nationalCode ===> {}", requestJson.getNationalCode());
        String channelIp = requestContext.getClientIp();
        UuidResponse response = purchaseService.buyGenerateUuid(new BuyRequestDTO(requestContext.getChannelEntity(), "", new BigDecimal(requestJson.getQuantity()),
                Long.parseLong(requestJson.getPrice()), requestJson.getAccountNumber(), "", requestJson.getMerchantId(),
                requestJson.getNationalCode(), null, requestJson.getCurrency(), channelIp,"", ""));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "/sell/generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد شناسه یکتا")
    @PreAuthorize("hasAuthority(\""+ ResourceService.GENERATE_PURCHASE_UNIQUE_IDENTIFIER +"\")")
    public ResponseEntity<BaseResponse<UuidResponse>> generateSellUuid(@Valid @RequestBody SellGenerateUuidRequestJson requestJson) throws InternalServiceException {
        log.info("start call sell uuid nationalCode ===> {}", requestJson.getNationalCode());
        UuidResponse response = purchaseService.sellGenerateUuid(requestContext.getChannelEntity(), requestJson.getNationalCode(), requestJson.getQuantity(), requestJson.getAccountNumber(), requestJson.getCurrency());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    @Timed(description = "Time taken to inquiry gold amount")
    @GetMapping(path = "/inquiry", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "پیگیری")
    @PreAuthorize("hasAuthority(\""+ ResourceService.GENERATE_PURCHASE_UNIQUE_IDENTIFIER +"\")")
    public ResponseEntity<BaseResponse<PurchaseTrackResponse>> sell(@Valid @RequestParam("uniqueIdentifier") String uniqueIdentifier,@Valid @RequestParam("type") @PurchaseTypeValidation(label = "نوع تراکنش")  String type) throws InternalServiceException {
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
    public ResponseEntity<BaseResponse<PurchaseResponse>> buy(@Valid @RequestBody BuyWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();

        log.info("start call buy in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        securityService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());

        PurchaseResponse purchaseResponse = purchaseService.buy(new BuyRequestDTO(requestContext.getChannelEntity(), requestJson.getUniqueIdentifier(), new BigDecimal(requestJson.getQuantity()),
                Long.parseLong(requestJson.getTotalPrice()), requestJson.getWalletAccountNumber(), requestJson.getAdditionalData(), requestJson.getMerchantId(),
                requestJson.getNationalCode(), new BigDecimal(requestJson.getCommissionObject().getAmount()), requestJson.getCurrency(), channelIp,"", requestJson.getCommissionObject().getCurrency()));

        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,purchaseResponse));
    }

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "/buy/direct", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "خرید")
    @PreAuthorize("hasAuthority(\""+ ResourceService.BUY_DIRECT +"\")")
    public ResponseEntity<BaseResponse<PurchaseResponse>> buyDirect(@Valid @RequestBody BuyDirectWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();

        log.info("start call buy direct in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        securityService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());

        PurchaseResponse purchaseResponse = purchaseService.buyDirect(new BuyRequestDTO(requestContext.getChannelEntity(), requestJson.getUniqueIdentifier(), new BigDecimal(requestJson.getQuantity()),
                Long.parseLong(requestJson.getTotalPrice()), requestJson.getWalletAccountNumber(), requestJson.getAdditionalData(), requestJson.getMerchantId(),
                requestJson.getNationalCode(), new BigDecimal(requestJson.getCommissionObject().getAmount()), requestJson.getCurrency(), channelIp,requestJson.getRefNumber(), requestJson.getCommissionObject().getCurrency()));

        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,purchaseResponse));
    }

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "/sell", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "فروش")
    @PreAuthorize("hasAuthority(\""+ ResourceService.SELL +"\")")
    public ResponseEntity<BaseResponse<PurchaseResponse>> sell(@Valid @RequestBody SellWalletRequestJson requestJson) throws InternalServiceException {

        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();

        log.info("start call sell in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        securityService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());

        PurchaseResponse cashInResponse = purchaseService.sell(new SellRequestDTO(requestContext.getChannelEntity(), requestJson.getUniqueIdentifier(), new BigDecimal(requestJson.getQuantity()),
                Long.parseLong(requestJson.getPrice()), requestJson.getWalletAccountNumber(), requestJson.getAdditionalData(), requestJson.getMerchantId(),
                requestJson.getNationalCode(), new BigDecimal(requestJson.getCommissionObject().getAmount()),requestJson.getCurrency(), channelIp, requestJson.getCommissionObject().getCurrency()));

        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }
}
