package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.paiement.HistoriquePaiementResponse;
import com.padelMarius.backend.dto.paiement.PaiementResponse;
import com.padelMarius.backend.dto.paiement.PayerParticipationRequest;
import com.padelMarius.backend.service.PaiementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    @PostMapping("/api/participations/{participationId}/paiements")
    public ResponseEntity<PaiementResponse> payerParticipation(
            @PathVariable Long participationId,
            @Valid @RequestBody PayerParticipationRequest request
    ) {
        PaiementResponse response = paiementService.payerParticipation(participationId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/api/membres/{matricule}/paiements")
    public ResponseEntity<List<HistoriquePaiementResponse>> consulterHistoriquePaiements(
            @PathVariable String matricule
    ) {
        return ResponseEntity.ok(
                paiementService.consulterHistoriquePaiements(matricule)
        );
    }
}
