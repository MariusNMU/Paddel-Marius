package com.padelMarius.backend.dto.admin;

import com.padelMarius.backend.entity.CategorieMembre;

import java.math.BigDecimal;

public record MembreAdminResponse(
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