package com.melli.hub.service;

import com.melli.hub.domain.dto.BalanceObjectDTO;
import com.melli.hub.domain.master.entity.ChannelEntity;
import com.melli.hub.domain.master.entity.WalletAccountEntity;
import com.melli.hub.domain.master.entity.WalletEntity;
import com.melli.hub.exception.InternalServiceException;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WalletAccountService {

    int ACTIVE = 1;
    int DISABLED = 2;
    int TEMP_BLOCK = 3;
    int DELETE_ACCOUNT = 4;
    String DELETED="true";
    String NOT_DELETED="false";

    short LEVEL_1 = 1;
    short LEVEL_2 = 2;

    List<WalletAccountEntity> findByWallet(WalletEntity walletEntity);

    List<WalletAccountEntity> findByWallet(WalletEntity wallet, Pageable pageable);

    List<WalletAccountEntity> findByPartnerId(int partnerId);

    WalletAccountEntity findByWalletAndPartnerId(WalletEntity walletEntity, int partnerId);

    WalletAccountEntity findByWalletAndAccount(WalletEntity walletEntity, String account);

    WalletAccountEntity findByWalletAndWalletAccountCurrencyId(WalletEntity walletEntity, long walletAccountCurrencyId);

    WalletAccountEntity findByAccountNumber(String account);

    long getBalance(long walletAccountId);

    void increaseBalance(long walletAccountId, long amount);

    int decreaseBalance(long walletAccountId, long amount);

    void blockAmount(long walletAccountId, long amount);

    void unblockAmount(long walletAccountId, long amount);

    void save(WalletAccountEntity walletAccount);

    void clearCache(String accountNumber);

    void createAccount(List<String> walletAccountCurrencyList , WalletEntity wallet, List<String> walletAccountType, ChannelEntity channel) throws InternalServiceException;

    void clearAllCache();

    BalanceObjectDTO getAllBalance(long walletAccountId);

    WalletAccountEntity findByAccountNumberAndEndTimeIsNull(String accountNumber);

}
