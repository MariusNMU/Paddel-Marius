package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.membre.InscriptionMembreRequest;
import com.padelMarius.backend.dto.membre.MembreResponse;
import com.padelMarius.backend.service.MembreInscriptionService;
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

    @PostMapping("/inscription")
    public ResponseEntity<MembreResponse> inscrireMembre(
            @Valid @RequestBody InscriptionMembreRequest request
    ) {
        MembreResponse response = membreInscriptionService.inscrireMembre(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}