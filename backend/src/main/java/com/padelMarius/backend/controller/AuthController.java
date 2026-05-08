package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.auth.AuthAdminResponse;
import com.padelMarius.backend.dto.auth.AuthJoueurResponse;
import com.padelMarius.backend.dto.auth.ConnexionAdminRequest;
import com.padelMarius.backend.dto.auth.ConnexionJoueurRequest;
import com.padelMarius.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/auth/joueur")
    public ResponseEntity<AuthJoueurResponse> authentifierJoueur(
            @Valid @RequestBody ConnexionJoueurRequest request
    ) {
        AuthJoueurResponse response = authService.authentifierJoueur(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/auth/admin")
    public ResponseEntity<AuthAdminResponse> authentifierAdmin(
            @Valid @RequestBody ConnexionAdminRequest request
    ) {
        AuthAdminResponse response = authService.authentifierAdmin(request);
        return ResponseEntity.ok(response);
    }
}