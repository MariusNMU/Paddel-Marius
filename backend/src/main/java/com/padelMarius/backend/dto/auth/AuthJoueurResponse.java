package com.padelMarius.backend.dto.auth;

import com.padelMarius.backend.entity.CategorieMembre;

import java.math.BigDecimal;

public record AuthJoueurResponse(
        Long membreId,
        String matricule,
        String nom,
        String prenom,
        CategorieMembre categorieMembre,
        Long siteRattachementId,
        String nomSiteRattachement,
        boolean actif,
        BigDecimal soldeCredit
) {
}