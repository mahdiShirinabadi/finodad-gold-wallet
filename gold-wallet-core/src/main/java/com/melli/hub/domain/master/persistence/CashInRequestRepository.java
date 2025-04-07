package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.CashInRequestEntity;
import com.melli.hub.domain.master.entity.RrnEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CashInRequestRepository extends CrudRepository<CashInRequestEntity, Long> {

    CashInRequestEntity findByRefNumber(String refNumber);
    CashInRequestEntity findByRrnEntityId(long traceId);
    Optional<CashInRequestEntity> findOptionalByRrnEntityId(long traceId);
    CashInRequestEntity findByRrnEntity(RrnEntity rrn);
    CashInRequestEntity findById(long requestId);
}
