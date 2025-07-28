package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.dto.AggregationCashOutDTO;
import com.melli.wallet.domain.slave.entity.ReportPhysicalCashOutRequestEntity;
import com.melli.wallet.domain.slave.entity.ReportRrnEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface ReportPhysicalCashOutRequestRepository extends CrudRepository<ReportPhysicalCashOutRequestEntity, Long> {

    ReportPhysicalCashOutRequestEntity findByRrnEntityId(long traceId);
    Optional<ReportPhysicalCashOutRequestEntity> findOptionalByRrnEntityId(long traceId);
    ReportPhysicalCashOutRequestEntity findByRrnEntity(ReportRrnEntity rrn);
    ReportPhysicalCashOutRequestEntity findById(long requestId);

    @Query(value = "select COALESCE(SUM(p.final_quantity), 0) as sumPrice, count(*) as countRecord from {h-schema}physical_cash_out_request p inner join {h-schema}request r on p.request_id = r.id  where p.wallet_account_id in :walletAccountId" +
            " and date(r.created_at) BETWEEN date(:fromDate) AND  date(:toDate) and r.result=0", nativeQuery = true)
    AggregationCashOutDTO findSumAmountBetweenDate(@Param("walletAccountId") long[] walletAccountId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
} 