package com.padelMarius.backend.security;

import org.springframework.security.core.AuthenticatedPrincipal;

public record JwtUtilisateur(
        String sujet,
        String typeUtilisateur
) implements AuthenticatedPrincipal {

    @Override
    public String getName() {
        return sujet;
    }
}
