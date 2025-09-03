package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.dto.AggregationP2PDTO;
import com.melli.wallet.domain.master.entity.Person2PersonRequestEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface P2PRequestRepository extends CrudRepository<Person2PersonRequestEntity, Long> {

    Person2PersonRequestEntity findByRrnEntityId(long traceId);
    Optional<Person2PersonRequestEntity> findOptionalByRrnEntityId(long traceId);
    Person2PersonRequestEntity findByRrnEntity(RrnEntity rrn);
    Person2PersonRequestEntity findById(long requestId);

    @Query(value = "select COALESCE(SUM(p.amount), 0) as sumAmount, count(*) as countRecord from {h-schema}p_2_p_request p inner join {h-schema}request r on p.request_id = r.id  where p.wallet_account_id in :walletAccountId" +
            " and date(r.created_at) BETWEEN date(:fromDate) AND  date(:toDate) and r.result=0", nativeQuery = true)
    AggregationP2PDTO findSumAmountBetweenDate(@Param("walletAccountId") long[] walletAccountId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
}
