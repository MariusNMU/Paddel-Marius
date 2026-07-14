package com.padelMarius.backend.config;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglesMetierTest {

    @Test
    void prix_des_participations_doivent_couvrir_le_prix_total_du_match() {
        BigDecimal totalParticipations =
                ReglesMetier.MONTANT_PARTICIPATION_STANDARD.multiply(
                        BigDecimal.valueOf(ReglesMetier.NOMBRE_JOUEURS_MAXIMUM)
                );

        assertEquals(ReglesMetier.PRIX_TOTAL_MATCH, totalParticipations);
    }

    @Test
    void durees_doivent_correspondre_aux_valeurs_exposees_par_api() {
        assertEquals(
                Duration.ofMinutes(ReglesMetier.DUREE_MATCH_MINUTES),
                ReglesMetier.DUREE_MATCH
        );

        assertEquals(
                Duration.ofMinutes(ReglesMetier.PAUSE_ENTRE_MATCHES_MINUTES),
                ReglesMetier.PAUSE_ENTRE_MATCHES
        );
    }

    @Test
    void valeurs_initiales_doivent_etre_strictement_positives() {
        assertTrue(ReglesMetier.SOLDE_INITIAL_JOUEUR.signum() > 0);
        assertTrue(ReglesMetier.DUREE_PENALITE_JOURS > 0);
    }
}