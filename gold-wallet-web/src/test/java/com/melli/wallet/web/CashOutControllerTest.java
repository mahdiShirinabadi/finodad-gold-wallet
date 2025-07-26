package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.cash.CashOutResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashOutTrackResponse;
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

import java.util.Date;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: CashControllerTest
 * Author: Mahdi Shirinabadi
 * Date: 6/7/2025
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
    private WalletAccountService walletAccountService;
    @Autowired
    private WalletService walletService;
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

    @Test
    @Order(1)
    @DisplayName("Initiate...")
    void initial() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);
        log.info("start cleaning initial values in test DB");
        flyway.clean();
        flyway.migrate();
        cacheClearService.clearCache();
    }

    @Test
    @Order(10)
    @DisplayName("Channel login successfully")
    void login_success() throws Exception {
        log.info("start login_success test");
        BaseResponse<LoginResponse> response = login(mockMvc, USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.OK,
                StatusService.SUCCESSFUL, true);
        ACCESS_TOKEN = response.getData().getAccessTokenObject().getToken();
        REFRESH_TOKEN = response.getData().getRefreshTokenObject().getToken();
    }

    @Test
    @Order(15)
    @DisplayName("Create wallet for testing")
    void createWalletForTesting() throws Exception {
        log.info("start createWalletForTesting test");
        BaseResponse<CreateWalletResponse> response = createWallet(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        log.info("Wallet created successfully for testing");
    }

    @Test
    @Order(20)
    @DisplayName("cashOutSuccess")
    void cashOutSuccess() throws Exception {
        log.info("start cashOutSuccess test");
        
        // Get account number using the pattern from CashInControllerTest
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // First, charge the account with cashIn to have sufficient balance
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        
        // Enable cashIn if disabled
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        String cashInValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashOutSuccess");
        }
        
        // Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        
        // Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        log.info("Account charged successfully with amount: {}", cashInAmount);
        
        // Now get amount for cashout
        String cashOutAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        
        // Enable cashout if disabled
        String cashOutValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_OUT, accountNumber);
        if("false".equalsIgnoreCase(cashOutValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_OUT).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashOutSuccess");
        }
        
        // Generate UUID for cashout
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Perform cashout
        BaseResponse<CashOutResponse> response = cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, cashOutAmount, NATIONAL_CODE_CORRECT, accountNumber, VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertEquals(NATIONAL_CODE_CORRECT, response.getData().getNationalCode());
        Assert.assertEquals(accountNumber, response.getData().getWalletAccountNumber());
        Assert.assertNotNull(response.getData().getBalance());
        Assert.assertEquals(uniqueIdentifier, response.getData().getUniqueIdentifier());
        log.info("Cashout operation completed successfully");
    }

    @Test
    @Order(21)
    @DisplayName("cashOutFail-InvalidUniqueIdentifier")
    void cashOutFailInvalidUniqueIdentifier() throws Exception {
        log.info("start cashOutFailInvalidUniqueIdentifier test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, walletAccountObject.getAccountNumber());
        
        cashOut(mockMvc, ACCESS_TOKEN, "invalid_uuid", amount, NATIONAL_CODE_CORRECT, walletAccountObject.getAccountNumber(), VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusService.UUID_NOT_FOUND, false);
    }


    @Test
    @Order(23)
    @DisplayName("cashOutFail-InsufficientBalance")
    void cashOutFailInsufficientBalance() throws Exception {
        log.info("start cashOutFailInsufficientBalance test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // First, charge the account with cashIn
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        
        // Enable cashIn if disabled
        String cashInValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "true","test cashOutFailInsufficientBalance");
        }
        
        // Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        
        // Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        log.info("Account charged successfully with amount: {}", cashInAmount);
        
        // Now try to cashOut with amount + 1 (insufficient balance)
        String cashOutAmount = String.valueOf(Long.parseLong(cashInAmount) + 1);
        log.info("Attempting cashOut with amount: {} (original amount + 1)", cashOutAmount);
        
        // Generate UUID for cashout
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // This should fail due to insufficient balance
        cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, cashOutAmount, NATIONAL_CODE_CORRECT, accountNumber, VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusService.BALANCE_IS_NOT_ENOUGH, false);
    }

    @Test
    @Order(24)
    @DisplayName("cashOutFail-DuplicateRequest")
    void     cashOutFailDuplicateRequest() throws Exception {
        log.info("start cashOutFailDuplicateRequest test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, walletAccountObject.getAccountNumber());
        
        // Generate a valid UUID first
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObject.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // First cashout should succeed
        BaseResponse<CashOutResponse> firstResponse = cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, amount, NATIONAL_CODE_CORRECT, walletAccountObject.getAccountNumber(), VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(firstResponse.getData());
        
        // Second cashout with same UUID should fail
        cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, amount, NATIONAL_CODE_CORRECT, walletAccountObject.getAccountNumber(), VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusService.DUPLICATE_UUID, false);
    }

    @Test
    @Order(25)
    @DisplayName("inquiryCashOutSuccess")
    void inquiryCashOutSuccess() throws Exception {
        log.info("start inquiryCashOutSuccess test");
        
        // Get account number
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // First, charge the account with cashIn to have sufficient balance
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        
        // Enable cashIn if disabled
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        String cashInValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test inquiryCashOutSuccess");
        }
        
        // Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        
        // Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        log.info("Account charged successfully with amount: {}", cashInAmount);
        
        // Now perform a successful cashout
        String cashOutAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        
        // Enable cashout if disabled
        String cashOutValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_OUT, accountNumber);
        if("false".equalsIgnoreCase(cashOutValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_OUT).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test inquiryCashOutSuccess");
        }
        
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        BaseResponse<CashOutResponse> cashOutResponse = cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, cashOutAmount, NATIONAL_CODE_CORRECT, accountNumber, VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashOutResponse.getData());
        
        // Now inquiry the cashout
        BaseResponse<CashOutTrackResponse> response = inquiryCashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertEquals(NATIONAL_CODE_CORRECT, response.getData().getNationalCode());
        Assert.assertEquals(Long.parseLong(cashOutAmount), response.getData().getAmount());
        Assert.assertEquals(uniqueIdentifier, response.getData().getUniqueIdentifier());
        Assert.assertEquals(StatusService.SUCCESSFUL, response.getData().getResult());
        log.info("Cashout inquiry completed successfully");
    }

    @Test
    @Order(26)
    @DisplayName("inquiryCashOutFail-InvalidUniqueIdentifier")
    void inquiryCashOutFailInvalidUniqueIdentifier() throws Exception {
        log.info("start inquiryCashOutFailInvalidUniqueIdentifier test");
        inquiryCashOut(mockMvc, ACCESS_TOKEN, "invalid_uuid", HttpStatus.OK, StatusService.UUID_NOT_FOUND, false);
    }

    @Test
    @Order(27)
    @DisplayName("cashOutFail-InvalidIban")
    void cashOutFailInvalidIban() throws Exception {
        log.info("start cashOutFailInvalidIban test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // First, charge the account with cashIn
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        
        // Enable cashIn if disabled
        String cashInValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "true","test cashOutFailInvalidIban");
        }
        
        // Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        
        // Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        
        // Now try to cashOut with invalid IBAN
        String cashOutAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // This should fail due to invalid IBAN
        cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, cashOutAmount, NATIONAL_CODE_CORRECT, accountNumber, "INVALID_IBAN", VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
    }

    @Test
    @Order(28)
    @DisplayName("cashOutFail-AmountLessThanMinimum")
    void cashOutFailAmountLessThanMinimum() throws Exception {
        log.info("start cashOutFailAmountLessThanMinimum test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // First, charge the account with cashIn
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        
        // Enable cashIn if disabled
        String cashInValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "true","test cashOutFailAmountLessThanMinimum");
        }
        
        // Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        
        // Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        
        // Now try to cashOut with amount less than minimum
        String minAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        String cashOutAmount = String.valueOf(Long.parseLong(minAmount) - 1);
        log.info("Attempting cashOut with amount: {} (less than minimum: {})", cashOutAmount, minAmount);
        
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusService.AMOUNT_LESS_THAN_MIN, false);
    }

    @Test
    @Order(29)
    @DisplayName("cashOutFail-AmountBiggerThanMaximum")
    void cashOutFailAmountBiggerThanMaximum() throws Exception {
        log.info("start cashOutFailAmountBiggerThanMaximum test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // First, charge the account with cashIn
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        
        // Enable cashIn if disabled
        String cashInValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "true","test cashOutFailAmountBiggerThanMaximum");
        }
        
        // Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        
        // Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        
        // Now try to cashOut with amount bigger than maximum
        String maxAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_AMOUNT_CASH_OUT, accountNumber);
        String cashOutAmount = String.valueOf(Long.parseLong(maxAmount) + 1);
        log.info("Attempting cashOut with amount: {} (bigger than maximum: {})", cashOutAmount, maxAmount);
        
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusService.AMOUNT_BIGGER_THAN_MAX, false);
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
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "true","test cashOutFailInvalidSign");
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

    @Test
    @Order(31)
    @DisplayName("cashOutFail-CashOutDisabled")
    void cashOutFailCashOutDisabled() throws Exception {
        log.info("start cashOutFailCashOutDisabled test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        
        // Disable cashOut
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
            limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_OUT).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
            walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
            "false","test cashOutFailCashOutDisabled");
        
        String cashOutAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        
        // This should fail due to cashOut being disabled
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusService.ACCOUNT_DONT_PERMISSION_FOR_CASH_OUT, false);
        
        // Re-enable cashOut for other tests
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
            limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_OUT).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
            walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
            "true","re-enable cashOut after test");
    }

    @Test
    @Order(32)
    @DisplayName("cashOutFail-InvalidNationalCode")
    void cashOutFailInvalidNationalCode() throws Exception {
        log.info("start cashOutFailInvalidNationalCode test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // First, charge the account with cashIn
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        
        // Enable cashIn if disabled
        String cashInValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "true","test cashOutFailInvalidNationalCode");
        }
        
        // Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        
        // Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        
        // Now try to cashOut with invalid national code
        String cashOutAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // This should fail due to invalid national code
        cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, cashOutAmount, NATIONAL_CODE_INCORRECT, accountNumber, VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
    }

    @Test
    @Order(33)
    @DisplayName("cashOutFail-InvalidAccountNumber")
    void cashOutFailInvalidAccountNumber() throws Exception {
        log.info("start cashOutFailInvalidAccountNumber test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // First, charge the account with cashIn
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, accountNumber);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        
        // Enable cashIn if disabled
        String cashInValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, accountNumber);
        if("false".equalsIgnoreCase(cashInValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "true","test cashOutFailInvalidAccountNumber");
        }
        
        // Generate UUID for cashIn
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        
        // Perform cashIn to charge the account
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, accountNumber, "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        
        // Now try to cashOut with invalid account number
        String cashOutAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, accountNumber);
        BaseResponse<UuidResponse> uuidResponse = generateCashOutUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashOutAmount, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // This should fail due to invalid account number
        cashOut(mockMvc, ACCESS_TOKEN, uniqueIdentifier, cashOutAmount, NATIONAL_CODE_CORRECT, "INVALID_ACCOUNT_NUMBER", VALID_IBAN, VALID_SIGN, ADDITIONAL_DATA, HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
    }

}
