package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.ShedLockEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShedLockRepository extends CrudRepository<ShedLockEntity, Long> {
    ShedLockEntity findByName(String name);

    Integer deleteByName(String name);

    Page<ShedLockEntity> findAll(Specification<ShedLockEntity> spec, Pageable pageable);
}
