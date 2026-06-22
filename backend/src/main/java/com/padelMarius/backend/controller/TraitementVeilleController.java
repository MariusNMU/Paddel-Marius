package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.traitement.TraitementVeilleResponse;
import com.padelMarius.backend.service.AdminAuthorizationService;
import com.padelMarius.backend.service.TraitementVeilleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class TraitementVeilleController {

    private final TraitementVeilleService traitementVeilleService;
    private final AdminAuthorizationService adminAuthorizationService;

    @PostMapping("/api/admin/matches/traitement-veille")
    public ResponseEntity<TraitementVeilleResponse> traiterVeille(
            @RequestHeader(name = "Authorization", required = false)
            String authorization,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        adminAuthorizationService.verifierAdminGlobal(authorization);

        TraitementVeilleResponse response =
                traitementVeilleService.traiterVeille(date);

        return ResponseEntity.ok(response);
    }
}
