package com.melli.wallet;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.melli.wallet.config.FlywayConfig;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.request.login.LoginRequestJson;
import com.melli.wallet.domain.request.login.RefreshTokenRequestJson;
import com.melli.wallet.domain.request.wallet.*;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.purchase.MerchantResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.service.ChannelService;
import com.melli.wallet.service.LimitationGeneralCustomService;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.service.WalletAccountService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
@SpringBootTest(classes = WalletApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ExtendWith(SpringExtension.class)
public class WalletApplicationTests {

    private static final String LOGIN_PATH = "/api/v1/auth/login";

    public static final String USERNAME_CORRECT = "admin";
    public static final String USERNAME_INCORRECT = "admin12";
    public static final String PASSWORD_CORRECT = "admin";

    private static final String CREATE_WALLET_PATH = "/api/v1/wallet/create";
    private static final String GET_DATA_WALLET_PATH = "/api/v1/wallet/get";
    private static final String ACTIVE_WALLET_PATH = "/api/v1/wallet/activate";
    private static final String DEACTIVATED_WALLET_PATH = "/api/v1/wallet/deactivate";
    private static final String DELETE_WALLET_PATH = "/api/v1/wallet/delete";
    private static final String GENERATE_UUID_PATH = "/api/v1/general/generate/uuid";
    private static final String BUY_GENERATE_UUID_PATH = "/api/v1/purchase/buy/generate/uuid";
    private static final String SELL_GENERATE_UUID_PATH = "/api/v1/purchase/sell/generate/uuid";
    private static final String BUY_IN_PATH = "/api/v1/purchase/buy";
    private static final String BUY_DIRECT_IN_PATH = "/api/v1/purchase/buy/direct";
    private static final String SELL_IN_PATH = "/api/v1/purchase/sell";
    private static final String PURCHASE_INQUIRY_IN_PATH = "/api/v1/purchase/inquiry";

    private static final String CASH_IN_GENERATE_UUID_PATH = "/api/v1/cash/generate/uuid";
    private static final String CASH_IN_PATH = "/api/v1/cash/charge";
    private static final String CASH_IN_INQUIRY_PATH = "/api/v1/cash/charge/inquiry";

    private static final String MERCHANT_GET_IN_PATH = "/api/v1/merchant/list";

    private static final String REFRESHTOKEN_PATH = "/api/v1/auth/refresh";
    private static final String LOGOUT_PATH = "/api/v1/auth/logout";

    private static final String NATIONAL_CODE_CORRECT = "0077847660";
    private static final String NATIONAL_CODE_INCORRECT = "0077847661";
    private static final String NATIONAL_CODE_CORRECT_BINA = "1292093781";
    private static final String NATIONAL_CODE_LENGTH_LESS_THAN_STANDARD = "0077847661";
    private static final String NATIONAL_CODE_LENGTH_BIGGER_THAN_STANDARD = "00778476611";
    private static final String MOBILE_CORRECT = "09124162337";

    @Autowired
    private WebApplicationContext webApplicationContext;



    private static MockMvc mockMvc;

    public static final ObjectMapper objectMapper = new ObjectMapper();

    public String mapToJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    @Autowired
    private FlywayConfig flywayConfig;


    public ResultMatcher buildErrorCodeMatch(int errorCode) {
        if (errorCode == StatusService.SUCCESSFUL) {
            return jsonPath("$.errorDetail").doesNotExist();
        } else {
            return jsonPath("$.errorDetail.code").value(errorCode);
        }
    }

    public MockHttpServletRequestBuilder buildPostRequest(String token, String path) {
        return buildPostRequest(token, path, null, null);
    }

    public MockHttpServletRequestBuilder buildPostRequest(String token, String path, String body) {
        return buildPostRequest(token, path, body, null);
    }

    public MockHttpServletRequestBuilder buildPostRequest(String token, String path, String body, String clientIp) {
        log.info("start with path({}), token({}), body({}), clientIp({})", path, token, body, clientIp);
        MockHttpServletRequestBuilder requestBuilder = post(path);
        if (StringUtils.isNotBlank(token)) {
            requestBuilder.header("Authorization", "bearer " + token);
        }
        if (StringUtils.isNotBlank(clientIp)) {
            requestBuilder.remoteAddress(clientIp);
        }
        if (StringUtils.isNotBlank(body)) {
            requestBuilder.contentType(MediaType.APPLICATION_JSON);
            requestBuilder.content(body);
        }
        return requestBuilder;
    }

    public MockHttpServletRequestBuilder buildGetRequest(String token, String path, Object... uriVariables) {
        MockHttpServletRequestBuilder requestBuilder = get(path, uriVariables);
        if (StringUtils.isNotBlank(token)) {
            requestBuilder.header("Authorization", "bearer " + token);
        }
        return requestBuilder;
    }

    public MockHttpServletRequestBuilder buildGetRequest(String token, String path, Map<String, String> params, Object... uriVariables) {
        MockHttpServletRequestBuilder requestBuilder = get(path, uriVariables);
        if (StringUtils.isNotBlank(token)) {
            requestBuilder.header("Authorization", "bearer " + token);
        }
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                requestBuilder.param(entry.getKey(), entry.getValue());
            }
        }
        return requestBuilder;
    }

    @Test
    public void contextLoads() {
        log.info("contextLoads");
    }

    public void setupDB() {
        log.info("start cleaning initial values in test DB");
        flywayConfig.cleanMigrate();
    }

    public String performTest(MockMvc mockMvc, MockHttpServletRequestBuilder getRequest, HttpStatus httpStatus, boolean success, int errorCode) throws Exception {
        ResultMatcher errorCodeMatch = buildErrorCodeMatch(errorCode);

        MvcResult mvcResult = mockMvc.perform(getRequest)
                .andDo(print())
                .andExpect(status().is(httpStatus.value()))
                .andExpect(jsonPath("$.success").value(success))
                .andExpect(errorCodeMatch)
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("finish with response({})", response);
        return response;
    }

    public BaseResponse<ObjectUtils.Null> logout(MockMvc mockMvc, String accessToken, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        MockHttpServletRequestBuilder postRequest = buildPostRequest(accessToken, LOGOUT_PATH);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<ObjectUtils.Null>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    public BaseResponse<LoginResponse> refresh(MockMvc mockMvc, String refreshToken, String username, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        RefreshTokenRequestJson refreshTokenRequestJson = new RefreshTokenRequestJson();
        refreshTokenRequestJson.setRefreshToken(refreshToken);
        refreshTokenRequestJson.setUsername(username);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(null, REFRESHTOKEN_PATH, mapToJson(refreshTokenRequestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<LoginResponse>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    public BaseResponse<LoginResponse> login(MockMvc mockMvc, String username, String password, HttpStatus httpStatus, int errorCode,
                                             boolean success) throws Exception {
        LoginRequestJson requestJson = new LoginRequestJson();
        requestJson.setUsername(username);
        requestJson.setPassword(password);
        MockHttpServletRequestBuilder postRequest = buildPostRequest("", LOGIN_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<LoginResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }


    public BaseResponse<CreateWalletResponse> createWallet(MockMvc mockMvc, String token, String nationalCode, String mobile, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        CreateWalletRequestJson createWalletRequestJson = new CreateWalletRequestJson();
        createWalletRequestJson.setMobile(mobile);
        createWalletRequestJson.setNationalCode(nationalCode);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, CREATE_WALLET_PATH, mapToJson(createWalletRequestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CreateWalletResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    public BaseResponse<ObjectUtils.Null> deactivatedWallet(MockMvc mockMvc, String token, String id, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        DeactivatedWalletRequestJson requestJson = new DeactivatedWalletRequestJson();
        requestJson.setId(id);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, DEACTIVATED_WALLET_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<ObjectUtils.Null>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    public BaseResponse<ObjectUtils.Null> activeWallet(MockMvc mockMvc, String token, String id, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        ActiveWalletRequestJson requestJson = new ActiveWalletRequestJson();
        requestJson.setId(id);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, ACTIVE_WALLET_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<ObjectUtils.Null>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    public BaseResponse<ObjectUtils.Null> deleteWallet(MockMvc mockMvc, String token, String id, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        DeleteWalletRequestJson requestJson = new DeleteWalletRequestJson();
        requestJson.setId(id);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, DELETE_WALLET_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<ObjectUtils.Null>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    public BaseResponse<UuidResponse> generateUniqueIdentifier(MockMvc mockMvc, String token, String nationalCode, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        GenerateUuidRequestJson requestJson = new GenerateUuidRequestJson();
        requestJson.setNationalCode(nationalCode);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, GENERATE_UUID_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<UuidResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    public BaseResponse<UuidResponse> generateCashInUniqueIdentifier(MockMvc mockMvc, String token, String nationalCode, String amount, String accountNumber, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        CashGenerateUuidRequestJson requestJson = new CashGenerateUuidRequestJson();
        requestJson.setNationalCode(nationalCode);
        requestJson.setAmount(amount);
        requestJson.setAccountNumber(accountNumber);

        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, CASH_IN_GENERATE_UUID_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<UuidResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    public BaseResponse<UuidResponse> generateSellUniqueIdentifier(MockMvc mockMvc, String token, String nationalCode, String amount, String accountNumber, String currency, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        SellGenerateUuidRequestJson requestJson = new SellGenerateUuidRequestJson();
        requestJson.setNationalCode(nationalCode);
        requestJson.setQuantity(amount);
        requestJson.setAccountNumber(accountNumber);
        requestJson.setCurrency(currency);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, SELL_GENERATE_UUID_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<UuidResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    public BaseResponse<CashInResponse> cashIn(MockMvc mockMvc, String token, String uniqueIdentifier, String referenceNumber, String amount, String nationalCode, String accountNumber, String sign, String additionalData, String cashInType, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        CashInWalletRequestJson requestJson = new CashInWalletRequestJson();
        requestJson.setUniqueIdentifier(uniqueIdentifier);
        requestJson.setReferenceNumber(referenceNumber);
        requestJson.setAmount(amount);
        requestJson.setNationalCode(nationalCode);
        requestJson.setAdditionalData(additionalData);
        requestJson.setAccountNumber(accountNumber);
        requestJson.setSign(sign);
        requestJson.setNationalCode(nationalCode);
        requestJson.setCashInType(cashInType);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, CASH_IN_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CashInResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    public BaseResponse<PurchaseResponse> buy(MockMvc mockMvc, String token, String uniqueIdentifier, String quantity, String price, String commissionCurrency, String commission, String nationalCode, String currency, String merchantId, String walletAccountNumber, String sign, String additionalData, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        BuyWalletRequestJson requestJson = new BuyWalletRequestJson();
        requestJson.setUniqueIdentifier(uniqueIdentifier);
        requestJson.setQuantity(quantity);
        requestJson.setTotalPrice(price);
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

    public BaseResponse<PurchaseResponse> buyDirect(MockMvc mockMvc, String refNumber, String token, String uniqueIdentifier, String quantity, String price, String commissionCurrency, String commission, String nationalCode, String currency, String merchantId, String walletAccountNumber, String sign, String additionalData, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        BuyDirectWalletRequestJson requestJson = new BuyDirectWalletRequestJson();
        requestJson.setUniqueIdentifier(uniqueIdentifier);
        requestJson.setQuantity(quantity);
        requestJson.setTotalPrice(price);
        requestJson.setCommissionObject(new CommissionObject(commissionCurrency, commission));
        requestJson.setNationalCode(nationalCode);
        requestJson.setCurrency(currency);
        requestJson.setMerchantId(merchantId);
        requestJson.setWalletAccountNumber(walletAccountNumber);
        requestJson.setAdditionalData(additionalData);
        requestJson.setSign(sign);
        requestJson.setRefNumber(refNumber);

        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, BUY_DIRECT_IN_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<PurchaseResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    public  BaseResponse<PurchaseResponse> sell(MockMvc mockMvc, String token, String uniqueIdentifier, String quantity, String price, String commissionCurrency, String commission, String nationalCode, String currency, String merchantId, String walletAccountNumber, String sign, String additionalData, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        SellWalletRequestJson requestJson = new SellWalletRequestJson();
        requestJson.setUniqueIdentifier(uniqueIdentifier);
        requestJson.setQuantity(quantity);
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

    public  BaseResponse<UuidResponse> generateBuyUuid(MockMvc mockMvc, String token, String accountNumber, String price, String nationalCode, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        BuyGenerateUuidRequestJson requestJson = new BuyGenerateUuidRequestJson();
        requestJson.setPrice(price);
        requestJson.setNationalCode(nationalCode);
        requestJson.setAccountNumber(accountNumber);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, BUY_GENERATE_UUID_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<UuidResponse>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    public BaseResponse<CreateWalletResponse> balance(MockMvc mockMvc, String token, String nationalCode, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        MockHttpServletRequestBuilder postRequest = buildGetRequest(token, GET_DATA_WALLET_PATH + "?nationalCode=" + nationalCode);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CreateWalletResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    public WalletAccountObject getAccountNumber(MockMvc mockMvc, String token, String nationalCode, String walletAccountType, String walletAccountCurrency) throws Exception {
        log.info("start get wallet data ...");
        BaseResponse<CreateWalletResponse> createWalletResponseBaseResponse = balance(mockMvc, token, nationalCode, HttpStatus.OK, StatusService.SUCCESSFUL, true);
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


    public BaseResponse<MerchantResponse> getMerchant(MockMvc mockMvc, String token, String currency, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        MockHttpServletRequestBuilder getRequest = buildGetRequest(token, MERCHANT_GET_IN_PATH + "?currency=" + currency);
        String response = performTest(mockMvc, getRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<MerchantResponse>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    public BaseResponse<CashInTrackResponse> inquiryCashIn(MockMvc mockMvc, String token, String uniqueIdentifier, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        MockHttpServletRequestBuilder postRequest = buildGetRequest(token, CASH_IN_INQUIRY_PATH + "?uniqueIdentifier=" + uniqueIdentifier);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CashInTrackResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    public BaseResponse<CashInTrackResponse> inquiryPurchase(MockMvc mockMvc, String token, String uniqueIdentifier, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        MockHttpServletRequestBuilder postRequest = buildGetRequest(token, PURCHASE_INQUIRY_IN_PATH + "?uniqueIdentifier=" + uniqueIdentifier);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CashInTrackResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    public String getSettingValue(WalletAccountService walletAccountService, LimitationGeneralCustomService limitationGeneralCustomService, ChannelService channelService, String channelName, String limitationName, String accountNumber) throws Exception {
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        return limitationGeneralCustomService.getSetting(channelService.getChannel(channelName),
                limitationName, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity());
    }

}
