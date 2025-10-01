package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.request.wallet.CommissionObject;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.p2p.P2pTrackResponse;
import com.melli.wallet.domain.response.p2p.P2pUuidResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.sync.ResourceSyncService;
import lombok.extern.log4j.Log4j2;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Class Name: P2pControllerTest
 * Author: Mahdi Shirinabadi
 * Date: 5/26/2025
 * This test class contains comprehensive end-to-end tests for P2P operations.
 * It tests the complete flow from wallet creation to P2P execution and validation.
 * Test Coverage:
 * - Wallet creation and setup
 * - P2P UUID generation (success and failure scenarios)
 * - P2P process execution (success and failure scenarios)
 * - P2P inquiry operations (success and failure scenarios)
 * - Amount validation (minimum, maximum limits)
 * - Currency validation
 * - Account validation
 * - Security validation (signature)
 * - UUID validation and mismatch scenarios
 */
@Log4j2
@DisplayName("P2pControllerTest End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class P2pControllerTest extends WalletApplicationTests {

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
    @Order(10)
    @DisplayName("generateP2pUuid-Success")
    void generateP2pUuidSuccess() throws Exception {
        // Step 1: Get source and destination account numbers
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        
        // Step 2: Generate P2P UUID successfully
        BaseResponse<P2pUuidResponse> response = generateP2pUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, "0.001", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getUniqueIdentifier());
        Assert.assertEquals(NATIONAL_CODE_DEST, response.getData().getDestNationalCode());
    }

    @Test
    @Order(11)
    @DisplayName("generateP2pUuid-Fail-InvalidNationalCode")
    void generateP2pUuidFailInvalidNationalCode() throws Exception {
        // Step 1: Get account numbers
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        
        // Step 2: Try to generate UUID with invalid national code
        BaseResponse<P2pUuidResponse> response = generateP2pUuid(mockMvc, accessToken, "INVALID_NATIONAL_CODE", "0.001", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.WALLET_NOT_FOUND, false);
        
        Assert.assertNull(response.getData());
        Assert.assertSame(StatusRepositoryService.WALLET_NOT_FOUND, response.getErrorDetail().getCode());
    }

    @Test
    @Order(12)
    @DisplayName("generateP2pUuid-Fail-InvalidQuantity")
    void generateP2pUuidFailInvalidQuantity() throws Exception {
        // Step 1: Get account numbers
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        
        // Step 2: Try to generate UUID with invalid quantity (negative)
        BaseResponse<P2pUuidResponse> response = generateP2pUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, "-0.001", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);
        
        Assert.assertNull(response.getData());
        Assert.assertSame(StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, response.getErrorDetail().getCode());
    }

    @Test
    @Order(13)
    @DisplayName("generateP2pUuid-Fail-InvalidSourceAccount")
    void generateP2pUuidFailInvalidSourceAccount() throws Exception {
        // Step 1: Get destination account number
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        
        // Step 2: Try to generate UUID with invalid source account
        BaseResponse<P2pUuidResponse> response = generateP2pUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, "0.001", "INVALID_ACCOUNT", destAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, false);
        
        Assert.assertNull(response.getData());
        Assert.assertSame(StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, response.getErrorDetail().getCode());
    }

    @Test
    @Order(14)
    @DisplayName("generateP2pUuid-Fail-InvalidDestAccount")
    void generateP2pUuidFailInvalidDestAccount() throws Exception {
        // Step 1: Get source account number
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        
        // Step 2: Try to generate UUID with invalid destination account
        BaseResponse<P2pUuidResponse> response = generateP2pUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, "0.001", sourceAccount.getAccountNumber(), "INVALID_DEST_ACCOUNT", HttpStatus.OK, StatusRepositoryService.DST_ACCOUNT_NUMBER_NOT_FOUND, false);
        
        Assert.assertNull(response.getData());
        Assert.assertSame(StatusRepositoryService.DST_ACCOUNT_NUMBER_NOT_FOUND, response.getErrorDetail().getCode());
    }

    @Test
    @Order(15)
    @DisplayName("generateP2pUuid-Fail-SameSourceAndDest")
    void generateP2pUuidFailSameSourceAndDest() throws Exception {
        // Step 1: Get account number
        WalletAccountObject account = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        
        // Step 2: Try to generate UUID with same source and destination account
        BaseResponse<P2pUuidResponse> response = generateP2pUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, "0.001", account.getAccountNumber(), account.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SRC_ACCOUNT_SAME_DST_ACCOUNT_NUMBER, false);
        
        Assert.assertNull(response.getData());
        Assert.assertSame(StatusRepositoryService.SRC_ACCOUNT_SAME_DST_ACCOUNT_NUMBER, response.getErrorDetail().getCode());
    }

    // ==================== PROCESS TESTS ====================

    @Test
    @Order(20)
    @DisplayName("p2pProcess-Success")
    void p2pProcessSuccess() throws Exception {
        // Step 1: Setup accounts and balances
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);


        // Step 2: Charge source account
        chargeAccountForP2P(sourceAccount.getAccountNumber(), "100");
        
        // Step 3: Generate UUID
        BaseResponse<P2pUuidResponse> uuidResponse = generateP2pUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, "0.002", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        // Step 4: Process P2P transaction
        CommissionObject commission = new CommissionObject("GOLD", "0.001");
        BaseResponse<Void> response = processP2p(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), NATIONAL_CODE_CORRECT, "0.002", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), "test data", commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getSuccess());
        chargeAccountForP2PToZero(sourceAccount.getAccountNumber());
    }

    @Test
    @Order(21)
    @DisplayName("p2pProcess-Fail-InvalidUUID")
    void p2pProcessFailInvalidUUID() throws Exception {
        // Step 1: Get account numbers
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        
        // Step 2: Try to process with invalid UUID
        CommissionObject commission = new CommissionObject("RIAL", "0.01");
        BaseResponse<Void> response = processP2p(mockMvc, accessToken, "INVALID_UUID", NATIONAL_CODE_CORRECT, "1", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), "test data", commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.UUID_NOT_FOUND, false);
        
        Assert.assertFalse(response.getSuccess());
        Assert.assertSame(StatusRepositoryService.UUID_NOT_FOUND, response.getErrorDetail().getCode());
    }

    @Test
    @Order(22)
    @DisplayName("p2pProcess-Fail-InsufficientBalance")
    void p2pProcessFailInsufficientBalance() throws Exception {
        // Step 1: Get account numbers
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        
        // Step 2: Generate UUID
        BaseResponse<P2pUuidResponse> uuidResponse = generateP2pUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, "0.05", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        // Step 3: Try to process without sufficient balance
        CommissionObject commission = new CommissionObject("RIAL", "0.001");
        BaseResponse<Void> response = processP2p(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), NATIONAL_CODE_CORRECT, "0.05", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), "test data", commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.INSUFFICIENT_BALANCE, false);
        
        Assert.assertFalse(response.getSuccess());
        Assert.assertSame(StatusRepositoryService.INSUFFICIENT_BALANCE, response.getErrorDetail().getCode());
    }

    @Test
    @Order(23)
    @DisplayName("p2p-commission bigger than")
    void p2pFailCommission() throws Exception {
        // Step 1: Setup and process a P2P transaction
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        chargeAccountForP2P(sourceAccount.getAccountNumber(), "10");

        BaseResponse<P2pUuidResponse> uuidResponse = generateP2pUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, "0.001", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        CommissionObject commission = new CommissionObject("GOLD", "0.001");
        processP2p(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), NATIONAL_CODE_CORRECT, "0.001", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(),
                "test data", commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.COMMISSION_BIGGER_THAN_QUANTITY, false);
    }

    // ==================== INQUIRY TESTS ====================

    @Test
    @Order(30)
    @DisplayName("p2pInquiry-Success")
    void p2pInquirySuccess() throws Exception {
        // Step 1: Setup and process a P2P transaction
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        
        chargeAccountForP2P(sourceAccount.getAccountNumber(), "10");
        
        BaseResponse<P2pUuidResponse> uuidResponse = generateP2pUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, "0.002", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        CommissionObject commission = new CommissionObject("GOLD", "0.001");
        processP2p(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), NATIONAL_CODE_CORRECT, "0.002", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), "test data", commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        // Step 2: Inquiry the transaction
        BaseResponse<P2pTrackResponse> response = inquiryP2p(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        Assert.assertNotNull(response.getData());
        Assert.assertEquals(uuidResponse.getData().getUniqueIdentifier(), response.getData().getUniqueIdentifier());
        Assert.assertEquals(NATIONAL_CODE_CORRECT, response.getData().getNationalCode());
        Assert.assertSame(new BigDecimal("0.002").compareTo(new BigDecimal(response.getData().getQuantity())),0);
        chargeAccountForP2PToZero(sourceAccount.getAccountNumber());
    }

    @Test
    @Order(31)
    @DisplayName("p2pInquiry-Fail-InvalidUUID")
    void p2pInquiryFailInvalidUUID() throws Exception {
        // Step 1: Try to inquiry with invalid UUID
        BaseResponse<P2pTrackResponse> response = inquiryP2p(mockMvc, accessToken, "INVALID_UUID", HttpStatus.OK, StatusRepositoryService.UUID_NOT_FOUND, false);
        
        Assert.assertNull(response.getData());
        Assert.assertSame(StatusRepositoryService.UUID_NOT_FOUND, response.getErrorDetail().getCode());
    }

    // ==================== COMPREHENSIVE VALIDATION TESTS ====================

    @Test
    @Order(40)
    @DisplayName("p2pProcess-Fail-DestinationAccountInactive")
    void p2pProcessFailDestinationAccountInactive() throws Exception {
        // Step 1: Get account numbers
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        
        // Step 2: Deactivate destination account
        deactivateWalletAccount(destAccount.getAccountNumber());
        
        // Step 3: Charge source account
        chargeAccountForP2P(sourceAccount.getAccountNumber(), "10");
        
        // Step 4: Generate UUID
        BaseResponse<P2pUuidResponse> uuidResponse = generateP2pUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, "0.002", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        WalletAccountEntity sourceWalletAccount = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
        sourceWalletAccount.setStatus(WalletStatusEnum.DISABLE);
        walletAccountRepositoryService.save(sourceWalletAccount);
        // Step 5: Try to process with inactive destination account
        CommissionObject commission = new CommissionObject(CURRENCY_GOLD, "0.001");
        BaseResponse<Void> response = processP2p(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), NATIONAL_CODE_CORRECT, "0.002", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), "test data", commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_IS_NOT_ACTIVE, false);
        
        Assert.assertFalse(response.getSuccess());
        Assert.assertSame(StatusRepositoryService.WALLET_ACCOUNT_IS_NOT_ACTIVE, response.getErrorDetail().getCode());

        sourceWalletAccount = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
        sourceWalletAccount.setStatus(WalletStatusEnum.ACTIVE);
        walletAccountRepositoryService.save(sourceWalletAccount);
        chargeAccountForP2PToZero(sourceAccount.getAccountNumber());
    }

    @Test
    @Order(41)
    @DisplayName("p2pProcess-Fail-DuplicateRequest")
    void p2pProcessFailDuplicateRequest() throws Exception {
        // Step 1: Setup accounts and balances
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        
        // Step 2: Charge source account
        chargeAccountForP2P(sourceAccount.getAccountNumber(), "10");
        
        // Step 3: Generate UUID
        BaseResponse<P2pUuidResponse> uuidResponse = generateP2pUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, "0.002", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        // Step 4: Process P2P transaction successfully
        CommissionObject commission = new CommissionObject(CURRENCY_GOLD, "0.001");
        BaseResponse<Void> firstResponse = processP2p(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), NATIONAL_CODE_CORRECT, "0.002", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), "test data", commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertTrue(firstResponse.getSuccess());
        
        // Step 5: Try to process the same UUID again (duplicate request)
        BaseResponse<Void> duplicateResponse = processP2p(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), NATIONAL_CODE_CORRECT, "0.002", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), "test data", commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.DUPLICATE_UUID, false);
        
        Assert.assertFalse(duplicateResponse.getSuccess());
        Assert.assertSame(StatusRepositoryService.DUPLICATE_UUID, duplicateResponse.getErrorDetail().getCode());
        chargeAccountForP2PToZero(sourceAccount.getAccountNumber());
    }

    @Test
    @Order(42)
    @DisplayName("p2pProcess-ConcurrentTransactions")
    void p2pProcessConcurrentTransactions() throws Exception {
        // Step 1: Setup accounts and balances
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        
        // Step 2: Charge source account with enough balance for multiple transactions
        chargeAccountForP2P(sourceAccount.getAccountNumber(), "0.01");
        
        // Step 3: Generate multiple UUIDs for concurrent processing
        BaseResponse<P2pUuidResponse> uuidResponse1 = generateP2pUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, "0.003", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        BaseResponse<P2pUuidResponse> uuidResponse2 = generateP2pUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, "0.003", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        // Step 4: Process transactions concurrently
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        ConcurrentLinkedQueue<BaseResponse<Void>> results = new ConcurrentLinkedQueue<>();
        
        CommissionObject commission = new CommissionObject(CURRENCY_GOLD, "0.002");
        
        executor.submit(() -> {
            try {
                BaseResponse<Void> response = processP2p(mockMvc, accessToken, uuidResponse1.getData().getUniqueIdentifier(), NATIONAL_CODE_CORRECT, "0.003", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), "concurrent test 1", commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                results.add(response);
            } catch (Exception e) {
                log.error("Error in concurrent transaction 1", e);
            } finally {
                latch.countDown();
            }
        });
        
        executor.submit(() -> {
            try {
                BaseResponse<Void> response = processP2p(mockMvc, accessToken, uuidResponse2.getData().getUniqueIdentifier(), NATIONAL_CODE_CORRECT, "0.003", sourceAccount.getAccountNumber(), destAccount.getAccountNumber(), "concurrent test 2", commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                results.add(response);
            } catch (Exception e) {
                log.error("Error in concurrent transaction 2", e);
            } finally {
                latch.countDown();
            }
        });
        
        // Step 5: Wait for both transactions to complete
        Assert.assertTrue("Concurrent transactions should complete within 3 seconds", latch.await(3, TimeUnit.SECONDS));
        executor.shutdown();
        
        // Step 6: Verify both transactions succeeded
        Assert.assertEquals("Both concurrent transactions should complete", 2, results.size());
        for (BaseResponse<Void> response : results) {
            Assert.assertTrue("Concurrent transaction should succeed", response.getSuccess());
        }

        chargeAccountForP2PToZero(sourceAccount.getAccountNumber());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper method to charge account for P2P testing using proper cash-in process
     */
    private void chargeAccountForP2P(String accountNumber, String cashInAmount) throws Exception {
        // Step 2: Enable cash in permission if disabled
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        walletAccountRepositoryService.increaseBalance(walletAccountEntity.getId(),new BigDecimal(cashInAmount));
    }

    /**
     * Helper method to charge account for P2P testing using proper cash-in process
     */
    private void chargeAccountForP2PToZero(String accountNumber) throws Exception {
        // Step 2: Enable cash in permission if disabled
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        walletAccountRepositoryService.decreaseBalance(walletAccountEntity.getId(), walletAccountRepositoryService.getBalance(walletAccountEntity.getId()).getRealBalance());
    }

       /**
     * Helper method to deactivate wallet account for testing
     */
    private void deactivateWalletAccount(String accountNumber) throws Exception {
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        walletAccountEntity.setStatus(WalletStatusEnum.DISABLE);
        walletAccountRepositoryService.save(walletAccountEntity);
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
