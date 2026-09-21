package com.ipagency.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final com.ipagency.mapper.SysUserMapper users;

    public JwtAuthInterceptor(JwtUtil jwtUtil, ObjectMapper objectMapper, com.ipagency.mapper.SysUserMapper users) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.users = users;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        CurrentUserContext.clear();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "缺少有效的登录凭证");
            return false;
        }
        try {
            var identity = jwtUtil.parse(authorization.substring(7));
            var user = users.selectById(identity.userId());
            if (user == null || !Integer.valueOf(1).equals(user.getStatus()) || !user.getRole().equals(identity.role())) throw new IllegalArgumentException("Inactive identity");
            CurrentUserContext.set(identity);
            String path = request.getRequestURI();
            String required = path.startsWith("/api/admin/") ? "ADMIN" : path.startsWith("/api/client/") ? "CLIENT" : path.startsWith("/api/agent/") ? "AGENT" : null;
            if (required != null && !required.equals(identity.role())) {
                CurrentUserContext.clear();
                response.setStatus(403);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getWriter(), ApiResponse.error("角色权限不足"));
                return false;
            }
            return true;
        } catch (Exception exception) {
            CurrentUserContext.clear();
            writeUnauthorized(response, "登录凭证无效或已过期");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }
}
