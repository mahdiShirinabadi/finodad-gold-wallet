package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.wallet.WalletResponse;
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
 * Class Name: WalletEndPointTest
 * Author: Mahdi Shirinabadi
 * Date: 4/7/2025
 */
@Log4j2
@DisplayName("CacheControllerTest End2End test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CacheControllerTest extends WalletApplicationTests {



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
    private WalletAccountService walletAccountService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletTypeService walletTypeService;
    @Autowired
    private LimitationGeneralCustomService limitationGeneralCustomService;
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
    @DisplayName("get balance fail- wallet not found")
    void getBalanceFail() throws Exception {
        balance(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.WALLET_NOT_FOUND, false);
    }
    @Test
    @Order(16)
    @DisplayName("get balance fail- invalid nationalCode")
    void getBalanceFailInvalidNationalCode() throws Exception {
        balance(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_INCORRECT, HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
    }
    @Test
    @Order(17)
    @DisplayName("get balance fail- NationalCodeLessStandard")
    void getBalanceFailNationalCodeLessStandard() throws Exception {
        balance(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_LENGTH_LESS_THAN_STANDARD, HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
    }
    @Test
    @Order(18)
    @DisplayName("get balance fail- NationalCodeBiggerStandard")
    void getBalanceFailNationalCodeBiggerStandard() throws Exception {
        balance(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_LENGTH_BIGGER_THAN_STANDARD, HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
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
    @Order(21)
    @DisplayName("create wallet duplicate - success")
    void createWalletDuplicateSuccess() throws Exception {
        List<WalletTypeEntity> walletTypeEntityList = walletTypeService.getAll();

        WalletTypeEntity walletTypeEntity = walletTypeEntityList.stream().filter(x -> x.getName().equalsIgnoreCase(WalletTypeService.NORMAL_USER)).findFirst().orElseThrow(() -> {
            log.error("walletType not found");
            return new Exception("walletType not found");
        });

        WalletEntity walletExistEntity = walletService.findByNationalCodeAndWalletTypeId(NATIONAL_CODE_CORRECT, walletTypeEntity.getId());

        BaseResponse<WalletResponse> response = createWallet(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        if (walletExistEntity.getId() != Long.parseLong(response.getData().getWalletId())) {
            log.error("create new wallet not same with created wallet!!!");
            throw new Exception("create new wallet not same with created wallet!!!");
        }
    }

    @Test
    @Order(25)
    @DisplayName("get balance success")
    void getBalanceSuccess() throws Exception {
        balance(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
    }

    @Test
    @Order(26)
    @DisplayName("cashInFailNotPermission")
    void cashInFailNotPermission() throws Exception {
        String refNumber = new Date().getTime() + "";
        String amount = "1000000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        BaseResponse<UuidResponse> responseUuid = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.ACCOUNT_DONT_PERMISSION_FOR_CASH_IN, false);
    }


    //cashInFail minAmount
    @Test
    @Order(28)
    @DisplayName("cashInFail-min amount")
    void cashInFailMinAmount() throws Exception {
        String refNumber = new Date().getTime() + "";
        String amount = "10";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }


        String valueMinAmountCashIn = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        boolean changeMinAmountCashIn = false;
        if( Long.parseLong(amount) >= Long.parseLong(valueMinAmountCashIn)){
            changeMinAmountCashIn = true;
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.MIN_AMOUNT_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    String.valueOf(Long.parseLong(valueMinAmountCashIn) - 1),"test cashInFailMinAmount");
        }
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.AMOUNT_LESS_THAN_MIN, false);
        if(changeMinAmountCashIn){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.MIN_AMOUNT_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    valueMinAmountCashIn,"test cashInFailMinAmount");
        }
    }

    //cashInFail MaxAmount
    @Test
    @Order(29)
    @DisplayName("cashInFail-max amount")
    void cashInFailMaxAmount() throws Exception {
        String refNumber = new Date().getTime() + "";
        String amount = "1000000000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }


        String valueMaxAmountCashIn = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.MAX_AMOUNT_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        boolean changeMaxAmountCashIn = false;
        if( Long.parseLong(amount) <= Long.parseLong(valueMaxAmountCashIn)){
            changeMaxAmountCashIn = true;
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.MAX_WALLET_BALANCE).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    String.valueOf(Long.parseLong(amount) - 1),"test cashInFailMaxAmount");
        }
        log.info("start get balance test");
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.AMOUNT_BIGGER_THAN_MAX, false);
        if(changeMaxAmountCashIn){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.MAX_WALLET_BALANCE).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    valueMaxAmountCashIn,"test cashInFailMaxAmount");
        }
    }

    //cashInFail MaxBalance
    @Test
    @Order(30)
    @DisplayName("cashInFail-max balance")
    void cashInFailMaxBalance() throws Exception {
        String refNumber = new Date().getTime() + "";
        String amount ="100000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());


        String valueEnableCashIn = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(valueEnableCashIn)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }


        String valueMaxWalletBalance = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.MAX_WALLET_BALANCE,  walletAccountObjectOptional.getAccountNumber());
        boolean changeMaxBalance = false;
        if( Long.parseLong(amount) <= Long.parseLong(valueMaxWalletBalance)){
            changeMaxBalance = true;
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.MAX_WALLET_BALANCE).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    String.valueOf(Long.parseLong(amount) - 1),"test cashInFailMaxBalance");
        }
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.BALANCE_MORE_THAN_STANDARD, false);
        if(changeMaxBalance){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.MAX_WALLET_BALANCE).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    valueMaxWalletBalance,"test cashInFailMaxBalance");
        }

    }

    //duplicate refnumber
    @Test
    @Order(32)
    @DisplayName("cashInFail-duplicate refNumber")
    void cashInFailDuplicateRefnumber() throws Exception {
        String refNumber = new Date().getTime() + "";
        String amount = "10000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        BaseResponse<UuidResponse> uniqueIdentifier2 = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, amount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifier2.getData().getUniqueIdentifier(), refNumber, amount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.REF_NUMBER_USED_BEFORE, false);
    }

    //duplicate refnumber
    @Test
    @Order(33)
    @DisplayName("cashInFail-duplicate rrn")
    void cashInFailDuplicateRrn() throws Exception {
        String refNumber = new Date().getTime() + "";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        BigDecimal balance = BigDecimal.valueOf(Float.parseFloat(walletAccountObjectOptional.getBalance()));

        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.decreaseBalance(walletAccountEntity.getId(), balance);

        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
        String minAmount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(Long.parseLong(minAmount) + 1), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, String.valueOf(Long.parseLong(minAmount) + 1), NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), new Date().getTime() + "", String.valueOf(Long.parseLong(minAmount) + 1), NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.DUPLICATE_UUID, false);
    }

    @Test
    @Order(40)
    @DisplayName("cashIn success")
    void cashInSuccess() throws Exception {
        String refNumber = new Date().getTime() + "";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifier1);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, amount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.SUCCESSFUL, true);
    }


    @Test
    @Order(41)
    @DisplayName("cashInFailAmountUuidNotSame")
    void cashInFailAmountUuidNotSame() throws Exception {
        String refNumber = new Date().getTime() + "";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifier1);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, String.valueOf(Long.parseLong(amount) + 1), NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.PRICE_NOT_SAME_WITH_UUID, false);
    }


    @Test
    @Order(42)
    @DisplayName("cashInFailAccountUuidNotSame")
    void cashInFailAccountNotFound() throws Exception {
        String refNumber = new Date().getTime() + "";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifier1);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
        cashIn(mockMvc, ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, String.valueOf(Long.parseLong(amount)), NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber()+"1", "", "", "ACCOUNT_TO_ACCOUNT", HttpStatus.OK, StatusService.ACCOUNT_NUMBER_NOT_SAME_WITH_UUID, false);
    }


    @Test
    @Order(43)
    @DisplayName("cashInFailWalletAccountNotFound")
    void cashInFailAccountNotFoundInUuid() throws Exception {
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber()+"1", HttpStatus.OK, StatusService.WALLET_ACCOUNT_NOT_FOUND, false);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
    }



    @Test
    @Order(44)
    @DisplayName("cashInFailAccountNotActive")
    void cashInFailAccountNotActive() throws Exception {

        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountEntity.setStatus(WalletStatusEnum.DISABLE);
        walletAccountService.save(walletAccountEntity);
        walletAccountService.clearCache(walletAccountObjectOptional.getAccountNumber());
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.WALLET_ACCOUNT_IS_NOT_ACTIVE, false);

        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
        walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountEntity.setStatus(WalletStatusEnum.ACTIVE);
        walletAccountService.save(walletAccountEntity);
        walletAccountService.clearCache(walletAccountObjectOptional.getAccountNumber());
    }


    @Test
    @Order(45)
    @DisplayName("cashInFailWalletNotActive")
    void cashInFailWalletNotActive() throws Exception {

        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());

        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountEntity.getWalletEntity().setStatus(WalletStatusEnum.DISABLE);
        walletService.save(walletAccountEntity.getWalletEntity());
        walletService.clearAllCache();

        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.WALLET_IS_NOT_ACTIVE, false);

        String value = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    limitationGeneralService.getSetting(LimitationGeneralService.ENABLE_CASH_IN).getId(), walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
        walletAccountEntity.getWalletEntity().setStatus(WalletStatusEnum.ACTIVE);
        walletService.save(walletAccountEntity.getWalletEntity());
        walletService.clearAllCache();

    }

    @Test
    @Order(46)
    @DisplayName("cashInFailInvalidNationalCode")
    void cashInFailInvalidNationalCode() throws Exception {
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountEntity.getWalletEntity().setStatus(WalletStatusEnum.DISABLE);
        walletService.save(walletAccountEntity.getWalletEntity());
        walletService.clearAllCache();
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_INCORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
    }

    //invalid amount
    @Test
    @Order(47)
    @DisplayName("cashInFailInvalidAmount")
    void cashInFailInvalidAmount() throws Exception {
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountEntity.getWalletEntity().setStatus(WalletStatusEnum.DISABLE);
        walletService.save(walletAccountEntity.getWalletEntity());
        walletService.clearAllCache();
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf("aa"), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
    }

    //invalid accountNumber
    @Test
    @Order(48)
    @DisplayName("cashInFailInvalidAccountNumber")
    void cashInFailInvalidAccountNumber() throws Exception {
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(walletAccountService, limitationGeneralCustomService, channelService,USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountEntity.getWalletEntity().setStatus(WalletStatusEnum.DISABLE);
        walletService.save(walletAccountEntity.getWalletEntity());
        walletService.clearAllCache();
        generateCashInUniqueIdentifier(mockMvc, ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), "546fgdgdfg5", HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
    }

    //




}
