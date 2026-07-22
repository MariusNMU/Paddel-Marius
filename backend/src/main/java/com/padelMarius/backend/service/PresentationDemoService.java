package com.padelMarius.backend.service;

import com.padelMarius.backend.config.DonneesDemonstrationProperties;
import com.padelMarius.backend.dto.demo.CategorieMembreDemoResponse;
import com.padelMarius.backend.dto.demo.CompteAdministrateurDemoResponse;
import com.padelMarius.backend.dto.demo.CompteJoueurDemoResponse;
import com.padelMarius.backend.dto.demo.PresentationDemoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.padelMarius.backend.config.ReglesMetier.FENETRE_RESERVATION_GLOBAL_JOURS;
import static com.padelMarius.backend.config.ReglesMetier.FENETRE_RESERVATION_LIBRE_JOURS;
import static com.padelMarius.backend.config.ReglesMetier.FENETRE_RESERVATION_SITE_JOURS;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(
        name = "padel.demo.enabled",
        havingValue = "true"
)
public class PresentationDemoService {

    private final DonneesDemonstrationProperties properties;
    private final SiteConsultationService siteConsultationService;

    public PresentationDemoResponse consulterPresentation() {
        return new PresentationDemoResponse(
                creerCategoriesMembres(),
                siteConsultationService.listerSitesActifs(),
                creerComptesJoueurs(),
                creerComptesAdministrateurs()
        );
    }

    private List<CategorieMembreDemoResponse> creerCategoriesMembres() {
        return List.of(
                new CategorieMembreDemoResponse(
                        "G",
                        "GLOBAL",
                        "Peut réserver sur tous les sites, jusqu'à "
                                + FENETRE_RESERVATION_GLOBAL_JOURS
                                + " jours avant."
                ),
                new CategorieMembreDemoResponse(
                        "S",
                        "SITE",
                        "Peut réserver uniquement sur son site de rattachement, jusqu'à "
                                + FENETRE_RESERVATION_SITE_JOURS
                                + " jours avant."
                ),
                new CategorieMembreDemoResponse(
                        "L",
                        "LIBRE",
                        "Peut réserver sur tous les sites, jusqu'à "
                                + FENETRE_RESERVATION_LIBRE_JOURS
                                + " jours avant."
                )
        );
    }

    private List<CompteJoueurDemoResponse> creerComptesJoueurs() {
        return properties.getJoueurs()
                .stream()
                .map(joueur -> new CompteJoueurDemoResponse(
                        joueur.getMatricule(),
                        properties.getMotDePasseJoueur(),
                        joueur.getDescription()
                ))
                .toList();
    }

    private List<CompteAdministrateurDemoResponse>
    creerComptesAdministrateurs() {
        return properties.getAdministrateurs()
                .stream()
                .map(administrateur ->
                        new CompteAdministrateurDemoResponse(
                                administrateur.getLogin(),
                                administrateur.getMotDePasse(),
                                administrateur.getDescription()
                        )
                )
                .toList();
    }
}
