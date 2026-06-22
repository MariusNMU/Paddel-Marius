package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.participation.AjouterParticipantPriveRequest;
import com.padelMarius.backend.dto.participation.InscriptionPubliqueRequest;
import com.padelMarius.backend.dto.participation.ParticipationResponse;
import com.padelMarius.backend.service.JoueurAuthorizationService;
import com.padelMarius.backend.service.ParticipationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches/{matchId}/participants")
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipationService participationService;
    private final JoueurAuthorizationService joueurAuthorizationService;

    @PostMapping("/prive")
    public ResponseEntity<ParticipationResponse> ajouterParticipantPrive(
            @RequestHeader(
                    name = "Authorization",
                    required = false
            )
            String authorization,

            @PathVariable
            Long matchId,

            @Valid
            @RequestBody
            AjouterParticipantPriveRequest request
    ) {
        joueurAuthorizationService.verifierOrganisateurDuMatch(
                authorization,
                matchId
        );

        ParticipationResponse response =
                participationService.ajouterParticipantPrive(
                        matchId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/public")
    public ResponseEntity<ParticipationResponse> inscrireParticipantPublic(
            @RequestHeader(
                    name = "Authorization",
                    required = false
            )
            String authorization,

            @PathVariable
            Long matchId,

            @Valid
            @RequestBody
            InscriptionPubliqueRequest request
    ) {
        joueurAuthorizationService.verifierAccesMatricule(
                authorization,
                request.matriculeJoueur()
        );

        ParticipationResponse response =
                participationService.inscrireParticipantPublic(
                        matchId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
