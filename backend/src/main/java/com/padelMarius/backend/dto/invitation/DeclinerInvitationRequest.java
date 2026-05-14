package com.padelMarius.backend.dto.invitation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeclinerInvitationRequest(

        @NotBlank(message = "Le matricule du joueur est obligatoire.")
        @Size(max = 10, message = "Le matricule ne peut pas dépasser 10 caractères.")
        String matriculeJoueur
) {
}