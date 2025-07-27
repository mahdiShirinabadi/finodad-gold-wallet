package com.melli.wallet.service.impl;

import com.melli.wallet.domain.dto.AggregationCashInDTO;
import com.melli.wallet.domain.dto.AggregationCashOutDTO;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.WalletCashInLimitationRepository;
import com.melli.wallet.domain.master.persistence.WalletCashOutLimitationRepository;
import com.melli.wallet.domain.master.persistence.WalletPhysicalCashOutLimitationRepository;
import com.melli.wallet.domain.redis.WalletCashInLimitationRedis;
import com.melli.wallet.domain.redis.WalletCashOutLimitationRedis;
import com.melli.wallet.domain.redis.WalletPhysicalCashOutLimitationRedis;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.util.Utility;
import com.melli.wallet.util.date.DateUtils;
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
    private final WalletCashOutLimitationRepository walletCashOutLimitationRepository;
    private final WalletPhysicalCashOutLimitationRepository walletPhysicalCashOutLimitationRepository;
    private final WalletAccountService walletAccountService;
    private final RedisLockService redisLockService;
    private final RequestService requestService;

    private void saveCashIn(WalletCashInLimitationRedis walletCashInLimitationRedis) {
        walletCashInLimitationRepository.save(walletCashInLimitationRedis);
    }

    private void saveCashOut(WalletCashOutLimitationRedis walletCashOutLimitationRedis) {
        walletCashOutLimitationRepository.save(walletCashOutLimitationRedis);
    }

    private void savePhysicalCashOut(WalletPhysicalCashOutLimitationRedis walletCashOutLimitationRedis) {
        walletPhysicalCashOutLimitationRepository.save(walletCashOutLimitationRedis);
    }

    @Override
    public void checkCashInLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount) throws InternalServiceException {
        log.info("checking CashInLimitation for nationalCode({}) ...", wallet.getNationalCode());

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        Boolean enableCashIn = Boolean.parseBoolean(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.ENABLE_CASH_IN, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));

        if (Boolean.FALSE.equals(enableCashIn)) {
            log.error("checkCashInLimitation: cash in for channel {}, walletAccountCurrencyEntity ({}) and walletAccountTypeEntity ({}) walletTypeEntity ({}) is not permission !!!", channel, walletAccountCurrencyEntity.getName(), walletAccountTypeEntity.getName(), walletTypeEntity.getName());
            StringBuilder st = new StringBuilder();
            st.append("account (").append( walletAccount.getAccountNumber()).append(") dont permission to cashIn");
            throw new InternalServiceException(st.toString(), StatusService.ACCOUNT_DONT_PERMISSION_FOR_CASH_IN, HttpStatus.OK);
        }

        BigDecimal minAmount = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MIN_AMOUNT_CASH_IN, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxAmount = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_AMOUNT_CASH_IN, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxBalance = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_WALLET_BALANCE, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxCashInDaily = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_WALLET_AMOUNT_DAILY_CASH_IN, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        String key = walletAccount.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (amount.compareTo(maxAmount)> 0) {
                log.error("checkCashInLimitation: CashIn's amount({}) for wallet({}), is bigger than maxCashIn({}) !!!", amount, wallet.getNationalCode(), maxAmount);
                throw new InternalServiceException("CashIn's amount is bigger than minCashIn", StatusService.AMOUNT_BIGGER_THAN_MAX, HttpStatus.OK,
                        Map.ofEntries(
                                entry("1", Utility.addComma(amount.longValue())),
                                entry("2", Utility.addComma(maxAmount.longValue()))
                        )
                );
            }

            if (amount.compareTo(minAmount) < 0) {
                log.error("checkCashInLimitation: CashIn's amount({}) for wallet({}), is less than minCashIn({}) !!!", amount, wallet.getNationalCode(), minAmount);
                throw new InternalServiceException("CashIn's amount is less than minCashIn", StatusService.AMOUNT_LESS_THAN_MIN, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(amount.longValue())),
                        entry("2", Utility.addComma(minAmount.longValue()))
                ));
            }

            BigDecimal balance = walletAccountService.getBalance(walletAccount.getId());

            if (balance.add(amount).compareTo(maxBalance)> 0) {
                log.error("cashIn's amount({}) for wallet({}) with balance ({}), is more than maxBalance({}) !!!", amount, wallet.getNationalCode(), balance, maxBalance.longValue());
                throw new InternalServiceException("cashIn's amount is more than maxBalance", StatusService.BALANCE_MORE_THAN_STANDARD, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(balance.add(amount).longValue())),
                        entry("2", Utility.addComma(maxBalance.longValue()))
                ));
            }

            log.info("Start checking daily cashIn limitation for wallet({}) ...", wallet.getNationalCode());

            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);
            String walletLimitationId = walletAccount.getId() + "-" + currentDate;

            WalletCashInLimitationRedis walletCashInLimitationRedis = findCashInById(walletLimitationId);

            if (walletCashInLimitationRedis == null) {
                log.info("wallet with nationalCode ({}) dont have any cashIn in date({})", wallet.getNationalCode(), currentDate);
                log.info("walletCashInLimitationRedis is null for walletAccount ({}) and nationalCode ({}) and start read from database", walletAccount.getAccountNumber(), wallet.getNationalCode());
                walletCashInLimitationRedis = new WalletCashInLimitationRedis();
                AggregationCashInDTO aggregationPurchaseDTO = requestService.findSumAmountCashInBetweenDate(new long[]{walletAccount.getId()}, new Date(), new Date());
                log.info("walletDailyBuyLimitationRedis read from database for walletAccount ({}), nationalCode ({}), sumAmount ({}), count ({}) for date ({})", walletAccount.getAccountNumber(), wallet.getNationalCode(), aggregationPurchaseDTO.getSumPrice(), aggregationPurchaseDTO.getCountRecord(), new Date());
                walletCashInLimitationRedis.setId(walletLimitationId);
                walletCashInLimitationRedis.setCashInDailyAmount(new BigDecimal(aggregationPurchaseDTO.getSumPrice()));
                walletCashInLimitationRedis.setCashInDailyCount(Long.parseLong(aggregationPurchaseDTO.getCountRecord()));
                walletCashInLimitationRepository.save(walletCashInLimitationRedis);
            }

            log.info("SumCashInAmount for wallet({}) in date: ({}) is: {}", wallet.getNationalCode(), currentDate, walletCashInLimitationRedis.getCashInDailyAmount());

            if ((walletCashInLimitationRedis.getCashInDailyAmount().add(amount).compareTo(BigDecimal.valueOf(maxCashInDaily.longValue()))) > 0) {
                log.error("wallet({}) on channel ({}) , exceeded amount limitation in cashIn!!! SumCashInAmount is: {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletCashInLimitationRedis.getCashInDailyAmount());
                throw new InternalServiceException("wallet exceeded cashIn the limitation !!!", StatusService.WALLET_EXCEEDED_AMOUNT_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletCashInLimitationRedis.getCashInDailyAmount().add(amount).longValue())),
                        entry("2", Utility.addComma((maxCashInDaily.longValue())))
                ));
            }
            return null;
        }, null);
    }

    @Override
    public void checkCashOutLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal amount, WalletAccountEntity walletAccount) throws InternalServiceException {
        log.info("checking checkCashOutLimitation for nationalCode({}) ...", wallet.getNationalCode());

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        Boolean enableCashOut = Boolean.parseBoolean(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.ENABLE_CASH_OUT, walletLevelEntity, walletAccountTypeEntity,
                walletAccountCurrencyEntity, walletTypeEntity));

        if (Boolean.FALSE.equals(enableCashOut)) {
            log.error("checkCashOutLimitation: cash out for channel {}, walletAccountCurrencyEntity ({}) and walletAccountTypeEntity ({}) walletTypeEntity ({}) is not permission !!!", channel,
                    walletAccount.getWalletAccountCurrencyEntity().getName(),  walletAccount.getWalletAccountTypeEntity().getName(), walletAccount.getWalletEntity().getWalletTypeEntity().getName());
            throw new InternalServiceException("account (" + walletAccount.getAccountNumber() + ") dont permission to cashIn", StatusService.ACCOUNT_DONT_PERMISSION_FOR_CASH_OUT, HttpStatus.OK);
        }

        BigDecimal minAmount = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MIN_AMOUNT_CASH_OUT, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxAmount = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_AMOUNT_CASH_OUT, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxCashInDaily = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_WALLET_AMOUNT_DAILY_CASH_OUT, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        String key = walletAccount.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (amount.compareTo(maxAmount)> 0) {
                log.error("checkCashOutLimitation: CashOut's amount({}) for wallet({}), is bigger than maxCashOut({}) !!!", amount, wallet.getNationalCode(), maxAmount);
                throw new InternalServiceException("CashOut's amount is bigger than minCashIn", StatusService.AMOUNT_BIGGER_THAN_MAX, HttpStatus.OK,
                        Map.ofEntries(
                                entry("1", Utility.addComma(amount.longValue())),
                                entry("2", Utility.addComma(maxAmount.longValue()))
                        )
                );
            }

            if (amount.compareTo(minAmount) < 0) {
                log.error("checkCashOutLimitation: CashOut's amount({}) for wallet({}), is less than maxCashOut({}) !!!", amount, wallet.getNationalCode(), minAmount);
                throw new InternalServiceException("CashOut's amount is less than minCashIn", StatusService.AMOUNT_LESS_THAN_MIN, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(amount.longValue())),
                        entry("2", Utility.addComma(minAmount.longValue()))
                ));
            }

            log.info("Start checking daily cashOut limitation for wallet({}) ...", wallet.getNationalCode());

            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);
            String walletLimitationId = walletAccount.getId() + "-" + currentDate;

            WalletCashOutLimitationRedis walletCashOutLimitationRedis = findCashOutById(walletLimitationId);

            if (walletCashOutLimitationRedis == null) {
                log.info("wallet with nationalCode ({}) dont have any cashOut in date({})", wallet.getNationalCode(), currentDate);
                walletCashOutLimitationRedis = new WalletCashOutLimitationRedis();
                AggregationCashOutDTO sumAmountCashOutBetweenDate = requestService.findSumAmountCashOutBetweenDate(new long[]{walletAccount.getId()}, new Date(), new Date());
                log.info("walletCashOutLimitationRedis read from database for walletAccount ({}), nationalCode ({}), sumAmount ({}), count ({}) for date ({})", walletAccount.getAccountNumber(),
                        wallet.getNationalCode(), sumAmountCashOutBetweenDate.getSumPrice(), sumAmountCashOutBetweenDate.getCountRecord(), new Date());
                walletCashOutLimitationRedis.setId(walletLimitationId);
                walletCashOutLimitationRedis.setCashOutDailyAmount(new BigDecimal(sumAmountCashOutBetweenDate.getSumPrice()));
                walletCashOutLimitationRedis.setCashOutDailyCount(Long.parseLong(sumAmountCashOutBetweenDate.getCountRecord()));
                walletCashOutLimitationRepository.save(walletCashOutLimitationRedis);
            }

            log.info("SumCashOutAmount for wallet({}) in date: ({}) is: {}", wallet.getNationalCode(), currentDate, walletCashOutLimitationRedis.getCashOutDailyAmount());

            if ((walletCashOutLimitationRedis.getCashOutDailyAmount().add(amount).compareTo(BigDecimal.valueOf(maxCashInDaily.longValue()))) > 0) {
                log.error("wallet({}) on channel ({}) , exceeded amount limitation in cashOut!!! SumCashInAmount is: {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletCashOutLimitationRedis.getCashOutDailyAmount());
                throw new InternalServiceException("wallet exceeded the limitation !!!", StatusService.CASHOUT_EXCEEDED_AMOUNT_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletCashOutLimitationRedis.getCashOutDailyAmount().add(amount).longValue())),
                        entry("2", Utility.addComma((maxCashInDaily.longValue())))
                ));
            }
            return null;
        }, null);
    }

    @Override
    public void checkPhysicalCashOutLimitation(ChannelEntity channel, WalletEntity wallet, BigDecimal quantity, WalletAccountEntity walletAccount) throws InternalServiceException {
        log.info("checking checkPhysicalCashOutLimitation for nationalCode({}) ...", wallet.getNationalCode());

        WalletLevelEntity walletLevelEntity = wallet.getWalletLevelEntity();
        WalletAccountTypeEntity walletAccountTypeEntity = walletAccount.getWalletAccountTypeEntity();
        WalletAccountCurrencyEntity walletAccountCurrencyEntity = walletAccount.getWalletAccountCurrencyEntity();
        WalletTypeEntity walletTypeEntity = wallet.getWalletTypeEntity();

        Boolean enableCashOut = Boolean.parseBoolean(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.ENABLE_PHYSICAL_CASH_OUT, walletLevelEntity, walletAccountTypeEntity,
                walletAccountCurrencyEntity, walletTypeEntity));

        if (Boolean.FALSE.equals(enableCashOut)) {
            log.error("checkPhysicalCashOutLimitation: physical cash out for channel {}, walletAccountCurrencyEntity ({}) and walletAccountTypeEntity ({}) walletTypeEntity ({}) is not permission !!!", channel,
                    walletAccount.getWalletAccountCurrencyEntity().getName(),  walletAccount.getWalletAccountTypeEntity().getName(), walletAccount.getWalletEntity().getWalletTypeEntity().getName());
            throw new InternalServiceException("account (" + walletAccount.getAccountNumber() + ") dont permission to cashIn", StatusService.ACCOUNT_DONT_PERMISSION_FOR_PHYSICAL_CASH_OUT, HttpStatus.OK);
        }

        BigDecimal minAmount = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MIN_QUANTITY_PHYSICAL_CASH_OUT, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxAmount = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_QUANTITY_PHYSICAL_CASH_OUT, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));
        BigDecimal maxCashInDaily = new BigDecimal(limitationGeneralCustomService.getSetting(channel, LimitationGeneralService.MAX_WALLET_QUANTITY_DAILY_PHYSICAL_CASH_OUT, walletLevelEntity, walletAccountTypeEntity, walletAccountCurrencyEntity, walletTypeEntity));


        String key = walletAccount.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {

            if (quantity.compareTo(minAmount) < 0) {
                log.error("checkPhysicalCashOutLimitation: PhysicalCashOut's quantity({}) for wallet({}), is less than minPhysicalCashOut({}) !!!", quantity, wallet.getNationalCode(), minAmount);
                throw new InternalServiceException("CashOut's amount is less than minCashOut", StatusService.QUANTITY_LESS_THAN_MIN, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(quantity.longValue())),
                        entry("2", Utility.addComma(minAmount.longValue()))
                ));
            }

            if (quantity.compareTo(maxAmount)> 0) {
                log.error("checkPhysicalCashOutLimitation: CashOut's amount({}) for wallet({}), is bigger than maxCashOut({}) !!!", quantity, wallet.getNationalCode(), maxAmount);
                throw new InternalServiceException("CashOut's amount is bigger than maxCashOut", StatusService.QUANTITY_BIGGER_THAN_MAX, HttpStatus.OK,
                        Map.ofEntries(
                                entry("1", Utility.addComma(quantity.longValue())),
                                entry("2", Utility.addComma(maxAmount.longValue()))
                        )
                );
            }



            log.info("Start checking daily cashOut limitation for wallet({}) ...", wallet.getNationalCode());

            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);
            String walletLimitationId = walletAccount.getId() + "-" + currentDate;

            WalletPhysicalCashOutLimitationRedis walletPhysicalCashOutLimitationRedis = findPhysicalCashOutById(walletLimitationId);

            if (walletPhysicalCashOutLimitationRedis == null) {
                log.info("wallet with nationalCode ({}) dont have any physical cashOut in date({})", wallet.getNationalCode(), currentDate);
                walletPhysicalCashOutLimitationRedis = new WalletPhysicalCashOutLimitationRedis();
                AggregationCashOutDTO sumAmountCashOutBetweenDate = requestService.findSumAmountPhysicalCashOutBetweenDate(new long[]{walletAccount.getId()}, new Date(), new Date());
                log.info("walletPhysicalCashOutLimitationRedis read from database for walletAccount ({}), nationalCode ({}), sumAmount ({}), count ({}) for date ({})", walletAccount.getAccountNumber(),
                        wallet.getNationalCode(), sumAmountCashOutBetweenDate.getSumPrice(), sumAmountCashOutBetweenDate.getCountRecord(), new Date());
                walletPhysicalCashOutLimitationRedis.setId(walletLimitationId);
                walletPhysicalCashOutLimitationRedis.setCashOutDailyAmount(new BigDecimal(sumAmountCashOutBetweenDate.getSumPrice()));
                walletPhysicalCashOutLimitationRedis.setCashOutDailyCount(Long.parseLong(sumAmountCashOutBetweenDate.getCountRecord()));
                walletPhysicalCashOutLimitationRepository.save(walletPhysicalCashOutLimitationRedis);
            }

            log.info("SumPhysicalCashOutAmount for wallet({}) in date: ({}) is: {}", wallet.getNationalCode(), currentDate, walletPhysicalCashOutLimitationRedis.getCashOutDailyAmount());

            if ((walletPhysicalCashOutLimitationRedis.getCashOutDailyAmount().add(quantity).compareTo(BigDecimal.valueOf(maxCashInDaily.longValue()))) > 0) {
                log.error("wallet({}) on channel ({}) , exceeded amount limitation in cashOut!!! SumPhysicalCashOutAmount is: {}", wallet.getNationalCode(), wallet.getOwner().getId(), walletPhysicalCashOutLimitationRedis.getCashOutDailyAmount());
                throw new InternalServiceException("wallet exceeded the limitation !!!", StatusService.PHYSICAL_CASHOUT_EXCEEDED_AMOUNT_DAILY_LIMITATION, HttpStatus.OK, Map.ofEntries(
                        entry("1", Utility.addComma(walletPhysicalCashOutLimitationRedis.getCashOutDailyAmount().add(quantity).longValue())),
                        entry("2", Utility.addComma((maxCashInDaily.longValue())))
                ));
            }
            return null;
        }, null);
    }

    @Override
    public void updatePhysicalCashOutLimitation(WalletAccountEntity walletAccountEntity, BigDecimal quantity) throws InternalServiceException {
        log.info("start update updatePhysicalCashOutLimitation for mobile {} with amount {}", walletAccountEntity.getWalletEntity().getNationalCode(), quantity);

        String key = walletAccountEntity.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {
            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);
            String walletLimitationId = walletAccountEntity.getId() + "-" + currentDate;

            WalletPhysicalCashOutLimitationRedis walletPhysicalCashOutLimitationRedis = findPhysicalCashOutById(walletLimitationId);

            if (walletPhysicalCashOutLimitationRedis == null) {
                log.info("It is the first time that creating updatePhysicalCashOutLimitation for walletAccount({}) for cashIn in date: {}", walletAccountEntity.getAccountNumber(), currentDate);
                walletPhysicalCashOutLimitationRedis = new WalletPhysicalCashOutLimitationRedis();
                walletPhysicalCashOutLimitationRedis.setId(walletLimitationId);
                walletPhysicalCashOutLimitationRedis.setCashOutDailyCount(1);
                walletPhysicalCashOutLimitationRedis.setCashOutDailyAmount(quantity);
            } else {
                walletPhysicalCashOutLimitationRedis.setCashOutDailyCount(walletPhysicalCashOutLimitationRedis.getCashOutDailyCount() + 1);
                walletPhysicalCashOutLimitationRedis.setCashOutDailyAmount(walletPhysicalCashOutLimitationRedis.getCashOutDailyAmount().add(quantity));
            }
            savePhysicalCashOut(walletPhysicalCashOutLimitationRedis);
            return null;
        }, null);
        log.info("finish updatePhysicalCashOutLimitation for mobile {} with amount {}", walletAccountEntity.getWalletEntity().getNationalCode(), quantity);

    }

    @Override
    public void updateCashOutLimitation(WalletAccountEntity walletAccountEntity, BigDecimal amount) throws InternalServiceException {
        log.info("start update updateCashOutLimitation for mobile {} with amount {}", walletAccountEntity.getWalletEntity().getNationalCode(), amount);

        String key = walletAccountEntity.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {
            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);
            String walletLimitationId = walletAccountEntity.getId() + "-" + currentDate;

            WalletCashOutLimitationRedis walletCashOutLimitationRedis = findCashOutById(walletLimitationId);

            if (walletCashOutLimitationRedis == null) {
                log.info("It is the first time that creating walletLimitation for walletAccount({}) for cashIn in date: {}", walletAccountEntity.getAccountNumber(), currentDate);
                walletCashOutLimitationRedis = new WalletCashOutLimitationRedis();
                walletCashOutLimitationRedis.setId(walletLimitationId);
                walletCashOutLimitationRedis.setCashOutDailyCount(1);
                walletCashOutLimitationRedis.setCashOutDailyAmount(amount);
            } else {
                walletCashOutLimitationRedis.setCashOutDailyCount(walletCashOutLimitationRedis.getCashOutDailyCount() + 1);
                walletCashOutLimitationRedis.setCashOutDailyAmount(walletCashOutLimitationRedis.getCashOutDailyAmount().add(amount));
            }
            saveCashOut(walletCashOutLimitationRedis);
            return null;
        }, null);
        log.info("finish update cashInLimitation for mobile {} with amount {}", walletAccountEntity.getWalletEntity().getNationalCode(), amount);
    }

    @Override
    @Async("threadPoolExecutor")
    public void updateCashInLimitation(WalletAccountEntity walletAccountEntity, BigDecimal amount) throws InternalServiceException {

        log.info("start update cashInLimitation for mobile {} with amount {}", walletAccountEntity.getWalletEntity().getNationalCode(), amount);

        String key = walletAccountEntity.getAccountNumber();

        redisLockService.runAfterLock(key, this.getClass(), () -> {
            String currentDate = DateUtils.getLocaleDate(DateUtils.ENGLISH_LOCALE, new Date(), "MMdd", false);
            String walletLimitationId = walletAccountEntity.getId() + "-" + currentDate;

            WalletCashInLimitationRedis walletCashInLimitationRedis = findCashInById(walletLimitationId);

            if (walletCashInLimitationRedis == null) {
                log.info("It is the first time that creating walletLimitation for walletAccount({}) for cashIn in date: {}", walletAccountEntity.getAccountNumber(), currentDate);
                walletCashInLimitationRedis = new WalletCashInLimitationRedis();
                walletCashInLimitationRedis.setId(walletLimitationId);
                walletCashInLimitationRedis.setCashInDailyCount(1);
                walletCashInLimitationRedis.setCashInDailyAmount(amount);
            } else {
                walletCashInLimitationRedis.setCashInDailyCount(walletCashInLimitationRedis.getCashInDailyCount() + 1);
                walletCashInLimitationRedis.setCashInDailyAmount(walletCashInLimitationRedis.getCashInDailyAmount().add(amount));
            }
            saveCashIn(walletCashInLimitationRedis);
            return null;
        }, null);
        log.info("finish update cashInLimitation for mobile {} with amount {}", walletAccountEntity.getWalletEntity().getNationalCode(), amount);

    }

    WalletCashInLimitationRedis findCashInById(String id) {
        return walletCashInLimitationRepository.findById(id).orElse(null);
    }

    WalletCashOutLimitationRedis findCashOutById(String id) {
        return walletCashOutLimitationRepository.findById(id).orElse(null);
    }

    WalletPhysicalCashOutLimitationRedis findPhysicalCashOutById(String id) {
        return walletPhysicalCashOutLimitationRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteAll() {
        log.info("delete all cash in limitation");
        walletCashInLimitationRepository.deleteAll();
        log.info("delete all cash out limitation");
        walletCashOutLimitationRepository.deleteAll();
        log.info("delete all physical cash out limitation");
        walletPhysicalCashOutLimitationRepository.deleteAll();
    }


}
