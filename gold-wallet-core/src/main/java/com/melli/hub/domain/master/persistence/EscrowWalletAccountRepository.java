package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.EscrowWalletAccountEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ShirinAbadi.Mahdi on 8/29/2022
 * @project wallet-api-multi-purchase
 */
@Component
public interface EscrowWalletAccountRepository extends CrudRepository<EscrowWalletAccountEntity, Integer> {
    List<EscrowWalletAccountEntity> findAllByWalletIdAndWalletAccountCurrencyId(long walletId, long walletAccountCurrencyId);
}
