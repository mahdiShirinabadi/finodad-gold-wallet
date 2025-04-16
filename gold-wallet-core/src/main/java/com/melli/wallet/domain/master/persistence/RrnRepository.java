package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.RrnEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RrnRepository extends CrudRepository<RrnEntity, Long> {

    RrnEntity findById(long id);

    @Query(value="select uid from {h-schema}rrn where id = :id", nativeQuery= true)
    String findUidById(@Param("id") long id);

    RrnEntity findByUuid(String uuid);

}
