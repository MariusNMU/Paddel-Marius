package com.padelMarius.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> ENDPOINTS_LIMITES = Set.of(
            "/api/auth/joueur",
            "/api/auth/admin",
            "/api/auth/refresh"
    );

    private final LimiteurTentativesAuthentification limiteur;
    private final SecurityErrorWriter securityErrorWriter;

    public AuthRateLimitFilter(
            LimiteurTentativesAuthentification limiteur,
            SecurityErrorWriter securityErrorWriter
    ) {
        this.limiteur = limiteur;
        this.securityErrorWriter = securityErrorWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.POST.matches(request.getMethod())
                || !ENDPOINTS_LIMITES.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        LimiteurTentativesAuthentification.Decision decision =
                limiteur.autoriser(
                        request.getRemoteAddr(),
                        request.getRequestURI()
                );

        if (decision.autorise()) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setHeader(
                HttpHeaders.RETRY_AFTER,
                String.valueOf(decision.secondesAvantNouvelEssai())
        );
        securityErrorWriter.write(
                response,
                HttpStatus.TOO_MANY_REQUESTS,
                "TROP_DE_TENTATIVES",
                "Trop de tentatives. Réessayez plus tard."
        );
    }
}
