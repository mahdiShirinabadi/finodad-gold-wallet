package com.melli.hub;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.http.impl.client.HttpClients.createDefault;

@Log4j2
public class WebCallUtils {

    public static final int GENERAL_ERROR = 999;
    public static final int TIME_OUT = 998;

    private WebCallUtils() {

    }

    public static String sendRequest(String classMethodName, HttpMethod httpMethod, URIBuilder builder, int timeout,
                                     Map<String, String> headers, Map<String, String> params, HttpEntity body, int[] successHttpStatusArray) throws ChannelException {
        log.info("({}), SendRequest for httpMethod({}), url({})", classMethodName, httpMethod, builder.toString());
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
            String url = builder.toString();
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .build();
            if (httpMethod == HttpMethod.GET) {
                return sendGet(classMethodName, url, requestConfig, headers, successHttpStatusArray);
            } else if (httpMethod == HttpMethod.POST) {
                return sendPost(classMethodName, url, requestConfig, headers, body, successHttpStatusArray);
            } else if (httpMethod == HttpMethod.PUT) {
                return sendPut(classMethodName, url, requestConfig, headers, body, successHttpStatusArray);
            } else if (httpMethod == HttpMethod.DELETE) {
                return sendDelete(classMethodName, url, requestConfig, headers, successHttpStatusArray);
            }
            log.error("({}), Method not found ,Method({})", classMethodName, httpMethod);
            throw new ChannelException("Exception in " + classMethodName, GENERAL_ERROR, "method not found", -1, "no implementation for method:" + classMethodName);
        } catch (ChannelException e) {
            log.error("({}), Exception with httpStatusCode ({})", classMethodName, e.getHttpStatusCode());
            throw new ChannelException("Exception in " + classMethodName, e.getResultCode(), e.getChannelMessage(), e.getHttpStatusCode(), e.getCompleteResponse() + classMethodName);
        } catch (IOException e) {
            log.error("({}), IOException ({})", classMethodName, e.getMessage());
            throw new ChannelException("IOException in " + classMethodName, TIME_OUT, "IOException in " + classMethodName, -1, "");
        }
    }

    public static String sendGet(String classMethodName, String url, RequestConfig requestConfig, Map<String, String> headers, int[] successHttpStatuses) throws IOException, ChannelException {
        log.info("({}), GetRequest for url({})", classMethodName, url);
        try (CloseableHttpClient httpClient = createDefault()) {
            HttpGet get = new HttpGet(url);
            get.setConfig(requestConfig);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                get.addHeader(entry.getKey(), entry.getValue());
            }
            return executeRequest(classMethodName, httpClient.execute(get), successHttpStatuses);
        }
    }

    public static String sendPost(String classMethodName, String url, RequestConfig requestConfig, Map<String, String> headers, HttpEntity body, int[] successHttpStatuses) throws IOException, ChannelException {
        log.info("({}), PostRequest for url({}), body({})", classMethodName, url, body);
        try (CloseableHttpClient httpClient = createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setConfig(requestConfig);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                post.addHeader(entry.getKey(), entry.getValue());
            }
            post.setEntity(body);
            return executeRequest(classMethodName, httpClient.execute(post), successHttpStatuses);
        }
    }

    public static String sendPut(String classMethodName, String url, RequestConfig requestConfig, Map<String, String> headers, HttpEntity body, int[] successHttpStatuses) throws IOException, ChannelException {
        log.info("({}), PutRequest for url({})", classMethodName, url);
        try (CloseableHttpClient httpClient = createDefault()) {
            HttpPut put = new HttpPut(url);
            put.setConfig(requestConfig);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                put.addHeader(entry.getKey(), entry.getValue());
            }
            put.setEntity(body);
            return executeRequest(classMethodName, httpClient.execute(put), successHttpStatuses);
        }
    }

    public static String sendDelete(String classMethodName, String url, RequestConfig requestConfig, Map<String, String> headers, int[] successHttpStatuses) throws IOException, ChannelException {
        log.info("({}), DeleteRequest for url({})", classMethodName, url);
        try (CloseableHttpClient httpClient = createDefault()) {
            HttpDelete delete = new HttpDelete(url);
            delete.setConfig(requestConfig);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                delete.addHeader(entry.getKey(), entry.getValue());
            }
            return executeRequest(classMethodName, httpClient.execute(delete), successHttpStatuses);
        }
    }

    public static String executeRequest(String classMethodName, CloseableHttpResponse execute, int[] successHttpStatusArray) throws ChannelException {
        log.info("({}), Execute request ...", classMethodName);
        BufferedReader rd;
        String line = "";

        if(execute.getEntity() != null) {
            try {
                rd = new BufferedReader(new InputStreamReader(execute.getEntity().getContent(), StandardCharsets.UTF_8));
                line = rd.lines().collect(Collectors.joining());
            } catch (IOException e) {
                throw new ChannelException("timeout error in " + classMethodName, TIME_OUT, "timeout", HttpStatus.REQUEST_TIMEOUT.value(), "timeout in ");
            }
        }else{
            log.info("response body is null and httpStatusCode is ({})", execute.getStatusLine().getStatusCode());
        }

        if (!ArrayUtils.contains(successHttpStatusArray, execute.getStatusLine().getStatusCode())) {
            log.error("({}), Error, httpStatus code ({}), line ({})", classMethodName, execute.getStatusLine().getStatusCode(), line);
            throw new ChannelException("General error in " + classMethodName, GENERAL_ERROR, line, execute.getStatusLine().getStatusCode(), line);
        }
        return line;
    }
}
