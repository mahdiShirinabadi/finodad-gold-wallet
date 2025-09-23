package com.melli.wallet.web;

import com.melli.wallet.domain.dto.GiftCardPaymentObjectDTO;
import com.melli.wallet.domain.dto.GiftCardProcessObjectDTO;
import com.melli.wallet.domain.request.wallet.giftcard.GiftCardProcessRequestJson;
import com.melli.wallet.domain.request.wallet.giftcard.GiftCardGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.giftcard.PaymentGiftCardRequestJson;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.giftcard.GiftCardResponse;
import com.melli.wallet.domain.response.giftcard.GiftCardTrackResponse;
import com.melli.wallet.domain.response.giftcard.GiftCardUuidResponse;
import com.melli.wallet.domain.response.p2p.P2pTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.operation.GiftCardOperationService;
import com.melli.wallet.service.operation.SecurityOperationService;
import com.melli.wallet.service.repository.ResourceDefinition;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
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
 * Class Name: P2pController
 * Author: Mahdi Shirinabadi
 * Date: 5/26/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/giftCard")
@Validated
@Log4j2
public class GiftCardController extends WebController{

    private final GiftCardOperationService giftCardOperationService;
    private final RequestContext requestContext;
    private final SecurityOperationService securityService;

    @Timed(description = "GiftCardController.generate.uuid")
    @PostMapping(path = "/generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد شناسه یکتا")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.GIFT_CARD_AUTH + "')")
    public ResponseEntity<BaseResponse<GiftCardUuidResponse>> generateUuid(@Valid @RequestBody GiftCardGenerateUuidRequestJson requestJson) throws InternalServiceException {
        log.info("start call uuid accountNumber ===> {}", requestJson.getAccountNumber());
        GiftCardUuidResponse response = giftCardOperationService.generateUuid(requestContext.getChannelEntity(), requestJson.getNationalCode(), requestJson.getQuantity(), requestJson.getAccountNumber(), requestJson.getCurrency());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    @Timed(description = "GiftCardController.process")
    @PostMapping(path = "/process", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایحاد کیف پول")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.GIFT_CARD_AUTH + "')")
    public ResponseEntity<BaseResponse<GiftCardResponse>> process(@Valid @RequestBody GiftCardProcessRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        securityService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());
        log.info("start call p2p in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        GiftCardResponse response = giftCardOperationService.process(new GiftCardProcessObjectDTO(requestContext.getChannelEntity(), requestJson.getUniqueIdentifier(),
                requestJson.getQuantity(), new BigDecimal(requestJson.getCommissionObject().getAmount()), requestJson.getCommissionObject().getCurrency(),
                requestJson.getNationalCode(),  requestJson.getAccountNumber(), requestJson.getDestinationNationalCode(),requestContext.getClientIp(), requestJson.getAdditionalData()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @Timed(description = "GiftCardController.payment.inquiry")
    @GetMapping(path = "/inquiry", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "استعلام کارت هدیه")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.GIFT_CARD_AUTH + "')")
    public ResponseEntity<BaseResponse<GiftCardTrackResponse>> inquiry(@Valid @RequestParam("uniqueIdentifier") String uniqueIdentifier) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call track p2p in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, uniqueIdentifier, channelIp);
        GiftCardTrackResponse response = giftCardOperationService.inquiry(requestContext.getChannelEntity(), uniqueIdentifier, requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    @Timed(description = "GiftCardController.payment")
    @PostMapping(path = "/payment", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "مصرف کیف پول")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.GIFT_CARD_AUTH + "')")
    public ResponseEntity<BaseResponse<Null>> payment(@Valid @RequestBody PaymentGiftCardRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call payment in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        giftCardOperationService.payment(new GiftCardPaymentObjectDTO(requestContext.getChannelEntity(), requestJson.getGiftCardUniqueCode(),
                requestJson.getQuantity(), requestJson.getCurrency(), requestJson.getNationalCode(), channelIp, requestJson.getAccountNumber(), requestJson.getAdditionalData()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
    }
}
