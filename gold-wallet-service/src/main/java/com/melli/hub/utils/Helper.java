package com.melli.hub.utils;

import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.master.entity.WalletAccountEntity;
import com.melli.hub.domain.master.entity.WalletEntity;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.base.ErrorDetail;
import com.melli.hub.domain.response.login.*;
import com.melli.hub.domain.response.channel.ChannelObject;
import com.melli.hub.domain.response.wallet.CreateWalletResponse;
import com.melli.hub.domain.response.wallet.WalletAccountObject;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.StatusService;
import com.melli.hub.service.WalletAccountService;
import com.melli.hub.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
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

    public LoginResponse fillLoginResponse(ChannelEntity channelEntity, String accessToken, Long accessTokenExpireTime, String refreshToken, Long refreshTokenExpireTime){
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setChannelObject(convertChannelEntityToChannelObject(channelEntity));
        loginResponse.setAccessTokenObject(new TokenObject(accessToken, accessTokenExpireTime));
        loginResponse.setRefreshTokenObject(new TokenObject(refreshToken, refreshTokenExpireTime));
        return loginResponse;
    }


    public SendOtpRegisterResponse fillOtpRegisterResponse(String  nationalCode, String tempUuid, Long expireTime){
        SendOtpRegisterResponse response = new SendOtpRegisterResponse();
        response.setNationalCode(nationalCode);
        response.setTempUuid(tempUuid);
        response.setOtpExpireTime(expireTime);
        return response;
    }

    public SendShahkarRegisterResponse fillShahkarRegisterResponse(String  nationalCode, String tempUuid){
        SendShahkarRegisterResponse response = new SendShahkarRegisterResponse();
        response.setNationalCode(nationalCode);
        response.setTempUuid(tempUuid);
        return response;
    }

    public CreateWalletResponse fillCreateWalletResponse(WalletEntity walletEntity, List<WalletAccountEntity> walletAccountEntityList, WalletAccountService walletAccountService) {
        CreateWalletResponse response = new CreateWalletResponse();
        List<WalletAccountObject> walletAccountObjectList = new ArrayList<>();
        response.setMobile(walletEntity.getMobile());
        response.setNationalCode(walletEntity.getNationalCode());
        response.setWalletId(String.valueOf(walletEntity.getId()));
        for(WalletAccountEntity walletAccountEntity : walletAccountEntityList){
            WalletAccountObject walletAccountObject = new WalletAccountObject();
            walletAccountObject.setWalletAccountTypeObject(SubHelper.convertWalletAccountEntityToObject(walletAccountEntity.getWalletAccountTypeEntity()));
            walletAccountObject.setWalletAccountCurrencyObject(SubHelper.convertWalletAccountCurrencyEntityToObject(walletAccountEntity.getWalletAccountCurrencyEntity()));
            walletAccountObject.setAccountNumber(walletAccountEntity.getAccountNumber());
            walletAccountObject.setStatus(String.valueOf(walletAccountEntity.getStatus()));
            walletAccountObject.setBalance(String.valueOf(walletAccountService.getBalance(walletAccountEntity.getId())));
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

    public static String encodePassword(PasswordEncoder passwordEncoder, String username, String password){
        return passwordEncoder.encode(username + "M@hd!" + password);
    }

    public static String generateHashForForgetPassword(PasswordEncoder passwordEncoder, String username){
        return passwordEncoder.encode(username + SALT_UPDATE_PASSWORD);
    }

    public static void checkGenerateHashForForgetPassword(PasswordEncoder passwordEncoder, String username, String registerHash) throws InternalServiceException {
        if(!passwordEncoder.matches(username + SALT_UPDATE_PASSWORD, registerHash)){
            log.error("invalid access to resource, hashString is changed!!!");
            throw new InternalServiceException("invalid access to resource, hashString is changed", StatusService.GENERAL_ERROR, HttpStatus.FORBIDDEN, null);
        }
    }

    public boolean notInAllowedList(String allowedList, String ip) {
        log.info("start check Ip ({}) in allowedList ({})", ip, allowedList);
        if(!StringUtils.hasText(allowedList)){
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
}