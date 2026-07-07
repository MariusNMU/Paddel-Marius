package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.reservation.ReservationJoueurResponse;
import com.padelMarius.backend.service.MembreReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membres")
@RequiredArgsConstructor
public class MembreReservationController {

    private final MembreReservationService membreReservationService;

    @GetMapping("/{matricule}/reservations")
    @PreAuthorize("@joueurAuthorizationService.peutAgirPourMatricule(authentication, #matricule)")
    public ResponseEntity<List<ReservationJoueurResponse>>
    consulterReservations(
            @PathVariable
            String matricule
    ) {
        return ResponseEntity.ok(
                membreReservationService
                        .consulterReservations(matricule)
        );
    }
}
