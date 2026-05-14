package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.traitement.TraitementEcheanceResponse;
import com.padelMarius.backend.service.TraitementEcheanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TraitementEcheanceController {

    private final TraitementEcheanceService traitementEcheanceService;

    @PostMapping("/api/admin/matches/traitement-echeance")
    public ResponseEntity<TraitementEcheanceResponse> traiterMatchesArrivesAEcheance() {
        return ResponseEntity.ok(
                traitementEcheanceService.traiterMatchesArrivesAEcheance()
        );
    }
}