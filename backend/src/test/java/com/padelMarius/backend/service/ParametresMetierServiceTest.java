package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.parametre.ParametresMetierResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParametresMetierServiceTest {

    private final ParametresMetierService parametresMetierService =
            new ParametresMetierService();

    @Test
    void consulterParametresMetier_shouldReturnConfiguredBusinessRules() {
        ParametresMetierResponse response =
                parametresMetierService.consulterParametresMetier();

        assertEquals(90, response.dureeMatchMinutes());
        assertEquals(15, response.pauseEntreMatchesMinutes());
        assertEquals(4, response.nombreJoueursMaximum());
        assertEquals(0, new BigDecimal("60.00").compareTo(response.prixTotalMatch()));
        assertEquals(0, new BigDecimal("15.00").compareTo(response.montantParticipationStandard()));
        assertEquals(0, new BigDecimal("100.00").compareTo(response.soldeInitialJoueur()));
    }
}