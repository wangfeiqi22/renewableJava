package com.renewable.ai.service;

import com.renewable.ai.common.Permission;
import com.renewable.ai.common.RoleCode;
import com.renewable.ai.entity.*;
import com.renewable.ai.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class PermissionService {
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    public List<String> getUserPermissions(Long userId) {
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return rolePermissionRepository.findPermissionsByRoleIds(roleIds);
    }
    
    public boolean hasPermission(Long userId, String permission) {
        List<String> permissions = getUserPermissions(userId);
        return permissions.contains(permission);
    }
    
    public boolean hasAnyPermission(Long userId, String... permissions) {
        List<String> userPermissions = getUserPermissions(userId);
        for (String permission : permissions) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasAllPermissions(Long userId, String... permissions) {
        List<String> userPermissions = getUserPermissions(userId);
        for (String permission : permissions) {
            if (!userPermissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }
    
    public List<String> getUserRoles(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        List<String> roleCodes = new ArrayList<>();
        for (UserRole userRole : userRoles) {
            roleRepository.findById(userRole.getRoleId()).ifPresent(role -> {
                roleCodes.add(role.getRoleCode());
            });
        }
        return roleCodes;
    }
    
    public boolean hasRole(Long userId, String roleCode) {
        List<String> roles = getUserRoles(userId);
        return roles.contains(roleCode);
    }
    
    public boolean hasAnyRole(Long userId, String... roleCodes) {
        List<String> roles = getUserRoles(userId);
        for (String roleCode : roleCodes) {
            if (roles.contains(roleCode)) {
                return true;
            }
        }
        return false;
    }
    
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId, Long grantedBy) {
        if (!userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setGrantedBy(grantedBy);
            userRoleRepository.save(userRole);
        }
    }
    
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        userRoleRepository.findByUserIdAndRoleId(userId, roleId)
                .ifPresent(userRoleRepository::delete);
    }
    
    @Transactional
    public void grantPermissionToRole(Long roleId, String permissionCode) {
        if (!rolePermissionRepository.existsByRoleIdAndPermissionCode(roleId, permissionCode)) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionCode(permissionCode);
            rolePermissionRepository.save(rolePermission);
        }
    }
    
    @Transactional
    public void revokePermissionFromRole(Long roleId, String permissionCode) {
        List<RolePermission> permissions = rolePermissionRepository.findByRoleId(roleId);
        permissions.stream()
                .filter(p -> p.getPermissionCode().equals(permissionCode))
                .findFirst()
                .ifPresent(rolePermissionRepository::delete);
    }
    
    @Transactional
    public void initializeDefaultRolesAndPermissions() {
        initializeRoleAndPermissions(RoleCode.SUPER_ADMIN, "超级管理员", true,
                Permission.FLEET_VIEW, Permission.FLEET_UPDATE,
                Permission.DRIVER_VIEW, Permission.DRIVER_CREATE, Permission.DRIVER_UPDATE, Permission.DRIVER_DELETE,
                Permission.ORDER_VIEW, Permission.ORDER_CREATE, Permission.ORDER_UPDATE, Permission.ORDER_DELETE,
                Permission.ORDER_ASSIGN, Permission.ORDER_REASSIGN,
                Permission.PROJECT_VIEW, Permission.PROJECT_CREATE, Permission.PROJECT_UPDATE, Permission.PROJECT_DELETE,
                Permission.MONITORING_VIEW, Permission.MONITORING_DISPATCH,
                Permission.GRAB_VIEW, Permission.GRAB_ORDER, Permission.GRAB_RULE_MANAGE, Permission.GRAB_POOL_MANAGE,
                Permission.ROLE_MANAGE, Permission.USER_MANAGE,
                Permission.EXPORT_DATA, Permission.IMPORT_DATA);
        
        initializeRoleAndPermissions(RoleCode.FLEET_OWNER, "车队所有者", true,
                Permission.FLEET_VIEW,
                Permission.DRIVER_VIEW, Permission.DRIVER_CREATE, Permission.DRIVER_UPDATE, Permission.DRIVER_DELETE,
                Permission.ORDER_VIEW, Permission.ORDER_CREATE, Permission.ORDER_UPDATE, Permission.ORDER_DELETE,
                Permission.ORDER_ASSIGN, Permission.ORDER_REASSIGN,
                Permission.PROJECT_VIEW, Permission.PROJECT_CREATE, Permission.PROJECT_UPDATE, Permission.PROJECT_DELETE,
                Permission.MONITORING_VIEW, Permission.MONITORING_DISPATCH,
                Permission.GRAB_VIEW, Permission.GRAB_ORDER, Permission.GRAB_RULE_MANAGE, Permission.GRAB_POOL_MANAGE,
                Permission.EXPORT_DATA);
        
        initializeRoleAndPermissions(RoleCode.FLEET_MANAGER, "车队管理员", true,
                Permission.DRIVER_VIEW, Permission.DRIVER_CREATE, Permission.DRIVER_UPDATE, Permission.DRIVER_DELETE,
                Permission.ORDER_VIEW, Permission.ORDER_CREATE, Permission.ORDER_UPDATE,
                Permission.ORDER_ASSIGN, Permission.ORDER_REASSIGN,
                Permission.PROJECT_VIEW, Permission.PROJECT_CREATE, Permission.PROJECT_UPDATE,
                Permission.MONITORING_VIEW, Permission.MONITORING_DISPATCH,
                Permission.GRAB_VIEW, Permission.GRAB_RULE_MANAGE, Permission.GRAB_POOL_MANAGE,
                Permission.EXPORT_DATA);
        
        initializeRoleAndPermissions(RoleCode.FLEET_OPERATOR, "车队操作员", false,
                Permission.DRIVER_VIEW,
                Permission.ORDER_VIEW, Permission.ORDER_CREATE,
                Permission.ORDER_ASSIGN,
                Permission.PROJECT_VIEW,
                Permission.MONITORING_VIEW,
                Permission.GRAB_VIEW);
        
        initializeRoleAndPermissions(RoleCode.DRIVER, "司机", false,
                Permission.ORDER_VIEW,
                Permission.GRAB_VIEW, Permission.GRAB_ORDER);
    }
    
    private void initializeRoleAndPermissions(String roleCode, String roleName, boolean fleetLevel, String... permissions) {
        if (!roleRepository.existsByRoleCode(roleCode)) {
            Role role = new Role();
            role.setRoleCode(roleCode);
            role.setRoleName(roleName);
            role.setFleetLevel(fleetLevel);
            Role savedRole = roleRepository.save(role);
            
            for (String permission : permissions) {
                grantPermissionToRole(savedRole.getId(), permission);
            }
        }
    }
}
