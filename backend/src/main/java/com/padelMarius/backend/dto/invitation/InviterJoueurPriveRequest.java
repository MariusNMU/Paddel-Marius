package com.padelMarius.backend.dto.invitation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InviterJoueurPriveRequest(

        @NotBlank(message = "Le matricule de l'organisateur est obligatoire.")
        @Size(max = 10, message = "Le matricule organisateur ne peut pas dépasser 10 caractères.")
        String matriculeOrganisateur,

        @NotBlank(message = "Le matricule du joueur invité est obligatoire.")
        @Size(max = 10, message = "Le matricule invité ne peut pas dépasser 10 caractères.")
        String matriculeInvite
) {
}