package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.SeizeCollateralRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeizeCollateralRequestRepository extends CrudRepository<SeizeCollateralRequestEntity, Long> {

    SeizeCollateralRequestEntity findByRrnEntityId(long traceId);
    Optional<SeizeCollateralRequestEntity> findOptionalByRrnEntityId(long traceId);
    SeizeCollateralRequestEntity findById(long requestId);
    Page<SeizeCollateralRequestEntity> findAll(Specification<SeizeCollateralRequestEntity> spec, Pageable pageable);

}
