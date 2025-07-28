package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportMerchantEntity;
import com.melli.wallet.domain.slave.entity.ReportMerchantWalletAccountCurrencyEntity;
import com.melli.wallet.domain.slave.entity.ReportWalletAccountCurrencyEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportMerchantWalletAccountCurrencyRepository extends CrudRepository<ReportMerchantWalletAccountCurrencyEntity, Long> {
   List<ReportMerchantWalletAccountCurrencyEntity> findByWalletAccountCurrencyEntityAndMerchantEntity(ReportWalletAccountCurrencyEntity walletAccountCurrencyEntity, ReportMerchantEntity merchantEntity);
   List<ReportMerchantWalletAccountCurrencyEntity> findByMerchantEntity(ReportMerchantEntity merchantEntity);
   List<ReportMerchantWalletAccountCurrencyEntity> findByWalletAccountCurrencyEntity(ReportWalletAccountCurrencyEntity walletAccountCurrencyEntity);
   List<ReportMerchantWalletAccountCurrencyEntity> findAll();
} 