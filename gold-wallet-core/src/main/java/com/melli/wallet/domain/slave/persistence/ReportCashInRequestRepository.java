package com.melli.wallet.domain.slave.persistence;

import com.melli.wallet.domain.dto.AggregationCashInDTO;
import com.melli.wallet.domain.slave.entity.ReportCashInRequestEntity;
import com.melli.wallet.domain.slave.entity.ReportRrnEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface ReportCashInRequestRepository extends CrudRepository<ReportCashInRequestEntity, Long> {

    ReportCashInRequestEntity findByRefNumber(String refNumber);
    ReportCashInRequestEntity findByRrnEntityId(long traceId);
    Optional<ReportCashInRequestEntity> findOptionalByRrnEntityId(long traceId);
    ReportCashInRequestEntity findByRrnEntity(ReportRrnEntity rrn);
    ReportCashInRequestEntity findById(long requestId);

    @Query(value = "select COALESCE(SUM(p.amount), 0) as sumPrice, count(*) as countRecord from {h-schema}cash_in_request p inner join {h-schema}request r on p.request_id = r.id  where p.wallet_account_id in :walletAccountId" +
            " and date(r.created_at) BETWEEN date(:fromDate) AND  date(:toDate) and r.result=0", nativeQuery = true)
    AggregationCashInDTO findSumAmountBetweenDate(@Param("walletAccountId") long[] walletAccountId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

} 