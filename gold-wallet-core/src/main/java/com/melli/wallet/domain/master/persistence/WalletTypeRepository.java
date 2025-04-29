package com.melli.wallet.domain.master.persistence;


import com.melli.wallet.domain.master.entity.WalletTypeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTypeRepository extends CrudRepository<WalletTypeEntity, Long> {
   WalletTypeEntity findById(long id);
   WalletTypeEntity findByName(String name);
   List<WalletTypeEntity> findAll();
}
