package com.padelMarius.backend.dto.auth;

import com.padelMarius.backend.entity.CategorieMembre;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuthJoueurResponse(
        Long membreId,
        String matricule,
        String nom,
        String prenom,
        CategorieMembre categorieMembre,
        Long siteRattachementId,
        String nomSiteRattachement,
        boolean actif,
        BigDecimal soldeCredit,
        String token,
        LocalDateTime expirationToken
) {
    public AuthJoueurResponse(
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
        this(
                membreId,
                matricule,
                nom,
                prenom,
                categorieMembre,
                siteRattachementId,
                nomSiteRattachement,
                actif,
                soldeCredit,
                null,
                null
        );
    }
}