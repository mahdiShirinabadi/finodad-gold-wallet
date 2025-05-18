package com.melli.wallet.web;

import com.melli.wallet.annotation.fund_type.PurchaseTypeValidation;
import com.melli.wallet.annotation.string.StringValidation;
import com.melli.wallet.domain.dto.BuyRequestDTO;
import com.melli.wallet.domain.dto.SellRequestDTO;
import com.melli.wallet.domain.request.wallet.BuyWalletRequestJson;
import com.melli.wallet.domain.request.wallet.PurchaseGenerateUuidRequestJson;
import com.melli.wallet.domain.request.wallet.SellWalletRequestJson;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.purchase.MerchantResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.purchase.PurchaseTrackResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.MerchantService;
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
@RequestMapping("/api/v1/merchant")
@Validated
@Log4j2
public class MerchantController extends WebController {

    private final RequestContext requestContext;
    private final MerchantService merchantService;

    @Timed(description = "Time taken to inquiry gold amount")
    @GetMapping(path = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "لیست پذیرنده ها")
    @PreAuthorize("hasAuthority(\""+ ResourceService.MERCHANT_LIST +"\")")
    public ResponseEntity<BaseResponse<MerchantResponse>> getMerchant(@Valid @StringValidation @RequestParam("currency") String currency) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call getMerchant in username ===> {}, currency ===> {}, from ip ===> {}", username, currency, channelIp);
        MerchantResponse merchantResponse = merchantService.getMerchant(requestContext.getChannelEntity(), currency);
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, merchantResponse));
    }
}
