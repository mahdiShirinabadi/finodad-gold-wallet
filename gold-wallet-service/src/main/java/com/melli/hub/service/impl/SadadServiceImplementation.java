package com.melli.hub.service.impl;

import com.melli.hub.ChannelException;
import com.melli.hub.domain.master.entity.ShahkarInfoEntity;
import com.melli.hub.domain.master.persistence.SadadDailyTokenRepository;
import com.melli.hub.domain.redis.SadadDailyTokenRedis;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.sadad.SadadChannelInterface;
import com.melli.hub.service.AlertService;
import com.melli.hub.service.SadadService;
import com.melli.hub.service.ShahkarInfoService;
import com.melli.hub.service.StatusService;
import com.melli.hub.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;


@Service
@Log4j2
@RequiredArgsConstructor
public class SadadServiceImplementation implements SadadService {
    private final SadadChannelInterface sadadChannel;
    private final ShahkarInfoService shahkarInfoService;
    private final SadadDailyTokenRepository sadadDailyTokenRepository;
    @Value("${sadad.client.id}")
    private String clientId;
    @Value("${sadad.client.secret}")
    private String clientSecret;
    private final Environment environment;


    @Override
    public Boolean shahkar(ShahkarInfoEntity shahkarInfoEntity) throws InternalServiceException {

        try {
            log.info("start call sadad shahkar inquiry for nationalCode ({})", shahkarInfoEntity.getNationalCode());
            String token = getTokenByScope(SadadChannelInterface.SCOPE_SHAHKAR);
            String response = sadadChannel.shahkar(token, shahkarInfoEntity.getNationalCode(), shahkarInfoEntity.getMobile());
            shahkarInfoEntity.setChannelResponse(response);
            JSONObject jsonObject = new JSONObject(response);
            JSONObject jsonObjectResponse = jsonObject.getJSONObject("response");
            if (jsonObjectResponse == null) {
                log.error("error in shahakr and response is null");
                shahkarInfoEntity.setIsMatch(false);
                throw new InternalServiceException("error in get shahkar info", StatusService.ERROR_IN_GET_SHAHKAR, HttpStatus.OK);
            }
            long code = jsonObjectResponse.optLong("response", -1);
            if (code == 200) {
                shahkarInfoEntity.setIsMatch(true);
                return true;
            }
            shahkarInfoEntity.setIsMatch(false);
            return false;
        } catch (ChannelException exception) {
            validateTokenInException(exception, SadadChannelInterface.SCOPE_SHAHKAR);
            log.error("error in inquiry nationalCode for {}, message ({})", shahkarInfoEntity.getNationalCode(), exception.getMessage());
            throw new InternalServiceException(exception.getMessage(), StatusService.ERROR_IN_GET_SHAHKAR, HttpStatus.OK);
        } catch (InternalServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("general error stack in inquiry shahkar for nationalCode ({}) is", shahkarInfoEntity.getNationalCode(), ex);
            throw new InternalServiceException(ex.getMessage(), StatusService.GENERAL_ERROR, HttpStatus.OK);
        } finally {
            shahkarInfoEntity.setChannelResponseTime(new Date());
            shahkarInfoService.save(shahkarInfoEntity);
        }
    }

    @Override
    public void sendSms(String message, String mobile) throws InternalServiceException {
        String token = getTokenByScope(SadadChannelInterface.SCOPE_SEND_SMS);
        try {
            log.info("start send message to mobile ({})", mobile);
            sadadChannel.sendSms(token, message, mobile, 5, 3);
        } catch (ChannelException ex) {
            validateTokenInException(ex, SadadChannelInterface.SCOPE_SEND_SMS);
            log.error("error in get sendSms to mobile ({}) and error is ({})", mobile, ex.getCompleteResponse());
            throw new InternalServiceException("error in sendSms to mobile " + mobile + ", error:" + ex.getChannelMessage(), StatusService.ERROR_IN_SEND_SMS, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("General error stack in sendSms for mobile ({}) is", mobile, ex);
        }
    }

    private String getTokenByScope(String scope) throws InternalServiceException {
        Optional<SadadDailyTokenRedis> sadadDailyTokenOptional = sadadDailyTokenRepository.findById(scope);

        if (sadadDailyTokenOptional.isPresent()) {
            long expireTime = sadadDailyTokenOptional.get().getExpireTime();
            if (expireTime > new Date().getTime() && StringUtils.hasText(sadadDailyTokenOptional.get().getToken())) {
                log.info("token for scope ({}) is valid until ({})", scope, sadadDailyTokenOptional.get().getExpireTime());
                return sadadDailyTokenOptional.get().getToken();
            } else {
                log.info("delete token for scope ({})", scope);
                sadadDailyTokenRepository.deleteById(scope);
            }
        }

        try {
            log.info("start get token for scope ({})", scope);
            String response = sadadChannel.getToken(clientId, clientSecret, scope);
            JSONObject jsonObject = new JSONObject(response);
            SadadDailyTokenRedis sadadDailyTokenRedis = new SadadDailyTokenRedis();
            sadadDailyTokenRedis.setScope(scope);
            sadadDailyTokenRedis.setExpireTime((Instant.now().getEpochSecond() + jsonObject.optLong("expires_in", 0L)) * 1000);
            sadadDailyTokenRedis.setToken(jsonObject.optString("access_token"));
            log.info("success get token for scope ({}), token start with ({})", scope, sadadDailyTokenRedis.getToken().substring(1, 20));
            sadadDailyTokenRepository.save(sadadDailyTokenRedis);

            return sadadDailyTokenRedis.getToken();
        } catch (ChannelException ex) {
            log.error("error in get token and error is", ex);
            throw new InternalServiceException(ex.getMessage(), StatusService.GENERAL_ERROR, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("general error in login and error is", ex);
            throw new InternalServiceException(ex.getMessage(), StatusService.GENERAL_ERROR, HttpStatus.OK);
        }
    }

    private void validateTokenInException(ChannelException channelException, String scope) {
        if (channelException.getHttpStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
            log.error("token with scope ({}) got httpStatus ({}) and system delete this token", scope, channelException.getHttpStatusCode());
            sadadDailyTokenRepository.deleteById(scope);
        }
    }

}
