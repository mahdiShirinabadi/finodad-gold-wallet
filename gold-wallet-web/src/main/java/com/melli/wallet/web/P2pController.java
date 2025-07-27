package com.melli.wallet.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /*@Timed(description = "Time taken to create wallet")
    @PostMapping(path = "/generate/uuid", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "ایجاد شناسه یکتا")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_IN +"\")")
    public ResponseEntity<BaseResponse<UuidResponse>> generateUuid(@Valid @RequestBody CashGenerateUuidRequestJson requestJson) throws InternalServiceException {
        log.info("start call uuid nationalCode ===> {}", requestJson.getNationalCode());
        UuidResponse response = cashInService.generateUuid(requestContext.getChannelEntity(), requestJson.getNationalCode(), requestJson.getAmount(), requestJson.getAccountNumber());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,response));
    }

    @Timed(description = "CashEndPoint.cashOut")
    @PostMapping(path = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "حساب به حساب")
    @PreAuthorize("hasAuthority(\""+ ResourceService.P2P +"\")")
    public ResponseEntity<BaseResponse<CashOutResponse>> cashOut(@Valid @RequestBody CashOutWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        securityService.checkSign(requestContext.getChannelEntity(), requestJson.getSign(), requestJson.getDataString());

        log.info("start call cashOut in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, requestJson.getNationalCode(), channelIp);
        CashOutResponse cashOutResponse = cashOutService.withdrawal(new CashOutObjectDTO(requestContext.getChannelEntity(), requestJson.getNationalCode(),
                requestJson.getUniqueIdentifier(),requestJson.getAmount(), requestJson.getIban(),
                requestJson.getAccountNumber(), requestJson.getAdditionalData(), requestContext.getClientIp()));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashOutResponse));
    }

    @Timed(description = "CashEndPoint.cashOut.inquiry")
    @GetMapping(path = "/inquiry", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "استعلام حساب به حساب")
    @PreAuthorize("hasAuthority(\""+ ResourceService.P2P +"\")")
    public ResponseEntity<BaseResponse<CashInTrackResponse>> inquiryCashOut(@Valid @RequestParam("uniqueIdentifier") String uniqueIdentifier) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call inquiry cashOut in username ===> {}, nationalCode ===> {}, from ip ===> {}", username, uniqueIdentifier, channelIp);
        CashInTrackResponse cashInResponse = cashInService.inquiry(requestContext.getChannelEntity(), uniqueIdentifier, requestContext.getClientIp());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,cashInResponse));
    }*/
}
