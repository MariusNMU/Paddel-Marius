package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.parametre.ParametresMetierResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.padelMarius.backend.config.ReglesMetier.DUREE_MATCH_MINUTES;
import static com.padelMarius.backend.config.ReglesMetier.MONTANT_PARTICIPATION_STANDARD;
import static com.padelMarius.backend.config.ReglesMetier.NOMBRE_JOUEURS_MAXIMUM;
import static com.padelMarius.backend.config.ReglesMetier.PAUSE_ENTRE_MATCHES_MINUTES;
import static com.padelMarius.backend.config.ReglesMetier.PRIX_TOTAL_MATCH;
import static com.padelMarius.backend.config.ReglesMetier.SOLDE_INITIAL_JOUEUR;

@Service
@Transactional(readOnly = true)
public class ParametresMetierService {

    public ParametresMetierResponse consulterParametresMetier() {
        return new ParametresMetierResponse(
                DUREE_MATCH_MINUTES,
                PAUSE_ENTRE_MATCHES_MINUTES,
                NOMBRE_JOUEURS_MAXIMUM,
                PRIX_TOTAL_MATCH,
                MONTANT_PARTICIPATION_STANDARD,
                SOLDE_INITIAL_JOUEUR
        );
    }
}