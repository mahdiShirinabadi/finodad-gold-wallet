package com.melli.wallet.domain.master.persistence;

import com.melli.wallet.domain.master.entity.RoleEntity;
import com.melli.wallet.domain.master.entity.ResourceEntity;
import com.melli.wallet.domain.master.entity.RoleResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface RoleResourceRepository extends JpaRepository<RoleResourceEntity, Long> {
    
    @Query("SELECT r.resources FROM RoleEntity r WHERE r.id = :roleId")
    Set<ResourceEntity> findResourcesByRoleId(@Param("roleId") Long roleId);
    
    @Modifying
    @Transactional
    @Query("UPDATE RoleResourceEntity r SET r.resourceEntity.id = :resources WHERE r.roleEntity.id = :roleId")
    void updateRoleResources(@Param("roleId") Long roleId, @Param("resources") Set<ResourceEntity> resources);

    @Modifying
    @Transactional
    @Query("DELETE RoleResourceEntity r WHERE r.roleEntity.id = :roleId")
    void deleteRoleResources(@Param("roleId") Long roleId);

    @Query("SELECT r FROM RoleResourceEntity r INNER JOIN RoleEntity re on r.roleEntity.id = re.id WHERE r.roleEntity.id = :roleId")
    RoleEntity findRoleWithResources(@Param("roleId") Long roleId);
}
