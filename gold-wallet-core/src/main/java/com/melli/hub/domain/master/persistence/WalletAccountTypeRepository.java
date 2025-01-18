package com.melli.hub.domain.master.persistence;


import com.melli.hub.domain.master.entity.WalletAccountCurrencyEntity;
import com.melli.hub.domain.master.entity.WalletAccountTypeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletAccountTypeRepository extends CrudRepository<WalletAccountTypeEntity, Long> {
   WalletAccountTypeEntity findById(long id);
   List<WalletAccountTypeEntity> findAll();
}
