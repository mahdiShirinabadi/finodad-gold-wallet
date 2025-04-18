package com.melli.wallet.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.request.wallet.*;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.service.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: WalletEndPointTest
 * Author: Mahdi Shirinabadi
 * Date: 4/7/2025
 */
@Log4j2
@DisplayName("WalletEndPoint End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WalletEndPointTest extends WalletApplicationTests {

    private static final String CREATE_WALLET_PATH = "/api/v1/wallet/create";
    private static final String GET_DATA_WALLET_PATH = "/api/v1/wallet/get";
    private static final String ACTIVE_WALLET_PATH = "/api/v1/wallet/activate";
    private static final String DEACTIVATED_WALLET_PATH = "/api/v1/wallet/deactivate";
    private static final String DELETE_WALLET_PATH = "/api/v1/wallet/delete";
    private static final String GENERATE_UUID_PATH = "/api/v1/general/generate/uuid";
    private static final String CASH_IN_PATH = "/api/v1/cash/cashIn";
    private static final String CASH_IN_INQUIRY_PATH = "/api/v1/cash/track/cashIn/";

    private static String NATIONAL_CODE_CORRECT = "0077847660";
    private static String MOBILE_CORRECT = "0077847660";

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
    private SettingGeneralCustomService settingGeneralCustomService;
    @Autowired
    private SettingGeneralService settingGeneralService;

    @Test
    @Order(1)
    @DisplayName("Initiate...")
    void initial() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);

        boolean success = setupDB();
        Assert.assertTrue(success);
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
    @Order(15)
    @DisplayName("get balance fail- wallet not found")
    void getBalanceFail() throws Exception {
        balance(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.WALLET_NOT_FOUND, false);
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
    @Order(20)
    @DisplayName("create wallet duplicate - success")
    void createWalletDuplicateSuccess() throws Exception {
        List<WalletTypeEntity> walletTypeEntityList = walletTypeService.getAll();

        WalletTypeEntity walletTypeEntity = walletTypeEntityList.stream().filter(x -> x.getName().equalsIgnoreCase(WalletTypeService.NORMAL_USER)).findFirst().orElseThrow(() -> {
            log.error("walletType not found");
            return new Exception("walletType not found");
        });

        WalletEntity walletExistEntity = walletService.findByNationalCodeAndWalletTypeId(NATIONAL_CODE_CORRECT, walletTypeEntity.getId());

        BaseResponse<CreateWalletResponse> response = createWallet(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        if (walletExistEntity.getId() != Long.parseLong(response.getData().getWalletId())) {
            log.error("create new wallet not same with created wallet!!!");
            throw new Exception("create new wallet not same with created wallet!!!");
        }
    }

    @Test
    @Order(25)
    @DisplayName("get balance success")
    void getBalanceSuccess() throws Exception {
        log.info("start get balance test");
        balance(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
    }

    @Test
    @Order(26)
    @DisplayName("cashIn success")
    void cashInFailNotPermission() throws Exception {
        String refNumber = "123456";
        String amount = "1000000";

        log.info("start get wallet data ...");
        BaseResponse<CreateWalletResponse> createWalletResponseBaseResponse = balance(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        List<WalletAccountObject> walletAccountObjectList = createWalletResponseBaseResponse.getData().getWalletAccountObjectList();
        Optional<WalletAccountObject> walletAccountObject = walletAccountObjectList.stream().filter(x -> x.getWalletAccountTypeObject().getName().equalsIgnoreCase(WalletAccountTypeService.NORMAL) && x.getWalletAccountCurrencyObject().getName().equalsIgnoreCase(WalletAccountCurrencyService.RIAL)).findFirst();
        if (walletAccountObject.isEmpty()) {
            log.error("wallet account rial not found for nationalCode ({})", NATIONAL_CODE_CORRECT);
            throw new Exception("wallet account rial not found for nationalCode ({})");
        }

        log.info("start get unique identifier for nationalCode ({})", NATIONAL_CODE_CORRECT);
        BaseResponse<UuidResponse> responseUuid = generateUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        log.info("start get account number for nationalCode ({})", NATIONAL_CODE_CORRECT);
        String uuid = responseUuid.getData().getUniqueIdentifier();
        log.info("start get balance test");
        cashIn(ACCESS_TOKEN, uuid, refNumber, amount, NATIONAL_CODE_CORRECT, walletAccountObject.get().getAccountNumber(), "", "", HttpStatus.OK, StatusService.ACCOUNT_DONT_PERMISSION_FOR_CASH_IN, false);
    }

    @Test
    @Order(27)
    @DisplayName("cashIn success")
    void cashInSuccess() throws Exception {
        String refNumber = "123456";
        String amount = "1000000";

        log.info("start get wallet data ...");
        BaseResponse<CreateWalletResponse> createWalletResponseBaseResponse = balance(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        List<WalletAccountObject> walletAccountObjectList = createWalletResponseBaseResponse.getData().getWalletAccountObjectList();
        Optional<WalletAccountObject> walletAccountObject = walletAccountObjectList.stream().filter(x -> x.getWalletAccountTypeObject().getName().equalsIgnoreCase(WalletAccountTypeService.NORMAL) && x.getWalletAccountCurrencyObject().getName().equalsIgnoreCase(WalletAccountCurrencyService.RIAL)).findFirst();
        if (walletAccountObject.isEmpty()) {
            log.error("wallet account rial not found for nationalCode ({})", NATIONAL_CODE_CORRECT);
            throw new Exception("wallet account rial not found for nationalCode ({})");
        }

        log.info("start get unique identifier for nationalCode ({})", NATIONAL_CODE_CORRECT);
        BaseResponse<UuidResponse> responseUuid = generateUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        log.info("start get account number for nationalCode ({})", NATIONAL_CODE_CORRECT);
        String uuid = responseUuid.getData().getUniqueIdentifier();
        log.info("start get balance test");
        cashIn(ACCESS_TOKEN, uuid, refNumber, amount, NATIONAL_CODE_CORRECT, walletAccountObject.get().getAccountNumber(), "", "", HttpStatus.OK, StatusService.ACCOUNT_DONT_PERMISSION_FOR_CASH_IN, false);
    }

    private void changeGeneralSetting(String settingName) {
        SettingGeneralEntity settingGeneralEntity = settingGeneralService.getSetting(settingName);
        SettingGeneralCustomEntity settingGeneralCustomEntity = settingGeneralCustomService.getSetting(settingGeneralEntity);

    }

    BaseResponse<CreateWalletResponse> balance(String token, String nationalCode, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        MockHttpServletRequestBuilder postRequest = buildGetRequest(token, GET_DATA_WALLET_PATH + "/" + nationalCode);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CreateWalletResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
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

    BaseResponse<CashInTrackResponse> inquiryCashIn(String token, String uniqueIdentifier, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        MockHttpServletRequestBuilder postRequest = buildGetRequest(token, CASH_IN_INQUIRY_PATH + "/" + uniqueIdentifier);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CashInTrackResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

}
