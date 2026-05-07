package com.padelMarius.backend.dto.participation;

import com.padelMarius.backend.entity.ModeEntreeParticipation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutParticipation;

import java.time.LocalDateTime;

public record ParticipationResponse(
        Long participationId,
        Long matchId,
        Long membreId,
        String matriculeJoueur,
        RoleParticipation roleParticipation,
        ModeEntreeParticipation modeEntree,
        StatutParticipation statutParticipation,
        LocalDateTime dateAffectation
) {
}