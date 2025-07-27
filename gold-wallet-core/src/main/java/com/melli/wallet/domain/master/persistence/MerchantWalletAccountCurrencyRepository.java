package com.melli.wallet.domain.master.persistence;


import com.melli.wallet.domain.master.entity.MerchantEntity;
import com.melli.wallet.domain.master.entity.MerchantWalletAccountCurrencyEntity;
import com.melli.wallet.domain.master.entity.WalletAccountCurrencyEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantWalletAccountCurrencyRepository extends CrudRepository<MerchantWalletAccountCurrencyEntity, Long> {
   List<MerchantWalletAccountCurrencyEntity> findByWalletAccountCurrencyEntityAndMerchantEntity(WalletAccountCurrencyEntity walletAccountCurrencyEntity, MerchantEntity merchantEntity);
   List<MerchantWalletAccountCurrencyEntity> findByMerchantEntity(MerchantEntity merchantEntity);
   List<MerchantWalletAccountCurrencyEntity> findByWalletAccountCurrencyEntity(WalletAccountCurrencyEntity walletAccountCurrencyEntity);
   List<MerchantWalletAccountCurrencyEntity> findAll();
}
