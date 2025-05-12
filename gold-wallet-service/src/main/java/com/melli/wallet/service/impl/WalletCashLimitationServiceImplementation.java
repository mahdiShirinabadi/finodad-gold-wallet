package com.melli.wallet.service.impl;

import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.WalletCashInLimitationRepository;
import com.melli.wallet.domain.redis.WalletCashInLimitationRedis;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
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
import java.util.Optional;

import static java.util.Map.entry;

/**
 * Class Name: WalletLimitationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/26/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class WalletCashLimitationServiceImplementation implements WalletCashLimitationService {

    private final LimitationGeneralCustomService limitationGeneralCustomService;
    private final WalletCashInLimitationRepository walletCashInLimitationRepository;
    private final WalletAccountService walletAccountService;
    private final RedisLockService redisLockService;

    private void save(WalletCashInLimitationRedis walletCashInLimitationRedis) {
        walletCashInLimitationRepository.save(walletCashInLimitationRedis);
    }

    @Override
    public void checkCashInLimitation(ChannelEntity channel, WalletEntity wallet, long amount, WalletAccountEntity walletAccount) throws InternalServiceException {
        log.info("checking CashInLimitation for nationalCode({}) ...", wallet.getNationalCode());

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        Boolean enableCashIn = Boolean.parseBoolean(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.ENABLE_CASH_IN, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));

        if (Boolean.FALSE.equals(enableCashIn)) {
            log.error("checkCashInLimitation: cash in for channel {}, walletAccountCurrencyEntity ({}) and walletAccountTypeEntity ({}) walletTypeEntity ({}) is not permission !!!", channel, walletAccountCurrencyEntity.getName(), walletAccountTypeEntity.getName(), walletTypeEntity.getName());
            throw new InternalServiceException("account (" + walletAccount.getAccountNumber() + ") dont permission to cashIn", StatusService.ACCOUNT_DONT_PERMISSION_FOR_CASH_IN, HttpStatus.OK);
        }

        BigDecimal minAmount = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxAmount = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_AMOUNT_CASH_IN, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxBalance = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_WALLET_BALANCE, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxCashInDaily = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_WALLET_AMOUNT_DAILY_CASH_IN, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        String key = walletAccount.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (amount > maxAmount.longValue()) {
                log.error("checkCashInLimitation: CashIn's amount({}) for wallet({}), is bigger than maxCashIn({}) !!!", amount, wallet.getNationalCode(), maxAmount);
                throw new InternalServiceException("CashIn's amount is bigger than minCashIn", StatusService.AMOUNT_BIGGER_THAN_MAX, HttpStatus.OK,
                        Map.ofEntries(
                                entry("1", Utility.addComma(amount)),
                                entry("2", Utility.addComma(maxAmount.longValue()))
                        )
                );
            }

            if (amount < minAmount.longValue()) {
                log.error("checkCashInLimitation: CashIn's amount({}) for wallet({}), is less than minCashIn({}) !!!", amount, wallet.getNationalCode(), minAmount);
                throw new InternalServiceException("CashIn's amount is less than minCashIn", StatusService.AMOUNT_LESS_THAN_MIN, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(amount)),
                        entry("2", Utility.addComma(minAmount.longValue()))
                ));
            }

            long balance = walletAccountService.getBalance(walletAccount.getId());

            if (balance + amount > maxBalance.longValue()) {
                log.error("cashIn's amount({}) for wallet({}) with balance ({}), is more than maxBalance({}) !!!", amount, wallet.getNationalCode(), balance, maxBalance.longValue());
                throw new InternalServiceException("cashIn's amount is more than maxBalance", StatusService.BALANCE_MORE_THAN_STANDARD, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(balance + amount)),
                        entry("2", Utility.addComma(maxBalance.longValue()))
                ));
            }

            log.info("Start checking daily cashIn limitation for wallet({}) ...", wallet.getNationalCode());

            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);
            String walletLimitationId = walletAccount.getId() + "-" + currentDate;

            WalletCashInLimitationRedis walletCashInLimitationRedis = findById(walletLimitationId);

            if (walletCashInLimitationRedis == null) {
                log.info("wallet with nationalCode ({}) dont have any cashIn in date({})", wallet.getNationalCode(), currentDate);
                return null;
            }

            log.info("SumCashInAmount for wallet({}) in date: ({}) is: {}", wallet.getNationalCode(), currentDate, walletCashInLimitationRedis.getCashInDailyAmount());

            if ((walletCashInLimitationRedis.getCashInDailyAmount() + amount) > maxCashInDaily.longValue()) {
                log.error("wallet({}) on channel ({}) , exceeded amount limitation in cashIn!!! SumCashInAmount is: {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletCashInLimitationRedis.getCashInDailyAmount());
                throw new InternalServiceException("wallet exceeded the limitation !!!", StatusService.WALLET_EXCEEDED_AMOUNT_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletCashInLimitationRedis.getCashInDailyAmount() + amount)),
                        entry("2", Utility.addComma((maxCashInDaily.longValue())))
                ));
            }
            return null;
        }, null);
    }

    @Override
    public void updateCashInLimitation(WalletAccountEntity walletAccountEntity, long amount) throws InternalServiceException {

        log.info("start update cashInLimitation for mobile {} with amount {}", walletAccountEntity.getWalletEntity().getNationalCode(), amount);

        String key = walletAccountEntity.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {
            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);
            String walletLimitationId = walletAccountEntity.getId() + "-" + currentDate;

            WalletCashInLimitationRedis walletCashInLimitationRedis = findById(walletLimitationId);

            if (walletCashInLimitationRedis == null) {
                log.info("It is the first time that creating walletLimitation for walletAccount({}) for cashIn in date: {}", walletAccountEntity.getAccountNumber(), currentDate);
                walletCashInLimitationRedis = new WalletCashInLimitationRedis();
                walletCashInLimitationRedis.setId(walletLimitationId);
                walletCashInLimitationRedis.setCashInDailyCount(1);
                walletCashInLimitationRedis.setCashInDailyAmount(amount);
            } else {
                walletCashInLimitationRedis.setCashInDailyCount(walletCashInLimitationRedis.getCashInDailyCount() + 1);
                walletCashInLimitationRedis.setCashInDailyAmount(walletCashInLimitationRedis.getCashInDailyAmount() + amount);
            }
            save(walletCashInLimitationRedis);
            return null;
        }, null);
        log.info("finish update cashInLimitation for mobile {} with amount {}", walletAccountEntity.getWalletEntity().getNationalCode(), amount);

    }

    WalletCashInLimitationRedis findById(String id) {
        return walletCashInLimitationRepository.findById(id).orElse(null);
    }


}
