package com.melli.wallet.utils;

import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.StockRepository;
import com.melli.wallet.domain.response.PanelChannelObject;
import com.melli.wallet.domain.response.PanelChannelResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.base.ErrorDetail;
import com.melli.wallet.domain.response.cash.*;
import com.melli.wallet.domain.response.channel.ChannelObject;
import com.melli.wallet.domain.response.collateral.*;
import com.melli.wallet.domain.response.giftcard.GiftCardResponse;
import com.melli.wallet.domain.response.giftcard.GiftCardTrackResponse;
import com.melli.wallet.domain.response.giftcard.GiftCardUuidResponse;
import com.melli.wallet.domain.response.limitation.*;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.login.TokenObject;
import com.melli.wallet.domain.response.p2p.P2pTrackResponse;
import com.melli.wallet.domain.response.p2p.P2pUuidResponse;
import com.melli.wallet.domain.response.panel.PanelResourceObject;
import com.melli.wallet.domain.response.panel.PanelRoleListResponse;
import com.melli.wallet.domain.response.panel.PanelRoleObject;
import com.melli.wallet.domain.response.purchase.*;
import com.melli.wallet.domain.response.stock.StockCurrencyListResponse;
import com.melli.wallet.domain.response.stock.StockCurrencyObject;
import com.melli.wallet.domain.response.stock.StockListResponse;
import com.melli.wallet.domain.response.stock.StockObject;
import com.melli.wallet.domain.response.transaction.ReportTransactionObject;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.transaction.StatementObject;
import com.melli.wallet.domain.response.transaction.StatementResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.domain.slave.entity.*;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.util.StringUtils;
import com.melli.wallet.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;


@Service
@Log4j2
public class Helper {

    private final WalletTypeRepositoryService walletTypeRepositoryService;

    public static final String FORMAT_DATE_RESPONSE = "yyyy/MM/dd HH:mm:ss";

    private static String SALT_UPDATE_PASSWORD = "108bc591f8d9e09327133e02fd64d23f67f8f52439374bb6c56510b8ad453f7d9c87860126b5811879d9a9628650a6a5";
    public static int WALLET_ACCOUNT_LENGTH = 8;



    public Helper(WalletTypeRepositoryService walletTypeRepositoryService) {
        this.walletTypeRepositoryService = walletTypeRepositoryService;
    }

    public BaseResponse<ObjectUtils.Null> fillBaseResponse(boolean result, ErrorDetail errorDetail) {
        BaseResponse<ObjectUtils.Null> response = new BaseResponse<>(result, errorDetail);
        return response;
    }

    public Pageable getPageableConfig(SettingGeneralRepositoryService settingGeneralRepositoryService, Integer page, Integer size) {
        SettingGeneralEntity settingPage = settingGeneralRepositoryService.getSetting(SettingGeneralRepositoryService.SETTLE_DEFAULT_PAGE);
        SettingGeneralEntity settingSize = settingGeneralRepositoryService.getSetting(SettingGeneralRepositoryService.SETTLE_DEFAULT_SIZE);
        return PageRequest.of(page == null ? Integer.parseInt(settingPage.getValue()) : page, size == null ? Integer.parseInt(settingSize.getValue()) : size);
    }

    public LoginResponse fillLoginResponse(ChannelEntity channelEntity, String accessToken, Long accessTokenExpireTime, String refreshToken, Long refreshTokenExpireTime) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setChannelObject(convertChannelEntityToChannelObject(channelEntity));
        loginResponse.setAccessTokenObject(new TokenObject(accessToken, accessTokenExpireTime));
        loginResponse.setRefreshTokenObject(new TokenObject(refreshToken, refreshTokenExpireTime));
        return loginResponse;
    }

    public String generateDailyLimitationKey(WalletAccountEntity walletAccount) {
        String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);
        String key = walletAccount.getWalletEntity().getNationalCode()+"-"+ walletAccount.getWalletAccountCurrencyEntity().getName() +"-"+ currentDate;
        log.info("generate key ({})", key);
        return key;
    }

    public String generateMonthlyLimitationKey(WalletAccountEntity walletAccount) {
        String currentPersianMonth = String.valueOf(convertDateToMonth(new Date()));
        return walletAccount.getWalletEntity().getNationalCode()+"-"+ walletAccount.getWalletAccountCurrencyEntity().getName() +"-"+ currentPersianMonth;
    }


    public CashInTrackResponse fillCashInTrackResponse(CashInRequestEntity cashInRequestEntity, StatusRepositoryService statusRepositoryService) {
        StatusEntity statusEntity = statusRepositoryService.findByCode(String.valueOf(cashInRequestEntity.getResult()));
        CashInTrackResponse response = new CashInTrackResponse();
        response.setId(cashInRequestEntity.getId());
        response.setNationalCode(cashInRequestEntity.getWalletAccount().getWalletEntity().getNationalCode());
        response.setRefNumber(cashInRequestEntity.getRefNumber());
        response.setAmount(cashInRequestEntity.getAmount());
        response.setUniqueIdentifier(cashInRequestEntity.getRrnEntity().getUuid());
        response.setResult(cashInRequestEntity.getResult());
        response.setDescription(statusEntity != null ? statusEntity.getPersianDescription() : "");
        response.setWalletAccountNumber(cashInRequestEntity.getWalletAccount().getAccountNumber());
        response.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, cashInRequestEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        response.setCreateTimeTimestamp(cashInRequestEntity.getCreatedAt().getTime());

        return response;
    }

    public P2pTrackResponse fillP2pTrackResponse(Person2PersonRequestEntity entity, StatusRepositoryService statusRepositoryService) {
        StatusEntity statusEntity = statusRepositoryService.findByCode(String.valueOf(entity.getResult()));
        P2pTrackResponse response = new P2pTrackResponse();
        response.setDestWalletAccountNumber(entity.getDestinationAccountWalletEntity().getAccountNumber());
        response.setDestNationalCode(entity.getDestinationAccountWalletEntity().getWalletEntity().getNationalCode());
        response.setId(entity.getId());
        response.setNationalCode(entity.getSourceAccountWalletEntity().getWalletEntity().getNationalCode());
        response.setQuantity(String.valueOf(entity.getAmount()));
        response.setUniqueIdentifier(entity.getRrnEntity().getUuid());
        response.setResult(entity.getResult());
        response.setDescription(statusEntity != null ? statusEntity.getPersianDescription() : "");
        response.setWalletAccountNumber(entity.getSourceAccountWalletEntity().getAccountNumber());
        response.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, entity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        response.setCreateTimeTimestamp(entity.getCreatedAt().getTime());
        return response;
    }


    public GiftCardTrackResponse fillGiftCardTrackResponse(GiftCardEntity entity) {
        GiftCardTrackResponse response = new GiftCardTrackResponse();
        response.setDestWalletAccountNumber(entity.getActivatedBy());
        response.setDestNationalCode(entity.getNationalCodeBy());
        response.setId(entity.getId());
        response.setNationalCode(entity.getWalletAccountEntity().getWalletEntity().getNationalCode());
        response.setQuantity(String.valueOf(entity.getQuantity()));
        response.setUniqueIdentifier(entity.getRrnEntity().getUuid());
        response.setStatus(entity.getStatus().getText());
        response.setWalletAccountNumber(entity.getWalletAccountEntity().getAccountNumber());
        response.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, entity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        response.setCreateTimeTimestamp(entity.getCreatedAt().getTime());
        response.setActiveCode(entity.getActiveCode());
        response.setExpireTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, entity.getExpireAt(), FORMAT_DATE_RESPONSE, false));
        response.setExpireTimeTimeStamp(entity.getExpireAt().getTime());
        return response;
    }

    public P2pUuidResponse fillP2pUuidResponse(String nationalCode, String uniqueIdentifier) {
        P2pUuidResponse response = new P2pUuidResponse();
        response.setDestNationalCode(nationalCode);
        response.setUniqueIdentifier(uniqueIdentifier);
        return response;
    }

    public GiftCardUuidResponse fillGiftCardUuidResponse(String uniqueIdentifier) {
        GiftCardUuidResponse response = new GiftCardUuidResponse();
        response.setUniqueIdentifier(uniqueIdentifier);
        return response;
    }

    public CollateralTrackResponse fillCollateralTrackResponse(CreateCollateralRequestEntity createCollateralRequestEntity, List<ReleaseCollateralRequestEntity> releaseCollateralRequestEntityList,
                                                               List<IncreaseCollateralRequestEntity> increaseCollateralRequestEntityList, StatusRepositoryService statusRepositoryService) {
        CollateralTrackResponse response = new CollateralTrackResponse();
        response.setCollateralCreateTrackObject(convertToCreateCollateralTrackResponse(createCollateralRequestEntity, statusRepositoryService));
        if(CollectionUtils.isNotEmpty(releaseCollateralRequestEntityList)){
            response.setCollateralReleaseTrackObject(releaseCollateralRequestEntityList.stream().map(x->convertToCollateralRelease(x, statusRepositoryService)).toList());
        }
        if(CollectionUtils.isNotEmpty(increaseCollateralRequestEntityList)){
            response.setCollateralIncreaseTrackObject(increaseCollateralRequestEntityList.stream().map(x->convertToCollateralIncrease(x, statusRepositoryService)).toList());
        }
        return response;
    }

    private CollateralCreateTrackObject convertToCreateCollateralTrackResponse(CreateCollateralRequestEntity createCollateralRequestEntity, StatusRepositoryService statusRepositoryService){
        StatusEntity statusEntity = statusRepositoryService.findByCode(String.valueOf(createCollateralRequestEntity.getResult()));
        CollateralCreateTrackObject response = new CollateralCreateTrackObject();
        response.setUniqueIdentifier(createCollateralRequestEntity.getRrnEntity().getUuid());
        response.setCollateralCode(createCollateralRequestEntity.getCode());
        response.setQuantity(String.valueOf(createCollateralRequestEntity.getQuantity().stripTrailingZeros()));
        response.setFinalQuantityBlock(String.valueOf(createCollateralRequestEntity.getFinalBlockQuantity().stripTrailingZeros()));
        response.setCommission(String.valueOf(createCollateralRequestEntity.getCommission().stripTrailingZeros()));
        response.setNationalCode(createCollateralRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
        response.setStatus(createCollateralRequestEntity.getCollateralStatusEnum().toString());
        response.setStatusDescription(createCollateralRequestEntity.getCollateralStatusEnum().toString());
        response.setAdditionalData(createCollateralRequestEntity.getAdditionalData());
        response.setCurrency(createCollateralRequestEntity.getWalletAccountEntity().getWalletAccountCurrencyEntity().getName());
        response.setWalletAccountNumber(createCollateralRequestEntity.getWalletAccountEntity().getAccountNumber());
        response.setChannelName(createCollateralRequestEntity.getChannel().getUsername());
        response.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, createCollateralRequestEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        response.setCreateTimeTimestamp(createCollateralRequestEntity.getCreatedAt().getTime());
        response.setResult(String.valueOf(createCollateralRequestEntity.getResult()));
        response.setDescription(statusEntity != null ? statusEntity.getPersianDescription() : "");
        return response;
    }

    private CollateralReleaseTrackObject convertToCollateralRelease(ReleaseCollateralRequestEntity releaseCollateralRequestEntity, StatusRepositoryService statusRepositoryService) {
        StatusEntity statusEntity = statusRepositoryService.findByCode(String.valueOf(releaseCollateralRequestEntity.getResult()));
        CollateralReleaseTrackObject trackObject = new CollateralReleaseTrackObject();
        trackObject.setChannelName(releaseCollateralRequestEntity.getChannel().getUsername());
        trackObject.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, releaseCollateralRequestEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        trackObject.setCreateTimeTimestamp(releaseCollateralRequestEntity.getCreatedAt().getTime());
        trackObject.setQuantity(String.valueOf(releaseCollateralRequestEntity.getQuantity().stripTrailingZeros()));
        trackObject.setResult(String.valueOf(releaseCollateralRequestEntity.getResult()));
        trackObject.setCommission(String.valueOf(releaseCollateralRequestEntity.getCommission().stripTrailingZeros()));
        trackObject.setDescription(statusEntity != null ? statusEntity.getPersianDescription() : "");
        return trackObject;
    }

    private CollateralIncreaseTrackObject convertToCollateralIncrease(IncreaseCollateralRequestEntity increaseCollateralRequestEntity, StatusRepositoryService statusRepositoryService) {
        StatusEntity statusEntity = statusRepositoryService.findByCode(String.valueOf(increaseCollateralRequestEntity.getResult()));
        CollateralIncreaseTrackObject trackObject = new CollateralIncreaseTrackObject();
        trackObject.setId(String.valueOf(increaseCollateralRequestEntity.getId()));
        trackObject.setChannelName(increaseCollateralRequestEntity.getChannel().getUsername());
        trackObject.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, increaseCollateralRequestEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        trackObject.setCreateTimeTimestamp(increaseCollateralRequestEntity.getCreatedAt().getTime());
        trackObject.setQuantity(String.valueOf(increaseCollateralRequestEntity.getQuantity().stripTrailingZeros()));
        trackObject.setResult(String.valueOf(increaseCollateralRequestEntity.getResult()));
        trackObject.setCommission(String.valueOf(increaseCollateralRequestEntity.getCommission().stripTrailingZeros()));
        trackObject.setDescription(statusEntity != null ? statusEntity.getPersianDescription() : "");
        return trackObject;
    }

    public CreateCollateralResponse fillCreateCollateralResponse(String collateralCode, String nationalCode, BigDecimal quantity) {
        CreateCollateralResponse response = new CreateCollateralResponse();
        response.setCollateralCode(collateralCode);
        response.setNationalCode(nationalCode);
        response.setQuantity(quantity.toString());
        return response;
    }

    public GiftCardResponse fillGiftCardResponse(String activeCode, String quantity, Date expireTime, String currency) {
        GiftCardResponse response = new GiftCardResponse();
        response.setActiveCode(activeCode);
        response.setQuantity(String.valueOf(quantity));
        response.setCurrency(currency);
        response.setExpireTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, expireTime, FORMAT_DATE_RESPONSE, false));
        response.setExpireTimeTimeStamp(expireTime.getTime());
        return response;
    }

    public CashOutTrackResponse fillCashOutTrackResponse(ReportCashOutRequestEntity cashOutRequestEntity, StatusRepositoryService statusRepositoryService) {
        StatusEntity statusEntity = statusRepositoryService.findByCode(String.valueOf(cashOutRequestEntity.getResult()));
        CashOutTrackResponse response = new CashOutTrackResponse();
        response.setId(cashOutRequestEntity.getId());
        response.setNationalCode(cashOutRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
        response.setAmount(cashOutRequestEntity.getAmount());
        response.setUniqueIdentifier(cashOutRequestEntity.getRrnEntity().getUuid());
        response.setResult(cashOutRequestEntity.getResult());
        response.setDescription(statusEntity != null ? statusEntity.getPersianDescription() : "");
        response.setWalletAccountNumber(cashOutRequestEntity.getWalletAccountEntity().getAccountNumber());
        response.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, cashOutRequestEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        response.setCreateTimeTimestamp(cashOutRequestEntity.getCreatedAt().getTime());
        return response;
    }

    public PanelChannelResponse fillPanelChannelResponse(Page<ReportChannelEntity> channelEntityPage) {
        PanelChannelResponse response = new PanelChannelResponse();
        response.setSize(channelEntityPage.getSize());
        response.setNumber(channelEntityPage.getNumber());
        response.setTotalPages(channelEntityPage.getTotalPages());
        response.setTotalElements(channelEntityPage.getTotalElements());
        response.setList(channelEntityPage.getContent().stream().map(this::fillPanelChannelObject).toList());
        return response;
    }

    private PanelChannelObject fillPanelChannelObject(ReportChannelEntity channelEntity) {
        PanelChannelObject panelChannelObject = new PanelChannelObject();
        panelChannelObject.setId(String.valueOf(channelEntity.getId()));
        panelChannelObject.setIp(channelEntity.getIp());
        panelChannelObject.setMobile(channelEntity.getMobile());
        panelChannelObject.setUsername(channelEntity.getUsername());
        panelChannelObject.setFirstName(channelEntity.getFirstName());
        panelChannelObject.setLastName(channelEntity.getLastName());
        panelChannelObject.setPublicKey(channelEntity.getPublicKey());
        panelChannelObject.setStatus(String.valueOf(channelEntity.getStatus()));
        panelChannelObject.setCreateBy(channelEntity.getCreatedBy());
        panelChannelObject.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, channelEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        panelChannelObject.setUpdateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, channelEntity.getUpdatedAt(), FORMAT_DATE_RESPONSE, false));
        panelChannelObject.setUpdateBy(channelEntity.getUpdatedBy());
        panelChannelObject.setTrust(channelEntity.getTrust());
        panelChannelObject.setSign(channelEntity.getSign());
        return panelChannelObject;
    }

    public PhysicalCashOutTrackResponse fillPhysicalCashOutTrackResponse(ReportPhysicalCashOutRequestEntity physicalCashOutRequestEntity, StatusRepositoryService statusRepositoryService) {
        StatusEntity statusEntity = statusRepositoryService.findByCode(String.valueOf(physicalCashOutRequestEntity.getResult()));
        PhysicalCashOutTrackResponse response = new PhysicalCashOutTrackResponse();
        response.setId(physicalCashOutRequestEntity.getId());
        response.setNationalCode(physicalCashOutRequestEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
        response.setQuantity(physicalCashOutRequestEntity.getFinalQuantity());
        response.setUniqueIdentifier(physicalCashOutRequestEntity.getRrnEntity().getUuid());
        response.setResult(physicalCashOutRequestEntity.getResult());
        response.setDescription(statusEntity != null ? statusEntity.getPersianDescription() : "");
        response.setWalletAccountNumber(physicalCashOutRequestEntity.getWalletAccountEntity().getAccountNumber());
        response.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, physicalCashOutRequestEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        response.setCreateTimeTimestamp(physicalCashOutRequestEntity.getCreatedAt().getTime());
        return response;
    }

    public MerchantResponse fillMerchantResponse(List<MerchantEntity> merchantEntityList){
        MerchantResponse response = new MerchantResponse();
        response.setMerchantObjectList(merchantEntityList.stream().map(this::convertMerchantEntityToMerchantObject).toList());
        return response;
    }

    private MerchantObject convertMerchantEntityToMerchantObject(MerchantEntity merchantEntity) {
        return new MerchantObject(String.valueOf(merchantEntity.getId()), merchantEntity.getName(), merchantEntity.getLogo(), String.valueOf(merchantEntity.getStatus()));
    }

    public CollateralResponse fillCollateralResponse(List<CollateralEntity> collateralEntityList){
        CollateralResponse response = new CollateralResponse();
        response.setCollateralObjectList(collateralEntityList.stream().map(this::convertCollateralEntityToCollateralObject).toList());
        return response;
    }

    private CollateralObject convertCollateralEntityToCollateralObject(CollateralEntity collateralEntity) {
        return new CollateralObject(String.valueOf(collateralEntity.getId()), collateralEntity.getName(), collateralEntity.getLogo(), String.valueOf(collateralEntity.getStatus()),  collateralEntity.getIban());
    }

    public PurchaseTrackResponse fillPurchaseTrackResponse(ReportPurchaseRequestEntity purchaseRequestEntity, StatusRepositoryService statusRepositoryService) {
        StatusEntity statusEntity = statusRepositoryService.findByCode(String.valueOf(purchaseRequestEntity.getResult()));
        PurchaseTrackObject response = new PurchaseTrackObject();
        response.setNationalCode(purchaseRequestEntity.getWalletAccount().getWalletEntity().getNationalCode());
        response.setAmount(String.valueOf(purchaseRequestEntity.getQuantity()));
        response.setUniqueIdentifier(purchaseRequestEntity.getRrnEntity().getUuid());
        response.setResult(String.valueOf(purchaseRequestEntity.getResult()));
        response.setDescription(statusEntity != null ? statusEntity.getPersianDescription() : "");
        response.setAccountNumber(purchaseRequestEntity.getWalletAccount().getAccountNumber());
        response.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, purchaseRequestEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        response.setCreateTimeTimestamp(purchaseRequestEntity.getCreatedAt().getTime());
        response.setChannelName(purchaseRequestEntity.getChannel().getUsername());
        response.setPrice(String.valueOf(purchaseRequestEntity.getPrice()));
        response.setType(purchaseRequestEntity.getRequestTypeEntity().getName());
        return new PurchaseTrackResponse(List.of(response));
    }

    public PurchaseResponse fillPurchaseResponse(PurchaseRequestEntity purchaseRequestEntity) {
        PurchaseResponse response = new PurchaseResponse();
        response.setNationalCode(purchaseRequestEntity.getWalletAccount().getWalletEntity().getNationalCode());
        response.setAmount(String.valueOf(purchaseRequestEntity.getQuantity()));
        response.setUniqueIdentifier(purchaseRequestEntity.getRrnEntity().getUuid());
        response.setPrice(String.valueOf(purchaseRequestEntity.getPrice()));
        response.setType(purchaseRequestEntity.getRequestTypeEntity().getName());
        response.setChannelName(purchaseRequestEntity.getChannel().getUsername());
        response.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, purchaseRequestEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        response.setCreateTimeTimestamp(purchaseRequestEntity.getCreatedAt().getTime());
        return response;
    }

    public LimitationListResponse fillLimitationListResponse(List<LimitationGeneralEntity> limitationGeneralEntityList){
        LimitationListResponse response = new LimitationListResponse();
        response.setLimitationObjectList(limitationGeneralEntityList.stream().map(this::fillLimitationObject).toList());
        return response;
    }

    public LimitationObject fillLimitationObject(LimitationGeneralEntity limitationGeneralEntity) {
        return new LimitationObject(limitationGeneralEntity.getName(), limitationGeneralEntity.getAdditionalData());
    }

    public GeneralLimitationListResponse fillGeneralLimitationListResponse(Page<LimitationGeneralEntity> limitationGeneralEntityPage) {
        GeneralLimitationListResponse response = new GeneralLimitationListResponse();
        response.setSize(limitationGeneralEntityPage.getSize());
        response.setNumber(limitationGeneralEntityPage.getNumber());
        response.setTotalPages(limitationGeneralEntityPage.getTotalPages());
        response.setTotalElements(limitationGeneralEntityPage.getTotalElements());
        response.setGeneralLimitationList(limitationGeneralEntityPage.getContent().stream().map(this::fillGeneralLimitationObject).toList());
        return response;
    }

    public GeneralCustomLimitationListResponse fillGeneralCustomLimitationListResponse(Page<LimitationGeneralCustomEntity> limitationGeneralCustomEntityPage) {
        GeneralCustomLimitationListResponse response = new GeneralCustomLimitationListResponse();
        response.setSize(limitationGeneralCustomEntityPage.getSize());
        response.setNumber(limitationGeneralCustomEntityPage.getNumber());
        response.setTotalPages(limitationGeneralCustomEntityPage.getTotalPages());
        response.setTotalElements(limitationGeneralCustomEntityPage.getTotalElements());
        response.setGeneralCustomLimitationList(limitationGeneralCustomEntityPage.getContent().stream().map(this::fillGeneralCustomLimitationObject).toList());
        return response;
    }

    public GeneralLimitationObject fillGeneralLimitationObject(LimitationGeneralEntity limitationGeneralEntity) {
        GeneralLimitationObject object = new GeneralLimitationObject();
        object.setId(String.valueOf(limitationGeneralEntity.getId()));
        object.setName(limitationGeneralEntity.getName());
        object.setValue(limitationGeneralEntity.getValue());
        object.setPattern(limitationGeneralEntity.getPattern());
        object.setAdditionalData(limitationGeneralEntity.getAdditionalData());
        object.setCreateTime(limitationGeneralEntity.getCreatedAt() != null ? 
            DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, limitationGeneralEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false) : null);
        object.setCreateBy(limitationGeneralEntity.getCreatedBy());
        object.setUpdateTime(limitationGeneralEntity.getUpdatedAt() != null ? 
            DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, limitationGeneralEntity.getUpdatedAt(), FORMAT_DATE_RESPONSE, false) : null);
        object.setUpdateBy(limitationGeneralEntity.getUpdatedBy());
        return object;
    }

    public GeneralCustomLimitationObject fillGeneralCustomLimitationObject(LimitationGeneralCustomEntity limitationGeneralCustomEntity) {
        GeneralCustomLimitationObject object = new GeneralCustomLimitationObject();
        object.setId(String.valueOf(limitationGeneralCustomEntity.getId()));
        object.setLimitationGeneralId(String.valueOf(limitationGeneralCustomEntity.getLimitationGeneralEntity().getId()));
        object.setLimitationGeneralName(limitationGeneralCustomEntity.getLimitationGeneralEntity().getName());
        object.setGeneralLimitationObject(this.fillGeneralLimitationObject(limitationGeneralCustomEntity.getLimitationGeneralEntity()));
        object.setValue(limitationGeneralCustomEntity.getValue());
        object.setAdditionalData(limitationGeneralCustomEntity.getAdditionalData());
        object.setWalletLevelId(limitationGeneralCustomEntity.getWalletLevelEntity() != null ? 
            String.valueOf(limitationGeneralCustomEntity.getWalletLevelEntity().getId()) : null);
        object.setWalletLevelName(limitationGeneralCustomEntity.getWalletLevelEntity() != null ? 
            limitationGeneralCustomEntity.getWalletLevelEntity().getName() : null);
        object.setWalletAccountTypeId(limitationGeneralCustomEntity.getWalletAccountTypeEntity() != null ? 
            String.valueOf(limitationGeneralCustomEntity.getWalletAccountTypeEntity().getId()) : null);
        object.setWalletAccountTypeName(limitationGeneralCustomEntity.getWalletAccountTypeEntity() != null ? 
            limitationGeneralCustomEntity.getWalletAccountTypeEntity().getName() : null);
        object.setWalletAccountCurrencyId(limitationGeneralCustomEntity.getWalletAccountCurrencyEntity() != null ? 
            String.valueOf(limitationGeneralCustomEntity.getWalletAccountCurrencyEntity().getId()) : null);
        object.setWalletAccountCurrencyName(limitationGeneralCustomEntity.getWalletAccountCurrencyEntity() != null ? 
            limitationGeneralCustomEntity.getWalletAccountCurrencyEntity().getName() : null);
        object.setWalletTypeId(limitationGeneralCustomEntity.getWalletTypeEntity() != null ? 
            String.valueOf(limitationGeneralCustomEntity.getWalletTypeEntity().getId()) : null);
        object.setWalletTypeName(limitationGeneralCustomEntity.getWalletTypeEntity() != null ? 
            limitationGeneralCustomEntity.getWalletTypeEntity().getName() : null);
        object.setChannelId(limitationGeneralCustomEntity.getChannelEntity() != null ? 
            String.valueOf(limitationGeneralCustomEntity.getChannelEntity().getId()) : null);
        object.setChannelName(limitationGeneralCustomEntity.getChannelEntity() != null ? 
            limitationGeneralCustomEntity.getChannelEntity().getUsername() : null);
        object.setCreateTime(limitationGeneralCustomEntity.getCreatedAt() != null ? 
            DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, limitationGeneralCustomEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false) : null);
        object.setCreateBy(limitationGeneralCustomEntity.getCreatedBy());
        object.setEndTime(limitationGeneralCustomEntity.getEndTime() != null ? 
            DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, limitationGeneralCustomEntity.getEndTime(), FORMAT_DATE_RESPONSE, false) : null);
        return object;
    }

    public CashInResponse fillCashInResponse(String nationalCode,  String uuid, String balance, String accountNumber) {
       return new CashInResponse(nationalCode, balance, uuid, accountNumber);
    }

    public StatementResponse fillStatementResponse(String nationalCode, List<ReportTransactionEntity> reportTransactionEntityList) {
        StatementResponse statementResponse = new StatementResponse();
        statementResponse.setNationalCode(nationalCode);
        statementResponse.setList(reportTransactionEntityList.stream().map(this::convertToStatementObject).toList());
        return statementResponse;
    }

    public ReportTransactionResponse fillReportStatementResponse(Page<ReportTransactionEntity> reportTransactionEntityPage) {
        ReportTransactionResponse statementResponse = new ReportTransactionResponse();
        statementResponse.setNumber(reportTransactionEntityPage.getNumber());
        statementResponse.setSize(reportTransactionEntityPage.getSize());
        statementResponse.setTotalElements(reportTransactionEntityPage.getTotalElements());
        statementResponse.setTotalPages(reportTransactionEntityPage.getTotalPages());
        statementResponse.setList(reportTransactionEntityPage.stream().map(this::convertToReportStatementObject).toList());
        return statementResponse;
    }

    public CollateralListResponse fillCollateralResponse(Page<CreateCollateralRequestEntity> createCollateralRequestEntityPage, StatusRepositoryService statusRepositoryService) {
        CollateralListResponse response = new CollateralListResponse();
        response.setNumber(createCollateralRequestEntityPage.getNumber());
        response.setSize(createCollateralRequestEntityPage.getSize());
        response.setTotalElements(createCollateralRequestEntityPage.getTotalElements());
        response.setTotalPages(createCollateralRequestEntityPage.getTotalPages());
        response.setCollateralCreateTrackObjectList(createCollateralRequestEntityPage.stream().map(x->convertToCreateCollateralTrackResponse(x, statusRepositoryService)).toList());
        return response;
    }

    public StatementObject convertToStatementObject(ReportTransactionEntity reportTransactionEntity) {
        StatementObject statementObject = new StatementObject();
        statementObject.setId(String.valueOf(reportTransactionEntity.getId()));
        statementObject.setAccountNumber(reportTransactionEntity.getWalletAccountEntity().getAccountNumber());
        statementObject.setType(reportTransactionEntity.getType());
        statementObject.setUniqueIdentifier(reportTransactionEntity.getRrnEntity().getUuid());
        statementObject.setCurrency(reportTransactionEntity.getWalletAccountEntity().getWalletAccountCurrencyEntity().getName());
        statementObject.setQuantity(String.valueOf(reportTransactionEntity.getAmount()));
        statementObject.setBalance(String.valueOf(reportTransactionEntity.getAvailableBalance()));
        statementObject.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, reportTransactionEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        return statementObject;
    }

    public ReportTransactionObject convertToReportStatementObject(ReportTransactionEntity reportTransactionEntity) {
        ReportTransactionObject statementObject = new ReportTransactionObject();
        statementObject.setNationalCode(reportTransactionEntity.getWalletAccountEntity().getWalletEntity().getNationalCode());
        statementObject.setId(String.valueOf(reportTransactionEntity.getId()));
        statementObject.setAccountNumber(reportTransactionEntity.getWalletAccountEntity().getAccountNumber());
        statementObject.setType(reportTransactionEntity.getType());
        statementObject.setUniqueIdentifier(reportTransactionEntity.getRrnEntity().getUuid());
        statementObject.setCurrency(reportTransactionEntity.getWalletAccountEntity().getWalletAccountCurrencyEntity().getName());
        statementObject.setQuantity(String.valueOf(reportTransactionEntity.getAmount()));
        statementObject.setBalance(String.valueOf(reportTransactionEntity.getAvailableBalance()));
        statementObject.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, reportTransactionEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        return statementObject;
    }

    public CashOutResponse fillCashOutResponse(String nationalCode, String uuid, String balance, String accountNumber) {
        return new CashOutResponse(nationalCode, balance, uuid, accountNumber);
    }

    public PhysicalCashOutResponse fillPhysicalCashOutResponse(String nationalCode, String uuid, String balance, String accountNumber) {
        return new PhysicalCashOutResponse(nationalCode, balance, uuid, accountNumber);
    }

    public CreateWalletResponse fillCreateWalletResponse(WalletEntity walletEntity, List<WalletAccountEntity> walletAccountEntityList, WalletAccountRepositoryService walletAccountRepositoryService) {
        CreateWalletResponse response = new CreateWalletResponse();
        List<WalletAccountObject> walletAccountObjectList = new ArrayList<>();
        response.setMobile(walletEntity.getMobile());
        response.setNationalCode(walletEntity.getNationalCode());
        response.setWalletId(String.valueOf(walletEntity.getId()));
        response.setStatus(walletEntity.getStatus().getText());
        response.setStatusDescription(walletEntity.getStatus().getPersianDescription());
        for (WalletAccountEntity walletAccountEntity : walletAccountEntityList) {
            WalletAccountObject walletAccountObject = new WalletAccountObject();
            walletAccountObject.setWalletAccountTypeObject(SubHelper.convertWalletAccountEntityToObject(walletAccountEntity.getWalletAccountTypeEntity()));
            walletAccountObject.setWalletAccountCurrencyObject(SubHelper.convertWalletAccountCurrencyEntityToObject(walletAccountEntity.getWalletAccountCurrencyEntity()));
            walletAccountObject.setAccountNumber(walletAccountEntity.getAccountNumber());
            walletAccountObject.setStatus(String.valueOf(walletAccountEntity.getStatus()));
            walletAccountObject.setBalance(String.valueOf(walletAccountRepositoryService.getBalance(walletAccountEntity.getId()).getRealBalance().stripTrailingZeros()));
            walletAccountObject.setAvailableBalance(String.valueOf(walletAccountRepositoryService.getBalance(walletAccountEntity.getId()).getAvailableBalance().stripTrailingZeros()));
            walletAccountObject.setStatus(walletAccountEntity.getStatus().getText());
            walletAccountObject.setStatusDescription(walletAccountEntity.getStatus().getPersianDescription());
            walletAccountObjectList.add(walletAccountObject);
        }
        response.setWalletAccountObjectList(walletAccountObjectList);
        return response;
    }

    public WalletBalanceResponse fillWalletBalanceResponse(List<WalletAccountEntity> walletAccountEntityList, WalletAccountRepositoryService walletAccountRepositoryService) {
        WalletBalanceResponse response = new WalletBalanceResponse();
        List<WalletAccountObject> walletAccountObjectList = new ArrayList<>();
        walletAccountEntityList.sort(Comparator.comparingDouble(WalletAccountEntity::getId));
        for (WalletAccountEntity walletAccountEntity : walletAccountEntityList) {
            WalletAccountObject walletAccountObject = new WalletAccountObject();
            walletAccountObject.setWalletAccountTypeObject(SubHelper.convertWalletAccountEntityToObject(walletAccountEntity.getWalletAccountTypeEntity()));
            walletAccountObject.setWalletAccountCurrencyObject(SubHelper.convertWalletAccountCurrencyEntityToObject(walletAccountEntity.getWalletAccountCurrencyEntity()));
            walletAccountObject.setAccountNumber(walletAccountEntity.getAccountNumber());
            walletAccountObject.setStatus(String.valueOf(walletAccountEntity.getStatus()));
            walletAccountObject.setBalance(prettyBalance(walletAccountRepositoryService.getBalance(walletAccountEntity.getId()).getRealBalance().stripTrailingZeros()));
            walletAccountObject.setAvailableBalance(String.valueOf(walletAccountRepositoryService.getBalance(walletAccountEntity.getId()).getAvailableBalance().stripTrailingZeros()));
            walletAccountObject.setStatus(walletAccountEntity.getStatus().getText());
            walletAccountObject.setStatusDescription(walletAccountEntity.getStatus().getPersianDescription());
            walletAccountObjectList.add(walletAccountObject);
        }
        response.setWalletAccountObjectList(walletAccountObjectList);
        return response;
    }

    private String prettyBalance(BigDecimal num){
        DecimalFormat df = new DecimalFormat("0.#####");
        return df.format(num);
    }


    private ChannelObject convertChannelEntityToChannelObject(ChannelEntity channelEntity) {
        ChannelObject channelObject = new ChannelObject();
        channelObject.setFirstName(channelEntity.getFirstName());
        channelObject.setLastName(channelEntity.getLastName());
        channelObject.setUsername(channelEntity.getUsername());
        channelObject.setMobile(channelEntity.getMobile());
        return channelObject;
    }


    public WalletAccountEntity checkWalletAndWalletAccount(WalletRepositoryService walletRepositoryService, String nationalCode, WalletAccountRepositoryService walletAccountRepositoryService, String accountNumber, WalletTypeEntity walletTypeEntity) throws InternalServiceException {

        WalletEntity walletEntity = checkWallet(walletRepositoryService, nationalCode, walletTypeEntity);

        WalletAccountEntity walletAccount = walletAccountRepositoryService.findByWalletAndAccount(walletEntity, accountNumber);

        if (walletAccount == null) {
            log.error("error find walletAccount for account ({})", accountNumber);
            throw new InternalServiceException("walletAccount for nationalCode (" + nationalCode + ") is not found!!", StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }

        if (!walletAccount.getStatus().getText().equalsIgnoreCase(WalletStatusEnum.ACTIVE.getText())) {
            log.error("wallet account {} is disable", walletAccount.getAccountNumber());
            throw new InternalServiceException("walletAccount for nationalCode (" + nationalCode + ") is not found!!", StatusRepositoryService.WALLET_ACCOUNT_IS_NOT_ACTIVE, HttpStatus.OK);
        }

        return walletAccount;
    }


    public WalletAccountEntity checkWalletAndWalletAccountForNormalUser(WalletRepositoryService walletRepositoryService, String nationalCode, WalletAccountRepositoryService walletAccountRepositoryService, String accountNumber) throws InternalServiceException {

        WalletTypeEntity walletTypeEntity = walletTypeRepositoryService.getAll().stream().filter(x -> x.getName().equals(WalletTypeRepositoryService.NORMAL_USER)).findFirst().orElseThrow(()->{
            log.error("wallet type for ({}) not found", WalletTypeRepositoryService.NORMAL_USER);
            return new InternalServiceException("wallet type not found", StatusRepositoryService.WALLET_TYPE_NOT_FOUND, HttpStatus.OK);
        });

        WalletEntity walletEntity = checkWallet(walletRepositoryService, nationalCode, walletTypeEntity);
        WalletAccountEntity walletAccount = walletAccountRepositoryService.findByWalletAndAccount(walletEntity, accountNumber);
        if (walletAccount == null) {
            log.error("error find walletAccount for account ({})", accountNumber);
            throw new InternalServiceException("walletAccount for nationalCode (" + nationalCode + ") is not found!!", StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }
        if (!walletAccount.getStatus().getText().equalsIgnoreCase(WalletStatusEnum.ACTIVE.getText())) {
            log.error("wallet account {} is disable", walletAccount.getAccountNumber());
            throw new InternalServiceException("walletAccount for nationalCode (" + nationalCode + ") is not found!!", StatusRepositoryService.WALLET_ACCOUNT_IS_NOT_ACTIVE, HttpStatus.OK);
        }

        return walletAccount;
    }

    public  int convertDateToMonth(Date dateInput){
        String persianDate = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, dateInput, "MM", false);
        return Integer.parseInt(persianDate);
    }

    public WalletEntity checkWallet(WalletRepositoryService walletRepositoryService, String nationalCode, WalletTypeEntity walletTypeEntity) throws InternalServiceException {

        WalletEntity walletEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId(nationalCode, walletTypeEntity.getId());

        if (walletEntity == null) {
            log.error("wallet for nationalCode ({}) is not found!", nationalCode);
            throw new InternalServiceException("wallet for nationalCode (" + nationalCode + ") is not found!!", StatusRepositoryService.WALLET_NOT_FOUND, HttpStatus.OK);
        }

        if (!Objects.equals(walletEntity.getStatus().getText(), WalletStatusEnum.ACTIVE.getText())) {
            log.error("wallet for nationalCode {} is not active and status is ({})!!!", nationalCode, walletEntity.getStatus().getText());
            throw new InternalServiceException("wallet for nationalCode (" + nationalCode + ") is not active!!", StatusRepositoryService.WALLET_IS_NOT_ACTIVE, HttpStatus.OK);
        }
        return walletEntity;
    }

    public PanelRoleListResponse fillChannelRoleListResponse(Page<ReportChannelRoleEntity> page) {
        PanelRoleListResponse response = new PanelRoleListResponse();
        response.setSize(page.getSize());
        response.setNumber(page.getNumber());
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        response.setList(page.getContent().stream().map(this::fillChannelRoleListObject).toList());
        return response;
    }

    public StockCurrencyListResponse fillStockCurrencyList(List<StockRepository.AggregationStockByCurrencyDTO> aggregationStockByCurrencyDTOList,
                                                           WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService){
        StockCurrencyListResponse response = new StockCurrencyListResponse();
        List<StockCurrencyObject> stockCurrencyObjectList = new ArrayList<>();
        for(StockRepository.AggregationStockByCurrencyDTO aggregationStockByCurrencyDTO : aggregationStockByCurrencyDTOList){
            try{
                WalletAccountCurrencyEntity walletAccountCurrencyEntity= walletAccountCurrencyRepositoryService.getById(Long.parseLong(aggregationStockByCurrencyDTO.getCurrency()));
                stockCurrencyObjectList.add(new StockCurrencyObject(aggregationStockByCurrencyDTO.getBalance(), walletAccountCurrencyEntity.getName()));
            }catch (InternalServiceException ex){
                log.error("currency with Id ({}) not found", aggregationStockByCurrencyDTO.getCurrency(), ex);
            }
        }
        response.setStockCurrencyObjectList(stockCurrencyObjectList);
        return response;
    }

    public StockListResponse fillStockList(List<StockRepository.AggregationStockDTO> aggregationStockDTOList){
        StockListResponse response = new StockListResponse();
        List<StockObject> stockObjectList = new ArrayList<>();
        for(StockRepository.AggregationStockDTO aggregationStockDTO : aggregationStockDTOList){
            stockObjectList.add(new StockObject(aggregationStockDTO.getId(), aggregationStockDTO.getBalance(), aggregationStockDTO.getCode()));
        }
        response.setStockObjectList(stockObjectList);
        return response;
    }

    private PanelRoleObject fillChannelRoleListObject(ReportChannelRoleEntity channelRoleEntity) {
        PanelRoleObject panelOperatorRoleObject = new PanelRoleObject();
        ReportRoleEntity roleEntity = channelRoleEntity.getRoleEntity();
        panelOperatorRoleObject.setId(String.valueOf(roleEntity.getId()));
        panelOperatorRoleObject.setName(roleEntity.getName());
        panelOperatorRoleObject.setCreatedTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, roleEntity.getCreatedAt(), DateUtils.DEFAULT_DATE_TIME_FORMAT, false));
        panelOperatorRoleObject.setCreatedBy(roleEntity.getCreatedBy());
        panelOperatorRoleObject.setUpdatedTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, roleEntity.getUpdatedAt(), DateUtils.DEFAULT_DATE_TIME_FORMAT, false));
        panelOperatorRoleObject.setUpdatedBy(roleEntity.getUpdatedBy());
        List<PanelResourceObject> resourceObjects = roleEntity.getResources().stream().map(this::fillPanelResourceListObject).toList();
        panelOperatorRoleObject.setResources(resourceObjects);
        return panelOperatorRoleObject;
    }

    private PanelResourceObject fillPanelResourceListObject(ReportResourceEntity resourceEntity) {
        PanelResourceObject panelOperatorRoleObject = new PanelResourceObject();
        panelOperatorRoleObject.setId(String.valueOf(resourceEntity.getId()));
        panelOperatorRoleObject.setName(resourceEntity.getName());
        panelOperatorRoleObject.setCreatedAt(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, resourceEntity.getCreatedAt(), DateUtils.DEFAULT_DATE_FORMAT, false));
        panelOperatorRoleObject.setCreatedBy(resourceEntity.getCreatedBy());
        panelOperatorRoleObject.setUpdatedAt(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, resourceEntity.getUpdatedAt(), DateUtils.DEFAULT_DATE_FORMAT, false));
        panelOperatorRoleObject.setUpdatedBy(resourceEntity.getUpdatedBy());
        return panelOperatorRoleObject;
    }


    public static String findInListMapValueByKey(List<Map<String, String>> listOfMaps, String key) {
        return listOfMaps.stream().filter(map -> map.containsKey(key) && StringUtils.hasText(map.get(key))).map(map -> map.get(key)).findFirst().orElse(null); // Return null if no match is found
    }

    public static boolean isValidJson(String json) {
        try {
            new JSONObject(json);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    public static String encodePassword(PasswordEncoder passwordEncoder, String username, String password) {
        return passwordEncoder.encode(username + "M@hd!" + password);
    }

    public static String generateHashForForgetPassword(PasswordEncoder passwordEncoder, String username) {
        return passwordEncoder.encode(username + SALT_UPDATE_PASSWORD);
    }

    public static void checkGenerateHashForForgetPassword(PasswordEncoder passwordEncoder, String username, String registerHash) throws InternalServiceException {
        if (!passwordEncoder.matches(username + SALT_UPDATE_PASSWORD, registerHash)) {
            log.error("invalid access to resource, hashString is changed!!!");
            throw new InternalServiceException("invalid access to resource, hashString is changed", StatusRepositoryService.GENERAL_ERROR, HttpStatus.FORBIDDEN, null);
        }
    }

    public boolean notInAllowedList(String allowedList, String ip) {
        log.info("start check Ip ({}) in allowedList ({})", ip, allowedList);
        if (!StringUtils.hasText(allowedList)) {
            log.info("allewdList is empty !!!");
            return false;
        }
        boolean isExist;
        List<String> ipList = new LinkedList<>(Arrays.asList(allowedList.split(";")));
        for (String validIp : ipList) {
            IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(validIp);
            isExist = ipAddressMatcher.matches(ip);
            if (isExist) {
                log.info("ip ({}) is match with value ({})", ip, validIp);
                return false;
            }
        }
        log.info("result ({}) for Ip ({})", false, ip);
        return true;
    }

    public static Integer generateRandomNumber() {
        Random r = new Random(System.currentTimeMillis());
        return ((1 + r.nextInt(2)) * 10000 + r.nextInt(10000));
    }

    @Profile("dev")
    public static void main(String[] args) {
        PasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
//        System.out.println(bCryptPasswordEncoder.encode("admin" + "M@hd!" + "admin"));
    }
}