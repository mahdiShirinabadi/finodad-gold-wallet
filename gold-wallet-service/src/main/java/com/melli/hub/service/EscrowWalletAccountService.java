package com.melli.hub.service;

import com.melli.hub.domain.master.entity.EscrowWalletAccountEntity;
import com.melli.hub.domain.master.entity.WalletAccountEntity;
import com.melli.hub.domain.master.entity.WalletEntity;
import com.melli.hub.exception.InternalServiceException;
import com.tara.wallet.exception.ServiceException;
import com.tara.wallet.master.domain.EscrowWalletAccount;
import com.tara.wallet.master.domain.Wallet;
import com.tara.wallet.master.domain.WalletAccount;

import java.util.List;

/**
 * @author ShirinAbadi.Mahdi on 8/29/2022
 * @project wallet-api-multi-purchase
 */
public interface EscrowWalletAccountService {

    WalletAccountEntity findEscrowWalletAccountBySourceWallet(WalletAccountEntity userWalletAccount, WalletEntity walletChannel) throws InternalServiceException;
    List<EscrowWalletAccountEntity> findAllByWalletIdAndWalletAccountTypeId(int walletId, int walletAccountTypeId);
    void save(EscrowWalletAccountEntity escrowWalletAccount);
}
