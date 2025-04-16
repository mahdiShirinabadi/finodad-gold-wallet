package com.melli.wallet.domain.master.persistence;


import com.melli.wallet.domain.master.entity.WalletAccountCurrencyEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletAccountCurrencyRepository extends CrudRepository<WalletAccountCurrencyEntity, Long> {
   WalletAccountCurrencyEntity findById(long id);
   List<WalletAccountCurrencyEntity> findAll();
   Optional<WalletAccountCurrencyEntity> findByNameIgnoreCase(String name);
}
