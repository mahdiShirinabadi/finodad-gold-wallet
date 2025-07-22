package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.domain.master.entity.MerchantWalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.persistence.MerchantWalletAccountCurrencyRepository;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.purchase.MerchantResponse;
import com.melli.wallet.service.*;
import lombok.extern.log4j.Log4j2;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: WalletEndPointTest
 * Author: Mahdi Shirinabadi
 * Date: 4/7/2025
 */
@Log4j2
@DisplayName("MerchantControllerTest End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MerchantControllerTest extends WalletApplicationTests {


    private static final String NATIONAL_CODE_CORRECT = "0077847660";
    private static final String MOBILE_CORRECT = "09124162337";

    private static MockMvc mockMvc;
    private static String ACCESS_TOKEN;
    private static String REFRESH_TOKEN;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private WalletAccountCurrencyService walletAccountCurrencyService;
    @Autowired
    private Flyway flyway;
    @Autowired
    private CacheClearService cacheClearService;
    @Autowired
    private MerchantWalletAccountCurrencyRepository merchantWalletAccountCurrencyRepository;
    @Autowired
    private MerchantService merchantService;


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
    @DisplayName("channel login successfully")
    void login_success() throws Exception {
        log.info("start login_success test");
        BaseResponse<LoginResponse> response = login(mockMvc, USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.OK,
                StatusService.SUCCESSFUL, true);
        ACCESS_TOKEN = response.getData().getAccessTokenObject().getToken();
        REFRESH_TOKEN = response.getData().getRefreshTokenObject().getToken();
    }


    @Test
    @Order(20)
    @DisplayName("merchantFail-Currency not ")
    void merchantCurrencyNotFound() throws Exception {
        getMerchant(mockMvc, ACCESS_TOKEN, "NOTHING", HttpStatus.OK, StatusService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);
    }

    @Test
    @Order(21)
    @DisplayName("merchantSuccess-empty")
    void merchantSuccessEmpty() throws Exception {
        BaseResponse<MerchantResponse> response = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        if(response.getData().getMerchantObjectList() != null){
            log.error("merchant must be null");
            throw new Exception("merchant must be null");
        }
    }

    @Test
    @Order(22)
    @DisplayName("merchantSuccess-one record")
    void merchantSuccess() throws Exception {
        MerchantWalletAccountCurrencyEntity merchantWalletAccountCurrencyEntity = new MerchantWalletAccountCurrencyEntity();
        merchantWalletAccountCurrencyEntity.setMerchantEntity(merchantService.findById(1));
        merchantWalletAccountCurrencyEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyService.findCurrency("GOLD"));
        merchantWalletAccountCurrencyEntity.setCreatedAt(new Date());
        merchantWalletAccountCurrencyEntity.setCreatedBy("System");
        merchantWalletAccountCurrencyRepository.save(merchantWalletAccountCurrencyEntity);
        BaseResponse<MerchantResponse> response = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        if(response.getData().getMerchantObjectList().size() != 1){
            log.error("merchant must be one Record");
            throw new Exception("merchant must be one record");
        }
    }

    @Test
    @Order(23)
    @DisplayName("merchantSuccessEmptyWithRial")
    void merchantSuccessEmptyWithRial() throws Exception {
        BaseResponse<MerchantResponse> response = getMerchant(mockMvc, ACCESS_TOKEN, "RIAL", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        if(response.getData().getMerchantObjectList() != null){
            log.error("merchant must be null");
            throw new Exception("merchant must be null");
        }
    }



}
