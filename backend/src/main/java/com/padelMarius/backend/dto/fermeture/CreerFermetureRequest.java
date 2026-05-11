package com.padelMarius.backend.dto.fermeture;

import com.padelMarius.backend.entity.PorteeFermeture;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreerFermetureRequest(

        @NotNull(message = "La date de fermeture est obligatoire.")
        LocalDate dateFermeture,

        @NotNull(message = "La portée de la fermeture est obligatoire.")
        PorteeFermeture portee,

        Long siteId,

        @Size(max = 255, message = "Le motif ne peut pas dépasser 255 caractères.")
        String motif
) {
}