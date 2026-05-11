package com.renewable.ai.interceptor;

import com.renewable.ai.annotation.RequiresPermission;
import com.renewable.ai.annotation.RequiresRole;
import com.renewable.ai.entity.User;
import com.renewable.ai.exception.BusinessException;
import com.renewable.ai.service.PermissionService;
import com.renewable.ai.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Arrays;

@Component
public class PermissionInterceptor implements HandlerInterceptor {
    
    @Autowired
    private PermissionService permissionService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        RequiresRole classRole = handlerMethod.getBeanType().getAnnotation(RequiresRole.class);
        RequiresRole methodRole = handlerMethod.getMethodAnnotation(RequiresRole.class);
        
        RequiresPermission classPermission = handlerMethod.getBeanType().getAnnotation(RequiresPermission.class);
        RequiresPermission methodPermission = handlerMethod.getMethodAnnotation(RequiresPermission.class);
        
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }
        
        Long userId = currentUser.getId();
        
        if (classRole != null || methodRole != null) {
            if (!checkRoles(userId, classRole, methodRole)) {
                response.setStatus(403);
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
                return false;
            }
        }
        
        if (classPermission != null || methodPermission != null) {
            if (!checkPermissions(userId, classPermission, methodPermission)) {
                response.setStatus(403);
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
                return false;
            }
        }
        
        return true;
    }
    
    private boolean checkRoles(Long userId, RequiresRole classRole, RequiresRole methodRole) {
        if (classRole == null && methodRole == null) {
            return true;
        }
        
        RequiresRole effectiveRole = methodRole != null ? methodRole : classRole;
        RequiresRole.Logical logical = effectiveRole.logical();
        String[] roles = effectiveRole.value();
        
        if (logical == RequiresRole.Logical.OR) {
            return permissionService.hasAnyRole(userId, roles);
        } else {
            for (String role : roles) {
                if (!permissionService.hasRole(userId, role)) {
                    return false;
                }
            }
            return true;
        }
    }
    
    private boolean checkPermissions(Long userId, RequiresPermission classPermission, RequiresPermission methodPermission) {
        if (classPermission == null && methodPermission == null) {
            return true;
        }
        
        RequiresPermission effectivePermission = methodPermission != null ? methodPermission : classPermission;
        RequiresPermission.Logical logical = effectivePermission.logical();
        String[] permissions = effectivePermission.value();
        
        if (logical == RequiresPermission.Logical.OR) {
            return permissionService.hasAnyPermission(userId, permissions);
        } else {
            for (String permission : permissions) {
                if (!permissionService.hasPermission(userId, permission)) {
                    return false;
                }
            }
            return true;
        }
    }
}
