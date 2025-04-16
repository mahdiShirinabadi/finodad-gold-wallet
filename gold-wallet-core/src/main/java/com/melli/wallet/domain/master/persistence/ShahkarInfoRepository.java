package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.ShahkarInfoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShahkarInfoRepository extends CrudRepository<ShahkarInfoEntity, Long> {

    Optional<ShahkarInfoEntity> findTopByMobileAndNationalCodeAndIsMatchOrderByIdDesc(String nationalCode, String mobile, Boolean isMatch);

    Page<ShahkarInfoEntity> findAll(Specification<ShahkarInfoEntity> spec, Pageable pageable);
}
