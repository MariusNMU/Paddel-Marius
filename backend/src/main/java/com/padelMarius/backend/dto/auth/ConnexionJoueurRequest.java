package com.padelMarius.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ConnexionJoueurRequest(
        @NotBlank(message = "Le matricule est obligatoire.")
        String matricule
) {
}