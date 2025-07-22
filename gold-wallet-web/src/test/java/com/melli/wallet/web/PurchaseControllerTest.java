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
import com.melli.wallet.domain.response.wallet.WalletResponse;
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
@DisplayName("PurchaseControllerTest End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PurchaseControllerTest extends WalletApplicationTests {


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
        BaseResponse<WalletResponse> response = createWallet(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        WalletEntity walletEntity = walletService.findById(Long.parseLong(response.getData().getWalletId()));
        if (!walletEntity.getNationalCode().equalsIgnoreCase(NATIONAL_CODE_CORRECT)) {
            log.error("wallet create not same with national code ({})", NATIONAL_CODE_CORRECT);
            throw new Exception("wallet create not same with national code ({})");
        }
    }

    @Test
    @Order(25)
    @DisplayName("create wallet- fail")
    void buyLessThanMinQuantityFail() throws Exception {
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String minAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_PRICE_BUY, walletAccountObjectOptional.getAccountNumber());
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), String.valueOf(Long.parseLong(minAmount) + 1), NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        buy(mockMvc, ACCESS_TOKEN, uuidResponseBaseResponse.getData().getUniqueIdentifier(), "0.000001", String.valueOf(Long.parseLong(minAmount) + 1), "RIAL", "0.002", NATIONAL_CODE_CORRECT, "GOLD", "1", walletAccountObjectOptional.getAccountNumber(),
                "", "", HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
    }


    @Test
    @Order(40)
    @DisplayName("buy fail- less than minPrice")
    void buyFailMinPrice() throws Exception {
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String minAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MIN_PRICE_BUY, walletAccountObjectOptional.getAccountNumber());
        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), String.valueOf(Long.parseLong(minAmount) - 1), NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.AMOUNT_LESS_THAN_MIN, false);
    }

    @Test
    @Order(41)
    @DisplayName("buy fail- bigger than maxPrice")
    void buyFailMaxPrice() throws Exception {
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String maxAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_PRICE_BUY, walletAccountObjectOptional.getAccountNumber());
        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), String.valueOf(Long.parseLong(maxAmount) + 1), NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.AMOUNT_BIGGER_THAN_MAX, false);
    }


    @Test
    @Order(43)
    @DisplayName("buy fail- invalid amount")
    void buyFailInvalidPrice() throws Exception {
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        generateBuyUuid(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf("123edfed"), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
    }

    @Test
    @Order(44)
    @DisplayName("generateBuyUuid-Success")
    void generateBuyUuidSuccess() throws Exception {
        String price = "100000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
    }

    //amount diff uuid and purchase
    @Test
    @Order(45)
    @DisplayName("amountUuidDifferentFromPurchaseAmount-fail")
    void amountUuidDifferentFromPurchaseAmountFail() throws Exception {
        String price = "100000";
        String quantity = "1.07";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        buy(mockMvc, ACCESS_TOKEN, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price) + 1), CURRENCY_RIAL, "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD
                , "1", walletAccountObjectOptional.getAccountNumber(), "", "differentAmount", HttpStatus.OK, StatusService.PRICE_NOT_SAME_WITH_UUID, false);
    }

    @Test
    @Order(46)
    @DisplayName("currencyNotValid-fail")
    void currencyNotValidFail() throws Exception {
        String price = "100000";
        String quantity = "1.07";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        BaseResponse<PurchaseResponse> response = buy(mockMvc, ACCESS_TOKEN, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price)), "RIAL", "2000", NATIONAL_CODE_CORRECT, "SILVER"
                , "1", walletAccountObjectOptional.getAccountNumber(), "", "differentAmount", HttpStatus.OK, StatusService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);
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


        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        buy(mockMvc, ACCESS_TOKEN, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price)), "RIAL", "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD
                , "1", walletAccountObjectOptional.getAccountNumber(), "", "differentAmount", HttpStatus.OK, StatusService.SUCCESSFUL, true);

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
        String valueMaxDailyPrice = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_PRICE_BUY, walletAccountObjectOptional.getAccountNumber());
        AggregationPurchaseDTO aggregationPurchaseDTO = requestService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccountEntity.getId()}, TransactionTypeEnum.BUY.name(), new Date(), new Date());

        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_DAILY_COUNT_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                aggregationPurchaseDTO.getCountRecord(), "change MAX_DAILY_COUNT_BUY");

        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.BUY_EXCEEDED_COUNT_DAILY_LIMITATION, false);

        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_DAILY_COUNT_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                valueMaxDailyCount, "change MAX_DAILY_COUNT_BUY");

        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_DAILY_PRICE_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                aggregationPurchaseDTO.getSumPrice(), "change MAX_DAILY_PRICE_BUY");

        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.BUY_EXCEEDED_AMOUNT_DAILY_LIMITATION, false);

        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_DAILY_PRICE_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                valueMaxDailyPrice, "change MAX_DAILY_PRICE_BUY");

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
        String valueMaxDailyCount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_COUNT_BUY, walletAccountObjectOptional.getAccountNumber());
        String valueMaxDailyPrice = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService, USERNAME_CORRECT, LimitationGeneralService.MAX_DAILY_PRICE_BUY, walletAccountObjectOptional.getAccountNumber());

        Date fromDate = DateUtils.findFirstDateOfPersianMonth(new Date());
        Date untilDate = new Date();
        log.info("found monthly fromTime ({}), until ({})", fromDate, untilDate);
        AggregationPurchaseDTO aggregationPurchaseDTO = requestService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccountEntity.getId()}, TransactionTypeEnum.BUY.name(), fromDate, untilDate);

        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_COUNT_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                aggregationPurchaseDTO.getCountRecord(), "change MAX_MONTHLY_COUNT_BUY");

        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.BUY_EXCEEDED_COUNT_MONTHLY_LIMITATION, false);

        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_PRICE_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                valueMaxDailyCount, "change MAX_MONTHLY_PRICE_BUY");

        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_COUNT_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                aggregationPurchaseDTO.getSumPrice(), "change MAX_MONTHLY_COUNT_BUY");

        generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.BUY_EXCEEDED_AMOUNT_MONTHLY_LIMITATION, false);

        limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                limitationGeneralService.getSetting(LimitationGeneralService.MAX_MONTHLY_PRICE_BUY).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                valueMaxDailyPrice, "change MAX_MONTHLY_PRICE_BUY");

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


        BaseResponse<UuidResponse> uuidResponseBaseResponse = generateBuyUuid(mockMvc, ACCESS_TOKEN, walletAccountObjectOptional.getAccountNumber(), price, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        buyDirect(mockMvc,"123456", ACCESS_TOKEN, uuidResponseBaseResponse.getData().getUniqueIdentifier(), quantity, String.valueOf(Long.parseLong(price)), "RIAL", "2000", NATIONAL_CODE_CORRECT, CURRENCY_GOLD
                , "1", walletAccountObjectOptional.getAccountNumber(), "", "differentAmount", HttpStatus.OK, StatusService.SUCCESSFUL, true);

    }

    //


}
