package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.slave.entity.ReportTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReportTransactionRepository extends JpaRepository<ReportTransactionEntity, Long> {

	Page<ReportTransactionEntity> findByWalletAccountEntityIdOrderByIdDesc(long walletAccountId, Pageable pageable);
	Page<ReportTransactionEntity> findAll(Specification<ReportTransactionEntity> spec, Pageable pageable);
	List<ReportTransactionEntity> findByWalletAccountEntityId(long walletAccountId);
	
    @Query("SELECT " +
           "SUM(CASE WHEN rt.requestTypeId = :increaseRequestTypeId THEN rt.amount ELSE 0 END) - " +
           "SUM(CASE WHEN rt.requestTypeId = :decreaseRequestTypeId THEN rt.amount ELSE 0 END) " +
           "FROM ReportTransactionEntity rt " +
           "WHERE rt.walletAccountEntity.id IN :walletAccountIds " +
           "AND rt.requestTypeId IN (:increaseRequestTypeId, :decreaseRequestTypeId) " +
           "AND rt.walletAccountEntity.walletAccountCurrencyEntity.id = :currencyId")
    BigDecimal calculateBalanceByWalletAccountIdsAndRequestTypesAndCurrency(
        @Param("walletAccountIds") List<Long> walletAccountIds,
        @Param("increaseRequestTypeId") Long increaseRequestTypeId,
        @Param("decreaseRequestTypeId") Long decreaseRequestTypeId,
        @Param("currencyId") Long currencyId
    );
	
	@Query("SELECT SUM(rt.amount) " +
	       "FROM ReportTransactionEntity rt " +
	       "WHERE rt.requestTypeId = :requestTypeId")
	BigDecimal calculateTotalQuantityByRequestType(@Param("requestTypeId") Long requestTypeId);
} 