package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.reservation.ReservationJoueurResponse;
import com.padelMarius.backend.service.JoueurAuthorizationService;
import com.padelMarius.backend.service.MembreReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membres")
@RequiredArgsConstructor
public class MembreReservationController {

    private final MembreReservationService membreReservationService;
    private final JoueurAuthorizationService joueurAuthorizationService;

    @GetMapping("/{matricule}/reservations")
    public ResponseEntity<List<ReservationJoueurResponse>>
    consulterReservations(
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
                membreReservationService
                        .consulterReservations(matricule)
        );
    }
}
