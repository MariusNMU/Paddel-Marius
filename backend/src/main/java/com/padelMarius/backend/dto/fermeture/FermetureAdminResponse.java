package com.padelMarius.backend.dto.fermeture;

import com.padelMarius.backend.entity.PorteeFermeture;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FermetureAdminResponse(
        Long fermetureId,
        LocalDate dateFermeture,
        PorteeFermeture portee,
        Long siteId,
        String nomSite,
        String motif,
        int nombreMatchesAnnules,
        int nombreRemboursementsCredites,
        BigDecimal montantTotalRembourse
) {
}