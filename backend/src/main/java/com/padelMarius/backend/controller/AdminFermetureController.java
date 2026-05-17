package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.fermeture.CreerFermetureRequest;
import com.padelMarius.backend.dto.fermeture.FermetureAdminResponse;
import com.padelMarius.backend.service.AdminAuthorizationService;
import com.padelMarius.backend.service.AdminFermetureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/fermetures")
@RequiredArgsConstructor
public class AdminFermetureController {

    private final AdminFermetureService adminFermetureService;
    private final AdminAuthorizationService adminAuthorizationService;

    @PostMapping
    public ResponseEntity<FermetureAdminResponse> creerFermeture(
            @RequestHeader(name = "X-Admin-Login", required = false)
            String adminLogin,

            @Valid @RequestBody
            CreerFermetureRequest request
    ) {
        adminAuthorizationService.verifierAccesFermeture(
                adminLogin,
                request.portee(),
                request.siteId()
        );

        FermetureAdminResponse response = adminFermetureService.creerFermeture(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}