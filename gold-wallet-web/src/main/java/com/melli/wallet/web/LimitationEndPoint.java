package com.melli.wallet.web;

import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.domain.response.limitation.LimitationCustomResponse;
import com.melli.wallet.domain.response.limitation.LimitationListResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.LimitationOperationService;
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
@RequestMapping(value = {"/api/v1/limitation"})
@Validated
@Log4j2
public class LimitationEndPoint extends WebEndPoint{

    private final RequestContext requestContext;
    private final LimitationOperationService limitationOperationService;

    @Timed(description = "Time taken to create wallet")
    @GetMapping(path = "/getValue", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "دریافت محدودیتها کلی")
    @PreAuthorize("hasAuthority(\""+ ResourceService.LIMITATION_LIST +"\")")
    public ResponseEntity<BaseResponse<LimitationCustomResponse>> generalList(@RequestParam(value = "limitationName") String limitationName,
                                                                              @RequestParam(value ="accountNumber") String accountNumber,
                                                                              @RequestParam(value ="nationalCode") String nationalCode
    ) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start get setting value in username ===> {}, limitationName ===> {}, accountNumber ===> {}, from ip ===> {}", username,limitationName, accountNumber, channelIp);
        String value = limitationOperationService.getValue(requestContext.getChannelEntity(), limitationName,  accountNumber, nationalCode , requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,new LimitationCustomResponse(value)));
    }

    @Timed(description = "Time taken to create wallet")
    @GetMapping(path = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "دریافت لیست محدودیتها")
    @PreAuthorize("hasAuthority(\""+ ResourceService.LIMITATION_LIST +"\")")
    public ResponseEntity<BaseResponse<LimitationListResponse>> getList() throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call cashIn in username ===> {}, from ip ===> {}", username, channelIp);
        LimitationListResponse response = limitationOperationService.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

}
