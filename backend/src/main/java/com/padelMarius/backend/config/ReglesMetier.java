package com.padelMarius.backend.config;

import java.math.BigDecimal;

public final class ReglesMetier {

    public static final int DUREE_MATCH_MINUTES = 90;
    public static final int PAUSE_ENTRE_MATCHES_MINUTES = 15;
    public static final int NOMBRE_JOUEURS_MAXIMUM = 4;

    public static final BigDecimal PRIX_TOTAL_MATCH = new BigDecimal("60.00");
    public static final BigDecimal MONTANT_PARTICIPATION_STANDARD = new BigDecimal("15.00");
    public static final BigDecimal SOLDE_INITIAL_JOUEUR = new BigDecimal("100.00");

    private ReglesMetier() {
    }
}