package com.padelMarius.backend.security;

import org.springframework.security.core.AuthenticatedPrincipal;

public record JwtUtilisateur(
        String sujet,
        String typeUtilisateur,
        String identifiantToken
) implements AuthenticatedPrincipal {

    public JwtUtilisateur(
            String sujet,
            String typeUtilisateur
    ) {
        this(sujet, typeUtilisateur, null);
    }

    @Override
    public String getName() {
        return sujet;
    }
}
