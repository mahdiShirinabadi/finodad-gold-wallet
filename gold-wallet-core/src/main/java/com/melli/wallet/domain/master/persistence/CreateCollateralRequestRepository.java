package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.CreateCollateralRequestEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreateCollateralRequestRepository extends CrudRepository<CreateCollateralRequestEntity, Long> {

    CreateCollateralRequestEntity findByRrnEntityId(long traceId);
    Optional<CreateCollateralRequestEntity> findOptionalByRrnEntityId(long traceId);
    CreateCollateralRequestEntity findById(long requestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CreateCollateralRequestEntity> findByCode(String code);

    Page<CreateCollateralRequestEntity> findAll(Specification<CreateCollateralRequestEntity> spec, Pageable pageable);

    @Query("select count(a.id) from CreateCollateralRequestEntity a where a.code = :code")
    Long countByCode(@Param("code") String code);
}
