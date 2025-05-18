package com.melli.wallet.service.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.dto.BalanceObjectDTO;
import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.WalletAccountRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.*;
import com.melli.wallet.util.Validator;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
@Log4j2
@RequiredArgsConstructor
@CacheConfig(cacheNames = ConstantRedisName.WALLET_ACCOUNT_NAME_CACHE)
public class WalletAccountServiceImplementation implements WalletAccountService {

    private final WalletAccountRepository walletAccountRepository;
    private final WalletAccountCurrencyService walletAccountCurrencyService;
    private final WalletAccountTypeService walletAccountTypeService;


    @Override
    public WalletAccountEntity findByAccountNumberAndEndTimeIsNull(String accountNumber) {
        return walletAccountRepository.findByAccountNumberAndEndTimeIsNull(accountNumber);
    }

    @Override
    public WalletAccountEntity findByAccountNumber(String accountNumber) {
        return walletAccountRepository.findByAccountNumber(accountNumber);
    }


    @Override
    public List<WalletAccountEntity> findByWallet(WalletEntity wallet) {
        return walletAccountRepository.findByWalletEntity(wallet);
    }


    @Override
    public List<WalletAccountEntity> findByWallet(WalletEntity wallet, Pageable pageable) {
        return walletAccountRepository.findByWalletEntity(wallet, pageable);
    }


    @Override
    public WalletAccountEntity findByWalletAndAccount(WalletEntity wallet, String account) {
        return walletAccountRepository.findByWalletEntityAndAccountNumberAndEndTimeIsNull(wallet, account);
    }

    @Override
    public WalletAccountEntity findByWalletAndWalletAccountCurrencyId(WalletEntity walletEntity, long walletAccountCurrencyId) {
        return walletAccountRepository.findTopByWalletEntityAndWalletAccountCurrencyEntityId(walletEntity, walletAccountCurrencyId);
    }

    @Override
    public BigDecimal getBalance(long walletAccountId) {
        return walletAccountRepository.getBalance(walletAccountId);
    }

    @Transactional
    @Override
    public void increaseBalance(long walletAccountId, BigDecimal amount) {
        walletAccountRepository.increaseBalance(walletAccountId, amount);
    }

    @Transactional
    @Override
    public int decreaseBalance(long walletAccountId, BigDecimal amount) {
        return walletAccountRepository.decreaseBalance(walletAccountId, amount);
    }

    @Transactional
    @Override
    public void blockAmount(long walletAccountId, long amount) {
        walletAccountRepository.blockAmount(walletAccountId, amount);
    }

    @Transactional
    @Override
    public void unblockAmount(long walletAccountId, long amount) {
        walletAccountRepository.unblockAmount(walletAccountId, amount);
    }

    @Transactional
    @Override
    @CacheEvict(key = "{#walletAccount?.accountNumber}")
    public void save(WalletAccountEntity walletAccount) {
        log.info("start save walletAccount with info ===> ({})", walletAccount);
        walletAccountRepository.save(walletAccount);
        log.info("finish save walletAccount with info ===> ({})", walletAccount);
    }

    @Override
    @CacheEvict(key = "{#accountNumber}")
    public void clearCache(String accountNumber) {
        log.info("start delete walletAccount with accountNumber ==> ({})", accountNumber);
    }

    @Override
    public void createAccount(List<String> walletAccountCurrencyList, WalletEntity wallet, List<String> walletAccountTypeList, ChannelEntity channel) throws InternalServiceException {

        //inquiry currency and accountType
        List<WalletAccountCurrencyEntity> walletAccountCurrencyEntityList = walletAccountCurrencyService.getAll();
        List<WalletAccountTypeEntity> walletAccountTypeEntityList = walletAccountTypeService.getAll();

        for(String walletAccountCurrencyName : walletAccountCurrencyList) {
            walletAccountCurrencyEntityList.stream().filter(walletAccountCurrencyEntity -> walletAccountCurrencyEntity.getName().equals(walletAccountCurrencyName)).findFirst().orElseThrow(()->{
                log.error("walletAccountCurrency with name ({}) is not found", walletAccountCurrencyName);
                return new InternalServiceException("walletAccountCurrency not found",StatusService.WALLET_ACCOUNT_CURRENCY_NOT_FOUND, HttpStatus.OK);
            });
        }

        for(String walletAccountTypeName : walletAccountTypeList) {
            walletAccountTypeEntityList.stream().filter(walletAccountTypeEntity -> walletAccountTypeEntity.getName().equals(walletAccountTypeName)).findFirst().orElseThrow(()->{
                log.error("walletAccountType with name ({}) is not found", walletAccountTypeName);
                return new InternalServiceException("walletAccountCurrency not found",StatusService.WALLET_ACCOUNT_TYPE_NOT_FOUND, HttpStatus.OK);
            });
        }


        for(String walletAccountCurrencyName : walletAccountCurrencyList){
            Optional<WalletAccountCurrencyEntity> walletAccountCurrencyEntity = walletAccountCurrencyEntityList.stream().filter(x -> x.getName().equals(walletAccountCurrencyName)).findFirst();
            for(String walletAccountTypeName : walletAccountTypeList){
                Optional<WalletAccountTypeEntity> walletAccountTypeEntity = walletAccountTypeEntityList.stream().filter(x -> x.getName().equals(walletAccountTypeName)).findFirst();
                WalletAccountEntity walletAccountEntity = new WalletAccountEntity();
                walletAccountEntity.setWalletEntity(wallet);
                //we checked in above!!
                walletAccountEntity.setWalletAccountTypeEntity(walletAccountTypeEntity.get());
                walletAccountEntity.setWalletAccountCurrencyEntity(walletAccountCurrencyEntity.get());
                walletAccountEntity.setStatus(WalletStatusEnum.ACTIVE);
                walletAccountEntity.setCreatedAt(new Date());
                walletAccountEntity.setCreatedBy(channel.getUsername());
                walletAccountRepository.save(walletAccountEntity);
                walletAccountEntity.setAccountNumber(Validator.padWithZero(String.valueOf(walletAccountEntity.getId()), Helper.WALLET_ACCOUNT_LENGTH));
                walletAccountRepository.save(walletAccountEntity);
            }
        }
    }


    private int generateCheckDigit(String str) {
        int[] ints = new int[str.length()];
        for (int i = 0; i < str.length(); i++) {
            ints[i] = Integer.parseInt(str.substring(i, i + 1));
        }
        for (int i = ints.length - 1; i >= 0; i = i - 2) {
            int j = ints[i];
            j = j * 2;
            if (j > 9) {
                j = j % 10 + 1;
            }
            ints[i] = j;
        }
        int sum = 0;
        for (int anInt : ints) {
            sum += anInt;
        }
        if (sum % 10 == 0) {
            return 0;
        } else
            return 10 - (sum % 10);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void clearAllCache() {
        log.info("start delete all wallet");
    }

    @Override
    public BalanceObjectDTO getAllBalance(long walletAccountId) {
        BalanceObjectDTO balanceObject = walletAccountRepository.getBalanceById(walletAccountId);
        if (balanceObject.getBalance() < 0) {
            balanceObject.setBalance(0);
        }
        return balanceObject;
    }
}

