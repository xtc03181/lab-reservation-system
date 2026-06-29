package com.gltu.labreservation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gltu.labreservation.common.ApiResponse;
import com.gltu.labreservation.common.TokenStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "DELETE");
    private final ObjectMapper objectMapper;
    private final TokenStore tokenStore;

    public RoleInterceptor(ObjectMapper objectMapper, TokenStore tokenStore) {
        this.objectMapper = objectMapper;
        this.tokenStore = tokenStore;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || request.getRequestURI().contains("/auth/")) {
            return true;
        }

        TokenStore.LoginUser loginUser = getLoginUser(request);
        if (loginUser == null) {
            writeForbidden(response, "请先登录");
            return false;
        }
        if (request.getHeader("X-User-Id") != null
                && !request.getHeader("X-User-Id").equals(String.valueOf(loginUser.userId()))) {
            writeForbidden(response, "登录身份与请求用户不一致");
            return false;
        }
        if (request.getHeader("X-User-Role") != null
                && !request.getHeader("X-User-Role").equalsIgnoreCase(loginUser.role())) {
            writeForbidden(response, "登录角色与请求角色不一致");
            return false;
        }

        if (isAllowed(request, loginUser.role().toUpperCase())) {
            return true;
        }

        writeForbidden(response, "当前角色无权执行此操作");
        return false;
    }

    private TokenStore.LoginUser getLoginUser(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return tokenStore.get(authorization.substring(7));
    }

    private boolean isAllowed(HttpServletRequest request, String role) {
        String path = request.getRequestURI();
        String method = request.getMethod().toUpperCase();

        if ("ADMIN".equals(role)) {
            return true;
        }

        if (path.contains("/users/me")) {
            return true;
        }
        if (path.equals("/api/users") && "GET".equals(method)) {
            return "TEACHER".equals(role);
        }
        if (path.contains("/users")) {
            return false;
        }
        if (path.contains("/operation-logs")) {
            return false;
        }
        if (path.contains("/messages")) {
            return true;
        }
        if (path.contains("/reservation-rules")) {
            return "GET".equals(method);
        }

        if (path.contains("/lab-reservations")) {
            if ("GET".equals(method)) {
                return true;
            }
            if ("POST".equals(method)) {
                return "STUDENT".equals(role);
            }
            if ("PUT".equals(method) && path.contains("/review")) {
                return "TEACHER".equals(role);
            }
            if ("PUT".equals(method) && path.contains("/cancel")) {
                return "STUDENT".equals(role);
            }
            if ("PUT".equals(method)) {
                return "STUDENT".equals(role);
            }
        }

        if (path.contains("/equipment-borrows")) {
            if ("GET".equals(method)) {
                return true;
            }
            if ("POST".equals(method)) {
                return "STUDENT".equals(role);
            }
            if ("PUT".equals(method) && path.contains("/review")) {
                return "TEACHER".equals(role);
            }
        }

        if (path.contains("/labs") || path.contains("/equipment") || path.contains("/notices")) {
            return !WRITE_METHODS.contains(method);
        }

        return false;
    }

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(message)));
    }
}
