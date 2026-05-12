package com.padelMarius.backend.dto.reservation;

import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.ModeEntreeParticipation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.VisibiliteMatch;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationJoueurResponse(
        Long participationId,
        Long matchId,
        Long siteId,
        String nomSite,
        Long terrainId,
        String numeroTerrain,
        LocalDateTime dateHeureDebut,
        LocalDateTime dateHeureFin,
        RoleParticipation roleParticipation,
        ModeEntreeParticipation modeEntree,
        StatutParticipation statutParticipation,
        ModeCreation modeCreation,
        VisibiliteMatch visibiliteCourante,
        EtatCycleMatch etatCycle,
        BigDecimal prixTotal
) {
}