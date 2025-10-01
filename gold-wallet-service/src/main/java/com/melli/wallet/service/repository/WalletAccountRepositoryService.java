package com.melli.wallet.service.repository;

import com.melli.wallet.domain.dto.BalanceDTO;
import com.melli.wallet.domain.master.entity.ChannelEntity;
import com.melli.wallet.domain.master.entity.WalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.entity.WalletAccountEntity;
import com.melli.wallet.domain.master.entity.WalletEntity;
import com.melli.wallet.exception.InternalServiceException;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface WalletAccountRepositoryService {

    int ACTIVE = 1;
    int DISABLED = 2;
    int TEMP_BLOCK = 3;
    int DELETE_ACCOUNT = 4;
    String DELETED="true";
    String NOT_DELETED="false";

    short LEVEL_1 = 1;
    short LEVEL_2 = 2;

    List<WalletAccountEntity> findByWallet(WalletEntity walletEntity) throws InternalServiceException;

    List<WalletAccountEntity> findByWallet(WalletEntity wallet, Pageable pageable);

    WalletAccountEntity findByWalletAndAccount(WalletEntity walletEntity, String account);

    WalletAccountEntity findByWalletAndWalletAccountCurrencyId(WalletEntity walletEntity, long walletAccountCurrencyId);

    WalletAccountEntity findByAccountNumber(String account);
    WalletAccountEntity findById(Long id) throws InternalServiceException;

    BalanceDTO getBalance(long walletAccountId);
    BigDecimal getBlockAmount(long walletAccountId);

    void increaseBalance(long walletAccountId, BigDecimal amount);

    int decreaseBalance(long walletAccountId, BigDecimal amount);

    int unblockAndDecreaseAmount(long walletAccountId, BigDecimal amount);

    int blockAmount(long walletAccountId, BigDecimal amount) throws InternalServiceException;

    int unblockAmount(long walletAccountId, BigDecimal amount);

    void save(WalletAccountEntity walletAccount) throws InternalServiceException;

    void clearCache(String accountNumber);

    void createAccount(List<String> walletAccountCurrencyList , WalletEntity wallet, List<String> walletAccountType, ChannelEntity channel) throws InternalServiceException;

    void clearAllCache();

    WalletAccountEntity findByAccountNumberAndEndTimeIsNull(String accountNumber);

    List<WalletAccountEntity> findByWalletIds(List<Long> walletIds);

    List<Object[]> findAccountDetailsByWalletIds(List<Long> walletIds);

    WalletAccountEntity findChannelCommissionAccount(ChannelEntity channel, String walletAccountTypeName) throws InternalServiceException;
    WalletAccountEntity findUserAccount(WalletEntity wallet, WalletAccountCurrencyEntity currencyEntity, String nationalCode) throws InternalServiceException;
    WalletAccountEntity checkUserAccount(WalletEntity wallet, WalletAccountCurrencyEntity currencyEntity, String walletAccountNumber, String nationalCode) throws InternalServiceException;
    public WalletAccountEntity findUserWalletAccount(
            WalletEntity walletEntity,
            WalletAccountCurrencyEntity currencyEntity,
            String currency
    ) throws InternalServiceException;

}
