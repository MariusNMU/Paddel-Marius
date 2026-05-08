package com.padelMarius.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ConnexionAdminRequest(
        @NotBlank(message = "Le login administrateur est obligatoire.")
        String login,

        @NotBlank(message = "Le mot de passe administrateur est obligatoire.")
        String motDePasse
) {
}