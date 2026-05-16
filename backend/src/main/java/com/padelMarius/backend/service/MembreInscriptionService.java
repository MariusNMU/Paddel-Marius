package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.membre.InscriptionMembreRequest;
import com.padelMarius.backend.dto.membre.MembreResponse;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class MembreInscriptionService {

    private static final BigDecimal SOLDE_INITIAL = new BigDecimal("100.00");
    private static final String MOT_DE_PASSE_INITIAL = "password";

    private final MembreRepository membreRepository;
    private final SiteRepository siteRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MembreResponse inscrireMembre(InscriptionMembreRequest request) {
        Site siteRattachement = trouverSiteRattachementSiNecessaire(request);

        String matricule = genererMatricule(request.categorieMembre());

        Membre membre = Membre.builder()
                .matricule(matricule)
                .nom(request.nom().trim())
                .prenom(request.prenom().trim())
                .categorieMembre(request.categorieMembre())
                .siteRattachement(siteRattachement)
                .actif(true)
                .motDePasseHash(passwordEncoder.encode(MOT_DE_PASSE_INITIAL))
                .soldeCredit(SOLDE_INITIAL)
                .build();

        Membre membreSauvegarde = membreRepository.save(membre);

        return convertirEnResponse(membreSauvegarde);
    }

    private Site trouverSiteRattachementSiNecessaire(InscriptionMembreRequest request) {
        if (request.categorieMembre() == CategorieMembre.SITE) {
            if (request.siteRattachementId() == null) {
                throw new ConfigurationMetierException(
                        "Un membre SITE doit avoir un site de rattachement."
                );
            }

            return siteRepository.findById(request.siteRattachementId())
                    .orElseThrow(() -> new RessourceIntrouvableException(
                            "Site introuvable avec l'id " + request.siteRattachementId()
                    ));
        }

        return null;
    }

    private String genererMatricule(CategorieMembre categorieMembre) {
        String prefixe = switch (categorieMembre) {
            case GLOBAL -> "G";
            case SITE -> "S";
            case LIBRE -> "L";
        };

        int dernierNumero = membreRepository.findByMatriculeStartingWith(prefixe)
                .stream()
                .map(Membre::getMatricule)
                .map(matricule -> extraireNumero(matricule, prefixe))
                .max(Comparator.naturalOrder())
                .orElse(1000);

        String nouveauMatricule = prefixe + (dernierNumero + 1);

        if (membreRepository.existsByMatricule(nouveauMatricule)) {
            throw new ConfigurationMetierException(
                    "Impossible de générer un matricule unique."
            );
        }

        return nouveauMatricule;
    }

    private int extraireNumero(String matricule, String prefixe) {
        if (matricule == null || !matricule.startsWith(prefixe)) {
            return 0;
        }

        try {
            return Integer.parseInt(matricule.substring(prefixe.length()));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private MembreResponse convertirEnResponse(Membre membre) {
        Site site = membre.getSiteRattachement();

        return new MembreResponse(
                membre.getId(),
                membre.getMatricule(),
                membre.getNom(),
                membre.getPrenom(),
                membre.getCategorieMembre(),
                site != null ? site.getId() : null,
                site != null ? site.getNom() : null,
                membre.isActif(),
                membre.getSoldeCredit()
        );
    }
}
