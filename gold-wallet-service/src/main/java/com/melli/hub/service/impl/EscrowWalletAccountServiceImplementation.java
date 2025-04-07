package com.melli.hub.service.impl;

import com.melli.hub.domain.master.entity.EscrowWalletAccountEntity;
import com.melli.hub.domain.master.entity.WalletAccountEntity;
import com.melli.hub.domain.master.entity.WalletEntity;
import com.melli.hub.domain.master.persistence.EscrowWalletAccountRepository;
import com.melli.hub.exception.InternalServiceException;
import com.melli.hub.service.EscrowWalletAccountService;
import com.melli.hub.service.WalletAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Class Name: EscrowWalletAccountServiceImplementation
 * Author: Mahdi Shirinabadi
 * Date: 1/27/2025
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class EscrowWalletAccountServiceImplementation implements EscrowWalletAccountService {

    private final EscrowWalletAccountRepository escrowWalletAccountRepository;
    private final WalletAccountService walletAccountService;

    @Override
    public WalletAccountEntity findEscrowWalletAccountBySourceWallet(WalletAccountEntity userWalletAccount, WalletEntity walletEntity) throws InternalServiceException {
        long walletAccountCurrencyId=userWalletAccount.getWalletAccountCurrencyEntity().getId();
        log.info("start find escrow wallet account for channel ({}) with accountType ({})", walletEntity.getOwner().getUsername(), walletAccountCurrencyId);
        List<EscrowWalletAccountEntity> escrowWalletAccountEntityList = escrowWalletAccountRepository.findAllByWalletIdAndWalletAccountCurrencyId(walletEntity.getId(), walletAccountCurrencyId);
        log.info("count find escrow wallet account for channel ({}) is: ({}) with accountType ({})", walletEntity.getOwner().getUsername(), escrowWalletAccountEntityList.size(), walletAccountCurrencyId);

        if (CollectionUtils.isEmpty(escrowWalletAccountEntityList)) {
            return walletAccountService.findByWalletAndWalletAccountCurrencyId(walletEntity, userWalletAccount.getWalletAccountCurrencyEntity().getId());
        }
        return null;
    }

    @Override
    public List<EscrowWalletAccountEntity> findAllByWalletIdAndWalletAccountTypeId(int walletId, int walletAccountTypeId) {
        return List.of();
    }

    @Override
    public void save(EscrowWalletAccountEntity escrowWalletAccount) {

    }
}
