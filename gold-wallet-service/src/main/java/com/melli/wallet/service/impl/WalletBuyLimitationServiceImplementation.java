package com.melli.wallet.service.impl;

import com.melli.wallet.domain.dto.AggregationPurchaseDTO;
import com.melli.wallet.domain.enumaration.TransactionTypeEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.WalletDailyBuyLimitationRepository;
import com.melli.wallet.domain.master.persistence.WalletMonthlyBuyLimitationRepository;
import com.melli.wallet.domain.redis.WalletDailyBuyLimitationRedis;
import com.melli.wallet.domain.redis.WalletMonthlyBuyLimitationRedis;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.util.Utility;
import com.melli.wallet.util.date.DateUtils;
import com.melli.wallet.utils.Helper;
import com.melli.wallet.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
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
    private final RequestService requestService;

    @Override
    public void deleteAll() {
        log.info("delete all buy daily");
        walletDailyBuyLimitationRepository.deleteAll();
        log.info("delete all buy monthly");
        walletMonthlyBuyLimitationRepository.deleteAll();
    }

    private void saveDaily(WalletDailyBuyLimitationRedis walletDailyBuyLimitationRedis) {
        walletDailyBuyLimitationRepository.save(walletDailyBuyLimitationRedis);
    }

    private void saveMonthly(WalletMonthlyBuyLimitationRedis walletMonthlyBuyLimitationRedis) {
        walletMonthlyBuyLimitationRepository.save(walletMonthlyBuyLimitationRedis);
    }

    @Override
    public void checkGeneral(ChannelEntity channel, WalletEntity wallet, BigDecimal quantity, WalletAccountEntity walletAccount, String uniqueIdentifier) throws InternalServiceException {
        log.info("checking checkBuyGeneral for nationalCode({}) ...", wallet.getNationalCode());

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();


        BigDecimal minQuantity = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MIN_QUANTITY_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxQuantity = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_QUANTITY_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        String key = walletAccount.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (quantity.compareTo(maxQuantity) > 0) {
                log.error("checkGeneral: buy quantity({}) for wallet({}), is bigger than maxQuantity({}) !!!", quantity, wallet.getNationalCode(), maxQuantity);
                throw new InternalServiceException("buy quantity is bigger than maxBuy", StatusService.QUANTITY_BIGGER_THAN_MAX, HttpStatus.OK, Map.ofEntries(
                        entry("1", String.valueOf(quantity)),
                        entry("2", String.valueOf((maxQuantity)))
                ));
            }

            if (quantity.compareTo(minQuantity) < 0) {
                log.error("checkGeneral: buy quantity({}) for wallet({}), is less than minQuantity({}) !!!", quantity, wallet.getNationalCode(), minQuantity);
                throw new InternalServiceException("buy quantity is less than minBuy", StatusService.QUANTITY_LESS_THAN_MIN, HttpStatus.OK, Map.ofEntries(
                        entry("1", String.valueOf(quantity.longValue())),
                        entry("2", String.valueOf((minQuantity)))
                ));
            }
            return null;
        }, uniqueIdentifier);
    }

    @Override
    public void checkDailyLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount, String uniqueIdentifier) throws InternalServiceException {

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        String key = walletAccount.getAccountNumber();

        BigDecimal maxCountDaily = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_DAILY_COUNT_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxQuantityDaily = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_DAILY_QUANTITY_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (maxCountDaily.longValue() <= 0) {
                log.info("countBuyDaily is zero and system skip check daily");
                return null;
            }

            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);

            String walletLimitationId = generateDailyLimitationKey(walletAccount);

            WalletDailyBuyLimitationRedis walletDailyBuyLimitationRedis = findDailyById(walletLimitationId);
            if (walletDailyBuyLimitationRedis == null) {
                log.info("walletDailyBuyLimitationRedis is null for walletAccount ({}) and nationalCode ({}) and start read from database", walletAccount.getAccountNumber(), wallet.getNationalCode());
                walletDailyBuyLimitationRedis = new WalletDailyBuyLimitationRedis();
                AggregationPurchaseDTO aggregationPurchaseDTO = requestService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccount.getId()}, TransactionTypeEnum.BUY.name(), new Date(), new Date());
                log.info("walletDailyBuyLimitationRedis read from database for walletAccount ({}), nationalCode ({}), sumAmount ({}), count ({}) for date ({})", walletAccount.getAccountNumber(), wallet.getNationalCode(), aggregationPurchaseDTO.getSumPrice(), aggregationPurchaseDTO.getCountRecord(), new Date());
                walletDailyBuyLimitationRedis.setId(walletLimitationId);
                walletDailyBuyLimitationRedis.setAmount(Long.parseLong(aggregationPurchaseDTO.getSumPrice()));
                walletDailyBuyLimitationRedis.setCount(Integer.parseInt(aggregationPurchaseDTO.getCountRecord()));
                walletDailyBuyLimitationRedis.setQuantity(new BigDecimal(aggregationPurchaseDTO.getSumQuantity()));
                walletDailyBuyLimitationRepository.save(walletDailyBuyLimitationRedis);
            }

            log.info("checkBuyDailyLimitation: SumPurchaseCount for wallet({}) in date: ({}) is: {}", wallet.getMobile(), currentDate, walletDailyBuyLimitationRedis.getAmount());

            if (walletDailyBuyLimitationRedis.getQuantity().add(amount).compareTo(maxQuantityDaily) > 0) {
                log.error("checkPurchaseDailyLimitation: wallet({}) on channel ({}) , exceeded amount limitation in purchase!!! SumPurchaseAmount plus amount is: {} and bigger than maxAmountDaily {}", wallet.getMobile(), wallet.getOwner().getId(), walletDailyBuyLimitationRedis.getQuantity().add(amount), maxQuantityDaily);
                throw new InternalServiceException("wallet sum amount buy exceeded the limitation !!!", StatusService.BUY_EXCEEDED_QUANTITY_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", String.valueOf(walletDailyBuyLimitationRedis.getQuantity().add(amount))),
                        entry("2", (String.valueOf(maxQuantityDaily)))
                ));
            }

            if ((walletDailyBuyLimitationRedis.getCount() + 1) > maxCountDaily.longValue()) {
                log.error("checkPurchaseDailyLimitation: wallet({}) on channel ({}) , exceeded count limitation in purchase!!!SumPurchaseCount is: {}", wallet.getMobile(), wallet.getOwner().getId(), walletDailyBuyLimitationRedis.getCount());
                throw new InternalServiceException("wallet count buy exceeded the limitation !!!", StatusService.BUY_EXCEEDED_COUNT_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletDailyBuyLimitationRedis.getAmount() + amount.longValue())),
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

        BigDecimal maxCountMonthly = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_MONTHLY_COUNT_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxQuantityMonthly = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_MONTHLY_QUANTITY_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (maxCountMonthly.longValue() <= 0) {
                log.info("maxCountMonthly is zero and system skip check daily");
                return null;
            }

            String walletLimitationId = generateMonthlyLimitationKey(walletAccount);

            WalletMonthlyBuyLimitationRedis walletMonthlyBuyLimitationRedis = findMonthlyById(walletLimitationId);
            if (walletMonthlyBuyLimitationRedis == null) {
                log.info("checkBuyMonthlyLimitation is null for walletAccount ({}) and nationalCode ({})", walletAccount.getAccountNumber(), wallet.getNationalCode());
                Date fromDate = DateUtils.findFirstDateOfPersianMonth(new Date());
                Date untilDate = new Date();
                log.info("checkBuyMonthlyLimitation is null for walletAccount ({}) and nationalCode ({}) and start read from database from Date ({}) until ({})", walletAccount.getAccountNumber(), wallet.getNationalCode(), fromDate, untilDate);
                walletMonthlyBuyLimitationRedis = new WalletMonthlyBuyLimitationRedis();
                AggregationPurchaseDTO aggregationPurchaseDTO = requestService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccount.getId()}, TransactionTypeEnum.BUY.name(), fromDate, untilDate);
                log.info("checkBuyMonthlyLimitation read from database for walletAccount ({}), nationalCode ({}), sumAmount ({}), count ({}) from Date ({}) until ({})", walletAccount.getAccountNumber(), wallet.getNationalCode(),
                        aggregationPurchaseDTO.getSumPrice(), aggregationPurchaseDTO.getCountRecord(), fromDate, untilDate);
                walletMonthlyBuyLimitationRedis.setId(walletLimitationId);
                walletMonthlyBuyLimitationRedis.setAmount(Long.parseLong(aggregationPurchaseDTO.getSumPrice()));
                walletMonthlyBuyLimitationRedis.setCount(Integer.parseInt(aggregationPurchaseDTO.getCountRecord()));
                walletMonthlyBuyLimitationRedis.setQuantity(new BigDecimal(aggregationPurchaseDTO.getSumQuantity()));
                walletMonthlyBuyLimitationRepository.save(walletMonthlyBuyLimitationRedis);
            }

            log.info("checkBuyMonthlyLimitation: SumPurchaseCount for wallet({}) in month: ({}) is: {}", wallet.getMobile(), helper.convertDateToMonth(new Date()), walletMonthlyBuyLimitationRedis.getAmount());

            if (walletMonthlyBuyLimitationRedis.getQuantity().add(amount).compareTo(maxQuantityMonthly) > 0) {
                log.error("checkBuyMonthlyLimitation: wallet({}) on channel ({}) , exceeded amount limitation in purchase!!! SumPurchaseAmount plus amount is: {} and bigger than maxAmountMonthly {}", wallet.getMobile(), wallet.getOwner().getId(), walletMonthlyBuyLimitationRedis.getAmount() + amount.longValue(), maxQuantityMonthly);
                throw new InternalServiceException("wallet sum amount buy exceeded the limitation !!!", StatusService.BUY_EXCEEDED_QUANTITY_MONTHLY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", String.valueOf(walletMonthlyBuyLimitationRedis.getQuantity().add(amount))),
                        entry("2", String.valueOf(maxQuantityMonthly))
                ));
            }

            if ((walletMonthlyBuyLimitationRedis.getCount() + 1) > maxCountMonthly.longValue()) {
                log.error("checkBuyMonthlyLimitation: wallet({}) on channel ({}) , exceeded count limitation in purchase!!!SumPurchaseCount is: {}", wallet.getMobile(), wallet.getOwner().getId(), walletMonthlyBuyLimitationRedis.getCount());
                throw new InternalServiceException("wallet count buy exceeded the limitation !!!", StatusService.BUY_EXCEEDED_COUNT_MONTHLY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletMonthlyBuyLimitationRedis.getAmount() + amount.longValue())),
                        entry("2", Utility.addComma((maxCountMonthly.longValue())))
                ));
            }

            return null;
        }, uniqueIdentifier);
    }


    private void updateBuyDailyLimitation(WalletAccountEntity walletAccount, BigDecimal amount, BigDecimal quantity, String uniqueIdentifier) throws InternalServiceException {
        log.info("start updating updateBuyDailyLimitation for walletAccount({}) ...", walletAccount.getAccountNumber());
        String key = walletAccount.getAccountNumber();
        redisLockService.runAfterLock(key, this.getClass(), () -> {
            String walletLimitationId = generateDailyLimitationKey(walletAccount);

            WalletDailyBuyLimitationRedis walletDailyBuyLimitationRedis = findDailyById(walletLimitationId);

            if (walletDailyBuyLimitationRedis == null) {

                log.info("start creating walletLimitation for walletAccount({}) for key: {}", walletAccount.getAccountNumber(), walletLimitationId);
                walletDailyBuyLimitationRedis = new WalletDailyBuyLimitationRedis();
                walletDailyBuyLimitationRedis.setQuantity(quantity);
                walletDailyBuyLimitationRedis.setId(walletLimitationId);
                walletDailyBuyLimitationRedis.setCount(1);
                walletDailyBuyLimitationRedis.setAmount(amount.longValue());

            } else {
                walletDailyBuyLimitationRedis.setQuantity(walletDailyBuyLimitationRedis.getQuantity().add(quantity));
                walletDailyBuyLimitationRedis.setCount(walletDailyBuyLimitationRedis.getCount() + 1);
                walletDailyBuyLimitationRedis.setAmount(walletDailyBuyLimitationRedis.getAmount() + amount.longValue());
            }
            saveDaily(walletDailyBuyLimitationRedis);
            log.info("finish updating walletDailyBuyLimitationRedis for walletAccount({}) ...", walletAccount.getAccountNumber());
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
            updateBuyMonthlyLimitation(walletAccount, amount, quantity, uniqueIdentifier);
            log.info("finish update monthlyLimitation for walletAccount ({}), amount ({})", walletAccount.getAccountNumber(), amount);
            log.info("start update dailyLimitation for walletAccount ({}), amount ({})", walletAccount.getAccountNumber(), amount);
            updateBuyDailyLimitation(walletAccount, amount, quantity, uniqueIdentifier);
            log.info("finish update dailyLimitation for walletAccount ({}), amount ({})", walletAccount.getAccountNumber(), amount);
        } catch (InternalServiceException e) {
            log.error("there is something wrong !!!! in updateBuyLimitation ==> ({})", e.getMessage());
        }
    }

    private void updateBuyMonthlyLimitation(WalletAccountEntity walletAccount, BigDecimal amount, BigDecimal quantity, String uniqueIdentifier) throws InternalServiceException {

        log.info("start updating updateBuyMonthlyLimitation for walletAccount({}) ...", walletAccount.getAccountNumber());
        String key = walletAccount.getAccountNumber();
        redisLockService.runAfterLock(key, this.getClass(), () -> {
            String walletLimitationId = generateMonthlyLimitationKey(walletAccount);

            WalletMonthlyBuyLimitationRedis walletMonthlyBuyLimitationRedis = findMonthlyById(walletLimitationId);

            if (walletMonthlyBuyLimitationRedis == null) {

                log.info("start creating walletMonthlyLimitation for walletAccount({}) for key: {}", walletAccount.getAccountNumber(), walletLimitationId);
                walletMonthlyBuyLimitationRedis = new WalletMonthlyBuyLimitationRedis();
                walletMonthlyBuyLimitationRedis.setId(walletLimitationId);
                walletMonthlyBuyLimitationRedis.setQuantity(quantity);
                walletMonthlyBuyLimitationRedis.setCount(1);
                walletMonthlyBuyLimitationRedis.setAmount(amount.longValue());

            } else {
                walletMonthlyBuyLimitationRedis.setQuantity(walletMonthlyBuyLimitationRedis.getQuantity().add(quantity));
                walletMonthlyBuyLimitationRedis.setCount(walletMonthlyBuyLimitationRedis.getCount() + 1);
                walletMonthlyBuyLimitationRedis.setAmount(walletMonthlyBuyLimitationRedis.getAmount() + amount.longValue());
            }
            saveMonthly(walletMonthlyBuyLimitationRedis);
            log.info("finish updating walletMonthlyBuyLimitationRedis for walletAccount({}) ...", walletAccount.getAccountNumber());
            return null;
        }, uniqueIdentifier);

    }

    private WalletDailyBuyLimitationRedis findDailyById(String id) {
        return walletDailyBuyLimitationRepository.findById(id).orElse(null);
    }

    private WalletMonthlyBuyLimitationRedis findMonthlyById(String id) {
        return walletMonthlyBuyLimitationRepository.findById(id).orElse(null);
    }
}
