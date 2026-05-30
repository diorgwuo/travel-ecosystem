package com.travel.travelecosystem.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public final class PublicApiPaths {

    public static final List<String> PUBLIC_PATH_FRAGMENTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/swagger-ui",
            "/api-docs",
            "/v3/api-docs"
    );

    private PublicApiPaths() {
    }

    public static boolean isPublicRequest(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String requestUri = request.getRequestURI();
        return PUBLIC_PATH_FRAGMENTS.stream().anyMatch(requestUri::contains);
    }
}
