package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.CashInRequestEntity;
import com.melli.wallet.domain.master.entity.CashOutRequestEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CashOutRequestRepository extends CrudRepository<CashOutRequestEntity, Long> {

    CashOutRequestEntity findByRrnEntityId(long traceId);
    Optional<CashOutRequestEntity> findOptionalByRrnEntityId(long traceId);
    CashOutRequestEntity findByRrnEntity(RrnEntity rrn);
    CashOutRequestEntity findById(long requestId);
}
