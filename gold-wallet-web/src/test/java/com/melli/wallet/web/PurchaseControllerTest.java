package com.melli.wallet.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.config.FlywayConfig;
import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.master.entity.WalletTypeEntity;
import com.melli.wallet.domain.request.wallet.*;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.service.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: WalletEndPointTest
 * Author: Mahdi Shirinabadi
 * Date: 4/7/2025
 */
@Log4j2
@DisplayName("PurchaseControllerTest End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PurchaseControllerTest extends WalletApplicationTests {

    private static final String CREATE_WALLET_PATH = "/api/v1/wallet/create";
    private static final String GET_DATA_WALLET_PATH = "/api/v1/wallet/get";
    private static final String ACTIVE_WALLET_PATH = "/api/v1/wallet/activate";
    private static final String DEACTIVATED_WALLET_PATH = "/api/v1/wallet/deactivate";
    private static final String DELETE_WALLET_PATH = "/api/v1/wallet/delete";
    private static final String GENERATE_UUID_PATH = "/api/v1/general/generate/uuid";
    private static final String PURCHASE_GENERATE_UUID_PATH = "/api/v1/purchase/generate/uuid";
    private static final String BUY_IN_PATH = "/api/v1/purchase/buy";
    private static final String SELL_IN_PATH = "/api/v1/purchase/sell";
    private static final String PURCHASE_INQUIRY_IN_PATH = "/api/v1/purchase/inquiry";

    private static final String CASH_IN_GENERATE_UUID_PATH = "/api/v1/cash/charge/generate/uuid";
    private static final String CASH_IN_PATH = "/api/v1/cash/charge";
    private static final String CASH_IN_INQUIRY_PATH = "/api/v1/cash/charge/inquiry";

    private static final String NATIONAL_CODE_CORRECT = "0077847660";
    private static final String NATIONAL_CODE_INCORRECT = "0077847661";
    private static final String NATIONAL_CODE_CORRECT_BINA = "1292093781";
    private static final String NATIONAL_CODE_LENGTH_LESS_THAN_STANDARD = "0077847661";
    private static final String NATIONAL_CODE_LENGTH_BIGGER_THAN_STANDARD = "00778476611";
    private static final String MOBILE_CORRECT = "09124162337";

    private static MockMvc mockMvc;
    private static String ACCESS_TOKEN;
    private static String REFRESH_TOKEN;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private WalletAccountService walletAccountService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletTypeService walletTypeService;
    @Autowired
    private LimitationGeneralCustomService limitationGeneralCustomService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private Flyway flyway;
    @Autowired
    private CacheClearService cacheClearService;



    @Test
    @Order(2)
    @DisplayName("Initiate cache...")
    void initial() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);
        log.info("start cleaning initial values in test DB for purchase");
        flyway.clean();
        flyway.migrate();
        log.info("start cleaning initial values in test DB for purchase");
        cacheClearService.clearCache();
    }

    @Test
    @Order(10)
    @DisplayName("Channel login successfully")
    void login_success() throws Exception {
        log.info("start login_success test");
        BaseResponse<LoginResponse> response = login(mockMvc, USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.OK,
                StatusService.SUCCESSFUL, true);
        ACCESS_TOKEN = response.getData().getAccessTokenObject().getToken();
        REFRESH_TOKEN = response.getData().getRefreshTokenObject().getToken();
    }

    @Test
    @Order(20)
    @DisplayName("create wallet- success")
    void createWalletSuccess() throws Exception {
        BaseResponse<CreateWalletResponse> response = createWallet(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        WalletEntity walletEntity = walletService.findById(Long.parseLong(response.getData().getWalletId()));
        if (!walletEntity.getNationalCode().equalsIgnoreCase(NATIONAL_CODE_CORRECT)) {
            log.error("wallet create not same with national code ({})", NATIONAL_CODE_CORRECT);
            throw new Exception("wallet create not same with national code ({})");
        }
    }



    @Test
    @Order(40)
    @DisplayName("cashIn success")
    void cashInSuccess() throws Exception {
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String refNumber = new Date().getTime() + "";

        String amount = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if ("false".equalsIgnoreCase(value)) {
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true", "test cashInFailMinAmount");
        }
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifier1);
        cashIn(ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, amount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", HttpStatus.OK, StatusService.SUCCESSFUL, true);
    }


    BaseResponse<CreateWalletResponse> balance(String token, String nationalCode, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        MockHttpServletRequestBuilder postRequest = buildGetRequest(token, GET_DATA_WALLET_PATH + "/" + nationalCode);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CreateWalletResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    private WalletAccountObject getAccountNumber(String nationalCode, String walletAccountType, String walletAccountCurrency) throws Exception {
        log.info("start get wallet data ...");
        BaseResponse<CreateWalletResponse> createWalletResponseBaseResponse = balance(ACCESS_TOKEN, nationalCode, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        List<WalletAccountObject> walletAccountObjectList = createWalletResponseBaseResponse.getData().getWalletAccountObjectList();
        Optional<WalletAccountObject> walletAccountObjectOptional = walletAccountObjectList.stream().filter(x -> x.getWalletAccountTypeObject().getName().equalsIgnoreCase(walletAccountType)
                        && x.getWalletAccountCurrencyObject().getName().equalsIgnoreCase(walletAccountCurrency))
                .findFirst();
        if (walletAccountObjectOptional.isEmpty()) {
            log.error("wallet account rial not found for nationalCode ({})", NATIONAL_CODE_CORRECT);
            throw new Exception("wallet account rial not found for nationalCode ({})");
        }
        return walletAccountObjectOptional.get();
    }


    BaseResponse<CreateWalletResponse> createWallet(String token, String nationalCode, String mobile, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        CreateWalletRequestJson createWalletRequestJson = new CreateWalletRequestJson();
        createWalletRequestJson.setMobile(mobile);
        createWalletRequestJson.setNationalCode(nationalCode);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, CREATE_WALLET_PATH, mapToJson(createWalletRequestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CreateWalletResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<ObjectUtils.Null> deactivatedWallet(String token, String id, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        DeactivatedWalletRequestJson requestJson = new DeactivatedWalletRequestJson();
        requestJson.setId(id);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, DEACTIVATED_WALLET_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<ObjectUtils.Null>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<ObjectUtils.Null> activeWallet(String token, String id, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        ActiveWalletRequestJson requestJson = new ActiveWalletRequestJson();
        requestJson.setId(id);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, ACTIVE_WALLET_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<ObjectUtils.Null>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<ObjectUtils.Null> deleteWallet(String token, String id, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        DeleteWalletRequestJson requestJson = new DeleteWalletRequestJson();
        requestJson.setId(id);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, DELETE_WALLET_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<ObjectUtils.Null>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<UuidResponse> generateUniqueIdentifier(String token, String nationalCode, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        GenerateUuidRequestJson requestJson = new GenerateUuidRequestJson();
        requestJson.setNationalCode(nationalCode);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, GENERATE_UUID_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<UuidResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<UuidResponse> generateCashInUniqueIdentifier(String token, String nationalCode, String amount, String accountNumber, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        CashInGenerateUuidRequestJson requestJson = new CashInGenerateUuidRequestJson();
        requestJson.setNationalCode(nationalCode);
        requestJson.setAmount(amount);
        requestJson.setAccountNumber(accountNumber);

        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, CASH_IN_GENERATE_UUID_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<UuidResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<UuidResponse> generatePurchaseUniqueIdentifier(String token, String nationalCode, String amount, String accountNumber, String type, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        PurchaseGenerateUuidRequestJson requestJson = new PurchaseGenerateUuidRequestJson();
        requestJson.setNationalCode(nationalCode);
        requestJson.setPrice(amount);
        requestJson.setAccountNumber(accountNumber);
        requestJson.setPurchaseType(type);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, PURCHASE_GENERATE_UUID_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<UuidResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<CashInResponse> cashIn(String token, String uniqueIdentifier, String referenceNumber, String amount, String nationalCode, String accountNumber, String sign, String additionalData, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        CashInWalletRequestJson requestJson = new CashInWalletRequestJson();
        requestJson.setUniqueIdentifier(uniqueIdentifier);
        requestJson.setReferenceNumber(referenceNumber);
        requestJson.setAmount(amount);
        requestJson.setNationalCode(nationalCode);
        requestJson.setAdditionalData(additionalData);
        requestJson.setAccountNumber(accountNumber);
        requestJson.setSign(sign);
        requestJson.setNationalCode(nationalCode);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, CASH_IN_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CashInResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<PurchaseResponse> buy(String token, String uniqueIdentifier, String amount, String price, String commissionCurrency, String commission, String nationalCode, String currency, String merchantId, String walletAccountNumber, String sign, String additionalData, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        BuyWalletRequestJson requestJson = new BuyWalletRequestJson();
        requestJson.setUniqueIdentifier(uniqueIdentifier);
        requestJson.setAmount(amount);
        requestJson.setPrice(price);
        requestJson.setCommissionObject(new CommissionObject(commissionCurrency, commission));
        requestJson.setNationalCode(nationalCode);
        requestJson.setCurrency(currency);
        requestJson.setMerchantId(merchantId);
        requestJson.setWalletAccountNumber(walletAccountNumber);
        requestJson.setAdditionalData(additionalData);
        requestJson.setSign(sign);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, BUY_IN_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<PurchaseResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<PurchaseResponse> sell(String token, String uniqueIdentifier, String amount, String price, String commissionCurrency, String commission, String nationalCode, String currency, String merchantId, String walletAccountNumber, String sign, String additionalData, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        SellWalletRequestJson requestJson = new SellWalletRequestJson();
        requestJson.setUniqueIdentifier(uniqueIdentifier);
        requestJson.setAmount(amount);
        requestJson.setPrice(price);
        requestJson.setCommissionObject(new CommissionObject(commissionCurrency, commission));
        requestJson.setNationalCode(nationalCode);
        requestJson.setCurrency(currency);
        requestJson.setMerchantId(merchantId);
        requestJson.setWalletAccountNumber(walletAccountNumber);
        requestJson.setAdditionalData(additionalData);
        requestJson.setSign(sign);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, SELL_IN_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<PurchaseResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<CashInTrackResponse> inquiryCashIn(String token, String uniqueIdentifier, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        MockHttpServletRequestBuilder postRequest = buildGetRequest(token, CASH_IN_INQUIRY_PATH + "/" + uniqueIdentifier);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CashInTrackResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<CashInTrackResponse> inquiryPurchase(String token, String uniqueIdentifier, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        MockHttpServletRequestBuilder postRequest = buildGetRequest(token, PURCHASE_INQUIRY_IN_PATH + "/" + uniqueIdentifier);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CashInTrackResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    private String getSettingValue(String channelName, String limitationName, String accountNumber) throws Exception {
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        return limitationGeneralCustomService.getSetting(channelService.getChannel(channelName),
                limitationName, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity());
    }

}
