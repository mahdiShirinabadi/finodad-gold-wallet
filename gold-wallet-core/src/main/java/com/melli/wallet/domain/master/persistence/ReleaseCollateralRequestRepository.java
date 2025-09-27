package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.CreateCollateralRequestEntity;
import com.melli.wallet.domain.master.entity.ReleaseCollateralRequestEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseCollateralRequestRepository extends CrudRepository<ReleaseCollateralRequestEntity, Long> {
    ReleaseCollateralRequestEntity findByRrnEntityId(long traceId);
    Optional<ReleaseCollateralRequestEntity> findOptionalByRrnEntityId(long traceId);
    ReleaseCollateralRequestEntity findById(long requestId);
    List<ReleaseCollateralRequestEntity> findByCreateCollateralRequestEntity(CreateCollateralRequestEntity createCollateralRequestEntity);
}
