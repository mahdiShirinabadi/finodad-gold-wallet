package com.melli.wallet.web.panel;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.MerchantEntity;
import com.melli.wallet.domain.master.entity.MerchantWalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.persistence.MerchantWalletAccountCurrencyRepository;
import com.melli.wallet.domain.request.setup.ChannelCreateRequestJson;
import com.melli.wallet.domain.request.setup.MerchantCreateRequestJson;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.*;
import com.melli.wallet.web.WebController;
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

import java.util.Date;
import java.util.List;

/**
 * Class Name: PanelLimitationController
 * Author: Mahdi Shirinabadi
 * Date: 7/21/2025
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = {"/api/v1/panel/manage"})
@Validated
@Log4j2
public class PanelChannelController extends WebController {

    private final RequestContext requestContext;
    private final ChannelService channelService;
    private final WalletService walletService;
    private final MerchantService merchantService;
    private final WalletOperationalService walletOperationalService;
    private final WalletAccountCurrencyService walletAccountCurrencyService;
    private final MerchantWalletAccountCurrencyRepository merchantWalletAccountCurrencyRepository;

    @Timed(description = "Time taken to update limitation general")
    @PostMapping(path = "/channel/wallet/create", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "Update general limitation (value and pattern only)")
    @PreAuthorize("hasAuthority(\"" + ResourceService.LIMITATION_MANAGE + "\")")
    public ResponseEntity<BaseResponse<String>> createChannelWallet(@Valid @RequestBody ChannelCreateRequestJson requestJson) throws InternalServiceException {

        ChannelEntity channel = channelService.getChannel(requestJson.getUsername());
        if (channel == null) {
            log.error("channel with name ({}) is not exist", requestJson.getUsername());
            throw new InternalServiceException("channel not found", StatusService.CHANNEL_NOT_FOUND, HttpStatus.OK);
        }

        if (channel.getWalletEntity() == null) {
            CreateWalletResponse createWalletResponse = walletOperationalService.createWallet(requestContext.getChannelEntity(), channel.getMobile(), requestJson.getNationalCode(), WalletTypeService.CHANNEL, List.of(WalletAccountCurrencyService.GOLD, WalletAccountCurrencyService.RIAL),
                    List.of(WalletAccountTypeService.WAGE));
            channel.setWalletEntity(walletService.findById(Long.parseLong(createWalletResponse.getWalletId())));
            channelService.save(channel);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "wage wallet created for channel"));
    }


    @Timed(description = "Time taken to update limitation general")
    @PostMapping(path = "/merchant/create", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "Update general limitation (value and pattern only)")
    @PreAuthorize("hasAuthority(\"" + ResourceService.LIMITATION_MANAGE + "\")")
    public ResponseEntity<BaseResponse<String>> createMerchantWallet(@Valid @RequestBody MerchantCreateRequestJson requestJson) throws InternalServiceException {

        CreateWalletResponse createWalletResponse = walletOperationalService.createWallet(requestContext.getChannelEntity(), requestJson.getMobileNumber(), "1111111111", WalletTypeService.MERCHANT, List.of(WalletAccountCurrencyService.GOLD, WalletAccountCurrencyService.RIAL),
                List.of(WalletAccountTypeService.NORMAL));
        MerchantEntity merchantEntity = new MerchantEntity();
        merchantEntity.setName(requestJson.getName());
        merchantEntity.setDescription("create merchant");
        merchantEntity.setMobile(requestJson.getMobileNumber());
        merchantEntity.setNationalCode("1111111111");
        merchantEntity.setEconomicalCode(requestJson.getEconomicCode());
        merchantEntity.setLogo("");
        merchantEntity.setWalletEntity(walletService.findById(Long.parseLong(createWalletResponse.getWalletId())));
        merchantEntity.setSettlementType(1);
        merchantEntity.setStatus(1);
        merchantEntity.setCreatedBy(requestContext.getChannelEntity().getUsername());
        merchantEntity.setCreatedAt(new Date());
        merchantService.save(merchantEntity);

        MerchantWalletAccountCurrencyEntity goldEntity = new MerchantWalletAccountCurrencyEntity();
        goldEntity.setCreatedBy(requestContext.getChannelEntity().getUsername());
        goldEntity.setCreatedAt(new Date());
        goldEntity.setMerchantEntity(merchantEntity);
        goldEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyService.findCurrency(WalletAccountCurrencyService.GOLD));
        merchantWalletAccountCurrencyRepository.save(goldEntity);

        MerchantWalletAccountCurrencyEntity rialEntity = new MerchantWalletAccountCurrencyEntity();
        rialEntity.setCreatedBy(requestContext.getChannelEntity().getUsername());
        rialEntity.setCreatedAt(new Date());
        rialEntity.setMerchantEntity(merchantEntity);
        rialEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyService.findCurrency(WalletAccountCurrencyService.RIAL));
        merchantWalletAccountCurrencyRepository.save(rialEntity);

        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "wage wallet created for channel"));
    }



}
