package com.padelMarius.backend.dto.invitation;

import com.padelMarius.backend.entity.StatutParticipation;

import java.time.LocalDateTime;

public record InvitationPriveeResponse(
        Long participationId,
        Long matchId,
        Long siteId,
        String nomSite,
        Long terrainId,
        String numeroTerrain,
        LocalDateTime dateHeureDebut,
        LocalDateTime dateHeureFin,

        Long organisateurId,
        String matriculeOrganisateur,
        String nomOrganisateur,
        String prenomOrganisateur,

        Long joueurInviteId,
        String matriculeInvite,
        String nomInvite,
        String prenomInvite,

        StatutParticipation statutParticipation
) {
}