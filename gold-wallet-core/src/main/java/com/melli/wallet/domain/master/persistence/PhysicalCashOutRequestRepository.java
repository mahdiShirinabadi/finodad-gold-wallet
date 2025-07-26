package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.dto.AggregationCashOutDTO;
import com.melli.wallet.domain.master.entity.PhysicalCashOutRequestEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface PhysicalCashOutRequestRepository extends CrudRepository<PhysicalCashOutRequestEntity, Long> {

    PhysicalCashOutRequestEntity findByRrnEntityId(long traceId);
    Optional<PhysicalCashOutRequestEntity> findOptionalByRrnEntityId(long traceId);
    PhysicalCashOutRequestEntity findByRrnEntity(RrnEntity rrn);
    PhysicalCashOutRequestEntity findById(long requestId);

    @Query(value = "select COALESCE(SUM(p.final_quantity), 0) as sumPrice, count(*) as countRecord from {h-schema}physical_cash_out_request p inner join {h-schema}request r on p.request_id = r.id  where p.wallet_account_id in :walletAccountId" +
            " and date(r.created_at) BETWEEN date(:fromDate) AND  date(:toDate) and r.result=0", nativeQuery = true)
    AggregationCashOutDTO findSumAmountBetweenDate(@Param("walletAccountId") long[] walletAccountId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
}
