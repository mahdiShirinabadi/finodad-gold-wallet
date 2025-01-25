package com.melli.hub.web;

import com.melli.hub.domain.request.wallet.CreateWalletRequestJson;
import com.melli.hub.domain.request.wallet.GenerateUuidRequestJson;
import com.melli.hub.domain.response.UuidResponse;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.wallet.CreateWalletResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.security.RequestContext;
import com.melli.hub.service.*;
import com.melli.hub.util.Utility;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Class Name: CashEndPoint
 * Author: Mahdi Shirinabadi
 * Date: 1/20/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/general")
@Validated
@Log4j2
public class GeneralEndPoint extends WebEndPoint{

    private final RequestContext requestContext;
    private final GeneralService generalService;

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد شناسه یکتا")
    @PreAuthorize("hasAuthority(\""+ ResourceService.GENERATE_UNIQUE_IDENTIFIER +"\")")
    public ResponseEntity<BaseResponse<UuidResponse>> generateUuid(@RequestBody GenerateUuidRequestJson requestJson) throws InternalServiceException {
        log.info("start call uuid nationalCode ===> {}", requestJson.getNationalCode());
        UuidResponse response = generalService.generateUuid(requestContext.getChannelEntity(), requestJson.getNationalCode());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }
}
