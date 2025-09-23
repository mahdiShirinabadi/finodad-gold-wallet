package com.melli.wallet.service.operation.impl;

import com.melli.wallet.domain.dto.AggregationP2PDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.redis.WalletDailyP2pLimitationRedis;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.operation.WalletP2pLimitationOperationService;
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
 * Class Name: WalletP2pLimitationOperationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 5/12/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class WalletP2pLimitationOperationServiceImplementation implements WalletP2pLimitationOperationService {

    private final WalletP2pLimitationRepositoryService walletP2pLimitationRepositoryService;
    private final LimitationGeneralCustomRepositoryService limitationGeneralCustomRepositoryService;
    private final RedisLockService redisLockService;
    private final Helper helper;
    private final RequestRepositoryService requestRepositoryService;


    @Override
    public void checkGeneral(ChannelEntity channel, WalletEntity wallet, BigDecimal quantity, WalletAccountEntity walletAccount) throws InternalServiceException {
        log.info("checking checkSellGeneral for nationalCode({}) ...", wallet.getNationalCode());

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();


        BigDecimal minQuantity = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MIN_QUANTITY_P2P, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxQuantity = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MAX_QUANTITY_P2P, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        String key = walletAccount.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (quantity.compareTo(maxQuantity) > 0) {
                log.error("checkSellGeneral: p2p quantity({}) for wallet({}), is bigger than maxQuantity({}) !!!", quantity, wallet.getNationalCode(), maxQuantity);
                throw new InternalServiceException("p2p amount is bigger than maxSell", StatusRepositoryService.QUANTITY_BIGGER_THAN_MAX, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(quantity.longValue())),
                        entry("2", Utility.addComma((maxQuantity.longValue())))
                ));
            }

            if (quantity.compareTo(minQuantity)< 0) {
                log.error("checkSellGeneral: p2p amount({}) for wallet({}), is less than minQuantity({}) !!!", quantity, wallet.getNationalCode(), minQuantity);
                throw new InternalServiceException("p2p amount is less than minSell", StatusRepositoryService.QUANTITY_LESS_THAN_MIN, HttpStatus.OK, Map.ofEntries(
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


        BigDecimal maxQuantityDaily = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MAX_DAILY_QUANTITY_P2P, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxCountDaily = new BigDecimal(limitationGeneralCustomRepositoryService.getSetting(channel, LimitationGeneralService.MAX_DAILY_COUNT_P2P, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (maxCountDaily.longValue() <= 0) {
                log.info("MAX_DAILY_COUNT_SELL is zero and system skip check daily");
                return null;
            }

            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);

            String walletLimitationId = helper.generateDailyLimitationKey(walletAccount);

            WalletDailyP2pLimitationRedis walletDailyP2pLimitationRedis = walletP2pLimitationRepositoryService.findDailyById(walletLimitationId);
            if (walletDailyP2pLimitationRedis == null) {
                log.info("checkP2pDailyLimitation is null for walletAccount ({}) and nationalCode ({}) and start read from database", walletAccount.getAccountNumber(), wallet.getNationalCode());
                walletDailyP2pLimitationRedis = new WalletDailyP2pLimitationRedis();
                AggregationP2PDTO aggregationP2PDTO = requestRepositoryService.findP2pSumAmountByTransactionTypeBetweenDate(new long[]{walletAccount.getId()}, new Date(), new Date());
                log.info("checkP2pDailyLimitation read from database for walletAccount ({}), nationalCode ({}), sumAmount ({}), count ({}) for date ({})", walletAccount.getAccountNumber(), wallet.getNationalCode(), aggregationP2PDTO.getSumQuantity(), aggregationP2PDTO.getCountRecord(), new Date());
                walletDailyP2pLimitationRedis.setId(walletLimitationId);
                walletDailyP2pLimitationRedis.setQuantity(new BigDecimal(aggregationP2PDTO.getSumQuantity()));
                walletDailyP2pLimitationRedis.setCount(Integer.parseInt(aggregationP2PDTO.getCountRecord()));
                walletP2pLimitationRepositoryService.saveDaily(walletDailyP2pLimitationRedis);
            }

            log.info("checkSellDailyLimitation: SumPurchaseCount for wallet({}) in date: ({}) is: {}", wallet.getNationalCode(), currentDate, walletDailyP2pLimitationRedis.getQuantity());

            if ((walletDailyP2pLimitationRedis.getQuantity().add(quantity).compareTo(maxQuantityDaily)) > 0) {
                log.error("checkPurchaseDailyLimitation: wallet({}) on channel ({}) , exceeded amount limitation in purchase!!! SumPurchaseAmount plus amount is: {} and bigger than maxAmountDaily {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletDailyP2pLimitationRedis.getQuantity().add(quantity), maxQuantityDaily);
                throw new InternalServiceException("wallet sum amount sell exceeded the limitation !!!", StatusRepositoryService.EXCEEDED_AMOUNT_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", String.valueOf(walletDailyP2pLimitationRedis.getQuantity().add(quantity))),
                        entry("2", String.valueOf(maxQuantityDaily))
                ));
            }

            if ((walletDailyP2pLimitationRedis.getCount() + 1) > maxCountDaily.longValue()) {
                log.error("checkPurchaseDailyLimitation: wallet({}) on channel ({}) , exceeded count limitation in p2p!!!SumCount is: {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletDailyP2pLimitationRedis.getCount());
                throw new InternalServiceException("wallet count sell exceeded the limitation !!!", StatusRepositoryService.EXCEEDED_COUNT_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletDailyP2pLimitationRedis.getCount() + 1)),
                        entry("2", Utility.addComma((maxCountDaily.longValue())))
                ));
            }

            return null;
        }, uniqueIdentifier);
    }

    private void updateDailyLimitation(WalletAccountEntity walletAccount, BigDecimal quantity, String uniqueIdentifier) throws InternalServiceException {
        log.info("start updating walletDailySellLimitationRedis for walletAccount({}) ...", walletAccount.getAccountNumber());
        String key = walletAccount.getAccountNumber();
        redisLockService.runAfterLock(key, this.getClass(), () -> {
            String walletLimitationId = helper.generateDailyLimitationKey(walletAccount);

            WalletDailyP2pLimitationRedis walletDailyP2pLimitationRedis = walletP2pLimitationRepositoryService.findDailyById(walletLimitationId);

            if (walletDailyP2pLimitationRedis == null) {

                log.info("start creating WalletDailyP2pLimitationRedis for walletAccount({}) for key: {}", walletAccount.getAccountNumber(), walletLimitationId);
                walletDailyP2pLimitationRedis = new WalletDailyP2pLimitationRedis();
                walletDailyP2pLimitationRedis.setId(walletLimitationId);
                walletDailyP2pLimitationRedis.setQuantity(quantity);
                walletDailyP2pLimitationRedis.setCount(1);
            } else {
                walletDailyP2pLimitationRedis.setQuantity(walletDailyP2pLimitationRedis.getQuantity().add(quantity));
                walletDailyP2pLimitationRedis.setCount(walletDailyP2pLimitationRedis.getCount() + 1);
            }
            walletP2pLimitationRepositoryService.saveDaily(walletDailyP2pLimitationRedis);
            log.info("finish updating walletDailySellLimitationRedis for walletAccount({}) ...", walletAccount.getAccountNumber());
            return null;
        }, uniqueIdentifier);

    }

    @Override
    public void deleteAll() {
        walletP2pLimitationRepositoryService.deleteAll();
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
}
