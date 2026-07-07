package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.disponibilite.DisponibilitesResponse;
import com.padelMarius.backend.service.DisponibiliteService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping
    @PreAuthorize("@joueurAuthorizationService.estJoueurActif(authentication)")
    public DisponibilitesResponse consulterDisponibilites(
            @RequestParam
            @Positive
            Long siteId,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date
    ) {
        return disponibiliteService.consulterDisponibilites(
                siteId,
                date
        );
    }
}
