package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.sync.ResourceSyncService;
import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.master.entity.WalletTypeEntity;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
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
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: CashInControllerTest
 * Author: Mahdi Shirinabadi
 * Date: 4/7/2025
 * 
 * This test class contains comprehensive end-to-end tests for Cash In operations.
 * It tests the complete flow from wallet creation to cash in execution and validation.
 * 
 * Test Coverage:
 * - Wallet creation and balance retrieval (success and failure scenarios)
 * - Cash in UUID generation and execution (success and failure scenarios)
 * - Amount validation (minimum, maximum, balance limits)
 * - Account validation (permissions, status, ownership)
 * - Duplicate request handling (refNumber, RRN)
 * - National code validation
 * - Wallet and account status validation
 */
@Log4j2
@DisplayName("CashInControllerTest End2End test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CashInControllerTest extends WalletApplicationTests {



    private static final String NATIONAL_CODE_CORRECT = "0077847660";
    private static final String NATIONAL_CODE_INCORRECT = "0077847661";
    private static final String NATIONAL_CODE_LENGTH_LESS_THAN_STANDARD = "0077847661";
    private static final String NATIONAL_CODE_LENGTH_BIGGER_THAN_STANDARD = "00778476611";
    private static final String MOBILE_CORRECT = "09124162337";

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
    private WalletTypeRepositoryService walletTypeRepositoryService;
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
     * Test balance retrieval failure when wallet not found.
     * This method:
     * - Attempts to get balance for non-existent wallet
     * - Expects WALLET_NOT_FOUND error
     */
    @Test
    @Order(15)
    @DisplayName("get balance fail- wallet not found")
    void getBalanceFail() throws Exception {
        balance(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.WALLET_NOT_FOUND, false);
    }
    /**
     * Test balance retrieval failure with invalid national code.
     * This method:
     * - Attempts to get balance with incorrect national code
     * - Expects INPUT_PARAMETER_NOT_VALID error
     */
    @Test
    @Order(16)
    @DisplayName("get balance fail- invalid nationalCode")
    void getBalanceFailInvalidNationalCode() throws Exception {
        balance(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_INCORRECT, HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);
    }
    /**
     * Test balance retrieval failure with national code shorter than standard.
     * This method:
     * - Attempts to get balance with national code shorter than required length
     * - Expects INPUT_PARAMETER_NOT_VALID error
     */
    @Test
    @Order(17)
    @DisplayName("get balance fail- NationalCodeLessStandard")
    void getBalanceFailNationalCodeLessStandard() throws Exception {
        balance(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_LENGTH_LESS_THAN_STANDARD, HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);
    }
    /**
     * Test balance retrieval failure with national code longer than standard.
     * This method:
     * - Attempts to get balance with national code longer than required length
     * - Expects INPUT_PARAMETER_NOT_VALID error
     */
    @Test
    @Order(18)
    @DisplayName("get balance fail- NationalCodeBiggerStandard")
    void getBalanceFailNationalCodeBiggerStandard() throws Exception {
        balance(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_LENGTH_BIGGER_THAN_STANDARD, HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);
    }

    /**
     * Test successful wallet creation.
     * This method:
     * - Creates a wallet for the test user
     * - Validates the wallet creation response
     * - Verifies the wallet has correct national code
     */
    @Test
    @Order(20)
    @DisplayName("create wallet- success")
    void createWalletSuccess() throws Exception {
        // Step 1: Create wallet for the test user
        BaseResponse<CreateWalletResponse> response = createWallet(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        // Step 2: Validate wallet creation and verify national code
        WalletEntity walletEntity = walletRepositoryService.findById(Long.parseLong(response.getData().getWalletId()));
        if (!walletEntity.getNationalCode().equalsIgnoreCase(NATIONAL_CODE_CORRECT)) {
            log.error("wallet create not same with national code ({})", NATIONAL_CODE_CORRECT);
            throw new Exception("wallet create not same with national code ({})");
        }
    }

    /**
     * Test successful duplicate wallet creation (should return existing wallet).
     * This method:
     * - Finds existing wallet for the user
     * - Attempts to create duplicate wallet
     * - Validates that the same wallet ID is returned
     */
    @Test
    @Order(21)
    @DisplayName("create wallet duplicate - success")
    void createWalletDuplicateSuccess() throws Exception {
        // Step 1: Get wallet type for normal user
        List<WalletTypeEntity> walletTypeEntityList = walletTypeRepositoryService.getAll();

        WalletTypeEntity walletTypeEntity = walletTypeEntityList.stream().filter(x -> x.getName().equalsIgnoreCase(WalletTypeRepositoryService.NORMAL_USER)).findFirst().orElseThrow(() -> {
            log.error("walletType not found");
            return new Exception("walletType not found");
        });

        // Step 2: Find existing wallet for the user
        WalletEntity walletExistEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId(NATIONAL_CODE_CORRECT, walletTypeEntity.getId());

        // Step 3: Attempt to create duplicate wallet and validate same ID is returned
        BaseResponse<CreateWalletResponse> response = createWallet(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        if (walletExistEntity.getId() != Long.parseLong(response.getData().getWalletId())) {
            log.error("create new wallet not same with created wallet!!!");
            throw new Exception("create new wallet not same with created wallet!!!");
        }
    }

    /**
     * Test successful balance retrieval.
     * This method:
     * - Retrieves balance for existing wallet
     * - Validates the balance response
     */
    @Test
    @Order(25)
    @DisplayName("get balance success")
    void getBalanceSuccess() throws Exception {
        balance(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
    }

    /**
     * Test cash in failure when account lacks permission.
     * This method:
     * - Gets user's RIAL account number
     * - Attempts to generate cash in UUID without permission
     * - Expects ACCOUNT_DONT_PERMISSION_FOR_CASH_IN error
     */
    @Test
    @Order(26)
    @DisplayName("cashInFailNotPermission")
    void cashInFailNotPermission() throws Exception {
        // Step 1: Define test parameters
        String refNumber = new Date().getTime() + "";
        String amount = "1000000";
        
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Step 3: Attempt to generate cash in UUID without permission
        BaseResponse<UuidResponse> responseUuid = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.ACCOUNT_DONT_PERMISSION_FOR_CASH_IN, false);
    }


    /**
     * Test cash in failure when amount is less than minimum.
     * This method:
     * - Gets user's RIAL account number
     * - Enables cash in permission if disabled
     * - Temporarily decreases MIN_AMOUNT_CASH_IN limit
     * - Attempts cash in with amount below minimum
     * - Expects AMOUNT_LESS_THAN_MIN error
     * - Restores original MIN_AMOUNT_CASH_IN limit
     */
    @Test
    @Order(28)
    @DisplayName("cashInFail-min amount")
    void cashInFailMinAmount() throws Exception {
        // Step 1: Define test parameters
        String refNumber = new Date().getTime() + "";
        String amount = "10";
        
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        
        // Step 3: Enable cash in permission if disabled
        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true");
        }

        // Step 4: Store original MIN_AMOUNT_CASH_IN value and temporarily decrease it
        String valueMinAmountCashIn = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        boolean changeMinAmountCashIn = false;
        if( Long.parseLong(amount) >= Long.parseLong(valueMinAmountCashIn)){
            changeMinAmountCashIn = true;
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountEntity, String.valueOf(Long.parseLong(valueMinAmountCashIn) - 1));
        }
        
        // Step 5: Attempt cash in with amount below minimum
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.AMOUNT_LESS_THAN_MIN, false);
        
        // Step 6: Restore original MIN_AMOUNT_CASH_IN limit
        if(changeMinAmountCashIn){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountEntity, valueMinAmountCashIn);
        }
    }

    /**
     * Test cash in failure when amount exceeds maximum limit.
     * This method:
     * - Gets user's RIAL account number
     * - Enables cash in permission if disabled
     * - Temporarily decreases MAX_AMOUNT_CASH_IN limit
     * - Attempts cash in with amount above maximum
     * - Expects AMOUNT_BIGGER_THAN_MAX error
     * - Restores original MAX_AMOUNT_CASH_IN limit
     */
    @Test
    @Order(29)
    @DisplayName("cashInFail-max amount")
    void cashInFailMaxAmount() throws Exception {
        // Step 1: Define test parameters
        String refNumber = new Date().getTime() + "";
        String amount = "10000000";
        
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        
        // Step 3: Enable cash in permission if disabled
        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true");
        }

        // Step 4: Store original MAX_AMOUNT_CASH_IN value and temporarily decrease it
        String valueMaxAmountCashIn = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.MAX_AMOUNT_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        boolean changeMaxAmountCashIn = false;
        if( Long.parseLong(amount) <= Long.parseLong(valueMaxAmountCashIn)){
            changeMaxAmountCashIn = true;
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_AMOUNT_CASH_IN, walletAccountEntity, String.valueOf(Long.parseLong(amount) - 1));
        }
        
        // Step 5: Attempt cash in with amount above maximum
        log.info("start get balance test");
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.AMOUNT_BIGGER_THAN_MAX, false);
        
        // Step 6: Restore original MAX_AMOUNT_CASH_IN limit
        if(changeMaxAmountCashIn){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_WALLET_BALANCE, walletAccountEntity, valueMaxAmountCashIn);
        }
    }

    /**
     * Test cash in failure when balance exceeds maximum wallet balance.
     * This method:
     * - Gets user's RIAL account number
     * - Enables cash in permission if disabled
     * - Temporarily decreases MAX_WALLET_BALANCE limit
     * - Attempts cash in that would exceed maximum balance
     * - Expects BALANCE_MORE_THAN_STANDARD error
     * - Restores original MAX_WALLET_BALANCE limit
     */
    @Test
    @Order(30)
    @DisplayName("cashInFail-max balance")
    void cashInFailMaxBalance() throws Exception {
        // Step 1: Define test parameters
        String refNumber = new Date().getTime() + "";
        String amount ="100000";
        
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());

        // Step 3: Enable cash in permission if disabled
        String valueEnableCashIn = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(valueEnableCashIn)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true");
        }

        // Step 4: Store original MAX_WALLET_BALANCE value and temporarily decrease it
        String valueMaxWalletBalance = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.MAX_WALLET_BALANCE,  walletAccountObjectOptional.getAccountNumber());
        boolean changeMaxBalance = false;
        if( Long.parseLong(amount) <= Long.parseLong(valueMaxWalletBalance)){
            changeMaxBalance = true;
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_WALLET_BALANCE, walletAccountEntity, String.valueOf(Long.parseLong(amount) - 1));
        }
        
        // Step 5: Attempt cash in that would exceed maximum balance
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.BALANCE_MORE_THAN_STANDARD, false);
        
        // Step 6: Restore original MAX_WALLET_BALANCE limit
        if(changeMaxBalance){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_WALLET_BALANCE, walletAccountEntity, valueMaxWalletBalance);
        }

    }

    /**
     * Test cash in failure when using duplicate reference number.
     * This method:
     * - Gets user's RIAL account number
     * - Enables cash in permission if disabled
     * - Generates UUID for cash in operation
     * - Performs successful cash in operation
     * - Attempts duplicate cash in with same reference number
     * - Expects DUPLICATE_REF_NUMBER error
     */
    @Test
    @Order(32)
    @DisplayName("cashInFail-duplicate refNumber")
    void cashInFailDuplicateRefnumber() throws Exception {
        // Step 1: Define test parameters
        String refNumber = new Date().getTime() + "";
        String amount = "10000";
        
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Step 3: Generate two UUIDs for cash in operations
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        BaseResponse<UuidResponse> uniqueIdentifier2 = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        // Step 4: Enable cash in permission if disabled
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashInFailDuplicateRefnumber");
        }
        
        // Step 5: Perform first cash in operation successfully
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, amount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        // Step 6: Attempt duplicate cash in with same reference number - should fail
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifier2.getData().getUniqueIdentifier(), refNumber, amount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.REF_NUMBER_USED_BEFORE, false);
    }

    /**
     * Test cash in failure when using duplicate RRN (UUID).
     * This method:
     * - Gets user's RIAL account number
     * - Decreases account balance to ensure sufficient space
     * - Enables cash in permission if disabled
     * - Generates UUID for cash in operation
     * - Performs successful cash in operation
     * - Attempts duplicate cash in with same UUID but different ref number
     * - Expects DUPLICATE_UUID error
     */
    @Test
    @Order(33)
    @DisplayName("cashInFail-duplicate rrn")
    void cashInFailDuplicateRrn() throws Exception {
        // Step 1: Define test parameters
        String refNumber = new Date().getTime() + "";
        
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        BigDecimal balance = BigDecimal.valueOf(Float.parseFloat(walletAccountObjectOptional.getBalance()));

        // Step 3: Decrease account balance to ensure sufficient space
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountRepositoryService.decreaseBalance(walletAccountEntity.getId(), balance);

        // Step 4: Enable cash in permission if disabled
        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashInFailDuplicateRrn");
        }
        
        // Step 5: Get minimum amount and generate UUID for cash in operation
        String minAmount = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(Long.parseLong(minAmount) + 1), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        // Step 6: Perform first cash in operation successfully
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, String.valueOf(Long.parseLong(minAmount) + 1), NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        // Step 7: Attempt duplicate cash in with same UUID but different ref number - should fail
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), new Date().getTime() + "", String.valueOf(Long.parseLong(minAmount) + 1), NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.DUPLICATE_UUID, false);
    }

    /**
     * Test successful cash in operation.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount for cash in operation
     * - Generates UUID for cash in operation
     * - Enables cash in permission if disabled
     * - Performs cash in operation successfully
     * - Validates the cash in response
     */
    @Test
    @Order(40)
    @DisplayName("cashIn success")
    void cashInSuccess() throws Exception {
        // Step 1: Define test parameters
        String refNumber = new Date().getTime() + "";
        
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Step 3: Get minimum amount for cash in operation
        String amount = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        
        // Step 4: Generate UUID for cash in operation
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifier1);
        
        // Step 5: Enable cash in permission if disabled
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashInSuccess");
        }
        
        // Step 6: Perform cash in operation successfully
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, amount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
    }


    /**
     * Test cash in failure when amount doesn't match UUID.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount for cash in operation
     * - Generates UUID for cash in operation
     * - Enables cash in permission if disabled
     * - Attempts cash in with different amount than UUID
     * - Expects PRICE_NOT_SAME_WITH_UUID error
     */
    @Test
    @Order(41)
    @DisplayName("cashInFailAmountUuidNotSame")
    void cashInFailAmountUuidNotSame() throws Exception {
        // Step 1: Define test parameters
        String refNumber = new Date().getTime() + "";
        
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Step 3: Get minimum amount for cash in operation
        String amount = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        
        // Step 4: Generate UUID for cash in operation
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifier1);
        
        // Step 5: Enable cash in permission if disabled
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashInFailAmountUuidNotSame");
        }
        
        // Step 6: Attempt cash in with different amount than UUID - should fail
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, String.valueOf(Long.parseLong(amount) + 1), NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.PRICE_NOT_SAME_WITH_UUID, false);
    }


    /**
     * Test cash in failure when account number doesn't match UUID.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount for cash in operation
     * - Generates UUID for cash in operation
     * - Enables cash in permission if disabled
     * - Attempts cash in with different account number than UUID
     * - Expects ACCOUNT_NUMBER_NOT_SAME_WITH_UUID error
     */
    @Test
    @Order(42)
    @DisplayName("cashInFailAccountUuidNotSame")
    void cashInFailAccountNotFound() throws Exception {
        // Step 1: Define test parameters
        String refNumber = new Date().getTime() + "";
        
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Step 3: Get minimum amount for cash in operation
        String amount = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        
        // Step 4: Generate UUID for cash in operation
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifier1);
        
        // Step 5: Enable cash in permission if disabled
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashInFailAccountNotFound");
        }
        
        // Step 6: Attempt cash in with different account number than UUID - should fail
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, String.valueOf(Long.parseLong(amount)), NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber()+"1", "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.ACCOUNT_NUMBER_NOT_SAME_WITH_UUID, false);
    }


    /**
     * Test cash in failure when wallet account not found in UUID.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount for cash in operation
     * - Attempts to generate UUID with non-existent account number
     * - Expects WALLET_ACCOUNT_NOT_FOUND error
     */
    @Test
    @Order(43)
    @DisplayName("cashInFailWalletAccountNotFound")
    void cashInFailAccountNotFoundInUuid() throws Exception {
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Step 2: Get minimum amount for cash in operation
        String amount = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        
        // Step 3: Attempt to generate UUID with non-existent account number - should fail
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber()+"1", HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, false);
        
        // Step 4: Enable cash in permission if disabled (for cleanup)
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashInFailAccountNotFoundInUuid");
        }
    }



    /**
     * Test cash in failure when account is not active.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount for cash in operation
     * - Disables the account status
     * - Attempts to generate UUID for cash in operation
     * - Expects ACCOUNT_NOT_ACTIVE error
     * - Re-enables the account status
     */
    @Test
    @Order(44)
    @DisplayName("cashInFailAccountNotActive")
    void cashInFailAccountNotActive() throws Exception {

        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Step 2: Get minimum amount for cash in operation
        String amount = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        
        // Step 3: Disable the account status
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountEntity.setStatus(WalletStatusEnum.DISABLE);
        walletAccountRepositoryService.save(walletAccountEntity);
        walletAccountRepositoryService.clearCache(walletAccountObjectOptional.getAccountNumber());
        
        // Step 4: Attempt to generate UUID for cash in operation - should fail
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_IS_NOT_ACTIVE, false);

        // Step 5: Enable cash in permission if disabled
        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashInFailAccountNotActive");
        }
        
        // Step 6: Re-enable the account status
        walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountEntity.setStatus(WalletStatusEnum.ACTIVE);
        walletAccountRepositoryService.save(walletAccountEntity);
        walletAccountRepositoryService.clearCache(walletAccountObjectOptional.getAccountNumber());
    }


    /**
     * Test cash in failure when wallet is not active.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount for cash in operation
     * - Disables the wallet status
     * - Attempts to generate UUID for cash in operation
     * - Expects WALLET_IS_NOT_ACTIVE error
     * - Re-enables the wallet status
     */
    @Test
    @Order(45)
    @DisplayName("cashInFailWalletNotActive")
    void cashInFailWalletNotActive() throws Exception {

        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Step 2: Get minimum amount for cash in operation
        String amount = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());

        // Step 3: Disable the wallet status
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountEntity.getWalletEntity().setStatus(WalletStatusEnum.DISABLE);
        walletRepositoryService.save(walletAccountEntity.getWalletEntity());
        walletRepositoryService.clearAllCache();

        // Step 4: Attempt to generate UUID for cash in operation - should fail
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.WALLET_IS_NOT_ACTIVE, false);

        // Step 5: Enable cash in permission if disabled
        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true"); //"test cashInFailWalletNotActive");
        }
        
        // Step 6: Re-enable the wallet status
        walletAccountEntity.getWalletEntity().setStatus(WalletStatusEnum.ACTIVE);
        walletRepositoryService.save(walletAccountEntity.getWalletEntity());
        walletRepositoryService.clearAllCache();

    }

    /**
     * Test cash in failure with invalid national code.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount for cash in operation
     * - Attempts to generate UUID with invalid national code
     * - Expects INPUT_PARAMETER_NOT_VALID error
     */
    @Test
    @Order(46)
    @DisplayName("cashInFailInvalidNationalCode")
    void cashInFailInvalidNationalCode() throws Exception {
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Step 2: Get minimum amount for cash in operation
        String amount = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());

        // Step 4: Attempt to generate UUID with invalid national code - should fail
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_INCORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);
    }

    /**
     * Test cash in failure with invalid amount format.
     * This method:
     * - Gets user's RIAL account number
     * - Disables wallet status for testing
     * - Attempts to generate UUID with invalid amount format
     * - Expects INPUT_PARAMETER_NOT_VALID error
     */
    @Test
    @Order(47)
    @DisplayName("cashInFailInvalidAmount")
    void cashInFailInvalidAmount() throws Exception {
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Step 2: Disable wallet status for testing
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountEntity.getWalletEntity().setStatus(WalletStatusEnum.DISABLE);
        walletRepositoryService.save(walletAccountEntity.getWalletEntity());
        walletRepositoryService.clearAllCache();
        
        // Step 3: Attempt to generate UUID with invalid amount format - should fail
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf("aa"), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);

        // Step 2: Disable wallet status for testing
        walletAccountEntity.getWalletEntity().setStatus(WalletStatusEnum.ACTIVE);
        walletRepositoryService.save(walletAccountEntity.getWalletEntity());
        walletRepositoryService.clearAllCache();
    }

    /**
     * Test cash in failure with invalid account number format.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount for cash in operation
     * - Disables wallet status for testing
     * - Attempts to generate UUID with invalid account number format
     * - Expects INPUT_PARAMETER_NOT_VALID error
     */
    @Test
    @Order(48)
    @DisplayName("cashInFailInvalidAccountNumber")
    void cashInFailInvalidAccountNumber() throws Exception {
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Step 2: Get minimum amount for cash in operation
        String amount = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());

        // Step 4: Attempt to generate UUID with wallet is disable - should fail
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), "546fgdgdfg5", HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, false);
    }


    /**
     * Test cash in failure with invalid account number format.
     * This method:
     * - Gets user's RIAL account number
     * - Gets minimum amount for cash in operation
     * - Disables wallet status for testing
     * - Attempts to generate UUID with invalid account number format
     * - Expects INPUT_PARAMETER_NOT_VALID error
     */
    @Test
    @Order(49)
    @DisplayName("cashInFailInvalidAccountNumber")
    void cashInFailDisableWallet() throws Exception {
        // Step 1: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);

        // Step 2: Get minimum amount for cash in operation
        String amount = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());

        // Step 3: Disable wallet status for testing
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountEntity.getWalletEntity().setStatus(WalletStatusEnum.DISABLE);
        walletRepositoryService.save(walletAccountEntity.getWalletEntity());
        walletRepositoryService.clearAllCache();

        // Step 4: Attempt to generate UUID with wallet is disable - should fail
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), "546fgdgdfg5", HttpStatus.OK, StatusRepositoryService.WALLET_IS_NOT_ACTIVE, false);
    }

    //




}
