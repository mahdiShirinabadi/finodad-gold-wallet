package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportWalletTypeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportWalletTypeRepository extends CrudRepository<ReportWalletTypeEntity, Long> {
   ReportWalletTypeEntity findById(long id);
   ReportWalletTypeEntity findByName(String name);
   List<ReportWalletTypeEntity> findAll();
} 