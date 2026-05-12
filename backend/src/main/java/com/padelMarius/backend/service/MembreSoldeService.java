package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.membre.SoldeJoueurResponse;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.MembreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembreSoldeService {

    private final MembreRepository membreRepository;

    @Transactional(readOnly = true)
    public SoldeJoueurResponse consulterSolde(String matricule) {
        if (matricule == null || matricule.isBlank()) {
            throw new ConfigurationMetierException("Le matricule est obligatoire.");
        }

        String matriculeNormalise = matricule.trim();

        Membre membre = membreRepository.findByMatricule(matriculeNormalise)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Membre introuvable avec le matricule " + matriculeNormalise
                ));

        return new SoldeJoueurResponse(
                membre.getId(),
                membre.getMatricule(),
                membre.getSoldeCredit()
        );
    }
}