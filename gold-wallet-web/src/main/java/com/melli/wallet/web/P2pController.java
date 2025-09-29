package com.melli.wallet.web;

import com.melli.wallet.domain.dto.P2pObjectDTO;
import com.melli.wallet.domain.request.wallet.p2p.P2pGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.p2p.P2pRequestJson;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.p2p.P2pTrackResponse;
import com.melli.wallet.domain.response.p2p.P2pUuidResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.operation.Person2PersonOperationService;
import com.melli.wallet.service.operation.SecurityOperationService;
import com.melli.wallet.domain.enumaration.ResourceDefinition;
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
@RequestMapping("/api/v1/p2p")
@Validated
@Log4j2
public class P2pController extends WebController{


    private final Person2PersonOperationService person2PersonOperationService;
    private final RequestContext requestContext;
    private final SecurityOperationService securityService;

    @Timed(description = "P2pController.generate.uuid")
    @PostMapping(path = "/generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد شناسه یکتا")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.P2P_AUTH + "')")
    public ResponseEntity<BaseResponse<P2pUuidResponse>> generateUuid(@Valid @RequestBody P2pGenerateUuidRequestJson requestJson) throws InternalServiceException {
        log.info("start call uuid accountNumber ===> {}", requestJson.getAccountNumber());
        P2pUuidResponse response = person2PersonOperationService.generateUuid(requestContext.getChannelEntity(), requestJson.getNationalCode(), requestJson.getQuantity(), requestJson.getAccountNumber(), requestJson.getDestAccountNumber());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    @Timed(description = "P2pController.p2p")
    @PostMapping(path = "/process", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "حساب به حساب")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.P2P_AUTH + "')")
    public ResponseEntity<BaseResponse<Null>> process(@Valid @RequestBody P2pRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        securityService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());
        log.info("start call p2p in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        person2PersonOperationService.process(new P2pObjectDTO(requestContext.getChannelEntity(), requestJson.getNationalCode(),
                requestJson.getUniqueIdentifier(), requestJson.getAccountNumber(), new BigDecimal(requestJson.getQuantity()), requestJson.getDestAccountNumber(),
                requestJson.getAdditionalData(), requestContext.getClientIp(), requestJson.getCurrency(), new BigDecimal(requestJson.getCommissionObject().getAmount()), requestJson.getCommissionObject().getCurrency()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
    }

    @Timed(description = "P2pController.inquiry")
    @GetMapping(path = "/inquiry", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "استعلام حساب به حساب")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.P2P_AUTH + "')")
    public ResponseEntity<BaseResponse<P2pTrackResponse>> inquiry(@Valid @RequestParam("uniqueIdentifier") String uniqueIdentifier) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call track p2p in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, uniqueIdentifier, channelIp);
        P2pTrackResponse response = person2PersonOperationService.inquiry(requestContext.getChannelEntity(), uniqueIdentifier, requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }
}
