package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportMerchantEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportMerchantRepository extends CrudRepository<ReportMerchantEntity, Long> {
	ReportMerchantEntity findById(long id);
	List<ReportMerchantEntity> findAllById(long merchantId);
} 