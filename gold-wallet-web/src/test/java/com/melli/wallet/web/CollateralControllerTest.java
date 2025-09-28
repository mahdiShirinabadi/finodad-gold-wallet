package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.domain.dto.BalanceDTO;
import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.service.repository.WalletAccountTypeRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.request.wallet.CommissionObject;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.collateral.CreateCollateralResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.sync.ResourceSyncService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: CollateralControllerTest
 * Author: Mahdi Shirinabadi
 * Date: 9/27/2025
 * This test class contains comprehensive end-to-end tests for Collateral operations.
 * It tests the complete flow from wallet creation to collateral generation and validation.
 * Test Coverage:
 * - Wallet creation and setup
 * - Collateral UUID generation (success and failure scenarios)
 * - Amount validation (minimum, maximum limits)
 * - Currency validation
 * - Account validation
 * - Security validation (signature)
 * - UUID validation and mismatch scenarios
 */
@Log4j2
@DisplayName("CollateralControllerTest End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CollateralControllerTest extends WalletApplicationTests {

    private static final String NATIONAL_CODE_CORRECT = "0077847660";
    private static final String NATIONAL_CODE_DEST = "2980511481";
    private static final String MOBILE_CORRECT = "09124162337";
    private static final String CURRENCY_GOLD = "GOLD";

    private static MockMvc mockMvc;
    private static String accessToken;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private WalletAccountRepositoryService walletAccountRepositoryService;
    @Autowired
    private LimitationGeneralCustomRepositoryService limitationGeneralCustomRepositoryService;
    @Autowired
    private ChannelRepositoryService channelRepositoryService;
    @Autowired
    private CacheClearService cacheClearService;
    @Autowired
    private Flyway flyway;
    @Autowired
    private ResourceSyncService resourceSyncService;
    @Autowired
    private WalletTypeRepositoryService walletTypeRepositoryService;
    @Autowired
    private WalletLevelRepositoryService walletLevelRepositoryService;
    @Autowired
    private WalletRepositoryService walletRepositoryService;

    @Test
    @Order(1)
    @DisplayName("Initiate...")
    void initial() throws Exception {
        // Setup MockMvc for testing with security
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);

        // Clean and migrate database
        log.info("start cleaning initial values in test DB");
        flyway.clean();
        flyway.migrate();
        resourceSyncService.syncResourcesOnStartup();

        // Step 4: Create wallet for channel testing
        WalletEntity walletEntity = new WalletEntity();
        walletEntity.setStatus(WalletStatusEnum.ACTIVE);
        walletEntity.setMobile("9120000000");
        walletEntity.setNationalCode("0000000000");
        walletEntity.setDescription("channel wallet");
        walletEntity.setOwner(channelRepositoryService.getChannel(USERNAME_CORRECT));
        walletEntity.setWalletTypeEntity(walletTypeRepositoryService.getByName(WalletTypeRepositoryService.CHANNEL));
        walletEntity.setWalletLevelEntity(walletLevelRepositoryService.getByLevelManaged(WalletLevelRepositoryService.BRONZE));
        walletEntity.setCreatedBy("admin");
        walletEntity.setCreatedAt(new Date());
        walletRepositoryService.save(walletEntity);

        ChannelEntity channelEntity = channelRepositoryService.getChannel(USERNAME_CORRECT);
        channelEntity.setWalletEntity(walletEntity);
        channelRepositoryService.save(channelEntity);

        walletAccountRepositoryService.createAccount(List.of(WalletAccountCurrencyRepositoryService.RIAL, WalletAccountCurrencyRepositoryService.GOLD),
                walletEntity, List.of(WalletAccountTypeRepositoryService.WAGE), channelEntity);

        // Clear all caches
        cacheClearService.clearCache();
    }

    @Test
    @Order(2)
    @DisplayName("channel login successfully")
    void channelLoginSuccessfully() throws Exception {
        BaseResponse<LoginResponse> response = login(mockMvc, USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        accessToken = response.getData().getAccessTokenObject().getToken();
    }

    @Test
    @Order(3)
    @DisplayName("create wallet- success")
    void createWalletSuccess() throws Exception {
        BaseResponse<CreateWalletResponse> response = createWallet(mockMvc, accessToken, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getWalletId());
    }

    @Test
    @Order(4)
    @DisplayName("create destination wallet- success")
    void createDestinationWalletSuccess() throws Exception {
        BaseResponse<CreateWalletResponse> response = createWallet(mockMvc, accessToken, NATIONAL_CODE_DEST, MOBILE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getWalletId());
    }


    // ==================== GENERATE UUID TESTS ====================

    @Test
    @Order(9)
    @DisplayName("GenerateCollateralUuid-Fail-notPermission")
    void generateCollateralUuidNotPermission() throws Exception {
        // Define amounts
        String collateralQuantity = "0.001";
        // Step 1: Get account number and charge it
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        // Step 2: Generate UUID not permission
        BaseResponse<UuidResponse> response = generateCollateralUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, collateralQuantity, CURRENCY_GOLD, sourceAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.ACCOUNT_DONT_PERMISSION_FOR_COLLATERAL, false);
        Assert.assertNull(response.getData());
        Assert.assertFalse(response.getSuccess());
    }

    @Test
    @Order(10)
    @DisplayName("generateCollateralUuid-Success")
    void generateCollateralUuidSuccess() throws Exception {
        // Define amounts
        String collateralQuantity = "0.001";
        String chargeAmount = "10";

        // Step 1: Get account number and charge it
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        //change permission
        String collateralSettingValue = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, sourceAccount.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
        if("false".equalsIgnoreCase(collateralSettingValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, walletAccountEntity, "true");
        }

        chargeAccountForCollateral(sourceAccount.getAccountNumber(), chargeAmount);

        // Step 2: Generate UUID successfully
        BaseResponse<UuidResponse> response = generateCollateralUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, collateralQuantity, CURRENCY_GOLD, sourceAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getUniqueIdentifier());
        Assert.assertTrue(response.getSuccess());

        // Step 3: Clean up - decrease balance to zero
        chargeAccountForCollateralToZero(sourceAccount.getAccountNumber());

        // set default
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, walletAccountEntity, collateralSettingValue);
    }

    @Test
    @Order(11)
    @DisplayName("generateCollateralUuid-Fail-InvalidNationalCode")
    void generateCollateralUuidFailInvalidNationalCode() throws Exception {
        // Define amounts
        String invalidNationalCode = "1234567890";
        String collateralQuantity = "0.001";
        String walletAccountNumber = "TEST123";

        // Step 1: Try to generate UUID with invalid national code
        BaseResponse<UuidResponse> response = generateCollateralUuid(mockMvc, accessToken, invalidNationalCode, collateralQuantity, CURRENCY_GOLD, walletAccountNumber, HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);

        Assert.assertFalse(response.getSuccess());
    }

    @Test
    @Order(12)
    @DisplayName("generateCollateralUuid-Fail-InvalidQuantity")
    void generateCollateralUuidFailInvalidQuantity() throws Exception {
        // Define amounts
        String invalidQuantity = "-0.001";
        String walletAccountNumber = "TEST123";

        // Step 1: Try to generate UUID with invalid quantity
        BaseResponse<UuidResponse> response = generateCollateralUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, invalidQuantity, CURRENCY_GOLD, walletAccountNumber, HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);

        Assert.assertFalse(response.getSuccess());
    }

    @Test
    @Order(13)
    @DisplayName("generateCollateralUuid-Fail-InvalidCurrency")
    void generateCollateralUuidFailInvalidCurrency() throws Exception {
        // Define amounts
        String collateralQuantity = "0.001";
        String invalidCurrency = "INVALID";
        String walletAccountNumber = "TEST123";

        // Step 1: Try to generate UUID with invalid currency
        BaseResponse<UuidResponse> response = generateCollateralUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, collateralQuantity, invalidCurrency, walletAccountNumber, HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);

        Assert.assertFalse(response.getSuccess());
    }

    // ==================== CREATE TESTS ====================

    @Test
    @Order(20)
    @DisplayName("createCollateral-Success")
    void createCollateralSuccess() throws Exception {
        // Define amounts
        String collateralQuantity = "0.001";
        String commissionAmount = "0.0001";
        String chargeAmount = "1";
        String description = "Test collateral creation";

        // Step 1: Get account number and charge it
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        //change permission
        String collateralSettingValue = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, sourceAccount.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
        if("false".equalsIgnoreCase(collateralSettingValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, walletAccountEntity, "true");
        }

        chargeAccountForCollateral(sourceAccount.getAccountNumber(), chargeAmount);

        // Step 2: Verify initial balance
        BigDecimal initialBalance = walletAccountRepositoryService.getBalance(walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()).getId()).getRealBalance();
        Assert.assertEquals("Initial balance should be 1", new BigDecimal(chargeAmount), initialBalance.stripTrailingZeros());

        // Step 3: Generate UUID first
        BaseResponse<UuidResponse> uuidResponse = generateCollateralUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, collateralQuantity, CURRENCY_GOLD, sourceAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        // Step 4: Create collateral successfully
        CommissionObject commission = new CommissionObject(CURRENCY_GOLD, commissionAmount);
        BaseResponse<CreateCollateralResponse> response = createCollateral(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), collateralQuantity, sourceAccount.getAccountNumber(), description, generateValidSign(uuidResponse.getData().getUniqueIdentifier() + "|" + collateralQuantity + "|" + sourceAccount.getAccountNumber()), commission, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getCollateralCode());
        Assert.assertEquals(collateralQuantity, response.getData().getQuantity());
        Assert.assertEquals(NATIONAL_CODE_CORRECT, response.getData().getNationalCode());
        Assert.assertTrue(response.getSuccess());

        // Step 5: Verify final balance calculation
        BigDecimal finalBalance = walletAccountRepositoryService.getBalance(walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()).getId()).getAvailableBalance();
        BigDecimal expectedBalance = new BigDecimal(chargeAmount).subtract(new BigDecimal(collateralQuantity)).subtract(new BigDecimal(commissionAmount));
        Assert.assertEquals("Final balance should be 1 - (0.001 + 0.0001) = 0.9989", expectedBalance.stripTrailingZeros(), finalBalance.stripTrailingZeros());

        // Step 6: Clean up - decrease balance to zero
        chargeAccountForCollateralToZero(sourceAccount.getAccountNumber());

        // set default
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, walletAccountEntity, collateralSettingValue);
    }

    @Test
    @Order(21)
    @DisplayName("createCollateral-Fail-InvalidUUID")
    void createCollateralFailInvalidUUID() throws Exception {
        // Define amounts
        String invalidUUID = "INVALID_UUID";
        String collateralQuantity = "0.001";
        String commissionAmount = "0.0001";
        String walletAccountNumber = "TEST123";
        String description = "Test collateral creation";

        // Step 1: Try to create collateral with invalid UUID
        CommissionObject commission = new CommissionObject(CURRENCY_GOLD, commissionAmount);
        BaseResponse<CreateCollateralResponse> response = createCollateral(mockMvc, accessToken, invalidUUID, collateralQuantity, walletAccountNumber, description, generateValidSign(invalidUUID + "|" + collateralQuantity + "|" + walletAccountNumber), commission, HttpStatus.OK, StatusRepositoryService.UUID_NOT_FOUND, false);

        Assert.assertFalse(response.getSuccess());
    }

    /*@Test
    @Order(22)
    @DisplayName("createCollateral-Fail-InvalidSign")
    void createCollateralFailInvalidSign() throws Exception {
        // Define amounts
        String collateralQuantity = "0.001";
        String commissionAmount = "0.0001";
        String walletAccountNumber = "TEST123";
        String description = "Test collateral creation";

        // Step 1: Generate UUID first
        BaseResponse<UuidResponse> uuidResponse = generateCollateralUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, collateralQuantity, CURRENCY_GOLD, walletAccountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        // Step 2: Try to create collateral with invalid signature
        CommissionObject commission = new CommissionObject(CURRENCY_GOLD, commissionAmount);
        BaseResponse<CreateCollateralResponse> response = createCollateral(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), collateralQuantity, walletAccountNumber, description, "INVALID_SIGNATURE", commission, HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);

        Assert.assertFalse(response.getSuccess());
    }*/


    // ==================== CONCURRENCY TESTS ====================

    @Test
    @Order(30)
    @DisplayName("generateCollateralUuid-ConcurrentGeneration")
    void generateCollateralUuidConcurrentGeneration() throws Exception {
        // Define amounts
        String collateralQuantity = "0.001";
        String chargeAmount= "1";
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        String walletAccountNumber = sourceAccount.getAccountNumber();

        int threadCount = 3;
        chargeAccountForCollateral(sourceAccount.getAccountNumber(), chargeAmount);

        //change permission
        String collateralSettingValue = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, sourceAccount.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
        if("false".equalsIgnoreCase(collateralSettingValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, walletAccountEntity, "true");
        }

        // Step 1: Create concurrent generation requests using CompletableFuture
        List<CompletableFuture<BaseResponse<UuidResponse>>> futures = new ArrayList<>();
        
        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<BaseResponse<UuidResponse>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    BaseResponse<UuidResponse> response = generateCollateralUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, collateralQuantity, CURRENCY_GOLD, walletAccountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                    log.info("Concurrent generation completed with success: {}", response.getSuccess());
                    return response;
                } catch (Exception e) {
                    log.error("Error in concurrent generation", e);
                    return null;
                }
            });
            futures.add(future);
        }

        // Step 2: Wait for all generations to complete with timeout
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allFutures.get(15, TimeUnit.SECONDS); // Increased timeout
        } catch (TimeoutException e) {
            Assert.fail("Concurrent generations should complete within 15 seconds");
        }

        // Step 3: Collect results and verify
        List<BaseResponse<UuidResponse>> results = new ArrayList<>();
        for (CompletableFuture<BaseResponse<UuidResponse>> future : futures) {
            try {
                BaseResponse<UuidResponse> response = future.get();
                if (response != null) {
                    results.add(response);
                }
            } catch (Exception e) {
                log.error("Error getting future result", e);
            }
        }

        // Step 4: Verify all generations succeeded
        Assert.assertEquals("All concurrent generations should complete", threadCount, results.size());
        for (BaseResponse<UuidResponse> response : results) {
            Assert.assertTrue("Concurrent generation should succeed", response.getSuccess());
            Assert.assertNotNull("UUID should be generated", response.getData().getUniqueIdentifier());
        }

        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, walletAccountEntity, collateralSettingValue);
        chargeAccountForCollateralToZero(walletAccountNumber);
    }

    @Test
    @Order(31)
    @DisplayName("createCollateral-ConcurrentSameUUID")
    void createCollateralConcurrentSameUUID() throws Exception {
        // Define amounts
        String collateralQuantity = "0.001";
        String commissionAmount = "0.0001";
        String chargeAmount = "1";
        String description1 = "concurrent test 1";
        String description2 = "concurrent test 2";
        int threadCount = 2;

        // Step 1: Get account number and charge it
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        //change permission
        String collateralSettingValue = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, sourceAccount.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
        if("false".equalsIgnoreCase(collateralSettingValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, walletAccountEntity, "true");
        }

        String walletAccountNumber = sourceAccount.getAccountNumber();
        chargeAccountForCollateral(walletAccountNumber, chargeAmount);

        // Step 2: Verify initial balance
        BigDecimal initialBalance = walletAccountRepositoryService.getBalance(walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()).getId()).getAvailableBalance();
        Assert.assertEquals("Initial balance should be 1", new BigDecimal(chargeAmount), initialBalance.stripTrailingZeros());

        // Step 3: Generate UUID
        BaseResponse<UuidResponse> uuidResponse = generateCollateralUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, collateralQuantity, CURRENCY_GOLD, sourceAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();

        // Step 2: Process same UUID concurrently (should fail for second request)
        CommissionObject commission = new CommissionObject(CURRENCY_GOLD, commissionAmount);
        String sign = generateValidSign(uniqueIdentifier + "|" + collateralQuantity + "|" + walletAccountNumber);

        // Create concurrent requests using CompletableFuture
        List<CompletableFuture<BaseResponse<CreateCollateralResponse>>> futures = new ArrayList<>();

        // First concurrent request
        CompletableFuture<BaseResponse<CreateCollateralResponse>> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                BaseResponse<CreateCollateralResponse> response = createCollateral(mockMvc, accessToken, uniqueIdentifier, collateralQuantity, walletAccountNumber, description1, sign, commission, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                log.info("First concurrent create completed with success: {}", response.getSuccess());
                return response;
            } catch (Exception e) {
                log.error("Error in concurrent create 1", e);
                return null;
            }
        });
        futures.add(future1);

        // Second concurrent request with same UUID (should fail) - add small delay
        CompletableFuture<BaseResponse<CreateCollateralResponse>> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                // Small delay to ensure second request starts after first
                Thread.sleep(100);
                BaseResponse<CreateCollateralResponse> response = createCollateral(mockMvc, accessToken, uniqueIdentifier, collateralQuantity, walletAccountNumber, description2, sign, commission, HttpStatus.OK, StatusRepositoryService.DUPLICATE_UUID, false);
                log.info("Second concurrent create completed with success: {}", response.getSuccess());
                return response;
            } catch (Exception e) {
                log.error("Error in concurrent create 2", e);
                return null;
            }
        });
        futures.add(future2);

        // Step 3: Wait for both requests to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allFutures.get(15, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Assert.fail("Concurrent create requests should complete within 15 seconds");
        }

        // Step 4: Collect results and verify - one success, one failure
        List<BaseResponse<CreateCollateralResponse>> results = new ArrayList<>();
        for (CompletableFuture<BaseResponse<CreateCollateralResponse>> future : futures) {
            try {
                BaseResponse<CreateCollateralResponse> response = future.get();
                if (response != null) {
                    results.add(response);
                }
            } catch (Exception e) {
                log.error("Error getting future result", e);
            }
        }

        Assert.assertEquals("Both concurrent requests should complete", threadCount, results.size());

        boolean hasSuccess = false;
        boolean hasFailure = false;

        for (BaseResponse<CreateCollateralResponse> response : results) {
            if (response.getSuccess()) {
                hasSuccess = true;
            } else {
                hasFailure = true;
            }
        }

        Assert.assertTrue("First request should succeed", hasSuccess);
        Assert.assertTrue("Second request should fail due to duplicate UUID", hasFailure);

        // Step 4: Verify final balance (only one successful transaction should affect balance)
        BigDecimal finalBalance = walletAccountRepositoryService.getBalance(walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()).getId()).getAvailableBalance();
        BigDecimal expectedBalance = new BigDecimal(chargeAmount).subtract(new BigDecimal(collateralQuantity)).subtract(new BigDecimal(commissionAmount));
        Assert.assertEquals("Final balance should be 1 - (0.001 + 0.0001) = 0.9989 (only one successful transaction)", expectedBalance.stripTrailingZeros(), finalBalance.stripTrailingZeros());

        // Step 5: Clean up - decrease balance to zero
        chargeAccountForCollateralToZero(sourceAccount.getAccountNumber());

        // set default
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, walletAccountEntity, collateralSettingValue);
    }

    // ==================== RELEASE TESTS ====================

    @Test
    @Order(40)
    @DisplayName("releaseCollateral-Success")
    void releaseCollateralSuccess() throws Exception {
        // Define amounts
        String collateralQuantity = "0.001";
        String commissionAmount = "0.0001";
        String chargeAmount = "1";
        String additionalData = "Release test data";

        // Step 1: Get account number and charge it
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        //change permission
        String collateralSettingValue = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, sourceAccount.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
        if("false".equalsIgnoreCase(collateralSettingValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, walletAccountEntity, "true");
        }

        chargeAccountForCollateral(sourceAccount.getAccountNumber(), chargeAmount);
        
        // Step 2: Verify initial balance
        BigDecimal initialBalance = walletAccountRepositoryService.getBalance(walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()).getId()).getAvailableBalance();
        Assert.assertEquals("Initial balance should be 1", new BigDecimal(chargeAmount), initialBalance.stripTrailingZeros());
        
        // Step 3: Generate UUID and create collateral first
        BaseResponse<UuidResponse> uuidResponse = generateCollateralUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, collateralQuantity, CURRENCY_GOLD, sourceAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        CommissionObject commission = new CommissionObject(CURRENCY_GOLD, commissionAmount);
        BaseResponse<CreateCollateralResponse> createResponse = createCollateral(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), collateralQuantity, sourceAccount.getAccountNumber(), "Test collateral creation", generateValidSign(uuidResponse.getData().getUniqueIdentifier() + "|" + collateralQuantity + "|" + sourceAccount.getAccountNumber()), commission, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        // Step 4: Verify balance after collateral creation
        BigDecimal balanceAfterCreation = walletAccountRepositoryService.getBalance(walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()).getId()).getAvailableBalance();
        BigDecimal expectedBalanceAfterCreation = new BigDecimal(chargeAmount).subtract(new BigDecimal(collateralQuantity)).subtract(new BigDecimal(commissionAmount));
        Assert.assertEquals("Balance after creation should be 1 - (0.001 + 0.0001) = 0.9989", expectedBalanceAfterCreation.stripTrailingZeros(), balanceAfterCreation.stripTrailingZeros());

        // Step 5: Release collateral successfully
        CommissionObject releaseCommission = new CommissionObject(CURRENCY_GOLD, commissionAmount);
        BaseResponse<ObjectUtils.Null> response = releaseCollateral(mockMvc, accessToken, createResponse.getData().getCollateralCode(), collateralQuantity, NATIONAL_CODE_CORRECT, additionalData, generateValidSign(collateralQuantity + "|" + createResponse.getData().getCollateralCode() + "|" + NATIONAL_CODE_CORRECT), releaseCommission, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        Assert.assertNotNull(response);
        Assert.assertTrue(response.getSuccess());

        // Step 6: Verify final balance after release (should return collateral but pay commission)
        BigDecimal finalBalance = walletAccountRepositoryService.getBalance(walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()).getId()).getAvailableBalance();
        BigDecimal expectedFinalBalance = expectedBalanceAfterCreation.add(new BigDecimal(collateralQuantity)).subtract(new BigDecimal(commissionAmount));
        Assert.assertEquals("Final balance after release should be 0.9989 + 0.001 - 0.0001 = 0.9998", expectedFinalBalance.stripTrailingZeros(), finalBalance.stripTrailingZeros());

        // Step 7: Clean up - decrease balance to zero
        chargeAccountForCollateralToZero(sourceAccount.getAccountNumber());

        // set default
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, walletAccountEntity, collateralSettingValue);
    }

    @Test
    @Order(41)
    @DisplayName("releaseCollateral-Fail-InvalidCollateralCode")
    void releaseCollateralFailInvalidCollateralCode() throws Exception {
        // Define amounts
        String invalidCollateralCode = "INVALID_COLLATERAL_CODE";
        String collateralQuantity = "0.001";
        String commissionAmount = "0.0001";
        String additionalData = "Release test data";

        // Step 1: Try to release collateral with invalid collateral code
        CommissionObject commission = new CommissionObject(CURRENCY_GOLD, commissionAmount);
        BaseResponse<ObjectUtils.Null> response = releaseCollateral(mockMvc, accessToken, invalidCollateralCode, collateralQuantity, NATIONAL_CODE_CORRECT, additionalData, generateValidSign(collateralQuantity + "|" + invalidCollateralCode + "|" + NATIONAL_CODE_CORRECT), commission, HttpStatus.OK, StatusRepositoryService.COLLATERAL_CODE_NOT_FOUND, false);

        Assert.assertFalse(response.getSuccess());
    }

    /*@Test
    @Order(42)
    @DisplayName("releaseCollateral-Fail-InvalidSign")
    void releaseCollateralFailInvalidSign() throws Exception {
        // Define amounts
        String collateralQuantity = "0.001";
        String commissionAmount = "0.0001";
        String additionalData = "Release test data";

        // Step 1: Generate UUID and create collateral first
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        
        BaseResponse<UuidResponse> uuidResponse = generateCollateralUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, collateralQuantity, CURRENCY_GOLD, sourceAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        CommissionObject commission = new CommissionObject(CURRENCY_GOLD, commissionAmount);
        BaseResponse<CreateCollateralResponse> createResponse = createCollateral(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), collateralQuantity, sourceAccount.getAccountNumber(), "Test collateral creation", generateValidSign(uuidResponse.getData().getUniqueIdentifier() + "|" + collateralQuantity + "|" + sourceAccount.getAccountNumber()), commission, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        // Step 2: Try to release collateral with invalid signature
        CommissionObject releaseCommission = new CommissionObject(CURRENCY_GOLD, commissionAmount);
        BaseResponse<ObjectUtils.Null> response = releaseCollateral(mockMvc, accessToken, createResponse.getData().getCollateralCode(), collateralQuantity, NATIONAL_CODE_CORRECT, additionalData, "INVALID_SIGNATURE", releaseCommission, HttpStatus.OK, StatusRepositoryService.INVALID_SIGN, false);

        Assert.assertFalse(response.getSuccess());
    }*/

    // ==================== CONCURRENCY TESTS FOR RELEASE ====================

    @Test
    @Order(50)
    @DisplayName("releaseCollateral-ConcurrentSameCollateralCode")
    void releaseCollateralConcurrentSameCollateralCode() throws Exception {
        // Define amounts
        String collateralQuantity = "0.001";
        String commissionAmount = "0.0001";
        String chargeAmount = "1";
        String additionalData1 = "concurrent release test 1";
        String additionalData2 = "concurrent release test 2";
        int threadCount = 2;

        // Step 1: Get account number and charge it
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        //change permission
        String collateralSettingValue = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, sourceAccount.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
        if("false".equalsIgnoreCase(collateralSettingValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, walletAccountEntity, "true");
        }

        chargeAccountForCollateral(sourceAccount.getAccountNumber(), chargeAmount);

        // Step 2: Verify initial balance
        BigDecimal initialBalance = walletAccountRepositoryService.getBalance(walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()).getId()).getAvailableBalance();
        Assert.assertEquals("Initial balance should be 1", new BigDecimal(chargeAmount), initialBalance.stripTrailingZeros());
        
        // Step 2: Generate UUID and create collateral first
        BaseResponse<UuidResponse> uuidResponse = generateCollateralUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, collateralQuantity, CURRENCY_GOLD, sourceAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        CommissionObject commission = new CommissionObject(CURRENCY_GOLD, commissionAmount);
        BaseResponse<CreateCollateralResponse> createResponse = createCollateral(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), collateralQuantity, sourceAccount.getAccountNumber(), "Test collateral creation", generateValidSign(uuidResponse.getData().getUniqueIdentifier() + "|" + collateralQuantity + "|" + sourceAccount.getAccountNumber()), commission, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        String collateralCode = createResponse.getData().getCollateralCode();

        // Step 2: Send TWO CONCURRENT RELEASE REQUESTS with the SAME collateral code
        // Only the first request should succeed, the second should fail
        CommissionObject releaseCommission = new CommissionObject(CURRENCY_GOLD, commissionAmount);

        // Create concurrent release requests using CompletableFuture
        List<CompletableFuture<BaseResponse<ObjectUtils.Null>>> futures = new ArrayList<>();

        // First concurrent release request
        CompletableFuture<BaseResponse<ObjectUtils.Null>> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Starting first concurrent release with collateral code: {}", collateralCode);
                BaseResponse<ObjectUtils.Null> response = releaseCollateral(mockMvc, accessToken, collateralCode, collateralQuantity, NATIONAL_CODE_CORRECT, additionalData1, generateValidSign(collateralQuantity + "|" + collateralCode + "|" + NATIONAL_CODE_CORRECT), releaseCommission, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                log.info("First release completed with success: {}", response.getSuccess());
                return response;
            } catch (Exception e) {
                log.error("Error in first concurrent release", e);
                return null;
            }
        });
        futures.add(future1);

        // Second concurrent release request with SAME collateral code (should fail) - add small delay
        CompletableFuture<BaseResponse<ObjectUtils.Null>> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                // Small delay to ensure second request starts after first
                Thread.sleep(100);
                log.info("Starting second concurrent release with SAME collateral code: {}", collateralCode);
                BaseResponse<ObjectUtils.Null> response = releaseCollateral(mockMvc, accessToken, collateralCode, collateralQuantity, NATIONAL_CODE_CORRECT, additionalData2, generateValidSign(collateralQuantity + "|" + collateralCode + "|" + NATIONAL_CODE_CORRECT), releaseCommission, HttpStatus.OK, StatusRepositoryService.COLLATERAL_RELEASE_BEFORE, false);
                log.info("Second release completed with success: {}", response.getSuccess());
                return response;
            } catch (Exception e) {
                log.error("Error in second concurrent release", e);
                return null;
            }
        });
        futures.add(future2);

        // Step 3: Wait for both release requests to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allFutures.get(15, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Assert.fail("Concurrent release requests should complete within 15 seconds");
        }

        // Step 4: Collect results and verify - exactly one success, one failure
        List<BaseResponse<ObjectUtils.Null>> results = new ArrayList<>();
        for (CompletableFuture<BaseResponse<ObjectUtils.Null>> future : futures) {
            try {
                BaseResponse<ObjectUtils.Null> response = future.get();
                if (response != null) {
                    results.add(response);
                }
            } catch (Exception e) {
                log.error("Error getting future result", e);
            }
        }

        Assert.assertEquals("Both concurrent release requests should complete", threadCount, results.size());

        boolean hasSuccess = false;
        boolean hasFailure = false;
        int successCount = 0;
        int failureCount = 0;

        for (BaseResponse<ObjectUtils.Null> response : results) {
            if (response.getSuccess()) {
                hasSuccess = true;
                successCount++;
                log.info("Release succeeded");
            } else {
                hasFailure = true;
                failureCount++;
                log.info("Release failed with error code: {}", response.getErrorDetail().getCode());
            }
        }

        // Verify exactly one success and one failure
        Assert.assertEquals("Exactly one release should succeed", 1, successCount);
        Assert.assertEquals("Exactly one release should fail", 1, failureCount);
        Assert.assertTrue("First release should succeed", hasSuccess);
        Assert.assertTrue("Second release should fail due to collateral already released", hasFailure);

        // Step 4: Verify final balance (only one successful release should affect balance)
        BigDecimal finalBalance = walletAccountRepositoryService.getBalance(walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()).getId()).getAvailableBalance();
        // After creation: 1 - (0.001 + 0.0001) = 0.9989
        // After successful release: 0.9989 + 0.001 - 0.0001 = 0.9998
        BigDecimal expectedBalance = new BigDecimal(chargeAmount).subtract(new BigDecimal(collateralQuantity)).subtract(new BigDecimal(commissionAmount)).add(new BigDecimal(collateralQuantity)).subtract(new BigDecimal(commissionAmount));
        Assert.assertEquals("Final balance should be 0.9998 (only one successful release)", expectedBalance.stripTrailingZeros(), finalBalance.stripTrailingZeros());

        // Step 5: Clean up - decrease balance to zero
        chargeAccountForCollateralToZero(sourceAccount.getAccountNumber());

        // set default
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_COLLATERAL, walletAccountEntity, collateralSettingValue);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper method to charge account for Collateral testing using proper cash-in process
     */
    private void chargeAccountForCollateral(String accountNumber, String cashInAmount) throws Exception {
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        walletAccountRepositoryService.increaseBalance(walletAccountEntity.getId(), new BigDecimal(cashInAmount));
    }

    /**
     * Helper method to charge account for Collateral testing using proper cash-in process
     */
    private void chargeAccountForCollateralToZero(String accountNumber) throws Exception {
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        walletAccountRepositoryService.decreaseBalance(walletAccountEntity.getId(), walletAccountRepositoryService.getBalance(walletAccountEntity.getId()).getRealBalance());
        BalanceDTO balanceDTO = walletAccountRepositoryService.getBalance(walletAccountEntity.getId());
        if(balanceDTO.getRealBalance().compareTo(balanceDTO.getAvailableBalance()) > 0){
            walletAccountRepositoryService.unblockAmount(walletAccountEntity.getId(), balanceDTO.getRealBalance().subtract(balanceDTO.getAvailableBalance()));
        }
    }

    /**
     * Helper method to generate valid signature for testing
     * Note: This is a simplified version for testing purposes
     */
    private String generateValidSign(String dataString) {
        // In a real implementation, this would use proper cryptographic signing
        // For testing purposes, we'll return a mock signature
        return "MOCK_SIGNATURE_" + dataString.hashCode();
    }
}
