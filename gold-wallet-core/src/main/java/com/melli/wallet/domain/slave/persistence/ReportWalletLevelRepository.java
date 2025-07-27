package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportWalletLevelEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportWalletLevelRepository extends CrudRepository<ReportWalletLevelEntity, Long> {
   ReportWalletLevelEntity findById(long id);
   List<ReportWalletLevelEntity> findAll();
   ReportWalletLevelEntity getByName(String walletLevelName);
} 