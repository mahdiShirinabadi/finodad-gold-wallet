package com.melli.wallet.domain.master.persistence;


import com.melli.wallet.domain.master.entity.TemplateEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends CrudRepository<TemplateEntity, Long>, JpaSpecificationExecutor<TemplateEntity> {
    TemplateEntity findByName(String name);
}
