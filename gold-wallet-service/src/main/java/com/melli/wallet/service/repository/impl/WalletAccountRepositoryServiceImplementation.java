package com.melli.wallet.service.repository.impl;

import com.melli.wallet.ConstantRedisName;
import com.melli.wallet.domain.dto.BalanceObjectDTO;
import com.melli.wallet.domain.enumaration.WalletStatusEnum;
import com.melli.wallet.domain.master.entity.*;
import com.melli.wallet.domain.master.persistence.WalletAccountRepository;
import com.melli.wallet.domain.slave.persistence.ReportWalletAccountRepository;
import com.melli.wallet.exception.InternalServiceException;
import com.melli.wallet.service.repository.StatusRepositoryService;
import com.melli.wallet.service.repository.WalletAccountCurrencyRepositoryService;
import com.melli.wallet.service.repository.WalletAccountRepositoryService;
import com.melli.wallet.service.repository.WalletAccountTypeRepositoryService;
import com.melli.wallet.util.Validator;
import com.melli.wallet.utils.AccountNumberGeneratorService;
import com.melli.wallet.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
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
public class WalletAccountRepositoryServiceImplementation implements WalletAccountRepositoryService {

    private final WalletAccountRepository walletAccountRepository;
    private final ReportWalletAccountRepository reportWalletAccountRepository;
    private final WalletAccountCurrencyRepositoryService walletAccountCurrencyRepositoryService;
    private final WalletAccountTypeRepositoryService walletAccountTypeRepositoryService;
    private final AccountNumberGeneratorService accountNumberGeneratorService;


    @Override
    public WalletAccountEntity findByAccountNumberAndEndTimeIsNull(String accountNumber) {
        return walletAccountRepository.findByAccountNumberAndEndTimeIsNull(accountNumber);
    }

    @Override
    public WalletAccountEntity findByAccountNumber(String accountNumber) {
        return walletAccountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public WalletAccountEntity findById(Long id) throws InternalServiceException {
        return walletAccountRepository.findById(id).orElseThrow(()->{
            return new InternalServiceException("walletAccount is not found", StatusRepositoryService.WALLET_ACCOUNT_NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }

    @Override
    public List<WalletAccountEntity> findByWallet(WalletEntity wallet) throws InternalServiceException {
        List<WalletAccountEntity> walletAccountEntityList = walletAccountRepository.findByWalletEntity(wallet);
        if (walletAccountEntityList.isEmpty()) {
            log.error("walletAccount is not create success");
            throw new InternalServiceException("walletAccount is not create success", StatusRepositoryService.WALLET_NOT_CREATE_SUCCESS, HttpStatus.OK);
        }
        return walletAccountEntityList;
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
        for (String currencyName : walletAccountCurrencyList) {
            WalletAccountCurrencyEntity currencyEntity = findCurrencyEntity(currencyName);
            if (currencyEntity == null) continue;

            for (String typeName : walletAccountTypeList) {
                WalletAccountTypeEntity typeEntity = findTypeEntity(typeName);
                if (typeEntity == null) continue;
                createSingleWalletAccount(wallet, channel, currencyEntity, typeEntity);
            }
        }
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

    @Override
    public List<WalletAccountEntity> findByWalletIds(List<Long> walletIds) {
        log.info("start find wallet accounts for {} wallet ids", walletIds.size());
        List<WalletAccountEntity> accounts = walletAccountRepository.findByWalletEntityIdIn(walletIds);
        log.info("found {} wallet accounts", accounts.size());
        return accounts;
    }



    @Override
    public List<Object[]> findAccountDetailsByWalletIds(List<Long> walletIds) {
        log.info("start find account details for {} wallet ids", walletIds.size());
        List<Object[]> result = reportWalletAccountRepository.findAccountDetailsByWalletIds(walletIds);
        log.info("found {} account detail records", result.size());
        return result;
    }

    /**
     * Find currency entity by name
     */
    private WalletAccountCurrencyEntity findCurrencyEntity(String currencyName) {
        List<WalletAccountCurrencyEntity> walletAccountCurrencyEntityList = walletAccountCurrencyRepositoryService.getAll();
        return walletAccountCurrencyEntityList.stream()
                .filter(x -> x.getName().equals(currencyName))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("Currency not found: {}", currencyName);
                    return null;
                });
    }

    /**
     * Find type entity by name
     */
    private WalletAccountTypeEntity findTypeEntity(String typeName) {
        List<WalletAccountTypeEntity> walletAccountTypeEntityList = walletAccountTypeRepositoryService.getAllManaged();
        return walletAccountTypeEntityList.stream()
                .filter(x -> x.getName().equals(typeName))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("Account type not found: {}", typeName);
                    return null;
                });
    }

    /**
     * Create a single wallet account with retry logic
     */
    private void createSingleWalletAccount(WalletEntity wallet, ChannelEntity channel,
                                           WalletAccountCurrencyEntity currencyEntity,
                                           WalletAccountTypeEntity typeEntity) throws InternalServiceException {

        int maxAttempts = 10;
        int attempts = 0;

        while (attempts < maxAttempts) {
            try {
                attempts++;

                // Create wallet account entity
                WalletAccountEntity walletAccountEntity = buildWalletAccountEntity(
                        wallet, channel, currencyEntity, typeEntity
                );

                // Generate unique account number
                String accountNumber = accountNumberGeneratorService.generateAccountNumberInternal(
                        wallet.getId(),
                        currencyEntity.getStandardName()
                );
                walletAccountEntity.setAccountNumber(accountNumber);
                walletAccountRepository.save(walletAccountEntity);
                log.info("Account created successfully for wallet {}: {}", wallet.getId(), accountNumber);
                return; // Success - exit method

            } catch (DataIntegrityViolationException e) {
                log.warn("Attempt {}: Duplicate account number for wallet {}, regenerating...",
                        attempts, wallet.getId());

                if (attempts >= maxAttempts) {
                    log.error("Cannot create unique account number for walletId ({}) after maximum attempts", wallet.getId());
                    throw new InternalServiceException(
                            "System cannot create unique account number after maximum attempts",
                            StatusRepositoryService.ERROR_IN_SAVE_UNIQUE_ACCOUNT_NUMBER,
                            HttpStatus.OK
                    );
                }
            }
        }
    }

    /**
     * Build wallet account entity
     */
    private WalletAccountEntity buildWalletAccountEntity(WalletEntity wallet, ChannelEntity channel,
                                                         WalletAccountCurrencyEntity currencyEntity,
                                                         WalletAccountTypeEntity typeEntity) {
        WalletAccountEntity entity = new WalletAccountEntity();
        entity.setWalletEntity(wallet);
        entity.setWalletAccountTypeEntity(typeEntity);
        entity.setWalletAccountCurrencyEntity(currencyEntity);
        entity.setStatus(WalletStatusEnum.ACTIVE);
        entity.setCreatedAt(new Date());
        entity.setCreatedBy(channel.getUsername());
        return entity;
    }

}

