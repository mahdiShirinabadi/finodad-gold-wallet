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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
    private static String ACCESS_TOKEN;

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
    private LimitationGeneralService limitationGeneralService;
    @Autowired
    private Flyway flyway;
    @Autowired
    private CacheClearService cacheClearService;
    @Autowired
    private WalletLevelService walletLevelService;

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
        BaseResponse<LoginResponse> loginResponse = login(mockMvc, USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        ACCESS_TOKEN = loginResponse.getData().getAccessTokenObject().getToken();

        // Step 2: Create wallet if needed
        try {
            createWallet(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
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
        walletEntity.setOwner(channelService.getChannel(USERNAME_CORRECT));
        walletEntity.setWalletTypeEntity(walletTypeService.getByName(WalletTypeService.CHANNEL));
        walletEntity.setWalletLevelEntity(walletLevelService.getAll().stream().filter(x -> x.getName().equalsIgnoreCase(WalletLevelService.BRONZE)).findFirst().get());
        walletEntity.setCreatedBy("admin");
        walletEntity.setCreatedAt(new Date());
        walletService.save(walletEntity);

        ChannelEntity channelEntity = channelService.getChannel(USERNAME_CORRECT);
        channelEntity.setWalletEntity(walletEntity);
        channelService.save(channelEntity);

        // Create wallet accounts for RIAL and GOLD currencies
        walletAccountService.createAccount(List.of(WalletAccountCurrencyService.RIAL, WalletAccountCurrencyService.GOLD),
                walletEntity, List.of(WalletAccountTypeService.WAGE), channelEntity);
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Step 2: Generate UUID for sell operation
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_NOT_FOUND, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.NATIONAL_CODE_NOT_FOUND, false);
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
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, "invalid_account", CURRENCY_GOLD, HttpStatus.OK, StatusService.WALLET_ACCOUNT_NOT_FOUND, false);
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), "INVALID_CURRENCY", HttpStatus.OK, StatusService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.QUANTITY_LESS_THAN_MIN, false);
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.QUANTITY_BIGGER_THAN_MAX, false);
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
        WalletAccountObject goldAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);

        // Step 3: Ensure user has enough GOLD balance for selling
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(goldAccountObject.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("2.0"));

        // Step 4: Ensure merchant has enough RIAL balance for buying
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("200000"));

        // Step 5: Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, goldAccountObject.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();

        // Step 6: Perform sell operation
        BaseResponse<PurchaseResponse> response = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "0.01", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", goldAccountObject.getAccountNumber(), "", "test sell success", HttpStatus.OK, "IR123456789012345678901234", StatusService.SUCCESSFUL, true);
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);

        BaseResponse<PurchaseResponse> response = sell(mockMvc, ACCESS_TOKEN, "invalid-uuid", quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test invalid uuid", HttpStatus.OK, "IR123456789012345678901234", StatusService.UUID_NOT_FOUND, false);
        Assert.assertSame(StatusService.UUID_NOT_FOUND, response.getErrorDetail().getCode());
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);

        // Ensure user has insufficient GOLD balance
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.decreaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("10.0")); // Remove all balance

        // Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();

        // Test with insufficient balance
        BaseResponse<PurchaseResponse> response = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test insufficient balance", HttpStatus.OK, "IR123456789012345678901234", StatusService.BALANCE_IS_NOT_ENOUGH, false);
        Assert.assertSame(StatusService.BALANCE_IS_NOT_ENOUGH, response.getErrorDetail().getCode());
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        // Step 3: Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 4: Test with invalid merchant ID
        BaseResponse<PurchaseResponse> response = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "999", walletAccountObjectOptional.getAccountNumber(), "", "test invalid merchant id", HttpStatus.OK, "IR123456789012345678901234", StatusService.MERCHANT_IS_NOT_EXIST, false);
        Assert.assertSame(StatusService.MERCHANT_IS_NOT_EXIST, response.getErrorDetail().getCode());
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        // Step 3: Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 4: Test with invalid commission currency (should be same as main currency)
        BaseResponse<PurchaseResponse> response = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "0.05", NATIONAL_CODE_CORRECT, "GOLDD", "1", walletAccountObjectOptional.getAccountNumber(), "", "test invalid commission currency", HttpStatus.OK, "IR123456789012345678901234", StatusService.COMMISSION_CURRENCY_NOT_VALID, false);
        Assert.assertSame(StatusService.COMMISSION_CURRENCY_NOT_VALID, response.getErrorDetail().getCode());
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        // Step 3: Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("5.0"));
        // Step 4: Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("200000"));
        // Step 5: Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 6: Perform first sell operation (should succeed)
        BaseResponse<PurchaseResponse> response1 = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test sell success", HttpStatus.OK, "IR123456789012345678901234", StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response1.getData());
        // Step 7: Try to perform the same sell operation again (should fail with duplicate)
        BaseResponse<PurchaseResponse> response2 = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test duplicate request", HttpStatus.OK, "IR123456789012345678901234", StatusService.DUPLICATE_UUID, false);
        Assert.assertSame(StatusService.DUPLICATE_UUID, response2.getErrorDetail().getCode());
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
        WalletAccountObject goldAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        // Step 3: Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(goldAccountObject.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("2.0"));
        // Step 4: Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("200000"));
        // Step 5: Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, goldAccountObject.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 6: Perform sell operation
        BaseResponse<PurchaseResponse> sellResponse = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", goldAccountObject.getAccountNumber(), "", "test sell for inquiry", HttpStatus.OK, "IR123456789012345678901234", StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(sellResponse.getData());
        // Step 7: Inquiry the sell operation
        BaseResponse<PurchaseTrackResponse> inquiryResponse = inquiryPurchase(mockMvc, ACCESS_TOKEN, uniqueIdentifier, "SELL", HttpStatus.OK, StatusService.SUCCESSFUL, true);
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
        BaseResponse<PurchaseTrackResponse> response = inquiryPurchase(mockMvc, ACCESS_TOKEN, "invalid-uuid", "SELL", HttpStatus.OK, StatusService.UUID_NOT_FOUND, false);
        Assert.assertSame(StatusService.UUID_NOT_FOUND, response.getErrorDetail().getCode());
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
        String price = "100";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        // Step 3: Generate sell UUID - should fail due to minimum quantity limitation
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.QUANTITY_LESS_THAN_MIN, false);
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
        String price = "100000000";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        // Step 3: Generate sell UUID - should fail due to maximum quantity limitation
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.QUANTITY_BIGGER_THAN_MAX, false);
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
        String price = "100000";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        // Step 3: Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("10.0"));
        // Step 4: Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("1000000"));
        // Step 5: Get current daily quantity limitation
        String maxDailyQuantity = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_SELL, walletAccountObjectOptional.getAccountNumber());

        // Step 6: Set a very low daily quantity limitation to trigger the error
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_SELL, goldWalletAccountEntity, "0.5");
        // Step 7: Generate sell UUID - should fail due to daily quantity limitation
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SELL_EXCEEDED_AMOUNT_DAILY_LIMITATION, false);
        Assert.assertSame(StatusService.SELL_EXCEEDED_AMOUNT_DAILY_LIMITATION, uuidResponse.getErrorDetail().getCode());
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
        String price = "10000";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        // Step 3: Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("10.0"));
        // Step 4: Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("1000000"));
        // Step 5: Get current daily count limitation
        String maxDailyCount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_SELL, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        // Step 6: Set a very low daily count limitation to trigger the error
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_SELL, walletAccountEntity, "1");
        // Step 7: Perform first sell operation (should succeed)
        BaseResponse<UuidResponse> uuidResponse2 = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SELL_EXCEEDED_COUNT_DAILY_LIMITATION, false);
        Assert.assertSame(StatusService.SELL_EXCEEDED_COUNT_DAILY_LIMITATION, uuidResponse2.getErrorDetail().getCode());
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
        String price = "100000";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        // Step 3: Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("10.0"));
        // Step 4: Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("1000000"));
        // Step 5: Get current monthly quantity limitation
        String maxMonthlyQuantity = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_QUANTITY_SELL, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        // Step 6: Set a very low monthly quantity limitation to trigger the error
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_QUANTITY_SELL, walletAccountEntity, "0.5");
        // Step 7: Generate sell UUID - should fail due to monthly quantity limitation
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SELL_EXCEEDED_AMOUNT_MONTHLY_LIMITATION, false);
        // Step 8: Restore original monthly quantity limitation
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_QUANTITY_SELL, walletAccountEntity, maxMonthlyQuantity);
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
        String price = "10000";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        // Step 3: Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("10.0"));
        // Step 4: Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("1000000"));
        // Step 5: Get current monthly count limitation
        String maxMonthlyCountValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_COUNT_SELL, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        // Step 6: Set a very low monthly count limitation to trigger the error
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_COUNT_SELL, walletAccountEntity, "1");
        // Step 7: Perform first sell operation (should succeed)
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SELL_EXCEEDED_COUNT_MONTHLY_LIMITATION, false);
        // Step 8: Restore original monthly count limitation
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_COUNT_SELL, walletAccountEntity, maxMonthlyCountValue);
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
    @Test
    @Order(96)
    @DisplayName("sell-Success-WithinLimitations")
    void sellSuccessWithinLimitations() throws Exception {
        log.info("start sellSuccessWithinLimitations test");
        // Step 1: Define test parameters
        String quantity = "0.5";
        String price = "50000";
        // Step 2: Get user's GOLD account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        // Step 3: Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("2.0"));
        // Step 4: Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("100000"));
        // Step 5: Set reasonable limitations that allow this transaction
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        // Step 6: Set minimum quantity to allow this transaction
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MIN_QUANTITY_SELL, walletAccountEntity, "0.1");
        // Step 7: Set maximum quantity to allow this transaction
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_SELL, walletAccountEntity, "10.0");
        // Step 8: Set daily quantity limitation to allow this transaction
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_SELL, walletAccountEntity, "5.0");
        // Step 9: Set daily count limitation to allow this transaction
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_SELL, walletAccountEntity, "10");
        // Step 10: Set monthly quantity limitation to allow this transaction
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_QUANTITY_SELL, walletAccountEntity, "50.0");
        // Step 11: Set monthly count limitation to allow this transaction
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_COUNT_SELL, walletAccountEntity, "100");
        // Step 12: Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 13: Perform sell operation - should succeed within all limitations
        BaseResponse<PurchaseResponse> response = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "1000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test sell within limitations", HttpStatus.OK, "IR123456789012345678901234", StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
    }
} 