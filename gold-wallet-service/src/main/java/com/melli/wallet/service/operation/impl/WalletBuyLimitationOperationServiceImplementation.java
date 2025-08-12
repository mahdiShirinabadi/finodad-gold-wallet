package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.AggregationPurchaseDTO;
import com.melli.wallet.domain.enumaration.TransactionTypeEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.redis.WalletDailyBuyLimitationRedis;
import com.melli.wallet.domain.redis.WalletMonthlyBuyLimitationRedis;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.WalletBuyLimitationOperationService;
import com.melli.wallet.service.repository.*;
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
public class WalletBuyLimitationOperationServiceImplementation implements WalletBuyLimitationOperationService {


    private final WalletBuyLimitationRepositoryService walletBuyLimitationRepositoryService;
    private final LimitationGeneralCustomRepositoryService limitationGeneralCustomRepositoryService;
    private final RedisLockService redisLockService;
    private final Helper helper;
    private final RequestRepositoryService requestRepositoryService;

    @Override
    public void deleteAll() {
        walletBuyLimitationRepositoryService.deleteAll();
    }



    @Override
    public void checkGeneral(ChannelEntity channel, WalletEntity wallet, BigDecimal quantity, WalletAccountEntity walletAccount, String uniqueIdentifier, WalletAccountCurrencyEntity walletAccountCurrencyEntity) throws InternalServiceException {
        log.info("checking checkBuyGeneral for nationalCode({}) ...", wallet.getNationalCode());

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();


        BigDecimal minQuantity = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MIN_QUANTITY_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxQuantity = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MAX_QUANTITY_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        String key = walletAccount.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (quantity.compareTo(maxQuantity) > 0) {
                log.error("checkGeneral: buy quantity({}) for wallet({}), is bigger than maxQuantity({}) !!!", quantity, wallet.getNationalCode(), maxQuantity);
                throw new InternalServiceException("buy quantity is bigger than maxBuy", StatusRepositoryService.QUANTITY_BIGGER_THAN_MAX, HttpStatus.OK, Map.ofEntries(
                        entry("1", String.valueOf(quantity)),
                        entry("2", String.valueOf((maxQuantity)))
                ));
            }

            if (quantity.compareTo(minQuantity) < 0) {
                log.error("checkGeneral: buy quantity({}) for wallet({}), is less than minQuantity({}) !!!", quantity, wallet.getNationalCode(), minQuantity);
                throw new InternalServiceException("buy quantity is less than minBuy", StatusRepositoryService.QUANTITY_LESS_THAN_MIN, HttpStatus.OK, Map.ofEntries(
                        entry("1", String.valueOf(quantity.longValue())),
                        entry("2", String.valueOf((minQuantity)))
                ));
            }
            return null;
        }, uniqueIdentifier);
    }

    @Override
    public void checkDailyLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal quantity, WalletAccountEntity walletAccount, String uniqueIdentifier, WalletAccountCurrencyEntity walletAccountCurrencyEntity) throws InternalServiceException {

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        String key = walletAccount.getAccountNumber();

        BigDecimal maxCountDaily = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MAX_DAILY_COUNT_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxQuantityDaily = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MAX_DAILY_QUANTITY_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (maxCountDaily.longValue() <= 0) {
                log.info("countBuyDaily is zero and system skip check daily");
                return null;
            }

            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);

            String walletLimitationId = helper.generateDailyLimitationKey(walletAccount);

            WalletDailyBuyLimitationRedis walletDailyBuyLimitationRedis = findDailyById(walletLimitationId);
            if (walletDailyBuyLimitationRedis == null) {
                log.info("walletDailyBuyLimitationRedis is null for walletAccount ({}) and nationalCode ({}) and start read from database", walletAccount.getAccountNumber(), wallet.getNationalCode());
                walletDailyBuyLimitationRedis = new WalletDailyBuyLimitationRedis();
                AggregationPurchaseDTO aggregationPurchaseDTO = requestRepositoryService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccount.getId()}, TransactionTypeEnum.BUY.name(), new Date(), new Date());
                log.info("walletDailyBuyLimitationRedis read from database for walletAccount ({}), nationalCode ({}), sumAmount ({}), count ({}) for date ({})", walletAccount.getAccountNumber(), wallet.getNationalCode(), aggregationPurchaseDTO.getSumPrice(), aggregationPurchaseDTO.getCountRecord(), new Date());
                walletDailyBuyLimitationRedis.setId(walletLimitationId);
                walletDailyBuyLimitationRedis.setAmount(Long.parseLong(aggregationPurchaseDTO.getSumPrice()));
                walletDailyBuyLimitationRedis.setCount(Integer.parseInt(aggregationPurchaseDTO.getCountRecord()));
                walletDailyBuyLimitationRedis.setQuantity(new BigDecimal(aggregationPurchaseDTO.getSumQuantity()));
                walletBuyLimitationRepositoryService.saveDaily(walletDailyBuyLimitationRedis);
            }

            log.info("checkBuyDailyLimitation: SumPurchaseQuantity for wallet({}) in date: ({}) is: {}", wallet.getNationalCode(), currentDate, walletDailyBuyLimitationRedis.getQuantity());

            if (walletDailyBuyLimitationRedis.getQuantity().add(quantity).compareTo(maxQuantityDaily) > 0) {
                log.error("checkPurchaseDailyLimitation: wallet({}) on channel ({}) , exceeded quantity limitation in purchase!!! SumPurchaseQuantity plus amount is: {} and bigger than maxQuantityDaily {}", wallet.getNationalCode(), wallet.getOwner().getUsername(), walletDailyBuyLimitationRedis.getQuantity().add(quantity), maxQuantityDaily);
                throw new InternalServiceException("wallet sum amount buy exceeded the limitation !!!", StatusRepositoryService.BUY_EXCEEDED_QUANTITY_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", String.valueOf(walletDailyBuyLimitationRedis.getQuantity().add(quantity))),
                        entry("2", (String.valueOf(maxQuantityDaily)))
                ));
            }

            if ((walletDailyBuyLimitationRedis.getCount() + 1) > maxCountDaily.longValue()) {
                log.error("checkPurchaseDailyLimitation: wallet({}) on channel ({}) , exceeded count limitation in purchase!!!SumPurchaseCount is: {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletDailyBuyLimitationRedis.getCount());
                throw new InternalServiceException("wallet count buy exceeded the limitation !!!", StatusRepositoryService.BUY_EXCEEDED_COUNT_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletDailyBuyLimitationRedis.getAmount() + quantity.longValue())),
                        entry("2", Utility.addComma((maxCountDaily.longValue())))
                ));
            }

            return null;
        }, uniqueIdentifier);
    }

    @Override
    public void checkMonthlyLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount, String uniqueIdentifier, WalletAccountCurrencyEntity walletAccountCurrencyEntity) throws InternalServiceException {
        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        String key = walletAccount.getAccountNumber();

        Boolean checkMonthly = Boolean.parseBoolean(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MONTHLY_VALIDATION_CHECK_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxCountMonthly = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MAX_MONTHLY_COUNT_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxQuantityMonthly = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MAX_MONTHLY_QUANTITY_BUY, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));

        if(Boolean.FALSE.equals(checkMonthly)){
            log.info("MONTHLY_VALIDATION_CHECK_BUY is set to ({}) and system skip check monthly validation", checkMonthly);
            return;
        }


        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (maxCountMonthly.longValue() <= 0) {
                log.info("maxCountMonthly is zero and system skip check daily");
                return null;
            }

            String walletLimitationId = helper.generateMonthlyLimitationKey(walletAccount);

            WalletMonthlyBuyLimitationRedis walletMonthlyBuyLimitationRedis = findMonthlyById(walletLimitationId);
            if (walletMonthlyBuyLimitationRedis == null) {
                log.info("checkBuyMonthlyLimitation is null for walletAccount ({}) and nationalCode ({})", walletAccount.getAccountNumber(), wallet.getNationalCode());
                Date fromDate = DateUtils.findFirstDateOfPersianMonth(new Date());
                Date untilDate = new Date();
                log.info("checkBuyMonthlyLimitation is null for walletAccount ({}) and nationalCode ({}) and start read from database from Date ({}) until ({})", walletAccount.getAccountNumber(), wallet.getNationalCode(), fromDate, untilDate);
                walletMonthlyBuyLimitationRedis = new WalletMonthlyBuyLimitationRedis();
                AggregationPurchaseDTO aggregationPurchaseDTO = requestRepositoryService.findSumAmountByTransactionTypeBetweenDate(new long[]{walletAccount.getId()}, TransactionTypeEnum.BUY.name(), fromDate, untilDate);
                log.info("checkBuyMonthlyLimitation read from database for walletAccount ({}), nationalCode ({}), sumAmount ({}), count ({}) from Date ({}) until ({})", walletAccount.getAccountNumber(), wallet.getNationalCode(),
                        aggregationPurchaseDTO.getSumPrice(), aggregationPurchaseDTO.getCountRecord(), fromDate, untilDate);
                walletMonthlyBuyLimitationRedis.setId(walletLimitationId);
                walletMonthlyBuyLimitationRedis.setAmount(Long.parseLong(aggregationPurchaseDTO.getSumPrice()));
                walletMonthlyBuyLimitationRedis.setCount(Integer.parseInt(aggregationPurchaseDTO.getCountRecord()));
                walletMonthlyBuyLimitationRedis.setQuantity(new BigDecimal(aggregationPurchaseDTO.getSumQuantity()));
                walletBuyLimitationRepositoryService.saveMonthly(walletMonthlyBuyLimitationRedis);
            }



            if (walletMonthlyBuyLimitationRedis.getQuantity().add(amount).compareTo(maxQuantityMonthly) > 0) {
                log.error("checkBuyMonthlyLimitation: wallet({}) on channel ({}) , exceeded amount limitation in purchase!!! SumPurchaseAmount plus amount is: {} and bigger than maxAmountMonthly {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletMonthlyBuyLimitationRedis.getAmount() + amount.longValue(), maxQuantityMonthly);
                throw new InternalServiceException("wallet sum amount buy exceeded the limitation !!!", StatusRepositoryService.BUY_EXCEEDED_QUANTITY_MONTHLY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", String.valueOf(walletMonthlyBuyLimitationRedis.getQuantity().add(amount))),
                        entry("2", String.valueOf(maxQuantityMonthly))
                ));
            }

            log.info("checkBuyMonthlyLimitation: SumPurchaseCount for wallet({}) in month: ({}) is: {}", wallet.getNationalCode(), helper.convertDateToMonth(new Date()), walletMonthlyBuyLimitationRedis.getCount());

            if ((walletMonthlyBuyLimitationRedis.getCount() + 1) > maxCountMonthly.longValue()) {
                log.error("checkBuyMonthlyLimitation: wallet({}) on channel ({}) , exceeded count limitation in purchase!!!SumPurchaseCount is: {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletMonthlyBuyLimitationRedis.getCount());
                throw new InternalServiceException("wallet count buy exceeded the limitation !!!", StatusRepositoryService.BUY_EXCEEDED_COUNT_MONTHLY_LIMITATION, HttpStatus.OK, Map.ofEntries(
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
            String walletLimitationId = helper.generateDailyLimitationKey(walletAccount);

            WalletDailyBuyLimitationRedis walletDailyBuyLimitationRedis = findDailyById(walletLimitationId);

            if (walletDailyBuyLimitationRedis == null) {

                log.info("start creating walletLimitation for walletAccount({}) for key: {}", walletAccount.getAccountNumber(), walletLimitationId);
                walletDailyBuyLimitationRedis = new WalletDailyBuyLimitationRedis();
                walletDailyBuyLimitationRedis.setQuantity(quantity);
                walletDailyBuyLimitationRedis.setId(walletLimitationId);
                walletDailyBuyLimitationRedis.setCount(1);
                walletDailyBuyLimitationRedis.setAmount(amount.longValue());

            } else {
                log.info("walletLimitation for walletAccount({}) for key: {} is exist with data ({})", walletAccount.getAccountNumber(), walletLimitationId, walletDailyBuyLimitationRedis.toString());
                walletDailyBuyLimitationRedis.setQuantity(walletDailyBuyLimitationRedis.getQuantity().add(quantity));
                walletDailyBuyLimitationRedis.setCount(walletDailyBuyLimitationRedis.getCount() + 1);
                walletDailyBuyLimitationRedis.setAmount(walletDailyBuyLimitationRedis.getAmount() + amount.longValue());
                log.info("finish set value walletLimitation for walletAccount({}) for key: {} is exist with data ({})", walletAccount.getAccountNumber(), walletLimitationId, walletDailyBuyLimitationRedis.toString());
            }
            walletBuyLimitationRepositoryService.saveDaily(walletDailyBuyLimitationRedis);
            log.info("finish updating walletDailyBuyLimitationRedis for walletAccount({}) ...", walletAccount.getAccountNumber());
            return null;
        }, uniqueIdentifier);

    }


    @Override
    @Async("threadPoolExecutor")
    public void updateLimitation(WalletAccountEntity walletAccount, BigDecimal amount, BigDecimal quantity, String uniqueIdentifier, WalletAccountCurrencyEntity walletAccountCurrencyEntity) {
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
            String walletLimitationId = helper.generateMonthlyLimitationKey(walletAccount);

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
            walletBuyLimitationRepositoryService.saveMonthly(walletMonthlyBuyLimitationRedis);
            log.info("finish updating walletMonthlyBuyLimitationRedis for walletAccount({}) ...", walletAccount.getAccountNumber());
            return null;
        }, uniqueIdentifier);

    }

    private WalletDailyBuyLimitationRedis findDailyById(String id) {
        return walletBuyLimitationRepositoryService.findDailyById(id);
    }

    private WalletMonthlyBuyLimitationRedis findMonthlyById(String id) {
        return walletBuyLimitationRepositoryService.findMonthlyById(id);
    }
}
