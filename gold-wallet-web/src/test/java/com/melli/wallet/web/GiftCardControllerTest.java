package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.GiftCardEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.giftcard.GiftCardResponse;
import com.melli.wallet.domain.response.giftcard.GiftCardUuidResponse;
import com.melli.wallet.domain.response.giftcard.GiftCardTrackResponse;
import com.melli.wallet.domain.request.wallet.CommissionObject;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.service.repository.*;
import com.melli.wallet.sync.ResourceSyncService;
import com.melli.wallet.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ObjectUtils;
import org.flywaydb.core.Flyway;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: GiftCardControllerTest
 * Author: Mahdi Shirinabadi
 * Date: 1/18/2025
 * This test class contains comprehensive end-to-end tests for Gift Card operations.
 * It tests the complete flow from wallet creation to gift card generation and validation.
 * Test Coverage:
 * - Wallet creation and setup
 * - Gift Card UUID generation (success and failure scenarios)
 * - Amount validation (minimum, maximum limits)
 * - Currency validation
 * - Account validation
 * - Security validation (signature)
 * - UUID validation and mismatch scenarios
 */
@Log4j2
@DisplayName("GiftCardControllerTest End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GiftCardControllerTest extends WalletApplicationTests {

    private static final String NATIONAL_CODE_CORRECT = "0077847660";
    private static final String NATIONAL_CODE_DEST = "2980511481";
    private static final String MOBILE_CORRECT = "09124162337";
    private static final String CURRENCY_GOLD = "GOLD";

    private static MockMvc mockMvc;
    private static String accessToken;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private WalletAccountRepositoryService walletAccountRepositoryService;
    @Autowired
    private LimitationGeneralCustomRepositoryService limitationGeneralCustomRepositoryService;
    @Autowired
    private ChannelRepositoryService channelRepositoryService;
    @Autowired
    private CacheClearService cacheClearService;
    @Autowired
    private Flyway flyway;
    @Autowired
    private ResourceSyncService resourceSyncService;
    @Autowired
    private WalletTypeRepositoryService walletTypeRepositoryService;
    @Autowired
    private WalletLevelRepositoryService walletLevelRepositoryService;
    @Autowired
    private WalletRepositoryService walletRepositoryService;
    @Autowired
    private GiftCardRepositoryService giftCardRepositoryService;

    @Test
    @Order(1)
    @DisplayName("Initiate...")
    void initial() throws Exception {
        // Setup MockMvc for testing with security
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);

        // Clean and migrate database
        log.info("start cleaning initial values in test DB");
        flyway.clean();
        flyway.migrate();
        resourceSyncService.syncResourcesOnStartup();

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

        // Clear all caches
        cacheClearService.clearCache();
    }

    @Test
    @Order(2)
    @DisplayName("channel login successfully")
    void channelLoginSuccessfully() throws Exception {
        BaseResponse<LoginResponse> response = login(mockMvc, USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        accessToken = response.getData().getAccessTokenObject().getToken();
    }

    @Test
    @Order(3)
    @DisplayName("create wallet- success")
    void createWalletSuccess() throws Exception {
        BaseResponse<CreateWalletResponse> response = createWallet(mockMvc, accessToken, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getWalletId());
    }

    @Test
    @Order(4)
    @DisplayName("create destination wallet- success")
    void createDestinationWalletSuccess() throws Exception {
        BaseResponse<CreateWalletResponse> response = createWallet(mockMvc, accessToken, NATIONAL_CODE_DEST, MOBILE_CORRECT, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getWalletId());
    }

    // ==================== GENERATE UUID TESTS ====================

    @Test
    @Order(10)
    @DisplayName("generateGiftCardUuid-Success")
    void generateGiftCardUuidSuccess() throws Exception {
        // Define amounts
        String giftCardQuantity = "0.001";

        // Step 1: Get source and destination account numbers
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_GIFT_CARD, sourceAccount.getAccountNumber());
        if ("false".equalsIgnoreCase(value)) {
            WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_GIFT_CARD, walletAccountEntity, "true");
        }


        // Step 2: Generate Gift Card UUID successfully
        BaseResponse<GiftCardUuidResponse> response = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getUniqueIdentifier());
    }

    @Test
    @Order(11)
    @DisplayName("generateGiftCardUuid-Fail-InvalidNationalCode")
    void generateGiftCardUuidFailInvalidNationalCode() throws Exception {
        // Define amounts
        String giftCardQuantity = "0.001";
        String invalidNationalCode = "INVALID_NATIONAL_CODE";

        // Step 1: Get account numbers
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_GIFT_CARD, sourceAccount.getAccountNumber());
        if ("false".equalsIgnoreCase(value)) {
            WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
            setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_GIFT_CARD, walletAccountEntity, "true");
        }

        // Step 2: Try to generate UUID with invalid national code
        BaseResponse<GiftCardUuidResponse> response = generateGiftCardUuid(mockMvc, accessToken, invalidNationalCode, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);

        Assert.assertNull(response.getData());
        Assert.assertSame(StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, response.getErrorDetail().getCode());
    }

    @Test
    @Order(12)
    @DisplayName("generateGiftCardUuid-Fail-InvalidQuantity")
    void generateGiftCardUuidFailInvalidQuantity() throws Exception {
        // Define amounts
        String invalidQuantity = "-0.001";

        // Step 1: Get account numbers
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        // Step 2: Try to generate UUID with invalid quantity (negative)
        BaseResponse<GiftCardUuidResponse> response = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, invalidQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);

        Assert.assertNull(response.getData());
        Assert.assertSame(StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, response.getErrorDetail().getCode());
    }

    @Test
    @Order(13)
    @DisplayName("generateGiftCardUuid-Fail-InvalidSourceAccount")
    void generateGiftCardUuidFailInvalidSourceAccount() throws Exception {
        // Define amounts
        String giftCardQuantity = "0.001";
        String invalidAccount = "INVALID_ACCOUNT";

        // Step 2: Try to generate UUID with invalid source account
        BaseResponse<GiftCardUuidResponse> response = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, invalidAccount, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, false);

        Assert.assertNull(response.getData());
        Assert.assertSame(StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, response.getErrorDetail().getCode());
    }

    @Test
    @Order(14)
    @DisplayName("generateGiftCardUuid-Fail-InvalidCurrency")
    void generateGiftCardUuidFailInvalidCurrency() throws Exception {
        // Define amounts
        String giftCardQuantity = "0.001";
        String invalidCurrency = "INVALID_CURRENCY";

        // Step 1: Get source account number
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        // Step 2: Try to generate UUID with invalid currency
        BaseResponse<GiftCardUuidResponse> response = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), invalidCurrency, HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);

        Assert.assertNull(response.getData());
        Assert.assertSame(StatusRepositoryService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, response.getErrorDetail().getCode());
    }

    @Test
    @Order(15)
    @DisplayName("generateGiftCardUuid-Fail-BiggerThanMax")
    void generateGiftCardUuidFailInsufficientBalance() throws Exception {
        // Define amounts


        // Step 1: Get account number
        WalletAccountObject account = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);


        String max = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_GIFT_CARD, account.getAccountNumber());
        String largeQuantity = new BigDecimal(max).add(new BigDecimal("0.01")).toString();
        // Step 2: Try to generate UUID with insufficient balance (large amount)
        BaseResponse<GiftCardUuidResponse> response = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, largeQuantity, account.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.QUANTITY_BIGGER_THAN_MAX, false);

        Assert.assertNull(response.getData());
        Assert.assertSame(StatusRepositoryService.QUANTITY_BIGGER_THAN_MAX, response.getErrorDetail().getCode());
    }

    // ==================== PROCESS TESTS ====================

    @Test
    @Order(20)
    @DisplayName("processGiftCard-Success")
    void processGiftCardSuccess() throws Exception {
        // Define amounts and commissions
        String chargeAmount = "100";
        String giftCardQuantity = "0.002";
        String commissionAmount = "0.001";
        String additionalData = "test data";

        // Step 1: Setup accounts and balances
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        // Step 2: Charge source account
        chargeAccountForGiftCard(sourceAccount.getAccountNumber(), chargeAmount);

        // Step 3: Generate UUID
        BaseResponse<GiftCardUuidResponse> uuidResponse = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        // Step 4: Process Gift Card transaction
        CommissionObject commission = new CommissionObject("GOLD", commissionAmount);
        BaseResponse<GiftCardResponse> response = processGiftCard(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), giftCardQuantity, NATIONAL_CODE_CORRECT, sourceAccount.getAccountNumber(), NATIONAL_CODE_DEST, additionalData,
                generateValidSign(uuidResponse.getData().getUniqueIdentifier() + "|" + giftCardQuantity + "|" + NATIONAL_CODE_CORRECT), commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        Assert.assertNotNull(response);
        Assert.assertTrue(response.getSuccess());
        chargeAccountForGiftCardToZero(sourceAccount.getAccountNumber());
    }

    @Test
    @Order(21)
    @DisplayName("processGiftCard-Fail-InvalidUUID")
    void processGiftCardFailInvalidUUID() throws Exception {
        // Define amounts and commissions
        String invalidUUID = "INVALID_UUID";
        String giftCardQuantity = "1";
        String commissionAmount = "0.01";
        String additionalData = "test data";

        // Step 1: Get account numbers
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        // Step 2: Try to process with invalid UUID
        CommissionObject commission = new CommissionObject("GOLD", commissionAmount);
        BaseResponse<GiftCardResponse> response = processGiftCard(mockMvc, accessToken, invalidUUID, giftCardQuantity, NATIONAL_CODE_CORRECT, sourceAccount.getAccountNumber(), NATIONAL_CODE_DEST, additionalData, generateValidSign(invalidUUID + "|" + giftCardQuantity + "|" + NATIONAL_CODE_CORRECT), commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.UUID_NOT_FOUND, false);

        Assert.assertFalse(response.getSuccess());
        Assert.assertSame(StatusRepositoryService.UUID_NOT_FOUND, response.getErrorDetail().getCode());
    }

    @Test
    @Order(22)
    @DisplayName("processGiftCard-Fail-InsufficientBalance")
    void processGiftCardFailInsufficientBalance() throws Exception {
        // Define amounts and commissions
        String giftCardQuantity = "0.05";
        String commissionAmount = "0.001";
        String additionalData = "test data";

        // Step 1: Get account numbers
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        // Step 2: Generate UUID
        BaseResponse<GiftCardUuidResponse> uuidResponse = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, sourceAccount.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountEntity, String.valueOf(1));



        // Step 3: Try to process without sufficient balance
        CommissionObject commission = new CommissionObject("GOLD", commissionAmount);
        BaseResponse<GiftCardResponse> response = processGiftCard(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), giftCardQuantity, NATIONAL_CODE_CORRECT, sourceAccount.getAccountNumber(), NATIONAL_CODE_DEST, additionalData, generateValidSign(uuidResponse.getData().getUniqueIdentifier() + "|" + giftCardQuantity + "|" + NATIONAL_CODE_CORRECT), commission, CURRENCY_GOLD, HttpStatus.OK,
                StatusRepositoryService.EXCEEDED_COUNT_DAILY_LIMITATION, false);
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountEntity, String.valueOf(Long.parseLong(value)));
        Assert.assertFalse(response.getSuccess());
        Assert.assertSame(StatusRepositoryService.EXCEEDED_COUNT_DAILY_LIMITATION, response.getErrorDetail().getCode());
    }

    /*@Test
    @Order(23)
    @DisplayName("processGiftCard-Fail-InvalidSign")
    void processGiftCardFailInvalidSign() throws Exception {
        // Define amounts and commissions
        String chargeAmount = "10";
        String giftCardQuantity = "0.001";
        String commissionAmount = "0.001";
        String additionalData = "test data";
        String invalidSign = "INVALID_SIGN";
        
        // Step 1: Setup accounts and balances
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        
        chargeAccountForGiftCard(sourceAccount.getAccountNumber(), chargeAmount);
        
        BaseResponse<GiftCardUuidResponse> uuidResponse = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        
        CommissionObject commission = new CommissionObject("GOLD", commissionAmount);
        processGiftCard(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), NATIONAL_CODE_DEST, additionalData, invalidSign, commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.INVALID_SIGN, false);
        
        chargeAccountForGiftCardToZero(sourceAccount.getAccountNumber());
    }*/

    // ==================== INQUIRY TESTS ====================

    @Test
    @Order(30)
    @DisplayName("giftCardInquiry-Success")
    void giftCardInquirySuccess() throws Exception {
        // Define amounts and commissions
        String chargeAmount = "10";
        String giftCardQuantity = "0.002";
        String commissionAmount = "0.001";
        String additionalData = "test data";

        // Step 1: Setup and process a Gift Card transaction
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        chargeAccountForGiftCard(sourceAccount.getAccountNumber(), chargeAmount);

        BaseResponse<GiftCardUuidResponse> uuidResponse = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, sourceAccount.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountEntity, String.valueOf(Long.parseLong(value)+ 100));


        CommissionObject commission = new CommissionObject("GOLD", commissionAmount);
        processGiftCard(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), giftCardQuantity, NATIONAL_CODE_CORRECT, sourceAccount.getAccountNumber(), NATIONAL_CODE_DEST, additionalData, generateValidSign(uuidResponse.getData().getUniqueIdentifier() + "|" + giftCardQuantity + "|" + NATIONAL_CODE_CORRECT), commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        // Step 2: Inquiry the transaction
        BaseResponse<GiftCardTrackResponse> response = inquiryGiftCard(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        Assert.assertNotNull(response.getData());
        Assert.assertEquals(uuidResponse.getData().getUniqueIdentifier(), response.getData().getUniqueIdentifier());
        Assert.assertEquals(NATIONAL_CODE_CORRECT, response.getData().getNationalCode());
        Assert.assertSame(new BigDecimal(giftCardQuantity).compareTo(new BigDecimal(response.getData().getQuantity())), 0);
        chargeAccountForGiftCardToZero(sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountEntity, String.valueOf(Long.parseLong(value)));
    }

    @Test
    @Order(31)
    @DisplayName("giftCardInquiry-Fail-InvalidUUID")
    void giftCardInquiryFailInvalidUUID() throws Exception {
        // Define amounts
        String invalidUUID = "INVALID_UUID";

        // Step 1: Try to inquiry with invalid UUID
        BaseResponse<GiftCardTrackResponse> response = inquiryGiftCard(mockMvc, accessToken, invalidUUID, HttpStatus.OK, StatusRepositoryService.UUID_NOT_FOUND, false);

        Assert.assertNull(response.getData());
        Assert.assertSame(StatusRepositoryService.UUID_NOT_FOUND, response.getErrorDetail().getCode());
    }

    // ==================== PAYMENT TESTS ====================

    @Test
    @Order(40)
    @DisplayName("paymentGiftCard-Success")
    void paymentGiftCardSuccess() throws Exception {
        // Define amounts and commissions
        String chargeAmount = "10";
        String giftCardQuantity = "0.002";
        String commissionAmount = "0.001";
        String additionalData = "test data";
        String paymentAdditionalData = "payment test";

        // Step 1: Setup and process a Gift Card transaction first
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        chargeAccountForGiftCard(sourceAccount.getAccountNumber(), chargeAmount);

        BaseResponse<GiftCardUuidResponse> uuidResponse = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        CommissionObject commission = new CommissionObject("GOLD", commissionAmount);
        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, sourceAccount.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountEntity, String.valueOf(Long.parseLong(value)+ 100));

        BaseResponse<GiftCardResponse> responseProcess = processGiftCard(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), giftCardQuantity, NATIONAL_CODE_CORRECT, sourceAccount.getAccountNumber(), NATIONAL_CODE_DEST, additionalData, generateValidSign(uuidResponse.getData().getUniqueIdentifier() + "|" + giftCardQuantity + "|" + NATIONAL_CODE_CORRECT), commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        // Step 2: Payment Gift Card successfully
        BaseResponse<ObjectUtils.Null> response = paymentGiftCard(mockMvc, accessToken, responseProcess.getData().getActiveCode(), responseProcess.getData().getQuantity(), CURRENCY_GOLD, NATIONAL_CODE_DEST, destAccount.getAccountNumber(), paymentAdditionalData, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        Assert.assertNotNull(response);
        Assert.assertTrue(response.getSuccess());
        chargeAccountForGiftCardToZero(sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountEntity, String.valueOf(Long.parseLong(value)));
    }

    @Test
    @Order(41)
    @DisplayName("paymentGiftCard-Fail-InvalidGiftCard")
    void paymentGiftCardFailInvalidGiftCard() throws Exception {
        // Define amounts
        String invalidGiftCard = "INVALID_GIFT_CARD";
        String giftCardQuantity = "0.002";
        String additionalData = "payment test";

        // Step 1: Get account number
        WalletAccountObject account = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        // Step 2: Try to payment with invalid gift card code
        BaseResponse<ObjectUtils.Null> response = paymentGiftCard(mockMvc, accessToken, invalidGiftCard, giftCardQuantity, CURRENCY_GOLD, NATIONAL_CODE_DEST, account.getAccountNumber(), additionalData, HttpStatus.OK, StatusRepositoryService.GIFT_CARD_NOT_FOUND, false);

        Assert.assertFalse(response.getSuccess());
        Assert.assertSame(StatusRepositoryService.GIFT_CARD_NOT_FOUND, response.getErrorDetail().getCode());
    }

    @Test
    @Order(42)
    @DisplayName("paymentGiftCard-Fail-InvalidAccount")
    void paymentGiftCardFailInvalidAccount() throws Exception {
        // Define amounts
        String validGiftCard = "VALID_GIFT_CARD";
        String giftCardQuantity = "0.002";
        String invalidAccount = "INVALID_ACCOUNT";
        String additionalData = "payment test";

        // Step 1: Try to payment with invalid account number
        BaseResponse<ObjectUtils.Null> response = paymentGiftCard(mockMvc, accessToken, validGiftCard, giftCardQuantity, CURRENCY_GOLD, NATIONAL_CODE_DEST, invalidAccount, additionalData, HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, false);

        Assert.assertFalse(response.getSuccess());
        Assert.assertSame(StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, response.getErrorDetail().getCode());
    }

    @Test
    @Order(43)
    @DisplayName("paymentGiftCard-Fail-ExpiredGiftCard")
    void paymentGiftCardFailExpiredGiftCard() throws Exception {
        // Define amounts
        // Define amounts and commissions
        String chargeAmount = "10";
        String giftCardQuantity = "0.002";
        String commissionAmount = "0.001";
        String additionalData = "test data";
        String paymentAdditionalData = "payment test";

        // Step 1: Setup and process a Gift Card transaction first
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        chargeAccountForGiftCard(sourceAccount.getAccountNumber(), chargeAmount);

        BaseResponse<GiftCardUuidResponse> uuidResponse = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        CommissionObject commission = new CommissionObject("GOLD", commissionAmount);
        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, sourceAccount.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountEntity, String.valueOf(Long.parseLong(value)+ 100));

        BaseResponse<GiftCardResponse> responseProcess = processGiftCard(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), giftCardQuantity, NATIONAL_CODE_CORRECT, sourceAccount.getAccountNumber(), NATIONAL_CODE_DEST, additionalData, generateValidSign(uuidResponse.getData().getUniqueIdentifier() + "|" + giftCardQuantity + "|" + NATIONAL_CODE_CORRECT), commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        Optional<GiftCardEntity> giftCardEntity = giftCardRepositoryService.findByUniqueCode(responseProcess.getData().getActiveCode());
        Assert.assertTrue(giftCardEntity.isPresent());
        giftCardEntity.get().setExpireAt(DateUtils.getNPreviousDay(new Date(), 1));
        giftCardRepositoryService.save(giftCardEntity.get());

        // Step 2: Payment Gift Card successfully
        BaseResponse<ObjectUtils.Null> response = paymentGiftCard(mockMvc, accessToken, responseProcess.getData().getActiveCode(), responseProcess.getData().getQuantity(), CURRENCY_GOLD, NATIONAL_CODE_DEST, destAccount.getAccountNumber(), paymentAdditionalData, HttpStatus.OK, StatusRepositoryService.GIFT_CARD_IS_EXPIRE, false);

        Assert.assertNotNull(response);
        Assert.assertFalse(response.getSuccess());
        chargeAccountForGiftCardToZero(sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountEntity, String.valueOf(Long.parseLong(value)));

    }

    @Test
    @Order(44)
    @DisplayName("paymentGiftCard-Fail-AccountNoPermissionForGiftCard")
    void paymentGiftCardFailAccountNoPermissionForGiftCard() throws Exception {
        // Define amounts
        String giftCardQuantity = "0.002";
        // Step 1: Get account number
        WalletAccountObject account = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(account.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_GIFT_CARD, walletAccountEntity, "false");
        BaseResponse<GiftCardUuidResponse> uuidResponse = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_DEST, giftCardQuantity, account.getAccountNumber(),
                CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.ACCOUNT_DONT_PERMISSION_FOR_GIFT_CARD, false);
        Assert.assertFalse(uuidResponse.getSuccess());
        Assert.assertSame(StatusRepositoryService.ACCOUNT_DONT_PERMISSION_FOR_GIFT_CARD, uuidResponse.getErrorDetail().getCode());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_GIFT_CARD, walletAccountEntity, "true");
    }

    @Test
    @Order(45)
    @DisplayName("paymentGiftCard-Fail-NationalCodeNoPermissionForPaymentGiftCard")
    void paymentGiftCardFailNationalCodeNoPermissionForPaymentGiftCard() throws Exception {
        // Define amounts
        String validGiftCard = "VALID_GIFT_CARD";
        String giftCardQuantity = "0.002";
        String invalidNationalCode = "INVALID_NATIONAL_CODE";
        String additionalData = "payment test";

        // Step 1: Get account number
        WalletAccountObject account = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        // Step 2: Try to payment with national code that doesn't have gift card payment permission
        BaseResponse<ObjectUtils.Null> response = paymentGiftCard(mockMvc, accessToken, validGiftCard, giftCardQuantity, CURRENCY_GOLD, invalidNationalCode, account.getAccountNumber(),
                additionalData, HttpStatus.OK, StatusRepositoryService.WALLET_NOT_FOUND, false);

        Assert.assertFalse(response.getSuccess());
        Assert.assertSame(StatusRepositoryService.WALLET_NOT_FOUND, response.getErrorDetail().getCode());
    }

    // ==================== CONCURRENCY TESTS ====================

    @Test
    @Order(50)
    @DisplayName("generateGiftCardUuid-ConcurrentGeneration")
    void generateGiftCardUuidConcurrentGeneration() throws Exception {
        // Define amounts
        String chargeAmount = "0.01";
        String giftCardQuantity = "0.003";
        int threadCount = 3;

        // Step 1: Setup accounts and balances
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        // Step 2: Charge source account with enough balance for multiple transactions
        chargeAccountForGiftCard(sourceAccount.getAccountNumber(), chargeAmount);

        // Step 3: Generate multiple UUIDs concurrently
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentLinkedQueue<BaseResponse<GiftCardUuidResponse>> results = new ConcurrentLinkedQueue<>();

        executor.submit(() -> {
            try {
                BaseResponse<GiftCardUuidResponse> response = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                results.add(response);
            } catch (Exception e) {
                log.error("Error in concurrent generation 1", e);
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                BaseResponse<GiftCardUuidResponse> response = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                results.add(response);
            } catch (Exception e) {
                log.error("Error in concurrent generation 2", e);
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                BaseResponse<GiftCardUuidResponse> response = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                results.add(response);
            } catch (Exception e) {
                log.error("Error in concurrent generation 3", e);
            } finally {
                latch.countDown();
            }
        });

        // Step 4: Wait for all generations to complete
        Assert.assertTrue("Concurrent generations should complete within 5 seconds", latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        // Step 5: Verify all generations succeeded
        Assert.assertEquals("All concurrent generations should complete", threadCount, results.size());
        for (BaseResponse<GiftCardUuidResponse> response : results) {
            Assert.assertTrue("Concurrent generation should succeed", response.getSuccess());
            Assert.assertNotNull("UUID should be generated", response.getData().getUniqueIdentifier());
        }

        chargeAccountForGiftCardToZero(sourceAccount.getAccountNumber());
    }

    @Test
    @Order(51)
    @DisplayName("paymentGiftCard-ConcurrentPayment")
    void paymentGiftCardConcurrentPayment() throws Exception {
        // Define amounts and commissions
        String chargeAmount = "0.1";
        String giftCardQuantity = "0.003";
        String commissionAmount = "0.001";
        String additionalData = "test data";
        int giftCardCount = 3;
        int threadCount = 3;



        // Step 1: Setup and process multiple Gift Card transactions first
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber());
        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountEntity, "100");

        chargeAccountForGiftCard(sourceAccount.getAccountNumber(), chargeAmount);

        // Step 2: Generate and process multiple gift cards
        List<String> giftCardCodes = new ArrayList<>();
        for (int i = 0; i < giftCardCount; i++) {
            BaseResponse<GiftCardUuidResponse> uuidResponse = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

            CommissionObject commission = new CommissionObject("GOLD", commissionAmount);
            BaseResponse<GiftCardResponse> giftCardResponseBaseResponse = processGiftCard(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), giftCardQuantity, NATIONAL_CODE_CORRECT, sourceAccount.getAccountNumber(), NATIONAL_CODE_DEST, additionalData,
                    generateValidSign(uuidResponse.getData().getUniqueIdentifier() + "|" + giftCardQuantity + "|" + NATIONAL_CODE_CORRECT), commission, CURRENCY_GOLD, HttpStatus.OK,
                    StatusRepositoryService.SUCCESSFUL, true);

            giftCardCodes.add(giftCardResponseBaseResponse.getData().getActiveCode());
        }

        // Step 3: Process payments concurrently
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentLinkedQueue<BaseResponse<ObjectUtils.Null>> results = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    BaseResponse<ObjectUtils.Null> response = paymentGiftCard(mockMvc, accessToken, giftCardCodes.get(index), giftCardQuantity, CURRENCY_GOLD, NATIONAL_CODE_DEST, destAccount.getAccountNumber(), "concurrent payment test " + index, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                    results.add(response);
                } catch (Exception e) {
                    log.error("Error in concurrent payment " + index, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Step 4: Wait for all payments to complete
        Assert.assertTrue("Concurrent payments should complete within 5 seconds", latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        // Step 5: Verify all payments succeeded
        Assert.assertEquals("All concurrent payments should complete", threadCount, results.size());
        for (BaseResponse<ObjectUtils.Null> response : results) {
            Assert.assertTrue("Concurrent payment should succeed", response.getSuccess());
        }

        chargeAccountForGiftCardToZero(sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountEntity, value);
    }

    @Test
    @Order(52)
    @DisplayName("generateGiftCardUuid-ConcurrentGenerationWithInsufficientBalance")
    void generateGiftCardUuidConcurrentGenerationWithInsufficientBalance() throws Exception {
        // Define amounts
        String limitedChargeAmount = "0.05";
        String giftCardQuantity = "0.03";
        CommissionObject commission = new CommissionObject("GOLD","0.001");
        int threadCount = 3;

        // Step 1: Setup accounts with limited balance
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()), "100");

        // Step 2: Charge with limited balance (only enough for 2 transactions)
        chargeAccountForGiftCard(sourceAccount.getAccountNumber(), limitedChargeAmount);

        // Step 3: Try to generate multiple UUIDs concurrently (more than balance allows)
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentLinkedQueue<BaseResponse<GiftCardResponse>> results = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    BaseResponse<GiftCardUuidResponse> uuidResponse = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                    BaseResponse<GiftCardResponse> giftCardResponseBaseResponse = processGiftCard(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), giftCardQuantity, NATIONAL_CODE_CORRECT, sourceAccount.getAccountNumber(), NATIONAL_CODE_DEST, "count",
                            generateValidSign(uuidResponse.getData().getUniqueIdentifier() + "|" + giftCardQuantity + "|" + NATIONAL_CODE_CORRECT), commission, CURRENCY_GOLD, HttpStatus.OK,
                            StatusRepositoryService.SUCCESSFUL, true);
                    results.add(giftCardResponseBaseResponse);
                } catch (Exception e) {
                    log.error("Error in concurrent generation with insufficient balance", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Step 4: Wait for all generations to complete
        Assert.assertTrue("Concurrent generations should complete within 5 seconds", latch.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        // Step 5: Verify some generations succeeded and some failed due to insufficient balance
        Assert.assertFalse("Some generations should complete", results.isEmpty());
        Assert.assertTrue("Not all generations should succeed due to insufficient balance", results.size() < threadCount);

        for (BaseResponse<GiftCardResponse> response : results) {
            if (response.getSuccess()) {
                Assert.assertNotNull("UUID should be generated for successful responses", response.getData().getActiveCode());
            }
        }

        chargeAccountForGiftCardToZero(sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()), value);
    }

    @Test
    @Order(53)
    @DisplayName("processGiftCard-ConcurrentSameUUID")
    void processGiftCardConcurrentSameUUID() throws Exception {
        // Define amounts and commissions
        String chargeAmount = "10";
        String giftCardQuantity = "0.002";
        String commissionAmount = "0.001";
        String additionalData1 = "concurrent test 1";
        String additionalData2 = "concurrent test 2";
        int threadCount = 2;

        // Step 1: Setup accounts and balances
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        // Step 2: Charge source account
        chargeAccountForGiftCard(sourceAccount.getAccountNumber(), chargeAmount);

        // Step 3: Generate UUID
        BaseResponse<GiftCardUuidResponse> uuidResponse = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();

        // Step 4: Process same UUID concurrently (should fail for second request)
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentLinkedQueue<BaseResponse<GiftCardResponse>> results = new ConcurrentLinkedQueue<>();

        CommissionObject commission = new CommissionObject("GOLD", commissionAmount);
        String sign = generateValidSign(uniqueIdentifier + "|" + giftCardQuantity + "|" + NATIONAL_CODE_CORRECT);

        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()), "100");


        // First concurrent request
        executor.submit(() -> {
            try {
                BaseResponse<GiftCardResponse> response = processGiftCard(mockMvc, accessToken, uniqueIdentifier, giftCardQuantity, NATIONAL_CODE_CORRECT, sourceAccount.getAccountNumber(), NATIONAL_CODE_DEST, additionalData1, sign, commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                results.add(response);
                log.info("First concurrent process completed with success: {}", response.getSuccess());
            } catch (Exception e) {
                log.error("Error in concurrent process 1", e);
            } finally {
                latch.countDown();
            }
        });


        // Second concurrent request with same UUID (should fail) - add small delay
        executor.submit(() -> {
            try {
                // Small delay to ensure second request starts after first
                Thread.sleep(100);
                BaseResponse<GiftCardResponse> response = processGiftCard(mockMvc, accessToken, uniqueIdentifier, giftCardQuantity, NATIONAL_CODE_CORRECT, sourceAccount.getAccountNumber(), NATIONAL_CODE_DEST, additionalData2, sign, commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.DUPLICATE_UUID, false);
                results.add(response);
                log.info("Second concurrent process completed with success: {}", response.getSuccess());
            } catch (Exception e) {
                log.error("Error in concurrent process 2", e);
            } finally {
                latch.countDown();
            }
        });

        // Step 5: Wait for both requests to complete
        Assert.assertTrue("Concurrent process requests should complete within 10 seconds", latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();


        // Step 6: Verify results - one success, one failure
        Assert.assertEquals("Both concurrent requests should complete", threadCount, results.size());



        boolean hasSuccess = false;
        boolean hasFailure = false;

        for (BaseResponse<GiftCardResponse> response : results) {
            if (response.getSuccess()) {
                hasSuccess = true;
            } else {
                hasFailure = true;
                Assert.assertSame("Second request should fail with duplicate UUID", StatusRepositoryService.DUPLICATE_UUID, response.getErrorDetail().getCode());
            }
        }

        Assert.assertTrue("First request should succeed", hasSuccess);
        Assert.assertTrue("Second request should fail due to duplicate UUID", hasFailure);

        chargeAccountForGiftCardToZero(sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()), value);
    }

    @Test
    @Order(54)
    @DisplayName("paymentGiftCard-ConcurrentSameGiftCardCode")
    void paymentGiftCardConcurrentSameGiftCardCode() throws Exception {
        // Step 1: Setup and process a Gift Card transaction first
        WalletAccountObject sourceAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_CORRECT, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);
        WalletAccountObject destAccount = getAccountNumber(mockMvc, accessToken, NATIONAL_CODE_DEST, WalletAccountTypeRepositoryService.NORMAL, CURRENCY_GOLD);

        // Define amounts and commissions
        String chargeAmount = "10";
        String giftCardQuantity = "0.002";
        String commissionAmount = "0.001";
        String additionalData = "test data";
        String paymentAdditionalData1 = "concurrent payment test 1";
        String paymentAdditionalData2 = "concurrent payment test 2";
        int threadCount = 2;

        chargeAccountForGiftCard(sourceAccount.getAccountNumber(), chargeAmount);
        String value = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, sourceAccount.getAccountNumber());
        String valueCountPayment = getLimitationSettingValue(walletAccountRepositoryService, limitationGeneralCustomRepositoryService, channelRepositoryService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_PAYMENT_GIFT_CARD, sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()), "100");
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_PAYMENT_GIFT_CARD, walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()), "100");
        // Step 2: Generate and process gift card to get a valid gift card code
        BaseResponse<GiftCardUuidResponse> uuidResponse = generateGiftCardUuid(mockMvc, accessToken, NATIONAL_CODE_CORRECT, giftCardQuantity, sourceAccount.getAccountNumber(), CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        CommissionObject commission = new CommissionObject("GOLD", commissionAmount);
        BaseResponse<GiftCardResponse> giftCardResponseBaseResponse = processGiftCard(mockMvc, accessToken, uuidResponse.getData().getUniqueIdentifier(), giftCardQuantity, NATIONAL_CODE_CORRECT, sourceAccount.getAccountNumber(), NATIONAL_CODE_DEST, additionalData, generateValidSign(uuidResponse.getData().getUniqueIdentifier() + "|" + giftCardQuantity + "|" + NATIONAL_CODE_CORRECT),
                commission, CURRENCY_GOLD, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        // This is the gift card code that will be used for payment
        String giftCardCode = giftCardResponseBaseResponse.getData().getActiveCode();

        // Step 3: Send TWO CONCURRENT PAYMENT REQUESTS with the SAME gift card code
        // Only the first request should succeed, the second should fail
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentLinkedQueue<BaseResponse<ObjectUtils.Null>> results = new ConcurrentLinkedQueue<>();

        // First concurrent payment request
        executor.submit(() -> {
            try {
                log.info("Starting first concurrent payment with gift card code: {}", giftCardCode);
                BaseResponse<ObjectUtils.Null> response = paymentGiftCard(mockMvc, accessToken, giftCardCode, giftCardQuantity, CURRENCY_GOLD, NATIONAL_CODE_DEST, destAccount.getAccountNumber(), paymentAdditionalData1, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
                results.add(response);
                log.info("First payment completed with success: {}", response.getSuccess());
            } catch (Exception e) {
                log.error("Error in first concurrent payment", e);
            } finally {
                latch.countDown();
            }
        });

        // Second concurrent payment request with SAME gift card code (should fail) - add small delay
        executor.submit(() -> {
            try {
                // Small delay to ensure second request starts after first
                Thread.sleep(100);
                log.info("Starting second concurrent payment with SAME gift card code: {}", giftCardCode);
                BaseResponse<ObjectUtils.Null> response = paymentGiftCard(mockMvc, accessToken, giftCardCode, giftCardQuantity, CURRENCY_GOLD, NATIONAL_CODE_DEST, destAccount.getAccountNumber(), paymentAdditionalData2, HttpStatus.OK, StatusRepositoryService.GIFT_CARD_NOT_FOUND, false);
                results.add(response);
                log.info("Second payment completed with success: {}", response.getSuccess());
            } catch (Exception e) {
                log.error("Error in second concurrent payment", e);
            } finally {
                latch.countDown();
            }
        });


        // Step 4: Wait for both payment requests to complete
        Assert.assertTrue("Concurrent payment requests should complete within 10 seconds", latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();



        // Step 5: Verify results - exactly one success, one failure
        Assert.assertEquals("Both concurrent payment requests should complete", threadCount, results.size());

        boolean hasSuccess = false;
        boolean hasFailure = false;
        int successCount = 0;
        int failureCount = 0;

        for (BaseResponse<ObjectUtils.Null> response : results) {
            if (response.getSuccess()) {
                hasSuccess = true;
                successCount++;
                log.info("Payment succeeded");
            } else {
                hasFailure = true;
                failureCount++;
                log.info("Payment failed with error code: {}", response.getErrorDetail().getCode());
                Assert.assertSame("Second payment should fail with gift card not found", StatusRepositoryService.GIFT_CARD_NOT_FOUND, response.getErrorDetail().getCode());
            }
        }

        // Verify exactly one success and one failure
        Assert.assertEquals("Exactly one payment should succeed", 1, successCount);
        Assert.assertEquals("Exactly one payment should fail", 1, failureCount);
        Assert.assertTrue("First payment should succeed", hasSuccess);
        Assert.assertTrue("Second payment should fail due to gift card already consumed", hasFailure);

        chargeAccountForGiftCardToZero(sourceAccount.getAccountNumber());
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()), value);
        setLimitationGeneralCustomValue(USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_PAYMENT_GIFT_CARD, walletAccountRepositoryService.findByAccountNumber(sourceAccount.getAccountNumber()), valueCountPayment);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper method to charge account for Gift Card testing using proper cash-in process
     */
    private void chargeAccountForGiftCard(String accountNumber, String cashInAmount) throws Exception {
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        walletAccountRepositoryService.increaseBalance(walletAccountEntity.getId(), new BigDecimal(cashInAmount));
    }

    /**
     * Helper method to charge account for Gift Card testing using proper cash-in process
     */
    private void chargeAccountForGiftCardToZero(String accountNumber) throws Exception {
        WalletAccountEntity walletAccountEntity = walletAccountRepositoryService.findByAccountNumber(accountNumber);
        walletAccountRepositoryService.decreaseBalance(walletAccountEntity.getId(), walletAccountRepositoryService.getBalance(walletAccountEntity.getId()).getRealBalance());
    }

    /**
     * Helper method to generate valid signature for testing
     * Note: This is a simplified version for testing purposes
     */
    private String generateValidSign(String dataString) {
        // In a real implementation, this would use proper cryptographic signing
        // For testing purposes, we'll return a mock signature
        return "MOCK_SIGNATURE_" + dataString.hashCode();
    }
}
