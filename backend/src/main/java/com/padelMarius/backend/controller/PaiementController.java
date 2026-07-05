package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.paiement.HistoriquePaiementResponse;
import com.padelMarius.backend.dto.paiement.PaiementResponse;
import com.padelMarius.backend.dto.paiement.PayerParticipationRequest;
import com.padelMarius.backend.service.JoueurAuthorizationService;
import com.padelMarius.backend.service.PaiementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;
    private final JoueurAuthorizationService joueurAuthorizationService;

    @PostMapping("/api/participations/{participationId}/paiements/standard")
    public ResponseEntity<PaiementResponse> payerParticipationStandard(
            @RequestHeader(
                    name = "Authorization",
                    required = false
            )
            String authorization,

            @PathVariable
            Long participationId
    ) {
        joueurAuthorizationService
                .verifierParticipationDuJoueur(
                        authorization,
                        participationId
                );

        PaiementResponse response =
                paiementService.payerParticipationStandard(participationId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/api/participations/{participationId}/paiements")
    public ResponseEntity<PaiementResponse> payerParticipation(
            @RequestHeader(
                    name = "Authorization",
                    required = false
            )
            String authorization,

            @PathVariable
            Long participationId,

            @Valid
            @RequestBody
            PayerParticipationRequest request
    ) {
        joueurAuthorizationService
                .verifierParticipationDuJoueur(
                        authorization,
                        participationId
                );

        PaiementResponse response =
                paiementService.payerParticipation(
                        participationId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/api/membres/{matricule}/paiements")
    public ResponseEntity<List<HistoriquePaiementResponse>>
    consulterHistoriquePaiements(
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
                paiementService
                        .consulterHistoriquePaiements(matricule)
        );
    }
}
