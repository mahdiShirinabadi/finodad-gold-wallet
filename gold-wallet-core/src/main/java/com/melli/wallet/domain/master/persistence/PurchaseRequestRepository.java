package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.dto.AggregationPurchaseDTO;
import com.melli.wallet.domain.master.entity.PurchaseRequestEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface PurchaseRequestRepository extends CrudRepository<PurchaseRequestEntity, Long> {

    PurchaseRequestEntity findByRrnEntityId(long traceId);
    Optional<PurchaseRequestEntity> findOptionalByRrnEntityId(long traceId);
    PurchaseRequestEntity findByRrnEntity(RrnEntity rrn);
    PurchaseRequestEntity findById(long requestId);

    //TODO move to report class
    @Query(value = "select COALESCE(SUM(p.price), 0) as sumPrice, count(*) as countRecord, COALESCE(SUM(p.quantity), 0) as sumQuantity from {h-schema}purchase_request p inner join {h-schema}request r on p.request_id = r.id  where p.wallet_account_id in :walletAccountId" +
            " and date(r.created_at) BETWEEN date(:fromDate) AND  date(:toDate) and r.result=0 and p.transaction_type = :transactionType", nativeQuery = true)
    AggregationPurchaseDTO findSumAmountByTransactionTypeBetweenDate(@Param("walletAccountId") long[] walletAccountId, @Param("transactionType") String transactionType, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

}
