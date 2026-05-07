package com.padelMarius.backend.dto.dette;

import com.padelMarius.backend.entity.StatutDette;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DetteResponse(
        Long detteId,
        Long matchId,
        Long membreResponsableId,
        String matriculeResponsable,
        BigDecimal montantInitial,
        BigDecimal montantRestant,
        StatutDette statutDette,
        LocalDateTime dateCreation,
        LocalDateTime dateReglement
) {
}