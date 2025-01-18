package com.melli.hub.domain.master.persistence;


import com.melli.hub.domain.master.entity.WalletAccountCurrencyEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletAccountCurrencyRepository extends CrudRepository<WalletAccountCurrencyEntity, Long> {
   WalletAccountCurrencyEntity findById(long id);
   List<WalletAccountCurrencyEntity> findAll();
}
