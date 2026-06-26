package com.changeowl.artifactgatewayservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class HttpRequestLogging extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestLogging.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.equals("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        String sessionId = request.getHeader("X-Session-ID");

        if (sessionId != null) {
            MDC.put("session_id", sessionId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;

            log.info("http.request.completed | Method: {} | Path: {} | Status: {} | Duration: {}ms",
                    request.getMethod(),
                    path,
                    response.getStatus(),
                    durationMs
            );

            MDC.clear();
        }
    }
}