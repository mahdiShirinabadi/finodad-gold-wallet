package com.melli.hub.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.melli.hub.WalletApplicationTests;
import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.request.login.RefreshTokenRequestJson;
import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.login.LoginResponse;
import com.melli.hub.service.ChannelService;
import com.melli.hub.service.StatusService;
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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Class Name: AuthenticationEndPOintTest
 * Author: Mahdi Shirinabadi
 * Date: 4/6/2025
 */
@Log4j2
@DisplayName("AuthenticationEndPoint End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationEndPointTest extends WalletApplicationTests {


    @Autowired
    private ChannelService channelService;


    private static final String REFRESHTOKEN_PATH = "/api/v1/auth/refresh";
    private static final String LOGOUT_PATH = "/api/v1/auth/logout";


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

    @Test
    @Order(11)
    @DisplayName("Channel login fail- invalid credential")
    void login_invalid_credential() throws Exception {
        log.info("start  test");
        BaseResponse<LoginResponse> response = login(USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.UNAUTHORIZED,
                StatusService.INVALID_USERNAME_PASSWORD, false);
        ACCESS_TOKEN = response.getData().getAccessTokenObject().getToken();
        REFRESH_TOKEN = response.getData().getRefreshTokenObject().getToken();
    }

    @Test
    @Order(12)
    @DisplayName("Channel login fail- invalid ip address")
    void login_ip_not_valid() throws Exception {
        log.info("start login for username ({})", USERNAME_CORRECT);


        log.info("start change ip for channel ({})", USERNAME_INCORRECT);
        ChannelEntity channel = channelService.findByUsername(USERNAME_CORRECT);
        String oldIp = channel.getIp();
        channel.setIp("1.1.1.1");
        channelService.save(channel);
        channelService.clearCacheAll();
        login(USERNAME_CORRECT, PASSWORD_CORRECT, HttpStatus.UNAUTHORIZED, StatusService.INVALID_IP_ADDRESS, false);

        channel.setIp(oldIp);
        channelService.save(channel);
        channelService.clearCacheAll();
    }

    @Test
    @Order(13)
    @DisplayName("refresh token- success")
    void refresh_success() throws Exception {
        log.info("start login for username ({})", USERNAME_CORRECT);
        BaseResponse<LoginResponse> loginResponse = refresh(REFRESH_TOKEN, USERNAME_CORRECT, HttpStatus.OK, StatusService.SUCCESSFUL, true);
        log.info("success refresh token " + REFRESH_TOKEN + "and new refreshToken is ({})", loginResponse.getData().getRefreshTokenObject().getToken());
        ACCESS_TOKEN = loginResponse.getData().getAccessTokenObject().getToken();
        REFRESH_TOKEN = loginResponse.getData().getRefreshTokenObject().getToken();
    }

    @Test
    @Order(13)
    @DisplayName("Channel login fail- invalid ip address")
    void refresh_fail_not_found() throws Exception {
        log.info("start login for username ({})", USERNAME_CORRECT);
        refresh("123i29312190381290312039823", USERNAME_CORRECT, HttpStatus.UNAUTHORIZED, StatusService.REFRESH_TOKEN_NOT_FOUND, false);
    }


    BaseResponse<ObjectUtils.Null> logout(HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        MockHttpServletRequestBuilder postRequest = buildPostRequest(ACCESS_TOKEN, LOGOUT_PATH);
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<ObjectUtils.Null>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }

    BaseResponse<LoginResponse> refresh(String refreshToken, String username, HttpStatus httpStatus, int errorCode, boolean success) throws Exception {
        RefreshTokenRequestJson refreshTokenRequestJson = new RefreshTokenRequestJson();
        refreshTokenRequestJson.setRefreshToken(refreshToken);
        refreshTokenRequestJson.setUsername(username);
        MockHttpServletRequestBuilder postRequest = buildPostRequest(null, LOGOUT_PATH, mapToJson(refreshTokenRequestJson));
        String response = performTest(mockMvc, postRequest, httpStatus, success, errorCode);
        TypeReference<BaseResponse<LoginResponse>> typeReference = new TypeReference<>() {};
        return objectMapper.readValue(response, typeReference);
    }
}
