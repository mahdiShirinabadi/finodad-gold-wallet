package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.CollateralEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollateralRepository extends CrudRepository<CollateralEntity, Long> {
	CollateralEntity findById(long id);
	List<CollateralEntity> findAllById(long collateralId);
}
