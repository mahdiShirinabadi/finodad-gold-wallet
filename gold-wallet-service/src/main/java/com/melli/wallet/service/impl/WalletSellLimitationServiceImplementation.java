package com.melli.wallet.service.impl;

import com.melli.wallet.domain.dto.AggregationPurchaseDTO;
import com.melli.wallet.domain.enumaration.TransactionTypeEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.WalletDailySellLimitationRepository;
import com.melli.wallet.domain.master.persistence.WalletMonthlySellLimitationRepository;
import com.melli.wallet.domain.redis.WalletDailySellLimitationRedis;
import com.melli.wallet.domain.redis.WalletMonthlySellLimitationRedis;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.util.Utility;
import com.melli.wallet.util.date.DateUtils;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import static java.util.Map.entry;

/**
 * Class Name: WalletSellLimitationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 5/12/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class WalletSellLimitationServiceImplementation implements WalletSellLimitationService {

    private final WalletMonthlySellLimitationRepository walletMonthlySellLimitationRepository;
    private final WalletDailySellLimitationRepository walletDailySellLimitationRepository;
    private final LimitationGeneralCustomService limitationGeneralCustomService;
    private final RedisLockService redisLockService;
    private final Helper helper;
    private final RequestService requestService;

    @Override
    public void deleteAll() {
        log.info("delete all sell daily");
        walletMonthlySellLimitationRepository.deleteAll();
        log.info("delete all sell monthly");
        walletDailySellLimitationRepository.deleteAll();
    }

    private void saveDaily(WalletDailySellLimitationRedis walletDailySellLimitationRedis) {
        walletDailySellLimitationRepository.save(walletDailySellLimitationRedis);
    }

    private void saveMonthly(WalletMonthlySellLimitationRedis walletMonthlySellLimitationRedis) {
        walletMonthlySellLimitationRepository.save(walletMonthlySellLimitationRedis);
    }

    @Override
    public void checkGeneral(ChannelEntity channel, WalletEntity wallet, BigDecimal quantity, WalletAccountEntity walletAccount, String uniqueIdentifier) throws InternalServiceException {
        log.info("checking checkSellGeneral for nationalCode({}) ...", wallet.getNationalCode());

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();


        BigDecimal minQuantity = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MIN_QUANTITY_SELL, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxQuantity = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_QUANTITY_SELL, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        String key = walletAccount.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (quantity.compareTo(maxQuantity) > 0) {
                log.error("checkSellGeneral: sell quantity({}) for wallet({}), is bigger than maxQuantity({}) !!!", quantity, wallet.getNationalCode(), maxQuantity);
                throw new InternalServiceException("sell amount is bigger than maxSell", StatusService.QUANTITY_BIGGER_THAN_MAX, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(quantity.longValue())),
                        entry("2", Utility.addComma((maxQuantity.longValue())))
                ));
            }

            if (quantity.compareTo(minQuantity)< 0) {
                log.error("checkSellGeneral: sell amount({}) for wallet({}), is less than minQuantity({}) !!!", quantity, wallet.getNationalCode(), minQuantity);
                throw new InternalServiceException("sell amount is less than minSell", StatusService.QUANTITY_LESS_THAN_MIN, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(quantity.longValue())),
                        entry("2", Utility.addComma((minQuantity.longValue())))
                ));
            }
            return null;
        }, uniqueIdentifier);
    }

    @Override
    public void checkDailyLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal quantity, WalletAccountEntity walletAccount, String uniqueIdentifier) throws InternalServiceException {

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        String key = walletAccount.getAccountNumber();

        BigDecimal maxQuantityDaily = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_DAILY_QUANTITY_SELL, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxCountDaily = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_DAILY_COUNT_SELL, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (maxCountDaily.longValue() <= 0) {
                log.info("MAX_DAILY_COUNT_SELL is zero and system skip check daily");
                return null;
            }

            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);

            String walletLimitationId = generateDailyLimitationKey(walletAccount);

            WalletDailySellLimitationRedis walletDailySellLimitationRedis = findDailyById(walletLimitationId);
            if (walletDailySellLimitationRedis == null) {
                log.info("checkSellDailyLimitation is null for walletAccount ({}) and nationalCode ({}) and start read from database", walletAccount.getAccountNumber(), wallet.getNationalCode());
                walletDailySellLimitationRedis = new WalletDailySellLimitationRedis();
                AggregationPurchaseDTO aggregationPurchaseDTO = requestService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccount.getId()}, TransactionTypeEnum.SELL.name(), new Date(), new Date());
                log.info("checkSellDailyLimitation read from database for walletAccount ({}), nationalCode ({}), sumAmount ({}), count ({}) for date ({})", walletAccount.getAccountNumber(), wallet.getNationalCode(), aggregationPurchaseDTO.getSumPrice(), aggregationPurchaseDTO.getCountRecord(), new Date());
                walletDailySellLimitationRedis.setId(walletLimitationId);
                walletDailySellLimitationRedis.setAmount(Long.parseLong(aggregationPurchaseDTO.getSumQuantity()));
                walletDailySellLimitationRedis.setQuantity(new BigDecimal(aggregationPurchaseDTO.getSumQuantity()));
                walletDailySellLimitationRedis.setCount(Integer.parseInt(aggregationPurchaseDTO.getCountRecord()));
                walletDailySellLimitationRepository.save(walletDailySellLimitationRedis);
            }

            log.info("checkSellDailyLimitation: SumPurchaseCount for wallet({}) in date: ({}) is: {}", wallet.getNationalCode(), currentDate, walletDailySellLimitationRedis.getAmount());

            if ((walletDailySellLimitationRedis.getQuantity().add(quantity).compareTo(maxQuantityDaily)) > 0) {
                log.error("checkPurchaseDailyLimitation: wallet({}) on channel ({}) , exceeded amount limitation in purchase!!! SumPurchaseAmount plus amount is: {} and bigger than maxAmountDaily {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletDailySellLimitationRedis.getQuantity().add(quantity), maxQuantityDaily);
                throw new InternalServiceException("wallet sum amount sell exceeded the limitation !!!", StatusService.SELL_EXCEEDED_AMOUNT_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletDailySellLimitationRedis.getAmount() + quantity.longValue())),
                        entry("2", Utility.addComma((maxQuantityDaily.longValue())))
                ));
            }

            if ((walletDailySellLimitationRedis.getCount() + 1) > maxCountDaily.longValue()) {
                log.error("checkPurchaseDailyLimitation: wallet({}) on channel ({}) , exceeded count limitation in purchase!!!SumPurchaseCount is: {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletDailySellLimitationRedis.getCount());
                throw new InternalServiceException("wallet count sell exceeded the limitation !!!", StatusService.SELL_EXCEEDED_COUNT_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletDailySellLimitationRedis.getAmount() + quantity.longValue())),
                        entry("2", Utility.addComma((maxCountDaily.longValue())))
                ));
            }

            return null;
        }, uniqueIdentifier);
    }

    @Override
    public void checkMonthlyLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount, String uniqueIdentifier) throws InternalServiceException {
        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        String key = walletAccount.getAccountNumber();

        BigDecimal maxCountMonthly = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_MONTHLY_COUNT_SELL, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxQuantityMonthly = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_MONTHLY_QUANTITY_SELL, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (maxCountMonthly.longValue() <= 0) {
                log.info("MAX_MONTHLY_COUNT_SELL is zero and system skip check daily");
                return null;
            }

            String walletLimitationId = generateMonthlyLimitationKey(walletAccount);

            WalletMonthlySellLimitationRedis walletMonthlySellLimitationRedis = findMonthlyById(walletLimitationId);
            if (walletMonthlySellLimitationRedis == null) {
                log.info("checkSellMonthlyLimitation is null for walletAccount ({}) and nationalCode ({})", walletAccount.getAccountNumber(), wallet.getNationalCode());
                Date fromDate = DateUtils.findFirstDateOfPersianMonth(new Date());
                Date untilDate = new Date();
                log.info("checkMonthlyLimitation is null for walletAccount ({}) and nationalCode ({}) and start read from database from Date ({}) until ({})", walletAccount.getAccountNumber(), wallet.getNationalCode(), fromDate, untilDate);
                walletMonthlySellLimitationRedis = new WalletMonthlySellLimitationRedis();
                AggregationPurchaseDTO aggregationPurchaseDTO = requestService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccount.getId()}, TransactionTypeEnum.SELL.name(), fromDate, untilDate);
                log.info("checkSellMonthlyLimitation read from database for walletAccount ({}), nationalCode ({}), sumAmount ({}), count ({}) from Date ({}) until ({})", walletAccount.getAccountNumber(), wallet.getNationalCode(),
                        aggregationPurchaseDTO.getSumPrice(), aggregationPurchaseDTO.getCountRecord(), fromDate, untilDate);
                walletMonthlySellLimitationRedis.setId(walletLimitationId);
                walletMonthlySellLimitationRedis.setAmount(Long.parseLong(aggregationPurchaseDTO.getSumPrice()));
                walletMonthlySellLimitationRedis.setCount(Integer.parseInt(aggregationPurchaseDTO.getCountRecord()));
                walletMonthlySellLimitationRedis.setQuantity(new BigDecimal(aggregationPurchaseDTO.getSumQuantity()));
                walletMonthlySellLimitationRepository.save(walletMonthlySellLimitationRedis);
            }

            log.info("checkSellMonthlyLimitation: SumPurchaseCount for wallet({}) in month: ({}) is: {}", wallet.getNationalCode(), helper.convertDateToMonth(new Date()), walletMonthlySellLimitationRedis.getAmount());

            if (walletMonthlySellLimitationRedis.getQuantity().add(amount).compareTo(maxQuantityMonthly)> 0) {
                log.error("checkSellMonthlyLimitation: wallet({}) on channel ({}) , exceeded amount limitation in purchase!!! SumPurchaseAmount plus amount is: {} and bigger than maxAmountMonthly {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletMonthlySellLimitationRedis.getAmount() + amount.longValue(), maxQuantityMonthly);
                throw new InternalServiceException("wallet sum amount sell exceeded the limitation !!!", StatusService.SELL_EXCEEDED_AMOUNT_MONTHLY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletMonthlySellLimitationRedis.getAmount() + amount.longValue())),
                        entry("2", Utility.addComma((maxQuantityMonthly.longValue())))
                ));
            }

            if ((walletMonthlySellLimitationRedis.getCount() + 1) > maxCountMonthly.longValue()) {
                log.error("checkSellMonthlyLimitation: wallet({}) on channel ({}) , exceeded count limitation in purchase!!!SumPurchaseCount is: {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletMonthlySellLimitationRedis.getCount());
                throw new InternalServiceException("wallet count sell exceeded the limitation !!!", StatusService.SELL_EXCEEDED_COUNT_MONTHLY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletMonthlySellLimitationRedis.getAmount() + amount.longValue())),
                        entry("2", Utility.addComma((maxCountMonthly.longValue())))
                ));
            }

            return null;
        }, uniqueIdentifier);
    }



    private void updateDailyLimitation(WalletAccountEntity walletAccount, BigDecimal amount, BigDecimal quantity, String uniqueIdentifier) throws InternalServiceException {
        log.info("start updating walletDailySellLimitationRedis for walletAccount({}) ...", walletAccount.getAccountNumber());
        String key = walletAccount.getAccountNumber();
        redisLockService.runAfterLock(key, this.getClass(), () -> {
            String walletLimitationId = generateDailyLimitationKey(walletAccount);

            WalletDailySellLimitationRedis walletDailySellLimitationRedis = findDailyById(walletLimitationId);

            if (walletDailySellLimitationRedis == null) {

                log.info("start creating walletDailySellLimitationRedis for walletAccount({}) for key: {}", walletAccount.getAccountNumber(), walletLimitationId);
                walletDailySellLimitationRedis = new WalletDailySellLimitationRedis();
                walletDailySellLimitationRedis.setId(walletLimitationId);
                walletDailySellLimitationRedis.setQuantity(quantity);
                walletDailySellLimitationRedis.setCount(1);
                walletDailySellLimitationRedis.setAmount(amount.longValue());

            } else {
                walletDailySellLimitationRedis.setQuantity(walletDailySellLimitationRedis.getQuantity().add(quantity));
                walletDailySellLimitationRedis.setCount(walletDailySellLimitationRedis.getCount() + 1);
                walletDailySellLimitationRedis.setAmount(walletDailySellLimitationRedis.getAmount() + amount.longValue());
            }
            saveDaily(walletDailySellLimitationRedis);
            log.info("finish updating walletDailySellLimitationRedis for walletAccount({}) ...", walletAccount.getAccountNumber());
            return null;
        }, uniqueIdentifier);

    }

    private String generateDailyLimitationKey(WalletAccountEntity walletAccount) {
        String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);
        return walletAccount.getAccountNumber() + currentDate;
    }

    private String generateMonthlyLimitationKey(WalletAccountEntity walletAccount) {
        String currentPersianMonth = String.valueOf(helper.convertDateToMonth(new Date()));
        return walletAccount.getAccountNumber() + currentPersianMonth;
    }

    @Override
    @Async("threadPoolExecutor")
    public void updateLimitation(WalletAccountEntity walletAccount, BigDecimal amount, BigDecimal quantity, String uniqueIdentifier) throws InternalServiceException {
        try {
            log.info("start update monthlyLimitation for walletAccount ({}), amount ({})", walletAccount.getAccountNumber(), amount);
            updateMonthlyLimitation(walletAccount, amount, quantity, uniqueIdentifier);
            log.info("finish update monthlyLimitation for walletAccount ({}), amount ({})", walletAccount.getAccountNumber(), amount);
            log.info("start update dailyLimitation for walletAccount ({}), amount ({})", walletAccount.getAccountNumber(), amount);
            updateDailyLimitation(walletAccount, amount, quantity, uniqueIdentifier);
            log.info("finish update dailyLimitation for walletAccount ({}), amount ({})", walletAccount.getAccountNumber(), amount);
        } catch (InternalServiceException e) {
            log.error("there is something wrong !!!! in updateSellLimitation ==> ({})", e.getMessage());
        }
    }

    private void updateMonthlyLimitation(WalletAccountEntity walletAccount, BigDecimal amount, BigDecimal quantity, String uniqueIdentifier) throws InternalServiceException {

        log.info("start updating updateSellMonthlyLimitation for walletAccount({}) ...", walletAccount.getAccountNumber());
        String key = walletAccount.getAccountNumber();
        redisLockService.runAfterLock(key, this.getClass(), () -> {
            String walletLimitationId = generateMonthlyLimitationKey(walletAccount);

            WalletMonthlySellLimitationRedis walletMonthlySellLimitationRedis = findMonthlyById(walletLimitationId);

            if (walletMonthlySellLimitationRedis == null) {

                log.info("start creating walletMonthlyLimitation for walletAccount({}) for key: {}", walletAccount.getAccountNumber(), walletLimitationId);
                walletMonthlySellLimitationRedis = new WalletMonthlySellLimitationRedis();
                walletMonthlySellLimitationRedis.setId(walletLimitationId);
                walletMonthlySellLimitationRedis.setQuantity(quantity);
                walletMonthlySellLimitationRedis.setCount(1);
                walletMonthlySellLimitationRedis.setAmount(amount.longValue());

            } else {
                walletMonthlySellLimitationRedis.setQuantity(walletMonthlySellLimitationRedis.getQuantity().add(quantity));
                walletMonthlySellLimitationRedis.setCount(walletMonthlySellLimitationRedis.getCount() + 1);
                walletMonthlySellLimitationRedis.setAmount(walletMonthlySellLimitationRedis.getAmount() + amount.longValue());
            }
            saveMonthly(walletMonthlySellLimitationRedis);
            log.info("finish updating walletMonthlySellLimitationRedis for walletAccount({}) ...", walletAccount.getAccountNumber());
            return null;
        }, uniqueIdentifier);

    }

    private WalletDailySellLimitationRedis findDailyById(String id) {
        return walletDailySellLimitationRepository.findById(id).orElse(null);
    }

    private WalletMonthlySellLimitationRedis findMonthlyById(String id) {
        return walletMonthlySellLimitationRepository.findById(id).orElse(null);
    }
}
