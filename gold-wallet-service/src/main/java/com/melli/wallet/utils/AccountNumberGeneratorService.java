package com.melli.wallet.utils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
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


    public static String generateAccountNumber(Long walletId, String currentType) {
        // سال جاری (2 کاراکتر)
        String year = String.valueOf(Year.now().getValue()).substring(2);
          // تولید نصف UUID (8 کاراکتر)
        String halfUuid = generateHalfUUID(walletId);
        // شماره حساب نهایی
        return String.format("GW-%s-%s-%s", year, currentType, halfUuid);
    }

    /**
     * تولید نصف UUID (8 کاراکتر) با ترکیب wallet ID
     */
    public static String generateHalfUUID(Long walletId) {
        // تولید UUID کامل
        UUID fullUuid = UUID.randomUUID();

        // ترکیب UUID با wallet ID برای تضمین یکتا بودن
        long mostSignificant = fullUuid.getMostSignificantBits() ^ walletId;

        // تبدیل به Base36 و کوتاه کردن به 8 کاراکتر
        String halfUuid36 = Long.toString(Math.abs(mostSignificant), 36).toUpperCase();

        // اگر کمتر از 8 کاراکتر بود، با صفر پر کن
        if (halfUuid36.length() < 8) {
            halfUuid36 = String.format("%8s", halfUuid36).replace(' ', '0');
        } else if (halfUuid36.length() > 8) {
            halfUuid36 = halfUuid36.substring(0, 8);
        }

        return halfUuid36;
    }

}
