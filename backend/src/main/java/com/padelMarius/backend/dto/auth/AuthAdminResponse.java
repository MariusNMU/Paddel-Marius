package com.padelMarius.backend.dto.auth;

import com.padelMarius.backend.entity.RoleAdministrateur;

public record AuthAdminResponse(
        Long administrateurId,
        String login,
        String nom,
        String prenom,
        RoleAdministrateur roleAdministrateur,
        Long siteId,
        String nomSite,
        boolean actif
) {
}