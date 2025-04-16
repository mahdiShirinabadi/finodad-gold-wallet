package com.melli.wallet.utils;

import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.base.ErrorDetail;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.domain.response.login.*;
import com.melli.wallet.domain.response.channel.ChannelObject;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.purchase.PurchaseTrackObject;
import com.melli.wallet.domain.response.purchase.PurchaseTrackResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.SettingGeneralService;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.service.WalletAccountService;
import com.melli.wallet.service.WalletService;
import com.melli.wallet.util.StringUtils;
import com.melli.wallet.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONException;
import org.json.JSONObject;
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

    public static final String FORMAT_DATE_RESPONSE = "yyyy/MM/dd HH:mm:ss";

    private static String SALT_UPDATE_PASSWORD = "108bc591f8d9e09327133e02fd64d23f67f8f52439374bb6c56510b8ad453f7d9c87860126b5811879d9a9628650a6a5";
    public static int WALLET_ACCOUNT_LENGTH = 8;

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


    public SendOtpRegisterResponse fillOtpRegisterResponse(String nationalCode, String tempUuid, Long expireTime) {
        SendOtpRegisterResponse response = new SendOtpRegisterResponse();
        response.setNationalCode(nationalCode);
        response.setTempUuid(tempUuid);
        response.setOtpExpireTime(expireTime);
        return response;
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
        response.setAccountNumber(cashInRequestEntity.getWalletAccount().getAccountNumber());
        response.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, cashInRequestEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        response.setCreateTimeTimestamp(cashInRequestEntity.getCreatedAt().getTime());

        return response;
    }

    public PurchaseTrackResponse fillPurchaseTrackResponse(PurchaseRequestEntity purchaseRequestEntity, StatusService statusService) {
        StatusEntity statusEntity = statusService.findByCode(String.valueOf(purchaseRequestEntity.getResult()));
        PurchaseTrackObject response = new PurchaseTrackObject();
        response.setNationalCode(purchaseRequestEntity.getWalletAccount().getWalletEntity().getNationalCode());
        response.setAmount(String.valueOf(purchaseRequestEntity.getAmount()));
        response.setUniqueIdentifier(purchaseRequestEntity.getRrnEntity().getUuid());
        response.setResult(String.valueOf(purchaseRequestEntity.getResult()));
        response.setDescription(statusEntity != null ? statusEntity.getPersianDescription() : "");
        response.setAccountNumber(purchaseRequestEntity.getWalletAccount().getAccountNumber());
        response.setCreateTime(DateUtils.getLocaleDate(DateUtils.FARSI_LOCALE, purchaseRequestEntity.getCreatedAt(), FORMAT_DATE_RESPONSE, false));
        response.setCreateTimeTimestamp(purchaseRequestEntity.getCreatedAt().getTime());
        response.setChannelName(purchaseRequestEntity.getChannel().getUsername());
        response.setPrice(String.valueOf(purchaseRequestEntity.getPrice()));
        response.setType(purchaseRequestEntity.getTransactionType().name());
        return new PurchaseTrackResponse(List.of(response));
    }

    public PurchaseResponse fillPurchaseResponse(PurchaseRequestEntity purchaseRequestEntity) {
        PurchaseResponse response = new PurchaseResponse();
        response.setNationalCode(purchaseRequestEntity.getWalletAccount().getWalletEntity().getNationalCode());
        response.setAmount(String.valueOf(purchaseRequestEntity.getAmount()));
        response.setUniqueIdentifier(purchaseRequestEntity.getRrnEntity().getUuid());
        response.setPrice(String.valueOf(purchaseRequestEntity.getPrice()));
        response.setType(purchaseRequestEntity.getTransactionType().name());
        return response;
    }

    public SendShahkarRegisterResponse fillShahkarRegisterResponse(String nationalCode, String tempUuid) {
        SendShahkarRegisterResponse response = new SendShahkarRegisterResponse();
        response.setNationalCode(nationalCode);
        response.setTempUuid(tempUuid);
        return response;
    }

    public CashInResponse fillCashInResponse(String nationalCode,  String uuid, long balance, String accountNumber) {
       return new CashInResponse(nationalCode, String.valueOf(balance), uuid, accountNumber);
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


    private ChannelObject convertChannelEntityToChannelObject(ChannelEntity channelEntity) {
        ChannelObject channelObject = new ChannelObject();
        channelObject.setFirstName(channelEntity.getFirstName());
        channelObject.setLastName(channelEntity.getLastName());
        channelObject.setUsername(channelEntity.getUsername());
        channelObject.setMobile(channelEntity.getMobile());
        return channelObject;
    }

    public ForgetPasswordProfileResponse fillForgetPasswordProfileResponse(String nationalCode, Long otpExpireTime, String mobileNumber, String registerHash) {
        ForgetPasswordProfileResponse response = new ForgetPasswordProfileResponse();
        response.setNationalCode(nationalCode);
        response.setOtpExpireTime(otpExpireTime);
        response.setMaskMobileNumber(mobileNumber.replaceAll(".(?=.{4})", "*"));
        response.setRegisterHash(registerHash);
        return response;
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

    public ForgetPasswordCheckOtpResponse fillForgetPasswordCheckOtpResponse(String nationalcode) {
        ForgetPasswordCheckOtpResponse response = new ForgetPasswordCheckOtpResponse();
        response.setRegisterHash(nationalcode);
        return response;
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