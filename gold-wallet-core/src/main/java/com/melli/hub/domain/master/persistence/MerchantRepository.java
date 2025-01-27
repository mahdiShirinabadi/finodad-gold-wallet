package com.melli.hub.domain.master.persistence;

import com.melli.hub.domain.master.entity.MerchantEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantRepository extends CrudRepository<MerchantEntity, Long> {
	MerchantEntity findById(long id);
	List<MerchantEntity> findAllById(long merchantId);
}
