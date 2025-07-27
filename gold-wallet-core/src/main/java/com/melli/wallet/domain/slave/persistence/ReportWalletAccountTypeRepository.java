package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportWalletAccountTypeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportWalletAccountTypeRepository extends CrudRepository<ReportWalletAccountTypeEntity, Long> {
   ReportWalletAccountTypeEntity findById(long id);
   ReportWalletAccountTypeEntity findByName(String name);
   List<ReportWalletAccountTypeEntity> findAll();
} 