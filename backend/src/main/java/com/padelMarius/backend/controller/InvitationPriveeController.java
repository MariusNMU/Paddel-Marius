package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.invitation.DeclinerInvitationRequest;
import com.padelMarius.backend.dto.invitation.InvitationPriveeResponse;
import com.padelMarius.backend.dto.invitation.InviterJoueurPriveRequest;
import com.padelMarius.backend.service.InvitationPriveeService;
import com.padelMarius.backend.service.JoueurAuthorizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InvitationPriveeController {

    private final InvitationPriveeService invitationPriveeService;
    private final JoueurAuthorizationService joueurAuthorizationService;

    @PostMapping("/api/matches/{matchId}/invitations/privees")
    public ResponseEntity<InvitationPriveeResponse> inviterJoueur(
            @RequestHeader(
                    name = "Authorization",
                    required = false
            )
            String authorization,

            @PathVariable
            Long matchId,

            @Valid
            @RequestBody
            InviterJoueurPriveRequest request
    ) {
        joueurAuthorizationService.verifierOrganisateurDuMatch(
                authorization,
                matchId
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        invitationPriveeService
                                .inviterJoueur(matchId, request)
                );
    }

    @GetMapping("/api/membres/{matricule}/invitations/recues")
    public ResponseEntity<List<InvitationPriveeResponse>>
    listerInvitationsRecues(
            @RequestHeader(
                    name = "Authorization",
                    required = false
            )
            String authorization,

            @PathVariable
            String matricule
    ) {
        joueurAuthorizationService.verifierAccesMatricule(
                authorization,
                matricule
        );

        return ResponseEntity.ok(
                invitationPriveeService.listerInvitationsRecues(matricule)
        );
    }

    @GetMapping("/api/membres/{matricule}/invitations/recues/count")
    public ResponseEntity<Integer> compterInvitationsRecues(
            @RequestHeader(
                    name = "Authorization",
                    required = false
            )
            String authorization,

            @PathVariable
            String matricule
    ) {
        joueurAuthorizationService.verifierAccesMatricule(
                authorization,
                matricule
        );

        return ResponseEntity.ok(
                invitationPriveeService.compterInvitationsRecues(matricule)
        );
    }

    @PostMapping("/api/participations/{participationId}/decliner")
    public ResponseEntity<InvitationPriveeResponse> declinerInvitation(
            @RequestHeader(
                    name = "Authorization",
                    required = false
            )
            String authorization,

            @PathVariable
            Long participationId,

            @Valid
            @RequestBody
            DeclinerInvitationRequest request
    ) {
        joueurAuthorizationService
                .verifierParticipationDuJoueur(
                        authorization,
                        participationId
                );

        return ResponseEntity.ok(
                invitationPriveeService
                        .declinerInvitation(participationId, request)
        );
    }
}
