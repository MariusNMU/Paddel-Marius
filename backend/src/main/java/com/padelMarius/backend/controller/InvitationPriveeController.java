package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.invitation.DeclinerInvitationRequest;
import com.padelMarius.backend.dto.invitation.InvitationPriveeResponse;
import com.padelMarius.backend.dto.invitation.InviterJoueurPriveRequest;
import com.padelMarius.backend.service.InvitationPriveeService;
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

    @PostMapping("/api/matches/{matchId}/invitations/privees")
    public ResponseEntity<InvitationPriveeResponse> inviterJoueur(
            @PathVariable Long matchId,
            @Valid @RequestBody InviterJoueurPriveRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(invitationPriveeService.inviterJoueur(matchId, request));
    }

    @GetMapping("/api/membres/{matricule}/invitations/recues")
    public ResponseEntity<List<InvitationPriveeResponse>> listerInvitationsRecues(
            @PathVariable String matricule
    ) {
        return ResponseEntity.ok(
                invitationPriveeService.listerInvitationsRecues(matricule)
        );
    }

    @GetMapping("/api/membres/{matricule}/invitations/recues/count")
    public ResponseEntity<Integer> compterInvitationsRecues(
            @PathVariable String matricule
    ) {
        return ResponseEntity.ok(
                invitationPriveeService.compterInvitationsRecues(matricule)
        );
    }

    @PostMapping("/api/participations/{participationId}/decliner")
    public ResponseEntity<InvitationPriveeResponse> declinerInvitation(
            @PathVariable Long participationId,
            @Valid @RequestBody DeclinerInvitationRequest request
    ) {
        return ResponseEntity.ok(
                invitationPriveeService.declinerInvitation(participationId, request)
        );
    }
}