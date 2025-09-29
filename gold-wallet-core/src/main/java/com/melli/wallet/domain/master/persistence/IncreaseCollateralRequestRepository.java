package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.CreateCollateralRequestEntity;
import com.melli.wallet.domain.master.entity.IncreaseCollateralRequestEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncreaseCollateralRequestRepository extends CrudRepository<IncreaseCollateralRequestEntity, Long> {
    IncreaseCollateralRequestEntity findByRrnEntityId(long traceId);
    Optional<IncreaseCollateralRequestEntity> findOptionalByRrnEntityId(long traceId);
    IncreaseCollateralRequestEntity findById(long requestId);
    List<IncreaseCollateralRequestEntity> findByCreateCollateralRequestEntity(CreateCollateralRequestEntity createCollateralRequestEntity);
}
