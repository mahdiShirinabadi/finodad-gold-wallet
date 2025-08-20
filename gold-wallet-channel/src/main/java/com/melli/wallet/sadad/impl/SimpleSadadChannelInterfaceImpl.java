package com.melli.wallet.sadad.impl;

import com.melli.wallet.ChannelException;
import com.melli.wallet.sadad.SadadChannelInterface;
import com.melli.wallet.sadad.config.SadadProperties;
import com.melli.wallet.util.Utility;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.melli.wallet.WebCallUtils.sendRequest;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Profile({"staging111"})
@Log4j2
@RequiredArgsConstructor
public class SimpleSadadChannelInterfaceImpl implements SadadChannelInterface {

    private final SadadProperties sadadProperties;


    @Override
    public String shahkar(String token, String nationalCode, String mobile) throws ChannelException, URISyntaxException {
        log.info("start shahkarUrl({}), nationalCode ({}), timeout({})", sadadProperties.getShahkarUrl(), nationalCode, sadadProperties.getTimeout());

        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, "Bearer " + token);
        headers.put("Content-Type", "application/json");
        Map<String, String> params = new HashMap<>();

        JSONObject json = new JSONObject();
        json.put("contactValue", mobile);
        json.put("serviceType", "2");
        json.put("identificationType", "0");
        json.put("identificationNo", nationalCode);
        json.put("callerUnitCode", "111111505");
        json.put("online", "TRUE");
        json.put("userIdentificationNo", nationalCode);
        json.put("acceptDays", "20");
        json.put("branchCode", "0");
        StringEntity body = new StringEntity(json.toString(), StandardCharsets.UTF_8);

        URIBuilder builder = new URIBuilder(sadadProperties.getShahkarUrl());

        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt(sadadProperties.getTimeout()), headers, params, body, new int[] {HttpStatus.OK.value()});
        log.info("({}), Finished for, with response({})", Utility.getCallerMethodName(), response);
        return response;
    }

    @Override
    public String getToken(String clientId, String clientSecret, String scope) throws ChannelException, URISyntaxException {
        String valueToEncode = clientId + ":" + clientSecret;
        String authentication = "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", "application/x-www-form-urlencoded");
        headers.put("Authorization", authentication);

        Map<String, String> params = new HashMap<>();
        params.put("scope", scope);
        params.put("grant_type", "client_credentials");
        URIBuilder builder = new URIBuilder(sadadProperties.getIdentityUrl());
        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt(sadadProperties.getTimeout()), headers, params, null, new int[] {HttpStatus.OK.value()});
        log.info("({}), Finished login  for scope ({}) with response({})", Utility.getCallerMethodName(), scope, response);
        return response;
    }

    @Override
    public String sendSms(String token, String message, String mobile, int priority, int ttl) throws ChannelException, URISyntaxException {
       /* log.info("({}) Start, mobile ({}),timeout({})", Utility.getCallerMethodName(), mobile, timeout);

        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, "Bearer " + token);
        headers.put("Content-Type", "application/json");
        Map<String, String> params = new HashMap<>();

        JSONObject json = new JSONObject();
        json.put("messageText", message);
        json.put("mobileNumber",mobile);
        json.put("priorityNumber", 5);
        json.put("ttl", 3);
        StringEntity body = new StringEntity(json.toString(), StandardCharsets.UTF_8);

        URIBuilder builder = new URIBuilder(smsUrl);

        log.info("send message with jsonRequest ({})", json.toString());

        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt(timeout), headers, params, body, new int[] {HttpStatus.OK.value()});
        log.info("({}), Finished for, with response({})", Utility.getCallerMethodName(), response);
        return response;*/
        return "ok";
    }

    @Override
    public String fundTransfer(String token, String traceId, String requestId, String fromAccount, String toAccount, String amount, Date date, String creditPayId) throws ChannelException {
        if ("1111".equals(amount)) {
            throw new ChannelException(
                    "Exception in " + Utility.getCallerClassAndMethodName(),
                    10011,
                    "",
                    10011,
                    "");
        }
        return """
                {
                    "resultSet": {
                        "innerResponse": {
                            "items": null,
                            "scheduledTransfer": null,
                            "id": "89dea36a-f942-4868-bd47-8ab0e0d45a8d",
                            "alias": "money-transfer",
                            "accountId": "0217118343009",
                            "instructedAmount": 10000,
                            "instructedCurrency": "IRR",
                            "counterpartyName": "مهدى شيرين آبادى فراهانى",
                            "counterpartyAccount": "0232827458008",
                            "paymentReference": "171994442312374",
                            "paymentDescription": "171994442312374",
                            "causeDescription": null,
                            "method": "INTRA_BANK",
                            "mode": "SINGLE",
                            "totalAmount": 0,
                            "totalCount": 0,
                            "creatorName": null,
                            "status": "SUCCEEDED",
                            "initiationDateTime": "1715165452132",
                            "updateDateTime": "1715165452324",
                            "onDate": "1715075803000",
                            "confirmationDateTime": "1715165452324",
                            "traceNo": "0302191420522358",
                            "extraTraceNo": null,
                            "responseCode": "",
                            "responseMessage": "تراکنش با موفقيت انجام شد",
                            "archived": false,
                            "creatorSSN": "111111505",
                            "creditPayId": null,
                            "debitPayId": null
                        }
                    },
                    "metaData": {
                        "notifications": [
                            {
                                "code": "200",
                                "type": "INFORMATION",
                                "message": "OK"
                            }
                        ]
                    }
                }
                """;
    }

    @Override
    public String fundTransferInquiry(String token, String uuid, String amount) throws ChannelException {
        if (amount.equalsIgnoreCase("13651")) {
            throw new ChannelException("IOException in " + "fundTransfer inquiry", TIME_OUT, "IOException in ", HttpStatus.NOT_FOUND.value(), "");
        }
        if (amount.equalsIgnoreCase("50000000")) {
            return """
                    {
                        "resultSet": {
                            "innerResponse": {
                                "items": null,
                                "scheduledTransfer": null,
                                "id": "89dea36a-f942-4868-bd47-8ab0e0d45a8d",
                                "alias": "money-transfer",
                                "accountId": "0217118343009",
                                "instructedAmount": 50000000,
                                "instructedCurrency": "IRR",
                                "counterpartyName": "مهدى کرامتی",
                                "counterpartyAccount": "0222712529001",
                                "paymentReference": "171994442312374",
                                "paymentDescription": "171994442312374",
                                "causeDescription": null,
                                "method": "INTRA_BANK",
                                "mode": "SINGLE",
                                "totalAmount": 0,
                                "totalCount": 0,
                                "creatorName": null,
                                "status": "SUCCEEDED",
                                "initiationDateTime": "1715165452132",
                                "updateDateTime": "1715165452324",
                                "onDate": "1715075803000",
                                "confirmationDateTime": "1715165452324",
                                "traceNo": "0302191420522358",
                                "extraTraceNo": null,
                                "responseCode": "",
                                "responseMessage": "تراکنش با موفقيت انجام شد",
                                "archived": false,
                                "creatorSSN": "111111505",
                                "creditPayId": "0924784369",
                                "debitPayId": null
                            }
                        },
                        "metaData": {
                            "notifications": [
                                {
                                    "code": "200",
                                    "type": "INFORMATION",
                                    "message": "OK"
                                }
                            ]
                        }
                    }
                    """;
        }
        if (amount.equalsIgnoreCase("10000000")) {
            return """
                    {
                        "resultSet": {
                            "innerResponse": {
                                "items": null,
                                "scheduledTransfer": null,
                                "id": "89dea36a-f942-4868-bd47-8ab0e0d45a8d",
                                "alias": "money-transfer",
                                "accountId": "0217118343009",
                                "instructedAmount": 10000000,
                                "instructedCurrency": "IRR",
                                "counterpartyName": "مهدى کرامتی",
                                "counterpartyAccount": "0222712529001",
                                "paymentReference": "171994442312374",
                                "paymentDescription": "171994442312374",
                                "causeDescription": null,
                                "method": "INTRA_BANK",
                                "mode": "SINGLE",
                                "totalAmount": 0,
                                "totalCount": 0,
                                "creatorName": null,
                                "status": "SUCCEEDED",
                                "initiationDateTime": "1715165452132",
                                "updateDateTime": "1715165452324",
                                "onDate": "1715075803000",
                                "confirmationDateTime": "1715165452324",
                                "traceNo": "0302191420522358",
                                "extraTraceNo": null,
                                "responseCode": "",
                                "responseMessage": "تراکنش با موفقيت انجام شد",
                                "archived": false,
                                "creatorSSN": "111111505",
                                "creditPayId": "0021218978",
                                "debitPayId": null
                            }
                        },
                        "metaData": {
                            "notifications": [
                                {
                                    "code": "200",
                                    "type": "INFORMATION",
                                    "message": "OK"
                                }
                            ]
                        }
                    }
                    """;
        }
        if (amount.equalsIgnoreCase("5000000")) {
            return """
                    {
                        "resultSet": {
                            "innerResponse": {
                                "items": null,
                                "scheduledTransfer": null,
                                "id": "89dea36a-f942-4868-bd47-8ab0e0d45a8d",
                                "alias": "money-transfer",
                                "accountId": "0217118343009",
                                "instructedAmount": 5000000,
                                "instructedCurrency": "IRR",
                                "counterpartyName": "مهدى کرامتی",
                                "counterpartyAccount": "0222712529001",
                                "paymentReference": "171994442312374",
                                "paymentDescription": "171994442312374",
                                "causeDescription": null,
                                "method": "INTRA_BANK",
                                "mode": "SINGLE",
                                "totalAmount": 0,
                                "totalCount": 0,
                                "creatorName": null,
                                "status": "SUCCEEDED",
                                "initiationDateTime": "1715165452132",
                                "updateDateTime": "1715165452324",
                                "onDate": "1715075803000",
                                "confirmationDateTime": "1715165452324",
                                "traceNo": "0302191420522358",
                                "extraTraceNo": null,
                                "responseCode": "",
                                "responseMessage": "تراکنش با موفقيت انجام شد",
                                "archived": false,
                                "creatorSSN": "111111505",
                                "creditPayId": "0077847660",
                                "debitPayId": null
                            }
                        },
                        "metaData": {
                            "notifications": [
                                {
                                    "code": "200",
                                    "type": "INFORMATION",
                                    "message": "OK"
                                }
                            ]
                        }
                    }
                    """;
        }
        if (amount.equalsIgnoreCase("136565")) {
            return """
                    {
                        "resultSet": {
                            "innerResponse": {
                                "items": null,
                                "scheduledTransfer": null,
                                "id": "89dea36a-f942-4868-bd47-8ab0e0d45a8d",
                                "alias": "money-transfer",
                                "accountId": "0217118343009",
                                "instructedAmount": 136565,
                                "instructedCurrency": "IRR",
                                "counterpartyName": "مهدى شيرين آبادى فراهانى",
                                "counterpartyAccount": "0222712529001",
                                "paymentReference": "171994442312374",
                                "paymentDescription": "171994442312374",
                                "causeDescription": null,
                                "method": "INTRA_BANK",
                                "mode": "SINGLE",
                                "totalAmount": 0,
                                "totalCount": 0,
                                "creatorName": null,
                                "status": "SUCCEEDED",
                                "initiationDateTime": "1715165452132",
                                "updateDateTime": "1715165452324",
                                "onDate": "1715075803000",
                                "confirmationDateTime": "1715165452324",
                                "traceNo": "0302191420522358",
                                "extraTraceNo": null,
                                "responseCode": "",
                                "responseMessage": "تراکنش با موفقيت انجام شد",
                                "archived": false,
                                "creatorSSN": "111111505",
                                "creditPayId": "0061397342",
                                "debitPayId": null
                            }
                        },
                        "metaData": {
                            "notifications": [
                                {
                                    "code": "200",
                                    "type": "INFORMATION",
                                    "message": "OK"
                                }
                            ]
                        }
                    }
                    """;
        }
        return """
                {
                    "resultSet": {
                        "innerResponse": {
                            "items": null,
                            "scheduledTransfer": null,
                            "id": "89dea36a-f942-4868-bd47-8ab0e0d45a8d",
                            "alias": "money-transfer",
                            "accountId": "0217118343009",
                            "instructedAmount": 13650,
                            "instructedCurrency": "IRR",
                            "counterpartyName": "مهدى شيرين آبادى فراهانى",
                            "counterpartyAccount": "123456789",
                            "paymentReference": "171994442312374",
                            "paymentDescription": "171994442312374",
                            "causeDescription": null,
                            "method": "INTRA_BANK",
                            "mode": "SINGLE",
                            "totalAmount": 0,
                            "totalCount": 0,
                            "creatorName": null,
                            "status": "SUCCEEDED",
                            "initiationDateTime": "1715165452132",
                            "updateDateTime": "1715165452324",
                            "onDate": "1715075803000",
                            "confirmationDateTime": "1715165452324",
                            "traceNo": "0302191420522358",
                            "extraTraceNo": null,
                            "responseCode": "",
                            "responseMessage": "تراکنش با موفقيت انجام شد",
                            "archived": false,
                            "creatorSSN": "111111505",
                            "creditPayId": "0077847660",
                            "debitPayId": null
                        }
                    },
                    "metaData": {
                        "notifications": [
                            {
                                "code": "200",
                                "type": "INFORMATION",
                                "message": "OK"
                            }
                        ]
                    }
                }
                """;
    }

    @Override
    public String statement(String token, String amount, String srcAccountNumber, String traceNumber, String fromDate, String toDate, String creditDebit, int page, int length) throws ChannelException, URISyntaxException {
        log.info("start statementUrl({}), srcAccountNumber ({}),timeout({}), traceNumber ({})", sadadProperties.getStatementUrl(), srcAccountNumber, sadadProperties.getTimeout(), traceNumber);

        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, "Bearer " + token);
        headers.put("Content-Type", "application/json");
        Map<String, String> params = new HashMap<>();

        JSONObject jsonObjectDate = new JSONObject();
        jsonObjectDate.put("fromDateTime", fromDate);
        jsonObjectDate.put("toDateTime", toDate);
        JSONObject jsonObjectAmount = new JSONObject();
        jsonObjectAmount.put("fromAmount", Long.parseLong(amount));
        jsonObjectAmount.put("toAmount", Long.parseLong(amount));

        JSONObject json = new JSONObject();
        json.put("offsetNumber", page);
        json.put("offsetLength", length);


        json.put("accountNumber", srcAccountNumber);
        json.put("dateTimeRange", jsonObjectDate);
        json.put("amountRange", jsonObjectAmount);
        json.put("creditDebit", creditDebit);
        json.put("traceNumber", traceNumber);
        StringEntity body = new StringEntity(json.toString(), StandardCharsets.UTF_8);

        URIBuilder builder = new URIBuilder(sadadProperties.getStatementUrl());

        log.info("start get statement with jsonRequest ({})", json.toString());

        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt(sadadProperties.getTimeout()), headers, params, body, new int[]{HttpStatus.OK.value()});
        log.info("({}), Finished for, with response({})", Utility.getCallerMethodName(), response);
//        String response1="{\"error\":null,\"response\":{\"accountStatementResponse\":[{\"transactionId\":103772186439,\"transactionDateTime\":\"2025-04-22T19:44:33Z\",\"transactionTraceCode\":\"BCG15018\",\"creditDebit\":\"CREDIT\",\"branchCode\":251,\"branchName\":\"تمرکز اسناد\",\"operatorCode\":\"BCGUSER\",\"additionalData1\":\"0110697402009\",\"additionalData2\":\"0080560921\",\"transactionCode\":\"000\",\"transactionDescription\":\"انتقالي\",\"realDateTime\":\"2025-04-22T19:44:33Z\",\"amount\":10000000,\"balance\":153426944456,\"channelType\":\"TELLER\",\"categoryId\":null,\"comment\":null,\"hyperLinkType\":0,\"rowNumber\":1,\"origKey\":null,\"channelDateTime\":null},{\"transactionId\":103772186435,\"transactionDateTime\":\"2025-04-22T19:25:49Z\",\"transactionTraceCode\":\"BCG14317\",\"creditDebit\":\"CREDIT\",\"branchCode\":251,\"branchName\":\"تمرکز اسناد\",\"operatorCode\":\"BCGUSER\",\"additionalData1\":\"0102960198001\",\"additionalData2\":\"3621007148\",\"transactionCode\":\"000\",\"transactionDescription\":\"انتقالي\",\"realDateTime\":\"2025-04-22T19:25:49Z\",\"amount\":10000000,\"balance\":153352944456,\"channelType\":\"TELLER\",\"categoryId\":null,\"comment\":null,\"hyperLinkType\":0,\"rowNumber\":2,\"origKey\":null,\"channelDateTime\":null},{\"transactionId\":103772186414,\"transactionDateTime\":\"2025-04-22T18:29:54Z\",\"transactionTraceCode\":\"BCG17858\",\"creditDebit\":\"CREDIT\",\"branchCode\":251,\"branchName\":\"تمرکز اسناد\",\"operatorCode\":\"BCGUSER\",\"additionalData1\":\"0100376066003\",\"additionalData2\":\"0064901777\",\"transactionCode\":\"000\",\"transactionDescription\":\"انتقالي\",\"realDateTime\":\"2025-04-22T18:29:54Z\",\"amount\":10000000,\"balance\":147397447296,\"channelType\":\"TELLER\",\"categoryId\":null,\"comment\":null,\"hyperLinkType\":0,\"rowNumber\":3,\"origKey\":null,\"channelDateTime\":null},{\"transactionId\":103772186404,\"transactionDateTime\":\"2025-04-22T18:02:27Z\",\"transactionTraceCode\":\"BCG18395\",\"creditDebit\":\"CREDIT\",\"branchCode\":251,\"branchName\":\"تمرکز اسناد\",\"operatorCode\":\"BCGUSER\",\"additionalData1\":\"0117287969000\",\"additionalData2\":\"2691182304\",\"transactionCode\":\"000\",\"transactionDescription\":\"انتقالي\",\"realDateTime\":\"2025-04-22T18:02:27Z\",\"amount\":10000000,\"balance\":145914127296,\"channelType\":\"TELLER\",\"categoryId\":null,\"comment\":null,\"hyperLinkType\":0,\"rowNumber\":4,\"origKey\":null,\"channelDateTime\":null},{\"transactionId\":103772186402,\"transactionDateTime\":\"2025-04-22T17:56:59Z\",\"transactionTraceCode\":\"BCG14541\",\"creditDebit\":\"CREDIT\",\"branchCode\":251,\"branchName\":\"تمرکز اسناد\",\"operatorCode\":\"BCGUSER\",\"additionalData1\":\"0110320689003\",\"additionalData2\":\"4431926151\",\"transactionCode\":\"000\",\"transactionDescription\":\"انتقالي\",\"realDateTime\":\"2025-04-22T17:56:59Z\",\"amount\":10000000,\"balance\":145754127296,\"channelType\":\"TELLER\",\"categoryId\":null,\"comment\":null,\"hyperLinkType\":0,\"rowNumber\":5,\"origKey\":null,\"channelDateTime\":null},{\"transactionId\":103772186397,\"transactionDateTime\":\"2025-04-22T17:50:31Z\",\"transactionTraceCode\":\"BCG19195\",\"creditDebit\":\"CREDIT\",\"branchCode\":251,\"branchName\":\"تمرکز اسناد\",\"operatorCode\":\"BCGUSER\",\"additionalData1\":\"0306050199007\",\"additionalData2\":\"5629937502\",\"transactionCode\":\"000\",\"transactionDescription\":\"انتقالي\",\"realDateTime\":\"2025-04-22T17:50:31Z\",\"amount\":10000000,\"balance\":144620127296,\"channelType\":\"TELLER\",\"categoryId\":null,\"comment\":null,\"hyperLinkType\":0,\"rowNumber\":6,\"origKey\":null,\"channelDateTime\":null},{\"transactionId\":103772186391,\"transactionDateTime\":\"2025-04-22T17:39:54Z\",\"transactionTraceCode\":\"BCG11151\",\"creditDebit\":\"CREDIT\",\"branchCode\":251,\"branchName\":\"تمرکز اسناد\",\"operatorCode\":\"BCGUSER\",\"additionalData1\":\"0108115802001\",\"additionalData2\":\"5459966035\",\"transactionCode\":\"000\",\"transactionDescription\":\"انتقالي\",\"realDateTime\":\"2025-04-22T17:39:54Z\",\"amount\":10000000,\"balance\":141092127296,\"channelType\":\"TELLER\",\"categoryId\":null,\"comment\":null,\"hyperLinkType\":0,\"rowNumber\":7,\"origKey\":null,\"channelDateTime\":null},{\"transactionId\":103772186386,\"transactionDateTime\":\"2025-04-22T17:29:18Z\",\"transactionTraceCode\":\"BCG12811\",\"creditDebit\":\"CREDIT\",\"branchCode\":251,\"branchName\":\"تمرکز اسناد\",\"operatorCode\":\"BCGUSER\",\"additionalData1\":\"0364140225007\",\"additionalData2\":\"4189574430\",\"transactionCode\":\"000\",\"transactionDescription\":\"انتقالي\",\"realDateTime\":\"2025-04-22T17:29:18Z\",\"amount\":10000000,\"balance\":140132127296,\"channelType\":\"TELLER\",\"categoryId\":null,\"comment\":null,\"hyperLinkType\":0,\"rowNumber\":8,\"origKey\":null,\"channelDateTime\":null},{\"transactionId\":103772186371,\"transactionDateTime\":\"2025-04-22T16:56:05Z\",\"transactionTraceCode\":\"BCG19942\",\"creditDebit\":\"CREDIT\",\"branchCode\":251,\"branchName\":\"تمرکز اسناد\",\"operatorCode\":\"BCGUSER\",\"additionalData1\":\"0301082116003\",\"additionalData2\":\"1750482363\",\"transactionCode\":\"000\",\"transactionDescription\":\"انتقالي\",\"realDateTime\":\"2025-04-22T16:56:05Z\",\"amount\":10000000,\"balance\":137808487296,\"channelType\":\"TELLER\",\"categoryId\":null,\"comment\":null,\"hyperLinkType\":0,\"rowNumber\":9,\"origKey\":null,\"channelDateTime\":null},{\"transactionId\":103772186369,\"transactionDateTime\":\"2025-04-22T16:54:30Z\",\"transactionTraceCode\":\"BCG19013\",\"creditDebit\":\"CREDIT\",\"branchCode\":251,\"branchName\":\"تمرکز اسناد\",\"operatorCode\":\"BCGUSER\",\"additionalData1\":\"0220786251000\",\"additionalData2\":\"0534998781\",\"transactionCode\":\"000\",\"transactionDescription\":\"انتقالي\",\"realDateTime\":\"2025-04-22T16:54:30Z\",\"amount\":10000000,\"balance\":137498487296,\"channelType\":\"TELLER\",\"categoryId\":null,\"comment\":null,\"hyperLinkType\":0,\"rowNumber\":10,\"origKey\":null,\"channelDateTime\":null},{\"transactionId\":103772184617,\"transactionDateTime\":\"2025-04-22T04:54:57Z\",\"transactionTraceCode\":\"bPOE0048\",\"creditDebit\":\"CREDIT\",\"branchCode\":271,\"branchName\":\"اسکان\",\"operatorCode\":\"A93957\",\"additionalData1\":\"0204916390001\",\"additionalData2\":\"0946619247\",\"transactionCode\":\"000\",\"transactionDescription\":\"انتقالي\",\"realDateTime\":\"2025-04-22T04:54:57Z\",\"amount\":1000000,\"balance\":328324943372,\"channelType\":\"TELLER\",\"categoryId\":null,\"comment\":null,\"hyperLinkType\":0,\"rowNumber\":1,\"origKey\":null,\"channelDateTime\":null}],\"outputRecordCount\":333}}";
//        String response="{\"error\":null,\"response\":{\"accountStatementResponse\":[{\"transactionId\":103772184617,\"transactionDateTime\":\"2025-04-22T04:54:57Z\",\"transactionTraceCode\":\"bPOE0048\",\"creditDebit\":\"CREDIT\",\"branchCode\":271,\"branchName\":\"اسکان\",\"operatorCode\":\"A93957\",\"additionalData1\":\"0204916390001\",\"additionalData2\":\"0946619247\",\"transactionCode\":\"000\",\"transactionDescription\":\"انتقالي\",\"realDateTime\":\"2025-04-22T04:54:57Z\",\"amount\":1000000,\"balance\":328324943372,\"channelType\":\"TELLER\",\"categoryId\":null,\"comment\":null,\"hyperLinkType\":0,\"rowNumber\":1,\"origKey\":null,\"channelDateTime\":null}],\"outputRecordCount\":333}}";
        return response;
    }

    @Override
    public String getBalance(String token, String accountNumber) throws ChannelException, URISyntaxException {
        return "{\n" +
                "    \"id\": \"0368163985003\",\n" +
                "    \"type\": \"040\",\n" +
                "    \"subType\": \"001\",\n" +
                "    \"state\": \"02\",\n" +
                "    \"cif\": \"0733459556\",\n" +
                "    \"branchCode\": 1033,\n" +
                "    \"openDate\": 1726605000000,\n" +
                "    \"freezDate\": null,\n" +
                "    \"sicCode\": \"\",\n" +
                "    \"backupNumber\": \"\",\n" +
                "    \"freezAmount\": 0,\n" +
                "    \"freezBranchCode\": 0,\n" +
                "    \"rate\": 0,\n" +
                "    \"availableBalance\": 129900000,\n" +
                "    \"usableBalance\": 129900000,\n" +
                "    \"currentBalance\": 129900000,\n" +
                "    \"profitAccountNumber\": \"\",\n" +
                "    \"lastTransactionDate\": null,\n" +
                "    \"iban\": \"IR220170000000368163985003\",\n" +
                "    \"firstName\": \"فناوران نوآور\",\n" +
                "    \"lastName\": \"سداد\",\n" +
                "    \"customerType\": \"حقوقي\",\n" +
                "    \"closeDate\": null,\n" +
                "    \"prefer\": null,\n" +
                "    \"ssn\": null,\n" +
                "    \"branchName\": \"برج آرميتا\",\n" +
                "    \"balanceType\": \"CURRENT\",\n" +
                "    \"priority\": 0,\n" +
                "    \"currency\": \"IRR\",\n" +
                "    \"showBalance\": false,\n" +
                "    \"showName\": false,\n" +
                "    \"specs\": \"NORMAL_ACCOUNT\",\n" +
                "    \"accountTitle\": null,\n" +
                "    \"default\": false\n" +
                "}";
    }

    @Override
    public String getAccount(String token, String nationalCode, String customerType) throws ChannelException, URISyntaxException {
        log.info("start getAccount({}), url ({}),timeout({})", sadadProperties.getAccountUrl(), nationalCode, sadadProperties.getTimeout());

        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, "Bearer " + token);
        headers.put("Content-Type", "application/json");
        Map<String, String> params = new HashMap<>();

        JSONObject json = new JSONObject();
        json.put("customerType", customerType);
        json.put("identifier", nationalCode);
        StringEntity body = new StringEntity(json.toString(), StandardCharsets.UTF_8);

        URIBuilder builder = new URIBuilder(sadadProperties.getAccountUrl());

        log.info("send message with jsonRequest ({})", json.toString());

        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt(sadadProperties.getTimeout()), headers, params, body, new int[] {HttpStatus.OK.value()});
        log.info("({}), Finished for, with response({})", Utility.getCallerMethodName(), response);
        return response;

    }


}
