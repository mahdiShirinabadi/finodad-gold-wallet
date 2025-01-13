package com.melli.hub;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.melli.hub.config.FlywayConfig;
import com.melli.hub.service.StatusService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
@SpringBootTest(classes = WalletApplication.class)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ExtendWith(SpringExtension.class)
public class WalletApplicationTests {

    public static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private FlywayConfig flywayConfig;

    private static boolean emptyDB = true;

    public ResultMatcher buildErrorCodeMatch(int errorCode) {
        if (errorCode == StatusService.SUCCESSFUL) {
            return jsonPath("$.errorDetail").doesNotExist();
        } else {
            return jsonPath("$.errorDetail.code").value(errorCode);
        }
    }

    public MockHttpServletRequestBuilder buildPostRequest(String token, String path) {
        return buildPostRequest(token, path, null, null);
    }

    public MockHttpServletRequestBuilder buildPostRequest(String token, String path, String body) {
        return buildPostRequest(token, path, body, null);
    }

    public MockHttpServletRequestBuilder buildPostRequest(String token, String path, String body, String clientIp) {
        log.info("start with path({}), token({}), body({}), clientIp({})", path, token, body, clientIp);
        MockHttpServletRequestBuilder requestBuilder = post(path);
        if (StringUtils.isNotBlank(token)) {
            requestBuilder.header("Authorization", "bearer " + token);
        }
        if (StringUtils.isNotBlank(clientIp)) {
            requestBuilder.remoteAddress(clientIp);
        }
        if (StringUtils.isNotBlank(body)) {
            requestBuilder.contentType(MediaType.APPLICATION_JSON);
            requestBuilder.content(body);
        }
        return requestBuilder;
    }

    public MockHttpServletRequestBuilder buildGetRequest(String token, String path, Object... uriVariables) {
        MockHttpServletRequestBuilder requestBuilder = get(path, uriVariables);
        if (StringUtils.isNotBlank(token)) {
            requestBuilder.header("Authorization", "bearer " + token);
        }
        return requestBuilder;
    }

    public MockHttpServletRequestBuilder buildGetRequest(String token, String path, Map<String, String> params, Object... uriVariables) {
        MockHttpServletRequestBuilder requestBuilder = get(path, uriVariables);
        if (StringUtils.isNotBlank(token)) {
            requestBuilder.header("Authorization", "bearer " + token);
        }
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                requestBuilder.param(entry.getKey(), entry.getValue());
            }
        }
        return requestBuilder;
    }

    @Test
    public void contextLoads() {
        log.info("contextLoads");
        Assert.assertNotNull(flywayConfig);
    }

    public boolean setupDB() {
        if (emptyDB) {
            log.info("start setting initial values in test DB");
            flywayConfig.clean();
            emptyDB = false;
        }
        return true;
    }

    public String performTest(MockMvc mockMvc, MockHttpServletRequestBuilder getRequest, HttpStatus httpStatus, boolean success, int errorCode) throws Exception {
        ResultMatcher errorCodeMatch = buildErrorCodeMatch(errorCode);

        MvcResult mvcResult = mockMvc.perform(getRequest)
                .andDo(print())
                .andExpect(status().is(httpStatus.value()))
                .andExpect(jsonPath("$.success").value(success))
                .andExpect(errorCodeMatch)
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        log.info("finish with response({})", response);
        return response;
    }

}
