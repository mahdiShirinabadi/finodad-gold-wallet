package com.melli.wallet.service.operation.impl;

import com.melli.wallet.ChannelException;
import com.melli.wallet.domain.master.entity.CashOutRequestEntity;
import com.melli.wallet.domain.master.entity.FundTransferAccountToAccountRequestEntity;
import com.melli.wallet.domain.master.entity.ShahkarInfoEntity;
import com.melli.wallet.domain.master.persistence.SadadDailyTokenRepository;
import com.melli.wallet.domain.redis.SadadDailyTokenRedis;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.sadad.SadadChannelInterface;
import com.melli.wallet.service.operation.SadadOperationService;
import com.melli.wallet.service.repository.ShahkarInfoRepositoryService;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.util.CustomStringUtils;
import com.melli.wallet.util.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static com.melli.wallet.utils.Helper.isValidJson;


@Service
@Log4j2
@RequiredArgsConstructor
public class SadadOperationServiceImplementation implements SadadOperationService {
    private final SadadChannelInterface sadadChannel;
    private final ShahkarInfoRepositoryService shahkarInfoRepositoryService;
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
                throw new InternalServiceException("error in get shahkar info", StatusRepositoryService.ERROR_IN_GET_SHAHKAR, HttpStatus.OK);
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
            throw new InternalServiceException(exception.getMessage(), StatusRepositoryService.ERROR_IN_GET_SHAHKAR, HttpStatus.OK);
        } catch (InternalServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("general error stack in inquiry shahkar for nationalCode ({}) is", shahkarInfoEntity.getNationalCode(), ex);
            throw new InternalServiceException(ex.getMessage(), StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
        } finally {
            shahkarInfoEntity.setChannelResponseTime(new Date());
            shahkarInfoRepositoryService.save(shahkarInfoEntity);
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
            throw new InternalServiceException("error in sendSms to mobile " + mobile + ", error:" + ex.getChannelMessage(), StatusRepositoryService.ERROR_IN_SEND_SMS, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("General error stack in sendSms for mobile ({}) is", mobile, ex);
        }
    }

    private String getTokenByScope(String scope) throws InternalServiceException {
        Optional<SadadDailyTokenRedis> sadadDailyTokenOptional = sadadDailyTokenRepository.findById(scope);

        if (sadadDailyTokenOptional.isPresent()) {
            long expireTime = sadadDailyTokenOptional.get().getExpireTime();
            if (expireTime > new Date().getTime() && CustomStringUtils.hasText(sadadDailyTokenOptional.get().getToken())) {
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
            throw new InternalServiceException(ex.getMessage(), StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("general error in login and error is", ex);
            throw new InternalServiceException(ex.getMessage(), StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
        }
    }

    private void validateTokenInException(ChannelException channelException, String scope) {
        if (channelException.getHttpStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
            log.error("token with scope ({}) got httpStatus ({}) and system delete this token", scope, channelException.getHttpStatusCode());
            sadadDailyTokenRepository.deleteById(scope);
        }
    }

    @Override
    public String accountToAccount(FundTransferAccountToAccountRequestEntity fundTransferAccountToAccountRequestEntity) throws InternalServiceException {
        String token = getTokenByScope(SadadChannelInterface.SCOPE_FUND_TRANSFER);

        String uuid = fundTransferAccountToAccountRequestEntity.getRrnEntity().getUuid();
        log.info("start accountToAccount from account ({}), to account ({}), amount ({}), uid ({})",
                fundTransferAccountToAccountRequestEntity.getFromAccount(), fundTransferAccountToAccountRequestEntity.getToAccount(), fundTransferAccountToAccountRequestEntity.getAmount(), uuid);
        try {
            log.info("Start call fundTransfer for uuid ({})", uuid);
            fundTransferAccountToAccountRequestEntity.setChannelRequestTime(new Date());
            String response = sadadChannel.fundTransfer(token, fundTransferAccountToAccountRequestEntity.getTraceNumber(),
                    String.valueOf(fundTransferAccountToAccountRequestEntity.getId()), fundTransferAccountToAccountRequestEntity.getFromAccount(),
                    fundTransferAccountToAccountRequestEntity.getToAccount(), String.valueOf(fundTransferAccountToAccountRequestEntity.getAmount()), new Date(),
                    String.valueOf(fundTransferAccountToAccountRequestEntity.getId()));
            log.info("response with fundTransfer uuid ({}), response ({})", uuid, response);
            processAccountToAccountResponse(fundTransferAccountToAccountRequestEntity, uuid, response);
            fundTransferAccountToAccountRequestEntity.setChannelResponse(response);
            return response;
        } catch (ChannelException ex) {
            log.error("get error in accountToAccount for uuid ({}) and errorCode ({}), errorMessage ({}), response ({})", uuid,
                    ex.getResultCode(), ex.getChannelMessage(), ex.getCompleteResponse());
            validateTokenInException(ex, SadadChannelInterface.SCOPE_FUND_TRANSFER);
            fundTransferAccountToAccountRequestEntity.setChannelResponse(ex.getCompleteResponse());
            fundTransferAccountToAccountRequestEntity.setResult(ex.getResultCode());
            if (isValidJson(ex.getCompleteResponse())) {
                JSONObject jsonObjectFail = new JSONObject(ex.getCompleteResponse());
                JSONArray jsonArray = jsonObjectFail.optJSONArray("notifications");
                JSONObject failObject = jsonArray != null ? jsonArray.optJSONObject(0) : null;
                String message = failObject != null ? failObject.getString("message") : "GENERAL ERROR";
                String status = failObject != null ? failObject.getString("code") : "GENERAL ERROR";
                throw new InternalServiceException(message, mapperCode(status), HttpStatus.OK);
            }
            throw new InternalServiceException(ex.getChannelMessage(), StatusRepositoryService.FUND_TRANSFER_IS_NOT_SUCCESS, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("General error in accountToAccount for uuid ({}) ===> {}", uuid, ex.getMessage());
            log.error("General error stack in accountToAccount for uuid ({}) is", uuid, ex);
            fundTransferAccountToAccountRequestEntity.setChannelResponse(ex.getMessage());
            fundTransferAccountToAccountRequestEntity.setResult(StatusRepositoryService.GENERAL_ERROR);
            throw new InternalServiceException(ex.getMessage(), StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
        } finally {
            fundTransferAccountToAccountRequestEntity.setChannelResponseTime(new Date());
        }
    }

    private int mapperCode(String code) {
        return switch (code) {
            default -> StatusRepositoryService.GENERAL_ERROR;
        };
    }

    @Override
    public String inquiryAccountToAccount(String uuid, Long amount) throws InternalServiceException {
        String token = getTokenByScope(SadadChannelInterface.SCOPE_FUND_TRANSFER);
        try {
            log.info("start call sadad fundTransfer inquiry for uuid ({})", uuid);
            String response = sadadChannel.fundTransferInquiry(token, uuid, String.valueOf(amount));

            JSONObject jsonObject = new JSONObject(response);
            JSONObject jsonObjectResultSet = new JSONObject(jsonObject.optString("resultSet"));
            JSONObject innerResponse = new JSONObject(jsonObjectResultSet.optString("innerResponse"));
            String status = innerResponse.optString("status");

            if (!"SUCCEEDED".equalsIgnoreCase(status)) {
                log.info("sadad fundTransfer inquiry status is not success for uuid ({}): {}", uuid, status);
                throw new InternalServiceException(status, StatusRepositoryService.FUND_TRANSFER_IS_NOT_SUCCESS, HttpStatus.OK);
            }
            return response;
        } catch (ChannelException exception) {
            log.error("error in inquiry accountToAccount for uid ({}): {}", uuid, exception.getMessage(), exception);
            validateTokenInException(exception, SadadChannelInterface.SCOPE_FUND_TRANSFER);
            if (exception.getHttpStatusCode() == HttpStatus.NOT_FOUND.value() ||
                    exception.getHttpStatusCode() == HttpStatus.REQUEST_TIMEOUT.value()) {
                log.error("request with uuid ({}) is timeout or not found and save suspend", uuid);
                //check time transaction more than 60 minutes change to fail
                throw new InternalServiceException(exception.getMessage(), StatusRepositoryService.FUND_TRANSFER_IS_SUSPEND,
                        HttpStatus.valueOf(exception.getHttpStatusCode()));
            }

            if (isValidJson(exception.getCompleteResponse())) {
                JSONObject jsonObjectFail = new JSONObject(exception.getCompleteResponse());
                JSONArray jsonArray = jsonObjectFail.optJSONArray("notifications");
                JSONObject failObject = jsonArray != null ? jsonArray.optJSONObject(0) : null;
                String message = failObject != null ? failObject.getString("message") : "GENERAL ERROR";
                String status = failObject != null ? failObject.getString("code") : "GENERAL ERROR";
                throw new InternalServiceException(message, mapperCode(status), HttpStatus.OK);
            }
            throw new InternalServiceException(exception.getMessage(), exception.getResultCode(), HttpStatus.OK);
        } catch (InternalServiceException ex) {

            throw ex;
        } catch (Exception ex) {
            log.error("general error in inquiry accountToAccount for uuid ({}) ===> {}", uuid, ex.getMessage());
            log.error("general error stack in inquiry accountToAccount for uuid ({}) is", uuid, ex);
            throw new InternalServiceException(ex.getMessage(), StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
        }
    }

    @Override
    public String statement(String amount, String srcAccountNumber, Long timeStampTransactionDate, String traceNumber, int timeFrame, int page, int length) throws InternalServiceException {
        String token = getTokenByScope(SadadChannelInterface.SCOPE_STATEMENT);
        Date statementFromDate = new Date(timeStampTransactionDate);
        Date statementToDateBefore = DateUtils.getNPreviousMinutes(statementFromDate, timeFrame);
        Date statementToDateAfter = DateUtils.getNNextMinutes(statementFromDate, timeFrame);
        String fromDate = convertDateTimeToInstant(statementToDateBefore);
        String toDate = convertDateTimeToInstant(statementToDateAfter);

        try {
            return sadadChannel.statement(token, amount, srcAccountNumber, traceNumber, fromDate, toDate, "CREDIT", page, length);
        } catch (ChannelException e) {
            log.error("error in get statement is srcAccountNumber ({})", srcAccountNumber);
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long getBalance(String accountNumber) throws InternalServiceException {
        String token = getTokenByScope(SadadChannelInterface.SCOPE_GET_BALANCE);
        try {
            log.info("start call sadad balance for account ({})", accountNumber);
            String response = sadadChannel.getBalance(token, accountNumber);
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.optLong("usableBalance");
        } catch (ChannelException exception) {
            log.error("error in sadad balance for account ({}): {}", accountNumber, exception.getMessage(), exception);
            throw new InternalServiceException(exception.getMessage(), exception.getResultCode(), HttpStatus.OK);
        } catch (Exception ex) {
            log.error("general error in sadad balance for accountNumber ({}) ===> {}", accountNumber, ex.getMessage());
            log.error("general error stack in sadad balance for accountNumber ({}) is", accountNumber, ex);
            throw new InternalServiceException(ex.getMessage(), StatusRepositoryService.GENERAL_ERROR, HttpStatus.OK);
        }
    }

    private void processAccountToAccountResponse(FundTransferAccountToAccountRequestEntity fundTransferAccountToAccountRequestEntity, String uuid,
                                                 String response) throws InternalServiceException {
        fundTransferAccountToAccountRequestEntity.setChannelResponse(response);
        log.info("success get Sadad fundTransfer response for uuid ({}): {}", uuid, response);

        JSONObject jsonObject = new JSONObject(response);
        JSONObject resultSetObject = jsonObject.optJSONObject("resultSet");
        JSONObject innerResponseObject = resultSetObject.optJSONObject("innerResponse");

        String status = innerResponseObject.optString("status");

        if (!"SUCCEEDED".equalsIgnoreCase(status)) {
            log.info("Sadad fundTransfer status is not success for uuid ({}): {}", uuid, status);
            fundTransferAccountToAccountRequestEntity.setChannelResult(status);
            throw new InternalServiceException("Inquiry fund transfer is not successful",
                    StatusRepositoryService.FUND_TRANSFER_IS_NOT_SUCCESS, HttpStatus.OK);
        }

        fundTransferAccountToAccountRequestEntity.setResult(StatusRepositoryService.SUCCESSFUL);
        fundTransferAccountToAccountRequestEntity.setRefNumber(innerResponseObject.optString("traceNo"));
    }

    private static String convertDateTimeToInstant(Date date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Instant fromTime = date.toInstant();
        ZonedDateTime fromTimeZdt = fromTime.atZone(ZoneOffset.UTC);
        return fromTimeZdt.format(formatter);
    }

}
