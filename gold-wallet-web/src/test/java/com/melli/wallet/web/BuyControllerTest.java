package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.sync.ResourceSyncService;
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
import com.melli.wallet.service.operation.WalletBuyLimitationOperationService;
import com.melli.wallet.service.repository.*;
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
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: BuyControllerTest
 * Author: Mahdi Shirinabadi
 * Date: 4/7/2025
 * This test class contains comprehensive end-to-end tests for Buy operations.
 * It tests the complete flow from wallet creation to buy execution and validation.
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
    private static final String CURRENCY_GOLD = "GOLD";

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
    private Flyway flyway;
    @Autowired
    private CacheClearService cacheClearService;
    @Autowired
    private WalletLevelRepositoryService walletLevelRepositoryService;
    @Autowired
    private WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService;
    @Autowired
    private WalletBuyLimitationOperationService walletBuyLimitationOperationService;
    @Autowired
    private RequestRepositoryService requestRepositoryService;
    @Autowired
    private LimitationGeneralService limitationGeneralService;
    @Autowired
    private ResourceSyncService resourceSyncService;


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
        resourceSyncService.syncResourcesOnStartup();
        log.info("start cleaning initial values in test DB for purchase");
        
        // Step 3: Clear all caches
        cacheClearService.clearCache();

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
                StatusRepositoryService.SUCCESSFUL, true);
        accessToken = response.getData().getAccessTokenObject().getToken();
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
        BaseResponse<CreateWalletResponse> response = createWallet(mockMvc, accessToken, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        WalletEntity walletEntity = walletRepositoryService.findById(Long.parseLong(response.getData().getWalletId()));
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        
        // Step 3: Get minimum quantity and calculate quantity below minimum
        String minAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_QUANTITY_BUY, walletAccountObjectOptional.getAccountNumber());
        String quantity = String.valueOf(new BigDecimal(minAmount).subtract(new BigDecimal("0.00001")));
        
        // Step 4: Increase merchant balance for testing
        increaseMerchantBalance("1", WalletAccountCurrencyRepositoryService.GOLD,"1111111111");
        
        // Step 5: Attempt to generate buy UUID with quantity below minimum
        generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.QUANTITY_LESS_THAN_MIN, false, merchantId, quantity, currency);
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        
        // Step 3: Get maximum quantity and calculate quantity above maximum
        String minAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_BUY, walletAccountObjectOptional.getAccountNumber());
        String quantity = String.valueOf(new BigDecimal(minAmount).add(new BigDecimal("0.00001")));
        
        // Step 4: Increase merchant balance for testing
        increaseMerchantBalance("1", WalletAccountCurrencyRepositoryService.GOLD,"1111111111");
        
        // Step 5: Attempt to generate buy UUID with quantity above maximum
        generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.QUANTITY_BIGGER_THAN_MAX, false, merchantId, quantity, currency);
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        
        // Step 3: Attempt to generate buy UUID with invalid amount format
       BaseResponse<UuidResponse> response = generateBuyUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, (invalidAmount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false, merchantId, quantity, currency);
       Assert.assertSame(StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, response.getErrorDetail().getCode());
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        
        // Step 3: Generate buy UUID successfully
        generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, quantity, currency);
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
        String commissionType = WalletAccountCurrencyRepositoryService.RIAL;
        String currency = WalletAccountCurrencyRepositoryService.GOLD;
        String sign = "";
        String additionalData = "differentAmount";
        
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, currency);
        
        // Step 3: Generate buy UUID with specific amount
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, "0.001", currency);
        
        // Step 4: Attempt buy operation with different amount - should fail
        BaseResponse<PurchaseResponse> response = buy(mockMvc, accessToken, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price) + 1), commissionType, commission, NATIONAL_CODE_CORRECT, currency
                , merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusRepositoryService.PRICE_NOT_SAME_WITH_UUID, false);
        Assert.assertSame(StatusRepositoryService.PRICE_NOT_SAME_WITH_UUID, response.getErrorDetail().getCode());
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        
        // Step 3: Generate buy UUID with valid currency
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, "0.001", currency);
        
        // Step 4: Attempt buy operation with invalid currency - should fail
        BaseResponse<PurchaseResponse> response = buy(mockMvc, accessToken, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, invalidCurrency
                , merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);
        Assert.assertSame(StatusRepositoryService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, response.getErrorDetail().getCode());
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
        walletBuyLimitationOperationService.deleteAll();
        
        // Step 3: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        WalletAccountObject walletAccountObjectOptionalRial = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);

        // Step 4: Update balance merchant wallet-account
        increaseMerchantBalance("1.07", WalletAccountCurrencyRepositoryService.GOLD, "1111111111");

        // Step 5: Enable cash in permission if disabled
        String value = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if ("false".equalsIgnoreCase(value)) {
            WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true");
        }

        String valueRial = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptionalRial.getAccountNumber());
        if ("false".equalsIgnoreCase(valueRial)) {
            WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptionalRial.getAccountNumber());
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true");
        }

        // Step 6: Generate UUID for cash in operation
        BaseResponse<UuidResponse> uniqueIdentifierCashIn = generateCashInUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, price, walletAccountObjectOptionalRial.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifierCashIn);
        
        // Step 7: Perform cash in operation to charge account
        cashIn(mockMvc, accessToken, uniqueIdentifierCashIn.getData().getUniqueIdentifier(), String.valueOf(new Date().getTime()), price, NATIONAL_CODE_CORRECT, walletAccountObjectOptionalRial.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        // Step 8: Define buy operation parameters
        String merchantId = "1";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "differentAmount";
        
        // Step 9: Generate buy UUID successfully
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, "0.001", currency);
        
        // Step 10: Perform buy operation successfully
        buy(mockMvc, accessToken, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, currency
                , merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

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
    @DisplayName("buyDailyLimitationFailOnGenerateUuid-fail")
    void buyDailyLimitationFailOnGenerateUuid() throws Exception {
        // Step 1: Define test parameters
        String price = "100000";
        
        // Step 2: Clear buy limitations for testing
        walletBuyLimitationOperationService.deleteAll();
        
        // Step 3: Get user's RIAL account number
        WalletAccountObject walletAccountCurrencyObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);

        // Step 4: Get current buy statistics and limitation values
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountCurrencyObjectOptional.getAccountNumber());
        String valueMaxDailyCount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_BUY, walletAccountCurrencyObjectOptional.getAccountNumber());
        String valueMaxDailyPrice = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_BUY, walletAccountCurrencyObjectOptional.getAccountNumber());
        AggregationPurchaseDTO aggregationPurchaseDTO = requestRepositoryService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccountEntity.getId()}, TransactionTypeEnum.BUY.name(), new Date(), new Date());

        // Step 5: Temporarily set MAX_DAILY_COUNT_BUY to current count
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_BUY, walletAccountEntity, aggregationPurchaseDTO.getCountRecord());

        // Step 6: Attempt to generate buy UUID - should fail due to daily count limitation
        generateBuyUuid(mockMvc, accessToken, walletAccountCurrencyObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.BUY_EXCEEDED_COUNT_DAILY_LIMITATION, false, "1", "0.001", "GOLD");

        // Step 7: Restore original MAX_DAILY_COUNT_BUY limit
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_BUY, walletAccountEntity, valueMaxDailyCount);

        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_BUY, walletAccountEntity, aggregationPurchaseDTO.getSumQuantity());

        generateBuyUuid(mockMvc, accessToken, walletAccountCurrencyObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.BUY_EXCEEDED_QUANTITY_DAILY_LIMITATION, false, "1", "0.001", "GOLD");

        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_BUY, walletAccountEntity, valueMaxDailyPrice);

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
    @Order(49)
    @DisplayName("buyDailyLimitationFailOnBuy-fail")
    void buyDailyLimitationOnBuyFail() throws Exception {
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "0.001";
        String merchantId = "1";
        String refNumber = new Date() + merchantId;
        String buyCommission = "RIAL";
        String commissionRialValue = "1000";
        String currencyToBuy = "GOLD";

        // Step 2: Clear buy limitations for testing
        walletBuyLimitationOperationService.deleteAll();

        // Step 3: Get user's RIAL account number
        WalletAccountObject walletAccountCurrencyObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);

        // Step 4: Get current buy statistics and limitation values
        WalletAccountEntity walletAccountCurrencyEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountCurrencyObjectOptional.getAccountNumber());
        String valueMaxDailyCount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_BUY, walletAccountCurrencyObjectOptional.getAccountNumber());
        String valueMaxDailyQuantity = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_BUY, walletAccountCurrencyObjectOptional.getAccountNumber());
        AggregationPurchaseDTO aggregationPurchaseDTO = requestRepositoryService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccountCurrencyEntity.getId()}, TransactionTypeEnum.BUY.name(), new Date(), new Date());

        // Step 5: Temporarily set MAX_DAILY_COUNT_BUY to current count we want generateUuid will be success
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_BUY, walletAccountCurrencyEntity, String.valueOf(Long.parseLong(aggregationPurchaseDTO.getCountRecord()) + 1));
        // Step 6: Attempt to generate buy UUID - should success due to daily count limitation
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, accessToken, walletAccountCurrencyObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, quantity, currencyToBuy);

        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_BUY, walletAccountCurrencyEntity, aggregationPurchaseDTO.getCountRecord());

        buyDirect(mockMvc, refNumber, accessToken, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price)), buyCommission, commissionRialValue, NATIONAL_CODE_CORRECT, currencyToBuy
                , merchantId, walletAccountCurrencyObjectOptional.getAccountNumber(), "", "", HttpStatus.OK, StatusRepositoryService.BUY_EXCEEDED_COUNT_DAILY_LIMITATION, false);

        // Step 7: Restore original MAX_DAILY_COUNT_BUY limit
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_BUY, walletAccountCurrencyEntity, valueMaxDailyCount);

        // step 8: Temporarily set MAX_DAILY_QUANTITY_BUY to current quantity we want generateUuid will be success
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_BUY, walletAccountCurrencyEntity, String.valueOf(new BigDecimal(aggregationPurchaseDTO.getSumQuantity()).add(new BigDecimal(10))));
        generateBuyUuid(mockMvc, accessToken, walletAccountCurrencyObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, quantity, currencyToBuy);
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_BUY, walletAccountCurrencyEntity, String.valueOf(aggregationPurchaseDTO.getSumQuantity()));

        buyDirect(mockMvc, refNumber, accessToken, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price)), buyCommission, commissionRialValue, NATIONAL_CODE_CORRECT, currencyToBuy
                , merchantId, walletAccountCurrencyObjectOptional.getAccountNumber(), "", "", HttpStatus.OK, StatusRepositoryService.BUY_EXCEEDED_QUANTITY_DAILY_LIMITATION, false);

        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_BUY, walletAccountCurrencyEntity, String.valueOf(valueMaxDailyQuantity));
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_BUY, walletAccountCurrencyEntity, String.valueOf(valueMaxDailyCount));
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
    @Order(50)
    @DisplayName("buyMonthlyLimitationFail-success")
    void buyMonthlyLimitationFail() throws Exception {
        // Step 1: Define test parameters
        String price = "100000";
        // Step 2: Clear buy limitation cache to start fresh
        walletBuyLimitationOperationService.deleteAll();
        // Step 3: Get user's RIAL account number
        WalletAccountObject walletAccountCurrencyObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 4: Update balance merchant wallet-account
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountCurrencyObjectOptional.getAccountNumber());
        // Step 5: Get current monthly limitation values for backup
        String valueMaxMonthlyCount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_COUNT_BUY, walletAccountEntity.getAccountNumber());
        String valueMaxMonthlyQuantity = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_QUANTITY_BUY, walletAccountEntity.getAccountNumber());
        String valueCheckMonthly = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MONTHLY_VALIDATION_CHECK_BUY, walletAccountEntity.getAccountNumber());
        // Step 6: Calculate current monthly usage period
        Date fromDate = DateUtils.findFirstDateOfPersianMonth(new Date());
        Date untilDate = new Date();
        log.info("found monthly fromTime ({}), until ({})", fromDate, untilDate);
        // Step 7: Get current monthly usage statistics
        AggregationPurchaseDTO aggregationPurchaseDTO = requestRepositoryService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccountEntity.getId()}, TransactionTypeEnum.BUY.name(), fromDate, untilDate);
        // Step 8: Set monthly count limitation to current usage to trigger failure
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_COUNT_BUY, walletAccountEntity, aggregationPurchaseDTO.getCountRecord());
        // Step 9: Test monthly count limitation failure
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MONTHLY_VALIDATION_CHECK_BUY, walletAccountEntity,"true");
        generateBuyUuid(mockMvc, accessToken, walletAccountEntity.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.BUY_EXCEEDED_COUNT_MONTHLY_LIMITATION, false, "1", "0.001", "GOLD");
        // Step 10: Restore original monthly count limitation
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_COUNT_BUY, walletAccountEntity, valueMaxMonthlyCount);
        // Step 11: Set monthly quantity limitation to current usage to trigger failure
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_QUANTITY_BUY, walletAccountEntity, aggregationPurchaseDTO.getSumQuantity());
        // Step 12: Test monthly quantity limitation failure
        generateBuyUuid(mockMvc, accessToken, walletAccountEntity.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.BUY_EXCEEDED_QUANTITY_MONTHLY_LIMITATION, false, "1", "0.001", "GOLD");
        // Step 13: Restore original monthly quantity limitation
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_QUANTITY_BUY, walletAccountEntity, valueMaxMonthlyQuantity);

        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MONTHLY_VALIDATION_CHECK_BUY, walletAccountEntity,valueCheckMonthly);
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
    @Order(51)
    @DisplayName("buyDirect-success")
    void buyDirectSuccess() throws Exception {
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "1.07";
        // Step 2: Clear buy limitation cache to start fresh
        walletBuyLimitationOperationService.deleteAll();
        // Step 3: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        WalletAccountObject walletAccountObjectOptionalRial = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.RIAL);
        // Step 4: Update balance merchant wallet-account
        WalletEntity walletMerchantEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeRepositoryService.getByName(WalletTypeRepositoryService.MERCHANT).getId());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountRepositoryService.findByWallet(walletMerchantEntity);
        // Step 5: Find gold wallet account
        long id = 0;
        try {
            id = walletAccountCurrencyRepositoryService.findCurrency(WalletAccountCurrencyRepositoryService.GOLD).getId();
        } catch (InternalServiceException ex) {
            log.error("walletAccountCurrencyEntityGold not found", ex);
        }
        // Step 6: Enable cash-in if disabled
        String valueCashInGold = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if ("false".equalsIgnoreCase(valueCashInGold)) {
            WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true");

        }
        WalletAccountEntity walletAccountEntityRial = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptionalRial.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntityRial, "true");

        // Step 7: Increase merchant GOLD balance
        long finalId = id;
        WalletAccountEntity walletAccountEntity = walletAccountEntityList.stream().filter(
                x -> x.getWalletAccountCurrencyEntity().getId() == finalId).findFirst().orElse(null);
        walletAccountRepositoryService.increaseBalance(walletAccountEntity.getId(), new BigDecimal("1.07"));
        // Step 8: Generate cash-in UUID and perform cash-in
        BaseResponse<UuidResponse> uniqueIdentifierCashIn = generateCashInUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, price, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifierCashIn);
        cashIn(mockMvc, accessToken, uniqueIdentifierCashIn.getData().getUniqueIdentifier(), String.valueOf(new Date().getTime()), price, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        // Step 9: Define buy direct parameters
        String merchantId = "1";
        String refNumber = new Date() + "";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "";
        // Step 10: Generate buy UUID
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, "0.001", currency);
        // Step 11: Perform buy direct operation
        buyDirect(mockMvc, refNumber, accessToken, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, currency
                , merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
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
    @Order(52)
    @DisplayName("buyDirectFail-InvalidCommissionCurrency")
    void buyDirectFailInvalidCommissionCurrency() throws Exception {
        log.info("start buyDirectFailInvalidCommissionCurrency test");
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "1.07";
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Charge account first
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String cashInValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(cashInValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true");
        }
        // Step 4: Perform cash-in operation
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, cashInAmount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, accessToken, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        // Step 5: Update merchant balance
        increaseMerchantBalance("1.07", WalletAccountCurrencyRepositoryService.GOLD, "1111111111");
        // Step 6: Define buy direct parameters with invalid commission currency
        String merchantId = "1";
        String commission = "2000";
        String commissionType = "GOLD"; // Invalid commission currency
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "test commission currency error";
        // Step 7: Generate UUID for buyDirect
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, "0.001", currency);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 8: Test with GOLD commission currency (should fail)
        BaseResponse<PurchaseResponse> response = buyDirect(mockMvc, refNumber, accessToken, uniqueIdentifier, quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, currency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusRepositoryService.COMMISSION_CURRENCY_NOT_VALID, false);
        Assert.assertSame(StatusRepositoryService.COMMISSION_CURRENCY_NOT_VALID, response.getErrorDetail().getCode());
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
    @Order(53)
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        String sign = "";
        String additionalData = "test invalid uuid";
        // Step 3: Test with invalid UUID
        buyDirect(mockMvc, refNumber, accessToken, invalidUuid, quantity, price, commissionType, commission, NATIONAL_CODE_CORRECT, CURRENCY_GOLD, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusRepositoryService.UUID_NOT_FOUND, false);
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
    @Order(54)
    @DisplayName("buyDirectFail-MerchantBalanceNotEnough")
    void buyDirectFailMerchantBalanceNotEnough() throws Exception {
        log.info("start buyDirectFailMerchantBalanceNotEnough test");
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "1.07";
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Charge account first
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String cashInValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(cashInValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true");
        }
        // Step 4: Perform cash-in operation
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, cashInAmount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, accessToken, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
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
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, quantity, currency);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 7: Set merchant balance to zero to simulate insufficient balance
        setMerchantBalanceToZero(WalletAccountCurrencyRepositoryService.GOLD,"1111111111");
        // Step 8: Test with insufficient merchant balance
        BaseResponse<PurchaseResponse> response = buyDirect(mockMvc, refNumber, accessToken, uniqueIdentifier, quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT,
                currency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusRepositoryService.MERCHANT_BALANCE_NOT_ENOUGH, false);
        Assert.assertSame(StatusRepositoryService.MERCHANT_BALANCE_NOT_ENOUGH, response.getErrorDetail().getCode());
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
    @Order(55)
    @DisplayName("buyDirectFail-InvalidMerchantId")
    void buyDirectFailInvalidMerchantId() throws Exception {
        log.info("start buyDirectFailInvalidMerchantId test");
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "1.07";
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Charge account first
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String cashInValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(cashInValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true");
        }
        // Step 4: Perform cash-in operation
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, cashInAmount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, accessToken, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        // Step 5: Update merchant balance
        increaseMerchantBalance("1.07", WalletAccountCurrencyRepositoryService.GOLD, "1111111111");
        // Step 6: Define buy direct parameters with invalid merchant ID
        String merchantId = "1";
        String invalidMerchantId = "999"; // Invalid merchant ID
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "test invalid merchant id";
        // Step 7: Generate buy UUID
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, "0.001", currency);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 8: Test with invalid merchant ID
        BaseResponse<PurchaseResponse> response = buyDirect(mockMvc, refNumber, accessToken, uniqueIdentifier, quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, currency, invalidMerchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusRepositoryService.MERCHANT_IS_NOT_EXIST, false);
        Assert.assertSame(StatusRepositoryService.MERCHANT_IS_NOT_EXIST, response.getErrorDetail().getCode());
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
    @Order(56)
    @DisplayName("buyDirectFail-InvalidCurrency")
    void buyDirectFailInvalidCurrency() throws Exception {
        log.info("start buyDirectFailInvalidCurrency test");
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "1.07";
        // Step 2: Get user's RIAL account number
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        // Step 3: Charge account first
        String refNumber = new Date().getTime() + "";
        String cashInAmount = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String cashInValue = getSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(cashInValue)){
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true");
        }
        // Step 4: Perform cash-in operation
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, accessToken, NATIONAL_CODE_CORRECT, cashInAmount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, accessToken, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());
        // Step 5: Update merchant balance
        increaseMerchantBalance("1.07", WalletAccountCurrencyRepositoryService.GOLD, "1111111111");
        // Step 6: Define buy direct parameters with invalid currency
        String merchantId = "1";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = "GOLD";
        String invalidCurrency = "INVALID_CURRENCY"; // Invalid currency
        String sign = "";
        String additionalData = "test invalid currency";
        // Step 7: Generate buy UUID
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, "0.001", currency);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        // Step 8: Test with invalid currency
        BaseResponse<PurchaseResponse> response = buyDirect(mockMvc, refNumber, accessToken, uniqueIdentifier, quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, invalidCurrency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);
        Assert.assertSame(StatusRepositoryService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, response.getErrorDetail().getCode());
    }

    /**
     * Simple test to verify buyDirect method works before attempting concurrency
     */
    @Test
    @Order(56)
    @DisplayName("simple buyDirect test - verify method works")
    void simpleBuyDirectTest() throws Exception {
        log.info("Testing simple buyDirect functionality...");
        
        // Setup
        String price = "100000";
        String quantity = "0.1";
        String merchantId = "1";
        String commission = "1000";
        String commissionType = "RIAL";
        String currency = "GOLD";
        String sign = "";
        String additionalData = "simple test";
        
        // Get account
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        
        // Setup merchant balance
        increaseMerchantBalance("5.0", WalletAccountCurrencyRepositoryService.GOLD, "1111111111");
        
        // Generate UUID and test buyDirect
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, "0.001", currency);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        BaseResponse<PurchaseResponse> response = buyDirect(mockMvc, "SIMPLE_TEST_" + new Date().getTime(), accessToken, uniqueIdentifier, quantity, price, commissionType, commission, NATIONAL_CODE_CORRECT, currency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        log.info("Simple buyDirect test result: Success={}, ErrorCode={}", response.getSuccess(), response.getErrorDetail() != null ? response.getErrorDetail().getCode() : "N/A");
        Assert.assertTrue("Simple buyDirect should succeed", response.getSuccess());
    }

        /**
     * Ultra-minimal test to identify the exact issue
     */
    @Test
    @Order(57)
    @DisplayName("ultra-minimal test")
    void ultraMinimalTest() throws Exception {
        log.info("=== Starting Ultra-Minimal Test ===");
        
        // Test 1: Can we create threads at all?
        log.info("Test 1: Basic thread creation");
        ExecutorService executor = Executors.newFixedThreadPool(1);
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] threadExecuted = {false};
        
        executor.submit(() -> {
            log.info("Basic thread is running!");
            threadExecuted[0] = true;
            latch.countDown();
        });
        
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        log.info("Basic thread executed: {}", threadExecuted[0]);
        Assert.assertTrue("Basic thread should execute", threadExecuted[0]);
        
        // Test 2: Can we call buyDirect in a thread?
        log.info("Test 2: buyDirect in thread");
        executor = Executors.newFixedThreadPool(1);
        final CountDownLatch latch1 = new CountDownLatch(1);
        final BaseResponse<PurchaseResponse>[] threadResponse = new BaseResponse[1];
        final Exception[] threadException = new Exception[1];
        
        executor.submit(() -> {
            try {
                log.info("Thread starting buyDirect call...");
                
                // Setup
                String price = "100000";
                String quantity = "0.1";
                String merchantId = "1";
                String commission = "1000";
                String commissionType = "RIAL";
                String currency = "GOLD";
                String sign = "";
                String additionalData = "ultra minimal test";
                
                WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
                increaseMerchantBalance("5.0", WalletAccountCurrencyRepositoryService.GOLD, "1111111111");
                
                BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, "0.001", currency);
                String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
                
                log.info("Thread calling buyDirect with UUID: {}", uniqueIdentifier);
                threadResponse[0] = buyDirect(mockMvc, "ULTRA_" + System.currentTimeMillis(), accessToken, uniqueIdentifier, quantity, price, commissionType, commission, NATIONAL_CODE_CORRECT, currency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                log.info("Thread completed buyDirect call successfully");
                
            } catch (Exception e) {
                log.error("Thread encountered exception: {}", e.getMessage(), e);
                threadException[0] = e;
            } finally {
                latch1.countDown();
            }
        });
        
        latch1.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        
        if (threadException[0] != null) {
            log.error("Thread failed with exception: {}", threadException[0].getMessage());
            Assert.fail("Thread failed: " + threadException[0].getMessage());
        }
        
        if (threadResponse[0] != null) {
            log.info("Thread response: Success={}", threadResponse[0].getSuccess());
        } else {
            log.error("Thread response is null!");
            Assert.fail("Thread response is null");
        }
        
        log.info("=== Ultra-Minimal Test Completed ===");
    }

    /**
     * Test without ExecutorService - using raw Threads
     */
    @Test
    @Order(58)
    @DisplayName("concurrent test without ExecutorService")
    void concurrentTestWithoutExecutorService() throws Exception {
        log.info("=== Starting Concurrent Test Without ExecutorService ===");
        
        // Setup
        String price = "100000";
        String quantity = "0.1";
        String merchantId = "1";
        String commission = "1000";
        String commissionType = "RIAL";
        String currency = "GOLD";
        String sign = "";
        String additionalData = "no executor test";
        
        // Get account and setup
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        increaseMerchantBalance("5.0", WalletAccountCurrencyRepositoryService.GOLD, "1111111111");
        
        // Generate UUID
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, "0.001", currency);
        String sharedUniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        log.info("Generated UUID: {}", sharedUniqueIdentifier);
        
        // Test with 2 threads using raw Thread objects
        final CountDownLatch latch = new CountDownLatch(2);
        final List<BuyResult> results = new ArrayList<>();
        
        // Thread 1
        Thread thread1 = new Thread(() -> {
            try {
                log.info("Raw Thread 1 starting...");
                String ref1 = "THREAD1_" + System.currentTimeMillis();
                BaseResponse<PurchaseResponse> response1 = buyDirectWithoutCheckResult(mockMvc, ref1, accessToken, sharedUniqueIdentifier, quantity, price, commissionType, commission, NATIONAL_CODE_CORRECT, currency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData + "_1");
                
                BuyResult result1 = new BuyResult();
                result1.threadId = 1;
                result1.success = response1.getSuccess();
                result1.errorCode = response1.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response1.getErrorDetail().getCode();
                result1.response = response1;
                
                synchronized (results) {
                    results.add(result1);
                    log.info("Raw Thread 1 added result to collection. Collection size now: {}", results.size());
                }
                log.info("Raw Thread 1 completed: Success={}, ErrorCode={}", result1.success, result1.errorCode);
                
            } catch (Exception e) {
                log.error("Raw Thread 1 exception: {}", e.getMessage(), e);
                BuyResult result1 = new BuyResult();
                result1.threadId = 1;
                result1.success = false;
                result1.errorCode = -999;
                result1.exception = e;
                synchronized (results) {
                    results.add(result1);
                    log.info("Raw Thread 1 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("Raw Thread 1 countDown called");
            }
        });
        
        // Thread 2
        Thread thread2 = new Thread(() -> {
            try {
                log.info("Raw Thread 2 starting...");
                String ref2 = "THREAD2_" + System.currentTimeMillis();
                BaseResponse<PurchaseResponse> response2 = buyDirectWithoutCheckResult(mockMvc, ref2, accessToken, sharedUniqueIdentifier, quantity, price, commissionType, commission, NATIONAL_CODE_CORRECT, currency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData + "_2");
                
                BuyResult result2 = new BuyResult();
                result2.threadId = 2;
                result2.success = response2.getSuccess();
                result2.errorCode = response2.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response2.getErrorDetail().getCode();
                result2.response = response2;
                
                synchronized (results) {
                    results.add(result2);
                    log.info("Raw Thread 2 added result to collection. Collection size now: {}", results.size());
                }
                log.info("Raw Thread 2 completed: Success={}, ErrorCode={}", result2.success, result2.errorCode);
                
            } catch (Exception e) {
                log.error("Raw Thread 2 exception: {}", e.getMessage(), e);
                BuyResult result2 = new BuyResult();
                result2.threadId = 2;
                result2.success = false;
                result2.errorCode = -999;
                result2.exception = e;
                synchronized (results) {
                    results.add(result2);
                    log.info("Raw Thread 2 added exception result to collection. Collection size now: {}", results.size());
                }
            } finally {
                latch.countDown();
                log.info("Raw Thread 2 countDown called");
            }
        });
        
        // Start both threads
        log.info("Starting both raw threads...");
        thread1.start();
        thread2.start();
        
        // Wait and analyze
        log.info("Waiting for threads to complete...");
        latch.await();
        
        log.info("All threads completed, Results collection size: {}", results.size());
        
        // Log all results
        for (BuyResult result : results) {
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
        BuyResult failedResult = results.stream().filter(r -> !r.success).findFirst().orElse(null);
        Assert.assertNotNull("Should have a failed result", failedResult);
        Assert.assertEquals(StatusRepositoryService.DUPLICATE_UUID, failedResult.errorCode);
        log.info("Failed result error code: {}", failedResult.errorCode);
        
                log.info("=== Concurrent Test Without ExecutorService Completed ===");
    }

    /**
     * Test concurrent buyDirect operations with same paymentId (refNumber) to ensure proper concurrency control.
     * This test covers scenario 1: two different UUIDs with same refNumber sent simultaneously.
     * This method:
     * - Sets up wallet and merchant balance
     * - Generates DIFFERENT UUIDs for buyDirect operations
     * - Runs multiple concurrent buyDirect requests with SAME refNumber but different UUIDs
     * - Verifies only one succeeds and others fail with appropriate error codes
     */
    @Test
    @Order(58)
    @DisplayName("concurrent buy operations with same paymentId (refNumber) - scenario 1")
    void concurrentBuyWithSamePaymentId() throws Exception {
        log.info("start concurrentBuyWithSamePaymentId test");
        
        // Step 1: Define test parameters
        String price = "100000";
        String quantity = "0.3";
        String merchantId = "1";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = "GOLD";
        String sign = "";
        String additionalData = "concurrent test same paymentId";
        String sharedRefNumber = "PAY_" + new Date().getTime(); // Same payment reference for all threads
        
        // Step 2: Clear buy limitations for testing
        walletBuyLimitationOperationService.deleteAll();
        
        // Step 3: Get user's GOLD account number (only GOLD needed for buyDirect)
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        
        // Step 4: Increase merchant balance with sufficient amount for multiple transactions
        increaseMerchantBalance("10.0", WalletAccountCurrencyRepositoryService.GOLD, "1111111111");
        
        // Step 5: Generate multiple UUIDs for concurrent operations (each thread gets its own UUID, no cashIn needed for buyDirect)
        int numberOfThreads = 5;
        List<String> uniqueIdentifiers = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, "0.001", currency);
            uniqueIdentifiers.add(uuidResponse.getData().getUniqueIdentifier());
        }
        
        // Step 8: Create multiple concurrent buy requests using the same refNumber but different UUIDs
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<BuyResult> results = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            final String uniqueIdentifier = uniqueIdentifiers.get(i);
            
            Thread thread = new Thread(() -> {
                try {
                    // All threads use the same refNumber to test payment ID concurrency
                    BaseResponse<PurchaseResponse> response = buyDirectWithoutCheckResult(mockMvc, sharedRefNumber, accessToken, uniqueIdentifier, quantity, price, commissionType, commission, NATIONAL_CODE_CORRECT, currency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData + "_" + threadId);
                    
                    BuyResult result = new BuyResult();
                    result.threadId = threadId;
                    result.success = response.getSuccess();
                    result.errorCode = response.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response.getErrorDetail().getCode();
                    result.response = response;
                    
                    synchronized (results) {
                        results.add(result);
                        log.info("Thread {} added result. Collection size now: {}", threadId, results.size());
                    }
                    
                } catch (Exception e) {
                    log.error("Thread {} encountered exception: {}", threadId, e.getMessage());
                    BuyResult result = new BuyResult();
                    result.threadId = threadId;
                    result.success = false;
                    result.errorCode = -999; // Exception occurred
                    result.exception = e;
                    synchronized (results) {
                        results.add(result);
                        log.info("Thread {} added exception result. Collection size now: {}", threadId, results.size());
                    }
                } finally {
                    latch.countDown();
                    log.info("Thread {} countDown called", threadId);
                }
            });
            
            threads.add(thread);
        }
        
        // Start all threads
        log.info("Starting {} threads...", numberOfThreads);
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Step 9: Wait for all threads to complete
        latch.await();
        
        // Step 10: Analyze results
        long successCount = results.stream().filter(r -> r.success).count();
        long failureCount = results.stream().filter(r -> !r.success).count();
        
        log.info("Concurrent test results: {} successes, {} failures out of {} threads", successCount, failureCount, numberOfThreads);
        
        // Step 11: Log detailed results for debugging
        logDetailedResults(new ConcurrentLinkedQueue<>(results), "Same RefNumber Test");
        
        // Step 12: Verify that only one transaction succeeded (to prevent duplicate payments)
        Assert.assertEquals("Only one buy operation should succeed with the same payment reference", 1, successCount);
        Assert.assertEquals("Other operations should fail", numberOfThreads - 1, failureCount);
        
        // Step 13: Verify that failed operations have appropriate error codes
        List<BuyResult> failedResults = results.stream().filter(r -> !r.success).toList();
        for (BuyResult failedResult : failedResults) {
            log.info("Failed thread {}: error code = {}", failedResult.threadId, failedResult.errorCode);
            // Common error codes for duplicate payment reference: REF_NUMBER_USED_BEFORE, etc.
            Assert.assertEquals("Failed operation should have a valid error code", StatusRepositoryService.REF_NUMBER_USED_BEFORE, failedResult.errorCode);
        }
        
        log.info("Concurrent paymentId test completed successfully");
    }

    /**
     * Comprehensive test for both concurrency scenarios:
     * Scenario 1: Two different UUIDs with same refNumber sent simultaneously
     * Scenario 2: Same UUID sent to method simultaneously
     * This test runs both scenarios in sequence to verify proper concurrency control.
     */
    @Test
    @Order(59)
    @DisplayName("comprehensive concurrent buy test - both scenarios")
    void comprehensiveConcurrentBuyTest() throws Exception {
        log.info("start comprehensiveConcurrentBuyTest");
        
        // Common setup
        String price = "100000";
        String quantity = "0.2";
        String merchantId = "1";
        String commission = "1000";
        String commissionType = "RIAL";
        String currency = "GOLD";
        String sign = "";
        String additionalData = "comprehensive concurrent test";
        
        // Clear limitations
        walletBuyLimitationOperationService.deleteAll();
        
        // Get account number (only GOLD needed for buyDirect)
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, WalletAccountCurrencyRepositoryService.GOLD);
        
        // Setup sufficient merchant balance (no cashIn needed for buyDirect)
        increaseMerchantBalance("20.0", WalletAccountCurrencyRepositoryService.GOLD, "1111111111");
        
        // SCENARIO 1: Different UUIDs with same refNumber
        log.info("Testing Scenario 1: Different UUIDs with same refNumber");
        testScenario1_DifferentUuidsWithSameRefNumber(price, quantity, merchantId, commission, commissionType, currency, sign, additionalData, walletAccountObjectOptional);
        
        // Wait a bit between scenarios
        Thread.sleep(1000);
        
        // SCENARIO 2: Same UUID with different refNumbers
        log.info("Testing Scenario 2: Same UUID with different refNumbers");
        testScenario2_SameUuidWithDifferentRefNumbers(price, quantity, merchantId, commission, commissionType, currency, sign, additionalData, walletAccountObjectOptional);
        
        log.info("Comprehensive concurrent test completed successfully");
    }

    /**
     * Test Scenario 1: Two different UUIDs with same refNumber sent simultaneously
     * This should fail for all but one transaction due to duplicate payment reference
     */
    private void testScenario1_DifferentUuidsWithSameRefNumber(String price, String quantity, String merchantId, 
            String commission, String commissionType, String currency, String sign, String additionalData,
            WalletAccountObject walletAccountObjectOptional) throws Exception {
        
        log.info("Executing Scenario 1: Different UUIDs with same refNumber");
        
        int numberOfThreads = 3;
        String sharedRefNumber = "SCENARIO1_" + new Date().getTime();
        
        // Generate different UUIDs for each thread
        List<String> uniqueIdentifiers = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, "0.001", currency);
            uniqueIdentifiers.add(uuidResponse.getData().getUniqueIdentifier());
        }
        
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<BuyResult> results = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        
        // Execute concurrent requests with different UUIDs but same refNumber
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            final String uniqueIdentifier = uniqueIdentifiers.get(i);
            
            Thread thread = new Thread(() -> {
                try {
                    BaseResponse<PurchaseResponse> response = buyDirectWithoutCheckResult(mockMvc, sharedRefNumber, accessToken, uniqueIdentifier, quantity, price, commissionType, commission, NATIONAL_CODE_CORRECT, currency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData + "_scenario1_" + threadId);
                    
                    BuyResult result = new BuyResult();
                    result.threadId = threadId;
                    result.success = response.getSuccess();
                    result.errorCode = response.getSuccess() ? StatusRepositoryService.SUCCESSFUL : response.getErrorDetail().getCode();
                    result.response = response;
                    
                    synchronized (results) {
                        results.add(result);
                        log.info("Scenario 1 - Thread {} added result. Collection size now: {}", threadId, results.size());
                    }
                    
                } catch (Exception e) {
                    log.error("Scenario 1 - Thread {} encountered exception: {}", threadId, e.getMessage());
                    BuyResult result = new BuyResult();
                    result.threadId = threadId;
                    result.success = false;
                    result.errorCode = -999;
                    result.exception = e;
                    synchronized (results) {
                        results.add(result);
                        log.info("Scenario 1 - Thread {} added exception result. Collection size now: {}", threadId, results.size());
                    }
                } finally {
                    latch.countDown();
                    log.info("Scenario 1 - Thread {} countDown called", threadId);
                }
            });
            
            threads.add(thread);
        }
        
        // Start all threads
        log.info("Starting {} threads for Scenario 1...", numberOfThreads);
        for (Thread thread : threads) {
            thread.start();
        }
        
        latch.await();
        
        // Analyze results for Scenario 1
        long successCount = results.stream().filter(r -> r.success).count();
        long failureCount = results.stream().filter(r -> !r.success).count();
        
        log.info("Scenario 1 results: {} successes, {} failures out of {} threads", successCount, failureCount, numberOfThreads);
        logDetailedResults(new ConcurrentLinkedQueue<>(results), "Scenario 1 - Different UUIDs, Same RefNumber");
        
        // Verify that only one transaction succeeded (due to duplicate refNumber prevention)
        Assert.assertEquals("Scenario 1: Only one buy operation should succeed with the same refNumber", 1, successCount);
        Assert.assertEquals("Scenario 1: Other operations should fail", numberOfThreads - 1, failureCount);
        
        // Verify failed operations have appropriate error codes
        List<BuyResult> failedResults = results.stream().filter(r -> !r.success).toList();
        for (BuyResult failedResult : failedResults) {
            log.debug("Scenario 1 - Thread {}: error code = {}, exception = {}", 
                failedResult.threadId, failedResult.errorCode, 
                failedResult.exception != null ? failedResult.exception.getMessage() : "none");
            Assert.assertTrue("Scenario 1: Failed operation should have error code indicating duplicate refNumber", 
                failedResult.errorCode == StatusRepositoryService.REF_NUMBER_USED_BEFORE && failedResult.errorCode > 0);
        }
    }

    /**
     * Test Scenario 2: Same UUID sent to method simultaneously
     * This should fail for all but one transaction due to UUID reuse
     */
    private void testScenario2_SameUuidWithDifferentRefNumbers(String price, String quantity, String merchantId,
            String commission, String commissionType, String currency, String sign, String additionalData,
            WalletAccountObject walletAccountObjectOptional) throws Exception {
        
        log.info("Executing Scenario 2: Same UUID with different refNumbers");
        
        int numberOfThreads = 3;
        
        // Generate single UUID for all threads
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, accessToken, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true, merchantId, "0.001", currency);
        String sharedUniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        List<BuyResult> results = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        
        // Execute concurrent requests with same UUID but different refNumbers
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            
            Thread thread = new Thread(() -> {
                try {
                    String threadRefNumber = "SCENARIO2_" + new Date().getTime() + "_thread_" + threadId;
                    BaseResponse<PurchaseResponse> response = buyDirectWithoutCheckResult(mockMvc, threadRefNumber, accessToken, sharedUniqueIdentifier, quantity, price, commissionType, commission, NATIONAL_CODE_CORRECT, currency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData + "_scenario2_" + threadId);
                    
                    BuyResult result = new BuyResult();
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
                    BuyResult result = new BuyResult();
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
        Assert.assertEquals("Scenario 2: Only one buy operation should succeed with the same UUID", 1, successCount);
        Assert.assertEquals("Scenario 2: Other operations should fail", numberOfThreads - 1, failureCount);
        
        // Verify failed operations have appropriate error codes
        List<BuyResult> failedResults = results.stream().filter(r -> !r.success).toList();
        for (BuyResult failedResult : failedResults) {
            log.debug("Scenario 2 - Thread {}: error code = {}, exception = {}", 
                failedResult.threadId, failedResult.errorCode, 
                failedResult.exception != null ? failedResult.exception.getMessage() : "none");
            Assert.assertTrue("Scenario 2: Failed operation should have error code indicating UUID reuse",
                    failedResult.errorCode == StatusRepositoryService.DUPLICATE_UUID && failedResult.errorCode > 0);
        }
    }
    
    /**
     * Helper method to log detailed results of concurrent operations for debugging
     */
    private void logDetailedResults(ConcurrentLinkedQueue<BuyResult> results, String testName) {
        log.info("=== Detailed Results for {} ===", testName);
        
        // Log successful results
        List<BuyResult> successfulResults = results.stream().filter(r -> r.success).toList();
        for (BuyResult result : successfulResults) {
            log.info("✓ Thread {}: SUCCESS - Transaction completed successfully", result.threadId);
            if (result.response != null && result.response.getData() != null) {
                log.debug("  └─ Response data: {}", result.response.getData());
            }
        }
        
        // Log failed results
        List<BuyResult> failedResults = results.stream().filter(r -> !r.success).toList();
        for (BuyResult result : failedResults) {
            log.info("✗ Thread {}: FAILED - Error code: {}", result.threadId, result.errorCode);
            if (result.exception != null) {
                log.debug("  └─ Exception: {}", result.exception.getMessage());
            }
            if (result.response != null && result.response.getErrorDetail() != null) {
                log.debug("  └─ Error detail: {}", result.response.getErrorDetail());
            }
        }
        
        log.info("=== End of Detailed Results ===");
    }

    /**
     * Helper class to store the results of concurrent buy operations.
     * This class captures all relevant information from each concurrent thread execution
     * for analysis, debugging, and test validation.
     */
    private static class BuyResult {
        /** The ID of the thread that executed this operation */
        int threadId;
        
        /** Whether the operation was successful */
        boolean success;
        
        /** The error code returned (0 for success, positive for specific errors) */
        int errorCode;
        
        /** The complete response object from the buyDirect operation */
        BaseResponse<PurchaseResponse> response;
        
        /** Any exception that occurred during execution */
        Exception exception;
        
        @Override
        public String toString() {
            return String.format("BuyResult{threadId=%d, success=%s, errorCode=%d, hasException=%s}", 
                threadId, success, errorCode, exception != null);
        }
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
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity, "true");
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
    private void increaseMerchantBalance(String val, String currency, String merchantNationalCode) throws Exception {
        // Step 1: Find merchant wallet entity
        WalletEntity walletMerchantEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId(merchantNationalCode, walletTypeRepositoryService.getByName(WalletTypeRepositoryService.MERCHANT).getId());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountRepositoryService.findByWallet(walletMerchantEntity);
        // Step 2: Find currency wallet account
        long id = 0;
        try {
            id = walletAccountCurrencyRepositoryService.findCurrency(currency).getId();
        } catch (InternalServiceException ex) {
            log.error("walletAccountCurrencyEntity({}) not found", currency, ex);
        }
        // Step 3: Find the specific currency account and increase balance
        long finalId = id;
        WalletAccountEntity walletAccountEntity = walletAccountEntityList.stream().filter(
                x -> x.getWalletAccountCurrencyEntity().getId() == finalId).findFirst().orElse(null);
        walletAccountRepositoryService.increaseBalance(walletAccountEntity.getId(), new BigDecimal(val));
    }

    /**
     * Helper method to decrease merchant balance for testing.
     * This method:
     * - Finds merchant by national code
     * - Decreases balance by specified amount and currency
     * - Used for setting up test scenarios
     */
    private void decreaseMerchantBalance(String val, String currency, String merchantNationalCode) throws Exception {
        // Step 1: Find merchant wallet entity
        WalletEntity walletMerchantEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId(merchantNationalCode, walletTypeRepositoryService.getByName(WalletTypeRepositoryService.MERCHANT).getId());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountRepositoryService.findByWallet(walletMerchantEntity);
        // Step 2: Find currency wallet account
        long id = 0;
        try {
            id = walletAccountCurrencyRepositoryService.findCurrency(currency).getId();
        } catch (InternalServiceException ex) {
            log.error("walletAccountCurrencyEntity({}) not found", currency, ex);
        }
        // Step 3: Find the specific currency account and decrease balance
        long finalId = id;
        WalletAccountEntity walletAccountEntity = walletAccountEntityList.stream().filter(
                x -> x.getWalletAccountCurrencyEntity().getId() == finalId).findFirst().orElse(null);
        walletAccountRepositoryService.decreaseBalance(walletAccountEntity.getId(), new BigDecimal(val));
    }


    /**
     * Helper method to set merchant balance to zero for testing.
     * This method:
     * - Finds merchant by national code
     * - Sets balance to zero for specified currency
     * - Used for setting up test scenarios
     */
    private void setMerchantBalanceToZero(String currency, String merchantNationalCode) throws Exception {
        // Step 1: Find merchant wallet entity
        WalletEntity walletMerchantEntity = walletRepositoryService.findByNationalCodeAndWalletTypeId(merchantNationalCode, walletTypeRepositoryService.getByName(WalletTypeRepositoryService.MERCHANT).getId());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountRepositoryService.findByWallet(walletMerchantEntity);
        // Step 2: Find currency wallet account
        long id = 0;
        try {
            id = walletAccountCurrencyRepositoryService.findCurrency(currency).getId();
        } catch (InternalServiceException ex) {
            log.error("walletAccountCurrencyEntity({}) not found", currency, ex);
        }
        // Step 3: Find the specific currency account and set balance to zero
        long finalId = id;
        WalletAccountEntity walletAccountEntity = walletAccountEntityList.stream().filter(
                x -> x.getWalletAccountCurrencyEntity().getId() == finalId).findFirst().orElse(null);
        walletAccountRepositoryService.decreaseBalance(walletAccountEntity.getId(), walletAccountRepositoryService.getBalance(walletAccountEntity.getId()));
    }
}
