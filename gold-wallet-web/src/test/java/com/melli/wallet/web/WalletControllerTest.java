package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.sync.ResourceSyncService;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.wallet.TotalWalletBalanceResponse;
import lombok.extern.log4j.Log4j2;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Log4j2
@DisplayName("WalletControllerTest End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WalletControllerTest extends WalletApplicationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CacheClearService cacheClearService;

    @Autowired
    private ResourceSyncService resourceSyncService;

    @Autowired
    private Flyway flyway;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        cacheClearService.clearAllCaches();
        resourceSyncService.syncAll();
    }

    @Test
    @Order(1)
    @DisplayName("calculateTotalBalanceSuccess")
    void calculateTotalBalanceSuccess() throws Exception {
        log.info("start calculateTotalBalanceSuccess test");

        // Test the calculate total balance endpoint
        BaseResponse<TotalWalletBalanceResponse> response = getWalletTotalBalance(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getTotalBalance());
        Assert.assertEquals("MERCHANT", response.getData().getExcludedWalletType());
        log.info("Wallet total balance calculated successfully: {}", response.getData().getTotalBalance());
    }

    @Test
    @Order(2)
    @DisplayName("calculateTotalBalanceFail-InvalidCurrency")
    void calculateTotalBalanceFailInvalidCurrency() throws Exception {
        log.info("start calculateTotalBalanceFailInvalidCurrency test");

        // Test with invalid currency
        getWalletTotalBalance(mockMvc, ACCESS_TOKEN, "INVALID_CURRENCY", HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);
    }
}
