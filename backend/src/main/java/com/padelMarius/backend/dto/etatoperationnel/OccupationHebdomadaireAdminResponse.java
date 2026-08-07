package com.padelMarius.backend.dto.etatoperationnel;

import java.time.LocalDate;
import java.util.List;

public record OccupationHebdomadaireAdminResponse(
        LocalDate dateDebut,
        LocalDate dateFin,
        Long siteId,
        String nomSite,
        boolean siteActif,
        List<EtatOperationnelAdminResponse> jours
) {
}
