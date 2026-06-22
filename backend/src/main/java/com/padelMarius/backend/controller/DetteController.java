package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.dette.DetteResponse;
import com.padelMarius.backend.dto.dette.PaiementDetteResponse;
import com.padelMarius.backend.dto.dette.PayerDetteRequest;
import com.padelMarius.backend.service.DetteService;
import com.padelMarius.backend.service.JoueurAuthorizationService;
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
public class DetteController {

    private final DetteService detteService;
    private final JoueurAuthorizationService joueurAuthorizationService;

    @GetMapping("/api/membres/{matricule}/dettes/ouvertes")
    public ResponseEntity<List<DetteResponse>> consulterDettesOuvertes(
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
                detteService.consulterDettesOuvertes(matricule)
        );
    }

    @PostMapping("/api/dettes/{detteId}/paiements")
    public ResponseEntity<PaiementDetteResponse> payerDette(
            @RequestHeader(
                    name = "Authorization",
                    required = false
            )
            String authorization,

            @PathVariable
            Long detteId,

            @Valid
            @RequestBody
            PayerDetteRequest request
    ) {
        joueurAuthorizationService.verifierDetteDuJoueur(
                authorization,
                detteId
        );

        PaiementDetteResponse response =
                detteService.payerDette(detteId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}