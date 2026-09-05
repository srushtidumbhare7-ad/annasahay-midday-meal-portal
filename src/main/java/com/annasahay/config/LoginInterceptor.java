package com.annasahay.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        // Allow login, logout, check status, and static pages
        if (path.equals("/api/auth/login") || path.equals("/api/auth/logout") || path.equals("/api/auth/check")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("admin") != null) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Unauthorized: Please login to access this resource\"}");
        return false;
    }
}
