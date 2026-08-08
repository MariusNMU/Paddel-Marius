package com.padelMarius.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConnexionJoueurRequest(
        @NotBlank(message = "Le matricule est obligatoire.")
        @Size(
                max = 10,
                message = "Le matricule ne peut pas dépasser 10 caractères."
        )
        String matricule,

        @NotBlank(message = "Le mot de passe est obligatoire.")
        @Size(
                max = 72,
                message = "Le mot de passe ne peut pas dépasser 72 caractères."
        )
        String motDePasse
) {
}
