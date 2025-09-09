package com.melli.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Performance Data Generator for creating test data
 * This class generates comprehensive test data for performance testing
 */
@SpringBootTest
@ActiveProfiles("dev")
@Log4j2
public class PerformanceDataGenerator {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Configuration
    private static final String BASE_URL = "http://localhost:8010/wallet";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJnZW5lcmF0ZVRpbWUiOjE3NTYyNzY2MzI5MzksInN1YiI6ImFkbWluIiwiaWF0IjoxNzU2Mjc2NjMyLCJleHAiOjE3NTYyODI2MzJ9.idu7GHFSWlxsonhpGJlJrEkPzQ8Vza8it6wM_xOQGC4CMFUXkNdg1Tgow5Zj1vAEk0-cUQlW7KHy7MWCDW9Ung";

    // Test Data Configuration
    private static final int TOTAL_USERS = 1000;
    private static final int CASH_IN_PER_USER = 5;
    private static final int PURCHASE_PER_USER = 10;
    private static final int CASH_OUT_PER_USER = 3;
    private static final int THREAD_POOL_SIZE = 10;

    // Iranian mobile prefixes
    private static final String[] MOBILE_PREFIXES = {
        "0910", "0911", "0912", "0913", "0914", "0915", "0916", "0917", "0918", "0919",
        "0990", "0991", "0992", "0993", "0994", "0995", "0996", "0997", "0998", "0999"
    };

    @Test
    public void generateCompletePerformanceData() {
        log.info("شروع تولید داده‌های تست عملکرد کامل...");
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Create Users
            List<String> nationalCodes = createUsers();
            log.info("{} کاربر ایجاد شد", nationalCodes.size());

            // Step 2: Generate Cash In Transactions
            generateCashInTransactions(nationalCodes);
            log.info("تراکنش‌های Cash In تکمیل شد");

            // Step 3: Generate Purchase Transactions
            generatePurchaseTransactions(nationalCodes);
            log.info("تراکنش‌های خرید تکمیل شد");

            // Step 4: Generate Cash Out Transactions
            generateCashOutTransactions(nationalCodes);
            log.info("تراکنش‌های Cash Out تکمیل شد");

        } catch (Exception e) {
            log.error("خطا در تولید داده‌های تست: {}", e.getMessage(), e);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("تولید داده‌های تست تکمیل شد! زمان کل: {} ثانیه", duration / 1000.0);
    }

    /**
     * Test method to verify national code generation
     */
    @Test
    public void testNationalCodeGeneration() {
        log.info("تست تولید کدهای ملی...");
        Set<String> generatedCodes = new HashSet<>();
        
        for (int i = 0; i < TOTAL_USERS; i++) {
            String nationalCode = generateNationalCode(i);
            
            // Check if code is unique
            if (!generatedCodes.add(nationalCode)) {
                log.error("کد ملی تکراری پیدا شد در index {}: {}", i, nationalCode);
            }
            
            // Check if code is 10 digits
            if (nationalCode.length() != 10) {
                log.error("کد ملی باید 10 رقم باشد در index {}: {}", i, nationalCode);
            }
            
            // Log first 10 codes for verification
            if (i < 10) {
                log.info("کد ملی {}: {}", i, nationalCode);
            }
        }
        
        log.info("تعداد کدهای ملی منحصر به فرد تولید شده: {}", generatedCodes.size());
        log.info("تست تولید کدهای ملی تکمیل شد!");
    }

    /**
     * Create users with unique national codes
     */
    private List<String> createUsers() {
        List<String> nationalCodes = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < TOTAL_USERS; i++) {
                final int index = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        String nationalCode = generateNationalCode(index);
                        String mobile = generateMobile();
                        boolean success = createUser(nationalCode, mobile);
                        
                        if (success) {
                            synchronized (nationalCodes) {
                                nationalCodes.add(nationalCode);
                            }
                            if (index % 100 == 0) {
                                log.info("کاربر {} ایجاد شد: {}", index, nationalCode);
                            }
                        }
                    } catch (Exception e) {
                        log.error("خطا در ایجاد کاربر {}: {}", index, e.getMessage());
                    }
                }, executor);

                futures.add(future);
            }

            // Wait for all user creation to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return nationalCodes;
    }

    /**
     * Generate Cash In transactions for all users
     */
    private void generateCashInTransactions(List<String> nationalCodes) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String nationalCode : nationalCodes) {
                for (int i = 0; i < CASH_IN_PER_USER; i++) {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            generateCashInTransaction(nationalCode);
                        } catch (Exception e) {
                            log.error("خطا در Cash In برای {}: {}", nationalCode, e.getMessage());
                        }
                    }, executor);
                    futures.add(future);
                }
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Generate Purchase transactions for all users
     */
    private void generatePurchaseTransactions(List<String> nationalCodes) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String nationalCode : nationalCodes) {
                for (int i = 0; i < PURCHASE_PER_USER; i++) {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            generatePurchaseTransaction(nationalCode);
                        } catch (Exception e) {
                            log.error("خطا در Purchase برای {}: {}", nationalCode, e.getMessage());
                        }
                    }, executor);
                    futures.add(future);
                }
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Generate Cash Out transactions for all users
     */
    private void generateCashOutTransactions(List<String> nationalCodes) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String nationalCode : nationalCodes) {
                for (int i = 0; i < CASH_OUT_PER_USER; i++) {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        try {
                            generateCashOutTransaction(nationalCode);
                        } catch (Exception e) {
                            log.error("خطا در Cash Out برای {}: {}", nationalCode, e.getMessage());
                        }
                    }, executor);
                    futures.add(future);
                }
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Create a single user
     */
    private boolean createUser(String nationalCode, String mobile) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("nationalCode", nationalCode);
            requestBody.put("mobile", mobile);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(ACCESS_TOKEN);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                BASE_URL + "/api/v1/wallet/create",
                HttpMethod.POST,
                request,
                String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("خطا در ایجاد کاربر {}: {}", nationalCode, e.getMessage());
            return false;
        }
    }

    /**
     * Generate Cash In transaction
     */
    private void generateCashInTransaction(String nationalCode) {
        try {
            // Generate UUID
            long amount = new Random().nextInt(9000000) + 100000; // 100K to 10M
            
            Map<String, Object> uuidRequestBody = new HashMap<>();
            uuidRequestBody.put("nationalCode", nationalCode);
            uuidRequestBody.put("amount", String.valueOf(amount));
            uuidRequestBody.put("accountNumber", nationalCode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(ACCESS_TOKEN);

            HttpEntity<Map<String, Object>> uuidRequest = new HttpEntity<>(uuidRequestBody, headers);

            ResponseEntity<String> uuidResponse = restTemplate.exchange(
                BASE_URL + "/api/v1/cashIn/generate/uuid",
                HttpMethod.POST,
                uuidRequest,
                String.class
            );

            if (uuidResponse.getStatusCode().is2xxSuccessful()) {
                // Parse UUID from response
                Map<String, Object> responseMap = objectMapper.readValue(uuidResponse.getBody(), Map.class);
                Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                String uuid = (String) data.get("uniqueIdentifier");

                // Perform Cash In
                Map<String, Object> chargeRequestBody = new HashMap<>();
                chargeRequestBody.put("nationalCode", nationalCode);
                chargeRequestBody.put("uniqueIdentifier", uuid);
                chargeRequestBody.put("amount", String.valueOf(amount));
                chargeRequestBody.put("referenceNumber", "REF" + System.currentTimeMillis());
                chargeRequestBody.put("accountNumber", nationalCode);
                chargeRequestBody.put("additionalData", "performance test");
                chargeRequestBody.put("cashInType", "NORMAL");
                chargeRequestBody.put("sign", "test_sign");
                chargeRequestBody.put("dataString", "test_data");

                HttpEntity<Map<String, Object>> chargeRequest = new HttpEntity<>(chargeRequestBody, headers);

                restTemplate.exchange(
                    BASE_URL + "/api/v1/cashIn/charge",
                    HttpMethod.POST,
                    chargeRequest,
                    String.class
                );
            }

        } catch (Exception e) {
            log.error("خطا در Cash In برای {}: {}", nationalCode, e.getMessage());
        }
    }

    /**
     * Generate Purchase transaction
     */
    private void generatePurchaseTransaction(String nationalCode) {
        try {
            // Generate Buy UUID
            double quantity = new Random().nextDouble() * 100 + 1; // 1 to 100 grams
            long price = new Random().nextInt(5000000) + 1000000; // 1M to 6M per gram
            
            Map<String, Object> uuidRequestBody = new HashMap<>();
            uuidRequestBody.put("nationalCode", nationalCode);
            uuidRequestBody.put("quantity", String.valueOf(quantity));
            uuidRequestBody.put("price", String.valueOf(price));
            uuidRequestBody.put("accountNumber", nationalCode);
            uuidRequestBody.put("merchantId", "MERCHANT" + new Random().nextInt(1000));
            uuidRequestBody.put("currency", "GOLD");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(ACCESS_TOKEN);

            HttpEntity<Map<String, Object>> uuidRequest = new HttpEntity<>(uuidRequestBody, headers);

            ResponseEntity<String> uuidResponse = restTemplate.exchange(
                BASE_URL + "/api/v1/purchase/buy/generate/uuid",
                HttpMethod.POST,
                uuidRequest,
                String.class
            );

            if (uuidResponse.getStatusCode().is2xxSuccessful()) {
                // Parse UUID from response
                Map<String, Object> responseMap = objectMapper.readValue(uuidResponse.getBody(), Map.class);
                Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                String uuid = (String) data.get("uniqueIdentifier");

                // Perform Buy
                long totalPrice = (long) (quantity * price);
                
                Map<String, Object> buyRequestBody = new HashMap<>();
                buyRequestBody.put("nationalCode", nationalCode);
                buyRequestBody.put("uniqueIdentifier", uuid);
                buyRequestBody.put("quantity", String.valueOf(quantity));
                buyRequestBody.put("totalPrice", String.valueOf(totalPrice));
                buyRequestBody.put("walletAccountNumber", nationalCode);
                buyRequestBody.put("additionalData", "performance test purchase");
                buyRequestBody.put("merchantId", "MERCHANT" + new Random().nextInt(1000));
                buyRequestBody.put("sign", "test_sign");
                buyRequestBody.put("dataString", "test_data");

                HttpEntity<Map<String, Object>> buyRequest = new HttpEntity<>(buyRequestBody, headers);

                restTemplate.exchange(
                    BASE_URL + "/api/v1/purchase/buy",
                    HttpMethod.POST,
                    buyRequest,
                    String.class
                );
            }

        } catch (Exception e) {
            log.error("خطا در Purchase برای {}: {}", nationalCode, e.getMessage());
        }
    }

    /**
     * Generate Cash Out transaction
     */
    private void generateCashOutTransaction(String nationalCode) {
        try {
            // Generate UUID
            long amount = new Random().nextInt(5000000) + 100000; // 100K to 5M
            
            Map<String, Object> uuidRequestBody = new HashMap<>();
            uuidRequestBody.put("nationalCode", nationalCode);
            uuidRequestBody.put("amount", String.valueOf(amount));
            uuidRequestBody.put("accountNumber", nationalCode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(ACCESS_TOKEN);

            HttpEntity<Map<String, Object>> uuidRequest = new HttpEntity<>(uuidRequestBody, headers);

            ResponseEntity<String> uuidResponse = restTemplate.exchange(
                BASE_URL + "/api/v1/cashOut/generate/uuid",
                HttpMethod.POST,
                uuidRequest,
                String.class
            );

            if (uuidResponse.getStatusCode().is2xxSuccessful()) {
                // Parse UUID from response
                Map<String, Object> responseMap = objectMapper.readValue(uuidResponse.getBody(), Map.class);
                Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                String uuid = (String) data.get("uniqueIdentifier");

                // Perform Cash Out
                Map<String, Object> withdrawRequestBody = new HashMap<>();
                withdrawRequestBody.put("nationalCode", nationalCode);
                withdrawRequestBody.put("uniqueIdentifier", uuid);
                withdrawRequestBody.put("amount", String.valueOf(amount));
                withdrawRequestBody.put("referenceNumber", "REF" + System.currentTimeMillis());
                withdrawRequestBody.put("accountNumber", nationalCode);
                withdrawRequestBody.put("additionalData", "performance test withdrawal");
                withdrawRequestBody.put("sign", "test_sign");
                withdrawRequestBody.put("dataString", "test_data");

                HttpEntity<Map<String, Object>> withdrawRequest = new HttpEntity<>(withdrawRequestBody, headers);

                restTemplate.exchange(
                    BASE_URL + "/api/v1/cashOut/withdraw",
                    HttpMethod.POST,
                    withdrawRequest,
                    String.class
                );
            }

        } catch (Exception e) {
            log.error("خطا در Cash Out برای {}: {}", nationalCode, e.getMessage());
        }
    }

    /**
     * Generate a valid 10-digit Iranian national code
     */
    private String generateNationalCode(int index) {
        // Use different ranges to ensure we can generate 1000 unique codes
        long baseNumber;
        
        if (index < 100) {
            // First 100 codes: 1000000000 to 1000000099
            baseNumber = 1000000000L + index;
        } else if (index < 500) {
            // Next 400 codes: 2000000000 to 2000000399
            baseNumber = 2000000000L + (index - 100);
        } else if (index < 800) {
            // Next 300 codes: 3000000000 to 3000000299
            baseNumber = 3000000000L + (index - 500);
        } else {
            // Last 200 codes: 4000000000 to 4000000199
            baseNumber = 4000000000L + (index - 800);
        }

        // Convert to string and ensure it's 10 digits
        String nationalCode = String.format("%010d", baseNumber);

        // Calculate check digit (Iranian national code validation algorithm)
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(nationalCode.charAt(i)) * (10 - i);
        }
        int remainder = sum % 11;
        int checkDigit = remainder < 2 ? remainder : 11 - remainder;

        // Replace the last digit with the calculated check digit
        return nationalCode.substring(0, 9) + checkDigit;
    }

    /**
     * Generate a realistic Iranian mobile number
     */
    private String generateMobile() {
        String prefix = MOBILE_PREFIXES[new Random().nextInt(MOBILE_PREFIXES.length)];
        String suffix = String.format("%07d", new Random().nextInt(10000000));
        return prefix + suffix;
    }
}
