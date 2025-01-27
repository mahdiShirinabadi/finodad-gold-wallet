package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.TransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

	List<TransactionEntity> findByWalletAccountEntityIdOrderByIdDesc(long walletAccountId, Pageable pageable);
	List<TransactionEntity> findByWalletAccountEntityId(long walletAccountId);
}
