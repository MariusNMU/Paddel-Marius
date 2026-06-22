package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.disponibilite.DisponibilitesResponse;
import com.padelMarius.backend.service.DisponibiliteService;
import com.padelMarius.backend.service.JoueurAuthorizationService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/disponibilites")
@RequiredArgsConstructor
@Validated
public class DisponibiliteController {

    private final DisponibiliteService disponibiliteService;
    private final JoueurAuthorizationService joueurAuthorizationService;

    @GetMapping
    public DisponibilitesResponse consulterDisponibilites(
            @RequestHeader(
                    name = "Authorization",
                    required = false
            )
            String authorization,

            @RequestParam
            @Positive
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

        return disponibiliteService.consulterDisponibilites(
                siteId,
                date
        );
    }
}
