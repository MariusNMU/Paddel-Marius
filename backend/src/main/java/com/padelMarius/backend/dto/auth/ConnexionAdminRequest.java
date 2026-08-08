package com.padelMarius.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConnexionAdminRequest(
        @NotBlank(message = "Le login administrateur est obligatoire.")
        @Size(
                max = 150,
                message = "Le login ne peut pas dépasser 150 caractères."
        )
        String login,

        @NotBlank(message = "Le mot de passe administrateur est obligatoire.")
        @Size(
                max = 72,
                message = "Le mot de passe ne peut pas dépasser 72 caractères."
        )
        String motDePasse
) {
}
