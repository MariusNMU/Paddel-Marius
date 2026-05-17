package com.padelMarius.backend.service;

import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.PorteeFermeture;
import com.padelMarius.backend.entity.RoleAdministrateur;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.exception.AutorisationException;
import com.padelMarius.backend.repository.AdministrateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthorizationServiceTest {

    @Mock
    private AdministrateurRepository administrateurRepository;

    private AdminAuthorizationService adminAuthorizationService;

    @BeforeEach
    void setUp() {
        adminAuthorizationService = new AdminAuthorizationService(administrateurRepository);
    }

    @Test
    void verifierAdminGlobal_shouldAcceptGlobalAdmin() {
        Administrateur admin = creerAdmin(
                "admin-global",
                RoleAdministrateur.GLOBAL,
                null,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-global"))
                .thenReturn(Optional.of(admin));

        adminAuthorizationService.verifierAdminGlobal("admin-global");
    }

    @Test
    void verifierAdminGlobal_shouldRejectSiteAdmin() {
        Site site = creerSite(1001L);

        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                site,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        AutorisationException exception = assertThrows(
                AutorisationException.class,
                () -> adminAuthorizationService.verifierAdminGlobal("admin-bruxelles")
        );

        assertEquals(
                "Seul un administrateur GLOBAL peut réaliser cette opération.",
                exception.getMessage()
        );
    }

    @Test
    void verifierAccesAdminSite_shouldAcceptGlobalAdminWithoutSiteId() {
        Administrateur admin = creerAdmin(
                "admin-global",
                RoleAdministrateur.GLOBAL,
                null,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-global"))
                .thenReturn(Optional.of(admin));

        adminAuthorizationService.verifierAccesAdminSite("admin-global", null);
    }

    @Test
    void verifierAccesAdminSite_shouldAcceptSiteAdminOnOwnSite() {
        Site site = creerSite(1001L);

        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                site,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        adminAuthorizationService.verifierAccesAdminSite("admin-bruxelles", 1001L);
    }

    @Test
    void verifierAccesAdminSite_shouldRejectSiteAdminWithoutSiteId() {
        Site site = creerSite(1001L);

        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                site,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        AutorisationException exception = assertThrows(
                AutorisationException.class,
                () -> adminAuthorizationService.verifierAccesAdminSite("admin-bruxelles", null)
        );

        assertEquals(
                "Un administrateur SITE ne peut pas accéder à une vue globale.",
                exception.getMessage()
        );
    }

    @Test
    void verifierAccesAdminSite_shouldRejectSiteAdminOnOtherSite() {
        Site site = creerSite(1001L);

        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                site,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        AutorisationException exception = assertThrows(
                AutorisationException.class,
                () -> adminAuthorizationService.verifierAccesAdminSite("admin-bruxelles", 1002L)
        );

        assertEquals(
                "Un administrateur SITE ne peut agir que sur son propre site.",
                exception.getMessage()
        );
    }

    @Test
    void verifierAccesFermeture_shouldRejectSiteAdminForGlobalClosure() {
        Site site = creerSite(1001L);

        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                site,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        AutorisationException exception = assertThrows(
                AutorisationException.class,
                () -> adminAuthorizationService.verifierAccesFermeture(
                        "admin-bruxelles",
                        PorteeFermeture.GLOBALE,
                        null
                )
        );

        assertEquals(
                "Un administrateur SITE ne peut pas créer une fermeture globale.",
                exception.getMessage()
        );
    }

    @Test
    void verifierAccesFermeture_shouldAcceptSiteAdminForLocalClosureOnOwnSite() {
        Site site = creerSite(1001L);

        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                site,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        adminAuthorizationService.verifierAccesFermeture(
                "admin-bruxelles",
                PorteeFermeture.LOCALE,
                1001L
        );
    }

    @Test
    void shouldRejectMissingAdminLogin() {
        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> adminAuthorizationService.verifierAdminGlobal(" ")
        );

        assertEquals(
                "Administrateur requis pour accéder à cette opération.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectUnknownAdmin() {
        when(administrateurRepository.findByEmailOuLogin("admin-inconnu"))
                .thenReturn(Optional.empty());

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> adminAuthorizationService.verifierAdminGlobal("admin-inconnu")
        );

        assertEquals(
                "Administrateur introuvable ou non authentifié.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectInactiveAdmin() {
        Administrateur admin = creerAdmin(
                "admin-global",
                RoleAdministrateur.GLOBAL,
                null,
                false
        );

        when(administrateurRepository.findByEmailOuLogin("admin-global"))
                .thenReturn(Optional.of(admin));

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> adminAuthorizationService.verifierAdminGlobal("admin-global")
        );

        assertEquals(
                "Administrateur introuvable ou non authentifié.",
                exception.getMessage()
        );
    }

    private Site creerSite(Long id) {
        Site site = Site.builder()
                .code("SITE-" + id)
                .nom("Site " + id)
                .adresse("Adresse " + id)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(site, "id", id);

        return site;
    }

    private Administrateur creerAdmin(
            String login,
            RoleAdministrateur role,
            Site site,
            boolean actif
    ) {
        return Administrateur.builder()
                .nom("Admin")
                .prenom("Test")
                .emailOuLogin(login)
                .motDePasseHash("hash")
                .roleAdministrateur(role)
                .site(site)
                .actif(actif)
                .build();
    }
}