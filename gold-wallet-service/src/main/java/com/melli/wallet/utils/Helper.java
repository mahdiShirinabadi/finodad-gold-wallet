package com.melli.wallet.utils;

import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.base.ErrorDetail;
import com.melli.wallet.domain.response.cash.*;
import com.melli.wallet.domain.response.channel.ChannelObject;
import com.melli.wallet.domain.response.limitation.*;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.login.TokenObject;
import com.melli.wallet.domain.response.purchase.*;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.util.StringUtils;
import com.melli.wallet.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Log4j2
public class Helper {

    private final WalletTypeService walletTypeService;

    public static final String FORMAT_DATE_RESPONSE = "yyyy/MM/dd HH:mm:ss";

    private static String SALT_UPDATE_PASSWORD = "108bc591f8d9e09327133e02fd64d23f67f8f52439374bb6c56510b8ad453f7d9c87860126b5811879d9a9628650a6a5";
    public static int WALLET_ACCOUNT_LENGTH = 8;

    public Helper(WalletTypeService walletTypeService) {
        this.walletTypeService = walletTypeService;
    }

    public BaseResponse<ObjectUtils.Null> fillBaseResponse(boolean result, ErrorDetail errorDetail) {
        BaseResponse<ObjectUtils.Null> response = new BaseResponse<>(result, errorDetail);
        return response;
    }

    public Pageable getPageableConfig(SettingGeneralService settingGeneralService, Integer page, Integer size) {
        SettingGeneralEntity settingPage = settingGeneralService.getSetting(SettingGeneralService.SETTLE_DEFAULT_PAGE);
        SettingGeneralEntity settingSize = settingGeneralService.getSetting(SettingGeneralService.SETTLE_DEFAULT_SIZE);
        return PageRequest.of(page == null ? Integer.parseInt(settingPage.getValue()) : page, size == null ? Integer.parseInt(settingSize.getValue()) : size);
    }

    public LoginResponse fillLoginResponse(ChannelEntity channelEntity, String accessToken, Long accessTokenExpireTime, String refreshToken, Long refreshTokenExpireTime) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setChannelObject(convertChannelEntityToChannelObject(channelEntity));
        loginResponse.setAccessTokenObject(new TokenObject(accessToken, accessTokenExpireTime));
        loginResponse.setRefreshTokenObject(new TokenObject(refreshToken, refreshTokenExpireTime));
        return loginResponse;
    }


    public CashInTrackResponse fillCashInTrackResponse(CashInRequestEntity cashInRequestEntity, StatusService statusService) {
        StatusEntity statusEntity = statusService.findByCode(String.valueOf(cashInRequestEntity.getResult()));
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

    public CashOutTrackResponse fillCashOutTrackResponse(CashOutRequestEntity cashOutRequestEntity, StatusService statusService) {
        StatusEntity statusEntity = statusService.findByCode(String.valueOf(cashOutRequestEntity.getResult()));
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

    public PhysicalCashOutTrackResponse fillPhysicalCashOutTrackResponse(PhysicalCashOutRequestEntity physicalCashOutRequestEntity, StatusService statusService) {
        StatusEntity statusEntity = statusService.findByCode(String.valueOf(physicalCashOutRequestEntity.getResult()));
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
        return new MerchantObject(String.valueOf(merchantEntity.getId()), merchantEntity.getName(), merchantEntity.getLogo());
    }

    public PurchaseTrackResponse fillPurchaseTrackResponse(PurchaseRequestEntity purchaseRequestEntity, StatusService statusService) {
        StatusEntity statusEntity = statusService.findByCode(String.valueOf(purchaseRequestEntity.getResult()));
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

    public CashOutResponse fillCashOutResponse(String nationalCode, String uuid, String balance, String accountNumber) {
        return new CashOutResponse(nationalCode, balance, uuid, accountNumber);
    }

    public PhysicalCashOutResponse fillPhysicalCashOutResponse(String nationalCode, String uuid, String balance, String accountNumber) {
        return new PhysicalCashOutResponse(nationalCode, balance, uuid, accountNumber);
    }

    public CreateWalletResponse fillCreateWalletResponse(WalletEntity walletEntity, List<WalletAccountEntity> walletAccountEntityList, WalletAccountService walletAccountService) {
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
            walletAccountObject.setBalance(String.valueOf(walletAccountService.getBalance(walletAccountEntity.getId())));
            walletAccountObject.setStatus(walletAccountEntity.getStatus().getText());
            walletAccountObject.setStatusDescription(walletAccountEntity.getStatus().getPersianDescription());
            walletAccountObjectList.add(walletAccountObject);
        }
        response.setWalletAccountObjectList(walletAccountObjectList);
        return response;
    }

    public WalletBalanceResponse fillWalletBalanceResponse(List<WalletAccountEntity> walletAccountEntityList, WalletAccountService walletAccountService) {
        WalletBalanceResponse response = new WalletBalanceResponse();
        List<WalletAccountObject> walletAccountObjectList = new ArrayList<>();
        for (WalletAccountEntity walletAccountEntity : walletAccountEntityList) {
            WalletAccountObject walletAccountObject = new WalletAccountObject();
            walletAccountObject.setWalletAccountTypeObject(SubHelper.convertWalletAccountEntityToObject(walletAccountEntity.getWalletAccountTypeEntity()));
            walletAccountObject.setWalletAccountCurrencyObject(SubHelper.convertWalletAccountCurrencyEntityToObject(walletAccountEntity.getWalletAccountCurrencyEntity()));
            walletAccountObject.setAccountNumber(walletAccountEntity.getAccountNumber());
            walletAccountObject.setStatus(String.valueOf(walletAccountEntity.getStatus()));
            walletAccountObject.setBalance(String.valueOf(walletAccountService.getBalance(walletAccountEntity.getId())));
            walletAccountObject.setStatus(walletAccountEntity.getStatus().getText());
            walletAccountObject.setStatusDescription(walletAccountEntity.getStatus().getPersianDescription());
            walletAccountObjectList.add(walletAccountObject);
        }
        response.setWalletAccountObjectList(walletAccountObjectList);
        return response;
    }


    private ChannelObject convertChannelEntityToChannelObject(ChannelEntity channelEntity) {
        ChannelObject channelObject = new ChannelObject();
        channelObject.setFirstName(channelEntity.getFirstName());
        channelObject.setLastName(channelEntity.getLastName());
        channelObject.setUsername(channelEntity.getUsername());
        channelObject.setMobile(channelEntity.getMobile());
        return channelObject;
    }


    public WalletAccountEntity checkWalletAndWalletAccount(WalletService walletService, String nationalCode, WalletAccountService walletAccountService, String accountNumber, WalletTypeEntity walletTypeEntity) throws InternalServiceException {

        WalletEntity walletEntity = checkWallet(walletService, nationalCode, walletTypeEntity);

        WalletAccountEntity walletAccount = walletAccountService.findByWalletAndAccount(walletEntity, accountNumber);

        if (walletAccount == null) {
            log.error("error find walletAccount for account ({})", accountNumber);
            throw new InternalServiceException("walletAccount for nationalCode (" + nationalCode + ") is not found!!", StatusService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }

        if (!walletAccount.getStatus().getText().equalsIgnoreCase(WalletStatusEnum.ACTIVE.getText())) {
            log.error("wallet account {} is disable", walletAccount.getAccountNumber());
            throw new InternalServiceException("walletAccount for nationalCode (" + nationalCode + ") is not found!!", StatusService.WALLET_ACCOUNT_IS_NOT_ACTIVE, HttpStatus.OK);
        }

        return walletAccount;
    }


    public WalletAccountEntity checkWalletAndWalletAccountForNormalUser(WalletService walletService, String nationalCode, WalletAccountService walletAccountService, String accountNumber) throws InternalServiceException {

        WalletTypeEntity walletTypeEntity = walletTypeService.getAll().stream().filter(x -> x.getName().equals(WalletTypeService.NORMAL_USER)).findFirst().orElseThrow(()->{
            log.error("wallet type for ({}) not found", WalletTypeService.NORMAL_USER);
            return new InternalServiceException("wallet type not found", StatusService.WALLET_TYPE_NOT_FOUND, HttpStatus.OK);
        });

        WalletEntity walletEntity = checkWallet(walletService, nationalCode, walletTypeEntity);
        WalletAccountEntity walletAccount = walletAccountService.findByWalletAndAccount(walletEntity, accountNumber);
        if (walletAccount == null) {
            log.error("error find walletAccount for account ({})", accountNumber);
            throw new InternalServiceException("walletAccount for nationalCode (" + nationalCode + ") is not found!!", StatusService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.OK);
        }
        if (!walletAccount.getStatus().getText().equalsIgnoreCase(WalletStatusEnum.ACTIVE.getText())) {
            log.error("wallet account {} is disable", walletAccount.getAccountNumber());
            throw new InternalServiceException("walletAccount for nationalCode (" + nationalCode + ") is not found!!", StatusService.WALLET_ACCOUNT_IS_NOT_ACTIVE, HttpStatus.OK);
        }

        return walletAccount;
    }

    public  int convertDateToMonth(Date dateInput){
        String persianDate = DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, dateInput, "MM", false);
        return Integer.parseInt(persianDate);
    }

    public WalletEntity checkWallet(WalletService walletService, String nationalCode, WalletTypeEntity walletTypeEntity) throws InternalServiceException {

        WalletEntity walletEntity = walletService.findByNationalCodeAndWalletTypeId(nationalCode, walletTypeEntity.getId());

        if (walletEntity == null) {
            log.error("wallet for nationalCode ({}) is not found!", nationalCode);
            throw new InternalServiceException("wallet for nationalCode (" + nationalCode + ") is not found!!", StatusService.WALLET_NOT_FOUND, HttpStatus.OK);
        }

        if (walletEntity.getStatus().getText() != WalletStatusEnum.ACTIVE.getText()) {
            log.error("wallet for nationalCode {} is not active and status is ({})!!!", nationalCode, walletEntity.getStatus().getText());
            throw new InternalServiceException("wallet for nationalCode (" + nationalCode + ") is not active!!", StatusService.WALLET_IS_NOT_ACTIVE, HttpStatus.OK);
        }
        return walletEntity;
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
            throw new InternalServiceException("invalid access to resource, hashString is changed", StatusService.GENERAL_ERROR, HttpStatus.FORBIDDEN, null);
        }
    }

    public boolean notInAllowedList(String allowedList, String ip) {
        log.info("start check Ip ({}) in allowedList ({})", ip, allowedList);
        if (!StringUtils.hasText(allowedList)) {
            log.info("allewdList is empty !!!");
            return false;
        }
        boolean isExist = false;
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

    public static void main(String[] args) {
        PasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        System.out.println(bCryptPasswordEncoder.encode("admin" + "M@hd!" + "admin"));
    }
}