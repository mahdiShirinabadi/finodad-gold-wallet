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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: PhysicalCashControllerTest
 * Author: Mahdi Shirinabadi
 * Date: 1/20/2025
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
    private WalletAccountCurrencyService walletAccountCurrencyService;
    @Autowired
    private WalletLevelService walletLevelService;


    @Test
    @Order(1)
    @DisplayName("Initiate...")
    void initial() throws Exception{
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);
        log.info("start cleaning initial values in test DB");
        flyway.clean();
        flyway.migrate();
        cacheClearService.clearCache();

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);
        log.info("start cleaning initial values in test DB for purchase");
        flyway.clean();
        flyway.migrate();
        log.info("start cleaning initial values in test DB for purchase");
        cacheClearService.clearCache();

        //create wallet for channel
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

    @Test
    @Order(20)
    @DisplayName("create wallet- success")
    void createWalletSuccess() throws Exception {
        BaseResponse<CreateWalletResponse> response = createWallet(mockMvc, accessToken, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        log.info("wallet created successfully for nationalCode: {}", NATIONAL_CODE_CORRECT);
    }

    @Test
    @Order(30)
    @DisplayName("physicalCashOutGenerateUuid-success")
    void physicalCashOutGenerateUuidSuccess() throws Exception {
        log.info("start physicalCashOutGenerateUuidSuccess test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // Enable physical cash out if disabled
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        String physicalCashOutValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, accountNumber);
        if("false".equalsIgnoreCase(physicalCashOutValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test physicalCashOutGenerateUuidSuccess");
        }
        
        String quantity = "5";
        BaseResponse<UuidResponse> response = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getUniqueIdentifier());
        log.info("Physical cash out UUID generated successfully: {}", response.getData().getUniqueIdentifier());
    }

    @Test
    @Order(31)
    @DisplayName("physicalCashOutGenerateUuid-fail-invalidAccountNumber")
    void physicalCashOutGenerateUuidFailInvalidAccountNumber() throws Exception {
        log.info("start physicalCashOutGenerateUuidFailInvalidAccountNumber test");
        String quantity = "1.07";
        generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, "00000023432", HttpStatus.OK, StatusService.WALLET_ACCOUNT_NOT_FOUND, false);
    }

    @Test
    @Order(32)
    @DisplayName("physicalCashOutGenerateUuid-fail-accountNotPermission")
    void physicalCashOutGenerateUuidFailAccountNotPermission() throws Exception {
        log.info("start physicalCashOutGenerateUuidFailAccountNotPermission test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // Disable physical cash out
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "false","test physicalCashOutGenerateUuidFailAccountNotPermission");
        
        String quantity = "1.07";
        generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, accountNumber, HttpStatus.OK, StatusService.ACCOUNT_DONT_PERMISSION_FOR_PHYSICAL_CASH_OUT, false);
    }

    @Test
    @Order(40)
    @DisplayName("physicalCashOut-success")
    void physicalCashOutSuccess() throws Exception {
        log.info("start physicalCashOutSuccess test");
        
        // Step 1: Get account numbers
        WalletAccountObject goldAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        String goldAccountNumber = goldAccountObject.getAccountNumber();

        //Step 3: update balance wallet-account
        walletAccountService.increaseBalance(walletAccountService.findByAccountNumber(goldAccountNumber).getId(), new BigDecimal("5.05"));


        
        // Step 5: Enable physical cash out
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(goldAccountNumber);
        String physicalCashOutValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, goldAccountNumber);
        if("false".equalsIgnoreCase(physicalCashOutValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT).getId(), goldWalletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    goldWalletAccountEntity.getWalletAccountTypeEntity(), goldWalletAccountEntity.getWalletAccountCurrencyEntity(), goldWalletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test physicalCashOutSuccess");
        }

        String valueMaxDailyPrice = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT, goldAccountObject.getAccountNumber());
        
        // Step 6: Generate UUID for physical cash out
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT).getId(), goldWalletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                goldWalletAccountEntity.getWalletAccountTypeEntity(), goldWalletAccountEntity.getWalletAccountCurrencyEntity(), goldWalletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                "10", "change MAX_DAILY_COUNT_BUY");

        String physicalCashOutQuantity = "5.05";
        BaseResponse<UuidResponse> physicalCashOutUuidResponse = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, physicalCashOutQuantity, goldAccountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = physicalCashOutUuidResponse.getData().getUniqueIdentifier();
        
        // Step 7: Perform physical cash out
        BaseResponse<PhysicalCashOutResponse> response = physicalCashOut(mockMvc, accessToken, uniqueIdentifier, physicalCashOutQuantity, NATIONAL_CODE_CORRECT, goldAccountNumber, "", VALID_SIGN, ADDITIONAL_DATA, CURRENCY_GOLD, "0.05",CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertEquals(NATIONAL_CODE_CORRECT, response.getData().getNationalCode());
        Assert.assertEquals(goldAccountNumber, response.getData().getWalletAccountNumber());
        Assert.assertNotNull(response.getData().getBalance());
        Assert.assertEquals(uniqueIdentifier, response.getData().getUniqueIdentifier());
        log.info("Physical cash out completed successfully");

        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT).getId(), goldWalletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                goldWalletAccountEntity.getWalletAccountTypeEntity(), goldWalletAccountEntity.getWalletAccountCurrencyEntity(), goldWalletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                valueMaxDailyPrice, "change MAX_DAILY_COUNT_BUY");
    }

    @Test
    @Order(41)
    @DisplayName("physicalCashOut-fail-invalidUniqueIdentifier")
    void physicalCashOutFailInvalidUniqueIdentifier() throws Exception {
        log.info("start physicalCashOutFailInvalidUniqueIdentifier test");
        String quantity = "1.07";
        physicalCashOut(mockMvc, accessToken, "invalid_uuid", quantity, NATIONAL_CODE_CORRECT, "1234567890", "", VALID_SIGN, ADDITIONAL_DATA, CURRENCY_GOLD, "100",CURRENCY_GOLD, HttpStatus.OK, StatusService.UUID_NOT_FOUND, false);
    }

    @Test
    @Order(42)
    @DisplayName("physicalCashOut-fail-invalidCommissionCurrency")
    void physicalCashOutFailInvalidCommissionCurrency() throws Exception {
        log.info("start physicalCashOutFailInvalidCommissionCurrency test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        String accountNumber = walletAccountObject.getAccountNumber();
        
        // Enable physical cash out
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        String physicalCashOutValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, accountNumber);
        if("false".equalsIgnoreCase(physicalCashOutValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test physicalCashOutFailInvalidCommissionCurrency");
        }
        
        String quantity = "5";
        BaseResponse<UuidResponse> uuidResponse = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Test with different commission currency (should fail)
        BaseResponse<PhysicalCashOutResponse> response = physicalCashOut(mockMvc, accessToken, uniqueIdentifier, quantity, NATIONAL_CODE_CORRECT, accountNumber, "", VALID_SIGN, ADDITIONAL_DATA, CURRENCY_GOLD, "0.01","RIAL", HttpStatus.OK, StatusService.COMMISSION_CURRENCY_NOT_VALID, false);
        Assert.assertSame(StatusService.COMMISSION_CURRENCY_NOT_VALID, response.getErrorDetail().getCode());
    }

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
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test physicalCashOutFailInvalidSign");
        }
        
        String quantity = "1.07";
        BaseResponse<UuidResponse> uuidResponse = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Test with invalid sign
        BaseResponse<PhysicalCashOutResponse> response = physicalCashOut(mockMvc, accessToken, uniqueIdentifier, quantity, NATIONAL_CODE_CORRECT, accountNumber, "", "invalid_sign", ADDITIONAL_DATA, "GOLD","GOLD", "100", HttpStatus.OK, StatusService.INVALID_SIGN, false);
        Assert.assertSame(StatusService.INVALID_SIGN, response.getErrorDetail().getCode());
    }*/


    @Test
    @Order(44)
    @DisplayName("physicalCashOut-fail-balance not enough")
    void physicalCashOutFailBalanceNoEnough() throws Exception {
        log.info("start physicalCashOutFailBalanceNoEnough test");
        WalletAccountObject walletAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        String accountNumber = walletAccountObject.getAccountNumber();

        // Enable physical cash out
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        String physicalCashOutValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, accountNumber);
        if("false".equalsIgnoreCase(physicalCashOutValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test physicalCashOutFailInvalidCommissionCurrency");
        }

        String quantity = "20";
        String valueMaxDailyPrice = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT, walletAccountObject.getAccountNumber());

        // Step 6: Generate UUID for physical cash out
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                quantity, "change MAX_QUANTITY_PHYSICAL_CASH_OUT");
        BaseResponse<UuidResponse> uuidResponse = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, quantity, accountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();

        walletAccountService.decreaseBalance(walletAccountEntity.getId(), new BigDecimal(quantity));

        // Test with different commission currency (should fail)
        BaseResponse<PhysicalCashOutResponse> response = physicalCashOut(mockMvc, accessToken, uniqueIdentifier, quantity, NATIONAL_CODE_CORRECT, accountNumber, "", VALID_SIGN, ADDITIONAL_DATA, CURRENCY_GOLD, "0.01","GOLD", HttpStatus.OK, StatusService.BALANCE_IS_NOT_ENOUGH, false);
        Assert.assertSame(StatusService.BALANCE_IS_NOT_ENOUGH, response.getErrorDetail().getCode());
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                valueMaxDailyPrice, "change MAX_QUANTITY_PHYSICAL_CASH_OUT");
    }

    @Test
    @Order(50)
    @DisplayName("physicalInquiryCashOut-success")
    void physicalInquiryCashOutSuccess() throws Exception {
        log.info("start physicalInquiryCashOutSuccess test");
        
        // First perform a successful physical cash out to have data to inquiry
        WalletAccountObject goldAccountObject = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.GOLD);
        String goldAccountNumber = goldAccountObject.getAccountNumber();
        
        // Charge RIAL account
        walletAccountService.increaseBalance(walletAccountService.findByAccountNumber(goldAccountNumber).getId(), new BigDecimal("10.01"));
        
        // Enable physical cash out
        WalletAccountEntity goldWalletAccountEntity = walletAccountService.findByAccountNumber(goldAccountNumber);
        String physicalCashOutValue = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, goldAccountNumber);
        if("false".equalsIgnoreCase(physicalCashOutValue)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT).getId(), goldWalletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    goldWalletAccountEntity.getWalletAccountTypeEntity(), goldWalletAccountEntity.getWalletAccountCurrencyEntity(), goldWalletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test physicalInquiryCashOutSuccess");
        }

        // Generate UUID and perform physical cash out
        String physicalCashOutQuantity = "10.01";
        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT).getId(), goldWalletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                goldWalletAccountEntity.getWalletAccountTypeEntity(), goldWalletAccountEntity.getWalletAccountCurrencyEntity(), goldWalletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                physicalCashOutQuantity, "change MAX_DAILY_COUNT_BUY");
        BaseResponse<UuidResponse> physicalCashOutUuidResponse = generatePhysicalCashOutUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, physicalCashOutQuantity, goldAccountNumber, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String uniqueIdentifier = physicalCashOutUuidResponse.getData().getUniqueIdentifier();
        BaseResponse<PhysicalCashOutResponse> physicalCashOutResponse = physicalCashOut(mockMvc, accessToken, uniqueIdentifier, physicalCashOutQuantity, NATIONAL_CODE_CORRECT, goldAccountNumber, "", VALID_SIGN, ADDITIONAL_DATA, CURRENCY_GOLD,"0.01", CURRENCY_GOLD, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(physicalCashOutResponse.getData());
        
        // Now perform inquiry
        BaseResponse<PhysicalCashOutTrackResponse> response = physicalInquiryCashOut(mockMvc, accessToken, uniqueIdentifier, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        log.info("Physical cash out inquiry completed successfully");
    }

    @Test
    @Order(51)
    @DisplayName("physicalInquiryCashOut-fail-invalidUniqueIdentifier")
    void physicalInquiryCashOutFailInvalidUniqueIdentifier() throws Exception {
        log.info("start physicalInquiryCashOutFailInvalidUniqueIdentifier test");
        physicalInquiryCashOut(mockMvc, accessToken, "invalid_uuid", HttpStatus.OK, StatusService.UUID_NOT_FOUND, false);
    }
} 