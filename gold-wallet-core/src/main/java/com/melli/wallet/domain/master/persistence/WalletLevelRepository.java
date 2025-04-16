package com.melli.wallet.domain.master.persistence;


import com.melli.wallet.domain.master.entity.WalletLevelEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletLevelRepository extends CrudRepository<WalletLevelEntity, Long> {
   WalletLevelEntity findById(long id);
   List<WalletLevelEntity> findAll();
}
