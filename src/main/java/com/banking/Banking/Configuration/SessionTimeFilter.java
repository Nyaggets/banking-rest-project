package com.banking.Banking.Configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SessionTimeFilter extends OncePerRequestFilter {
    private static final long MAX_SESSION_DURATION_MS = 5 * 60 * 1000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            long creationTime = session.getCreationTime();
            if (System.currentTimeMillis() - creationTime > MAX_SESSION_DURATION_MS) {
                session.invalidate();
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Сессия истекла");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}