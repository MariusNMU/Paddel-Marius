package com.padelMarius.backend.dto.statistique;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StatistiquesAdminResponse(
        LocalDate dateDebut,
        LocalDate dateFin,
        Long siteId,
        String nomSite,
        long nombreMatches,
        long nombreMatchesAVenir,
        long nombreMatchesTermines,
        long nombrePaiements,
        BigDecimal chiffreAffaires,
        long nombreDettesOuvertes,
        BigDecimal montantDettesOuvertes,
        long nombreParticipationsActives,
        long capaciteTheoriqueJoueurs,
        BigDecimal tauxRemplissage
) {
}