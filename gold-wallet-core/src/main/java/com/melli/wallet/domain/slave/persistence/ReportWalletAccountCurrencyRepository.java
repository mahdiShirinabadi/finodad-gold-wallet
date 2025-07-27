package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportWalletAccountCurrencyEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportWalletAccountCurrencyRepository extends CrudRepository<ReportWalletAccountCurrencyEntity, Long> {
   ReportWalletAccountCurrencyEntity findById(long id);
   List<ReportWalletAccountCurrencyEntity> findAll();
   Optional<ReportWalletAccountCurrencyEntity> findByNameIgnoreCase(String name);
} 