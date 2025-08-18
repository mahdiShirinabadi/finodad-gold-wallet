package com.melli.wallet.web.panel;

import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.MerchantEntity;
import com.melli.wallet.domain.master.entity.MerchantWalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.persistence.MerchantWalletAccountCurrencyRepository;
import com.melli.wallet.domain.request.PanelBaseSearchJson;
import com.melli.wallet.domain.request.PanelChannelResourceUpdateRequest;
import com.melli.wallet.domain.request.setup.ChannelCreateRequestJson;
import com.melli.wallet.domain.request.setup.MerchantCreateRequestJson;
import com.melli.wallet.domain.response.PanelChannelResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.panel.PanelRoleListResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.operation.WalletOperationalService;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.util.Utility;
import com.melli.wallet.web.WebController;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
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
    private final ChannelRepositoryService channelRepositoryService;
    private final WalletRepositoryService walletRepositoryService;
    private final MerchantRepositoryService merchantRepositoryService;
    private final WalletOperationalService walletOperationalService;
    private final WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService;
    private final MerchantWalletAccountCurrencyRepository merchantWalletAccountCurrencyRepository;

    @Timed(description = "Time taken to update limitation general")
    @PostMapping(path = "/channel/wallet/create", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "Update general limitation (value and pattern only)")
    @PreAuthorize("hasAuthority(\"" + ResourceRepositoryService.PANEL_CHANNEL_LIST + "\")")
    public ResponseEntity<BaseResponse<String>> createChannelWallet(@Valid @RequestBody ChannelCreateRequestJson requestJson) throws InternalServiceException {

        ChannelEntity channel = channelRepositoryService.getChannel(requestJson.getUsername());
        if (channel == null) {
            log.error("channel with name ({}) is not exist", requestJson.getUsername());
            throw new InternalServiceException("channel not found", StatusRepositoryService.CHANNEL_NOT_FOUND, HttpStatus.OK);
        }

        if (channel.getWalletEntity() == null) {
            CreateWalletResponse createWalletResponse = walletOperationalService.createWallet(requestContext.getChannelEntity(), channel.getMobile(), requestJson.getNationalCode(), WalletTypeRepositoryService.CHANNEL, List.of(WalletAccountCurrencyRepositoryService.GOLD, WalletAccountCurrencyRepositoryService.RIAL),
                    List.of(WalletAccountTypeRepositoryService.WAGE));
            channel.setWalletEntity(walletRepositoryService.findById(Long.parseLong(createWalletResponse.getWalletId())));
            channelRepositoryService.save(channel);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "wage wallet created for channel"));
    }


    @Timed(description = "Time taken to update limitation general")
    @PostMapping(path = "/merchant/create", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(security = {@SecurityRequirement(name = "bearer-key")}, summary = "Update general limitation (value and pattern only)")
    @PreAuthorize("hasAuthority(\"" + ResourceRepositoryService.PANEL_CHANNEL_LIST + "\")")
    public ResponseEntity<BaseResponse<String>> createMerchantWallet(@Valid @RequestBody MerchantCreateRequestJson requestJson) throws InternalServiceException {

        CreateWalletResponse createWalletResponse = walletOperationalService.createWallet(requestContext.getChannelEntity(), requestJson.getMobileNumber(), "1111111111", WalletTypeRepositoryService.MERCHANT, List.of(WalletAccountCurrencyRepositoryService.GOLD, WalletAccountCurrencyRepositoryService.RIAL),
                List.of(WalletAccountTypeRepositoryService.NORMAL));
        MerchantEntity merchantEntity = new MerchantEntity();
        merchantEntity.setName(requestJson.getName());
        merchantEntity.setDescription("create merchant");
        merchantEntity.setMobile(requestJson.getMobileNumber());
        merchantEntity.setNationalCode("1111111111");
        merchantEntity.setEconomicalCode(requestJson.getEconomicCode());
        merchantEntity.setLogo("");
        merchantEntity.setWalletEntity(walletRepositoryService.findById(Long.parseLong(createWalletResponse.getWalletId())));
        merchantEntity.setSettlementType(1);
        merchantEntity.setStatus(1);
        merchantEntity.setCreatedBy(requestContext.getChannelEntity().getUsername());
        merchantEntity.setCreatedAt(new Date());
        merchantRepositoryService.save(merchantEntity);

        MerchantWalletAccountCurrencyEntity goldEntity = new MerchantWalletAccountCurrencyEntity();
        goldEntity.setCreatedBy(requestContext.getChannelEntity().getUsername());
        goldEntity.setCreatedAt(new Date());
        goldEntity.setMerchantEntity(merchantEntity);
        goldEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.GOLD));
        merchantWalletAccountCurrencyRepository.save(goldEntity);

        MerchantWalletAccountCurrencyEntity rialEntity = new MerchantWalletAccountCurrencyEntity();
        rialEntity.setCreatedBy(requestContext.getChannelEntity().getUsername());
        rialEntity.setCreatedAt(new Date());
        rialEntity.setMerchantEntity(merchantEntity);
        rialEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.RIAL));
        merchantWalletAccountCurrencyRepository.save(rialEntity);

        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(true, "wage wallet created for channel"));
    }



}
