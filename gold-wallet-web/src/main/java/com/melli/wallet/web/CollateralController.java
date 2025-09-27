package com.melli.wallet.web;

import com.melli.wallet.annotation.LogExecutionTime;
import com.melli.wallet.domain.dto.CreateCollateralObjectDTO;
import com.melli.wallet.domain.dto.ReleaseCollateralObjectDTO;
import com.melli.wallet.domain.enumaration.ResourceDefinition;
import com.melli.wallet.domain.request.wallet.collateral.CreateCollateralRequestJson;
import com.melli.wallet.domain.request.wallet.collateral.CreateUniqueIdentifierCollateralRequestJson;
import com.melli.wallet.domain.request.wallet.collateral.ReleaseCollateralRequestJson;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @Timed(description = "CollateralController.uuid")
    @PostMapping(path = "/generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "کد پیگیری ایجاد وثیقه")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("create uuid collateral")
    public ResponseEntity<BaseResponse<UuidResponse>> generateUuid(@Valid @RequestBody CreateUniqueIdentifierCollateralRequestJson requestJson) throws InternalServiceException {

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
                new BigDecimal(requestJson.getCommissionObject().getAmount()), requestJson.getCommissionObject().getCurrency(), requestContext.getClientIp()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    //create new collateral
    @Timed(description = "CollateralController.release")
    @PostMapping(path = "/release", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد وثیقه")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.COLLATERAL_AUTH + "')")
    @LogExecutionTime("create collateral")
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

    //increase collateral for current block dedicate
   /* @Timed(description = "CollateralController.cashIn")
    @PostMapping(path = "/increase", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد وثیقه")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.CASH_IN_AUTH + "')")
    @LogExecutionTime("Cash in charge")
    public ResponseEntity<BaseResponse<CashInResponse>> increase(@Valid @RequestBody CashInWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();

        securityOperationService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());

        log.info("start call cashIn in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        CashInResponse cashInResponse = cashInOperationService.charge(new ChargeObjectDTO(requestContext.getChannelEntity(), requestJson.getNationalCode(),  requestJson.getUniqueIdentifier(),requestJson.getAmount(), requestJson.getReferenceNumber(),
                requestJson.getAccountNumber(), requestJson.getAdditionalData(), requestContext.getClientIp(), requestJson.getCashInType()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }*/



    //stop collateral
  /*  @Timed(description = "CollateralController.cashIn")
    @PostMapping(path = "/increase", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد وثیقه")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.CASH_IN_AUTH + "')")
    @LogExecutionTime("Cash in charge")
    public ResponseEntity<BaseResponse<CashInResponse>> release(@Valid @RequestBody CashInWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();

        securityOperationService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());

        log.info("start call cashIn in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        CashInResponse cashInResponse = cashInOperationService.charge(new ChargeObjectDTO(requestContext.getChannelEntity(), requestJson.getNationalCode(),  requestJson.getUniqueIdentifier(),requestJson.getAmount(), requestJson.getReferenceNumber(),
                requestJson.getAccountNumber(), requestJson.getAdditionalData(), requestContext.getClientIp(), requestJson.getCashInType()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }

    //list collateral for customer
    @Timed(description = "CollateralController.cashIn")
    @PostMapping(path = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد وثیقه")
    @PreAuthorize("hasAuthority('" + ResourceDefinition.CASH_IN_AUTH + "')")
    @LogExecutionTime("Cash in charge")
    public ResponseEntity<BaseResponse<CashInResponse>> release(@Valid @RequestBody CashInWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();

        securityOperationService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());

        log.info("start call cashIn in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        CashInResponse cashInResponse = cashInOperationService.charge(new ChargeObjectDTO(requestContext.getChannelEntity(), requestJson.getNationalCode(),  requestJson.getUniqueIdentifier(),requestJson.getAmount(), requestJson.getReferenceNumber(),
                requestJson.getAccountNumber(), requestJson.getAdditionalData(), requestContext.getClientIp(), requestJson.getCashInType()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }*/


}
