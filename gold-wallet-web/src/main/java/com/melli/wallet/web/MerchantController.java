package com.melli.wallet.web;

import com.melli.wallet.annotation.number.NumberValidation;
import com.melli.wallet.annotation.string.StringValidation;
import com.melli.wallet.domain.request.merchant.MerchantBalanceRequest;
import com.melli.wallet.domain.request.merchant.MerchantUpdateRequest;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.purchase.MerchantResponse;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.repository.MerchantRepositoryService;
import com.melli.wallet.service.repository.ResourceRepositoryService;
import com.melli.wallet.service.operation.MerchantOperationService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
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
@RequestMapping("/api/v1/merchant")
@Validated
@Log4j2
public class MerchantController extends WebController {

    private final RequestContext requestContext;
    private final MerchantRepositoryService merchantRepositoryService;
    private final MerchantOperationService merchantOperationService;

    @Timed(description = "Time taken to inquiry gold amount")
    @GetMapping(path = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "لیست پذیرنده ها")
    @PreAuthorize("hasAuthority(\""+ ResourceRepositoryService.MERCHANT_LIST +"\")")
    @LogExecutionTime("Get merchant list")
    public ResponseEntity<BaseResponse<MerchantResponse>> getMerchant(@Valid @StringValidation @RequestParam("currency") String currency) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call getMerchant in username ===> {}, currency ===> {}, from ip ===> {}", username, currency, channelIp);
        MerchantResponse merchantResponse = merchantRepositoryService.getMerchant(requestContext.getChannelEntity(), currency);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, merchantResponse));
    }

    @Timed(description = "Time taken to inquiry gold amount")
    @GetMapping(path = "/balance", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "مانده پذیرنده")
    @PreAuthorize("hasAuthority(\""+ ResourceRepositoryService.MERCHANT_BALANCE +"\")")
    @LogExecutionTime("Get merchant balance")
    public ResponseEntity<BaseResponse<WalletBalanceResponse>> getBalanceMerchant(@Valid @NumberValidation @RequestParam("merchantId") String merchantId) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call balance getMerchant in username ===> {}, merchantId ===> {}, from ip ===> {}", username, merchantId, channelIp);
        WalletBalanceResponse response = merchantOperationService.getBalance(requestContext.getChannelEntity(), merchantId);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, response));
    }

    @Timed(description = "Time taken to inquiry gold amount")
    @PostMapping(path = "/updateStatus", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "تغییر وضعیت پذیرنده")
    @PreAuthorize("hasAuthority(\""+ ResourceRepositoryService.MERCHANT_MANAGE +"\")")
    @LogExecutionTime("Update merchant status")
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> update(@Valid @RequestBody MerchantUpdateRequest request) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call update merchant in username ===> {}, merchantId ===> {}, from ip ===> {}, data ===> ({})", username, request.getMerchantId(), channelIp, request);
        merchantOperationService.updateStatus(requestContext.getChannelEntity(), request.getMerchantId(), request.getStatus());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true));
    }

    @Timed(description = "Time taken to increase merchant balance")
    @PostMapping(path = "/balance/increase", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "افزایش مانده پذیرنده")
    @PreAuthorize("hasAuthority(\""+ ResourceRepositoryService.MERCHANT_INCREASE_BALANCE +"\")")
    @LogExecutionTime("Increase merchant balance")
    public ResponseEntity<BaseResponse<String>> increaseBalance(@Valid @RequestBody MerchantBalanceRequest request) throws InternalServiceException {
        
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call increaseBalance in username ===> {}, walletAccountNumber ===> {}, amount ===> {}, merchantId ===> {}, from ip ===> {}", 
                username, request.getWalletAccountNumber(), request.getAmount(), request.getMerchantId(), channelIp);
        
        String traceId = merchantOperationService.increaseBalance(
                requestContext.getChannelEntity(),
                request.getWalletAccountNumber(),
                request.getAmount(),
                request.getMerchantId()
        );
        
        log.info("finish increaseBalance for merchant {} with amount {} and traceId {}", request.getMerchantId(), request.getAmount(), traceId);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "Balance increased successfully. TraceId: " + traceId));
    }

    @Timed(description = "Time taken to decrease merchant balance")
    @PostMapping(path = "/balance/decrease", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "کاهش مانده پذیرنده")
    @PreAuthorize("hasAuthority(\""+ ResourceRepositoryService.MERCHANT_DECREASE_BALANCE +"\")")
    @LogExecutionTime("Decrease merchant balance")
    public ResponseEntity<BaseResponse<String>> decreaseBalance(@Valid @RequestBody MerchantBalanceRequest request) throws InternalServiceException {
        
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call decreaseBalance in username ===> {}, walletAccountNumber ===> {}, amount ===> {}, merchantId ===> {}, from ip ===> {}", 
                username, request.getWalletAccountNumber(), request.getAmount(), request.getMerchantId(), channelIp);
        
        String traceId = merchantOperationService.decreaseBalance(
                requestContext.getChannelEntity(),
                request.getWalletAccountNumber(),
                request.getAmount(),
                request.getMerchantId()
        );
        
        log.info("finish decreaseBalance for merchant {} with amount {} and traceId {}", request.getMerchantId(), request.getAmount(), traceId);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "Balance decreased successfully. TraceId: " + traceId));
    }
}
