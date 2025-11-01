package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.dto.AggregationCashOutDTO;
import com.melli.wallet.domain.enumaration.SettlementStepEnum;
import com.melli.wallet.domain.master.entity.CashOutRequestEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashOutRequestRepository extends CrudRepository<CashOutRequestEntity, Long> {

    CashOutRequestEntity findByRrnEntityId(long traceId);
    Optional<CashOutRequestEntity> findOptionalByRrnEntityId(long traceId);
    CashOutRequestEntity findByRrnEntity(RrnEntity rrn);
    CashOutRequestEntity findById(long requestId);

    @Query(value = "select COALESCE(SUM(p.amount), 0) as sumPrice, count(*) as countRecord from {h-schema}cash_out_request p inner join {h-schema}request r on p.request_id = r.id  where p.wallet_account_id in :walletAccountId" +
            " and date(r.created_at) BETWEEN date(:fromDate) AND  date(:toDate) and r.result=0", nativeQuery = true)
    AggregationCashOutDTO findSumAmountBetweenDate(@Param("walletAccountId") long[] walletAccountId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    @Query(value = "SELECT * FROM {h-schema}cash_out_request WHERE settlement_step = :settlementStepEnum ORDER BY request_id ASC LIMIT :limit FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<CashOutRequestEntity> findAllBySettlementStepEnumOrderByIdAscWithLimit(@Param("settlementStepEnum") String settlementStepEnum, @Param("limit") int limit);
    
    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query(value = "UPDATE {h-schema}cash_out_request SET settlement_step = :newStep WHERE request_id IN (:ids) AND settlement_step = :oldStep", nativeQuery = true)
    int updateSettlementStepAtomically(@Param("ids") List<Long> ids, @Param("oldStep") String oldStep, @Param("newStep") String newStep);
    
    @Query(value = "SELECT * FROM {h-schema}cash_out_request WHERE request_id IN (:ids) AND settlement_step = :settlementStep", nativeQuery = true)
    List<CashOutRequestEntity> findByIdsAndSettlementStep(@Param("ids") List<Long> ids, @Param("settlementStep") String settlementStep);
}
