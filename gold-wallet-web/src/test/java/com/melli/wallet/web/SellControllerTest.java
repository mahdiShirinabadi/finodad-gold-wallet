package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.UuidResponse;
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
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

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

    private static final String NATIONAL_CODE_CORRECT = "0077847660";
    private static final String MOBILE_CORRECT = "09124162337";
    private static final String CURRENCY_GOLD = "GOLD";

    @Test
    @Order(1)
    @DisplayName("Setup test environment")
    void setup() throws Exception {


        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);
        log.info("start cleaning initial values in test DB for purchase");
        flyway.clean();
        flyway.migrate();
        log.info("start cleaning initial values in test DB for purchase");
        cacheClearService.clearCache();
        
        // Login to get access token
        BaseResponse<LoginResponse> loginResponse = login(mockMvc, USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        ACCESS_TOKEN = loginResponse.getData().getAccessTokenObject().getToken();

        // Create wallet if needed
        try {
            createWallet(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        } catch (Exception e) {
            // Wallet might already exist
            log.info("Wallet might already exist: {}", e.getMessage());
        }
    }

    // ==================== SELL TESTS ====================

    @Test
    @Order(60)
    @DisplayName("generateSellUuid-Success")
    void generateSellUuidSuccess() throws Exception {
        log.info("start generateSellUuidSuccess test");
        String quantity = "1.07";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
    }

    @Test
    @Order(61)
    @DisplayName("generateSellUuid-Fail-InvalidNationalCode")
    void generateSellUuidFailInvalidNationalCode() throws Exception {
        log.info("start generateSellUuidFailInvalidNationalCode test");
        String quantity = "1.07";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, "1234567890", quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.WALLET_NOT_FOUND, false);
    }

    @Test
    @Order(62)
    @DisplayName("generateSellUuid-Fail-InvalidAccountNumber")
    void generateSellUuidFailInvalidAccountNumber() throws Exception {
        log.info("start generateSellUuidFailInvalidAccountNumber test");
        String quantity = "1.07";
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, "invalid_account", CURRENCY_GOLD, HttpStatus.OK, StatusService.WALLET_ACCOUNT_NOT_FOUND, false);
    }

    @Test
    @Order(63)
    @DisplayName("generateSellUuid-Fail-InvalidCurrency")
    void generateSellUuidFailInvalidCurrency() throws Exception {
        log.info("start generateSellUuidFailInvalidCurrency test");
        String quantity = "1.07";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), "INVALID_CURRENCY", HttpStatus.OK, StatusService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);
    }

    @Test
    @Order(64)
    @DisplayName("generateSellUuid-Fail-LessThanMinQuantity")
    void generateSellUuidFailLessThanMinQuantity() throws Exception {
        log.info("start generateSellUuidFailLessThanMinQuantity test");
        String quantity = "0.001"; // Very small quantity
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.AMOUNT_LESS_THAN_MIN, false);
    }

    @Test
    @Order(65)
    @DisplayName("generateSellUuid-Fail-BiggerThanMaxQuantity")
    void generateSellUuidFailBiggerThanMaxQuantity() throws Exception {
        log.info("start generateSellUuidFailBiggerThanMaxQuantity test");
        String quantity = "1000000"; // Very large quantity
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.AMOUNT_BIGGER_THAN_MAX, false);
    }

    @Test
    @Order(70)
    @DisplayName("sell-Success")
    void sellSuccess() throws Exception {
        log.info("start sellSuccess test");
        String quantity = "1.07";
        String price = "100000";
        
        // Get account numbers
        WalletAccountObject goldAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);

        // Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(goldAccountObject.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("2.0"));
        
        // Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("200000"));
        
        // Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, goldAccountObject.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Perform sell operation
        BaseResponse<PurchaseResponse> response = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", goldAccountObject.getAccountNumber(), "", "test sell success", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
    }

    @Test
    @Order(71)
    @DisplayName("sell-Fail-InvalidUniqueIdentifier")
    void sellFailInvalidUniqueIdentifier() throws Exception {
        log.info("start sellFailInvalidUniqueIdentifier test");
        String quantity = "1.07";
        String price = "100000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        BaseResponse<PurchaseResponse> response = sell(mockMvc, ACCESS_TOKEN, "invalid-uuid", quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test invalid uuid", HttpStatus.OK, StatusService.UUID_NOT_FOUND, false);
        Assert.assertSame(StatusService.UUID_NOT_FOUND, response.getErrorDetail().getCode());
    }

    @Test
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
        BaseResponse<PurchaseResponse> response = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "invalid_sign", "test invalid sign", HttpStatus.OK, StatusService.INVALID_SIGN, false);
        Assert.assertSame(StatusService.INVALID_SIGN, response.getErrorDetail().getCode());
    }

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
        BaseResponse<PurchaseResponse> response = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test insufficient balance", HttpStatus.OK, StatusService.BALANCE_IS_NOT_ENOUGH, false);
        Assert.assertSame(StatusService.BALANCE_IS_NOT_ENOUGH, response.getErrorDetail().getCode());
    }

    @Test
    @Order(74)
    @DisplayName("sell-Fail-InvalidMerchantId")
    void sellFailInvalidMerchantId() throws Exception {
        log.info("start sellFailInvalidMerchantId test");
        String quantity = "1.07";
        String price = "100000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Test with invalid merchant ID
        BaseResponse<PurchaseResponse> response = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "999", walletAccountObjectOptional.getAccountNumber(), "", "test invalid merchant id", HttpStatus.OK, StatusService.MERCHANT_IS_NOT_EXIST, false);
        Assert.assertSame(StatusService.MERCHANT_IS_NOT_EXIST, response.getErrorDetail().getCode());
    }

    @Test
    @Order(75)
    @DisplayName("sell-Fail-InvalidCommissionCurrency")
    void sellFailInvalidCommissionCurrency() throws Exception {
        log.info("start sellFailInvalidCommissionCurrency test");
        String quantity = "1.07";
        String price = "100000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Test with invalid commission currency (should be same as main currency)
        BaseResponse<PurchaseResponse> response = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test invalid commission currency", HttpStatus.OK, StatusService.COMMISSION_CURRENCY_NOT_VALID, false);
        Assert.assertSame(StatusService.COMMISSION_CURRENCY_NOT_VALID, response.getErrorDetail().getCode());
    }

    @Test
    @Order(76)
    @DisplayName("sell-Fail-DuplicateRequest")
    void sellFailDuplicateRequest() throws Exception {
        log.info("start sellFailDuplicateRequest test");
        String quantity = "1.07";
        String price = "100000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("5.0"));
        
        // Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("200000"));
        
        // Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Perform first sell operation (should succeed)
        BaseResponse<PurchaseResponse> response1 = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test sell success", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response1.getData());
        
        // Try to perform the same sell operation again (should fail with duplicate)
        BaseResponse<PurchaseResponse> response2 = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test duplicate request", HttpStatus.OK, StatusService.DUPLICATE_UUID, false);
        Assert.assertSame(StatusService.DUPLICATE_UUID, response2.getErrorDetail().getCode());
    }

    @Test
    @Order(80)
    @DisplayName("inquirySell-Success")
    void inquirySellSuccess() throws Exception {
        log.info("start inquirySellSuccess test");
        String quantity = "1.07";
        String price = "100000";
        
        // Get account numbers
        WalletAccountObject goldAccountObject = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(goldAccountObject.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("2.0"));
        
        // Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("200000"));
        
        // Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, goldAccountObject.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Perform sell operation
        BaseResponse<PurchaseResponse> sellResponse = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", goldAccountObject.getAccountNumber(), "", "test sell for inquiry", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(sellResponse.getData());
        
        // Inquiry the sell operation
        BaseResponse<PurchaseTrackResponse> inquiryResponse = inquiryPurchase(mockMvc, ACCESS_TOKEN, uniqueIdentifier, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(inquiryResponse.getData());
    }

    @Test
    @Order(81)
    @DisplayName("inquirySell-Fail-InvalidUniqueIdentifier")
    void inquirySellFailInvalidUniqueIdentifier() throws Exception {
        log.info("start inquirySellFailInvalidUniqueIdentifier test");
        
        BaseResponse<PurchaseTrackResponse> response = inquiryPurchase(mockMvc, ACCESS_TOKEN, "invalid-uuid", HttpStatus.OK, StatusService.UUID_NOT_FOUND, false);
        Assert.assertSame(StatusService.UUID_NOT_FOUND, response.getErrorDetail().getCode());
    }

    // ==================== SELL LIMITATION TESTS ====================

    @Test
    @Order(90)
    @DisplayName("sell-Fail-LessThanMinQuantity")
    void sellFailLessThanMinQuantity() throws Exception {
        log.info("start sellFailLessThanMinQuantity test");
        String quantity = "0.001"; // Very small quantity below minimum
        String price = "100";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.AMOUNT_LESS_THAN_MIN, false);
        Assert.assertSame(StatusService.AMOUNT_LESS_THAN_MIN, uuidResponse.getErrorDetail().getCode());
    }

    @Test
    @Order(91)
    @DisplayName("sell-Fail-BiggerThanMaxQuantity")
    void sellFailBiggerThanMaxQuantity() throws Exception {
        log.info("start sellFailBiggerThanMaxQuantity test");
        String quantity = "1000000"; // Very large quantity above maximum
        String price = "100000000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.AMOUNT_BIGGER_THAN_MAX, false);
        Assert.assertSame(StatusService.AMOUNT_BIGGER_THAN_MAX, uuidResponse.getErrorDetail().getCode());
    }

    @Test
    @Order(92)
    @DisplayName("sell-Fail-DailyQuantityLimitation")
    void sellFailDailyQuantityLimitation() throws Exception {
        log.info("start sellFailDailyQuantityLimitation test");
        String quantity = "1.07";
        String price = "100000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("10.0"));
        
        // Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("1000000"));
        
        // Get current daily quantity limitation
        String maxDailyQuantity = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_SELL, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        
        // Set a very low daily quantity limitation to trigger the error
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_DAILY_QUANTITY_SELL).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "0.5", "test daily quantity limitation");
        
        // Generate sell UUID - should fail due to daily quantity limitation
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SELL_EXCEEDED_AMOUNT_DAILY_LIMITATION, false);
        Assert.assertSame(StatusService.SELL_EXCEEDED_AMOUNT_DAILY_LIMITATION, uuidResponse.getErrorDetail().getCode());
    }

    @Test
    @Order(93)
    @DisplayName("sell-Fail-DailyCountLimitation")
    void sellFailDailyCountLimitation() throws Exception {
        log.info("start sellFailDailyCountLimitation test");
        String quantity = "0.1";
        String price = "10000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("10.0"));
        
        // Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("1000000"));
        
        // Get current daily count limitation
        String maxDailyCount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_SELL, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        
        // Set a very low daily count limitation to trigger the error
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_DAILY_COUNT_SELL).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "1", "test daily count limitation");
        
        // Perform first sell operation (should succeed)
        BaseResponse<UuidResponse> uuidResponse1 = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier1 = uuidResponse1.getData().getUniqueIdentifier();
        BaseResponse<PurchaseResponse> response1 = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier1, quantity, price, CURRENCY_GOLD, "200", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test sell success", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response1.getData());
        
        // Try second sell operation (should fail due to daily count limitation)
        BaseResponse<UuidResponse> uuidResponse2 = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SELL_EXCEEDED_COUNT_DAILY_LIMITATION, false);
        Assert.assertSame(StatusService.SELL_EXCEEDED_COUNT_DAILY_LIMITATION, uuidResponse2.getErrorDetail().getCode());
    }

    @Test
    @Order(94)
    @DisplayName("sell-Fail-MonthlyQuantityLimitation")
    void sellFailMonthlyQuantityLimitation() throws Exception {
        log.info("start sellFailMonthlyQuantityLimitation test");
        String quantity = "1.07";
        String price = "100000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("10.0"));
        
        // Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("1000000"));
        
        // Get current monthly quantity limitation
        String maxMonthlyQuantity = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_QUANTITY_SELL, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        
        // Set a very low monthly quantity limitation to trigger the error
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_QUANTITY_SELL).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "0.5", "test monthly quantity limitation");
        
        // Generate sell UUID - should fail due to monthly quantity limitation
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.BUY_EXCEEDED_AMOUNT_MONTHLY_LIMITATION, false);
        Assert.assertSame(StatusService.BUY_EXCEEDED_AMOUNT_MONTHLY_LIMITATION, uuidResponse.getErrorDetail().getCode());
    }

    @Test
    @Order(95)
    @DisplayName("sell-Fail-MonthlyCountLimitation")
    void sellFailMonthlyCountLimitation() throws Exception {
        log.info("start sellFailMonthlyCountLimitation test");
        String quantity = "0.1";
        String price = "10000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("10.0"));
        
        // Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("1000000"));
        
        // Get current monthly count limitation
        String maxMonthlyCount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_COUNT_SELL, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        
        // Set a very low monthly count limitation to trigger the error
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_COUNT_SELL).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "1", "test monthly count limitation");
        
        // Perform first sell operation (should succeed)
        BaseResponse<UuidResponse> uuidResponse1 = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier1 = uuidResponse1.getData().getUniqueIdentifier();
        BaseResponse<PurchaseResponse> response1 = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier1, quantity, price, CURRENCY_GOLD, "200", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test sell success", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response1.getData());
        
        // Try second sell operation (should fail due to monthly count limitation)
        BaseResponse<UuidResponse> uuidResponse2 = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SELL_EXCEEDED_COUNT_MONTHLY_LIMITATION, false);
        Assert.assertSame(StatusService.SELL_EXCEEDED_COUNT_MONTHLY_LIMITATION, uuidResponse2.getErrorDetail().getCode());
    }

    @Test
    @Order(96)
    @DisplayName("sell-Success-WithinLimitations")
    void sellSuccessWithinLimitations() throws Exception {
        log.info("start sellSuccessWithinLimitations test");
        String quantity = "0.5";
        String price = "50000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        
        // Ensure user has enough GOLD to sell
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.increaseBalance(goldWalletAccountEntity.getId(), new BigDecimal("2.0"));
        
        // Ensure merchant has enough RIAL to buy
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> merchantAccounts = walletAccountService.findByWallet(walletMerchantEntity);
        WalletAccountEntity merchantRialAccount = merchantAccounts.stream()
                .filter(x -> x.getWalletAccountCurrencyEntity().getName().equals(WalletAccountCurrencyService.RIAL))
                .findFirst().orElse(null);
        walletAccountService.increaseBalance(merchantRialAccount.getId(), new BigDecimal("100000"));
        
        // Set reasonable limitations that allow this transaction
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        
        // Set minimum quantity to allow this transaction
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MIN_QUANTITY_SELL).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "0.1", "test min quantity sell");
        
        // Set maximum quantity to allow this transaction
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_QUANTITY_SELL).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "10.0", "test max quantity sell");
        
        // Set daily quantity limitation to allow this transaction
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_DAILY_QUANTITY_SELL).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "5.0", "test daily quantity sell");
        
        // Set daily count limitation to allow this transaction
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_DAILY_COUNT_SELL).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "10", "test daily count sell");
        
        // Set monthly quantity limitation to allow this transaction
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_QUANTITY_SELL).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "50.0", "test monthly quantity sell");
        
        // Set monthly count limitation to allow this transaction
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_COUNT_SELL).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "100", "test monthly count sell");
        
        // Generate sell UUID
        BaseResponse<UuidResponse> uuidResponse = generateSellUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, quantity, walletAccountObjectOptional.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Perform sell operation - should succeed within all limitations
        BaseResponse<PurchaseResponse> response = sell(mockMvc, ACCESS_TOKEN, uniqueIdentifier, quantity, price, CURRENCY_GOLD, "1000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD, "1", walletAccountObjectOptional.getAccountNumber(), "", "test sell within limitations", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
    }
} 