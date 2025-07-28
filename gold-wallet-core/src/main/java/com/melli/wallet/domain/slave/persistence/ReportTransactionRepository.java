package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportTransactionRepository extends JpaRepository<ReportTransactionEntity, Long> {

	Page<ReportTransactionEntity> findByWalletAccountEntityIdOrderByIdDesc(long walletAccountId, Pageable pageable);
	List<ReportTransactionEntity> findByWalletAccountEntityId(long walletAccountId);
} 