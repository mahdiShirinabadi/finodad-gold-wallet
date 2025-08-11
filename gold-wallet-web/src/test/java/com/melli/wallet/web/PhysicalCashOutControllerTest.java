package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.cash.*;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.service.*;
import lombok.extern.log4j.Log4j2;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;


import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: PhysicalCashControllerTest
 * Author: Mahdi Shirinabadi
 * Date: 1/20/2025
 * This test class contains comprehensive end-to-end tests for Physical Cash Out operations.
 * It tests the complete flow from UUID generation to physical cash withdrawal and inquiry.
 * Test Coverage:
 * - Physical cash out UUID generation (success and failure scenarios)
 * - Physical cash out execution (success and failure scenarios)
 * - Physical cash out inquiry (success and failure scenarios)
 * - Balance validation
 * - Permission validation
 * - Commission currency validation
 * - Limitation handling
 */
@Log4j2
@DisplayName("PhysicalCashControllerTest End2End test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PhysicalCashOutControllerTest extends WalletApplicationTests {

    private static final String NATIONAL_CODE_CORRECT = "0077847660";
    private static final String MOBILE_CORRECT = "09124162337";
    private static final String VALID_SIGN = "valid_sign_for_testing";
    private static final String ADDITIONAL_DATA = "Test physical cashout operation";
    private static final String CURRENCY_GOLD = "GOLD";

    private static MockMvc mockMvc;
    private static String accessToken;

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
    private LimitationGeneralService limitationGeneralService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private CacheClearService cacheClearService;
    @Autowired
    private Flyway flyway;
    @Autowired
    private WalletLevelService walletLevelService;


    /**
     * Initial setup method that runs before all tests.
     * This method:
     * - Sets up MockMvc for testing
     * - Cleans and migrates the database
     * - Clears all caches
     * - Creates a channel wallet for testing
     * - Sets up necessary wallet accounts
     */
    @Test
    @Order(1)
    @DisplayName("Initiate...")
    void initial() throws Exception{
        // Setup MockMvc for testing with security
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);
        
        // Clean and migrate database
        log.info("start cleaning initial values in test DB");
        flyway.clean();
        flyway.migrate();
        cacheClearService.clearCache();

        // Re-setup MockMvc after database changes
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);
        log.info("start cleaning initial values in test DB for purchase");
        flyway.clean();
        flyway.migrate();
        log.info("start cleaning initial values in test DB for purchase");
        cacheClearService.clearCache();

        // Create wallet for channel testing
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

        // Associate wallet with channel
        ChannelEntity channelEntity = channelService.getChannel(USERNAME_CORRECT);
        channelEntity.setWalletEntity(walletEntity);
        channelService.save(channelEntity);

        // Create wallet accounts for RIAL and GOLD currencies
        walletAccountService.createAccount(List.of(WalletAccountCurrencyService.RIAL, WalletAccountCurrencyService.GOLD),
                walletEntity, List.of(WalletAccountTypeService.WAGE), channelEntity);
    }

    /**
     * Test successful channel login.
     * This method:
     * - Performs login with correct credentials
     * - Validates the response contains valid data
     * - Stores the access token for subsequent tests
     */
    @Test
    @Order(10)
    @DisplayName("channel login successfully")
    void login_success() throws Exception {
        BaseResponse<LoginResponse> response = login(mockMvc, USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.OK,
                StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        accessToken = response.getData().getAccessTokenObject().getToken();
        log.info("login successful with token: {}", accessToken);
    }

    /**
     * Test successful wallet creation.
     * This method:
     * - Creates a wallet for the test user
     * - Validates the wallet creation response
     * - Logs the successful creation
     */
    @Test
    @Order(20)
    @DisplayName("create wallet- success")
    void createWalletSuccess() throws Exception {
        BaseResponse<CreateWalletResponse> response = createWallet(mockMvc, accessToken, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        log.info("wallet created successfully for nationalCode: {}", NATIONAL_CODE_CORRECT);
    }

    /**
     * Test successful physical cash out UUID generation.
     * This method:
     * - Gets the user's GOLD account number
     * - Enables physical cash out permission if disabled
     * - Generates a UUID for physical cash out operation
     * - Validates the UUID generation response
     */
    @Test
    @Order(30)
    @DisplayName("physicalCashOutGenerateUuid-success")
    void physicalCashOutGenerateUuidSuccess() throws Exception {
        log.info("start physicalCashOutGenerateUuidSuccess test");
        
        // Get user's GOLD account number for testing
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // Enable physical cash out permission if currently disabled
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        String physicalCashOutValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, accountNumber);
        if("false".equalsIgnoreCase(physicalCashOutValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, walletAccountEntity, "true");
        }
        
        // Generate UUID for physical cash out operation
        String quantity = "5";
        BaseResponse<UuidResponse> response = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getUniqueIdentifier());
        log.info("Physical cash out UUID generated successfully: {}", response.getData().getUniqueIdentifier());
    }

    /**
     * Test physical cash out UUID generation failure with invalid account number.
     * This method:
     * - Attempts to generate UUID with a non-existent account number
     * - Expects WALLET_ACCOUNT_NOT_FOUND error
     */
    @Test
    @Order(31)
    @DisplayName("physicalCashOutGenerateUuid-fail-invalidAccountNumber")
    void physicalCashOutGenerateUuidFailInvalidAccountNumber() throws Exception {
        log.info("start physicalCashOutGenerateUuidFailInvalidAccountNumber test");
        String quantity = "1.07";
        generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, "00000023432", HttpStatus.OK, StatusService.WALLET_ACCOUNT_NOT_FOUND, false);
    }

    /**
     * Test physical cash out UUID generation failure when account lacks permission.
     * This method:
     * - Gets a valid account number
     * - Disables physical cash out permission for the account
     * - Attempts to generate UUID and expects permission error
     */
    @Test
    @Order(32)
    @DisplayName("physicalCashOutGenerateUuid-fail-accountNotPermission")
    void physicalCashOutGenerateUuidFailAccountNotPermission() throws Exception {
        log.info("start physicalCashOutGenerateUuidFailAccountNotPermission test");
        
        // Get user's GOLD account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // Disable physical cash out permission to test failure scenario
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, walletAccountEntity, "false");
        
        // Attempt to generate UUID with disabled permission
        String quantity = "1.07";
        generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, accountNumber, HttpStatus.OK, StatusService.ACCOUNT_DONT_PERMISSION_FOR_PHYSICAL_CASH_OUT, false);
    }

    /**
     * Test successful physical cash out operation.
     * This method:
     * - Gets user's GOLD account number
     * - Increases account balance for testing
     * - Enables physical cash out permission
     * - Temporarily increases MAX_QUANTITY_PHYSICAL_CASH_OUT limit
     * - Generates UUID for physical cash out
     * - Performs physical cash out operation
     * - Validates the response
     * - Restores original MAX_QUANTITY_PHYSICAL_CASH_OUT limit
     */
    @Test
    @Order(40)
    @DisplayName("physicalCashOut-success")
    void physicalCashOutSuccess() throws Exception {
        log.info("start physicalCashOutSuccess test");
        
        // Step 1: Get user's GOLD account number
        WalletAccountObject goldAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        String goldAccountNumber = goldAccountObject.getAccountNumber();

        // Step 2: Increase account balance for testing
        walletAccountService.increaseBalance(walletAccountService.findByAccountNumber(goldAccountNumber).getId(), new BigDecimal("5.05"));

        // Step 3: Enable physical cash out permission if disabled
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(goldAccountNumber);
        String physicalCashOutValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, goldAccountNumber);
        if("false".equalsIgnoreCase(physicalCashOutValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, goldWalletAccountEntity, "true");
        }

        // Step 4: Store original MAX_QUANTITY_PHYSICAL_CASH_OUT value for restoration
        String valueMaxDailyPrice = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT, goldAccountObject.getAccountNumber());
        
        // Step 5: Temporarily increase MAX_QUANTITY_PHYSICAL_CASH_OUT limit to allow operation
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT, goldWalletAccountEntity, "10");

        // Step 6: Generate UUID for physical cash out operation
        String physicalCashOutQuantity = "5.05";
        BaseResponse<UuidResponse> physicalCashOutUuidResponse = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, physicalCashOutQuantity, goldAccountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = physicalCashOutUuidResponse.getData().getUniqueIdentifier();
        
        // Step 7: Perform physical cash out operation
        BaseResponse<PhysicalCashOutResponse> response = physicalCashOut(mockMvc, accessToken, uniqueIdentifier, physicalCashOutQuantity, NATIONAL_CODE_CORRECT, goldAccountNumber, "", VALID_SIGN, ADDITIONAL_DATA, CURRENCY_GOLD, "0.05",CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertEquals(NATIONAL_CODE_CORRECT, response.getData().getNationalCode());
        Assert.assertEquals(goldAccountNumber, response.getData().getWalletAccountNumber());
        Assert.assertNotNull(response.getData().getBalance());
        Assert.assertEquals(uniqueIdentifier, response.getData().getUniqueIdentifier());
        log.info("Physical cash out completed successfully");

        // Step 8: Restore original MAX_QUANTITY_PHYSICAL_CASH_OUT limit
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT, goldWalletAccountEntity, valueMaxDailyPrice);
    }

    /**
     * Test physical cash out failure with invalid UUID.
     * This method:
     * - Attempts physical cash out with a non-existent UUID
     * - Expects UUID_NOT_FOUND error
     */
    @Test
    @Order(41)
    @DisplayName("physicalCashOut-fail-invalidUniqueIdentifier")
    void physicalCashOutFailInvalidUniqueIdentifier() throws Exception {
        log.info("start physicalCashOutFailInvalidUniqueIdentifier test");
        String quantity = "1.07";
        physicalCashOut(mockMvc, accessToken, "invalid_uuid", quantity, NATIONAL_CODE_CORRECT, "1234567890", "", VALID_SIGN, ADDITIONAL_DATA, CURRENCY_GOLD, "100",CURRENCY_GOLD, HttpStatus.OK, StatusService.UUID_NOT_FOUND, false);
    }

    /**
     * Test physical cash out failure with invalid commission currency.
     * This method:
     * - Gets user's GOLD account number
     * - Enables physical cash out permission
     * - Generates valid UUID for physical cash out
     * - Attempts physical cash out with different commission currency (RIAL instead of GOLD)
     * - Expects COMMISSION_CURRENCY_NOT_VALID error
     */
    @Test
    @Order(42)
    @DisplayName("physicalCashOut-fail-invalidCommissionCurrency")
    void physicalCashOutFailInvalidCommissionCurrency() throws Exception {
                log.info("start physicalCashOutFailInvalidCommissionCurrency test");
        
        // Get user's GOLD account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // Enable physical cash out permission if disabled
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        String physicalCashOutValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, accountNumber);
        if("false".equalsIgnoreCase(physicalCashOutValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, walletAccountEntity, "true");
        }
        
        // Generate UUID for physical cash out operation
        String quantity = "5";
        BaseResponse<UuidResponse> uuidResponse = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Test with different commission currency (RIAL instead of GOLD) - should fail
        BaseResponse<PhysicalCashOutResponse> response = physicalCashOut(mockMvc, accessToken, uniqueIdentifier, quantity, NATIONAL_CODE_CORRECT, accountNumber, "", VALID_SIGN, ADDITIONAL_DATA, CURRENCY_GOLD, "0.01","RIAL", HttpStatus.OK, StatusService.COMMISSION_CURRENCY_NOT_VALID, false);
        Assert.assertSame(StatusService.COMMISSION_CURRENCY_NOT_VALID, response.getErrorDetail().getCode());
    }

    /*
    /**
     * Test physical cash out failure with invalid sign.
     * This method is currently commented out but would:
     * - Get user's GOLD account number
     * - Enable physical cash out permission
     * - Generate valid UUID for physical cash out
     * - Attempt physical cash out with invalid sign
     * - Expect INVALID_SIGN error
     */
    /*@Test
    @Order(43)
    @DisplayName("physicalCashOut-fail-invalidSign")
    void physicalCashOutFailInvalidSign() throws Exception {
        log.info("start physicalCashOutFailInvalidSign test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // Enable physical cash out
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        String physicalCashOutValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, accountNumber);
        if("false".equalsIgnoreCase(physicalCashOutValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, walletAccountEntity, "true"); //"test physicalCashOutFailInvalidSign");
        }
        
        String quantity = "1.07";
        BaseResponse<UuidResponse> uuidResponse = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Test with invalid sign
        BaseResponse<PhysicalCashOutResponse> response = physicalCashOut(mockMvc, accessToken, uniqueIdentifier, quantity, NATIONAL_CODE_CORRECT, accountNumber, "", "invalid_sign", ADDITIONAL_DATA, "GOLD","GOLD", "100", HttpStatus.OK, StatusService.INVALID_SIGN, false);
        Assert.assertSame(StatusService.INVALID_SIGN, response.getErrorDetail().getCode());
    }*/


    /**
     * Test physical cash out failure when balance is insufficient.
     * This method:
     * - Gets user's GOLD account number
     * - Enables physical cash out permission
     * - Temporarily increases MAX_QUANTITY_PHYSICAL_CASH_OUT limit
     * - Generates UUID for physical cash out
     * - Decreases account balance to create insufficient balance scenario
     * - Attempts physical cash out and expects BALANCE_IS_NOT_ENOUGH error
     * - Restores original MAX_QUANTITY_PHYSICAL_CASH_OUT limit
     */
    @Test
    @Order(44)
    @DisplayName("physicalCashOut-fail-balance not enough")
    void physicalCashOutFailBalanceNoEnough() throws Exception {
        log.info("start physicalCashOutFailBalanceNoEnough test");
        
        // Get user's GOLD account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        String accountNumber = walletAccountObject.getAccountNumber();

        // Enable physical cash out permission if disabled
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        String physicalCashOutValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, accountNumber);
        if("false".equalsIgnoreCase(physicalCashOutValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, walletAccountEntity, "true");
        }

        // Store original MAX_QUANTITY_PHYSICAL_CASH_OUT value for restoration
        String quantity = "20";
        String valueMaxDailyPrice = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT, walletAccountObject.getAccountNumber());

        // Temporarily increase MAX_QUANTITY_PHYSICAL_CASH_OUT limit to allow UUID generation
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT, walletAccountEntity, quantity);
        
        // Generate UUID for physical cash out operation
        BaseResponse<UuidResponse> uuidResponse = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();

        // Decrease account balance to create insufficient balance scenario
        walletAccountService.decreaseBalance(walletAccountEntity.getId(), new BigDecimal(quantity));

        // Attempt physical cash out with insufficient balance - should fail
        BaseResponse<PhysicalCashOutResponse> response = physicalCashOut(mockMvc, accessToken, uniqueIdentifier, quantity, NATIONAL_CODE_CORRECT, accountNumber, "", VALID_SIGN, ADDITIONAL_DATA, CURRENCY_GOLD, "0.01","GOLD", HttpStatus.OK, StatusService.BALANCE_IS_NOT_ENOUGH, false);
        Assert.assertSame(StatusService.BALANCE_IS_NOT_ENOUGH, response.getErrorDetail().getCode());
        
        // Restore original MAX_QUANTITY_PHYSICAL_CASH_OUT limit
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT, walletAccountEntity, valueMaxDailyPrice);
    }

    /**
     * Test successful physical cash out inquiry.
     * This method:
     * - Gets user's GOLD account number
     * - Increases account balance for testing
     * - Enables physical cash out permission
     * - Temporarily increases MAX_QUANTITY_PHYSICAL_CASH_OUT limit
     * - Performs a complete physical cash out operation
     * - Performs inquiry on the completed operation
     * - Validates the inquiry response
     */
    @Test
    @Order(50)
    @DisplayName("physicalInquiryCashOut-success")
    void physicalInquiryCashOutSuccess() throws Exception {
        log.info("start physicalInquiryCashOutSuccess test");
        
        // Step 1: Get user's GOLD account number
        WalletAccountObject goldAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        String goldAccountNumber = goldAccountObject.getAccountNumber();
        
        // Step 2: Increase account balance for testing
        walletAccountService.increaseBalance(walletAccountService.findByAccountNumber(goldAccountNumber).getId(), new BigDecimal("10.01"));
        
        // Step 3: Enable physical cash out permission if disabled
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(goldAccountNumber);
        String physicalCashOutValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, goldAccountNumber);
        if("false".equalsIgnoreCase(physicalCashOutValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, goldWalletAccountEntity, "true");
        }

        // Step 4: Temporarily increase MAX_QUANTITY_PHYSICAL_CASH_OUT limit
        String physicalCashOutQuantity = "10.01";
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT, goldWalletAccountEntity, physicalCashOutQuantity);
        
        // Step 5: Generate UUID and perform physical cash out operation
        BaseResponse<UuidResponse> physicalCashOutUuidResponse = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, physicalCashOutQuantity, goldAccountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = physicalCashOutUuidResponse.getData().getUniqueIdentifier();
        BaseResponse<PhysicalCashOutResponse> physicalCashOutResponse = physicalCashOut(mockMvc, accessToken, uniqueIdentifier, physicalCashOutQuantity, NATIONAL_CODE_CORRECT, goldAccountNumber, "", VALID_SIGN, ADDITIONAL_DATA, CURRENCY_GOLD,"0.01", CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(physicalCashOutResponse.getData());
        
        // Step 6: Perform inquiry on the completed physical cash out operation
        BaseResponse<PhysicalCashOutTrackResponse> response = physicalInquiryCashOut(mockMvc, accessToken, uniqueIdentifier, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        log.info("Physical cash out inquiry completed successfully");
    }

    /**
     * Test physical cash out inquiry failure with invalid UUID.
     * This method:
     * - Attempts inquiry with a non-existent UUID
     * - Expects UUID_NOT_FOUND error
     * Test concurrent physical cash out operations with same UUID to ensure proper concurrency control.
     * This test verifies that when the same UUID is used simultaneously, only one operation succeeds.
     */
    @Test
    @Order(52)
    @DisplayName("concurrent physical cash out operations with same UUID")
    void concurrentPhysicalCashOutWithSameUuid() throws Exception {
        log.info("=== Starting Concurrent Physical Cash Out Test with Same UUID ===");
        
        // Setup
        String quantity = "5";
        String commission = "0.1";
        String commissionType = "GOLD";
        String currency = "GOLD";
        String sign = VALID_SIGN;
        String additionalData = "concurrent physical cash out test";
        
        // Get account and setup balance
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Ensure user has enough GOLD balance for physical cash out
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("5"));
        
        // Generate UUID
        BaseResponse<UuidResponse> uuidResponse = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String sharedUniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        log.info("Generated UUID: {}", sharedUniqueIdentifier);
        
        // Test with 2 threads using the same UUID
        final CountDownLatch latch = new CountDownLatch(2);
        final List<PhysicalCashOutResult> results = new ArrayList<>();
        
        // Thread 1
        Thread thread1 = new Thread(() -> {
            try {
                log.info("Physical Cash Out Thread 1 starting...");
                BaseResponse<PhysicalCashOutResponse> response1 = physicalCashOutWithoutCheckResult(mockMvc, accessToken, sharedUniqueIdentifier, quantity,NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), additionalData + "_1",sign,"", currency, commission,commissionType);
                
                PhysicalCashOutResult result1 = new PhysicalCashOutResult();
                result1.threadId = 1;
                result1.success = response1.getSuccess();
                result1.errorCode = response1.getSuccess() ? StatusService.SUCCESSFUL : response1.getErrorDetail().getCode();
                result1.response = response1;
                
                synchronized (results) {
                    results.add(result1);
                    log.info("Physical Cash Out Thread 1 added result to collection. Collection size now: {}", results.size());
                }
                log.info("Physical Cash Out Thread 1 completed: Success={}, ErrorCode={}", result1.success, result1.errorCode);
                
            } catch (Exception e) {
                log.error("Physical Cash Out Thread 1 exception: {}", e.getMessage(), e);
                PhysicalCashOutResult result1 = new PhysicalCashOutResult();
                result1.threadId = 1;
                result1.success = false;
                result1.errorCode = -999;
                result1.exception = e;
                synchronized (results) {
                    results.add(result1);
                    log.info("Physical Cash Out Thread 1 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("Physical Cash Out Thread 1 countDown called");
            }
        });
        
        // Thread 2
        Thread thread2 = new Thread(() -> {
            try {
                log.info("Physical Cash Out Thread 2 starting...");
                BaseResponse<PhysicalCashOutResponse> response2 = physicalCashOutWithoutCheckResult(mockMvc, accessToken, sharedUniqueIdentifier, quantity,NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), additionalData + "_2",sign,"", currency, commission,commissionType);
                
                PhysicalCashOutResult result2 = new PhysicalCashOutResult();
                result2.threadId = 2;
                result2.success = response2.getSuccess();
                result2.errorCode = response2.getSuccess() ? StatusService.SUCCESSFUL : response2.getErrorDetail().getCode();
                result2.response = response2;
                
                synchronized (results) {
                    results.add(result2);
                    log.info("Physical Cash Out Thread 2 added result to collection. Collection size now: {}", results.size());
                }
                log.info("Physical Cash Out Thread 2 completed: Success={}, ErrorCode={}", result2.success, result2.errorCode);
                
            } catch (Exception e) {
                log.error("Physical Cash Out Thread 2 exception: {}", e.getMessage(), e);
                PhysicalCashOutResult result2 = new PhysicalCashOutResult();
                result2.threadId = 2;
                result2.success = false;
                result2.errorCode = -999;
                result2.exception = e;
                synchronized (results) {
                    results.add(result2);
                    log.info("Physical Cash Out Thread 2 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("Physical Cash Out Thread 2 countDown called");
            }
        });
        
        // Start both threads
        log.info("Starting both physical cash out threads...");
        thread1.start();
        thread2.start();
        
        // Wait and analyze
        log.info("Waiting for threads to complete...");
        latch.await();
        
        log.info("All threads completed, Results collection size: {}", results.size());
        
        // Log all results
        for (PhysicalCashOutResult result : results) {
            log.info("Result: Thread={}, Success={}, ErrorCode={}", result.threadId, result.success, result.errorCode);
        }
        
        // Validation
        Assert.assertTrue("Should have exactly 2 results", results.size() == 2);
        
        // Check concurrency behavior: one should succeed, one should fail with duplicate UUID error
        long successCount = results.stream().filter(r -> r.success).count();
        long failureCount = results.stream().filter(r -> !r.success).count();
        
        log.info("Success count: {}, Failure count: {}", successCount, failureCount);
        Assert.assertTrue("Should have exactly 1 success and 1 failure", successCount == 1 && failureCount == 1);
        
        // Verify the failure is due to duplicate UUID
        PhysicalCashOutResult failedResult = results.stream().filter(r -> !r.success).findFirst().orElse(null);
        Assert.assertNotNull("Should have a failed result", failedResult);
        Assert.assertEquals(StatusService.DUPLICATE_UUID, failedResult.errorCode);
        log.info("Failed result error code: {}", failedResult.errorCode);
        
        log.info("=== Concurrent Physical Cash Out Test Completed ===");
    }

    /**
     * Test concurrent physical cash out UUID generation to ensure proper concurrency control.
     * This test verifies that multiple UUID generation requests work correctly.
     */
    @Test
    @Order(53)
    @DisplayName("concurrent physical cash out on one account generation")
    void concurrentPhysicalCashOutUuidGeneration() throws Exception {
        log.info("=== Starting Concurrent Physical Cash Out UUID Generation Test ===");
        
        // Setup
        String quantity = "5";
        String currency = "GOLD";
        
        // Get account
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Test with 3 threads generating UUIDs simultaneously
        final CountDownLatch latch = new CountDownLatch(3);
        final List<PhysicalCashOutUuidResult> results = new ArrayList<>();
        
        // Thread 1
        Thread thread1 = new Thread(() -> {
            try {
                log.info("Physical Cash Out UUID Thread 1 starting...");
                BaseResponse<UuidResponse> response1 = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
                
                PhysicalCashOutUuidResult result1 = new PhysicalCashOutUuidResult();
                result1.threadId = 1;
                result1.success = response1.getSuccess();
                result1.errorCode = response1.getSuccess() ? StatusService.SUCCESSFUL : response1.getErrorDetail().getCode();
                result1.uuid = response1.getSuccess() ? response1.getData().getUniqueIdentifier() : null;
                result1.response = response1;
                
                synchronized (results) {
                    results.add(result1);
                    log.info("Physical Cash Out UUID Thread 1 added result to collection. Collection size now: {}", results.size());
                }
                log.info("Physical Cash Out UUID Thread 1 completed: Success={}, ErrorCode={}, UUID={}", result1.success, result1.errorCode, result1.uuid);
                
            } catch (Exception e) {
                log.error("Physical Cash Out UUID Thread 1 exception: {}", e.getMessage(), e);
                PhysicalCashOutUuidResult result1 = new PhysicalCashOutUuidResult();
                result1.threadId = 1;
                result1.success = false;
                result1.errorCode = -999;
                result1.exception = e;
                synchronized (results) {
                    results.add(result1);
                    log.info("Physical Cash Out UUID Thread 1 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("Physical Cash Out UUID Thread 1 countDown called");
            }
        });
        
        // Thread 2
        Thread thread2 = new Thread(() -> {
            try {
                log.info("Physical Cash Out UUID Thread 2 starting...");
                BaseResponse<UuidResponse> response2 = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
                
                PhysicalCashOutUuidResult result2 = new PhysicalCashOutUuidResult();
                result2.threadId = 2;
                result2.success = response2.getSuccess();
                result2.errorCode = response2.getSuccess() ? StatusService.SUCCESSFUL : response2.getErrorDetail().getCode();
                result2.uuid = response2.getSuccess() ? response2.getData().getUniqueIdentifier() : null;
                result2.response = response2;
                
                synchronized (results) {
                    results.add(result2);
                    log.info("Physical Cash Out UUID Thread 2 added result to collection. Collection size now: {}", results.size());
                }
                log.info("Physical Cash Out UUID Thread 2 completed: Success={}, ErrorCode={}, UUID={}", result2.success, result2.errorCode, result2.uuid);
                
            } catch (Exception e) {
                log.error("Physical Cash Out UUID Thread 2 exception: {}", e.getMessage(), e);
                PhysicalCashOutUuidResult result2 = new PhysicalCashOutUuidResult();
                result2.threadId = 2;
                result2.success = false;
                result2.errorCode = -999;
                result2.exception = e;
                synchronized (results) {
                    results.add(result2);
                    log.info("Physical Cash Out UUID Thread 2 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("Physical Cash Out UUID Thread 2 countDown called");
            }
        });
        
        // Thread 3
        Thread thread3 = new Thread(() -> {
            try {
                log.info("Physical Cash Out UUID Thread 3 starting...");
                BaseResponse<UuidResponse> response3 = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
                
                PhysicalCashOutUuidResult result3 = new PhysicalCashOutUuidResult();
                result3.threadId = 3;
                result3.success = response3.getSuccess();
                result3.errorCode = response3.getSuccess() ? StatusService.SUCCESSFUL : response3.getErrorDetail().getCode();
                result3.uuid = response3.getSuccess() ? response3.getData().getUniqueIdentifier() : null;
                result3.response = response3;
                
                synchronized (results) {
                    results.add(result3);
                    log.info("Physical Cash Out UUID Thread 3 added result to collection. Collection size now: {}", results.size());
                }
                log.info("Physical Cash Out UUID Thread 3 completed: Success={}, ErrorCode={}, UUID={}", result3.success, result3.errorCode, result3.uuid);
                
            } catch (Exception e) {
                log.error("Physical Cash Out UUID Thread 3 exception: {}", e.getMessage(), e);
                PhysicalCashOutUuidResult result3 = new PhysicalCashOutUuidResult();
                result3.threadId = 3;
                result3.success = false;
                result3.errorCode = -999;
                result3.exception = e;
                synchronized (results) {
                    results.add(result3);
                    log.info("Physical Cash Out UUID Thread 3 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("Physical Cash Out UUID Thread 3 countDown called");
            }
        });
        
        // Start all threads
        log.info("Starting all physical cash out UUID generation threads...");
        thread1.start();
        thread2.start();
        thread3.start();
        
        // Wait and analyze
        log.info("Waiting for threads to complete...");
        latch.await();
        
        log.info("All threads completed, Results collection size: {}", results.size());
        
        // Log all results
        for (PhysicalCashOutUuidResult result : results) {
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
        
        log.info("=== Concurrent Physical Cash Out UUID Generation Test Completed ===");
    }


    /**
     * Test concurrent physical cash out UUID generation to ensure proper concurrency control.
     * This test verifies that multiple UUID generation requests work correctly.
     */
    @Test
    @Order(54)
    @DisplayName("concurrent physical cash out one account for check balance")
    void concurrentPhysicalCashOutGeneration() throws Exception {
        log.info("=== Starting Concurrent Physical Cash Out UUID Generation Test ===");

        // Setup
        String quantity = "5";
        String currency = "GOLD";

        // Get account
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, currency);
        setupBalancesForSellToZero(walletAccountObjectOptional);
        setupBalancesForSell(walletAccountObjectOptional,"10","1000000");

        int numberOfThreads = 10;
        // Test with 3 threads generating UUIDs simultaneously
        final CountDownLatch latch = new CountDownLatch(numberOfThreads);
        final List<PhysicalCashOutResult> results = new ArrayList<>();

        // Generate different UUIDs for each thread
        List<String> uniqueIdentifiers = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            BaseResponse<UuidResponse> uuidResponse = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
            uniqueIdentifiers.add(uuidResponse.getData().getUniqueIdentifier());
        }

        List<Thread> threads = new ArrayList<>();

        for(int i=0; i< numberOfThreads;i++){
            final int threadId = i;
            Thread thread = new Thread(() -> {
                try {
                    log.info("Physical Cash Out UUID Thread 1 starting...");
                    BaseResponse<PhysicalCashOutResponse> response = physicalCashOutWithoutCheckResult(mockMvc, accessToken, uniqueIdentifiers.get(threadId), quantity, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(),"","","", currency, "0.05",currency);

                    PhysicalCashOutResult result = new PhysicalCashOutResult();
                    result.threadId = 1;
                    result.success = response.getSuccess();
                    result.errorCode = response.getSuccess() ? StatusService.SUCCESSFUL : response.getErrorDetail().getCode();
                    result.response = response;
                    result.uuid = uniqueIdentifiers.get(threadId);

                    synchronized (results) {
                        results.add(result);
                        log.info("Physical Cash Out UUID Thread ({}) added result to collection. Collection size now: {}", threadId, results.size());
                    }
                    log.info("Physical Cash Out Thread ({}) completed: Success={}, ErrorCode={}", threadId, result.success, result.errorCode);

                } catch (Exception e) {
                    log.error("Physical Cash Out UUID Thread 1 exception: {}", e.getMessage(), e);
                    PhysicalCashOutResult result = new PhysicalCashOutResult();
                    result.threadId = threadId;
                    result.success = false;
                    result.errorCode = -999;
                    result.exception = e;
                    result.uuid = uniqueIdentifiers.get(threadId);
                    synchronized (results) {
                        results.add(result);
                        log.info("Physical Cash Out Thread ({}) added exception result to collection. Collection size now: {}", threadId, results.size());
                    }
                } finally {
                    latch.countDown();
                    log.info("Physical Cash Out Thread countDown called");
                }
            });
            threads.add(thread);
        }

        // Thread 1






        // Start all threads
        log.info("Starting all physical cash out threads...");
        // Start all threads
        log.info("Starting {} threads for Scenario 2...", numberOfThreads);
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait and analyze
        log.info("Waiting for threads to complete...");
        latch.await();

        log.info("All threads completed, Results collection size: {}", results.size());

        // Log all results
        for (PhysicalCashOutResult result : results) {
            log.info("Result: Thread={}, Success={}, ErrorCode={}", result.threadId, result.success, result.errorCode);
        }

        // Validation
        Assert.assertEquals("Should have exactly 10 results", 10, results.size());

        // All should succeed and generate different UUIDs
        long successCount = results.stream().filter(r -> r.success).count();
        Assert.assertEquals("All UUID generation should succeed", 2, successCount);

        // Verify failed operations have appropriate error codes
        List<PhysicalCashOutResult> failedResults = results.stream().filter(r -> !r.success).toList();
        for (PhysicalCashOutResult failedResult : failedResults) {
            log.debug("Scenario 2 - Thread {}: error code = {}, exception = {}, uuid = {}",
                    failedResult.threadId, failedResult.errorCode,
                    failedResult.exception != null ? failedResult.exception.getMessage() : "none", failedResult.uuid);
            Assert.assertEquals("Scenario 2: Failed operation should have error code indicating UUID reuse", StatusService.BALANCE_IS_NOT_ENOUGH, failedResult.errorCode);
        }

        log.info("=== Concurrent Physical Cash Out UUID Generation Test Completed ===");
    }

    /**
     * Result class for capturing physical cash out operation results from concurrent threads
     */
    private static class PhysicalCashOutResult {
        int threadId;
        boolean success;
        int errorCode;
        String uuid;
        BaseResponse<PhysicalCashOutResponse> response;
        Exception exception;

        @Override
        public String toString() {
            return String.format("PhysicalCashOutResult{threadId=%d, success=%s, errorCode=%d, hasResponse=%s, hasException=%s}",
                    threadId, success, errorCode, response != null, exception != null);
        }
    }

    /**
     * Result class for capturing physical cash out UUID generation results from concurrent threads
     */
    private static class PhysicalCashOutUuidResult {
        int threadId;
        boolean success;
        int errorCode;
        String uuid;
        BaseResponse<UuidResponse> response;
        Exception exception;

        @Override
        public String toString() {
            return String.format("PhysicalCashOutUuidResult{threadId=%d, success=%s, errorCode=%d, uuid=%s, hasResponse=%s, hasException=%s}",
                    threadId, success, errorCode, uuid, response != null, exception != null);
        }
    }

    @Test
    @Order(54)
    @DisplayName("physicalInquiryCashOut-fail-invalidUniqueIdentifier")
    void physicalInquiryCashOutFailInvalidUniqueIdentifier() throws Exception {
        log.info("start physicalInquiryCashOutFailInvalidUniqueIdentifier test");
        physicalInquiryCashOut(mockMvc, accessToken, "invalid_uuid", HttpStatus.OK, StatusService.UUID_NOT_FOUND, false);
    }





    /**
     * Helper method to setup balances for sell operations
     */
    private void setupBalancesForSell(WalletAccountObject walletAccountObjectOptional, String goldAmount, String rialAmount) {
        // Ensure user has enough GOLD balance for selling
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal(goldAmount));

        // Ensure merchant has enough RIAL balance for buying
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal(rialAmount));
    }


    /**
     * Helper method to setup balances for sell operations
     */
    private void setupBalancesForSellToZero(WalletAccountObject walletAccountObjectOptional) {
        // Ensure user has enough GOLD balance for selling
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        BigDecimal balance = walletAccountService.getBalance(goldWalletAccountEntity.getId());
        walletAccountService.decreaseBalance(goldWalletAccountEntity.getId(), balance);
    }
} 