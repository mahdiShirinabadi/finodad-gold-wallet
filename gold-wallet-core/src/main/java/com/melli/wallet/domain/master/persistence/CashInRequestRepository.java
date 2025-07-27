package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.dto.AggregationCashInDTO;
import com.melli.wallet.domain.master.entity.CashInRequestEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface CashInRequestRepository extends CrudRepository<CashInRequestEntity, Long> {

    CashInRequestEntity findByRefNumber(String refNumber);
    CashInRequestEntity findByRrnEntityId(long traceId);
    Optional<CashInRequestEntity> findOptionalByRrnEntityId(long traceId);
    CashInRequestEntity findByRrnEntity(RrnEntity rrn);
    CashInRequestEntity findById(long requestId);

    //TODO move to report class
    @Query(value = "select COALESCE(SUM(p.amount), 0) as sumPrice, count(*) as countRecord from {h-schema}cash_in_request p inner join {h-schema}request r on p.request_id = r.id  where p.wallet_account_id in :walletAccountId" +
            " and date(r.created_at) BETWEEN date(:fromDate) AND  date(:toDate) and r.result=0", nativeQuery = true)
    AggregationCashInDTO findSumAmountBetweenDate(@Param("walletAccountId") long[] walletAccountId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

}
