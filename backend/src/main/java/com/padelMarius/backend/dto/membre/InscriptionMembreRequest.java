package com.padelMarius.backend.dto.membre;

import com.padelMarius.backend.entity.CategorieMembre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InscriptionMembreRequest(

        @NotBlank(message = "Le nom est obligatoire.")
        @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères.")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire.")
        @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères.")
        String prenom,

        @NotNull(message = "La catégorie du membre est obligatoire.")
        CategorieMembre categorieMembre,

        Long siteRattachementId
) {
}