package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.WalletDailyBuyLimitationRepository;
import com.melli.wallet.domain.master.persistence.WalletMonthlyBuyLimitationRepository;
import com.melli.wallet.domain.redis.WalletDailyBuyLimitationRedis;
import com.melli.wallet.domain.redis.WalletMonthlyBuyLimitationRedis;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.LimitationGeneralCustomService;
import com.melli.wallet.service.LimitationGeneralService;
import com.melli.wallet.service.StatusService;
import com.melli.wallet.service.WalletBuyLimitationService;
import com.melli.wallet.util.Utility;
import com.melli.wallet.util.date.DateUtils;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import static java.util.Map.entry;

/**
 * Class Name: WalletBuyLimitationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 5/12/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class WalletBuyLimitationServiceImplementation implements WalletBuyLimitationService {

    private final WalletMonthlyBuyLimitationRepository walletMonthlyBuyLimitationRepository;
    private final WalletDailyBuyLimitationRepository walletDailyBuyLimitationRepository;
    private final LimitationGeneralCustomService limitationGeneralCustomService;
    private final RedisLockService redisLockService;
    private final Helper helper;

    private void saveDaily(WalletDailyBuyLimitationRedis walletDailyBuyLimitationRedis) {
        walletDailyBuyLimitationRepository.save(walletDailyBuyLimitationRedis);
    }

    private void saveMonthly(WalletMonthlyBuyLimitationRedis walletMonthlyBuyLimitationRedis) {
        walletMonthlyBuyLimitationRepository.save(walletMonthlyBuyLimitationRedis);
    }

    @Override
    public void checkBuyGeneral(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount, String uniqueIdentifier) throws InternalServiceException {
        log.info("checking CashInLimitation for nationalCode({}) ...", wallet.getNationalCode());

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();


        BigDecimal minAmount = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MIN_AMOUNT_SELL, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxAmount = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_AMOUNT_SELL, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        String key = walletAccount.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (amount.longValue() > maxAmount.longValue()) {
                log.error("checkBuyGeneral: buy amount({}) for wallet({}), is bigger than maxCashIn({}) !!!", amount, wallet.getNationalCode(), maxAmount);
                throw new InternalServiceException("buy amount is bigger than maxBuy", StatusService.AMOUNT_BIGGER_THAN_MAX, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(amount.longValue())),
                        entry("2", Utility.addComma((maxAmount.longValue())))
                ));
            }

            if (amount.longValue() < minAmount.longValue()) {
                log.error("checkBuyGeneral: buy amount({}) for wallet({}), is less than minCashIn({}) !!!", amount, wallet.getNationalCode(), minAmount);
                throw new InternalServiceException("buy amount is less than minBuy", StatusService.AMOUNT_LESS_THAN_MIN, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(amount.longValue())),
                        entry("2", Utility.addComma((minAmount.longValue())))
                ));
            }
            return null;
        }, uniqueIdentifier);
    }

    @Override
    public void checkBuyDailyLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount, MerchantEntity merchant, String uniqueIdentifier) throws InternalServiceException {

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        String key = walletAccount.getAccountNumber();

        BigDecimal maxCountDaily = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_DAILY_COUNT_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxAmountDaily = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_DAILY_AMOUNT_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (maxCountDaily.longValue() <= 0) {
                log.info("countBuyDaily is zero and system skip check daily");
                return null;
            }

            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);
            String walletLimitationId = walletAccount.getAccountNumber() + currentDate;
            WalletDailyBuyLimitationRedis walletDailyBuyLimitationRedis = findDailyById(walletLimitationId);
            if (walletDailyBuyLimitationRedis == null) {
                log.info("walletDailyBuyLimitationRedis is null for walletAccount ({}) and nationalCode ({})", walletAccount.getAccountNumber(), wallet.getNationalCode());
                return null;
            }

            log.info("checkBuyDailyLimitation: SumPurchaseCount for wallet({}) in date: ({}) is: {}", wallet.getMobile(), currentDate, walletDailyBuyLimitationRedis.getAmount());

            if ((walletDailyBuyLimitationRedis.getAmount() + amount.longValue()) > maxAmountDaily.longValue()) {
                log.error("checkPurchaseDailyLimitation: wallet({}) on channel ({}) , exceeded amount limitation in purchase!!! SumPurchaseAmount plus amount is: {} and bigger than maxAmountDaily {}", wallet.getMobile(), wallet.getOwner().getId(), walletDailyBuyLimitationRedis.getAmount() + amount.longValue(), maxAmountDaily);
                throw new InternalServiceException("wallet sum amount buy exceeded the limitation !!!", StatusService.BUY_EXCEEDED_AMOUNT_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletDailyBuyLimitationRedis.getAmount() + amount.longValue())),
                        entry("2", Utility.addComma((maxAmountDaily.longValue())))
                ));
            }

            if ((walletDailyBuyLimitationRedis.getCount() + 1) > maxCountDaily.longValue()) {
                log.error("checkPurchaseDailyLimitation: wallet({}) on channel ({}) , exceeded count limitation in purchase!!!SumPurchaseCount is: {}", wallet.getMobile(), wallet.getOwner().getId(), walletDailyBuyLimitationRedis.getCount());
                throw new InternalServiceException("wallet count buy exceeded the limitation !!!", StatusService.BUY_EXCEEDED_COUNT_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletDailyBuyLimitationRedis.getAmount() + amount.longValue())),
                        entry("2", Utility.addComma((maxAmountDaily.longValue())))
                ));
            }

            return null;
        }, uniqueIdentifier);
    }

    @Override
    public void checkBuyMonthlyLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount, MerchantEntity merchant, String uniqueIdentifier) throws InternalServiceException {
        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        String key = walletAccount.getAccountNumber();

        BigDecimal maxCountMonthly = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_MONTHLY_COUNT_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxAmountMonthly = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_MONTHLY_AMOUNT_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (maxCountMonthly.longValue() <= 0) {
                log.info("maxCountMonthly is zero and system skip check daily");
                return null;
            }

            String currentPersianMonth = String.valueOf(helper.convertDateToMonth(new Date()));
            String walletLimitationId = walletAccount.getAccountNumber() + currentPersianMonth;
            WalletMonthlyBuyLimitationRedis walletDailyBuyLimitationRedis = findMonthlyById(walletLimitationId);
            if (walletDailyBuyLimitationRedis == null) {
                log.info("checkBuyMonthlyLimitation is null for walletAccount ({}) and nationalCode ({})", walletAccount.getAccountNumber(), wallet.getNationalCode());
                return null;
            }

            log.info("checkBuyMonthlyLimitation: SumPurchaseCount for wallet({}) in month: ({}) is: {}", wallet.getMobile(), helper.convertDateToMonth(new Date()), walletDailyBuyLimitationRedis.getAmount());

            if ((walletDailyBuyLimitationRedis.getAmount() + amount.longValue()) > maxAmountMonthly.longValue()) {
                log.error("checkBuyMonthlyLimitation: wallet({}) on channel ({}) , exceeded amount limitation in purchase!!! SumPurchaseAmount plus amount is: {} and bigger than maxAmountDaily {}", wallet.getMobile(), wallet.getOwner().getId(), walletDailyBuyLimitationRedis.getAmount() + amount.longValue(), maxAmountDaily);
                throw new InternalServiceException("wallet sum amount buy exceeded the limitation !!!", StatusService.BUY_EXCEEDED_AMOUNT_MONTHLY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletDailyBuyLimitationRedis.getAmount() + amount.longValue())),
                        entry("2", Utility.addComma((maxAmountMonthly.longValue())))
                ));
            }

            if ((walletDailyBuyLimitationRedis.getCount() + 1) > maxCountMonthly.longValue()) {
                log.error("checkBuyMonthlyLimitation: wallet({}) on channel ({}) , exceeded count limitation in purchase!!!SumPurchaseCount is: {}", wallet.getMobile(), wallet.getOwner().getId(), walletDailyBuyLimitationRedis.getCount());
                throw new InternalServiceException("wallet count buy exceeded the limitation !!!", StatusService.BUY_EXCEEDED_COUNT_MONTHLY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletDailyBuyLimitationRedis.getAmount() + amount.longValue())),
                        entry("2", Utility.addComma((maxCountMonthly.longValue())))
                ));
            }

            return null;
        }, uniqueIdentifier);
    }

    @Override
    public void updateBuyDailyLimitation(WalletAccountEntity walletAccount, BigDecimal amount) throws InternalServiceException {

    }

    @Override
    public void updateBuyMonthlyLimitation(WalletAccountEntity walletAccount, BigDecimal amount) throws InternalServiceException {

    }

    private WalletDailyBuyLimitationRedis findDailyById(String id) {
        return walletDailyBuyLimitationRepository.findById(id).orElse(null);
    }

    private WalletMonthlyBuyLimitationRedis findMonthlyById(String id) {
        return walletMonthlyBuyLimitationRepository.findById(id).orElse(null);
    }
}
