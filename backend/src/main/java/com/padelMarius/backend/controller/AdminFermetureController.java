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
            @RequestHeader(name = "Authorization", required = false)
            String authorization,

            @Valid @RequestBody
            CreerFermetureRequest request
    ) {
        adminAuthorizationService.verifierAccesFermeture(
                authorization,
                request.portee(),
                request.siteId()
        );

        FermetureAdminResponse response =
                adminFermetureService.creerFermeture(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
