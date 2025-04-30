package com.melli.wallet.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.master.entity.WalletTypeEntity;
import com.melli.wallet.domain.request.wallet.*;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.cash.CashInResponse;
import com.melli.wallet.domain.response.cash.CashInTrackResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.purchase.PurchaseResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.service.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: WalletEndPointTest
 * Author: Mahdi Shirinabadi
 * Date: 4/7/2025
 */
@Log4j2
@DisplayName("WalletEndPoint End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PurchaseControllerTest extends WalletApplicationTests {

    private static final String CREATE_WALLET_PATH = "/api/v1/wallet/create";
    private static final String GET_DATA_WALLET_PATH = "/api/v1/wallet/get";
    private static final String ACTIVE_WALLET_PATH = "/api/v1/wallet/activate";
    private static final String DEACTIVATED_WALLET_PATH = "/api/v1/wallet/deactivate";
    private static final String DELETE_WALLET_PATH = "/api/v1/wallet/delete";
    private static final String GENERATE_UUID_PATH = "/api/v1/general/generate/uuid";
    private static final String CASH_IN_GENERATE_UUID_PATH = "/api/v1/cash/generate/uuid";
    private static final String PURCHASE_GENERATE_UUID_PATH = "/api/v1/purchase/generate/uuid";
    private static final String BUY_IN_PATH = "/api/v1/purchase/buy";
    private static final String CELL_IN_PATH = "/api/v1/purchase/cell";
    private static final String PURCHASE_INQUIRY_IN_PATH = "/api/v1/purchase/inquiry";
    private static final String CASH_IN_PATH = "/api/v1/cash/cashIn";
    private static final String CASH_IN_INQUIRY_PATH = "/api/v1/cash/track/cashIn/";

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
    @Autowired
    private ChannelService channelService;


    @Test
    @Order(1)
    @DisplayName("Initiate...")
    void initial() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);

        boolean success = setupDB();
        Assert.assertTrue(success);
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
        balance(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.WALLET_NOT_FOUND, false);
    }
    @Test
    @Order(16)
    @DisplayName("get balance fail- invalid nationalCode")
    void getBalanceFailInvalidNationalCode() throws Exception {
        balance(ACCESS_TOKEN, NATIONAL_CODE_INCORRECT, HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
    }
    @Test
    @Order(17)
    @DisplayName("get balance fail- NationalCodeLessStandard")
    void getBalanceFailNationalCodeLessStandard() throws Exception {
        balance(ACCESS_TOKEN, NATIONAL_CODE_LENGTH_LESS_THAN_STANDARD, HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
    }
    @Test
    @Order(18)
    @DisplayName("get balance fail- NationalCodeBiggerStandard")
    void getBalanceFailNationalCodeBiggerStandard() throws Exception {
        balance(ACCESS_TOKEN, NATIONAL_CODE_LENGTH_BIGGER_THAN_STANDARD, HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
    }

    @Test
    @Order(20)
    @DisplayName("create wallet- success")
    void createWalletSuccess() throws Exception {
        BaseResponse<CreateWalletResponse> response = createWallet(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
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

        BaseResponse<CreateWalletResponse> response = createWallet(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, MOBILE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        if (walletExistEntity.getId() != Long.parseLong(response.getData().getWalletId())) {
            log.error("create new wallet not same with created wallet!!!");
            throw new Exception("create new wallet not same with created wallet!!!");
        }
    }

    @Test
    @Order(25)
    @DisplayName("get balance success")
    void getBalanceSuccess() throws Exception {
        balance(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
    }

    @Test
    @Order(26)
    @DisplayName("cashInFailNotPermission")
    void cashInFailNotPermission() throws Exception {
        String refNumber = new Date().getTime() + "";
        String amount = "1000000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        BaseResponse<UuidResponse> responseUuid = generateCashInUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.ACCOUNT_DONT_PERMISSION_FOR_CASH_IN, false);
    }


    //cashInFail minAmount
    @Test
    @Order(28)
    @DisplayName("cashInFail-min amount")
    void cashInFailMinAmount() throws Exception {
        String refNumber = new Date().getTime() + "";
        String amount = "10";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);


        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }


        String valueMinAmountCashIn = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        boolean changeMinAmountCashIn = false;
        if( Long.parseLong(amount) >= Long.parseLong(valueMinAmountCashIn)){
            changeMinAmountCashIn = true;
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    String.valueOf(Long.parseLong(valueMinAmountCashIn) - 1),"test cashInFailMinAmount");
        }
        generateCashInUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.AMOUNT_LESS_THAN_MIN, false);
        if(changeMinAmountCashIn){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }


        String valueMaxAmountCashIn = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.MAX_AMOUNT_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        boolean changeMaxAmountCashIn = false;
        if( Long.parseLong(amount) <= Long.parseLong(valueMaxAmountCashIn)){
            changeMaxAmountCashIn = true;
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.MAX_WALLET_BALANCE, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    String.valueOf(Long.parseLong(amount) - 1),"test cashInFailMaxAmount");
        }
        log.info("start get balance test");
        generateCashInUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.AMOUNT_BIGGER_THAN_MAX, false);
        if(changeMaxAmountCashIn){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.MAX_WALLET_BALANCE, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
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
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());


        String valueEnableCashIn = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(valueEnableCashIn)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }


        String valueMaxWalletBalance = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.MAX_WALLET_BALANCE,  walletAccountObjectOptional.getAccountNumber());
        boolean changeMaxBalance = false;
        if( Long.parseLong(amount) <= Long.parseLong(valueMaxWalletBalance)){
            changeMaxBalance = true;
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.MAX_WALLET_BALANCE, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    String.valueOf(Long.parseLong(amount) - 1),"test cashInFailMaxBalance");
        }
        generateCashInUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.BALANCE_MORE_THAN_STANDARD, false);
        if(changeMaxBalance){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.MAX_WALLET_BALANCE, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    valueMaxWalletBalance,"test cashInFailMaxBalance");
        }

    }

    //CashInFail MaxBalanceDaily
    /*@Test
    @Order(31)
    @DisplayName("cashInFail-max balance daily")
    void cashInFailMaxBalanceDaily() throws Exception {
        String refNumber = new Date().getTime() + "";
        String amount = "10000000000";
        Optional<WalletAccountObject> walletAccountObjectOptional = getAccountNumber(NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String uuid = generateUuid(NATIONAL_CODE_CORRECT);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.get().getAccountNumber());
        String value = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.get().getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
        cashIn(ACCESS_TOKEN, uuid, refNumber, amount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.get().getAccountNumber(), "", "", HttpStatus.OK, StatusService.WALLET_EXCEEDED_AMOUNT_LIMITATION, false);
    }*/


    //duplicate refnumber
    @Test
    @Order(32)
    @DisplayName("cashInFail-duplicate refNumber")
    void cashInFailDuplicateRefnumber() throws Exception {
        String refNumber = new Date().getTime() + "";
        String amount = "10000";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        BaseResponse<UuidResponse> uniqueIdentifier2 = generateCashInUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, amount, walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
        cashIn(ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, amount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        cashIn(ACCESS_TOKEN, uniqueIdentifier2.getData().getUniqueIdentifier(), refNumber, amount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", HttpStatus.OK, StatusService.REF_NUMBER_USED_BEFORE, false);
    }

    //duplicate refnumber
    @Test
    @Order(33)
    @DisplayName("cashInFail-duplicate rrn")
    void cashInFailDuplicateRrn() throws Exception {
        String refNumber = new Date().getTime() + "";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        long balance = Long.parseLong(walletAccountObjectOptional.getBalance());

        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountService.decreaseBalance(walletAccountEntity.getId(), Double.parseDouble(String.valueOf(balance)));

        String value = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
        String minAmount = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(Long.parseLong(minAmount) + 1), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        cashIn(ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, String.valueOf(Long.parseLong(minAmount) + 1), NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        cashIn(ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), new Date().getTime() + "", String.valueOf(Long.parseLong(minAmount) + 1), NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", HttpStatus.OK, StatusService.DUPLICATE_UUID, false);
    }

    @Test
    @Order(40)
    @DisplayName("cashIn success")
    void cashInSuccess() throws Exception {
        String refNumber = new Date().getTime() + "";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifier1);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
        cashIn(ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, amount, NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", HttpStatus.OK, StatusService.SUCCESSFUL, true);
    }


    @Test
    @Order(41)
    @DisplayName("cashInFailAmountUuidNotSame")
    void cashInFailAmountUuidNotSame() throws Exception {
        String refNumber = new Date().getTime() + "";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifier1);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
        cashIn(ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, String.valueOf(Long.parseLong(amount) + 1), NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber(), "", "", HttpStatus.OK, StatusService.AMOUNT_NOT_SAME_WITH_UUID, false);
    }


    @Test
    @Order(42)
    @DisplayName("cashInFailAccountUuidNotSame")
    void cashInFailAccountNotFound() throws Exception {
        String refNumber = new Date().getTime() + "";
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        BaseResponse<UuidResponse> uniqueIdentifier1 = generateCashInUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.SUCCESSFUL, true);
        log.info("generate uuid " + uniqueIdentifier1);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
        cashIn(ACCESS_TOKEN, uniqueIdentifier1.getData().getUniqueIdentifier(), refNumber, String.valueOf(Long.parseLong(amount)), NATIONAL_CODE_CORRECT, walletAccountObjectOptional.getAccountNumber()+"1", "", "", HttpStatus.OK, StatusService.ACCOUNT_NUMBER_NOT_SAME_WITH_UUID, false);
    }


    @Test
    @Order(43)
    @DisplayName("cashInFailWalletAccountNotFound")
    void cashInFailAccountNotFoundInUuid() throws Exception {
        WalletAccountObject walletAccountObjectOptional = getAccountNumber(NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        generateCashInUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber()+"1", HttpStatus.OK, StatusService.WALLET_ACCOUNT_NOT_FOUND, false);
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        String value = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
    }



    @Test
    @Order(44)
    @DisplayName("cashInFailAccountNotActive")
    void cashInFailAccountNotActive() throws Exception {

        WalletAccountObject walletAccountObjectOptional = getAccountNumber(NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountEntity.setStatus(WalletStatusEnum.DISABLE);
        walletAccountService.save(walletAccountEntity);
        walletAccountService.clearCache(walletAccountObjectOptional.getAccountNumber());
        generateCashInUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.WALLET_ACCOUNT_IS_NOT_ACTIVE, false);

        String value = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
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

        WalletAccountObject walletAccountObjectOptional = getAccountNumber(NATIONAL_CODE_CORRECT, WalletAccountTypeService.NORMAL, WalletAccountCurrencyService.RIAL);
        String amount = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletAccountObjectOptional.getAccountNumber());

        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(walletAccountObjectOptional.getAccountNumber());
        walletAccountEntity.getWalletEntity().setStatus(WalletStatusEnum.DISABLE);
        walletService.save(walletAccountEntity.getWalletEntity());
        walletService.clearAllCache();

        generateCashInUniqueIdentifier(ACCESS_TOKEN, NATIONAL_CODE_CORRECT, String.valueOf(amount), walletAccountObjectOptional.getAccountNumber(), HttpStatus.OK, StatusService.WALLET_IS_NOT_ACTIVE, false);

        String value = getSettingValue(USERNAME_CORRECT, LimitationGeneralService.ENABLE_CASH_IN,  walletAccountObjectOptional.getAccountNumber());
        if("false".equalsIgnoreCase(value)){
            limitationGeneralCustomService.create(channelService.getChannel(USERNAME_CORRECT),
                    LimitationGeneralService.ENABLE_CASH_IN, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                    walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity(),
                    "true","test cashInFailMinAmount");
        }
        walletAccountEntity.getWalletEntity().setStatus(WalletStatusEnum.ACTIVE);
        walletService.save(walletAccountEntity.getWalletEntity());
        walletService.clearAllCache();

    }





    BaseResponse<CreateWalletResponse> balance(String token, String nationalCode, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        MockHttpServletRequestBuilder postRequest = buildGetRequest(token, GET_DATA_WALLET_PATH + "/" + nationalCode);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CreateWalletResponse>> typeReference = new TypeReference<>() {
        };
        return objectMapper.readValue(response, typeReference);
    }

    private WalletAccountObject getAccountNumber(String nationalCode, String walletAccountType, String walletAccountCurrency) throws Exception {
        log.info("start get wallet data ...");
        BaseResponse<CreateWalletResponse> createWalletResponseBaseResponse = balance(ACCESS_TOKEN, nationalCode, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        List<WalletAccountObject> walletAccountObjectList = createWalletResponseBaseResponse.getData().getWalletAccountObjectList();
        Optional<WalletAccountObject> walletAccountObjectOptional = walletAccountObjectList.stream().filter(x -> x.getWalletAccountTypeObject().getName().equalsIgnoreCase(walletAccountType)
                        && x.getWalletAccountCurrencyObject().getName().equalsIgnoreCase(walletAccountCurrency))
                .findFirst();
        if (walletAccountObjectOptional.isEmpty()) {
            log.error("wallet account rial not found for nationalCode ({})", NATIONAL_CODE_CORRECT);
            throw new Exception("wallet account rial not found for nationalCode ({})");
        }
        return walletAccountObjectOptional.get();
    }



    BaseResponse<CreateWalletResponse> createWallet(String token, String nationalCode, String mobile, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        CreateWalletRequestJson createWalletRequestJson = new CreateWalletRequestJson();
        createWalletRequestJson.setMobile(mobile);
        createWalletRequestJson.setNationalCode(nationalCode);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, CREATE_WALLET_PATH, mapToJson(createWalletRequestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CreateWalletResponse>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<ObjectUtils.Null> deactivatedWallet(String token, String id, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        DeactivatedWalletRequestJson requestJson = new DeactivatedWalletRequestJson();
        requestJson.setId(id);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, DEACTIVATED_WALLET_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<ObjectUtils.Null>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<ObjectUtils.Null> activeWallet(String token, String id, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        ActiveWalletRequestJson requestJson = new ActiveWalletRequestJson();
        requestJson.setId(id);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, ACTIVE_WALLET_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<ObjectUtils.Null>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<ObjectUtils.Null> deleteWallet(String token, String id, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        DeleteWalletRequestJson requestJson = new DeleteWalletRequestJson();
        requestJson.setId(id);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, DELETE_WALLET_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<ObjectUtils.Null>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<UuidResponse> generateUniqueIdentifier(String token, String nationalCode, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        GenerateUuidRequestJson requestJson = new GenerateUuidRequestJson();
        requestJson.setNationalCode(nationalCode);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, GENERATE_UUID_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<UuidResponse>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<UuidResponse> generateCashInUniqueIdentifier(String token, String nationalCode, String amount, String accountNumber, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        CashInGenerateUuidRequestJson requestJson = new CashInGenerateUuidRequestJson();
        requestJson.setNationalCode(nationalCode);
        requestJson.setAmount(amount);
        requestJson.setAccountNumber(accountNumber);

        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, CASH_IN_GENERATE_UUID_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<UuidResponse>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<UuidResponse> generatePurchaseUniqueIdentifier(String token, String nationalCode, String amount, String accountNumber, String type, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        PurchaseGenerateUuidRequestJson requestJson = new PurchaseGenerateUuidRequestJson();
        requestJson.setNationalCode(nationalCode);
        requestJson.setAmount(amount);
        requestJson.setAccountNumber(accountNumber);
        requestJson.setPurchaseType(type);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, PURCHASE_GENERATE_UUID_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<UuidResponse>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<CashInResponse> cashIn(String token, String uniqueIdentifier, String referenceNumber, String amount, String nationalCode, String accountNumber, String sign, String additionalData, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        CashInWalletRequestJson requestJson = new CashInWalletRequestJson();
        requestJson.setUniqueIdentifier(uniqueIdentifier);
        requestJson.setReferenceNumber(referenceNumber);
        requestJson.setAmount(amount);
        requestJson.setNationalCode(nationalCode);
        requestJson.setAdditionalData(additionalData);
        requestJson.setAccountNumber(accountNumber);
        requestJson.setSign(sign);
        requestJson.setNationalCode(nationalCode);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, CASH_IN_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CashInResponse>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<PurchaseResponse> buy(String token, String uniqueIdentifier, String amount, String price, String commissionCurrency, String commission, String nationalCode, String currency, String merchantId, String walletAccountNumber, String sign, String additionalData, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        BuyWalletRequestJson requestJson = new BuyWalletRequestJson();
        requestJson.setUniqueIdentifier(uniqueIdentifier);
        requestJson.setAmount(amount);
        requestJson.setPrice(price);
        requestJson.setCommissionObject(new CommissionObject(commissionCurrency, commission));
        requestJson.setNationalCode(nationalCode);
        requestJson.setCurrency(currency);
        requestJson.setMerchantId(merchantId);
        requestJson.setWalletAccountNumber(walletAccountNumber);
        requestJson.setAdditionalData(additionalData);
        requestJson.setSign(sign);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, BUY_IN_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<PurchaseResponse>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<PurchaseResponse> sell(String token, String uniqueIdentifier,String amount, String price, String commissionCurrency, String commission,  String nationalCode, String currency, String merchantId, String walletAccountNumber, String sign, String additionalData, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        SellWalletRequestJson requestJson = new SellWalletRequestJson();
        requestJson.setUniqueIdentifier(uniqueIdentifier);
        requestJson.setAmount(amount);
        requestJson.setPrice(price);
        requestJson.setCommissionObject(new CommissionObject(commissionCurrency, commission));
        requestJson.setNationalCode(nationalCode);
        requestJson.setCurrency(currency);
        requestJson.setMerchantId(merchantId);
        requestJson.setWalletAccountNumber(walletAccountNumber);
        requestJson.setAdditionalData(additionalData);
        requestJson.setSign(sign);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(token, CELL_IN_PATH, mapToJson(requestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<PurchaseResponse>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<CashInTrackResponse> inquiryCashIn(String token, String uniqueIdentifier, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        MockHttpServletRequestBuilder postRequest = buildGetRequest(token, CASH_IN_INQUIRY_PATH + "/" + uniqueIdentifier);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CashInTrackResponse>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<CashInTrackResponse> inquiryPurchase(String token, String uniqueIdentifier, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        MockHttpServletRequestBuilder postRequest = buildGetRequest(token, PURCHASE_INQUIRY_IN_PATH + "/" + uniqueIdentifier);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<CashInTrackResponse>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    private String getSettingValue(String channelName, String limitationName, String accountNumber) throws Exception{
        WalletAccountEntity walletAccountEntity = walletAccountService.findByAccountNumber(accountNumber);
        return limitationGeneralCustomService.getSetting(channelService.getChannel(channelName),
                limitationName, walletAccountEntity.getWalletEntity().getWalletLevelEntity(),
                walletAccountEntity.getWalletAccountTypeEntity(), walletAccountEntity.getWalletAccountCurrencyEntity(), walletAccountEntity.getWalletEntity().getWalletTypeEntity());
    }

}
