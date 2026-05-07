package com.padelMarius.backend.dto.match;

import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.VisibiliteMatch;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MatchResponse(
        Long matchId,
        Long terrainId,
        Long siteId,
        String matriculeOrganisateur,
        LocalDateTime dateHeureDebut,
        LocalDateTime dateHeureFin,
        ModeCreation modeCreation,
        VisibiliteMatch visibiliteCourante,
        BigDecimal prixTotal,
        EtatCycleMatch etatCycle,
        Long participationOrganisateurId
) {
}