package com.melli.wallet.domain.master.persistence;


import com.melli.wallet.domain.master.entity.WalletAccountTypeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletAccountTypeRepository extends CrudRepository<WalletAccountTypeEntity, Long> {
   WalletAccountTypeEntity findById(long id);
   WalletAccountTypeEntity findByName(String name);
   List<WalletAccountTypeEntity> findAll();
}
