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


    private static final String NATIONAL_CODE_CORRECT = "0077847660";
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String refNumber = new Date().getTime() + "";

        String amount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
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

    @Test
    @Order(50)
    @DisplayName("cashIn success")
    void buySuccess() throws Exception {
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String refNumber = new Date().getTime() + "";

        String amount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
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




}
