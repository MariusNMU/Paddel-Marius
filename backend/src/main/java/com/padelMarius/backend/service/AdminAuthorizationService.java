package com.padelMarius.backend.service;

import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.PorteeFermeture;
import com.padelMarius.backend.entity.RoleAdministrateur;
import com.padelMarius.backend.repository.AdministrateurRepository;
import com.padelMarius.backend.security.JwtService;
import com.padelMarius.backend.security.JwtUtilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service("adminAuthorizationService")
@RequiredArgsConstructor
public class AdminAuthorizationService {

    private final AdministrateurRepository administrateurRepository;

    public boolean estAdminGlobal(Authentication authentication) {
        return chargerAdministrateurActif(authentication)
                .map(administrateur ->
                        administrateur.getRoleAdministrateur()
                                == RoleAdministrateur.GLOBAL
                )
                .orElse(false);
    }

    public boolean peutAccederAuSite(
            Authentication authentication,
            Long siteIdDemande
    ) {
        Optional<Administrateur> administrateurConnecte =
                chargerAdministrateurActif(authentication);

        if (administrateurConnecte.isEmpty()) {
            return false;
        }

        Administrateur administrateur = administrateurConnecte.get();

        if (administrateur.getRoleAdministrateur()
                == RoleAdministrateur.GLOBAL) {
            return true;
        }

        if (siteIdDemande == null) {
            return false;
        }

        return estAdminSiteSurSonSite(administrateur, siteIdDemande);
    }

    public boolean peutGererFermeture(
            Authentication authentication,
            PorteeFermeture portee,
            Long siteId
    ) {
        Optional<Administrateur> administrateurConnecte =
                chargerAdministrateurActif(authentication);

        if (administrateurConnecte.isEmpty()) {
            return false;
        }

        Administrateur administrateur = administrateurConnecte.get();

        if (administrateur.getRoleAdministrateur()
                == RoleAdministrateur.GLOBAL) {
            return true;
        }

        if (portee == PorteeFermeture.GLOBALE) {
            return false;
        }

        if (siteId == null) {
            return false;
        }

        return estAdminSiteSurSonSite(administrateur, siteId);
    }

    private Optional<Administrateur> chargerAdministrateurActif(
            Authentication authentication
    ) {
        return extrairePrincipal(authentication)
                .filter(utilisateur ->
                        JwtService.TYPE_UTILISATEUR_ADMIN.equals(
                                utilisateur.typeUtilisateur()
                        )
                )
                .flatMap(utilisateur ->
                        administrateurRepository.findByEmailOuLogin(
                                utilisateur.sujet()
                        )
                )
                .filter(Administrateur::isActif);
    }

    private Optional<JwtUtilisateur> extrairePrincipal(
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof JwtUtilisateur utilisateur) {
            return Optional.of(utilisateur);
        }

        return Optional.empty();
    }

    private boolean estAdminSiteSurSonSite(
            Administrateur administrateur,
            Long siteIdDemande
    ) {
        if (administrateur.getSite() == null
                || administrateur.getSite().getId() == null) {
            return false;
        }

        return Objects.equals(
                administrateur.getSite().getId(),
                siteIdDemande
        );
    }
}