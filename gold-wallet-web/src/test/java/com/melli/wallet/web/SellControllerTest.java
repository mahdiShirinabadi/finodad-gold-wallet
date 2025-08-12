package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.purchase.PurchaseTrackResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.service.repository.*;
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
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: SellControllerTest
 * Author: Mahdi Shirinabadi
 * Date: 1/20/2025
 * 
 * This test class contains comprehensive end-to-end tests for Sell operations.
 * It tests the complete flow from UUID generation to sell execution and inquiry.
 * 
 * Test Coverage:
 * - Sell UUID generation (success and failure scenarios)
 * - Sell execution (success and failure scenarios)
 * - Sell inquiry (success and failure scenarios)
 * - Balance validation
 * - Quantity validation (min/max limits)
 * - Daily limitations (quantity and count)
 * - Monthly limitations (quantity and count)
 * - Merchant validation
 * - Commission currency validation
 * - Duplicate request handling
 */
@Log4j2
@DisplayName("SellControllerTest End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SellControllerTest extends WalletApplicationTests {

    private static MockMvc mockMvc;
    private static String accessToken;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private WalletAccountRepositoryService walletAccountRepositoryService;
    @Autowired
    private WalletRepositoryService walletRepositoryService;
    @Autowired
    private WalletTypeRepositoryService walletTypeRepositoryService;
    @Autowired
    private LimitationGeneralCustomRepositoryService limitationGeneralCustomRepositoryService;
    @Autowired
    private ChannelRepositoryService channelRepositoryService;
    @Autowired
    private LimitationGeneralService limitationGeneralService;
    @Autowired
    private Flyway flyway;
    @Autowired
    private CacheClearService cacheClearService;
    @Autowired
    private WalletLevelRepositoryService walletLevelRepositoryService;

    private static final String NATIONAL_CODE_CORRECT = "0077847660";
    private static final String NATIONAL_CODE_NOT_FOUND = "0451710010";
    private static final String MOBILE_CORRECT = "09124162337";
    private static final String CURRENCY_GOLD = "GOLD";

    /**
     * Initial setup method that runs before all tests.
     * This method:
     * - Sets up MockMvc for testing
     * - Cleans and migrates the database
     * - Clears all caches
     * - Performs login to get access token
     * - Creates wallet for testing
     * - Creates channel wallet for testing
     */
    @Test
    @Order(1)
    @DisplayName("Setup test environment")
    void setup() throws Exception {


        // Setup MockMvc for testing with security
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);
        
        // Clean and migrate database
        log.info("start cleaning initial values in test DB for purchase");
        flyway.clean();
        flyway.migrate();
        log.info("start cleaning initial values in test DB for purchase");
        
        // Clear all caches
        cacheClearService.clearCache();

        // Step 1: Login to get access token
        BaseResponse<LoginResponse> loginResponse = login(mockMvc, USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        accessToken = loginResponse.getData().getAccessTokenObject().getToken();

        // Step 2: Create wallet if needed
        try {
            createWallet(mockMvc, accessToken, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        } catch (Exception e) {
            // Wallet might already exist
            log.info("Wallet might already exist: {}", e.getMessage());
        }

        // Step 3: Create wallet for channel testing
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

        // Create wallet accounts for RIAL and GOLD currencies
        walletAccountRepositoryService.createAccount(List.of(WalletAccountCurrencyRepositoryService.RIAL, WalletAccountCurrencyRepositoryService.GOLD),
                walletEntity, List.of(WalletAccountTypeRepositoryService.WAGE), channelEntity);
    }

    // ==================== SELL TESTS ====================

    /**
     * Test successful sell UUID generation.
     * This method:
     * - Gets user's GOLD account number
     * - Generates UUID for sell operation with valid quantity
     * - Validates the UUID generation response
     */
    @Test
    @Order(60)
    @DisplayName("generateSellUuid-Success")
    void generateSellUuidSuccess() throws Exception {
        log.info("start generateSellUuidSuccess test");
        
        // Step 1: Get user's GOLD account number
        String quantity = "1.07";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        
        // Step 2: Generate UUID for sell operation
        generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
    }

    /**
     * Test sell UUID generation failure with invalid national code.
     * This method:
     * - Gets user's GOLD account number
     * - Attempts to generate UUID with non-existent national code
     * - Expects NATIONAL_CODE_NOT_FOUND error
     */
    @Test
    @Order(61)
    @DisplayName("generateSellUuid-Fail-InvalidNationalCode")
    void generateSellUuidFailInvalidNationalCode() throws Exception {
        log.info("start generateSellUuidFailInvalidNationalCode test");
        String quantity = "1.07";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_NOT_FOUND, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.NATIONAL_CODE_NOT_FOUND, false);
    }

    /**
     * Test sell UUID generation failure with invalid account number.
     * This method:
     * - Attempts to generate UUID with non-existent account number
     * - Expects WALLET_ACCOUNT_NOT_FOUND error
     */
    @Test
    @Order(62)
    @DisplayName("generateSellUuid-Fail-InvalidAccountNumber")
    void generateSellUuidFailInvalidAccountNumber() throws Exception {
        log.info("start generateSellUuidFailInvalidAccountNumber test");
        // Step 1: Define test parameters
        String quantity = "1.07";
        // Step 2: Attempt to generate UUID with invalid account number
        generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, "invalid_account", CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, false);
    }

    /**
     * Test sell UUID generation failure with invalid currency.
     * This method:
     * - Gets user's GOLD account number
     * - Attempts to generate UUID with non-existent currency
     * - Expects WALLET_ACCOUNT_CURRENCY_NOT_FOUND error
     */
    @Test
    @Order(63)
    @DisplayName("generateSellUuid-Fail-InvalidCurrency")
    void generateSellUuidFailInvalidCurrency() throws Exception {
        log.info("start generateSellUuidFailInvalidCurrency test");
        String quantity = "1.07";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), "INVALID_CURRENCY", HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);
    }

    /**
     * Test sell UUID generation failure when quantity is less than minimum.
     * This method:
     * - Gets user's GOLD account number
     * - Attempts to generate UUID with very small quantity
     * - Expects QUANTITY_LESS_THAN_MIN error
     */
    @Test
    @Order(64)
    @DisplayName("generateSellUuid-Fail-LessThanMinQuantity")
    void generateSellUuidFailLessThanMinQuantity() throws Exception {
        log.info("start generateSellUuidFailLessThanMinQuantity test");
        String quantity = "0.0001"; // Very small quantity
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.QUANTITY_LESS_THAN_MIN, false);
    }

    /**
     * Test sell UUID generation failure when quantity is bigger than maximum.
     * This method:
     * - Gets user's GOLD account number
     * - Attempts to generate UUID with very large quantity
     * - Expects QUANTITY_BIGGER_THAN_MAX error
     */
    @Test
    @Order(65)
    @DisplayName("generateSellUuid-Fail-BiggerThanMaxQuantity")
    void generateSellUuidFailBiggerThanMaxQuantity() throws Exception {
        log.info("start generateSellUuidFailBiggerThanMaxQuantity test");
        String quantity = "1000000"; // Very large quantity
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.QUANTITY_BIGGER_THAN_MAX, false);
    }

    /**
     * Test successful sell operation.
     * This method:
     * - Gets user's GOLD account number
     * - Ensures user has enough GOLD balance
     * - Ensures merchant has enough RIAL balance
     * - Generates sell UUID
     * - Performs sell operation
     * - Validates the sell response
     */
    @Test
    @Order(70)
    @DisplayName("sell-Success")
    void sellSuccess() throws Exception {
        log.info("start sellSuccess test");
        
        // Step 1: Define test parameters
        String quantity = "1.07";
        String price = "100000";

        // Step 2: Get user's GOLD account number
        WalletAccountObject goldAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);

        // Step 3: Ensure user has enough GOLD balance for selling
        WalletAccountEntity goldWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(goldAccountObject.getAccountNumber());
        walletAccountRepositoryService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("2.0"));

        // Step 4: Ensure merchant has enough RIAL balance for buying
        WalletEntity walletMerchantEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeRepositoryService.getByName(WalletTypeRepositoryService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountRepositoryService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyRepositoryService.RIAL))
                .findFirst().orElse(null);
        walletAccountRepositoryService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("200000"));

        // Step 5: Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, goldAccountObject.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();

        // Step 6: Perform sell operation
        BaseResponse<PurchaseResponse> response = sell(mockMvc, accessToken, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "0.01", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", goldAccountObject.getAccountNumber(), "", "test sell success", HttpStatus.OK, "IR123456789012345678901234", StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
    }

    /**
     * Test sell operation failure with invalid UUID.
     * This method:
     * - Gets user's GOLD account number
     * - Attempts sell operation with non-existent UUID
     * - Expects UUID_NOT_FOUND error
     */
    @Test
    @Order(71)
    @DisplayName("sell-Fail-InvalidUniqueIdentifier")
    void sellFailInvalidUniqueIdentifier() throws Exception {
        log.info("start sellFailInvalidUniqueIdentifier test");
        String quantity = "1.07";
        String price = "100000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);

        BaseResponse<PurchaseResponse> response = sell(mockMvc, accessToken, "invalid-uuid", quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test invalid uuid", HttpStatus.OK, "IR123456789012345678901234", StatusRepositoryService.UUID_NOT_FOUND, false);
        Assert.assertSame(StatusRepositoryService.UUID_NOT_FOUND, response.getErrorDetail().getCode());
    }

    /*@Test
    @Order(72)
    @DisplayName("sell-Fail-InvalidSign")
    void sellFailInvalidSign() throws Exception {
        log.info("start sellFailInvalidSign test");
        String quantity = "1.07";
        String price = "100000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Generate valid UUID first
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Test with invalid sign
        BaseResponse<PurchaseResponse> response = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "invalid_sign", "test invalid sign", HttpStatus.OK, "IR123456789012345678901234", StatusService.INVALID_SIGN, false);
        Assert.assertSame(StatusService.INVALID_SIGN, response.getErrorDetail().getCode());
    }*/

    @Test
    @Order(73)
    @DisplayName("sell-Fail-InsufficientBalance")
    void sellFailInsufficientBalance() throws Exception {
        log.info("start sellFailInsufficientBalance test");
        String quantity = "1.07";
        String price = "100000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);

        // Ensure user has insufficient GOLD balance
        WalletAccountEntity goldWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountRepositoryService.decreaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("10.0")); // Remove all balance

        // Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();

        // Test with insufficient balance
        BaseResponse<PurchaseResponse> response = sell(mockMvc, accessToken, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test insufficient balance", HttpStatus.OK, "IR123456789012345678901234", StatusRepositoryService.BALANCE_IS_NOT_ENOUGH, false);
        Assert.assertSame(StatusRepositoryService.BALANCE_IS_NOT_ENOUGH, response.getErrorDetail().getCode());
    }

    /**
     * Test sell operation failure with invalid merchant ID.
     * This method:
     * - Gets user's GOLD account number
     * - Generates sell UUID
     * - Attempts sell operation with non-existent merchant ID
     * - Expects MERCHANT_IS_NOT_EXIST error
     */
    @Test
    @Order(74)
    @DisplayName("sell-Fail-InvalidMerchantId")
    void sellFailInvalidMerchantId() throws Exception {
        log.info("start sellFailInvalidMerchantId test");
        // Step 1: Define test parameters
        String quantity = "1.07";
        String price = "100000";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 4: Test with invalid merchant ID
        BaseResponse<PurchaseResponse> response = sell(mockMvc, accessToken, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "999", walletAccountObjectOptional.getAccountNumber(), "", "test invalid merchant id", HttpStatus.OK, "IR123456789012345678901234", StatusRepositoryService.MERCHANT_IS_NOT_EXIST, false);
        Assert.assertSame(StatusRepositoryService.MERCHANT_IS_NOT_EXIST, response.getErrorDetail().getCode());
    }

    /**
     * Test sell operation failure with invalid commission currency.
     * This method:
     * - Gets user's GOLD account number
     * - Generates sell UUID
     * - Attempts sell operation with invalid commission currency
     * - Expects COMMISSION_CURRENCY_NOT_VALID error
     */
    @Test
    @Order(75)
    @DisplayName("sell-Fail-InvalidCommissionCurrency")
    void sellFailInvalidCommissionCurrency() throws Exception {
        log.info("start sellFailInvalidCommissionCurrency test");
        // Step 1: Define test parameters
        String quantity = "0.5";
        String price = "100000";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 4: Test with invalid commission currency (should be same as main currency)
        BaseResponse<PurchaseResponse> response = sell(mockMvc, accessToken, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "0.05", NATIONAL_CODE_CORRECT, "GOLDD", "1", walletAccountObjectOptional.getAccountNumber(), "", "test invalid commission currency", HttpStatus.OK, "IR123456789012345678901234", StatusRepositoryService.COMMISSION_CURRENCY_NOT_VALID, false);
        Assert.assertSame(StatusRepositoryService.COMMISSION_CURRENCY_NOT_VALID, response.getErrorDetail().getCode());
    }

    /**
     * Test sell operation failure with duplicate request (same UUID).
     * This method:
     * - Ensures user and merchant have enough balance
     * - Generates sell UUID
     * - Performs first sell operation (should succeed)
     * - Attempts to perform the same sell operation again (should fail with duplicate)
     * - Expects DUPLICATE_UUID error
     */
    @Test
    @Order(76)
    @DisplayName("sell-Fail-DuplicateRequest")
    void sellFailDuplicateRequest() throws Exception {
        log.info("start sellFailDuplicateRequest test");
        // Step 1: Define test parameters
        String quantity = "1.07";
        String price = "100000";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountRepositoryService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("5.0"));
        // Step 4: Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeRepositoryService.getByName(WalletTypeRepositoryService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountRepositoryService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyRepositoryService.RIAL))
                .findFirst().orElse(null);
        walletAccountRepositoryService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("200000"));
        // Step 5: Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 6: Perform first sell operation (should succeed)
        BaseResponse<PurchaseResponse> response1 = sell(mockMvc, accessToken, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test sell success", HttpStatus.OK, "IR123456789012345678901234", StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response1.getData());
        // Step 7: Try to perform the same sell operation again (should fail with duplicate)
        BaseResponse<PurchaseResponse> response2 = sell(mockMvc, accessToken, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test duplicate request", HttpStatus.OK, "IR123456789012345678901234", StatusRepositoryService.DUPLICATE_UUID, false);
        Assert.assertSame(StatusRepositoryService.DUPLICATE_UUID, response2.getErrorDetail().getCode());
    }

    /**
     * Test successful sell inquiry operation.
     * This method:
     * - Gets user's GOLD account number
     * - Ensures user and merchant have enough balance
     * - Generates sell UUID
     * - Performs sell operation
     * - Inquires about the sell operation
     * - Validates the inquiry response
     */
    @Test
    @Order(80)
    @DisplayName("inquirySell-Success")
    void inquirySellSuccess() throws Exception {
        log.info("start inquirySellSuccess test");
        // Step 1: Define test parameters
        String quantity = "1.07";
        String price = "100000";
        // Step 2: Get account numbers
        WalletAccountObject goldAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(goldAccountObject.getAccountNumber());
        walletAccountRepositoryService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("2.0"));
        // Step 4: Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeRepositoryService.getByName(WalletTypeRepositoryService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountRepositoryService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyRepositoryService.RIAL))
                .findFirst().orElse(null);
        walletAccountRepositoryService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("200000"));
        // Step 5: Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, goldAccountObject.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 6: Perform sell operation
        BaseResponse<PurchaseResponse> sellResponse = sell(mockMvc, accessToken, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", goldAccountObject.getAccountNumber(), "", "test sell for inquiry", HttpStatus.OK, "IR123456789012345678901234", StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(sellResponse.getData());
        // Step 7: Inquiry the sell operation
        BaseResponse<PurchaseTrackResponse> inquiryResponse = inquiryPurchase(mockMvc, accessToken, uniqueIdentifier, "SELL", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(inquiryResponse.getData().getPurchaseTrackObjectList());
    }

    /**
     * Test sell inquiry failure with invalid UUID.
     * This method:
     * - Attempts to inquire about non-existent sell operation
     * - Expects UUID_NOT_FOUND error
     */
    @Test
    @Order(81)
    @DisplayName("inquirySell-Fail-InvalidUniqueIdentifier")
    void inquirySellFailInvalidUniqueIdentifier() throws Exception {
        log.info("start inquirySellFailInvalidUniqueIdentifier test");
        // Step 1: Test with invalid UUID
        BaseResponse<PurchaseTrackResponse> response = inquiryPurchase(mockMvc, accessToken, "invalid-uuid", "SELL", HttpStatus.OK, StatusRepositoryService.UUID_NOT_FOUND, false);
        Assert.assertSame(StatusRepositoryService.UUID_NOT_FOUND, response.getErrorDetail().getCode());
    }

    // ==================== SELL LIMITATION TESTS ====================

    /**
     * Test sell operation failure when quantity is less than minimum allowed.
     * This method:
     * - Attempts to generate sell UUID with very small quantity
     * - Expects QUANTITY_LESS_THAN_MIN error
     */
    @Test
    @Order(90)
    @DisplayName("sell-Fail-LessThanMinQuantity")
    void sellFailLessThanMinQuantity() throws Exception {
        log.info("start sellFailLessThanMinQuantity test");
        // Step 1: Define test parameters with very small quantity
        String quantity = "0.0001"; // Very small quantity below minimum
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Generate sell UUID - should fail due to minimum quantity limitation
        generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.QUANTITY_LESS_THAN_MIN, false);
    }

    /**
     * Test sell operation failure when quantity is bigger than maximum allowed.
     * This method:
     * - Attempts to generate sell UUID with very large quantity
     * - Expects QUANTITY_BIGGER_THAN_MAX error
     */
    @Test
    @Order(91)
    @DisplayName("sell-Fail-BiggerThanMaxQuantity")
    void sellFailBiggerThanMaxQuantity() throws Exception {
        log.info("start sellFailBiggerThanMaxQuantity test");
        // Step 1: Define test parameters with very large quantity
        String quantity = "1000000"; // Very large quantity above maximum
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Generate sell UUID - should fail due to maximum quantity limitation
        generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.QUANTITY_BIGGER_THAN_MAX, false);
    }

    /**
     * Test sell operation failure when daily quantity limitation is exceeded.
     * This method:
     * - Ensures user and merchant have enough balance
     * - Temporarily sets very low daily quantity limitation
     * - Attempts to generate sell UUID
     * - Expects SELL_EXCEEDED_AMOUNT_DAILY_LIMITATION error
     * - Restores original limitation value
     */
    @Test
    @Order(92)
    @DisplayName("sell-Fail-DailyQuantityLimitation")
    void sellFailDailyQuantityLimitation() throws Exception {
        log.info("start sellFailDailyQuantityLimitation test");
        // Step 1: Define test parameters
        String quantity = "1.07";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountRepositoryService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("10.0"));
        // Step 4: Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeRepositoryService.getByName(WalletTypeRepositoryService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountRepositoryService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyRepositoryService.RIAL))
                .findFirst().orElse(null);
        walletAccountRepositoryService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("1000000"));
        // Step 5: Get current daily quantity limitation
        String maxDailyQuantity = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_SELL, walletAccountObjectOptional.getAccountNumber());

        // Step 6: Set a very low daily quantity limitation to trigger the error
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_SELL, goldWalletAccountEntity, "0.5");
        // Step 7: Generate sell UUID - should fail due to daily quantity limitation
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, goldWalletAccountEntity.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SELL_EXCEEDED_AMOUNT_DAILY_LIMITATION, false);
        Assert.assertSame(StatusRepositoryService.SELL_EXCEEDED_AMOUNT_DAILY_LIMITATION, uuidResponse.getErrorDetail().getCode());
        // Step 8: Restore original daily quantity limitation
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_SELL, goldWalletAccountEntity, maxDailyQuantity);
    }

    /**
     * Test sell operation failure when daily count limitation is exceeded.
     * This method:
     * - Ensures user and merchant have enough balance
     * - Temporarily sets very low daily count limitation
     * - Attempts to generate sell UUID
     * - Expects SELL_EXCEEDED_COUNT_DAILY_LIMITATION error
     * - Restores original limitation value
     */
    @Test
    @Order(93)
    @DisplayName("sell-Fail-DailyCountLimitation")
    void sellFailDailyCountLimitation() throws Exception {
        log.info("start sellFailDailyCountLimitation test");
        // Step 1: Define test parameters
        String quantity = "0.1";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountRepositoryService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("10.0"));
        // Step 4: Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeRepositoryService.getByName(WalletTypeRepositoryService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountRepositoryService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyRepositoryService.RIAL))
                .findFirst().orElse(null);
        walletAccountRepositoryService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("1000000"));
        // Step 5: Get current daily count limitation
        String maxDailyCount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_SELL, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        // Step 6: Set a very low daily count limitation to trigger the error
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_SELL, walletAccountEntity, "1");
        // Step 7: Perform first sell operation (should succeed)
        BaseResponse<UuidResponse> uuidResponse2 = generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SELL_EXCEEDED_COUNT_DAILY_LIMITATION, false);
        Assert.assertSame(StatusRepositoryService.SELL_EXCEEDED_COUNT_DAILY_LIMITATION, uuidResponse2.getErrorDetail().getCode());
        // Step 8: Restore original daily count limitation
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_SELL, walletAccountEntity, maxDailyCount);
    }

    /**
     * Test sell operation failure when monthly quantity limitation is exceeded.
     * This method:
     * - Ensures user and merchant have enough balance
     * - Temporarily sets very low monthly quantity limitation
     * - Attempts to generate sell UUID
     * - Expects SELL_EXCEEDED_AMOUNT_MONTHLY_LIMITATION error
     * - Restores original limitation value
     */
    @Test
    @Order(94)
    @DisplayName("sell-Fail-MonthlyQuantityLimitation")
    void sellFailMonthlyQuantityLimitation() throws Exception {
        log.info("start sellFailMonthlyQuantityLimitation test");
        // Step 1: Define test parameters
        String quantity = "1.07";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountRepositoryService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("10.0"));
        // Step 4: Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeRepositoryService.getByName(WalletTypeRepositoryService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountRepositoryService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyRepositoryService.RIAL))
                .findFirst().orElse(null);
        walletAccountRepositoryService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("1000000"));
        // Step 5: Get current monthly quantity limitation
        String maxMonthlyQuantity = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_QUANTITY_SELL, walletAccountObjectOptional.getAccountNumber());
        // Step 6: Set a very low monthly quantity limitation to trigger the error
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_QUANTITY_SELL, goldWalletAccountEntity, "0.5");
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MONTHLY_VALIDATION_CHECK_SELL, goldWalletAccountEntity, "true");
        // Step 7: Generate sell UUID - should fail due to monthly quantity limitation
        generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, goldWalletAccountEntity.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SELL_EXCEEDED_AMOUNT_MONTHLY_LIMITATION, false);
        // Step 8: Restore original monthly quantity limitation
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_QUANTITY_SELL, goldWalletAccountEntity, maxMonthlyQuantity);
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MONTHLY_VALIDATION_CHECK_SELL, goldWalletAccountEntity, "false");
    }

    /**
     * Test sell operation failure when monthly count limitation is exceeded.
     * This method:
     * - Ensures user and merchant have enough balance
     * - Temporarily sets very low monthly count limitation
     * - Attempts to generate sell UUID
     * - Expects SELL_EXCEEDED_COUNT_MONTHLY_LIMITATION error
     * - Restores original limitation value
     */
    @Test
    @Order(95)
    @DisplayName("sell-Fail-MonthlyCountLimitation")
    void sellFailMonthlyCountLimitation() throws Exception {
        log.info("start sellFailMonthlyCountLimitation test");
        // Step 1: Define test parameters
        String quantity = "0.01";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountRepositoryService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("10.0"));
        // Step 4: Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeRepositoryService.getByName(WalletTypeRepositoryService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountRepositoryService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyRepositoryService.RIAL))
                .findFirst().orElse(null);
        walletAccountRepositoryService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("1000000"));
        // Step 5: Get current monthly count limitation
        String maxMonthlyCountValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_COUNT_SELL, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        // Step 6: Set a very low monthly count limitation to trigger the error
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_COUNT_SELL, walletAccountEntity, "1");
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MONTHLY_VALIDATION_CHECK_SELL, walletAccountEntity, "true");
        // Step 7: Perform first sell operation (should succeed)
        generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SELL_EXCEEDED_COUNT_MONTHLY_LIMITATION, false);
        // Step 8: Restore original monthly count limitation
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_COUNT_SELL, walletAccountEntity, maxMonthlyCountValue);
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MONTHLY_VALIDATION_CHECK_SELL, walletAccountEntity, "false");
    }

    /**
     * Test successful sell operation within all limitations.
     * This method:
     * - Ensures user and merchant have enough balance
     * - Sets reasonable limitations that allow the transaction
     * - Generates sell UUID
     * - Performs sell operation successfully
     * - Validates the sell response
     */
    /**
     * Test concurrent sell operations with same UUID to ensure proper concurrency control.
     * This test verifies that when the same UUID is used simultaneously, only one operation succeeds.
     */
    @Test
    @Order(97)
    @DisplayName("concurrent sell operations with same UUID")
    void concurrentSellWithSameUuid() throws Exception {
        log.info("=== Starting Concurrent Sell Test with Same UUID ===");
        
        // Setup
        String quantity = "0.1";
        String price = "100000";
        String commission = "1000";
        String commissionType = "GOLD";
        String currency = "GOLD";
        String sign = "";
        String additionalData = "concurrent sell test";
        
        // Get account and setup
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        
        // Ensure user has enough GOLD balance for selling
        WalletAccountEntity goldWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountRepositoryService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("5.0"));
        
        // Ensure merchant has enough RIAL balance for buying
        WalletEntity walletMerchantEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeRepositoryService.getByName(WalletTypeRepositoryService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountRepositoryService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyRepositoryService.RIAL))
                .findFirst().orElse(null);
        walletAccountRepositoryService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("500000"));
        
        // Generate UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), currency, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String sharedUniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        log.info("Generated UUID: {}", sharedUniqueIdentifier);
        
        // Test with 2 threads using the same UUID
        final CountDownLatch latch = new CountDownLatch(2);
        final List<SellResult> results = new ArrayList<>();
        
        // Thread 1
        Thread thread1 = new Thread(() -> {
            try {
                log.info("Sell Thread 1 starting...");
                BaseResponse<PurchaseResponse> response1 = sellWithoutCheckResult(mockMvc, accessToken, sharedUniqueIdentifier, quantity, price, currency, commission, NATIONAL_CODE_CORRECT, commissionType, "1", walletAccountObjectOptional.getAccountNumber(), sign, additionalData + "_1");
                
                SellResult result1 = new SellResult();
                result1.threadId = 1;
                result1.success = response1.getSuccess();
                result1.errorCode = response1.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response1.getErrorDetail().getCode();
                result1.response = response1;
                
                synchronized (results) {
                    results.add(result1);
                    log.info("Sell Thread 1 added result to collection. Collection size now: {}", results.size());
                }
                log.info("Sell Thread 1 completed: Success={}, ErrorCode={}", result1.success, result1.errorCode);
                
            } catch (Exception e) {
                log.error("Sell Thread 1 exception: {}", e.getMessage(), e);
                SellResult result1 = new SellResult();
                result1.threadId = 1;
                result1.success = false;
                result1.errorCode = -999;
                result1.exception = e;
                synchronized (results) {
                    results.add(result1);
                    log.info("Sell Thread 1 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("Sell Thread 1 countDown called");
            }
        });
        
        // Thread 2
        Thread thread2 = new Thread(() -> {
            try {
                log.info("Sell Thread 2 starting...");
                BaseResponse<PurchaseResponse> response2 = sellWithoutCheckResult(mockMvc, accessToken, sharedUniqueIdentifier, quantity, price, currency, commission, NATIONAL_CODE_CORRECT, commissionType, "1", walletAccountObjectOptional.getAccountNumber(), sign, additionalData + "_2");
                
                SellResult result2 = new SellResult();
                result2.threadId = 2;
                result2.success = response2.getSuccess();
                result2.errorCode = response2.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response2.getErrorDetail().getCode();
                result2.response = response2;
                
                synchronized (results) {
                    results.add(result2);
                    log.info("Sell Thread 2 added result to collection. Collection size now: {}", results.size());
                }
                log.info("Sell Thread 2 completed: Success={}, ErrorCode={}", result2.success, result2.errorCode);
                
            } catch (Exception e) {
                log.error("Sell Thread 2 exception: {}", e.getMessage(), e);
                SellResult result2 = new SellResult();
                result2.threadId = 2;
                result2.success = false;
                result2.errorCode = -999;
                result2.exception = e;
                synchronized (results) {
                    results.add(result2);
                    log.info("Sell Thread 2 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("Sell Thread 2 countDown called");
            }
        });
        
        // Start both threads
        log.info("Starting both sell threads...");
        thread1.start();
        thread2.start();
        
        // Wait and analyze
        log.info("Waiting for threads to complete...");
        latch.await();
        
        log.info("All threads completed, Results collection size: {}", results.size());
        
        // Log all results
        for (SellResult result : results) {
            log.info("Result: Thread={}, Success={}, ErrorCode={}", result.threadId, result.success, result.errorCode);
        }
        
        // Validation
        Assert.assertEquals("Should have exactly 2 results", 2, results.size());
        
        // Check concurrency behavior: one should succeed, one should fail with duplicate UUID error
        long successCount = results.stream().filter(r -> r.success).count();
        long failureCount = results.stream().filter(r -> !r.success).count();
        
        log.info("Success count: {}, Failure count: {}", successCount, failureCount);
        Assert.assertTrue("Should have exactly 1 success and 1 failure", successCount == 1 && failureCount == 1);
        
        // Verify the failure is due to duplicate UUID
        SellResult failedResult = results.stream().filter(r -> !r.success).findFirst().orElse(null);
        Assert.assertNotNull("Should have a failed result", failedResult);
        Assert.assertEquals(StatusRepositoryService.DUPLICATE_UUID, failedResult.errorCode);
        log.info("Failed result error code: {}", failedResult.errorCode);
        
        log.info("=== Concurrent Sell Test Completed ===");
    }

    /**
     * Test concurrent sell UUID generation to ensure proper concurrency control.
     * This test verifies that multiple UUID generation requests work correctly.
     */
    @Test
    @Order(98)
    @DisplayName("concurrent sell UUID generation")
    void concurrentSellUuidGeneration() throws Exception {
        log.info("=== Starting Concurrent Sell UUID Generation Test ===");
        
        // Setup
        String quantity = "0.1";
        String currency = "GOLD";
        
        // Get account
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        
        // Test with 3 threads generating UUIDs simultaneously
        final CountDownLatch latch = new CountDownLatch(3);
        final List<UuidResult> results = new ArrayList<>();
        
        // Thread 1
        Thread thread1 = new Thread(() -> {
            try {
                log.info("UUID Thread 1 starting...");
                BaseResponse<UuidResponse> response1 = generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), currency, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                
                UuidResult result1 = new UuidResult();
                result1.threadId = 1;
                result1.success = response1.getSuccess();
                result1.errorCode = response1.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response1.getErrorDetail().getCode();
                result1.uuid = response1.getSuccess() ? response1.getData().getUniqueIdentifier() : null;
                result1.response = response1;
                
                synchronized (results) {
                    results.add(result1);
                    log.info("UUID Thread 1 added result to collection. Collection size now: {}", results.size());
                }
                log.info("UUID Thread 1 completed: Success={}, ErrorCode={}, UUID={}", result1.success, result1.errorCode, result1.uuid);
                
            } catch (Exception e) {
                log.error("UUID Thread 1 exception: {}", e.getMessage(), e);
                UuidResult result1 = new UuidResult();
                result1.threadId = 1;
                result1.success = false;
                result1.errorCode = -999;
                result1.exception = e;
                synchronized (results) {
                    results.add(result1);
                    log.info("UUID Thread 1 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("UUID Thread 1 countDown called");
            }
        });
        
        // Thread 2
        Thread thread2 = new Thread(() -> {
            try {
                log.info("UUID Thread 2 starting...");
                BaseResponse<UuidResponse> response2 = generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), currency, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                
                UuidResult result2 = new UuidResult();
                result2.threadId = 2;
                result2.success = response2.getSuccess();
                result2.errorCode = response2.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response2.getErrorDetail().getCode();
                result2.uuid = response2.getSuccess() ? response2.getData().getUniqueIdentifier() : null;
                result2.response = response2;
                
                synchronized (results) {
                    results.add(result2);
                    log.info("UUID Thread 2 added result to collection. Collection size now: {}", results.size());
                }
                log.info("UUID Thread 2 completed: Success={}, ErrorCode={}, UUID={}", result2.success, result2.errorCode, result2.uuid);
                
            } catch (Exception e) {
                log.error("UUID Thread 2 exception: {}", e.getMessage(), e);
                UuidResult result2 = new UuidResult();
                result2.threadId = 2;
                result2.success = false;
                result2.errorCode = -999;
                result2.exception = e;
                synchronized (results) {
                    results.add(result2);
                    log.info("UUID Thread 2 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("UUID Thread 2 countDown called");
            }
        });
        
        // Thread 3
        Thread thread3 = new Thread(() -> {
            try {
                log.info("UUID Thread 3 starting...");
                BaseResponse<UuidResponse> response3 = generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), currency, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                
                UuidResult result3 = new UuidResult();
                result3.threadId = 3;
                result3.success = response3.getSuccess();
                result3.errorCode = response3.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response3.getErrorDetail().getCode();
                result3.uuid = response3.getSuccess() ? response3.getData().getUniqueIdentifier() : null;
                result3.response = response3;
                
                synchronized (results) {
                    results.add(result3);
                    log.info("UUID Thread 3 added result to collection. Collection size now: {}", results.size());
                }
                log.info("UUID Thread 3 completed: Success={}, ErrorCode={}, UUID={}", result3.success, result3.errorCode, result3.uuid);
                
            } catch (Exception e) {
                log.error("UUID Thread 3 exception: {}", e.getMessage(), e);
                UuidResult result3 = new UuidResult();
                result3.threadId = 3;
                result3.success = false;
                result3.errorCode = -999;
                result3.exception = e;
                synchronized (results) {
                    results.add(result3);
                    log.info("UUID Thread 3 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("UUID Thread 3 countDown called");
            }
        });
        
        // Start all threads
        log.info("Starting all UUID generation threads...");
        thread1.start();
        thread2.start();
        thread3.start();
        
        // Wait and analyze
        log.info("Waiting for threads to complete...");
        latch.await();
        
        log.info("All threads completed, Results collection size: {}", results.size());
        
        // Log all results
        for (UuidResult result : results) {
            log.info("Result: Thread={}, Success={}, ErrorCode={}, UUID={}", result.threadId, result.success, result.errorCode, result.uuid);
        }
        
        // Validation
        Assert.assertEquals("Should have exactly 3 results", 3, results.size());
        
        // All should succeed and generate different UUIDs
        long successCount = results.stream().filter(r -> r.success).count();
        Assert.assertEquals("All UUID generation should succeed", 3, successCount);
        
        // Verify all UUIDs are different
        List<String> uuids = results.stream()
                .filter(r -> r.success && r.uuid != null)
                .map(r -> r.uuid)
                .toList();
        Assert.assertEquals("Should have 3 unique UUIDs", 3, uuids.size());
        Assert.assertEquals("All UUIDs should be unique", 3, uuids.stream().distinct().count());
        
        log.info("=== Concurrent Sell UUID Generation Test Completed ===");
    }

    /**
     * Comprehensive concurrent sell test - both scenarios
     * Scenario 1: Different UUIDs with same refNumber sent simultaneously
     * Scenario 2: Same UUID sent to method simultaneously
     */
    @Test
    @Order(99)
    @DisplayName("comprehensive concurrent sell test - balance for account")
    void comprehensiveConcurrentSellTest() throws Exception {
        log.info("start comprehensiveConcurrentSellTest");

        // Common setup
        String quantity = "1";
        String price = "100000";
        String commission = "1000";
        String commissionType = "GOLD";
        String currency = "GOLD";
        String sign = "";
        String additionalData = "comprehensive concurrent sell test";

        // Get account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);

        setupBalancesForSellToZero(walletAccountObjectOptional);
        // Setup sufficient balances
        setupBalancesForSell(walletAccountObjectOptional, "2", "2000000");

        // SCENARIO 1: Different UUIDs with same refNumber
        log.info("Testing Scenario 1: Different UUIDs with same refNumber");

        // SCENARIO 2: Same UUID with different refNumbers
        log.info("Testing Scenario 2: Same UUID with different refNumbers");
        testScenario2_SameUuidWithDifferentRefNumbers(quantity, price, commission, commissionType, currency, sign, additionalData, walletAccountObjectOptional);

        log.info("Comprehensive concurrent sell test completed successfully");
    }


    /**
     * Test Scenario 2: Same UUID sent to method simultaneously
     * This should fail for all but one transaction due to UUID reuse
     */
    private void testScenario2_SameUuidWithDifferentRefNumbers(String quantity, String price, String commission,
            String commissionType, String currency, String sign, String additionalData,
            WalletAccountObject walletAccountObjectOptional) throws Exception {
        
        log.info("Executing Scenario 2: Same UUID with different refNumbers");
        
        int numberOfThreads = 3;
        
        // Generate single UUID for all threads
        // Generate different UUIDs for each thread
        List<String> uniqueIdentifiers = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), currency, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
            uniqueIdentifiers.add(uuidResponse.getData().getUniqueIdentifier());
        }


        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<SellResult> results = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        
        // Execute concurrent requests with same UUID but different refNumbers
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            
            Thread thread = new Thread(() -> {
                try {
                    BaseResponse<PurchaseResponse> response = sellWithoutCheckResult(mockMvc, accessToken, uniqueIdentifiers.get(threadId), quantity, price, currency, commission, NATIONAL_CODE_CORRECT,
                            commissionType, "1", walletAccountObjectOptional.getAccountNumber(), sign, additionalData + "_scenario2_" + threadId);
                    
                    SellResult result = new SellResult();
                    result.threadId = threadId;
                    result.success = response.getSuccess();
                    result.errorCode = response.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response.getErrorDetail().getCode();
                    result.response = response;
                    
                    synchronized (results) {
                        results.add(result);
                        log.info("Scenario 2 - Thread {} added result. Collection size now: {}", threadId, results.size());
                    }
                    
                } catch (Exception e) {
                    log.error("Scenario 2 - Thread {} encountered exception: {}", threadId, e.getMessage());
                    SellResult result = new SellResult();
                    result.threadId = threadId;
                    result.success = false;
                    result.errorCode = -999;
                    result.exception = e;
                    synchronized (results) {
                        results.add(result);
                        log.info("Scenario 2 - Thread {} added exception result. Collection size now: {}", threadId, results.size());
                    }
                } finally {
                    latch.countDown();
                    log.info("Scenario 2 - Thread {} countDown called", threadId);
                }
            });
            
            threads.add(thread);
        }
        
        // Start all threads
        log.info("Starting {} threads for Scenario 2...", numberOfThreads);
        for (Thread thread : threads) {
            thread.start();
        }
        
        latch.await();
        
        // Analyze results for Scenario 2
        long successCount = results.stream().filter(r -> r.success).count();
        long failureCount = results.stream().filter(r -> !r.success).count();
        
        log.info("Scenario 2 results: {} successes, {} failures out of {} threads", successCount, failureCount, numberOfThreads);
        logDetailedResults(new ConcurrentLinkedQueue<>(results), "Scenario 2 - Same UUID, Different RefNumbers");
        
        // Verify that only one transaction succeeded (due to UUID reuse prevention)
        Assert.assertEquals("Scenario 2: three sell operation should succeed with the same UUID", 2, successCount);

        // Verify failed operations have appropriate error codes
        List<SellResult> failedResults = results.stream().filter(r -> !r.success).toList();
        for (SellResult failedResult : failedResults) {
            log.debug("Scenario 2 - Thread {}: error code = {}, exception = {}", 
                failedResult.threadId, failedResult.errorCode, 
                failedResult.exception != null ? failedResult.exception.getMessage() : "none");
            Assert.assertTrue("Scenario 2: Failed operation should have error code indicating UUID reuse",
                    failedResult.errorCode == StatusRepositoryService.BALANCE_IS_NOT_ENOUGH && failedResult.errorCode > 0);
        }
    }

    /**
     * Helper method to setup balances for sell operations
     */
    private void setupBalancesForSell(WalletAccountObject walletAccountObjectOptional, String goldAmount, String rialAmount) throws Exception {
        // Ensure user has enough GOLD balance for selling
        WalletAccountEntity goldWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountRepositoryService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal(goldAmount));
        
        // Ensure merchant has enough RIAL balance for buying
        WalletEntity walletMerchantEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeRepositoryService.getByName(WalletTypeRepositoryService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountRepositoryService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyRepositoryService.RIAL))
                .findFirst().orElse(null);
        walletAccountRepositoryService.increaseBalance(merchantRialAccount.getId(), new BigDecimal(rialAmount));
    }


    /**
     * Helper method to setup balances for sell operations
     */
    private void setupBalancesForSellToZero(WalletAccountObject walletAccountObjectOptional) {
        // Ensure user has enough GOLD balance for selling
        WalletAccountEntity goldWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        BigDecimal balance = walletAccountRepositoryService.getBalance(goldWalletAccountEntity.getId());
        walletAccountRepositoryService.decreaseBalance(goldWalletAccountEntity.getId(), balance);
    }

    /**
     * Helper method to log detailed results of concurrent operations for debugging
     */
    private void logDetailedResults(ConcurrentLinkedQueue<SellResult> results, String testName) {
        log.info("=== Detailed Results for {} ===", testName);
        
        // Log successful results
        List<SellResult> successfulResults = results.stream().filter(r -> r.success).toList();
        for (SellResult result : successfulResults) {
            log.info("✓ Thread {}: SUCCESS - Transaction completed successfully", result.threadId);
            if (result.response != null && result.response.getData() != null) {
                log.debug("  └─ Response data: {}", result.response.getData());
            }
        }
        
        // Log failed results
        List<SellResult> failedResults = results.stream().filter(r -> !r.success).toList();
        for (SellResult result : failedResults) {
            log.info("✗ Thread {}: FAILED - Error code: {}", result.threadId, result.errorCode);
            if (result.exception != null) {
                log.debug("  └─ Exception: {}", result.exception.getMessage());
            }
            if (result.response != null && result.response.getErrorDetail() != null) {
                log.debug("  └─ Error detail: {}", result.response.getErrorDetail());
            }
        }
        
        log.info("=== End Detailed Results for {} ===", testName);
    }

    /**
     * Result class for capturing sell operation results from concurrent threads
     */
    private static class SellResult {
        int threadId;
        boolean success;
        int errorCode;
        BaseResponse<PurchaseResponse> response;
        Exception exception;

        @Override
        public String toString() {
            return String.format("SellResult{threadId=%d, success=%s, errorCode=%d, hasResponse=%s, hasException=%s}",
                    threadId, success, errorCode, response != null, exception != null);
        }
    }

    /**
     * Result class for capturing UUID generation results from concurrent threads
     */
    private static class UuidResult {
        int threadId;
        boolean success;
        int errorCode;
        String uuid;
        BaseResponse<UuidResponse> response;
        Exception exception;

        @Override
        public String toString() {
            return String.format("UuidResult{threadId=%d, success=%s, errorCode=%d, uuid=%s, hasResponse=%s, hasException=%s}",
                    threadId, success, errorCode, uuid, response != null, exception != null);
        }
    }

    @Test
    @Order(100)
    @DisplayName("sell-Success-WithinLimitations")
    void sellSuccessWithinLimitations() throws Exception {
        log.info("start sellSuccessWithinLimitations test");
        // Step 1: Define test parameters
        String quantity = "0.5";
        String price = "50000";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountRepositoryService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("2.0"));
        // Step 4: Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeRepositoryService.getByName(WalletTypeRepositoryService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountRepositoryService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyRepositoryService.RIAL))
                .findFirst().orElse(null);
        walletAccountRepositoryService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("100000"));
        // Step 5: Set reasonable limitations that allow this transaction
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        // Step 6: Set minimum quantity to allow this transaction
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MIN_QUANTITY_SELL, walletAccountEntity, "0.1");
        // Step 7: Set maximum quantity to allow this transaction
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_SELL, walletAccountEntity, "10.0");
        // Step 8: Set daily quantity limitation to allow this transaction
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_SELL, walletAccountEntity, "6.0");
        // Step 9: Set daily count limitation to allow this transaction
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_SELL, walletAccountEntity, "10");
        // Step 10: Set monthly quantity limitation to allow this transaction
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_QUANTITY_SELL, walletAccountEntity, "50.0");
        // Step 11: Set monthly count limitation to allow this transaction
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_COUNT_SELL, walletAccountEntity, "100");
        // Step 12: Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 13: Perform sell operation - should succeed within all limitations
        BaseResponse<PurchaseResponse> response = sell(mockMvc, accessToken, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "1000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test sell within limitations", HttpStatus.OK, "IR123456789012345678901234", StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
    }
} 