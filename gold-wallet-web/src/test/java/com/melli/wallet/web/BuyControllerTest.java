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
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
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
 * Class Name: WalletEndPointTest
 * Author: Mahdi Shirinabadi
 * Date: 4/7/2025
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


    @Test
    @Order(2)
    @DisplayName("Initiate cache...")
    void initial() throws Exception {
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
        log.info("start login_success test");
        BaseResponse<LoginResponse> response = login(mockMvc, USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.OK,
                StatusService.SUCCESSFUL, true);
        ACCESS_TOKEN = response.getData().getAccessTokenObject().getToken();
        REFRESH_TOKEN = response.getData().getRefreshTokenObject().getToken();
    }

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

    @Test
    @Order(25)
    @DisplayName("get uuid buy fail - less Than min quantity")
    void buyLessThanMinQuantityFail() throws Exception {
        String merchantId = "1";
        String currency = "GOLD";
        String price="10000000";

        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String minAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_QUANTITY_BUY, walletAccountObjectOptional.getAccountNumber());
        String quantity = String.valueOf(new BigDecimal(minAmount).subtract(new BigDecimal("0.00001")));
        increaseMerchantBalance("1",WalletAccountCurrencyService.GOLD,"1111111111");
        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.QUANTITY_LESS_THAN_MIN, false, merchantId, quantity, currency);
    }


    @Test
    @Order(41)
    @DisplayName("get uuid buy fail- bigger than maxQuantity")
    void buyFailMaxPrice() throws Exception {
        String merchantId = "1";
        String currency = "GOLD";
        String price="10000000";

        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String minAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_QUANTITY_BUY, walletAccountObjectOptional.getAccountNumber());
        String quantity = String.valueOf(new BigDecimal(minAmount).add(new BigDecimal("0.00001")));
        increaseMerchantBalance("1",WalletAccountCurrencyService.GOLD,"1111111111");
        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.QUANTITY_BIGGER_THAN_MAX, false, merchantId, quantity, currency);
    }


    @Test
    @Order(43)
    @DisplayName("buy fail- invalid amount")
    void buyFailInvalidPrice() throws Exception {
        String merchantId = "1";
        String quantity = "0.001";
        String currency = "GOLD";
        String invalidAmount = "123edfed";
        
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        generateBuyUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(invalidAmount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false, merchantId, quantity, currency);
    }

    @Test
    @Order(44)
    @DisplayName("generateBuyUuid-Success")
    void generateBuyUuidSuccess() throws Exception {
        String price = "100000";
        String merchantId = "1";
        String quantity = "0.001";
        String currency = "GOLD";
        
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, quantity, currency);
    }

    //amount diff uuid and purchase
    @Test
    @Order(45)
    @DisplayName("amountUuidDifferentFromPurchaseAmount-fail")
    void amountUuidDifferentFromPurchaseAmountFail() throws Exception {
        String price = "100000";
        String quantity = "1.07";
        String merchantId = "1";
        String commission = "2000";
        String commissionType = CURRENCY_RIAL;
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "differentAmount";
        
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, "0.001", currency);
        buy(mockMvc, ACCESS_TOKEN, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price) + 1), commissionType, commission, NATIONAL_CODE_CORRECT, currency
                , merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.PRICE_NOT_SAME_WITH_UUID, false);
    }

    @Test
    @Order(46)
    @DisplayName("currencyNotValid-fail")
    void currencyNotValidFail() throws Exception {
        String price = "100000";
        String quantity = "1.07";
        String merchantId = "1";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = "GOLD";
        String invalidCurrency = "SILVER";
        String sign = "";
        String additionalData = "differentAmount";
        
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, "0.001", currency);
        BaseResponse<PurchaseResponse> response = buy(mockMvc, ACCESS_TOKEN, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, invalidCurrency
                , merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);
        Assert.assertSame(StatusService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, response.getErrorDetail().getCode());
    }

    @Test
    @Order(47)
    @DisplayName("buy-success")
    void buySuccess() throws Exception {
        String price = "100000";
        String quantity = "1.07";
        walletBuyLimitationService.deleteAll();
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        //update balance merchant wallet-account
        increaseMerchantBalance("1.07", WalletAccountCurrencyService.GOLD, "1111111111");

        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if ("false".equalsIgnoreCase(value)) {
            WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true", "test cashInFailMinAmount");
        }




        BaseResponse<UuidResponse> uniqueIdentifierCashIn = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, price, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifierCashIn);
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifierCashIn.getData().getUniqueIdentifier(), String.valueOf(new Date().getTime()), price, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);


        String merchantId = "1";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "differentAmount";
        
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, "0.001", currency);
        buy(mockMvc, ACCESS_TOKEN, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, currency
                , merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.SUCCESSFUL, true);

    }



    //check buy daily limitation
    @Test
    @Order(48)
    @DisplayName("buyDailyLimitationFail-success")
    void buyDailyLimitationFail() throws Exception {
        String price = "100000";
        walletBuyLimitationService.deleteAll();
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        //update balance merchant wallet-account

        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String valueMaxDailyCount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_BUY, walletAccountObjectOptional.getAccountNumber());
        String valueMaxDailyPrice = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_QUANTITY_BUY, walletAccountObjectOptional.getAccountNumber());
        AggregationPurchaseDTO aggregationPurchaseDTO = requestService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccountEntity.getId()}, TransactionTypeEnum.BUY.name(), new Date(), new Date());

        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_DAILY_COUNT_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                aggregationPurchaseDTO.getCountRecord(), "change MAX_DAILY_COUNT_BUY");

        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.BUY_EXCEEDED_COUNT_DAILY_LIMITATION, false, "1", "0.001", "GOLD");

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


    //check buy monthly limitation
    @Test
    @Order(49)
    @DisplayName("buyMonthlyLimitationFail-success")
    void buyMonthlyLimitationFail() throws Exception {
        String price = "100000";
        walletBuyLimitationService.deleteAll();
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        //update balance merchant wallet-account

        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String valueMaxMonthlyCount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_COUNT_BUY, walletAccountObjectOptional.getAccountNumber());
        String valueMaxMonthlyQuantity = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_MONTHLY_QUANTITY_BUY, walletAccountObjectOptional.getAccountNumber());

        Date fromDate = DateUtils.findFirstDateOfPersianMonth(new Date());
        Date untilDate = new Date();
        log.info("found monthly fromTime ({}), until ({})", fromDate, untilDate);
        AggregationPurchaseDTO aggregationPurchaseDTO = requestService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccountEntity.getId()}, TransactionTypeEnum.BUY.name(), fromDate, untilDate);


        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_COUNT_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                aggregationPurchaseDTO.getCountRecord(), "change MAX_MONTHLY_COUNT_BUY");

        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.BUY_EXCEEDED_COUNT_MONTHLY_LIMITATION, false, "1", "0.001", "GOLD");

        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_COUNT_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                valueMaxMonthlyCount, "change MAX_MONTHLY_COUNT_BUY");



        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_QUANTITY_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                aggregationPurchaseDTO.getSumQuantity(), "change MAX_MONTHLY_COUNT_BUY");

        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.BUY_EXCEEDED_QUANTITY_MONTHLY_LIMITATION, false, "1", "0.001", "GOLD");

        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_QUANTITY_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                valueMaxMonthlyQuantity, "change MAX_MONTHLY_QUANTITY_BUY");

    }


    @Test
    @Order(50)
    @DisplayName("buyDirect-success")
    void buyDirectSuccess() throws Exception {
        String price = "100000";
        String quantity = "1.07";
        walletBuyLimitationService.deleteAll();
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);

        //update balance merchant wallet-account
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId("1111111111", walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountService.findByWallet(walletMerchantEntity);

        //find gold wallet account
        long id = 0;
        try {
            id = walletAccountCurrencyService.findCurrency(WalletAccountCurrencyService.GOLD).getId();
        } catch (InternalServiceException ex) {
            log.error("walletAccountCurrencyEntityGold not found", ex);
        }

        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        if ("false".equalsIgnoreCase(value)) {
            WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true", "test cashInFailMinAmount");
        }


        long finalId = id;
        WalletAccountEntity walletAccountEntity = walletAccountEntityList.stream().filter(
                x -> x.getWalletAccountCurrencyEntity().getId() == finalId).findFirst().orElse(null);
        walletAccountService.increaseBalance(walletAccountEntity.getId(), new BigDecimal("1.07"));

        BaseResponse<UuidResponse> uniqueIdentifierCashIn = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, price, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifierCashIn);
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifierCashIn.getData().getUniqueIdentifier(), String.valueOf(new Date().getTime()), price, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);


        String merchantId = "1";
        String refNumber = new Date() + "";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "differentAmount";
        
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, "0.001", currency);
        buyDirect(mockMvc, refNumber, ACCESS_TOKEN, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, currency
                , merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.SUCCESSFUL, true);

    }

    //


    @Test
    @Order(51)
    @DisplayName("buyDirectFail-InvalidCommissionCurrency")
    void buyDirectFailInvalidCommissionCurrency() throws Exception {
        log.info("start buyDirectFailInvalidCommissionCurrency test");
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
                "true","test buyDirectFailInvalidCommissionCurrency");
        }
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());

        // Update merchant balance
        increaseMerchantBalance("1.07", WalletAccountCurrencyService.GOLD, "1111111111");

        String merchantId = "1";
        String commission = "2000";
        String commissionType = "GOLD";
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "test commission currency error";
        
        // Generate UUID for buyDirect
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, "0.001", currency);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Test with GOLD commission currency (should fail)
        BaseResponse<PurchaseResponse> response = buyDirect(mockMvc, refNumber, ACCESS_TOKEN, uniqueIdentifier, quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, currency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.COMMISSION_CURRENCY_NOT_VALID, false);
        Assert.assertSame(StatusService.COMMISSION_CURRENCY_NOT_VALID, response.getErrorDetail().getCode());
    }

    @Test
    @Order(52)
    @DisplayName("buyDirectFail-InvalidUniqueIdentifier")
    void buyDirectFailInvalidUniqueIdentifier() throws Exception {
        log.info("start buyDirectFailInvalidUniqueIdentifier test");
        String quantity = "1.07";
        String price = "100000";
        String refNumber = new Date().getTime() + "";
        String merchantId = "1";
        String commission = "2000";
        String commissionType = "RIAL";
        String invalidUuid = "invalid_uuid";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);;
        String sign = "";
        String additionalData = "test invalid uuid";
        
        buyDirect(mockMvc, refNumber, ACCESS_TOKEN, invalidUuid, quantity, price, commissionType, commission, NATIONAL_CODE_CORRECT, CURRENCY_GOLD, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.UUID_NOT_FOUND, false);
    }

    @Test
    @Order(53)
    @DisplayName("buyDirectFail-MerchantBalanceNotEnough")
    void buyDirectFailMerchantBalanceNotEnough() throws Exception {
        log.info("start buyDirectFailMerchantBalanceNotEnough test");
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
                "true","test buyDirectFailMerchantBalanceNotEnough");
        }
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());

        String merchantId = "1";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "test merchant balance not enough";

        refNumber = new Date().getTime() + "";
        
        // Don't update merchant balance to simulate insufficient balance
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, quantity, currency);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();

        setMerchantBalanceToZero(WalletAccountCurrencyService.GOLD,"1111111111");
        
        // Test with insufficient merchant balance
        BaseResponse<PurchaseResponse> response = buyDirect(mockMvc, refNumber, ACCESS_TOKEN, uniqueIdentifier, quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT,
                currency, merchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.MERCHANT_BALANCE_NOT_ENOUGH, false);
        Assert.assertSame(StatusService.MERCHANT_BALANCE_NOT_ENOUGH, response.getErrorDetail().getCode());
    }

    @Test
    @Order(54)
    @DisplayName("buyDirectFail-InvalidMerchantId")
    void buyDirectFailInvalidMerchantId() throws Exception {
        log.info("start buyDirectFailInvalidMerchantId test");
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
                "true","test buyDirectFailInvalidMerchantId");
        }
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());

        // Update merchant balance
        increaseMerchantBalance("1.07", WalletAccountCurrencyService.GOLD, "1111111111");

        String merchantId = "1";
        String invalidMerchantId = "999";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = CURRENCY_GOLD;
        String sign = "";
        String additionalData = "test invalid merchant id";
        
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, "0.001", currency);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Test with invalid merchant ID
        BaseResponse<PurchaseResponse> response = buyDirect(mockMvc, refNumber, ACCESS_TOKEN, uniqueIdentifier, quantity, String.valueOf(Long.parseLong(price)), commissionType, commission, NATIONAL_CODE_CORRECT, currency, invalidMerchantId, walletAccountObjectOptional.getAccountNumber(), sign, additionalData, HttpStatus.OK, StatusService.MERCHANT_IS_NOT_EXIST, false);
        Assert.assertSame(StatusService.MERCHANT_IS_NOT_EXIST, response.getErrorDetail().getCode());
    }

    @Test
    @Order(55)
    @DisplayName("buyDirectFail-InvalidCurrency")
    void buyDirectFailInvalidCurrency() throws Exception {
        log.info("start buyDirectFailInvalidCurrency test");
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
                "true","test buyDirectFailInvalidCurrency");
        }
        BaseResponse<UuidResponse> cashInUuidResponse = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, cashInAmount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        BaseResponse<CashInResponse> cashInResponse = cashIn(mockMvc, ACCESS_TOKEN, cashInUuidResponse.getData().getUniqueIdentifier(), refNumber, cashInAmount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(cashInResponse.getData());

        // Update merchant balance
        increaseMerchantBalance("1.07", WalletAccountCurrencyService.GOLD, "1111111111");

        String merchantId = "1";
        String commission = "2000";
        String commissionType = "RIAL";
        String currency = "GOLD";
        String invalidCurrency = "INVALID_CURRENCY";
        String sign = "";
        String additionalData = "test invalid currency";
        
        BaseResponse<UuidResponse> uuidResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true, merchantId, "0.001", currency);
        String uniqueIdentifier = uuidResponse.getData().getUniqueIdentifier();
        
        // Test with invalid currency
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


    private void increaseMerchantBalance(String val, String currency, String merchantNationalCode) {
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId(merchantNationalCode, walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountService.findByWallet(walletMerchantEntity);

        //find gold wallet account
        long id = 0;
        try {
            id = walletAccountCurrencyService.findCurrency(currency).getId();
        } catch (InternalServiceException ex) {
            log.error("walletAccountCurrencyEntity({}) not found", currency, ex);
        }

        long finalId = id;
        WalletAccountEntity walletAccountEntity = walletAccountEntityList.stream().filter(
                x -> x.getWalletAccountCurrencyEntity().getId() == finalId).findFirst().orElse(null);
        walletAccountService.increaseBalance(walletAccountEntity.getId(), new BigDecimal(val));
    }

    private void decreaseMerchantBalance(String val, String currency, String merchantNationalCode) {
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId(merchantNationalCode, walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountService.findByWallet(walletMerchantEntity);

        //find gold wallet account
        long id = 0;
        try {
            id = walletAccountCurrencyService.findCurrency(currency).getId();
        } catch (InternalServiceException ex) {
            log.error("walletAccountCurrencyEntity({}) not found", currency, ex);
        }

        long finalId = id;
        WalletAccountEntity walletAccountEntity = walletAccountEntityList.stream().filter(
                x -> x.getWalletAccountCurrencyEntity().getId() == finalId).findFirst().orElse(null);
        walletAccountService.decreaseBalance(walletAccountEntity.getId(), new BigDecimal(val));
    }


    private void setMerchantBalanceToZero(String currency, String merchantNationalCode) {
        WalletEntity walletMerchantEntity = walletService.findByNationalCodeAndWalletTypeId(merchantNationalCode, walletTypeService.getByName(WalletTypeService.MERCHANT).getId());
        List<WalletAccountEntity> walletAccountEntityList = walletAccountService.findByWallet(walletMerchantEntity);

        //find gold wallet account
        long id = 0;
        try {
            id = walletAccountCurrencyService.findCurrency(currency).getId();
        } catch (InternalServiceException ex) {
            log.error("walletAccountCurrencyEntity({}) not found", currency, ex);
        }

        long finalId = id;
        WalletAccountEntity walletAccountEntity = walletAccountEntityList.stream().filter(
                x -> x.getWalletAccountCurrencyEntity().getId() == finalId).findFirst().orElse(null);
        walletAccountService.decreaseBalance(walletAccountEntity.getId(), walletAccountService.getBalance(walletAccountEntity.getId()));
    }

}
