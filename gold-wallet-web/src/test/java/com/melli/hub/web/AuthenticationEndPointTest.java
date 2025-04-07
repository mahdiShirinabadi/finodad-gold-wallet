package com.melli.hub.web;

import com.melli.hub.WalletApplicationTests;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Class Name: AuthenticationEndPOintTest
 * Author: Mahdi Shirinabadi
 * Date: 4/6/2025
 */
@Log4j2
@DisplayName("AuthenticationEndPoint End2End test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationEndPointTest extends WalletApplicationTests {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REFRESHTOKEN_PATH = "/api/v1/auth/refresh";
    private static final String LOGOUT_PATH = "/api/v1/auth/logout";
}
