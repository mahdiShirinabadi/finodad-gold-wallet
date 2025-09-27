package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.AggregationGiftCardDTO;
import com.melli.wallet.domain.dto.AggregationGiftCardPaymentDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.redis.WalletDailyGiftCardLimitationRedis;
import com.melli.wallet.domain.redis.WalletDailyPaymentGiftCardLimitationRedis;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.WalletGiftCardLimitationOperationService;
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
 * Class Name: WalletGiftCardLimitationOperationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 9/22/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class WalletGiftCardLimitationOperationServiceImplementation implements WalletGiftCardLimitationOperationService {

    private final LimitationGeneralCustomRepositoryService limitationGeneralCustomRepositoryService;
    private final WalletGiftCardLimitationRepositoryService walletGiftCardLimitationRepositoryService;
    private final WalletGiftCardPaymentLimitationRepositoryService walletGiftCardPaymentLimitationRepositoryService;
    private final RedisLockService redisLockService;
    private final RequestRepositoryService requestRepositoryService;
    private final Helper helper;


    @Override
    public void checkGeneral(ChannelEntity channel, WalletEntity wallet, BigDecimal quantity, WalletAccountEntity walletAccount) throws InternalServiceException {
        log.info("checking checkSellGeneral for nationalCode({}) ...", wallet.getNationalCode());

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        Boolean enableGiftCard = Boolean.parseBoolean(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.ENABLE_GIFT_CARD, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));

        if (Boolean.FALSE.equals(enableGiftCard)) {
            log.error("checkGiftCadLimitation: ENABLE_GIFT_CARD in for channel {}, walletAccountCurrencyEntity ({}) and walletAccountTypeEntity ({}) walletTypeEntity ({}) is not permission !!!", channel.getUsername(), walletAccountCurrencyEntity.getName(), walletAccountTypeEntity.getName(), walletTypeEntity.getName());
            StringBuilder st;
            st = new StringBuilder();
            st.append("checkGiftCadLimitation: account (").append( walletAccount.getAccountNumber()).append(") dont permission to giftCard");
            throw new InternalServiceException(st.toString(), StatusRepositoryService.ACCOUNT_DONT_PERMISSION_FOR_GIFT_CARD, HttpStatus.OK);
        }


        BigDecimal minQuantity = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MIN_QUANTITY_GIFT_CARD, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxQuantity = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MAX_QUANTITY_GIFT_CARD, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        String key = walletAccount.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (quantity.compareTo(maxQuantity) > 0) {
                log.error("checkSellGeneral: giftCard quantity({}) for wallet({}), is bigger than maxQuantity({}) !!!", quantity, wallet.getNationalCode(), maxQuantity);
                throw new InternalServiceException("giftCard amount is bigger than maxQuantity", StatusRepositoryService.QUANTITY_BIGGER_THAN_MAX, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(quantity.longValue())),
                        entry("2", Utility.addComma((maxQuantity.longValue())))
                ));
            }

            if (quantity.compareTo(minQuantity)< 0) {
                log.error("checkSellGeneral: giftCard amount({}) for wallet({}), is less than minQuantity({}) !!!", quantity, wallet.getNationalCode(), minQuantity);
                throw new InternalServiceException("giftCard amount is less than minQuantity", StatusRepositoryService.QUANTITY_LESS_THAN_MIN, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(quantity.longValue())),
                        entry("2", Utility.addComma((minQuantity.longValue())))
                ));
            }
            return null;
        }, key);
    }

    @Override
    public void checkDailyLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal quantity, WalletAccountEntity walletAccount, String uniqueIdentifier) throws InternalServiceException {

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        String key = walletAccount.getAccountNumber();


        BigDecimal maxQuantityDaily = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MAX_DAILY_QUANTITY_GIFT_CARD, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxCountDaily = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MAX_DAILY_COUNT_GIFT_CARD, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (maxCountDaily.longValue() <= 0) {
                log.info("MAX_DAILY_COUNT_GIFT_CARD is zero and system skip check daily");
                return null;
            }

            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);

            String walletLimitationId = helper.generateDailyLimitationKey(walletAccount);

            WalletDailyGiftCardLimitationRedis walletDailyGiftCardLimitationRedis = walletGiftCardLimitationRepositoryService.findDailyById(walletLimitationId);
            if (walletDailyGiftCardLimitationRedis == null) {
                log.info("checkGiftCardDailyLimitation is null for walletAccount ({}) and nationalCode ({}) and start read from database", walletAccount.getAccountNumber(), wallet.getNationalCode());
                walletDailyGiftCardLimitationRedis = new WalletDailyGiftCardLimitationRedis();
                AggregationGiftCardDTO aggregationDTO = requestRepositoryService.findGiftCardSumAmountByTransactionTypeBetweenDate(new long[]{walletAccount.getId()}, new Date(), new Date());
                log.info("checkGiftCardDailyLimitation read from database for walletAccount ({}), nationalCode ({}), sumAmount ({}), count ({}) for date ({})", walletAccount.getAccountNumber(), wallet.getNationalCode(), aggregationDTO.getSumQuantity(), aggregationDTO.getCountRecord(), new Date());
                walletDailyGiftCardLimitationRedis.setId(walletLimitationId);
                walletDailyGiftCardLimitationRedis.setQuantity(new BigDecimal(aggregationDTO.getSumQuantity()));
                walletDailyGiftCardLimitationRedis.setCount(Integer.parseInt(aggregationDTO.getCountRecord()));
                walletGiftCardLimitationRepositoryService.saveDaily(walletDailyGiftCardLimitationRedis);
            }

            log.info("checkSellDailyLimitation: SumPurchaseCount for wallet({}) in date: ({}) is: {}", wallet.getNationalCode(), currentDate, walletDailyGiftCardLimitationRedis.getQuantity());

            if ((walletDailyGiftCardLimitationRedis.getQuantity().add(quantity).compareTo(maxQuantityDaily)) > 0) {
                log.error("checkGiftCardDailyLimitation: wallet({}) on channel ({}) , exceeded amount limitation in checkGiftCardDailyLimitation!!! checkGiftCardDailyLimitation plus amount is: {} and bigger than maxAmountDaily {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletDailyGiftCardLimitationRedis.getQuantity().add(quantity), maxQuantityDaily);
                throw new InternalServiceException("wallet sum amount sell exceeded the limitation !!!", StatusRepositoryService.EXCEEDED_AMOUNT_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", String.valueOf(walletDailyGiftCardLimitationRedis.getQuantity().add(quantity))),
                        entry("2", String.valueOf(maxQuantityDaily))
                ));
            }

            if ((walletDailyGiftCardLimitationRedis.getCount() + 1) > maxCountDaily.longValue()) {
                log.error("checkGiftCardDailyLimitation: wallet({}) on channel ({}) , exceeded count limitation in giftCard!!!SumCount is: {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletDailyGiftCardLimitationRedis.getCount());
                throw new InternalServiceException("wallet count sell exceeded the limitation !!!", StatusRepositoryService.EXCEEDED_COUNT_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletDailyGiftCardLimitationRedis.getCount() + 1)),
                        entry("2", Utility.addComma((maxCountDaily.longValue())))
                ));
            }

            return null;
        }, uniqueIdentifier);
    }

    @Override
    public void checkDailyPaymentLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal quantity, WalletAccountEntity walletAccount, String uniqueIdentifier) throws InternalServiceException {
        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        String key = walletAccount.getAccountNumber();


        BigDecimal maxQuantityDaily = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MAX_DAILY_QUANTITY_PAYMENT_GIFT_CARD, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxCountDaily = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MAX_DAILY_COUNT_PAYMENT_GIFT_CARD, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (maxCountDaily.longValue() <= 0) {
                log.info("MAX_DAILY_COUNT_PAYMENT_GIFT_CARD is zero and system skip check daily");
                return null;
            }

            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);

            String walletLimitationId = helper.generateDailyLimitationKey(walletAccount);

            WalletDailyPaymentGiftCardLimitationRedis paymentGiftCardLimitationRedis = walletGiftCardPaymentLimitationRepositoryService.findDailyById(walletLimitationId);
            if (paymentGiftCardLimitationRedis == null) {
                log.info("checkDailyPaymentLimitation is null for walletAccount ({}) and nationalCode ({}) and start read from database", walletAccount.getAccountNumber(), wallet.getNationalCode());
                paymentGiftCardLimitationRedis = new WalletDailyPaymentGiftCardLimitationRedis();
                AggregationGiftCardPaymentDTO aggregationDTO = requestRepositoryService.findGiftCardPaymentSumAmountByTransactionTypeBetweenDate(new long[]{walletAccount.getId()}, new Date(), new Date());
                log.info("checkDailyPaymentLimitation read from database for walletAccount ({}), nationalCode ({}), sumAmount ({}), count ({}) for date ({})", walletAccount.getAccountNumber(), wallet.getNationalCode(), aggregationDTO.getSumQuantity(), aggregationDTO.getCountRecord(), new Date());
                paymentGiftCardLimitationRedis.setId(walletLimitationId);
                paymentGiftCardLimitationRedis.setQuantity(new BigDecimal(aggregationDTO.getSumQuantity()));
                paymentGiftCardLimitationRedis.setCount(Integer.parseInt(aggregationDTO.getCountRecord()));
                walletGiftCardPaymentLimitationRepositoryService.saveDaily(paymentGiftCardLimitationRedis);
            }

            log.info("checkDailyPaymentLimitation: SumPurchaseCount for wallet({}) in date: ({}) is: {}", wallet.getNationalCode(), currentDate, paymentGiftCardLimitationRedis.getQuantity());

            if ((paymentGiftCardLimitationRedis.getQuantity().add(quantity).compareTo(maxQuantityDaily)) > 0) {
                log.error("checkDailyPaymentLimitation: wallet({}) on channel ({}) , exceeded amount limitation in checkGiftCardDailyLimitation!!! checkGiftCardDailyLimitation plus amount is: {} and bigger than maxAmountDaily {}", wallet.getNationalCode(), wallet.getOwner().getId(), paymentGiftCardLimitationRedis.getQuantity().add(quantity), maxQuantityDaily);
                throw new InternalServiceException("wallet sum amount sell exceeded the limitation !!!", StatusRepositoryService.EXCEEDED_AMOUNT_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", String.valueOf(paymentGiftCardLimitationRedis.getQuantity().add(quantity))),
                        entry("2", String.valueOf(maxQuantityDaily))
                ));
            }

            if ((paymentGiftCardLimitationRedis.getCount() + 1) > maxCountDaily.longValue()) {
                log.error("checkDailyPaymentLimitation: wallet({}) on channel ({}) , exceeded count limitation in giftCard!!!SumCount is: {}", wallet.getNationalCode(), wallet.getOwner().getId(), paymentGiftCardLimitationRedis.getCount());
                throw new InternalServiceException("wallet count sell exceeded the limitation !!!", StatusRepositoryService.EXCEEDED_COUNT_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(paymentGiftCardLimitationRedis.getCount() + 1)),
                        entry("2", Utility.addComma((maxCountDaily.longValue())))
                ));
            }

            return null;
        }, uniqueIdentifier);
    }

    @Override
    @Async("threadPoolExecutor")
    public void updatePaymentLimitation(WalletAccountEntity walletAccount, BigDecimal quantity, String uniqueIdentifier) throws InternalServiceException {
        try {
            log.info("start update dailyActiveLimitation for walletAccount ({}), quantity ({})", walletAccount.getAccountNumber(), quantity);
            updatePaymentDailyLimitation(walletAccount, quantity, uniqueIdentifier);
            log.info("finish update dailyActiveLimitation for walletAccount ({}), quantity ({})", walletAccount.getAccountNumber(), quantity);
        } catch (InternalServiceException e) {
            log.error("there is something wrong !!!! in dailyActiveLimitation ==> ({})", e.getMessage());
        }
    }

    private void updateDailyLimitation(WalletAccountEntity walletAccount, BigDecimal quantity, String uniqueIdentifier) throws InternalServiceException {
        log.info("start updating walletDailySellLimitationRedis for walletAccount({}) ...", walletAccount.getAccountNumber());
        String key = walletAccount.getAccountNumber();
        redisLockService.runAfterLock(key, this.getClass(), () -> {
            String walletLimitationId = helper.generateDailyLimitationKey(walletAccount);

            WalletDailyGiftCardLimitationRedis walletDailyGiftCardLimitationRedis = walletGiftCardLimitationRepositoryService.findDailyById(walletLimitationId);

            if (walletDailyGiftCardLimitationRedis == null) {

                log.info("start creating updateDailyLimitation for walletAccount({}) for key: {}", walletAccount.getAccountNumber(), walletLimitationId);
                walletDailyGiftCardLimitationRedis = new WalletDailyGiftCardLimitationRedis();
                walletDailyGiftCardLimitationRedis.setId(walletLimitationId);
                walletDailyGiftCardLimitationRedis.setQuantity(quantity);
                walletDailyGiftCardLimitationRedis.setCount(1);
            } else {
                walletDailyGiftCardLimitationRedis.setQuantity(walletDailyGiftCardLimitationRedis.getQuantity().add(quantity));
                walletDailyGiftCardLimitationRedis.setCount(walletDailyGiftCardLimitationRedis.getCount() + 1);
            }
            walletGiftCardLimitationRepositoryService.saveDaily(walletDailyGiftCardLimitationRedis);
            log.info("finish updating updateDailyLimitation for walletAccount({}) ...", walletAccount.getAccountNumber());
            return null;
        }, uniqueIdentifier);

    }


    private void updatePaymentDailyLimitation(WalletAccountEntity walletAccount, BigDecimal quantity, String uniqueIdentifier) throws InternalServiceException {
        log.info("start updating walletActiveDailySellLimitationRedis for walletAccount({}) ...", walletAccount.getAccountNumber());
        String key = walletAccount.getAccountNumber();
        redisLockService.runAfterLock(key, this.getClass(), () -> {
            String walletLimitationId = helper.generateDailyLimitationKey(walletAccount);

            WalletDailyPaymentGiftCardLimitationRedis walletDailyPaymentGiftCardLimitationRedis = walletGiftCardPaymentLimitationRepositoryService.findDailyById(walletLimitationId);

            if (walletDailyPaymentGiftCardLimitationRedis == null) {

                log.info("start creating WalletDailyP2pLimitationRedis for walletAccount({}) for key: {}", walletAccount.getAccountNumber(), walletLimitationId);
                walletDailyPaymentGiftCardLimitationRedis = new WalletDailyPaymentGiftCardLimitationRedis();
                walletDailyPaymentGiftCardLimitationRedis.setId(walletLimitationId);
                walletDailyPaymentGiftCardLimitationRedis.setQuantity(quantity);
                walletDailyPaymentGiftCardLimitationRedis.setCount(1);
            } else {
                walletDailyPaymentGiftCardLimitationRedis.setQuantity(walletDailyPaymentGiftCardLimitationRedis.getQuantity().add(quantity));
                walletDailyPaymentGiftCardLimitationRedis.setCount(walletDailyPaymentGiftCardLimitationRedis.getCount() + 1);
            }
            walletGiftCardPaymentLimitationRepositoryService.saveDaily(walletDailyPaymentGiftCardLimitationRedis);
            log.info("finish updating walletDailySellLimitationRedis for walletAccount({}) ...", walletAccount.getAccountNumber());
            return null;
        }, uniqueIdentifier);

    }

    @Override
    @Async("threadPoolExecutor")
    public void updateLimitation(WalletAccountEntity walletAccount, BigDecimal quantity, String uniqueIdentifier) {
        try {
            log.info("start update dailyLimitation for walletAccount ({}), quantity ({})", walletAccount.getAccountNumber(), quantity);
            updateDailyLimitation(walletAccount, quantity, uniqueIdentifier);
            log.info("finish update dailyLimitation for walletAccount ({}), quantity ({})", walletAccount.getAccountNumber(), quantity);
        } catch (InternalServiceException e) {
            log.error("there is something wrong !!!! in updateSellLimitation ==> ({})", e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        log.info("delete all walletGiftRedis");
        walletGiftCardLimitationRepositoryService.deleteAll();
        walletGiftCardPaymentLimitationRepositoryService.deleteAll();
    }
}
