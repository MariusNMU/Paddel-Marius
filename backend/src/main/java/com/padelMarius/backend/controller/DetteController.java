package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.dette.DetteResponse;
import com.padelMarius.backend.dto.dette.PaiementDetteResponse;
import com.padelMarius.backend.dto.dette.PayerDetteRequest;
import com.padelMarius.backend.service.DetteService;
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
public class DetteController {

    private final DetteService detteService;

    @PostMapping("/api/matches/{matchId}/dettes/generer")
    public ResponseEntity<DetteResponse> genererDettePourMatch(@PathVariable Long matchId) {
        DetteResponse response = detteService.genererDettePourMatch(matchId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/api/membres/{matricule}/dettes/ouvertes")
    public ResponseEntity<List<DetteResponse>> consulterDettesOuvertes(@PathVariable String matricule) {
        List<DetteResponse> response = detteService.consulterDettesOuvertes(matricule);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/dettes/{detteId}/paiements")
    public ResponseEntity<PaiementDetteResponse> payerDette(
            @PathVariable Long detteId,
            @Valid @RequestBody PayerDetteRequest request
    ) {
        PaiementDetteResponse response = detteService.payerDette(detteId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}