package com.padelMarius.backend.service;

import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.PorteeFermeture;
import com.padelMarius.backend.entity.RoleAdministrateur;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.exception.AutorisationException;
import com.padelMarius.backend.repository.AdministrateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthorizationService {

    private static final String HEADER_ADMIN_OBLIGATOIRE =
            "Administrateur requis pour accéder à cette opération.";

    private final AdministrateurRepository administrateurRepository;

    public void verifierAdminGlobal(String adminLogin) {
        Administrateur administrateur = chargerAdministrateurActif(adminLogin);

        if (administrateur.getRoleAdministrateur() != RoleAdministrateur.GLOBAL) {
            throw new AutorisationException(
                    "Seul un administrateur GLOBAL peut réaliser cette opération."
            );
        }
    }

    public void verifierAccesAdminSite(String adminLogin, Long siteIdDemande) {
        Administrateur administrateur = chargerAdministrateurActif(adminLogin);

        if (administrateur.getRoleAdministrateur() == RoleAdministrateur.GLOBAL) {
            return;
        }

        if (siteIdDemande == null) {
            throw new AutorisationException(
                    "Un administrateur SITE ne peut pas accéder à une vue globale."
            );
        }

        verifierAdminSiteSurSonSite(administrateur, siteIdDemande);
    }

    public void verifierAccesFermeture(
            String adminLogin,
            PorteeFermeture portee,
            Long siteId
    ) {
        Administrateur administrateur = chargerAdministrateurActif(adminLogin);

        if (administrateur.getRoleAdministrateur() == RoleAdministrateur.GLOBAL) {
            return;
        }

        if (portee == PorteeFermeture.GLOBALE) {
            throw new AutorisationException(
                    "Un administrateur SITE ne peut pas créer une fermeture globale."
            );
        }

        if (siteId == null) {
            throw new AutorisationException(
                    "Un administrateur SITE doit agir sur son propre site."
            );
        }

        verifierAdminSiteSurSonSite(administrateur, siteId);
    }

    private Administrateur chargerAdministrateurActif(String adminLogin) {
        if (adminLogin == null || adminLogin.isBlank()) {
            throw new AuthentificationException(HEADER_ADMIN_OBLIGATOIRE);
        }

        Administrateur administrateur = administrateurRepository
                .findByEmailOuLogin(adminLogin.trim())
                .orElseThrow(() -> new AuthentificationException(
                        "Administrateur introuvable ou non authentifié."
                ));

        if (!administrateur.isActif()) {
            throw new AuthentificationException(
                    "Administrateur introuvable ou non authentifié."
            );
        }

        return administrateur;
    }

    private void verifierAdminSiteSurSonSite(
            Administrateur administrateur,
            Long siteIdDemande
    ) {
        if (administrateur.getSite() == null || administrateur.getSite().getId() == null) {
            throw new AutorisationException(
                    "L'administrateur SITE n'a pas de site rattaché."
            );
        }

        if (!administrateur.getSite().getId().equals(siteIdDemande)) {
            throw new AutorisationException(
                    "Un administrateur SITE ne peut agir que sur son propre site."
            );
        }
    }
}