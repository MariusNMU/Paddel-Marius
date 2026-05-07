package com.padelMarius.backend.dto.dette;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PayerDetteRequest(

        @NotNull(message = "Le montant du paiement est obligatoire.")
        @DecimalMin(value = "0.01", message = "Le montant du paiement doit être positif.")
        BigDecimal montant
) {
}