package com.padelMarius.backend.security;

public record JwtUtilisateur(
        String sujet,
        String typeUtilisateur,
        String role,
        Long siteId
) {
}