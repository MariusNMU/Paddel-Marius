package com.padelMarius.backend.dto.auth;

import com.padelMarius.backend.entity.RoleAdministrateur;

import java.time.LocalDateTime;

public record AuthAdminResponse(
        Long administrateurId,
        String login,
        String nom,
        String prenom,
        RoleAdministrateur roleAdministrateur,
        Long siteId,
        String nomSite,
        boolean actif,
        String token,
        LocalDateTime expirationToken
) {
    public AuthAdminResponse(
            Long administrateurId,
            String login,
            String nom,
            String prenom,
            RoleAdministrateur roleAdministrateur,
            Long siteId,
            String nomSite,
            boolean actif
    ) {
        this(
                administrateurId,
                login,
                nom,
                prenom,
                roleAdministrateur,
                siteId,
                nomSite,
                actif,
                null,
                null
        );
    }
}