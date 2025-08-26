package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.sync.ResourceSyncService;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashOutResponse;
import com.melli.wallet.domain.response.cash.CashOutTrackResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.service.repository.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: CashOutControllerTest
 * Author: Mahdi Shirinabadi
 * Date: 6/7/2025
 * 
 * This test class contains comprehensive end-to-end tests for Cash Out operations.
 * It tests the complete flow from wallet creation to cash out execution and inquiry.
 * 
 * Test Coverage:
 * - Wallet creation for testing
 * - Cash out execution (success and failure scenarios)
 * - Cash out inquiry (success and failure scenarios)
 * - Balance validation (insufficient balance)
 * - Amount validation (minimum, maximum limits)
 * - IBAN validation
 * - Duplicate request handling
 * - Account and wallet status validation
 * - National code validation
 * - Cash out permission validation
 */
@Log4j2
@DisplayName("CashOutControllerTest End2End test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CashOutControllerTest extends WalletApplicationTests {

    private static final String NATIONAL_CODE_CORRECT = "0077847660";
    private static final String NATIONAL_CODE_INCORRECT = "0077847661";
    private static final String MOBILE_CORRECT = "09124162337";
    private static final String VALID_IBAN = "IR220170000000368163985003";
    private static final String VALID_SIGN = "valid_sign_for_testing";
    private static final String ADDITIONAL_DATA = "Test cashout operation";

    private static MockMvc mockMvc;
    private static String ACCESS_TOKEN;
    private static String REFRESH_TOKEN;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private WalletAccountRepositoryService walletAccountRepositoryService;
    @Autowired
    private WalletRepositoryService walletRepositoryService;
    @Autowired
    private LimitationGeneralCustomRepositoryService limitationGeneralCustomRepositoryService;
    @Autowired
    private LimitationGeneralService limitationGeneralService;
    @Autowired
    private ChannelRepositoryService channelRepositoryService;
    @Autowired
    private CacheClearService cacheClearService;
    @Autowired
    private Flyway flyway;
    @Autowired
    private ResourceSyncService resourceSyncService;

    /**
     * Initial setup method that runs before all tests.
     * This method:
     * - Sets up MockMvc for testing
     * - Cleans and migrates the database
     * - Clears all caches
     */
    @Test
    @Order(1)
    @DisplayName("Initiate...")
    void initial() {
        // Setup MockMvc for testing with security
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);
        
        // Clean and migrate database
        log.info("start cleaning initial values in test DB");
        flyway.clean();
        flyway.migrate();
        resourceSyncService.syncResourcesOnStartup();
        
        // Clear all caches
        cacheClearService.clearCache();
    }

    /**
     * Test successful channel login.
     * This method:
     * - Performs login with correct credentials
     * - Stores the access token for subsequent tests
     */
    @Test
    @Order(10)
    @DisplayName("Channel login successfully")
    void login_success() throws Exception {
        log.info("start login_success test");
        BaseResponse<LoginResponse> response = login(mockMvc, USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.OK,
                StatusRepositoryService.SUCCESSFUL, true);
        ACCESS_TOKEN = response.getData().getAccessTokenObject().getToken();
        REFRESH_TOKEN = response.getData().getRefreshTokenObject().getToken();
    }

    /**
     * Test wallet creation for testing purposes.
     * This method:
     * - Creates a wallet for the test user
     * - Validates the wallet creation response
     * - Logs successful creation
     */
    @Test
    @Order(15)
    @DisplayName("Create wallet for testing")
    void createWalletForTesting() throws Exception {
        log.info("start createWalletForTesting test");
        
        // Create wallet for testing
        BaseResponse<CreateWalletResponse> response = createWallet(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        log.info("Wallet created successfully for testing");
    }

    /**
     * Test successful cash out operation.
     * This method:
     * - Gets user's RIAL account number
     * - Charges account with cash in to ensure sufficient balance
     * - Enables cash in and cash out permissions if disabled
     * - Generates UUID for cash out operation
     * - Performs cash out operation
     * - Validates the cash out response
     */
    @Test
    @Order(20)
    @DisplayName("cashOutSuccess")
    void cashOutSuccess() throws Exception {
        log.info("start cashOutSuccess test");
        
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // Step 2: Charge the account with cash in to ensure sufficient balance
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        
        // Step 3: Enable cash in permission if disabled
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        String cashInValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true");
        }
        
        // Step 4: Generate UUID for cash in operation
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        // Step 5: Perform cash in to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        log.info("Account charged successfully with amount: {}", cashInAmount);
        
        // Step 6: Get amount for cash out operation
        String cashOutAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        
        // Step 7: Enable cash out permission if disabled
        String cashOutValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_OUT, accountNumber);
        if("false".equalsIgnoreCase(cashOutValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_OUT, walletAccountEntity, "true");
        }
        
        // Step 8: Generate UUID for cash out operation
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Step 9: Perform cash out operation
        BaseResponse<CashOutResponse> response = cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, cashOutAmount, NATIONAL_CODE_CORRECT, accountNumber, VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertEquals(NATIONAL_CODE_CORRECT, response.getData().getNationalCode());
        Assert.assertEquals(accountNumber, response.getData().getWalletAccountNumber());
        Assert.assertNotNull(response.getData().getBalance());
        Assert.assertEquals(uniqueIdentifier, response.getData().getUniqueIdentifier());
        log.info("Cashout operation completed successfully");
    }

    /**
     * Test cash out failure with invalid unique identifier.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount for cash out operation
     * - Attempts cash out with invalid UUID
     * - Expects UUID_NOT_FOUND error
     */
    @Test
    @Order(21)
    @DisplayName("cashOutFail-InvalidUniqueIdentifier")
    void cashOutFailInvalidUniqueIdentifier() throws Exception {
        log.info("start cashOutFailInvalidUniqueIdentifier test");
        
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Step 2: Get minimum amount for cash out operation
        String amount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, walletAccountObject.getAccountNumber());
        
        // Step 3: Attempt cash out with invalid UUID - should fail
        cashOut(mockMvc, ACCESS_TOKEN, "invalid_uuid", amount, NATIONAL_CODE_CORRECT, walletAccountObject.getAccountNumber(), VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusRepositoryService.UUID_NOT_FOUND, false);
    }


    /**
     * Test cash out failure when balance is insufficient.
     * This method:
     * - Gets user's RIAL account number
     * - Charges account with cash in to have some balance
     * - Enables cash in permission if disabled
     * - Attempts cash out with amount + 1 (insufficient balance)
     * - Expects BALANCE_IS_NOT_ENOUGH error
     */
    @Test
    @Order(23)
    @DisplayName("cashOutFail-InsufficientBalance")
    void cashOutFailInsufficientBalance() throws Exception {
        log.info("start cashOutFailInsufficientBalance test");
        
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // Step 2: Charge the account with cash in to have some balance
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        
        // Step 3: Enable cash in permission if disabled
        String cashInValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashOutFailInsufficientBalance");
        }
        
        // Step 4: Generate UUID for cash in operation
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        // Step 5: Perform cash in to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        log.info("Account charged successfully with amount: {}", cashInAmount);
        
        // Step 6: Attempt cash out with amount + 1 (insufficient balance)
        String cashOutAmount = String.valueOf(Long.parseLong(cashInAmount) + 1);
        log.info("Attempting cashOut with amount: {} (original amount + 1)", cashOutAmount);
        
        // Step 7: Generate UUID for cash out operation
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Step 8: Attempt cash out - should fail due to insufficient balance
        cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, cashOutAmount, NATIONAL_CODE_CORRECT, accountNumber, VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusRepositoryService.BALANCE_IS_NOT_ENOUGH, false);
    }

    /**
     * Test cash out failure with duplicate request.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount for cash out operation
     * - Generates UUID for cash out operation
     * - Performs first cash out successfully
     * - Attempts second cash out with same UUID
     * - Expects DUPLICATE_UUID error
     */
    @Test
    @Order(24)
    @DisplayName("cashOutFail-DuplicateRequest")
    void     cashOutFailDuplicateRequest() throws Exception {
        log.info("start cashOutFailDuplicateRequest test");
        
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Step 2: Get minimum amount for cash out operation
        String amount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, walletAccountObject.getAccountNumber());
        
        // Step 3: Generate UUID for cash out operation
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObject.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Step 4: Perform first cash out successfully
        BaseResponse<CashOutResponse> firstResponse = cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, amount, NATIONAL_CODE_CORRECT, walletAccountObject.getAccountNumber(), VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(firstResponse.getData());
        
        // Step 5: Attempt second cash out with same UUID - should fail
        cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, amount, NATIONAL_CODE_CORRECT, walletAccountObject.getAccountNumber(), VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusRepositoryService.DUPLICATE_UUID, false);
    }

    /**
     * Test successful cash out inquiry operation.
     * This method:
     * - Gets user's RIAL account number
     * - Charges account with cash in to ensure sufficient balance
     * - Enables cash in and cash out permissions if disabled
     * - Generates UUID for cash out operation
     * - Performs cash out operation successfully
     * - Performs inquiry on the cash out operation
     * - Validates the inquiry response
     */
    @Test
    @Order(25)
    @DisplayName("inquiryCashOutSuccess")
    void inquiryCashOutSuccess() throws Exception {
        log.info("start inquiryCashOutSuccess test");
        // Step 1: Get account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        // Step 2: First, charge the account with cashIn to have sufficient balance
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        // Step 3: Enable cashIn if disabled
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        String cashInValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true");
        }
        // Step 4: Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        // Step 5: Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        log.info("Account charged successfully with amount: {}", cashInAmount);
        // Step 6: Now perform a successful cashout
        String cashOutAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        // Step 7: Enable cashout if disabled
        String cashOutValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_OUT, accountNumber);
        if("false".equalsIgnoreCase(cashOutValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_OUT, walletAccountEntity, "true"); //"test inquiryCashOutSuccess");
        }
        // Step 8: Generate UUID for cash out
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 9: Perform cash out operation
        BaseResponse<CashOutResponse> cashOutResponse = cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, cashOutAmount, NATIONAL_CODE_CORRECT, accountNumber, VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(cashOutResponse.getData());
        // Step 10: Now inquiry the cashout
        BaseResponse<CashOutTrackResponse> response = inquiryCashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertEquals(NATIONAL_CODE_CORRECT, response.getData().getNationalCode());
        Assert.assertEquals(Long.parseLong(cashOutAmount), response.getData().getAmount());
        Assert.assertEquals(uniqueIdentifier, response.getData().getUniqueIdentifier());
        Assert.assertEquals(StatusRepositoryService.SUCCESSFUL, response.getData().getResult());
        log.info("Cashout inquiry completed successfully");
    }

    /**
     * Test cash out inquiry failure with invalid unique identifier.
     * This method:
     * - Attempts inquiry with invalid UUID
     * - Expects UUID_NOT_FOUND error
     */
    @Test
    @Order(26)
    @DisplayName("inquiryCashOutFail-InvalidUniqueIdentifier")
    void inquiryCashOutFailInvalidUniqueIdentifier() throws Exception {
        log.info("start inquiryCashOutFailInvalidUniqueIdentifier test");
        // Step 1: Test with invalid UUID
        inquiryCashOut(mockMvc, ACCESS_TOKEN, "invalid_uuid", HttpStatus.OK, StatusRepositoryService.UUID_NOT_FOUND, false);
    }

    /**
     * Test cash out failure with invalid IBAN.
     * This method:
     * - Gets user's RIAL account number
     * - Charges account with cash in to ensure sufficient balance
     * - Enables cash in and cash out permissions if disabled
     * - Generates UUID for cash out operation
     * - Attempts cash out with invalid IBAN
     * - Expects IBAN_NOT_VALID error
     */
    @Test
    @Order(27)
    @DisplayName("cashOutFail-InvalidIban")
    void cashOutFailInvalidIban() throws Exception {
        log.info("start cashOutFailInvalidIban test");
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        // Step 2: First, charge the account with cashIn
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        // Step 3: Enable cashIn if disabled
        String cashInValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashOutFailInvalidIban");
        }
        // Step 4: Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        // Step 5: Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        // Step 6: Now try to cashOut with invalid IBAN
        String cashOutAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 7: This should fail due to invalid IBAN
        cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, cashOutAmount, NATIONAL_CODE_CORRECT, accountNumber, "INVALID_IBAN", VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);
    }

    /**
     * Test cash out failure when amount is less than minimum.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount and calculates amount below minimum
     * - Attempts to generate UUID for cash out operation
     * - Expects AMOUNT_LESS_THAN_MIN error
     */
    @Test
    @Order(28)
    @DisplayName("cashOutFail-AmountLessThanMinimum")
    void cashOutFailAmountLessThanMinimum() throws Exception {
        log.info("start cashOutFailAmountLessThanMinimum test");
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        // Step 2: First, charge the account with cashIn
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        // Step 3: Enable cashIn if disabled
        String cashInValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashOutFailAmountLessThanMinimum");
        }
        // Step 4: Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        // Step 5: Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        // Step 6: Now try to cashOut with amount less than minimum
        String minAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        String cashOutAmount = String.valueOf(Long.parseLong(minAmount) - 1);
        log.info("Attempting cashOut with amount: {} (less than minimum: {})", cashOutAmount, minAmount);
        // Step 7: This should fail due to amount less than minimum
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.AMOUNT_LESS_THAN_MIN, false);
    }

    /**
     * Test cash out failure when amount exceeds maximum.
     * This method:
     * - Gets user's RIAL account number
     * - Gets maximum amount and calculates amount above maximum
     * - Attempts to generate UUID for cash out operation
     * - Expects AMOUNT_BIGGER_THAN_MAX error
     */
    @Test
    @Order(29)
    @DisplayName("cashOutFail-AmountBiggerThanMaximum")
    void cashOutFailAmountBiggerThanMaximum() throws Exception {
        log.info("start cashOutFailAmountBiggerThanMaximum test");
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        // Step 2: First, charge the account with cashIn
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        // Step 3: Enable cashIn if disabled
        String cashInValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashOutFailAmountBiggerThanMaximum");
        }
        // Step 4: Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        // Step 5: Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        // Step 6: Now try to cashOut with amount bigger than maximum
        String maxAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_AMOUNT_CASH_OUT, accountNumber);
        String cashOutAmount = String.valueOf(Long.parseLong(maxAmount) + 1);
        log.info("Attempting cashOut with amount: {} (bigger than maximum: {})", cashOutAmount, maxAmount);
        // Step 7: This should fail due to amount bigger than maximum
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.AMOUNT_BIGGER_THAN_MAX, false);
    }

    /*@Test
    @Order(30)
    @DisplayName("cashOutFail-InvalidSign")
    void cashOutFailInvalidSign() throws Exception {
        log.info("start cashOutFailInvalidSign test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // First, charge the account with cashIn
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        
        // Enable cashIn if disabled
        String cashInValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashOutFailInvalidSign");
        }
        
        // Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        
        // Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        
        // Now try to cashOut with invalid sign
        String cashOutAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // This should fail due to invalid sign
        cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, cashOutAmount, NATIONAL_CODE_CORRECT, accountNumber, VALID_IBAN, "INVALID_SIGN", ADDITIONAL_DATA, HttpStatus.OK, StatusService.INVALID_SIGN, false);
    }*/

    /**
     * Test cash out failure when cash out is disabled.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount for cash out operation
     * - Disables cash out permission
     * - Attempts to generate UUID for cash out operation
     * - Expects CASH_OUT_DISABLED error
     * - Re-enables cash out permission
     */
    @Test
    @Order(31)
    @DisplayName("cashOutFail-CashOutDisabled")
    void cashOutFailCashOutDisabled() throws Exception {
        log.info("start cashOutFailCashOutDisabled test");
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        // Step 2: Disable cashOut
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_OUT, walletAccountEntity, "false");
        // Step 3: Get minimum cash out amount
        String cashOutAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        // Step 4: This should fail due to cashOut being disabled
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.ACCOUNT_DONT_PERMISSION_FOR_CASH_OUT, false);
        // Step 5: Re-enable cashOut for other tests
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_OUT, walletAccountEntity, "true"); //"re-enable cashOut after test");
    }

    /**
     * Test cash out failure with invalid national code.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount for cash out operation
     * - Attempts to generate UUID with invalid national code
     * - Expects INPUT_PARAMETER_NOT_VALID error
     */
    @Test
    @Order(32)
    @DisplayName("cashOutFail-InvalidNationalCode")
    void cashOutFailInvalidNationalCode() throws Exception {
        log.info("start cashOutFailInvalidNationalCode test");
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        // Step 2: First, charge the account with cashIn
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        // Step 3: Enable cashIn if disabled
        String cashInValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashOutFailInvalidNationalCode");
        }
        // Step 4: Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        // Step 5: Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        // Step 6: Now try to cashOut with invalid national code
        String cashOutAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 7: This should fail due to invalid national code
        cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, cashOutAmount, NATIONAL_CODE_INCORRECT, accountNumber, VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);
    }

    /**
     * Test cash out failure with invalid account number.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount for cash out operation
     * - Attempts to generate UUID with invalid account number
     * - Expects INPUT_PARAMETER_NOT_VALID error
     */
    /**
     * Test concurrent cash out operations with same UUID to ensure proper concurrency control.
     * This test verifies that when the same UUID is used simultaneously, only one operation succeeds.
     */
    @Test
    @Order(34)
    @DisplayName("concurrent cash out operations with same UUID")
    void concurrentCashOutWithSameUuid() throws Exception {
        log.info("=== Starting Concurrent Cash Out Test with Same UUID ===");
        
        // Setup
        String amount = "100000";
        String iban = VALID_IBAN;
        String sign = VALID_SIGN;
        String additionalData = "concurrent cash out test";
        
        // Get account and setup balance
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Ensure user has enough RIAL balance for cash out
        WalletAccountEntity rialWalletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountRepositoryService.increaseBalance(rialWalletAccountEntity.getId(), new BigDecimal("500000"));
        
        // Generate UUID
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String sharedUniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        log.info("Generated UUID: {}", sharedUniqueIdentifier);
        
        // Test with 2 threads using the same UUID
        final CountDownLatch latch = new CountDownLatch(2);
        final List<CashOutResult> results = new ArrayList<>();
        
        // Thread 1
        Thread thread1 = new Thread(() -> {
            try {
                log.info("Cash Out Thread 1 starting...");
                BaseResponse<CashOutResponse> response1 = cashOutWithoutCheckResult(mockMvc, ACCESS_TOKEN, sharedUniqueIdentifier, amount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), iban, sign, additionalData + "_1");
                CashOutResult result1 = new CashOutResult();
                result1.threadId = 1;
                result1.success = response1.getSuccess();
                result1.errorCode = response1.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response1.getErrorDetail().getCode();
                result1.response = response1;
                
                synchronized (results) {
                    results.add(result1);
                    log.info("Cash Out Thread 1 added result to collection. Collection size now: {}", results.size());
                }
                log.info("Cash Out Thread 1 completed: Success={}, ErrorCode={}", result1.success, result1.errorCode);
                
            } catch (Exception e) {
                log.error("Cash Out Thread 1 exception: {}", e.getMessage(), e);
                CashOutResult result1 = new CashOutResult();
                result1.threadId = 1;
                result1.success = false;
                result1.errorCode = -999;
                result1.exception = e;
                synchronized (results) {
                    results.add(result1);
                    log.info("Cash Out Thread 1 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("Cash Out Thread 1 countDown called");
            }
        });
        
        // Thread 2
        Thread thread2 = new Thread(() -> {
            try {
                log.info("Cash Out Thread 2 starting...");
                BaseResponse<CashOutResponse> response2 = cashOutWithoutCheckResult(mockMvc, ACCESS_TOKEN, sharedUniqueIdentifier, amount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(),iban, sign, additionalData + "_2");
                
                CashOutResult result2 = new CashOutResult();
                result2.threadId = 2;
                result2.success = response2.getSuccess();
                result2.errorCode = response2.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response2.getErrorDetail().getCode();
                result2.response = response2;
                
                synchronized (results) {
                    results.add(result2);
                    log.info("Cash Out Thread 2 added result to collection. Collection size now: {}", results.size());
                }
                log.info("Cash Out Thread 2 completed: Success={}, ErrorCode={}", result2.success, result2.errorCode);
                
            } catch (Exception e) {
                log.error("Cash Out Thread 2 exception: {}", e.getMessage(), e);
                CashOutResult result2 = new CashOutResult();
                result2.threadId = 2;
                result2.success = false;
                result2.errorCode = -999;
                result2.exception = e;
                synchronized (results) {
                    results.add(result2);
                    log.info("Cash Out Thread 2 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("Cash Out Thread 2 countDown called");
            }
        });
        
        // Start both threads
        log.info("Starting both cash out threads...");
        thread1.start();
        thread2.start();
        
        // Wait and analyze
        log.info("Waiting for threads to complete...");
        latch.await();
        
        log.info("All threads completed, Results collection size: {}", results.size());
        
        // Log all results
        for (CashOutResult result : results) {
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
        CashOutResult failedResult = results.stream().filter(r -> !r.success).findFirst().orElse(null);
        Assert.assertNotNull("Should have a failed result", failedResult);
        Assert.assertEquals(StatusRepositoryService.DUPLICATE_UUID, failedResult.errorCode);
        log.info("Failed result error code: {}", failedResult.errorCode);
        
        log.info("=== Concurrent Cash Out Test Completed ===");
    }

    /**
     * Test concurrent cash out UUID generation to ensure proper concurrency control.
     * This test verifies that multiple UUID generation requests work correctly.
     */
    @Test
    @Order(35)
    @DisplayName("concurrent cash out UUID generation")
    void concurrentCashOutUuidGeneration() throws Exception {
        log.info("=== Starting Concurrent Cash Out UUID Generation Test ===");
        
        // Setup
        String amount = "100000";
        String iban = VALID_IBAN;
        
        // Get account
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Test with 3 threads generating UUIDs simultaneously
        final CountDownLatch latch = new CountDownLatch(3);
        final List<CashOutUuidResult> results = new ArrayList<>();
        
        // Thread 1
        Thread thread1 = new Thread(() -> {
            try {
                log.info("Cash Out UUID Thread 1 starting...");
                BaseResponse<UuidResponse> response1 = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                
                CashOutUuidResult result1 = new CashOutUuidResult();
                result1.threadId = 1;
                result1.success = response1.getSuccess();
                result1.errorCode = response1.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response1.getErrorDetail().getCode();
                result1.uuid = response1.getSuccess() ? response1.getData().getUniqueIdentifier() : null;
                result1.response = response1;
                
                synchronized (results) {
                    results.add(result1);
                    log.info("Cash Out UUID Thread 1 added result to collection. Collection size now: {}", results.size());
                }
                log.info("Cash Out UUID Thread 1 completed: Success={}, ErrorCode={}, UUID={}", result1.success, result1.errorCode, result1.uuid);
                
            } catch (Exception e) {
                log.error("Cash Out UUID Thread 1 exception: {}", e.getMessage(), e);
                CashOutUuidResult result1 = new CashOutUuidResult();
                result1.threadId = 1;
                result1.success = false;
                result1.errorCode = -999;
                result1.exception = e;
                synchronized (results) {
                    results.add(result1);
                    log.info("Cash Out UUID Thread 1 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("Cash Out UUID Thread 1 countDown called");
            }
        });
        
        // Thread 2
        Thread thread2 = new Thread(() -> {
            try {
                log.info("Cash Out UUID Thread 2 starting...");
                BaseResponse<UuidResponse> response2 = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                
                CashOutUuidResult result2 = new CashOutUuidResult();
                result2.threadId = 2;
                result2.success = response2.getSuccess();
                result2.errorCode = response2.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response2.getErrorDetail().getCode();
                result2.uuid = response2.getSuccess() ? response2.getData().getUniqueIdentifier() : null;
                result2.response = response2;
                
                synchronized (results) {
                    results.add(result2);
                    log.info("Cash Out UUID Thread 2 added result to collection. Collection size now: {}", results.size());
                }
                log.info("Cash Out UUID Thread 2 completed: Success={}, ErrorCode={}, UUID={}", result2.success, result2.errorCode, result2.uuid);
                
            } catch (Exception e) {
                log.error("Cash Out UUID Thread 2 exception: {}", e.getMessage(), e);
                CashOutUuidResult result2 = new CashOutUuidResult();
                result2.threadId = 2;
                result2.success = false;
                result2.errorCode = -999;
                result2.exception = e;
                synchronized (results) {
                    results.add(result2);
                    log.info("Cash Out UUID Thread 2 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("Cash Out UUID Thread 2 countDown called");
            }
        });
        
        // Thread 3
        Thread thread3 = new Thread(() -> {
            try {
                log.info("Cash Out UUID Thread 3 starting...");
                BaseResponse<UuidResponse> response3 = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                
                CashOutUuidResult result3 = new CashOutUuidResult();
                result3.threadId = 3;
                result3.success = response3.getSuccess();
                result3.errorCode = response3.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response3.getErrorDetail().getCode();
                result3.uuid = response3.getSuccess() ? response3.getData().getUniqueIdentifier() : null;
                result3.response = response3;
                
                synchronized (results) {
                    results.add(result3);
                    log.info("Cash Out UUID Thread 3 added result to collection. Collection size now: {}", results.size());
                }
                log.info("Cash Out UUID Thread 3 completed: Success={}, ErrorCode={}, UUID={}", result3.success, result3.errorCode, result3.uuid);
                
            } catch (Exception e) {
                log.error("Cash Out UUID Thread 3 exception: {}", e.getMessage(), e);
                CashOutUuidResult result3 = new CashOutUuidResult();
                result3.threadId = 3;
                result3.success = false;
                result3.errorCode = -999;
                result3.exception = e;
                synchronized (results) {
                    results.add(result3);
                    log.info("Cash Out UUID Thread 3 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("Cash Out UUID Thread 3 countDown called");
            }
        });
        
        // Start all threads
        log.info("Starting all cash out UUID generation threads...");
        thread1.start();
        thread2.start();
        thread3.start();
        
        // Wait and analyze
        log.info("Waiting for threads to complete...");
        latch.await();
        
        log.info("All threads completed, Results collection size: {}", results.size());
        
        // Log all results
        for (CashOutUuidResult result : results) {
            log.info("Result: Thread={}, Success={}, ErrorCode={}, UUID={}", result.threadId, result.success, result.errorCode, result.uuid);
        }
        
        // Validation
        Assert.assertTrue("Should have exactly 3 results", results.size() == 3);
        
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
        
        log.info("=== Concurrent Cash Out UUID Generation Test Completed ===");
    }

    /**
     * Result class for capturing cash out operation results from concurrent threads
     */
    private static class CashOutResult {
        int threadId;
        boolean success;
        int errorCode;
        BaseResponse<CashOutResponse> response;
        Exception exception;

        @Override
        public String toString() {
            return String.format("CashOutResult{threadId=%d, success=%s, errorCode=%d, hasResponse=%s, hasException=%s}",
                    threadId, success, errorCode, response != null, exception != null);
        }
    }

    /**
     * Result class for capturing cash out UUID generation results from concurrent threads
     */
    private static class CashOutUuidResult {
        int threadId;
        boolean success;
        int errorCode;
        String uuid;
        BaseResponse<UuidResponse> response;
        Exception exception;

        @Override
        public String toString() {
            return String.format("CashOutUuidResult{threadId=%d, success=%s, errorCode=%d, uuid=%s, hasResponse=%s, hasException=%s}",
                    threadId, success, errorCode, uuid, response != null, exception != null);
        }
    }

    @Test
    @Order(36)
    @DisplayName("cashOutFail-InvalidAccountNumber")
    void cashOutFailInvalidAccountNumber() throws Exception {
        log.info("start cashOutFailInvalidAccountNumber test");
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        // Step 2: First, charge the account with cashIn
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        // Step 3: Enable cashIn if disabled
        String cashInValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashOutFailInvalidAccountNumber");
        }
        // Step 4: Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        // Step 5: Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        // Step 6: Now try to cashOut with invalid account number
        String cashOutAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 7: This should fail due to invalid account number
        cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, cashOutAmount, NATIONAL_CODE_CORRECT, "INVALID_ACCOUNT_NUMBER", VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);
    }

}
