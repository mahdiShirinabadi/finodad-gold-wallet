package com.melli.wallet.web;

import com.melli.wallet.WalletApplicationTests;
import com.melli.wallet.config.CacheClearService;
import com.melli.wallet.sync.ResourceSyncService;
import com.melli.wallet.domain.master.entity.MerchantWalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.persistence.MerchantWalletAccountCurrencyRepository;
import com.melli.wallet.domain.response.base.BaseResponse;
import com.melli.wallet.domain.response.login.LoginResponse;
import com.melli.wallet.domain.response.merchant.MerchantBalanceCalculationResponse;
import com.melli.wallet.domain.response.purchase.MerchantResponse;
import com.melli.wallet.domain.response.wallet.WalletAccountObject;
import com.melli.wallet.domain.response.wallet.WalletBalanceResponse;
import com.melli.wallet.security.RequestContext;
import com.melli.wallet.service.operation.MerchantOperationService;
import com.melli.wallet.service.repository.MerchantRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.service.repository.WalletAccountCurrencyRepositoryService;
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
 * Class Name: MerchantControllerTest
 * Author: Mahdi Shirinabadi
 * Date: 4/7/2025
 * 
 * This test class contains comprehensive end-to-end tests for Merchant operations.
 * It tests merchant balance management including increase, decrease, and balance inquiry operations.
 * 
 * Test Coverage:
 * - Merchant information retrieval (success and failure scenarios)
 * - Merchant balance inquiry (success and failure scenarios)
 * - Merchant balance increase operations (success and failure scenarios)
 * - Merchant balance decrease operations (success and failure scenarios)
 * - Currency validation
 * - Merchant ownership validation
 * - Balance sufficiency validation
 */
@Log4j2
@DisplayName("MerchantControllerTest End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MerchantControllerTest extends WalletApplicationTests {

    // Test data for merchant balance operations
    private static final String VALID_AMOUNT = "1000000";
    private static final String INVALID_WALLET_ACCOUNT_NUMBER = "9999999999";
    private static final String INVALID_MERCHANT_ID = "999";

    private static MockMvc mockMvc;
    private static String ACCESS_TOKEN;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService;
    @Autowired
    private Flyway flyway;
    @Autowired
    private CacheClearService cacheClearService;
    @Autowired
    private MerchantWalletAccountCurrencyRepository merchantWalletAccountCurrencyRepository;
    @Autowired
    private MerchantRepositoryService merchantRepositoryService;
    @Autowired
    private MerchantOperationService merchantOperationService;
    @Autowired
    private RequestContext requestContext;
    @Autowired
    private ResourceSyncService resourceSyncService;


    /**
     * Initial setup method that runs before all tests.
     * This method:
     * - Sets up MockMvc for testing
     * - Cleans and migrates the database
     * - Clears all caches
     */
    @Test
    @Order(2)
    @DisplayName("Initiate cache...")
    void initial() {
        // Setup MockMvc for testing with security
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        Assert.assertNotNull(mockMvc);
        
        // Clean and migrate database
        log.info("start cleaning initial values in test DB for purchase");
        flyway.clean();
        flyway.migrate();
        resourceSyncService.syncResourcesOnStartup();
        log.info("start cleaning initial values in test DB for purchase");
        
        // Clear all caches
        cacheClearService.clearCache();
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
        ACCESS_TOKEN = response.getData().getAccessTokenObject().getToken();
    }


    /**
     * Test merchant retrieval failure with invalid currency.
     * This method:
     * - Attempts to get merchants with non-existent currency "NOTHING"
     * - Expects WALLET_ACCOUNT_CURRENCY_NOT_FOUND error
     */
    @Test
    @Order(20)
    @DisplayName("merchantFail-Currency not ")
    void merchantCurrencyNotFound() throws Exception {
        getMerchant(mockMvc, ACCESS_TOKEN, "NOTHING", HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);
    }

    /**
     * Test successful merchant retrieval with empty result for GOLD currency.
     * This method:
     * - Retrieves merchants for GOLD currency
     * - Validates that the result is empty (no merchants found)
     */
    @Test
    @Order(21)
    @DisplayName("merchantSuccess-empty")
    void merchantSuccessEmpty() throws Exception {
        BaseResponse<MerchantResponse> response = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        if(response.getData().getMerchantObjectList() != null){
            log.error("merchant must be null");
            throw new Exception("merchant must be null");
        }
    }

    /**
     * Test successful merchant retrieval with one record for GOLD currency.
     * This method:
     * - Creates a merchant wallet account currency entity for GOLD
     * - Saves it to the database
     * - Retrieves merchants for GOLD currency
     * - Validates that exactly one merchant is found
     */
    @Test
    @Order(22)
    @DisplayName("merchantSuccess-one record")
    void merchantSuccess() throws Exception {
        // Create merchant wallet account currency entity for GOLD
        MerchantWalletAccountCurrencyEntity merchantWalletAccountCurrencyEntity = new MerchantWalletAccountCurrencyEntity();
        merchantWalletAccountCurrencyEntity.setMerchantEntity(merchantRepositoryService.findById(1));
        merchantWalletAccountCurrencyEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyRepositoryService.findCurrency("GOLD"));
        merchantWalletAccountCurrencyEntity.setCreatedAt(new Date());
        merchantWalletAccountCurrencyEntity.setCreatedBy("System");
        merchantWalletAccountCurrencyRepository.save(merchantWalletAccountCurrencyEntity);
        BaseResponse<MerchantResponse> response = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        if(response.getData().getMerchantObjectList().size() != 1){
            log.error("merchant must be one Record");
            throw new Exception("merchant must be one record");
        }
    }

    /**
     * Test successful merchant retrieval with empty result for RIAL currency.
     * This method:
     * - Retrieves merchants for RIAL currency
     * - Validates that the result is empty (no merchants found)
     */
    @Test
    @Order(23)
    @DisplayName("merchantSuccessEmptyWithRial")
    void merchantSuccessEmptyWithRial() throws Exception {
        BaseResponse<MerchantResponse> response = getMerchant(mockMvc, ACCESS_TOKEN, "RIAL", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        if(response.getData().getMerchantObjectList() != null){
            log.error("merchant must be null");
            throw new Exception("merchant must be null");
        }
    }

    /**
     * Test successful merchant balance retrieval.
     * This method:
     * - Gets merchant data to ensure merchant exists
     * - Retrieves merchant ID from the first merchant
     * - Calls get balance endpoint
     * - Validates the balance response
     */
    @Test
    @Order(25)
    @DisplayName("getMerchantBalanceSuccess")
    void getMerchantBalanceSuccess() throws Exception {
        log.info("start getMerchantBalanceSuccess test");
        
        // Step 1: Get merchant data to ensure merchant exists
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(merchantResponse.getData());
        Assert.assertNotNull(merchantResponse.getData().getMerchantObjectList());
        Assert.assertTrue(merchantResponse.getData().getMerchantObjectList().size() > 0);
        
        // Step 2: Get merchant ID from the first merchant
        String merchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        
        // Step 3: Test the get balance endpoint
        BaseResponse<WalletBalanceResponse> response = getMerchantBalance(mockMvc, ACCESS_TOKEN, merchantId, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getWalletAccountObjectList());
        // The wallet account list might be empty if merchant has no accounts, which is valid
        log.info("Merchant balance retrieved successfully for merchantId: {}", merchantId);
    }

    /**
     * Test merchant balance retrieval failure when merchant not found.
     * This method:
     * - Attempts to get balance for non-existent merchant ID
     * - Expects MERCHANT_IS_NOT_EXIST error
     */
    @Test
    @Order(26)
    @DisplayName("getMerchantBalanceFail-MerchantNotFound")
    void getMerchantBalanceFailMerchantNotFound() throws Exception {
        log.info("start getMerchantBalanceFailMerchantNotFound test");
        getMerchantBalance(mockMvc, ACCESS_TOKEN, INVALID_MERCHANT_ID, HttpStatus.OK, StatusRepositoryService.MERCHANT_IS_NOT_EXIST, false);
    }

    /**
     * Test merchant balance retrieval failure with invalid merchant ID format.
     * This method:
     * - Attempts to get balance with non-numeric merchant ID
     * - Expects INPUT_PARAMETER_NOT_VALID error
     */
    @Test
    @Order(27)
    @DisplayName("getMerchantBalanceFail-InvalidMerchantId")
    void getMerchantBalanceFailInvalidMerchantId() throws Exception {
        log.info("start getMerchantBalanceFailInvalidMerchantId test");
        
        // Test with invalid merchant ID format (non-numeric)
        getMerchantBalance(mockMvc, ACCESS_TOKEN, "invalid_merchant_id", HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);
    }

    /**
     * Test successful merchant balance calculation from transactions.
     * This method:
     * - Gets merchant data to ensure merchant exists
     * - Retrieves merchant ID from the first merchant
     * - Calls calculate balance endpoint
     * - Validates the calculated balance response
     */
    @Test
    @Order(28)
    @DisplayName("getMerchantBalanceCalculatedSuccess")
    void getMerchantBalanceCalculatedSuccess() throws Exception {
        log.info("start getMerchantBalanceCalculatedSuccess test");
        
        // Step 1: Get merchant data to ensure merchant exists
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(merchantResponse.getData());
        Assert.assertNotNull(merchantResponse.getData().getMerchantObjectList());
        Assert.assertTrue(merchantResponse.getData().getMerchantObjectList().size() > 0);
        
        // Step 2: Get merchant ID from the first merchant
        String merchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        
        // Step 3: Test the calculate balance endpoint
        BaseResponse<MerchantBalanceCalculationResponse> response = getMerchantBalanceCalculated(mockMvc, ACCESS_TOKEN, merchantId, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(response.getData());
        Assert.assertNotNull(response.getData().getBalance());
        Assert.assertEquals(merchantId, response.getData().getMerchantId());
        log.info("Merchant balance calculated successfully for merchantId: {} with balance: {}", merchantId, response.getData().getBalance());
    }

    /**
     * Test merchant balance calculation failure when merchant not found.
     * This method:
     * - Attempts to calculate balance for non-existent merchant ID
     * - Expects MERCHANT_IS_NOT_EXIST error
     */
    @Test
    @Order(29)
    @DisplayName("getMerchantBalanceCalculatedFail-MerchantNotFound")
    void getMerchantBalanceCalculatedFailMerchantNotFound() throws Exception {
        log.info("start getMerchantBalanceCalculatedFailMerchantNotFound test");
        getMerchantBalanceCalculated(mockMvc, ACCESS_TOKEN, INVALID_MERCHANT_ID, "GOLD", HttpStatus.OK, StatusRepositoryService.MERCHANT_IS_NOT_EXIST, false);
    }

    /**
     * Test merchant balance calculation failure with invalid merchant ID format.
     * This method:
     * - Attempts to calculate balance with non-numeric merchant ID
     * - Expects INPUT_PARAMETER_NOT_VALID error
     */
    @Test
    @Order(30)
    @DisplayName("getMerchantBalanceCalculatedFail-InvalidMerchantId")
    void getMerchantBalanceCalculatedFailInvalidMerchantId() throws Exception {
        log.info("start getMerchantBalanceCalculatedFailInvalidMerchantId test");
        
        // Test with invalid merchant ID format (non-numeric)
        getMerchantBalanceCalculated(mockMvc, ACCESS_TOKEN, "invalid_merchant_id", "GOLD", HttpStatus.OK, StatusRepositoryService.INPUT_PARAMETER_NOT_VALID, false);
    }

    /**
     * Test merchant balance calculation failure with invalid currency.
     * This method:
     * - Attempts to calculate balance with invalid currency
     * - Expects CURRENCY_NOT_FOUND error
     */
    @Test
    @Order(31)
    @DisplayName("getMerchantBalanceCalculatedFail-InvalidCurrency")
    void getMerchantBalanceCalculatedFailInvalidCurrency() throws Exception {
        log.info("start getMerchantBalanceCalculatedFailInvalidCurrency test");
        
        // Test with invalid currency
        getMerchantBalanceCalculated(mockMvc, ACCESS_TOKEN, "1", "INVALID_CURRENCY", HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, false);
    }



    /**
     * Test successful merchant balance increase operation.
     * This method:
     * - Gets merchant data to ensure merchant exists
     * - Retrieves merchant ID and wallet account numbers
     * - Increases balance for both GOLD and RIAL accounts
     * - Validates the increase operation responses
     */
    @Test
    @Order(30)
    @DisplayName("increaseBalanceSuccess")
    void increaseBalanceSuccess() throws Exception {
        log.info("start increaseBalanceSuccess test");
        
        // Step 1: Get merchant data to ensure merchant exists
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(merchantResponse.getData());
        Assert.assertNotNull(merchantResponse.getData().getMerchantObjectList());
        Assert.assertTrue(merchantResponse.getData().getMerchantObjectList().size() > 0);
        
        // Step 2: Get merchant ID from the first merchant
        String merchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        
        // Step 3: Get balance to find valid wallet account numbers
        WalletBalanceResponse balanceResponse = merchantOperationService.getBalance(requestContext.getChannelEntity(), merchantId);
        Assert.assertNotNull(balanceResponse);
        Assert.assertNotNull(balanceResponse.getWalletAccountObjectList());
        Assert.assertTrue(balanceResponse.getWalletAccountObjectList().size() > 0);
        
        // Step 4: Extract wallet account numbers for GOLD and RIAL
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

        // Step 5: Test the increase balance with valid data for both currencies
        BaseResponse<String> responseGold = increaseMerchantBalance(mockMvc, ACCESS_TOKEN, walletGoldAccountNumber, VALID_AMOUNT, merchantId, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        BaseResponse<String> responseRial = increaseMerchantBalance(mockMvc, ACCESS_TOKEN, walletRialAccountNumber, VALID_AMOUNT, merchantId, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        Assert.assertNotNull(responseGold.getData());
        Assert.assertTrue(responseGold.getData().contains("Balance increased successfully"));
        Assert.assertTrue(responseGold.getData().contains("TraceId:"));

        Assert.assertNotNull(responseRial.getData());
        Assert.assertTrue(responseRial.getData().contains("Balance increased successfully"));
        Assert.assertTrue(responseRial.getData().contains("TraceId:"));
    }

    /**
     * Test merchant balance increase failure when merchant not found.
     * This method:
     * - Gets a valid wallet account number
     * - Attempts to increase balance for non-existent merchant ID
     * - Expects MERCHANT_IS_NOT_EXIST error
     */
    @Test
    @Order(31)
    @DisplayName("increaseBalanceFail-MerchantNotFound")
    void increaseBalanceFailMerchantNotFound() throws Exception {
        log.info("start increaseBalanceFailMerchantNotFound test");
        
        // Step 1: Get a valid wallet account number first
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String validWalletAccountNumber = "1234567890"; // Use a hardcoded value for failure test
        if (merchantResponse.getData().getMerchantObjectList() != null && !merchantResponse.getData().getMerchantObjectList().isEmpty()) {
            WalletBalanceResponse balanceResponse = merchantOperationService.getBalance(requestContext.getChannelEntity(), String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId()));
            if (balanceResponse.getWalletAccountObjectList() != null && !balanceResponse.getWalletAccountObjectList().isEmpty()) {
                validWalletAccountNumber = balanceResponse.getWalletAccountObjectList().get(0).getAccountNumber();
            }
        }
        
        // Step 2: Attempt to increase balance for non-existent merchant ID
        increaseMerchantBalance(mockMvc, ACCESS_TOKEN, validWalletAccountNumber, VALID_AMOUNT, INVALID_MERCHANT_ID, HttpStatus.OK, StatusRepositoryService.MERCHANT_IS_NOT_EXIST, false);
    }

    /**
     * Test merchant balance increase failure when wallet account not found.
     * This method:
     * - Gets a valid merchant ID
     * - Attempts to increase balance for non-existent wallet account number
     * - Expects WALLET_ACCOUNT_NOT_FOUND error
     */
    @Test
    @Order(32)
    @DisplayName("increaseBalanceFail-WalletAccountNotFound")
    void increaseBalanceFailWalletAccountNotFound() throws Exception {
        log.info("start increaseBalanceFailWalletAccountNotFound test");
        // Get a valid merchant ID first
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String validMerchantId = "1"; // Default fallback
        if (merchantResponse.getData().getMerchantObjectList() != null && !merchantResponse.getData().getMerchantObjectList().isEmpty()) {
            validMerchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        }
        increaseMerchantBalance(mockMvc, ACCESS_TOKEN, INVALID_WALLET_ACCOUNT_NUMBER, VALID_AMOUNT, validMerchantId, HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, false);
    }

    /**
     * Test merchant balance increase failure when wallet account doesn't belong to merchant.
     * This method:
     * - Gets a valid merchant ID
     * - Attempts to increase balance for wallet account that doesn't belong to the merchant
     * - Expects WALLET_ACCOUNT_NOT_FOUND error
     */
    @Test
    @Order(33)
    @DisplayName("increaseBalanceFail-WalletAccountNotBelongToMerchant")
    void increaseBalanceFailWalletAccountNotBelongToMerchant() throws Exception {
        log.info("start increaseBalanceFailWalletAccountNotBelongToMerchant test");
        // This test assumes there's a wallet account that doesn't belong to the merchant
        // Get a valid merchant ID first
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String validMerchantId = "1"; // Default fallback
        if (merchantResponse.getData().getMerchantObjectList() != null && !merchantResponse.getData().getMerchantObjectList().isEmpty()) {
            validMerchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        }
        increaseMerchantBalance(mockMvc, ACCESS_TOKEN, "9876543210", VALID_AMOUNT, validMerchantId, HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, false);
    }

    /**
     * Test successful merchant balance decrease operation.
     * This method:
     * - Gets merchant data to ensure merchant exists
     * - Retrieves merchant ID and wallet account numbers
     * - Decreases balance for both GOLD and RIAL accounts
     * - Validates the decrease operation responses
     */
    @Test
    @Order(40)
    @DisplayName("decreaseBalanceSuccess")
    void decreaseBalanceSuccess() throws Exception {
        log.info("start decreaseBalanceSuccess test");
        
        // Step 1: Get merchant data to ensure merchant exists
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        Assert.assertNotNull(merchantResponse.getData());
        Assert.assertNotNull(merchantResponse.getData().getMerchantObjectList());
        Assert.assertTrue(merchantResponse.getData().getMerchantObjectList().size() > 0);
        
        // Step 2: Get merchant ID from the first merchant
        String merchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        
        // Step 3: Get balance to find valid wallet account numbers
        WalletBalanceResponse balanceResponse = merchantOperationService.getBalance(requestContext.getChannelEntity(), merchantId);
        Assert.assertNotNull(balanceResponse);
        Assert.assertNotNull(balanceResponse.getWalletAccountObjectList());
        Assert.assertTrue(balanceResponse.getWalletAccountObjectList().size() > 0);
        
        // Step 4: Extract wallet account numbers for GOLD and RIAL
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
        
        // Step 5: Test the decrease balance with valid data for both currencies
        BaseResponse<String> responseGold = decreaseMerchantBalance(mockMvc, ACCESS_TOKEN, walletGoldAccountNumber, VALID_AMOUNT, merchantId, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        BaseResponse<String> responseRial = decreaseMerchantBalance(mockMvc, ACCESS_TOKEN, walletRialAccountNumber, VALID_AMOUNT, merchantId, HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);

        Assert.assertNotNull(responseGold.getData());
        Assert.assertTrue(responseGold.getData().contains("Balance decreased successfully"));
        Assert.assertTrue(responseGold.getData().contains("TraceId:"));

        Assert.assertNotNull(responseRial.getData());
        Assert.assertTrue(responseRial.getData().contains("Balance decreased successfully"));
        Assert.assertTrue(responseRial.getData().contains("TraceId:"));
    }

    /**
     * Test merchant balance decrease failure when merchant not found.
     * This method:
     * - Gets a valid wallet account number
     * - Attempts to decrease balance for non-existent merchant ID
     * - Expects MERCHANT_IS_NOT_EXIST error
     */
    @Test
    @Order(41)
    @DisplayName("decreaseBalanceFail-MerchantNotFound")
    void decreaseBalanceFailMerchantNotFound() throws Exception {
        log.info("start decreaseBalanceFailMerchantNotFound test");
        // Get a valid wallet account number first
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String validWalletAccountNumber = "1234567890"; // Use a hardcoded value for failure test
        if (merchantResponse.getData().getMerchantObjectList() != null && !merchantResponse.getData().getMerchantObjectList().isEmpty()) {
            WalletBalanceResponse balanceResponse = merchantOperationService.getBalance(requestContext.getChannelEntity(), String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId()));
            if (balanceResponse.getWalletAccountObjectList() != null && !balanceResponse.getWalletAccountObjectList().isEmpty()) {
                for(WalletAccountObject walletAccountObject : balanceResponse.getWalletAccountObjectList()){
                    if(walletAccountObject.getWalletAccountCurrencyObject().getName().equalsIgnoreCase("GOLD")){
                        validWalletAccountNumber = walletAccountObject.getAccountNumber();
                    }
                }
            }
        }
        decreaseMerchantBalance(mockMvc, ACCESS_TOKEN, validWalletAccountNumber, VALID_AMOUNT, INVALID_MERCHANT_ID, HttpStatus.OK, StatusRepositoryService.MERCHANT_IS_NOT_EXIST, false);
    }

    /**
     * Test merchant balance decrease failure when wallet account not found.
     * This method:
     * - Gets a valid merchant ID
     * - Attempts to decrease balance for non-existent wallet account number
     * - Expects WALLET_ACCOUNT_NOT_FOUND error
     */
    @Test
    @Order(42)
    @DisplayName("decreaseBalanceFail-WalletAccountNotFound")
    void decreaseBalanceFailWalletAccountNotFound() throws Exception {
        log.info("start decreaseBalanceFailWalletAccountNotFound test");
        // Get a valid merchant ID first
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String validMerchantId = "1"; // Default fallback
        if (merchantResponse.getData().getMerchantObjectList() != null && !merchantResponse.getData().getMerchantObjectList().isEmpty()) {
            validMerchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        }
        decreaseMerchantBalance(mockMvc, ACCESS_TOKEN, INVALID_WALLET_ACCOUNT_NUMBER, VALID_AMOUNT, validMerchantId, HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, false);
    }

    /**
     * Test merchant balance decrease failure when balance is insufficient.
     * This method:
     * - Gets a valid merchant ID and wallet account number
     * - Attempts to decrease balance when merchant doesn't have enough balance
     * - Expects BALANCE_IS_NOT_ENOUGH error
     */
    @Test
    @Order(43)
    @DisplayName("decreaseBalanceFail-InsufficientBalance")
    void decreaseBalanceFailInsufficientBalance() throws Exception {
        log.info("start decreaseBalanceFailInsufficientBalance test");
        // This test assumes the merchant doesn't have enough balance
        // Get a valid merchant ID and wallet account number first
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String validMerchantId = "1"; // Default fallback
        String validWalletAccountNumber = null; // Default fallback
        if (merchantResponse.getData().getMerchantObjectList() != null && !merchantResponse.getData().getMerchantObjectList().isEmpty()) {
            validMerchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
            WalletBalanceResponse balanceResponse = merchantOperationService.getBalance(requestContext.getChannelEntity(), validMerchantId);
            if (balanceResponse.getWalletAccountObjectList() != null && !balanceResponse.getWalletAccountObjectList().isEmpty()) {
                for(WalletAccountObject walletAccountObject : balanceResponse.getWalletAccountObjectList()){
                    if(walletAccountObject.getWalletAccountCurrencyObject().getName().equalsIgnoreCase("GOLD")){
                        validWalletAccountNumber = walletAccountObject.getAccountNumber();
                    }
                }
            }
        }
        decreaseMerchantBalance(mockMvc, ACCESS_TOKEN, validWalletAccountNumber, String.valueOf(Long.parseLong(VALID_AMOUNT) + 1), validMerchantId, HttpStatus.OK, StatusRepositoryService.BALANCE_IS_NOT_ENOUGH, false);
    }

    /**
     * Test merchant balance decrease failure when wallet account doesn't belong to merchant.
     * This method:
     * - Gets a valid merchant ID
     * - Attempts to decrease balance for wallet account that doesn't belong to the merchant
     * - Expects WALLET_ACCOUNT_NOT_FOUND error
     */
    @Test
    @Order(44)
    @DisplayName("decreaseBalanceFail-WalletAccountNotBelongToMerchant")
    void decreaseBalanceFailWalletAccountNotBelongToMerchant() throws Exception {
        log.info("start decreaseBalanceFailWalletAccountNotBelongToMerchant test");
        // This test assumes there's a wallet account that doesn't belong to the merchant
        // Get a valid merchant ID first
        BaseResponse<MerchantResponse> merchantResponse = getMerchant(mockMvc, ACCESS_TOKEN, "GOLD", HttpStatus.OK, StatusRepositoryService.SUCCESSFUL, true);
        String validMerchantId = "1"; // Default fallback
        if (merchantResponse.getData().getMerchantObjectList() != null && !merchantResponse.getData().getMerchantObjectList().isEmpty()) {
            validMerchantId = String.valueOf(merchantResponse.getData().getMerchantObjectList().get(0).getId());
        }
        decreaseMerchantBalance(mockMvc, ACCESS_TOKEN, "9876543210", VALID_AMOUNT, validMerchantId, HttpStatus.OK, StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, false);
    }

}
