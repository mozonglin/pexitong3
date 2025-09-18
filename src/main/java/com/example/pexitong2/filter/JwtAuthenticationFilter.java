package com.example.pexitong2.filter;

import com.example.pexitong2.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // 跳过公开接口的JWT验证
        String requestPath = request.getRequestURI();
        System.out.println("JWT Filter - Request Path: " + requestPath);
        System.out.println("JWT Filter - Is Public Path: " + isPublicPath(requestPath));
        
        if (isPublicPath(requestPath)) {
            System.out.println("JWT Filter - Skipping JWT validation for public path: " + requestPath);
            filterChain.doFilter(request, response);
            return;
        }
        
        String authorizationHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null;
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Token解析失败，继续处理
            }
        }
        
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt, username)) {
                String userType = jwtUtil.extractUserType(jwt);
                String userId = jwtUtil.extractUserId(jwt);
                
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(userType);
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));
                
                // 将用户ID添加到认证对象的详细信息中
                authToken.setDetails(userId);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 判断是否为公开路径
     */
    private boolean isPublicPath(String path) {
        String[] publicPaths = {
            "/auth/send-verification-code",
            "/auth/register", 
            "/auth/login",
            "/security/captcha",
            "/test/health",
            "/test/jwt",
            "/test/password",
                "/api/debug/check-role",
                "/api/debug/check-approval-status",
                "/api/debug/check-users1-info",

        };
        
        for (String publicPath : publicPaths) {
            if (path.equals(publicPath)) {
                return true;
            }
        }
        return false;
    }
} 