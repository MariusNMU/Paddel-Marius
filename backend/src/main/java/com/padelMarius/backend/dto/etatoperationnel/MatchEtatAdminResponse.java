package com.padelMarius.backend.dto.etatoperationnel;

import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.VisibiliteMatch;

import java.time.LocalDateTime;

public record MatchEtatAdminResponse(
        Long matchId,
        LocalDateTime dateHeureDebut,
        LocalDateTime dateHeureFin,
        VisibiliteMatch visibiliteCourante,
        EtatCycleMatch etatCycle,
        long nombreParticipants
) {
}
