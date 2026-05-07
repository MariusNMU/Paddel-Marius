package com.padelMarius.backend.dto.participation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InscriptionPubliqueRequest(
        @NotBlank
        @Size(max = 10)
        String matriculeJoueur
) {
}