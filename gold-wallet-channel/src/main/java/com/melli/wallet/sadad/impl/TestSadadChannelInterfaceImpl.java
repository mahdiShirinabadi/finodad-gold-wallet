package com.melli.wallet.sadad.impl;

import com.melli.wallet.ChannelException;
import com.melli.wallet.sadad.SadadChannelInterface;
import com.melli.wallet.util.Utility;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.melli.wallet.WebCallUtils.sendRequest;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Profile({"test","dev"})
@Log4j2
public class TestSadadChannelInterfaceImpl implements SadadChannelInterface {

    @Value("${sadad.timeout}")
    private String timeout;
    @Value("${sadad.identity.url}")
    private String identityUrl;

    @Value("${sadad.sms.url}")
    private String smsUrl;

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
    public String shahkar(String token, String nationalCode, String mobile) throws ChannelException, URISyntaxException {
        if (mobile.equalsIgnoreCase("09124162337")) {
            return """
                    {"error":null,"response":{"response":200,"result":"OK","comment":"درخواست با موفقیت ثبت شد","inquiryDate":"2024-07-23T08:35:31.331","online":true}}
                    """;
        }
        return """
                {"error":null,"response":{"response":600,"result":"NotIdentifiedException","comment":"اطلاعات ارسالی با اطلاعات موجود در سامانه در یکی یا بیشتر از فیلدهای زیر مشابهت ندارد.(نام،نام خانوادگی،نام پدر،تاریخ تولد،جنسیت برای حقیقی ) (شماره ثبت شرکت،نام شرکت،تاریخ ثبت،فعال یا غیر فعال بودن برای حقوقی ایرانی)","inquiryDate":null,"online":true}}
                """;
    }

    @Override
    public String getToken(String clientId, String clientSecret, String scope) throws ChannelException, URISyntaxException {
        return """
                {"last_logins":"[]","token_type":"bearer","expires_in":86400,"access_token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJncmFudCI6IkNMSUVOVCIsImlzcyI6Imh0dHA6Ly9hcGkuYm1pLmlyL3NlY3VyaXR5IiwiYXVkIjoiZmlub2RhZC1zdWt1ay1jbGllbnQiLCJleHAiOjE3MzM2MjkxMTc2NzQsIm5iZiI6MTczMzU0MjcxNzY3NCwicm9sZSI6IiIsInNlcmlhbCI6ImYzNTI4M2IwLTM1Y2ItM2E2Ni1hODc3LTdjMTFiMjczOTMyZSIsInNzbiI6IjExMTExMTUzOSIsImNsaWVudF9pZCI6IjExMTExMTUzOSIsInNjb3BlcyI6WyJzdmMtbWdtdC1hY2Mtc3RtdC1vbCJdfQ==.zJtHg0M8_33WgmRwuW99VCX9Qc2jjNyuWI2cBqba15Y"}
                """;
    }

    @Override
    public String statement(String token, String amount, String srcAccountNumber, String traceNumber, String fromDate, String toDate, String creditDebit, int page, int length) throws ChannelException, URISyntaxException {
        if (amount.equalsIgnoreCase("13651")) {
            JSONObject statementObject = new JSONObject();
            JSONObject responseObject = new JSONObject();
            responseObject.put("outputRecordCount", 1);
            JSONArray accountStatementArray = new JSONArray();
            JSONObject accountStatementObject = new JSONObject();
            accountStatementObject.put("additionalData1", srcAccountNumber);
            accountStatementObject.put("amount", amount);
            accountStatementObject.put("origKey", "" + new Date().getTime());
            accountStatementObject.put("transactionDateTime", new Date());
            accountStatementArray.put(accountStatementObject);
            responseObject.put("accountStatementResponse", accountStatementArray);
            statementObject.put("response", responseObject);
            return statementObject.toString();
        }
        if (amount.equalsIgnoreCase("13650")) {
            return """
                    {
                        "error": null,
                        "response": {
                            "accountStatementResponse": [
                                {
                                    "transactionId": -21923326335,
                                    "transactionDateTime": "2024-11-09T04:04:24Z",
                                    "transactionTraceCode": "BCG12076",
                                    "creditDebit": "CREDIT",
                                    "branchCode": 251,
                                    "branchName": null,
                                    "operatorCode": "BCGUSER",
                                    "additionalData1": "00015454545",
                                    "additionalData2": "4890206809",
                                    "transactionCode": "008",
                                    "transactionDescription": "انتقالي",
                                    "realDateTime": "2024-11-09T04:04:24Z",
                                    "amount": 13650,
                                    "balance": 1208584741214,
                                    "channelType": null,
                                    "categoryId": null,
                                    "comment": null,
                                    "hyperLinkType": 0,
                                    "rowNumber": null,
                                    "origKey": "14030819073424BCG12076",
                                    "channelDateTime": "2024-11-09T04:04:24Z"
                                }
                            ],
                            "outputRecordCount": 1
                        }
                    }
                    """;
        } else if (amount.equalsIgnoreCase("6000000")) {
            return """
                    {
                        "error": null,
                        "response": {
                            "accountStatementResponse": [
                                {
                                    "transactionId": -21923326335,
                                    "transactionDateTime": "2024-11-09T04:04:24Z",
                                    "transactionTraceCode": "BCG12076",
                                    "creditDebit": "CREDIT",
                                    "branchCode": 251,
                                    "branchName": null,
                                    "operatorCode": "BCGUSER",
                                    "additionalData1": "0217118343009",
                                    "additionalData2": "4890206809",
                                    "transactionCode": "008",
                                    "transactionDescription": "انتقالي",
                                    "realDateTime": "2024-11-09T04:04:24Z",
                                    "amount": 6000000,
                                    "balance": 1208584741214,
                                    "channelType": null,
                                    "categoryId": null,
                                    "comment": null,
                                    "hyperLinkType": 0,
                                    "rowNumber": null,
                                    "origKey": "14030839073424BCG12076",
                                    "channelDateTime": "2024-11-09T04:04:24Z"
                                }
                            ],
                            "outputRecordCount": 1
                        }
                    }
                    """;
        } else if (amount.equalsIgnoreCase("6000005")) {
            return """
                    {
                        "error": null,
                        "response": {
                            "accountStatementResponse": [
                                {
                                    "transactionId": -21923326335,
                                    "transactionDateTime": "2024-11-09T04:04:24Z",
                                    "transactionTraceCode": "BCG12076",
                                    "creditDebit": "CREDIT",
                                    "branchCode": 251,
                                    "branchName": null,
                                    "operatorCode": "BCGUSER",
                                    "additionalData1": "0217118343009",
                                    "additionalData2": "4890206809",
                                    "transactionCode": "008",
                                    "transactionDescription": "انتقالي",
                                    "realDateTime": "2024-11-09T04:04:24Z",
                                    "amount": 6000005,
                                    "balance": 1208584741214,
                                    "channelType": null,
                                    "categoryId": null,
                                    "comment": null,
                                    "hyperLinkType": 0,
                                    "rowNumber": null,
                                    "origKey": "14030819073424BCG12076",
                                    "channelDateTime": "2024-11-09T04:04:24Z"
                                }
                            ],
                            "outputRecordCount": 1
                        }
                    }
                    """;
        }

        return """
                {
                    "error": null,
                    "response": {
                        "accountStatementResponse": [
                            {
                                "transactionId": -21923326335,
                                "transactionDateTime": "2024-11-09T04:04:24Z",
                                "transactionTraceCode": "BCG12076",
                                "creditDebit": "CREDIT",
                                "branchCode": 251,
                                "branchName": null,
                                "operatorCode": "BCGUSER",
                                "additionalData1": "0217118343009",
                                "additionalData2": "4890206809",
                                "transactionCode": "008",
                                "transactionDescription": "انتقالي",
                                "realDateTime": "2024-11-09T04:04:24Z",
                                "amount": 1000000,
                                "balance": 1208584741214,
                                "channelType": null,
                                "categoryId": null,
                                "comment": null,
                                "hyperLinkType": 0,
                                "rowNumber": null,
                                "origKey": "14030903192858BCG13731",
                                "channelDateTime": "2024-11-09T04:04:24Z"
                            }
                        ],
                        "outputRecordCount": 1
                    }
                }
                """;
    }

    @Override
    public String sendSms(String token, String message, String mobile, int priority, int ttl) throws ChannelException, URISyntaxException {
        log.info("start statementUrl({}), mobile ({}),timeout({})", smsUrl, mobile, timeout);

        /*Map<String, String> headers = new HashMap<>();
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
        return "OK";
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
        return "{\n" +
                "\"error\": null,\n" +
                "\"response\": {\n" +
                "\"accounts\": [\n" +
                "\"0100000005002\",\n" +
                "\"0100000005003\",\n" +
                "\"0100000005004\",\n" +
                "\"0100000005005\",\n" +
                "\"0100000005006\",\n" +
                "\"0100000005007\",\n" +
                "\"0100000005008\",\n" +
                "\"0100000005009\",\n" +
                "\"0100000006007\",\n" +
                "\"0100000007007\",\n" +
                "\"0100000008008\"\n" +
                "]\n" +
                "}\n" +
                "}";
    }

    public static void main(String[] args) throws URISyntaxException, ChannelException {


        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJncmFudCI6IkNMSUVOVCIsImlzcyI6Imh0dHA6Ly9hcGkuYm1pLmlyL3NlY3VyaXR5IiwiYXVkIjoiZmlub2RhZC1zYWRhZC1jbGllbnQiLCJleHAiOjE3NDUzOTI2OTI5NDcsIm5iZiI6MTc0NTMwNjI5Mjk0Nywicm9sZSI6IiIsInNlcmlhbCI6IjI5Mjc0ZGJiLWM2Y2UtMzA4YS1iMGExLTViMWRjMjAwMmUzZiIsInNzbiI6IjExMTExMTUwNSIsImNsaWVudF9pZCI6IjExMTExMTUwNSIsInNjb3BlcyI6WyJzdmMtbWdtdC1tcS1zdG10LWluZm8iXX0=.JEk-1HlFTWz6bLCv14734Dg9ZfVk7dzvVq-r0ih7meU";

        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, "Bearer " + token);
        headers.put("Content-Type", "application/json");
        Map<String, String> params = new HashMap<>();

        JSONObject jsonObjectDate = new JSONObject();
        jsonObjectDate.put("fromDateTime", "2025-01-26T00:00:01.000Z");
        jsonObjectDate.put("toDateTime", "2025-01-27T00:00:00.000Z");
        JSONObject jsonObjectAmount = new JSONObject();
        jsonObjectAmount.put("fromAmount", Long.parseLong("90000000"));
        jsonObjectAmount.put("toAmount", Long.parseLong("90000000"));

        JSONObject json = new JSONObject();
        json.put("offsetNumber", 1);
        json.put("offsetLength", 100);


        json.put("accountNumber", "0222712529001");
        json.put("dateTimeRange", jsonObjectDate);
        json.put("amountRange", jsonObjectAmount);
        json.put("creditDebit", "CREDIT");
        StringEntity body = new StringEntity(json.toString(), StandardCharsets.UTF_8);

        URIBuilder builder = new URIBuilder("http://localhost:7070/api/mq-statement/v2/online-statement");

        log.info("start get statement with jsonRequest ({})", json.toString());

        String response = sendRequest(Utility.getCallerClassAndMethodName(), HttpMethod.POST, builder, Integer.parseInt("30000"), headers, params, body, new int[]{HttpStatus.OK.value()});
        log.info("({}), Finished for, with response({})", Utility.getCallerMethodName(), response);
        System.out.println(response);
    }
}
