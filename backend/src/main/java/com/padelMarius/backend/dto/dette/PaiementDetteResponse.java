package com.padelMarius.backend.dto.dette;

import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaiementDetteResponse(
        Long paiementId,
        Long detteId,
        Long membreId,
        String matriculeMembre,
        NaturePaiement naturePaiement,
        BigDecimal montant,
        StatutPaiement statutPaiement,
        StatutDette statutDette,
        LocalDateTime dateHeurePaiement,
        LocalDateTime dateReglementDette
) {
}