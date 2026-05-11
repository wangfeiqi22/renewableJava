package com.renewable.ai.repository;

import com.renewable.ai.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByRoleCode(String roleCode);
    
    boolean existsByRoleCode(String roleCode);
    
    Optional<Role> findByRoleName(String roleName);
}
