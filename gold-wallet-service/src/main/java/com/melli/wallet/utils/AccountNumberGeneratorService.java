package com.melli.wallet.utils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Arrays;
import java.util.UUID;

/**
 * Class Name: AccountNumberGeneratorService
 * Author: Mahdi Shirinabadi
 * Date: 9/2/2025
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class AccountNumberGeneratorService {


    /**
     * تولید شماره حساب داخلی
     */
    public String generateAccountNumberInternal(Long walletId, String currencyType) {
        // Hash از Wallet ID (4 کاراکتر)
        String walletHash = generateWalletHash(walletId);
                // نصف UUID (8 کاراکتر)
        String halfUuid = generateHalfUUID(walletId);

        return String.format("%s-%s-%s", walletHash, currencyType, halfUuid);
    }

    /**
     * تولید Hash از Wallet ID
     */
    private String generateWalletHash(Long walletId) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(walletId.toString().getBytes(StandardCharsets.UTF_8));

            long hash = Arrays.hashCode(hashBytes);
            String hash36 = Long.toString(Math.abs(hash), 36).toUpperCase();

            if (hash36.length() < 4) {
                hash36 = String.format("%4s", hash36).replace(' ', '0');
            } else if (hash36.length() > 4) {
                hash36 = hash36.substring(0, 4);
            }

            return hash36;
        } catch (Exception e) {
            log.error("خطا در تولید Hash: {}", e.getMessage());
            return generateSimpleHash(walletId);
        }
    }

    /**
     * تولید Hash ساده
     */
    private String generateSimpleHash(Long walletId) {
        long hash = walletId ^ (walletId >>> 32) ^ (walletId << 16);
        String hash36 = Long.toString(Math.abs(hash), 36).toUpperCase();

        if (hash36.length() < 4) {
            hash36 = String.format("%4s", hash36).replace(' ', '0');
        } else if (hash36.length() > 4) {
            hash36 = hash36.substring(0, 4);
        }

        return hash36;
    }

    /**
     * تولید نصف UUID
     */
    private String generateHalfUUID(Long walletId) {
        UUID fullUuid = UUID.randomUUID();
        long mostSignificant = fullUuid.getMostSignificantBits() ^ walletId;

        String uuid36 = Long.toString(Math.abs(mostSignificant), 36).toUpperCase();

        if (uuid36.length() < 8) {
            uuid36 = String.format("%8s", uuid36).replace(' ', '0');
        } else if (uuid36.length() > 8) {
            uuid36 = uuid36.substring(0, 8);
        }

        return uuid36;
    }


}
