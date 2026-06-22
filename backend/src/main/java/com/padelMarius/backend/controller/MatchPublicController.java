package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.matchpublic.MatchPublicResponse;
import com.padelMarius.backend.dto.matchpublic.RejoindreMatchPublicRequest;
import com.padelMarius.backend.dto.matchpublic.RejoindreMatchPublicResponse;
import com.padelMarius.backend.service.JoueurAuthorizationService;
import com.padelMarius.backend.service.MatchPublicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchPublicController {

    private final MatchPublicService matchPublicService;
    private final JoueurAuthorizationService joueurAuthorizationService;

    @GetMapping("/publics")
    public ResponseEntity<List<MatchPublicResponse>>
    listerMatchesPublics(
            @RequestHeader(
                    name = "Authorization",
                    required = false
            )
            String authorization,

            @RequestParam
            Long siteId,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date
    ) {
        joueurAuthorizationService.verifierJoueurConnecte(
                authorization
        );

        return ResponseEntity.ok(
                matchPublicService
                        .listerMatchesPublicsDisponibles(
                                siteId,
                                date
                        )
        );
    }

    @PostMapping("/{matchId}/participants/public/payer")
    public ResponseEntity<RejoindreMatchPublicResponse>
    rejoindreEtPayer(
            @RequestHeader(
                    name = "Authorization",
                    required = false
            )
            String authorization,

            @PathVariable
            Long matchId,

            @Valid
            @RequestBody
            RejoindreMatchPublicRequest request
    ) {
        joueurAuthorizationService.verifierAccesMatricule(
                authorization,
                request.matriculeJoueur()
        );

        RejoindreMatchPublicResponse response =
                matchPublicService.rejoindreEtPayer(
                        matchId,
                        request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
