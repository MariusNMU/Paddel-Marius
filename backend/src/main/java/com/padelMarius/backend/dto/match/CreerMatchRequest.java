package com.padelMarius.backend.dto.match;

import com.padelMarius.backend.entity.ModeCreation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreerMatchRequest(
        @NotNull
        @Positive
        Long terrainId,

        @NotBlank
        @Size(max = 10)
        String matriculeOrganisateur,

        @NotNull
        LocalDateTime dateHeureDebut,

        @NotNull
        ModeCreation modeCreation
) {
}