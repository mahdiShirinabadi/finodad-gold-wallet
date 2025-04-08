package com.melli.hub.web;

import com.melli.hub.WalletApplicationTests;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.login.LoginResponse;
import com.melli.hub.service.StatusService;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: WalletEndPointTest
 * Author: Mahdi Shirinabadi
 * Date: 4/7/2025
 */
@Log4j2
@DisplayName("WalletEndPoint End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WalletEndPointTest extends WalletApplicationTests {

    private static final String CREATE_WALLET_PATH = "/api/v1/auth/login";
    private static final String GET_DATA_WALLET_PATH = "/api/v1/auth/login";
    private static final String ACTIVE_WALLET_PATH = "/api/v1/auth/login";
    private static final String DIACTIVE_WALLET_PATH = "/api/v1/auth/login";

    private static MockMvc mockMvc;
    private static String ACCESS_TOKEN;
    private static String REFRESH_TOKEN;

    @Autowired
    private WebApplicationContext webApplicationContext;

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
        BaseResponse<LoginResponse> response = login(USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.OK,
                StatusService.SUCCESSFUL, true);
        ACCESS_TOKEN = response.getData().getAccessTokenObject().getToken();
        REFRESH_TOKEN = response.getData().getRefreshTokenObject().getToken();
    }
}
