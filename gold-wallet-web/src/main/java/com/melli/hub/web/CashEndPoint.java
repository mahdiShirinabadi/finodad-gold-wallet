package com.melli.hub.web;

import com.melli.hub.domain.request.wallet.CreateWalletRequestJson;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.wallet.CreateWalletResponse;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.security.RequestContext;
import com.melli.hub.service.ResourceService;
import com.melli.hub.service.WalletAccountCurrencyService;
import com.melli.hub.service.WalletAccountTypeService;
import com.melli.hub.service.WalletTypeService;
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
@RequestMapping("/api/v1/cash")
@Validated
@Log4j2
public class CashEndPoint extends WebEndPoint{

    private final RequestContext requestContext;

    @Timed(description = "Time taken to create wallet")
    @PostMapping(path = "/cashIn", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = { @SecurityRequirement(name = "bearer-key") },summary = "افزایش کیف پول")
    @PreAuthorize("hasAuthority(\""+ ResourceService.CASH_IN +"\")")
    public ResponseEntity<BaseResponse<CreateWalletResponse>> createWallet(@RequestBody CreateWalletRequestJson requestJson) throws InternalServiceException {
        String channelIp = requestContext.getClientIp();
        String username = requestContext.getChannelEntity().getUsername();
        log.info("start call create wallet in username ===> {}, mobile ===> {}, from ip ===> {}", username, requestJson.getMobile(), channelIp);
        String cleanMobile = Utility.cleanPhoneNumber(requestJson.getMobile());
        CreateWalletResponse createWalletResponse = walletOperationalService.createWallet(requestContext.getChannelEntity(), cleanMobile, requestJson.getNationalCode(), WalletTypeService.NORMAL_USER, List.of(WalletAccountCurrencyService.GOLD, WalletAccountCurrencyService.RIAL),
                List.of(WalletAccountTypeService.NORMAL));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true,createWalletResponse));
    }
}
