package com.melli.hub.slack.impl;

import com.melli.hub.ChannelException;
import com.melli.hub.slack.SlackChannelInterface;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
@Profile({"dev","prod","staging"})
@Log4j2
public class SlackChannelInterfaceImplementation implements SlackChannelInterface {

    
    @Value("${slack.url}")
    private String url;

    @Value(value = "${slack.timeout}")
    private String timeout;

    @Override
    public void sendMessage(String message) throws ChannelException {
        log.info("start call slack ...({})", message);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setConfig(RequestConfig.custom().setSocketTimeout(Integer.parseInt(timeout)).setConnectTimeout(Integer.parseInt(timeout)).build());
        post.addHeader("Content-type", "application/json");
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("text", message);
            StringEntity params = new StringEntity(jsonRequest.toString(), "UTF-8");
            post.setEntity(params);
            CloseableHttpResponse response = httpClient.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
            String line = rd.lines().collect(Collectors.joining());
            if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                log.error("error in slack , and http status code ===> {}", response.getStatusLine().getStatusCode());
                throw new ChannelException("get error in slack request", GENERAL_ERROR,line,response.getStatusLine().getStatusCode(), line);
            }

            log.info("slack response, request ===> {}", line);
        } catch (IOException e) {
            log.error("io exception error in  slack request ===> {}", e.getMessage());
            throw new ChannelException("IO EXCEPTION is occurred in slack request!", TIME_OUT,"",-1,"");
        } catch (Exception e) {
            if (e instanceof ChannelException channelException) {
                throw new ChannelException(channelException.getMessage(), channelException.getResultCode(),"",-1,"");
            }
            log.error("general error in send message in slack", e);
            throw new ChannelException("General error is occurred in slack request!", GENERAL_ERROR,"",-1,"");
        }
    }


}
