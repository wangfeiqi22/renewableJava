package com.renewable.ai.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.renewable.ai.util.SecurityUtil;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        // 注册页“所属车队”下拉需要车队列表，未登录也可访问（仅 GET 列表，不含 /api/fleets/xxx）
        String uri = request.getRequestURI();
        if ("GET".equals(request.getMethod()) && uri != null && uri.endsWith("/api/fleets") && !uri.contains("/api/fleets/")) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }

        // In a real app, validate token with DB or JWT parser
        // For MVP, we bind token -> userId on login and validate against it.
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
        return true;
    }
}
