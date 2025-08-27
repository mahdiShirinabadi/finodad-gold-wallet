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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Test Data Generator for creating 1000 test wallets
 * Run this test to generate test data
 */
@SpringBootTest
@ActiveProfiles("dev")
@Log4j2
public class TestDataGenerator {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final int TOTAL_WALLETS = 100;
    private static final String BASE_URL = "http://localhost:8010/wallet";
    private static final String CREATE_WALLET_URL = "/api/v1/wallet/create";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJnZW5lcmF0ZVRpbWUiOjE3NTYyNzY2MzI5MzksInN1YiI6ImFkbWluIiwiaWF0IjoxNzU2Mjc2NjMyLCJleHAiOjE3NTYyODI2MzJ9.idu7GHFSWlxsonhpGJlJrEkPzQ8Vza8it6wM_xOQGC4CMFUXkNdg1Tgow5Zj1vAEk0-cUQlW7KHy7MWCDW9Ung"; // Add your access token here
    
    // Iranian mobile prefixes
    private static final String[] MOBILE_PREFIXES = {
        "0910", "0911", "0912", "0913", "0914", "0915", "0916", "0917", "0918", "0919",
        "0990", "0991", "0992", "0993", "0994", "0995", "0996", "0997", "0998", "0999"
    };

    @Test
    public void generateTestWallets() {
        log.info("Starting test data generation for {} wallets", TOTAL_WALLETS);
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;
        Random random = new Random();
        
        for (int i = 0; i < TOTAL_WALLETS; i++) {
            try {
                // Generate unique 10-digit national code
                String nationalCode = generateNationalCode(i);
                
                // Generate unique mobile number
                String prefix = MOBILE_PREFIXES[random.nextInt(MOBILE_PREFIXES.length)];
                String mobile = prefix + String.format("%07d", random.nextInt(10000000));
                
                // Create request body
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("nationalCode", nationalCode);
                requestBody.put("mobileNumber", mobile);
                
                // Set headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(ACCESS_TOKEN); // Add Bearer token to header

                // Create HTTP entity
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                
                // Call API
                ResponseEntity<String> response = restTemplate.exchange(
                    BASE_URL + CREATE_WALLET_URL, 
                    HttpMethod.POST, 
                    request, 
                    String.class
                );
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    successCount++;
                    if (i % 100 == 0) {
                        log.info("Created wallet {}: NationalCode={}, Mobile={}, Status={}", 
                            i, nationalCode, mobile, response.getStatusCode());
                    }
                } else {
                    failureCount++;
                    log.warn("Failed to create wallet {}: NationalCode={}, Mobile={}, Status={}", 
                        i, nationalCode, mobile, response.getStatusCode());
                }
                
                // Small delay to avoid overwhelming the system
                Thread.sleep(50);
                
            } catch (Exception e) {
                failureCount++;
                log.error("Error creating wallet {}: {}", i, e.getMessage());
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        log.info("Test data generation completed!");
        log.info("Total wallets created: {}", successCount);
        log.info("Total failures: {}", failureCount);
        log.info("Total time: {} seconds", duration / 1000.0);
        log.info("Average time per wallet: {} ms", duration / (double) TOTAL_WALLETS);
    }
    
    /**
     * Generate a valid 10-digit Iranian national code
     */
    private String generateNationalCode(int index) {
        // Start with a base number and add index to ensure uniqueness
        long baseNumber = 1234567890L + index;
        
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
}
