package com.renewable.ai.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.renewable.ai.entity.User;
import com.renewable.ai.util.SecurityUtil;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        
        String uri = request.getRequestURI();
        if ("GET".equals(request.getMethod()) && uri != null && uri.endsWith("/api/fleets") && !uri.contains("/api/fleets/")) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }

        String actualToken = token.substring(7);
        if (actualToken.isEmpty()) {
            response.setStatus(401);
            return false;
        }

        Long userId = SecurityUtil.getUserIdByToken(actualToken);
        if (userId == null) {
            response.setStatus(401);
            return false;
        }

        request.setAttribute("userId", userId);
        
        User user = SecurityUtil.getUserByToken(actualToken);
        if (user != null) {
            SecurityUtil.setCurrentUser(user);
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        SecurityUtil.clearCurrentUser();
    }
}
