package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.RoleEntity;
import com.melli.wallet.domain.master.entity.ResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface RoleResourceRepository extends JpaRepository<RoleEntity, Long> {
    
    @Query("SELECT r.resources FROM RoleEntity r WHERE r.id = :roleId")
    Set<ResourceEntity> findResourcesByRoleId(@Param("roleId") Long roleId);
    
    @Modifying
    @Transactional
    @Query("UPDATE RoleEntity r SET r.resources = :resources WHERE r.id = :roleId")
    void updateRoleResources(@Param("roleId") Long roleId, @Param("resources") Set<ResourceEntity> resources);

    @Modifying
    @Transactional
    @Query("DELETE RoleEntity r WHERE r.id = :roleId")
    void deleteRoleResources(@Param("roleId") Long roleId);

    @Query("SELECT r FROM RoleEntity r INNER JOIN FETCH r.resources WHERE r.id = :roleId")
    RoleEntity findRoleWithResources(@Param("roleId") Long roleId);
}
