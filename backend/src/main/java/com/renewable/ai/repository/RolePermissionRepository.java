package com.renewable.ai.repository;

import com.renewable.ai.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    
    List<RolePermission> findByRoleId(Long roleId);
    
    List<RolePermission> findByRoleIdIn(List<Long> roleIds);
    
    boolean existsByRoleIdAndPermissionCode(Long roleId, String permissionCode);
    
    void deleteByRoleId(Long roleId);
    
    @Query("SELECT rp.permissionCode FROM RolePermission rp WHERE rp.roleId IN :roleIds")
    List<String> findPermissionsByRoleIds(@Param("roleIds") List<Long> roleIds);
}
