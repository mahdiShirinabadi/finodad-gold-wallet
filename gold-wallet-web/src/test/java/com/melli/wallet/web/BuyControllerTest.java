package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.domain.dto.AggregationPurchaseDTO;
import com.melli.wallet.domain.enumaration.TransactionTypeEnum;
import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.util.date.DateUtils;
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
 * Class Name: BuyControllerTest
 * Author: Mahdi Shirinabadi
 * Date: 4/7/2025
 * 
 * This test class contains comprehensive end-to-end tests for Buy operations.
 * It tests the complete flow from wallet creation to buy execution and validation.
 * 
 * Test Coverage:
 * - Wallet creation and setup
 * - Buy UUID generation (success and failure scenarios)
 * - Buy execution (success and failure scenarios)
 * - Buy direct operations (success and failure scenarios)
 * - Amount validation (minimum, maximum limits)
 * - Currency validation
 * - Daily and monthly limitation tests
 * - Merchant balance validation
 * - Commission currency validation
 * - UUID validation and mismatch scenarios
 */
@Log4j2
@DisplayName("BuyControllerTest End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BuyControllerTest extends WalletApplicationTests {


    private static final String NATIONAL_CODE_CORRECT = "0077847660";
    private static final String MOBILE_CORRECT = "09124162337";
    private static final String CURRENCY_RIAL = "RIAL";
    private static final String CURRENCY_GOLD = "GOLD";

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
    private WalletTypeService walletTypeService;
    @Autowired
    private LimitationGeneralCustomService limitationGeneralCustomService;
    @Autowired
    private ChannelService channelService;
    @Autowired
    private Flyway flyway;
    @Autowired
    private CacheClearService cacheClearService;
    @Autowired
    private WalletLevelService walletLevelService;
    @Autowired
    private WalletAccountCurrencyService walletAccountCurrencyService;
    @Autowired
    private WalletBuyLimitationService walletBuyLimitationService;
    @Autowired
    private RequestService requestService;
    @Autowired
    private LimitationGeneralService limitationGeneralService;


    /**
     * Initial setup method that runs before all tests.
     * This method:
     * - Sets up MockMvc for testing
     * - Cleans and migrates the database
     * - Clears all caches
     * - Creates wallet for channel testing
     * - Sets up wallet accounts for RIAL and GOLD currencies
     */
    @Test
    @Order(2)
    @DisplayName("Initiate cache...")
    void initial() throws Exception {
        // Step 1: Setup MockMvc for testing with security
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);
        
        // Step 2: Clean and migrate database
        log.info("start cleaning initial values in test DB for purchase");
        flyway.clean();
        flyway.migrate();
        log.info("start cleaning initial values in test DB for purchase");
        
        // Step 3: Clear all caches
        cacheClearService.clearCache();

        // Step 4: Create wallet for channel testing
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

        walletAccountService.createAccount(List.of(WalletAccountCurrencyService.RIAL, WalletAccountCurrencyService.GOLD),
                walletEntity, List.of(WalletAccountTypeService.WAGE), channelEntity);

    }

    /**
     * Test successful channel login.
     * This method:
     * - Performs login with correct credentials
     * - Stores the access token for subsequent tests
     */
    @Test
    @Order(10)
    @DisplayName("channel login successfully")
    void login_success() throws Exception {
        log.info("start login_success test");
        BaseResponse<LoginResponse> response = login(mockMvc, USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.OK,
                StatusService.SUCCESSFUL, true);
        ACCESS_TOKEN = response.getData().getAccessTokenObject().getToken();
        REFRESH_TOKEN = response.getData().getRefreshTokenObject().getToken();
    }

    /**
     * Test successful wallet creation.
     * This method:
     * - Creates a wallet for the test user
     * - Validates the wallet creation response
     */
    @Test
    @Order(20)
    @DisplayName("create wallet- success")
    void createWalletSuccess() throws Exception {
        BaseResponse<CreateWalletResponse> response = createWallet(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        WalletEntity walletEntity = walletService.findById(Long.parseLong(response.getData().getWalletId()));
        if (!walletEntity.getNationalCode().equalsIgnoreCase(NATIONAL_CODE_CORRECT)) {
            log.error("wallet create not same with national code ({})", NATIONAL_CODE_CORRECT);
            throw new Exception("wallet create not same with national code ({})");
        }
    }

    /**
     * Test buy UUID generation failure when quantity is less than minimum.
     * This method:
     * - Gets user's GOLD account number
     * - Attempts to generate buy UUID with quantity below minimum
     * - Expects QUANTITY_LESS_THAN_MIN error
     */
    @Test
    @Order(25)
    @DisplayName("get uuid buy fail - less Than min quantity")
    void buyLessThanMinQuantityFail() throws Exception {
        // Step 1: Define test parameters
        String merchantId = "1";
        String currency = "GOLD";
        String price="10000000";

        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        
        // Step 3: Get minimum quantity and calculate quantity below minimum
        String minAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_QUANTITY_BUY, walletAccountObjectOptional.getAccountNumber());
        String quantity = String.valueOf(new BigDecimal(minAmount).subtract(new BigDecimal("0.00001")));
        
        // Step 4: Increase merchant balance for testing
        increaseMerchantBalance("1",WalletAccountCurrencyService.GOLD,"1111111111");
        
        // Step 5: Attempt to generate buy UUID with quantity below minimum
        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.QUANTITY_LESS_THAN_MIN, false, merchantId, quantity, currency);
    }


    /**
     * Test buy UUID generation failure when quantity exceeds maximum.
     * This method:
     * - Gets user's RIAL account number
     * - Gets maximum quantity and calculates quantity above maximum
     * - Increases merchant balance for testing
     * - Attempts to generate buy UUID with quantity above maximum
     * - Expects QUANTITY_BIGGER_THAN_MAX error
     */
    @Test
    @Order(41)
    @DisplayName("get uuid buy fail- bigger than maxQuantity")
    void buyFailMaxPrice() throws Exception {
        // Step 1: Define test parameters
        String merchantId = "1";
        String currency = "GOLD";
        String price="10000000";

        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        
        // Step 3: Get maximum quantity and calculate quantity above maximum
        String minAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_BUY, walletAccountObjectOptional.getAccountNumber());
        String quantity = String.valueOf(new BigDecimal(minAmount).add(new BigDecimal("0.00001")));
        
        // Step 4: Increase merchant balance for testing
        increaseMerchantBalance("1",WalletAccountCurrencyService.GOLD,"1111111111");
        
        // Step 5: Attempt to generate buy UUID with quantity above maximum
        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.QUANTITY_BIGGER_THAN_MAX, false, merchantId, quantity, currency);
    }


    /**
     * Test buy failure with invalid amount format.
     * This method:
     * - Gets user's RIAL account number
     * - Attempts to generate buy UUID with invalid amount format
     * - Expects INPUT_PARAMETER_NOT_VALID error
     */
    @Test
    @Order(43)
    @DisplayName("buy fail- invalid amount")
    void buyFailInvalidPrice() throws Exception {
        // Step 1: Define test parameters
        String merchantId = "1";
        String quantity = "0.001";
        String currency = "GOLD";
        String invalidAmount = "123edfed";
        
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        
        // Step 3: Attempt to generate buy UUID with invalid amount format
        generateBuyUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(invalidAmount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false, merchantId, quantity, currency);
    }

    /**
     * Test successful buy UUID generation.
     * This method:
     * - Gets user's RIAL account number
     * - Increases merchant balance for testing
     * - Generates buy UUID successfully
     * - Validates the UUID generation response
     */
    @Test
    @Order(44)
    @DisplayName("generateBuyUuid-Success")
    void generateBuyUuidSuccess() throws Exception {
        // Step 1: Define test parameters
        String price = "100000";
        String merchantId = "1";
        String quantity = "0.001";
        String currency = "GOLD";
        
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        
        // Step 3: Generate buy UUID successfully
        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, quantity, currency);
    }

    /**
     * Test buy failure when amount differs from UUID amount.
     * This method:
     * - Gets user's RIAL account number
     * - Generates buy UUID with specific amount
     * - Attempts buy operation with different amount
     * - Expects PRICE_NOT_SAME_WITH_UUID error
     */
    @Test
    @Order(45)
    @DisplayName("amountUuidDifferentFromPurchaseAmount-fail")
    void amountUuidDifferentFromPurchaseAmountFail() throws Exception {
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "1.07";
        String merchantId = "1";
        String commission = "2000";
        String commissionType = CURRENCY_RIAL;
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "differentAmount";
        
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        
        // Step 3: Generate buy UUID with specific amount
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, "0.001", currency);
        
        // Step 4: Attempt buy operation with different amount - should fail
        buy(mockMvc, ACCESS_TOKEN, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price) + 1), commissionType, commission, NATIONAL_CODE_CORRECT, currency
                , merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.PRICE_NOT_SAME_WITH_UUID, false);
    }

    /**
     * Test buy failure with invalid currency.
     * This method:
     * - Gets user's RIAL account number
     * - Generates buy UUID with valid currency
     * - Attempts buy operation with invalid currency
     * - Expects WALLET_ACCOUNT_CURRENCY_NOT_FOUND error
     */
    @Test
    @Order(46)
    @DisplayName("currencyNotValid-fail")
    void currencyNotValidFail() throws Exception {
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "1.07";
        String merchantId = "1";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = "GOLD";
        String invalidCurrency = "SILVER";
        String sign = "";
        String additionalData = "differentAmount";
        
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        
        // Step 3: Generate buy UUID with valid currency
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, "0.001", currency);
        
        // Step 4: Attempt buy operation with invalid currency - should fail
        BaseResponse<PurchaseResponse> response = buy(mockMvc, ACCESS_TOKEN, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, invalidCurrency
                , merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);
        Assert.assertSame(StatusService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, response.getErrorDetail().getCode());
    }

    /**
     * Test successful buy operation.
     * This method:
     * - Clears buy limitations for testing
     * - Gets user's RIAL account number
     * - Increases merchant balance for testing
     * - Generates buy UUID successfully
     * - Performs buy operation successfully
     * - Validates the buy response
     */
    @Test
    @Order(47)
    @DisplayName("buy-success")
    void buySuccess() throws Exception {
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "1.07";
        
        // Step 2: Clear buy limitations for testing
        walletBuyLimitationService.deleteAll();
        
        // Step 3: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        
        // Step 4: Update balance merchant wallet-account
        increaseMerchantBalance("1.07", WalletAccountCurrencyService.GOLD, "1111111111");

        // Step 5: Enable cash in permission if disabled
        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if ("false".equalsIgnoreCase(value)) {
            WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true", "test buySuccess");
        }

        // Step 6: Generate UUID for cash in operation
        BaseResponse<UuidResponse> uniqueIdentifierCashIn = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, price, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifierCashIn);
        
        // Step 7: Perform cash in operation to charge account
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifierCashIn.getData().getUniqueIdentifier(), String.valueOf(new Date().getTime()), price, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);

        // Step 8: Define buy operation parameters
        String merchantId = "1";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "differentAmount";
        
        // Step 9: Generate buy UUID successfully
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, "0.001", currency);
        
        // Step 10: Perform buy operation successfully
        buy(mockMvc, ACCESS_TOKEN, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, currency
                , merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.SUCCESSFUL, true);

    }



    /**
     * Test buy failure when daily count limitation is exceeded.
     * This method:
     * - Clears buy limitations for testing
     * - Gets user's RIAL account number
     * - Temporarily sets MAX_DAILY_COUNT_BUY to current count
     * - Attempts to generate buy UUID
     * - Expects BUY_EXCEEDED_COUNT_DAILY_LIMITATION error
     * - Restores original MAX_DAILY_COUNT_BUY limit
     */
    @Test
    @Order(48)
    @DisplayName("buyDailyLimitationFail-success")
    void buyDailyLimitationFail() throws Exception {
        // Step 1: Define test parameters
        String price = "100000";
        
        // Step 2: Clear buy limitations for testing
        walletBuyLimitationService.deleteAll();
        
        // Step 3: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);

        // Step 4: Get current buy statistics and limitation values
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String valueMaxDailyCount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_BUY, walletAccountObjectOptional.getAccountNumber());
        String valueMaxDailyPrice = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_BUY, walletAccountObjectOptional.getAccountNumber());
        AggregationPurchaseDTO aggregationPurchaseDTO = requestService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccountEntity.getId()}, TransactionTypeEnum.BUY.name(), new Date(), new Date());

        // Step 5: Temporarily set MAX_DAILY_COUNT_BUY to current count
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_DAILY_COUNT_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                aggregationPurchaseDTO.getCountRecord(), "change MAX_DAILY_COUNT_BUY to generate uuid");

        // Step 6: Attempt to generate buy UUID - should fail due to daily count limitation
        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.BUY_EXCEEDED_COUNT_DAILY_LIMITATION, false, "1", "0.001", "GOLD");

        // Step 7: Restore original MAX_DAILY_COUNT_BUY limit
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_DAILY_COUNT_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                valueMaxDailyCount, "change MAX_DAILY_COUNT_BUY");

        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_DAILY_QUANTITY_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                aggregationPurchaseDTO.getSumQuantity(), "change MAX_DAILY_QUANTITY_BUY");

        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.BUY_EXCEEDED_QUANTITY_DAILY_LIMITATION, false, "1", "0.001", "GOLD");

        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_DAILY_QUANTITY_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                valueMaxDailyPrice, "change MAX_DAILY_QUANTITY_BUY");

    }


    /**
     * Test buy operation failure when monthly limitations are exceeded.
     * This method:
     * - Clears buy limitation cache
     * - Gets user's RIAL account number
     * - Retrieves current monthly limitation values
     * - Calculates current monthly usage
     * - Tests monthly count limitation failure
     * - Tests monthly quantity limitation failure
     * - Restores original limitation values
     */
    @Test
    @Order(49)
    @DisplayName("buyMonthlyLimitationFail-success")
    void buyMonthlyLimitationFail() throws Exception {
        // Step 1: Define test parameters
        String price = "100000";
        // Step 2: Clear buy limitation cache to start fresh
        walletBuyLimitationService.deleteAll();
        // Step 3: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        // Step 4: Update balance merchant wallet-account
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        // Step 5: Get current monthly limitation values for backup
        String valueMaxMonthlyCount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_COUNT_BUY, walletAccountObjectOptional.getAccountNumber());
        String valueMaxMonthlyQuantity = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_QUANTITY_BUY, walletAccountObjectOptional.getAccountNumber());
        // Step 6: Calculate current monthly usage period
        Date fromDate = DateUtils.findFirstDateOfPersianMonth(new Date());
        Date untilDate = new Date();
        log.info("found monthly fromTime ({}), until ({})", fromDate, untilDate);
        // Step 7: Get current monthly usage statistics
        AggregationPurchaseDTO aggregationPurchaseDTO = requestService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccountEntity.getId()}, TransactionTypeEnum.BUY.name(), fromDate, untilDate);
        // Step 8: Set monthly count limitation to current usage to trigger failure
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_COUNT_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                aggregationPurchaseDTO.getCountRecord(), "change MAX_MONTHLY_COUNT_BUY");
        // Step 9: Test monthly count limitation failure
        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.BUY_EXCEEDED_COUNT_MONTHLY_LIMITATION, false, "1", "0.001", "GOLD");
        // Step 10: Restore original monthly count limitation
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_COUNT_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                valueMaxMonthlyCount, "change MAX_MONTHLY_COUNT_BUY");
        // Step 11: Set monthly quantity limitation to current usage to trigger failure
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_QUANTITY_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                aggregationPurchaseDTO.getSumQuantity(), "change MAX_MONTHLY_COUNT_BUY");
        // Step 12: Test monthly quantity limitation failure
        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.BUY_EXCEEDED_QUANTITY_MONTHLY_LIMITATION, false, "1", "0.001", "GOLD");
        // Step 13: Restore original monthly quantity limitation
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_QUANTITY_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                valueMaxMonthlyQuantity, "change MAX_MONTHLY_QUANTITY_BUY");
    }


    /**
     * Test successful buy direct operation.
     * This method:
     * - Gets user's RIAL account number
     * - Increases merchant balance for testing
     * - Performs buy direct operation successfully
     * - Validates the buy direct response
     */
    @Test
    @Order(50)
    @DisplayName("buyDirect-success")
    void buyDirectSuccess() throws Exception {
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "1.07";
        // Step 2: Clear buy limitation cache to start fresh
        walletBuyLimitationService.deleteAll();
        // Step 3: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        // Step 4: Update balance merchant wallet-account
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountService.findByWallet(walletMerchantEntity);
        // Step 5: Find gold wallet account
        long id = 0;
        try {
            id = walletAccountCurrencyService.findCurrency(WalletAccountCurrencyService.GOLD).getId();
        } catch (InternalServiceException ex) {
            log.error("walletAccountCurrencyEntityGold not found", ex);
        }
        // Step 6: Enable cash-in if disabled
        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if ("false".equalsIgnoreCase(value)) {
            WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true", "test cashInFailMinAmount");
        }
        // Step 7: Increase merchant GOLD balance
        long finalId = id;
        WalletAccountEntity walletAccountEntity = walletAccountEntityList.stream().filter(
                x -> x.getWalletAccountCurrencyEntity().getId() == finalId).findFirst().orElse(null);
        walletAccountService.increaseBalance(walletAccountEntity.getId(), new BigDecimal("1.07"));
        // Step 8: Generate cash-in UUID and perform cash-in
        BaseResponse<UuidResponse> uniqueIdentifierCashIn = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, price, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifierCashIn);
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifierCashIn.getData().getUniqueIdentifier(), String.valueOf(new Date().getTime()), price, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        // Step 9: Define buy direct parameters
        String merchantId = "1";
        String refNumber = new Date() + "";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "differentAmount";
        // Step 10: Generate buy UUID
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, "0.001", currency);
        // Step 11: Perform buy direct operation
        buyDirect(mockMvc, refNumber, ACCESS_TOKEN, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, currency
                , merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.SUCCESSFUL, true);
    }

    //

    /**
     * Test buy direct failure with invalid commission currency.
     * This method:
     * - Gets user's RIAL account number
     * - Increases merchant balance for testing
     * - Attempts buy direct with invalid commission currency
     * - Expects COMMISSION_CURRENCY_NOT_VALID error
     */
    @Test
    @Order(51)
    @DisplayName("buyDirectFail-InvalidCommissionCurrency")
    void buyDirectFailInvalidCommissionCurrency() throws Exception {
        log.info("start buyDirectFailInvalidCommissionCurrency test");
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "1.07";
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        // Step 3: Charge account first
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String cashInValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(cashInValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "true","test buyDirectFailInvalidCommissionCurrency");
        }
        // Step 4: Perform cash-in operation
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        // Step 5: Update merchant balance
        increaseMerchantBalance("1.07", WalletAccountCurrencyService.GOLD, "1111111111");
        // Step 6: Define buy direct parameters with invalid commission currency
        String merchantId = "1";
        String commission = "2000";
        String commissionType = "GOLD"; // Invalid commission currency
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "test commission currency error";
        // Step 7: Generate UUID for buyDirect
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, "0.001", currency);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 8: Test with GOLD commission currency (should fail)
        BaseResponse<PurchaseResponse> response = buyDirect(mockMvc, refNumber, ACCESS_TOKEN, uniqueIdentifier, quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, currency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.COMMISSION_CURRENCY_NOT_VALID, false);
        Assert.assertSame(StatusService.COMMISSION_CURRENCY_NOT_VALID, response.getErrorDetail().getCode());
    }

    /**
     * Test buy direct failure with invalid unique identifier.
     * This method:
     * - Gets user's RIAL account number
     * - Increases merchant balance for testing
     * - Attempts buy direct with invalid UUID
     * - Expects UUID_NOT_FOUND error
     */
    @Test
    @Order(52)
    @DisplayName("buyDirectFail-InvalidUniqueIdentifier")
    void buyDirectFailInvalidUniqueIdentifier() throws Exception {
        log.info("start buyDirectFailInvalidUniqueIdentifier test");
        // Step 1: Define test parameters
        String quantity = "1.07";
        String price = "100000";
        String refNumber = new Date().getTime() + "";
        String merchantId = "1";
        String commission = "2000";
        String commissionType = "RIAL";
        String invalidUuid = "invalid_uuid";
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String sign = "";
        String additionalData = "test invalid uuid";
        // Step 3: Test with invalid UUID
        buyDirect(mockMvc, refNumber, ACCESS_TOKEN, invalidUuid, quantity, price, commissionType, commission, NATIONAL_CODE_CORRECT, CURRENCY_GOLD, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.UUID_NOT_FOUND, false);
    }

    /**
     * Test buy direct failure when merchant balance is insufficient.
     * This method:
     * - Gets user's RIAL account number
     * - Sets merchant balance to zero
     * - Attempts buy direct operation
     * - Expects MERCHANT_BALANCE_NOT_ENOUGH error
     */
    @Test
    @Order(53)
    @DisplayName("buyDirectFail-MerchantBalanceNotEnough")
    void buyDirectFailMerchantBalanceNotEnough() throws Exception {
        log.info("start buyDirectFailMerchantBalanceNotEnough test");
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "1.07";
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        // Step 3: Charge account first
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String cashInValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(cashInValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "true","test buyDirectFailMerchantBalanceNotEnough");
        }
        // Step 4: Perform cash-in operation
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        // Step 5: Define buy direct parameters
        String merchantId = "1";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "test merchant balance not enough";
        refNumber = new Date().getTime() + "";
        // Step 6: Generate buy UUID
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, quantity, currency);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 7: Set merchant balance to zero to simulate insufficient balance
        setMerchantBalanceToZero(WalletAccountCurrencyService.GOLD,"1111111111");
        // Step 8: Test with insufficient merchant balance
        BaseResponse<PurchaseResponse> response = buyDirect(mockMvc, refNumber, ACCESS_TOKEN, uniqueIdentifier, quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT,
                currency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.MERCHANT_BALANCE_NOT_ENOUGH, false);
        Assert.assertSame(StatusService.MERCHANT_BALANCE_NOT_ENOUGH, response.getErrorDetail().getCode());
    }

    /**
     * Test buy direct failure with invalid merchant ID.
     * This method:
     * - Gets user's RIAL account number
     * - Increases merchant balance for testing
     * - Attempts buy direct with invalid merchant ID
     * - Expects MERCHANT_NOT_FOUND error
     */
    @Test
    @Order(54)
    @DisplayName("buyDirectFail-InvalidMerchantId")
    void buyDirectFailInvalidMerchantId() throws Exception {
        log.info("start buyDirectFailInvalidMerchantId test");
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "1.07";
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        // Step 3: Charge account first
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String cashInValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(cashInValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "true","test buyDirectFailInvalidMerchantId");
        }
        // Step 4: Perform cash-in operation
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        // Step 5: Update merchant balance
        increaseMerchantBalance("1.07", WalletAccountCurrencyService.GOLD, "1111111111");
        // Step 6: Define buy direct parameters with invalid merchant ID
        String merchantId = "1";
        String invalidMerchantId = "999"; // Invalid merchant ID
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "test invalid merchant id";
        // Step 7: Generate buy UUID
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, "0.001", currency);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 8: Test with invalid merchant ID
        BaseResponse<PurchaseResponse> response = buyDirect(mockMvc, refNumber, ACCESS_TOKEN, uniqueIdentifier, quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, currency, invalidMerchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.MERCHANT_IS_NOT_EXIST, false);
        Assert.assertSame(StatusService.MERCHANT_IS_NOT_EXIST, response.getErrorDetail().getCode());
    }

    /**
     * Test buy direct failure with invalid currency.
     * This method:
     * - Gets user's RIAL account number
     * - Increases merchant balance for testing
     * - Attempts buy direct with invalid currency
     * - Expects WALLET_ACCOUNT_CURRENCY_NOT_FOUND error
     */
    @Test
    @Order(55)
    @DisplayName("buyDirectFail-InvalidCurrency")
    void buyDirectFailInvalidCurrency() throws Exception {
        log.info("start buyDirectFailInvalidCurrency test");
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "1.07";
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        // Step 3: Charge account first
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String cashInValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(cashInValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "true","test buyDirectFailInvalidCurrency");
        }
        // Step 4: Perform cash-in operation
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        // Step 5: Update merchant balance
        increaseMerchantBalance("1.07", WalletAccountCurrencyService.GOLD, "1111111111");
        // Step 6: Define buy direct parameters with invalid currency
        String merchantId = "1";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = "GOLD";
        String invalidCurrency = "INVALID_CURRENCY"; // Invalid currency
        String sign = "";
        String additionalData = "test invalid currency";
        // Step 7: Generate buy UUID
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, "0.001", currency);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 8: Test with invalid currency
        BaseResponse<PurchaseResponse> response = buyDirect(mockMvc, refNumber, ACCESS_TOKEN, uniqueIdentifier, quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, invalidCurrency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);
        Assert.assertSame(StatusService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, response.getErrorDetail().getCode());
    }

    /*@Test
    @Order(56)
    @DisplayName("buyDirectFail-InvalidSign")
    void buyDirectFailInvalidSign() throws Exception {
        log.info("start buyDirectFailInvalidSign test");
        String price = "100000";
        String quantity = "1.07";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        
        // Charge account first
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String cashInValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(cashInValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "true","test buyDirectFailInvalidSign");
        }
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());

        // Update merchant balance
        increaseMerchantBalance(quantity, WalletAccountCurrencyService.GOLD, "1111111111");

        String merchantId = "1";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = CURRENCY_GOLD;
        String invalidSign = "invalid_sign";
        String additionalData = "test invalid sign";
        
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, quantity, currency);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Test with invalid sign
        BaseResponse<PurchaseResponse> response = buyDirect(mockMvc, refNumber, ACCESS_TOKEN, uniqueIdentifier, quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, currency, merchantId, walletAccountObjectOptional.getAccountNumber(), invalidSign, additionalData, HttpStatus.OK, StatusService.INVALID_SIGN, false);
        Assert.assertSame(StatusService.INVALID_SIGN, response.getErrorDetail().getCode());
    }*/


    /**
     * Helper method to increase merchant balance for testing.
     * This method:
     * - Finds merchant by national code
     * - Increases balance by specified amount and currency
     * - Used for setting up test scenarios
     */
    private void increaseMerchantBalance(String val, String currency, String merchantNationalCode) {
        // Step 1: Find merchant wallet entity
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId(merchantNationalCode, walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountService.findByWallet(walletMerchantEntity);
        // Step 2: Find currency wallet account
        long id = 0;
        try {
            id = walletAccountCurrencyService.findCurrency(currency).getId();
        } catch (InternalServiceException ex) {
            log.error("walletAccountCurrencyEntity({}) not found", currency, ex);
        }
        // Step 3: Find the specific currency account and increase balance
        long finalId = id;
        WalletAccountEntity walletAccountEntity = walletAccountEntityList.stream().filter(
                x -> x.getWalletAccountCurrencyEntity().getId() == finalId).findFirst().orElse(null);
        walletAccountService.increaseBalance(walletAccountEntity.getId(), new BigDecimal(val));
    }

    /**
     * Helper method to decrease merchant balance for testing.
     * This method:
     * - Finds merchant by national code
     * - Decreases balance by specified amount and currency
     * - Used for setting up test scenarios
     */
    private void decreaseMerchantBalance(String val, String currency, String merchantNationalCode) {
        // Step 1: Find merchant wallet entity
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId(merchantNationalCode, walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountService.findByWallet(walletMerchantEntity);
        // Step 2: Find currency wallet account
        long id = 0;
        try {
            id = walletAccountCurrencyService.findCurrency(currency).getId();
        } catch (InternalServiceException ex) {
            log.error("walletAccountCurrencyEntity({}) not found", currency, ex);
        }
        // Step 3: Find the specific currency account and decrease balance
        long finalId = id;
        WalletAccountEntity walletAccountEntity = walletAccountEntityList.stream().filter(
                x -> x.getWalletAccountCurrencyEntity().getId() == finalId).findFirst().orElse(null);
        walletAccountService.decreaseBalance(walletAccountEntity.getId(), new BigDecimal(val));
    }


    /**
     * Helper method to set merchant balance to zero for testing.
     * This method:
     * - Finds merchant by national code
     * - Sets balance to zero for specified currency
     * - Used for setting up test scenarios
     */
    private void setMerchantBalanceToZero(String currency, String merchantNationalCode) {
        // Step 1: Find merchant wallet entity
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId(merchantNationalCode, walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountService.findByWallet(walletMerchantEntity);
        // Step 2: Find currency wallet account
        long id = 0;
        try {
            id = walletAccountCurrencyService.findCurrency(currency).getId();
        } catch (InternalServiceException ex) {
            log.error("walletAccountCurrencyEntity({}) not found", currency, ex);
        }
        // Step 3: Find the specific currency account and set balance to zero
        long finalId = id;
        WalletAccountEntity walletAccountEntity = walletAccountEntityList.stream().filter(
                x -> x.getWalletAccountCurrencyEntity().getId() == finalId).findFirst().orElse(null);
        walletAccountService.decreaseBalance(walletAccountEntity.getId(), walletAccountService.getBalance(walletAccountEntity.getId()));
    }
}
