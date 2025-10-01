package com.melli.wallet.web;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.dto.*;
import com.melli.wallet.domain.enumaration.ResourceDefinition;
import com.melli.wallet.domain.request.wallet.collateral.*;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.collateral.CollateralTrackResponse;
import com.melli.wallet.domain.response.collateral.CreateCollateralResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.operation.CollateralOperationService;
import com.melli.wallet.service.operation.SecurityOperationService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Class Name: BlockController
 * Author: Mahdi Shirinabadi
 * Date: 9/27/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/collateral")
@Validated
@Log4j2
public class CollateralController extends WebController {

    private final RequestContext requestContext;
    private final CollateralOperationService collateralOperationService;
    private final SecurityOperationService securityOperationService;

    //collateral generate uniqueIdentifier
    @Timed(description = "CollateralController.create.uuid")
    @PostMapping(path = "/create/generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "کد پیگیری ایجاد وثیقه")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("create uuid collateral")
    public ResponseEntity<BaseResponse<UuidResponse>> generateUuid(@Valid @RequestBody UniqueIdentifierCollateralRequestJson requestJson) throws InternalServiceException {

        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call uuid in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        UuidResponse response = collateralOperationService.generateUniqueIdentifier(requestContext.getChannelEntity(), requestJson.getNationalCode(),  requestJson.getQuantity(),requestJson.getCurrency(), requestJson.getWalletAccountNumber());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    //create new collateral
    @Timed(description = "CollateralController.create")
    @PostMapping(path = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد وثیقه")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("create collateral")
    public ResponseEntity<BaseResponse<CreateCollateralResponse>> create(@Valid @RequestBody CreateCollateralRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();

        securityOperationService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());

        log.info("start call create in username ===> {}, account ===> {}, from ip ===> {}", username, requestJson.getAccountNumber(), channelIp);
        CreateCollateralResponse response = collateralOperationService.create(new CreateCollateralObjectDTO(requestContext.getChannelEntity(),
                requestJson.getUniqueIdentifier(), new BigDecimal(requestJson.getQuantity()) ,requestJson.getAccountNumber(), requestJson.getDescription(),
                new BigDecimal(requestJson.getCommissionObject().getAmount()), requestJson.getCommissionObject().getCurrency(), requestContext.getClientIp(), requestJson.getCollateralId()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    //release exist collateral
    @Timed(description = "CollateralController.release")
    @PostMapping(path = "/release", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "آزادسازی کامل وثیقه")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("release collateral")
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> release(@Valid @RequestBody ReleaseCollateralRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();

        securityOperationService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());

        log.info("start call release in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        collateralOperationService.release(new ReleaseCollateralObjectDTO(requestContext.getChannelEntity(),
                requestJson.getCollateralCode(), new BigDecimal(requestJson.getQuantity()) ,requestJson.getNationalCode(), requestJson.getAdditionalData(),
                new BigDecimal(requestJson.getCommissionObject().getAmount()), requestJson.getCommissionObject().getCurrency(), requestContext.getClientIp()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
    }


    //inquiry collateral
    @Timed(description = "CollateralController.inquiry")
    @GetMapping(path = "/inquiry", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "استعلام وثیقه")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("inquiry collateral")
    public ResponseEntity<BaseResponse<CollateralTrackResponse>> inquiry(@Valid @RequestParam("uniqueIdentifier") String uniqueIdentifier) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call track p2p in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, uniqueIdentifier, channelIp);
        CollateralTrackResponse response = collateralOperationService.inquiry(requestContext.getChannelEntity(), uniqueIdentifier, requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }


    //collateral generate uniqueIdentifier
    @Timed(description = "CollateralController.create.uuid")
    @PostMapping(path = "/increase/generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "کد پیگیری ایجاد وثیقه")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("create uuid collateral")
    public ResponseEntity<BaseResponse<UuidResponse>> increaseGenerateUuid(@Valid @RequestBody UniqueIdentifierCollateralRequestJson requestJson) throws InternalServiceException {

        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call increase uuid in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        UuidResponse response = collateralOperationService.generateIncreaseUniqueIdentifier(requestContext.getChannelEntity(), requestJson.getNationalCode(),  requestJson.getQuantity(),requestJson.getCurrency(), requestJson.getWalletAccountNumber());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    //increase collateral
    @Timed(description = "CollateralController.increase")
    @PostMapping(path = "/increase", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "افزایش وثیقه")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("increase collateral")
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> increase(@Valid @RequestBody IncreaseCollateralRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();

        securityOperationService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());

        log.info("start call increase in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        collateralOperationService.increase(new IncreaseCollateralObjectDTO(requestContext.getChannelEntity(),
                requestJson.getCollateralCode(), new BigDecimal(requestJson.getQuantity()) ,requestJson.getNationalCode(), requestJson.getAdditionalData(),
                new BigDecimal(requestJson.getCommissionObject().getAmount()), requestJson.getCommissionObject().getCurrency(), requestContext.getClientIp(), requestJson.getUniqueIdentifier()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
    }


    //seize collateral
    @Timed(description = "CollateralController.record")
    @PostMapping(path = "/seize", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ضبط وثیقه")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("seize collateral")
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> seize(@Valid @RequestBody SeizeCollateralRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();


        log.info("start call seize in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        collateralOperationService.seize(new SeizeCollateralObjectDTO(requestContext.getChannelEntity(),
                requestJson.getCollateralCode(), requestJson.getNationalCode(), requestJson.getAdditionalData(),
                requestContext.getClientIp()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
    }


    //sell collateral
    @Timed(description = "CollateralController.sell")
    @PostMapping(path = "/sell", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "فروش وثیقه")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("sell collateral")
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> sell(@Valid @RequestBody SellCollateralRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();

        securityOperationService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());

        log.info("start call sell in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        collateralOperationService.sell(new SellCollateralObjectDTO(requestContext.getChannelEntity(),
                requestJson.getCollateralCode(), new BigDecimal(requestJson.getQuantity()), requestJson.getNationalCode(), requestJson.getAdditionalData(),
                new BigDecimal(requestJson.getCommissionObject().getAmount()), requestJson.getCommissionObject().getCurrency() , requestContext.getClientIp(),
                requestJson.getPrice(), requestJson.getMerchantId()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
    }
}
