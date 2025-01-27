package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.*;
import com.melli.hub.domain.master.persistence.WalletLimitationRepository;
import com.melli.hub.domain.master.persistence.WalletMonthlyLimitationRepository;
import com.melli.hub.domain.redis.WalletLimitationRedis;
import com.melli.hub.domain.redis.WalletMonthlyLimitationRedis;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.*;
import com.melli.hub.util.date.DateUtils;
import com.melli.hub.utils.Helper;
import com.melli.hub.utils.RedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Class Name: WalletLimitationServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/26/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class WalletLimitationServiceImplementation implements WalletLimitationService {

    private final SettingGeneralService settingGeneralService;
    private final SettingGeneralCustomService settingGeneralCustomService;
    private final WalletLimitationRepository walletLimitationRepository;
    private final WalletMonthlyLimitationRepository walletMonthlyLimitationRepository;
    private final WalletAccountService walletAccountService;
    private final RedisLockService redisLockService;
    private final Helper helper;

    private WalletLimitationRedis findById(String id) {
        return walletLimitationRepository.findById(id).orElse(null);
    }

    private WalletMonthlyLimitationRedis findMonthlyById(String id) {
        return walletMonthlyLimitationRepository.findById(id).orElse(null);
    }

    @Override
    public void save(WalletLimitationRedis walletLimitation) {
        walletLimitationRepository.save(walletLimitation);
    }

    @Override
    public void saveMonthly(WalletMonthlyLimitationRedis walletMonthlyLimitation) {
        walletMonthlyLimitationRepository.save(walletMonthlyLimitation);
    }

    @Override
    public void checkPurchaseLimitation(ChannelEntity channel, WalletEntity wallet, long amount, WalletAccountEntity walletAccount, MerchantEntity merchant) throws InternalServiceException {

    }

    @Override
    public void checkCashInLimitation(ChannelEntity channel, WalletEntity wallet, long amount, WalletAccountEntity walletAccount) throws InternalServiceException {
        log.info("checking CashInLimitation for nationalCode({}) ...", wallet.getNationalCode());

        WalletLevelEntity walletLevelEntity = wallet.getWalletLEvelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        Boolean enableCashIn = Boolean.parseBoolean(settingGeneralCustomService.getSetting(channel, SettingGeneralService.ENABLE_CASH_IN, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));

        if (Boolean.FALSE.equals(enableCashIn)) {
            log.error("checkCashInLimitation: cash in for channel {}, walletAccountCurrencyEntity ({}) and walletAccountTypeEntity ({}) walletTypeEntity ({}) is not permission !!!", channel, walletAccountCurrencyEntity.getName(), walletAccountTypeEntity.getName(), walletTypeEntity.getName());
            throw new InternalServiceException("account (" + walletAccount.getAccountNumber() + ") dont permission to cashIn", StatusService.ACCOUNT_DONT_PERMISSION_FOR_CASH_IN, HttpStatus.OK);
        }

        BigDecimal minAmount = new BigDecimal(settingGeneralCustomService.getSetting(channel, SettingGeneralService.MIN_AMOUNT_CASH_IN, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxAmount = new BigDecimal(settingGeneralCustomService.getSetting(channel, SettingGeneralService.MAX_AMOUNT_CASH_IN, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));

        BigDecimal maxBalance;
        BigDecimal maxCashInDaily;


        log.info("checking CashInLimitation in second level for wallet({}) ...", wallet.getMobile());
        maxBalance = new BigDecimal(settingGeneralCustomService.getSetting(channel, SettingGeneralService.MAX_WALLET_BALANCE, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        maxCashInDaily = maxBalance;


        String key = walletAccount.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if(amount > maxAmount.longValue()){
                log.error("checkCashInLimitation: CashIn's amount({}) for wallet({}), is bigger than maxCashIn({}) !!!", amount, wallet.getMobile(), maxAmount);
                throw new InternalServiceException("CashIn's amount is bigger than minCashIn", StatusService.AMOUNT_BIGGER_THAN_MAX, HttpStatus.OK);
            }

            if (amount < minAmount.longValue()) {
                log.error("checkCashInLimitation: CashIn's amount({}) for wallet({}), is less than minCashIn({}) !!!", amount, wallet.getMobile(), minAmount);
                throw new InternalServiceException("CashIn's amount is less than minCashIn", StatusService.AMOUNT_LESS_THAN_MIN, HttpStatus.OK);
            }

            long balance = walletAccountService.getBalance(walletAccount.getId());

            if (balance + amount > maxBalance.longValue()) {
                log.error("cashIn's amount({}) for wallet({}) with balance ({}), is more than maxBalance({}) !!!", amount, wallet.getMobile(), balance, maxBalance.longValue());
                throw new InternalServiceException("cashIn's amount is more than maxBalance", StatusService.BALANCE_MORE_THAN_STANDARD, HttpStatus.OK);
            }

            log.info("Start checking daily cashIn limitation for wallet({}) ...", wallet.getMobile());

            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);
            String walletLimitationId = walletAccount.getId() + "-" + currentDate;

            WalletLimitationRedis walletLimitation = findById(walletLimitationId);

            if (walletLimitation == null) {
                return null;
            }

            log.info("SumCashInAmount for wallet({}) in date: ({}) is: {}", wallet.getMobile(), currentDate, walletLimitation.getCashInDailyAmount());

            if ((walletLimitation.getCashInDailyAmount() + amount) > maxCashInDaily.longValue()) {
                log.error("wallet({}) on channel ({}) , exceeded amount limitation in cashIn!!! SumCashInAmount is: {}", wallet.getMobile(), wallet.getOwner().getId(), walletLimitation.getCashInDailyAmount());
                throw new InternalServiceException("wallet exceeded the limitation !!!", StatusService.WALLET_EXCEEDED_AMOUNT_LIMITATION, HttpStatus.OK);
            }
            return null;
        }, null);
    }

    @Override
    public void checkCashOutLimitation(ChannelEntity channel, WalletEntity wallet, long amount, WalletAccountEntity walletAccount) throws InternalServiceException {

    }

    @Override
    public void checkP2PLimitation(ChannelEntity channel, WalletEntity wallet, WalletAccountEntity sourceWalletAccount, WalletAccountEntity destinationWalletAccount, long amount) throws InternalServiceException {

    }

    @Override
    public void checkBlockLimitation(ChannelEntity channel, WalletAccountEntity walletAccount, long amount) throws InternalServiceException {

    }

    @Override
    public void updatePurchaseLimitation(WalletAccountEntity wallet, long amount, MerchantEntity merchantEntity) {

    }

    @Override
    public void updateCashInLimitation(WalletAccountEntity walletAccountEntity, long amount) throws InternalServiceException {

        log.info("start update cashInLimitation for mobile {} with amount {}", walletAccountEntity.getWalletEntity().getNationalCode(), amount);

        String key = walletAccountEntity.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {
            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);
            String walletLimitationId = walletAccountEntity.getId() + "-" + currentDate;

            WalletLimitationRedis walletLimitation = findById(walletLimitationId);

            if (walletLimitation == null) {
                log.info("It is the first time that creating walletLimitation for walletAccount({}) for cashIn in date: {}", walletAccountEntity.getAccountNumber(), currentDate);
                walletLimitation = new WalletLimitationRedis();
                walletLimitation.setId(walletLimitationId);
                walletLimitation.setCashInDailyCount(1);
                walletLimitation.setCashInDailyAmount(amount);

            } else {
                walletLimitation.setCashInDailyCount(walletLimitation.getCashInDailyCount() + 1);
                walletLimitation.setCashInDailyAmount(walletLimitation.getCashInDailyAmount() + amount);
            }
            save(walletLimitation);
            return null;
        }, null);
        log.info("finish update cashInLimitation for mobile {} with amount {}", walletAccountEntity.getWalletEntity().getNationalCode(), amount);

    }

    @Override
    public void updateCashOutLimitation(WalletAccountEntity wallet, long amount) throws InternalServiceException {

    }

    @Override
    public void updateP2PLimitation(WalletAccountEntity walletAccount, long amount) throws InternalServiceException {

    }

    @Override
    public void updateBlockLimitation(WalletAccountEntity walletAccount, long amount) throws InternalServiceException {

    }

    @Override
    public void updateUnBlockLimitation(WalletAccountEntity walletAccount, long amount) throws InternalServiceException {

    }
}
