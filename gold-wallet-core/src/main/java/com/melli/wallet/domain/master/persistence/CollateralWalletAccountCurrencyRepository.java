package com.melli.wallet.domain.master.persistence;


import com.melli.wallet.domain.master.entity.CollateralEntity;
import com.melli.wallet.domain.master.entity.CollateralWalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.entity.WalletAccountCurrencyEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollateralWalletAccountCurrencyRepository extends CrudRepository<CollateralWalletAccountCurrencyEntity, Long> {
   List<CollateralWalletAccountCurrencyEntity> findByWalletAccountCurrencyEntityAndCollateralEntity(WalletAccountCurrencyEntity walletAccountCurrencyEntity, CollateralEntity collateralEntity);
   List<CollateralWalletAccountCurrencyEntity> findByCollateralEntity(CollateralEntity collateralEntity);
   List<CollateralWalletAccountCurrencyEntity> findByWalletAccountCurrencyEntity(WalletAccountCurrencyEntity walletAccountCurrencyEntity);
   List<CollateralWalletAccountCurrencyEntity> findAll();
}
