package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.PurchaseRequestEntity;
import com.melli.wallet.domain.master.entity.RrnEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseRequestRepository extends CrudRepository<PurchaseRequestEntity, Long> {

    PurchaseRequestEntity findByRrnEntityId(long traceId);
    Optional<PurchaseRequestEntity> findOptionalByRrnEntityId(long traceId);
    PurchaseRequestEntity findByRrnEntity(RrnEntity rrn);
    PurchaseRequestEntity findById(long requestId);
}
