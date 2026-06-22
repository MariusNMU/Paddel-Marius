package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.membre.InscriptionMembreRequest;
import com.padelMarius.backend.dto.membre.MembreResponse;
import com.padelMarius.backend.dto.membre.SoldeJoueurResponse;
import com.padelMarius.backend.service.JoueurAuthorizationService;
import com.padelMarius.backend.service.MembreInscriptionService;
import com.padelMarius.backend.service.MembreSoldeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/membres")
@RequiredArgsConstructor
public class MembreController {

    private final MembreInscriptionService membreInscriptionService;
    private final MembreSoldeService membreSoldeService;
    private final JoueurAuthorizationService joueurAuthorizationService;

    @PostMapping("/inscription")
    public ResponseEntity<MembreResponse> inscrireMembre(
            @Valid @RequestBody InscriptionMembreRequest request
    ) {
        MembreResponse response = membreInscriptionService.inscrireMembre(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{matricule}/solde")
    public ResponseEntity<SoldeJoueurResponse> consulterSolde(
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
                membreSoldeService.consulterSolde(matricule)
        );
    }
}
