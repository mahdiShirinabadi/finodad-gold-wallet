package com.melli.wallet.web.panel;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.domain.request.PanelBaseSearchJson;
import com.melli.wallet.domain.request.setup.PanelCollateralCreateRequestJson;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.collateral.CollateralListResponse;
import com.melli.wallet.domain.response.collateral.CollateralResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.transaction.ReportTransactionResponse;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.service.repository.WalletAccountCurrencyRepositoryService;
import com.melli.wallet.sync.ResourceSyncService;
import lombok.extern.log4j.Log4j2;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

/**
 * Class Name: PanelCollateralControllerTest
 * Author: Mahdi Shirinabadi
 * Date: 7/21/2025
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
public class PanelCollateralControllerTest extends WalletApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    private static String accessToken;

    @Autowired
    private Flyway flyway;

    @Autowired
    private ResourceSyncService resourceSyncService;

    @Test
    @Order(1)
    @DisplayName("init")
    void initTest() throws Exception {

        // Clean and migrate database
        log.info("start cleaning initial values in test DB");
        flyway.clean();
        flyway.migrate();
        resourceSyncService.syncResourcesOnStartup();

    }

    @Test
    @Order(2)
    @DisplayName("channel login successfully")
    void channelLoginSuccessfully() throws Exception {
        BaseResponse<LoginResponse> response = login(mockMvc, USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        accessToken = response.getData().getAccessTokenObject().getToken();
    }

    // ==================== CREATE COLLATERAL WALLET TESTS ====================

    @Test
    @Order(10)
    @DisplayName("createCollateralWallet-Success")
    void createCollateralWalletSuccess() throws Exception {
        // Define test data
        String mobileNumber = "09123456789";
        String economicCode = "1234567890";
        String name = "Test Collateral Company";
        String iban = "IR123456789012345678901234";

        // Step 1: Create request
        PanelCollateralCreateRequestJson request = new PanelCollateralCreateRequestJson();
        request.setMobileNumber(mobileNumber);
        request.setEconomicCode(economicCode);
        request.setName(name);
        request.setIban(iban);

        // Step 2: Call API
        BaseResponse<String> response = createCollateralWallet(mockMvc, accessToken, request, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        // Step 3: Verify response
        Assert.assertNotNull("Response should not be null", response);
        Assert.assertTrue("Response should be successful", response.getSuccess());
        Assert.assertNotNull("Data should not be null", response.getData());
    }

    // ==================== LIST COLLATERALS TESTS ====================

    @Test
    @Order(20)
    @DisplayName("listCollaterals-Success")
    void listCollateralsSuccess() throws Exception {
        // Define test data
        String currency = WalletAccountCurrencyRepositoryService.GOLD;

        // Step 1: Call API
        BaseResponse<CollateralResponse> response = listCollaterals(mockMvc, accessToken, currency, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        // Step 2: Verify response
        Assert.assertNotNull("Response should not be null", response);
        Assert.assertTrue("Response should be successful", response.getSuccess());
        Assert.assertNotNull("Data should not be null", response.getData());
    }

    // ==================== CREATE LIST (SEARCH) TESTS ====================

    @Test
    @Order(30)
    @DisplayName("createListCollaterals-Success")
    void createListCollateralsSuccess() throws Exception {
        // Define test data
        String collateralId = "1";
        String page = "0";
        String size = "10";
        String orderBy = "id";
        String sort = "asc";

        // Step 1: Create request
        PanelBaseSearchJson request = new PanelBaseSearchJson();
        Map<String, String> map = new HashMap<>();
        map.put("collateralId", collateralId);
        map.put("page", page);
        map.put("size", size);
        map.put("orderBy", orderBy);
        map.put("sort", sort);
        request.setMap(map);

        // Step 2: Call API
        BaseResponse<CollateralListResponse> response = createListCollaterals(mockMvc, accessToken, request, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        // Step 3: Verify response
        Assert.assertNotNull("Response should not be null", response);
        Assert.assertTrue("Response should be successful", response.getSuccess());
        Assert.assertNotNull("Data should not be null", response.getData());
    }

    // ==================== GET BALANCE TESTS ====================

    @Test
    @Order(40)
    @DisplayName("getCollateralBalance-Success")
    void getCollateralBalanceSuccess() throws Exception {
        // Define test data
        String collateralId = "1";

        // Step 1: Call API
        BaseResponse<WalletBalanceResponse> response = getCollateralBalance(mockMvc, accessToken, collateralId, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        // Step 2: Verify response
        Assert.assertNotNull("Response should not be null", response);
        Assert.assertTrue("Response should be successful", response.getSuccess());
        Assert.assertNotNull("Data should not be null", response.getData());
    }

    // ==================== GENERATE REPORT TESTS ====================

    @Test
    @Order(50)
    @DisplayName("generateCollateralReport-Success")
    void generateCollateralReportSuccess() throws Exception {
        // Define test data
        String collateralId = "1";
        String fromTime = "1403/01/01";
        String toTime = "1403/09/01";
        String page = "0";
        String size = "10";
        String orderBy = "id";
        String sort = "asc";

        // Step 1: Create request
        PanelBaseSearchJson request = new PanelBaseSearchJson();
        Map<String, String> map = new HashMap<>();
        map.put("collateralId", collateralId);
        request.setMap(map);

        // Step 2: Call API
        BaseResponse<ReportTransactionResponse> response = generateCollateralReport(mockMvc, accessToken, request, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        // Step 3: Verify response
        Assert.assertNotNull("Response should not be null", response);
        Assert.assertTrue("Response should be successful", response.getSuccess());
        Assert.assertNotNull("Data should not be null", response.getData());
    }
}
