package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.domain.master.entity.MerchantWalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.domain.master.persistence.MerchantWalletAccountCurrencyRepository;
import com.melli.wallet.domain.response.UuidResponse;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.purchase.MerchantResponse;
import com.melli.wallet.domain.response.wallet.CreateWalletResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.service.*;
import com.melli.wallet.security.RequestContext;
import lombok.extern.log4j.Log4j2;
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
 * Class Name: WalletEndPointTest
 * Author: Mahdi Shirinabadi
 * Date: 4/7/2025
 */
@Log4j2
@DisplayName("MerchantControllerTest End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MerchantControllerTest extends WalletApplicationTests {


    private static final String NATIONAL_CODE_CORRECT = "0077847660";
    private static final String MOBILE_CORRECT = "09124162337";
    
    // Test data for merchant balance operations
    private static final String VALID_AMOUNT = "1000000";
    private static final String INVALID_WALLET_ACCOUNT_NUMBER = "9999999999";
    private static final String INVALID_MERCHANT_ID = "999";
    private static final String INVALID_AMOUNT = "0";

    private static MockMvc mockMvc;
    private static String ACCESS_TOKEN;
    private static String REFRESH_TOKEN;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private WalletAccountCurrencyService walletAccountCurrencyService;
    @Autowired
    private Flyway flyway;
    @Autowired
    private CacheClearService cacheClearService;
    @Autowired
    private MerchantWalletAccountCurrencyRepository merchantWalletAccountCurrencyRepository;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private RequestContext requestContext;


    @Test
    @Order(2)
    @DisplayName("Initiate cache...")
    void initial() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);
        log.info("start cleaning initial values in test DB for purchase");
        flyway.clean();
        flyway.migrate();
        log.info("start cleaning initial values in test DB for purchase");
        cacheClearService.clearCache();
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
    @DisplayName("merchantFail-Currency not ")
    void merchantCurrencyNotFound() throws Exception {
        getMerchant(mockMvc, ACCESS_TOKEN, "NOTHING", HttpStatus.OK, StatusService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);
    }

    @Test
    @Order(21)
    @DisplayName("merchantSuccess-empty")
    void merchantSuccessEmpty() throws Exception {
        BaseResponse<MerchantResponse> response = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        if(response.getData().getMerchantObjectList() != null){
            log.error("merchant must be null");
            throw new Exception("merchant must be null");
        }
    }

    @Test
    @Order(22)
    @DisplayName("merchantSuccess-one record")
    void merchantSuccess() throws Exception {
        MerchantWalletAccountCurrencyEntity merchantWalletAccountCurrencyEntity = new MerchantWalletAccountCurrencyEntity();
        merchantWalletAccountCurrencyEntity.setMerchantEntity(merchantService.findById(1));
        merchantWalletAccountCurrencyEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyService.findCurrency("GOLD"));
        merchantWalletAccountCurrencyEntity.setCreatedAt(new Date());
        merchantWalletAccountCurrencyEntity.setCreatedBy("System");
        merchantWalletAccountCurrencyRepository.save(merchantWalletAccountCurrencyEntity);
        BaseResponse<MerchantResponse> response = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        if(response.getData().getMerchantObjectList().size() != 1){
            log.error("merchant must be one Record");
            throw new Exception("merchant must be one record");
        }
    }

    @Test
    @Order(23)
    @DisplayName("merchantSuccessEmptyWithRial")
    void merchantSuccessEmptyWithRial() throws Exception {
        BaseResponse<MerchantResponse> response = getMerchant(mockMvc, ACCESS_TOKEN, "RIAL", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        if(response.getData().getMerchantObjectList() != null){
            log.error("merchant must be null");
            throw new Exception("merchant must be null");
        }
    }

    // Test methods for merchant balance
    @Test
    @Order(25)
    @DisplayName("getMerchantBalanceSuccess")
    void getMerchantBalanceSuccess() throws Exception {
        log.info("start getMerchantBalanceSuccess test");
        
        // First get merchant data to ensure merchant exists
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(merchantResponse.getData());
        Assert.assertNotNull(merchantResponse.getData().getMerchantObjectList());
        Assert.assertTrue(merchantResponse.getData().getMerchantObjectList().size() > 0);
        
        // Get merchant ID from the first merchant
        String merchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        
        // Test the get balance endpoint
        BaseResponse<WalletBalanceResponse> response = getMerchantBalance(mockMvc, ACCESS_TOKEN, merchantId, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getWalletAccountObjectList());
        // The wallet account list might be empty if merchant has no accounts, which is valid
        log.info("Merchant balance retrieved successfully for merchantId: {}", merchantId);
    }

    @Test
    @Order(26)
    @DisplayName("getMerchantBalanceFail-MerchantNotFound")
    void getMerchantBalanceFailMerchantNotFound() throws Exception {
        log.info("start getMerchantBalanceFailMerchantNotFound test");
        getMerchantBalance(mockMvc, ACCESS_TOKEN, INVALID_MERCHANT_ID, HttpStatus.OK, StatusService.MERCHANT_IS_NOT_EXIST, false);
    }

    @Test
    @Order(27)
    @DisplayName("getMerchantBalanceFail-InvalidMerchantId")
    void getMerchantBalanceFailInvalidMerchantId() throws Exception {
        log.info("start getMerchantBalanceFailInvalidMerchantId test");
        // Test with invalid merchant ID format (non-numeric)
        getMerchantBalance(mockMvc, ACCESS_TOKEN, "invalid_merchant_id", HttpStatus.OK, StatusService.INPUT_PARAMETER_NOT_VALID, false);
    }



    // Test methods for increase balance
    @Test
    @Order(30)
    @DisplayName("increaseBalanceSuccess")
    void increaseBalanceSuccess() throws Exception {
        log.info("start increaseBalanceSuccess test");
        
        // First get merchant data to ensure merchant exists
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(merchantResponse.getData());
        Assert.assertNotNull(merchantResponse.getData().getMerchantObjectList());
        Assert.assertTrue(merchantResponse.getData().getMerchantObjectList().size() > 0);
        
        // Get merchant ID from the first merchant
        String merchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        
        // Get balance to find valid wallet account number
        WalletBalanceResponse balanceResponse = merchantService.getBalance(requestContext.getChannelEntity(), merchantId);
        Assert.assertNotNull(balanceResponse);
        Assert.assertNotNull(balanceResponse.getWalletAccountObjectList());
        Assert.assertTrue(balanceResponse.getWalletAccountObjectList().size() > 0);
        
        // Get wallet account number from the first account
        String walletGoldAccountNumber = null;
        String walletRialAccountNumber = null;
        for(WalletAccountObject walletAccountObject : balanceResponse.getWalletAccountObjectList()){
            if(walletAccountObject.getWalletAccountCurrencyObject().getName().equalsIgnoreCase("GOLD")){
                walletGoldAccountNumber = walletAccountObject.getAccountNumber();
            }
            if(walletAccountObject.getWalletAccountCurrencyObject().getName().equalsIgnoreCase("RIAL")){
                walletRialAccountNumber = walletAccountObject.getAccountNumber();
            }
        }

        // Now test the increase balance with valid data
        BaseResponse<String> responseGold = increaseMerchantBalance(mockMvc, ACCESS_TOKEN, walletGoldAccountNumber, VALID_AMOUNT, merchantId, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        BaseResponse<String> responseRial = increaseMerchantBalance(mockMvc, ACCESS_TOKEN, walletRialAccountNumber, VALID_AMOUNT, merchantId, HttpStatus.OK, StatusService.SUCCESSFUL, true);

        Assert.assertNotNull(responseGold.getData());
        Assert.assertTrue(responseGold.getData().contains("Balance increased successfully"));
        Assert.assertTrue(responseGold.getData().contains("TraceId:"));

        Assert.assertNotNull(responseRial.getData());
        Assert.assertTrue(responseRial.getData().contains("Balance increased successfully"));
        Assert.assertTrue(responseRial.getData().contains("TraceId:"));
    }

    @Test
    @Order(31)
    @DisplayName("increaseBalanceFail-MerchantNotFound")
    void increaseBalanceFailMerchantNotFound() throws Exception {
        log.info("start increaseBalanceFailMerchantNotFound test");
        // Get a valid wallet account number first
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String validWalletAccountNumber = "1234567890"; // Use a hardcoded value for failure test
        if (merchantResponse.getData().getMerchantObjectList() != null && !merchantResponse.getData().getMerchantObjectList().isEmpty()) {
            WalletBalanceResponse balanceResponse = merchantService.getBalance(requestContext.getChannelEntity(), String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId()));
            if (balanceResponse.getWalletAccountObjectList() != null && !balanceResponse.getWalletAccountObjectList().isEmpty()) {
                validWalletAccountNumber = balanceResponse.getWalletAccountObjectList().get(0).getAccountNumber();
            }
        }
        increaseMerchantBalance(mockMvc, ACCESS_TOKEN, validWalletAccountNumber, VALID_AMOUNT, INVALID_MERCHANT_ID, HttpStatus.OK, StatusService.MERCHANT_IS_NOT_EXIST, false);
    }

    @Test
    @Order(32)
    @DisplayName("increaseBalanceFail-WalletAccountNotFound")
    void increaseBalanceFailWalletAccountNotFound() throws Exception {
        log.info("start increaseBalanceFailWalletAccountNotFound test");
        // Get a valid merchant ID first
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String validMerchantId = "1"; // Default fallback
        if (merchantResponse.getData().getMerchantObjectList() != null && !merchantResponse.getData().getMerchantObjectList().isEmpty()) {
            validMerchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        }
        increaseMerchantBalance(mockMvc, ACCESS_TOKEN, INVALID_WALLET_ACCOUNT_NUMBER, VALID_AMOUNT, validMerchantId, HttpStatus.OK, StatusService.WALLET_ACCOUNT_NOT_FOUND, false);
    }

    @Test
    @Order(33)
    @DisplayName("increaseBalanceFail-WalletAccountNotBelongToMerchant")
    void increaseBalanceFailWalletAccountNotBelongToMerchant() throws Exception {
        log.info("start increaseBalanceFailWalletAccountNotBelongToMerchant test");
        // This test assumes there's a wallet account that doesn't belong to the merchant
        // Get a valid merchant ID first
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String validMerchantId = "1"; // Default fallback
        if (merchantResponse.getData().getMerchantObjectList() != null && !merchantResponse.getData().getMerchantObjectList().isEmpty()) {
            validMerchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        }
        increaseMerchantBalance(mockMvc, ACCESS_TOKEN, "9876543210", VALID_AMOUNT, validMerchantId, HttpStatus.OK, StatusService.WALLET_ACCOUNT_NOT_FOUND, false);
    }

    // Test methods for decrease balance

    @Test
    @Order(40)
    @DisplayName("decreaseBalanceSuccess")
    void decreaseBalanceSuccess() throws Exception {
        log.info("start decreaseBalanceSuccess test");
        
        // First get merchant data to ensure merchant exists
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        Assert.assertNotNull(merchantResponse.getData());
        Assert.assertNotNull(merchantResponse.getData().getMerchantObjectList());
        Assert.assertTrue(merchantResponse.getData().getMerchantObjectList().size() > 0);
        
        // Get merchant ID from the first merchant
        String merchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        
        // Get balance to find valid wallet account number
        WalletBalanceResponse balanceResponse = merchantService.getBalance(requestContext.getChannelEntity(), merchantId);
        Assert.assertNotNull(balanceResponse);
        Assert.assertNotNull(balanceResponse.getWalletAccountObjectList());
        Assert.assertTrue(balanceResponse.getWalletAccountObjectList().size() > 0);
        
        // Get wallet account number from the first account
        // Get wallet account number from the first account
        String walletGoldAccountNumber = null;
        String walletRialAccountNumber = null;
        for(WalletAccountObject walletAccountObject : balanceResponse.getWalletAccountObjectList()){
            if(walletAccountObject.getWalletAccountCurrencyObject().getName().equalsIgnoreCase("GOLD")){
                walletGoldAccountNumber = walletAccountObject.getAccountNumber();
            }
            if(walletAccountObject.getWalletAccountCurrencyObject().getName().equalsIgnoreCase("RIAL")){
                walletRialAccountNumber = walletAccountObject.getAccountNumber();
            }
        }
        
        // Now test the decrease balance with valid data
        BaseResponse<String> responseGold = decreaseMerchantBalance(mockMvc, ACCESS_TOKEN, walletGoldAccountNumber, VALID_AMOUNT, merchantId, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        BaseResponse<String> responseRial = decreaseMerchantBalance(mockMvc, ACCESS_TOKEN, walletRialAccountNumber, VALID_AMOUNT, merchantId, HttpStatus.OK, StatusService.SUCCESSFUL, true);

        Assert.assertNotNull(responseGold.getData());
        Assert.assertTrue(responseGold.getData().contains("Balance decreased successfully"));
        Assert.assertTrue(responseGold.getData().contains("TraceId:"));

        Assert.assertNotNull(responseRial.getData());
        Assert.assertTrue(responseRial.getData().contains("Balance decreased successfully"));
        Assert.assertTrue(responseRial.getData().contains("TraceId:"));
    }

    @Test
    @Order(41)
    @DisplayName("decreaseBalanceFail-MerchantNotFound")
    void decreaseBalanceFailMerchantNotFound() throws Exception {
        log.info("start decreaseBalanceFailMerchantNotFound test");
        // Get a valid wallet account number first
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String validWalletAccountNumber = "1234567890"; // Use a hardcoded value for failure test
        if (merchantResponse.getData().getMerchantObjectList() != null && !merchantResponse.getData().getMerchantObjectList().isEmpty()) {
            WalletBalanceResponse balanceResponse = merchantService.getBalance(requestContext.getChannelEntity(), String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId()));
            if (balanceResponse.getWalletAccountObjectList() != null && !balanceResponse.getWalletAccountObjectList().isEmpty()) {
                for(WalletAccountObject walletAccountObject : balanceResponse.getWalletAccountObjectList()){
                    if(walletAccountObject.getWalletAccountCurrencyObject().getName().equalsIgnoreCase("GOLD")){
                        validWalletAccountNumber = walletAccountObject.getAccountNumber();
                    }
                }
            }
        }
        decreaseMerchantBalance(mockMvc, ACCESS_TOKEN, validWalletAccountNumber, VALID_AMOUNT, INVALID_MERCHANT_ID, HttpStatus.OK, StatusService.MERCHANT_IS_NOT_EXIST, false);
    }

    @Test
    @Order(42)
    @DisplayName("decreaseBalanceFail-WalletAccountNotFound")
    void decreaseBalanceFailWalletAccountNotFound() throws Exception {
        log.info("start decreaseBalanceFailWalletAccountNotFound test");
        // Get a valid merchant ID first
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String validMerchantId = "1"; // Default fallback
        if (merchantResponse.getData().getMerchantObjectList() != null && !merchantResponse.getData().getMerchantObjectList().isEmpty()) {
            validMerchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        }
        decreaseMerchantBalance(mockMvc, ACCESS_TOKEN, INVALID_WALLET_ACCOUNT_NUMBER, VALID_AMOUNT, validMerchantId, HttpStatus.OK, StatusService.WALLET_ACCOUNT_NOT_FOUND, false);
    }

    @Test
    @Order(43)
    @DisplayName("decreaseBalanceFail-InsufficientBalance")
    void decreaseBalanceFailInsufficientBalance() throws Exception {
        log.info("start decreaseBalanceFailInsufficientBalance test");
        // This test assumes the merchant doesn't have enough balance
        // Get a valid merchant ID and wallet account number first
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String validMerchantId = "1"; // Default fallback
        String validWalletAccountNumber = null; // Default fallback
        if (merchantResponse.getData().getMerchantObjectList() != null && !merchantResponse.getData().getMerchantObjectList().isEmpty()) {
            validMerchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
            WalletBalanceResponse balanceResponse = merchantService.getBalance(requestContext.getChannelEntity(), validMerchantId);
            if (balanceResponse.getWalletAccountObjectList() != null && !balanceResponse.getWalletAccountObjectList().isEmpty()) {
                for(WalletAccountObject walletAccountObject : balanceResponse.getWalletAccountObjectList()){
                    if(walletAccountObject.getWalletAccountCurrencyObject().getName().equalsIgnoreCase("GOLD")){
                        validWalletAccountNumber = walletAccountObject.getAccountNumber();
                    }
                }
            }
        }
        decreaseMerchantBalance(mockMvc, ACCESS_TOKEN, validWalletAccountNumber, String.valueOf(Long.parseLong(VALID_AMOUNT) + 1), validMerchantId, HttpStatus.OK, StatusService.BALANCE_IS_NOT_ENOUGH, false);
    }

    @Test
    @Order(44)
    @DisplayName("decreaseBalanceFail-WalletAccountNotBelongToMerchant")
    void decreaseBalanceFailWalletAccountNotBelongToMerchant() throws Exception {
        log.info("start decreaseBalanceFailWalletAccountNotBelongToMerchant test");
        // This test assumes there's a wallet account that doesn't belong to the merchant
        // Get a valid merchant ID first
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusService.SUCCESSFUL, true);
        String validMerchantId = "1"; // Default fallback
        if (merchantResponse.getData().getMerchantObjectList() != null && !merchantResponse.getData().getMerchantObjectList().isEmpty()) {
            validMerchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        }
        decreaseMerchantBalance(mockMvc, ACCESS_TOKEN, "9876543210", VALID_AMOUNT, validMerchantId, HttpStatus.OK, StatusService.WALLET_ACCOUNT_NOT_FOUND, false);
    }

}
