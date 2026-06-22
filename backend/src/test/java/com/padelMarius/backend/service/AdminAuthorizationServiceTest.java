package com.padelMarius.backend.service;

import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.PorteeFermeture;
import com.padelMarius.backend.entity.RoleAdministrateur;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.exception.AutorisationException;
import com.padelMarius.backend.repository.AdministrateurRepository;
import com.padelMarius.backend.security.JwtService;
import com.padelMarius.backend.security.JwtUtilisateur;
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

    @Mock
    private JwtService jwtService;

    private AdminAuthorizationService adminAuthorizationService;

    @BeforeEach
    void setUp() {
        adminAuthorizationService = new AdminAuthorizationService(
                administrateurRepository,
                jwtService
        );
    }

    @Test
    void verifierAdminGlobal_shouldAcceptGlobalAdmin() {
        String authorization = "Bearer jwt-admin-global";

        Administrateur admin = creerAdmin(
                "admin-global",
                RoleAdministrateur.GLOBAL,
                null,
                true
        );

        simulerTokenAdmin(
                authorization,
                "admin-global",
                "GLOBAL",
                null
        );

        when(administrateurRepository.findByEmailOuLogin("admin-global"))
                .thenReturn(Optional.of(admin));

        adminAuthorizationService.verifierAdminGlobal(authorization);
    }

    @Test
    void verifierAdminGlobal_shouldAcceptBearerJwtAdminToken() {
        String authorization = "Bearer jwt-admin-global";

        Administrateur admin = creerAdmin(
                "admin-global",
                RoleAdministrateur.GLOBAL,
                null,
                true
        );

        simulerTokenAdmin(
                authorization,
                "admin-global",
                "GLOBAL",
                null
        );

        when(administrateurRepository.findByEmailOuLogin("admin-global"))
                .thenReturn(Optional.of(admin));

        adminAuthorizationService.verifierAdminGlobal(authorization);
    }

    @Test
    void verifierAdminGlobal_shouldRejectBearerJwtPlayerToken() {
        when(jwtService.extraireUtilisateurDepuisAuthorization("Bearer jwt-joueur"))
                .thenReturn(new JwtUtilisateur(
                        "G1001",
                        JwtService.TYPE_UTILISATEUR_JOUEUR,
                        "GLOBAL",
                        null
                ));

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> adminAuthorizationService.verifierAdminGlobal("Bearer jwt-joueur")
        );

        assertEquals("Token administrateur requis.", exception.getMessage());
    }

    @Test
    void verifierAdminGlobal_shouldRejectSiteAdmin() {
        String authorization = "Bearer jwt-admin-site";
        Site site = creerSite(1001L);

        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                site,
                true
        );

        simulerTokenAdmin(
                authorization,
                "admin-bruxelles",
                "SITE",
                1001L
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        AutorisationException exception = assertThrows(
                AutorisationException.class,
                () -> adminAuthorizationService.verifierAdminGlobal(authorization)
        );

        assertEquals(
                "Seul un administrateur GLOBAL peut réaliser cette opération.",
                exception.getMessage()
        );
    }

    @Test
    void verifierAccesAdminSite_shouldAcceptGlobalAdminWithoutSiteId() {
        String authorization = "Bearer jwt-admin-global";

        Administrateur admin = creerAdmin(
                "admin-global",
                RoleAdministrateur.GLOBAL,
                null,
                true
        );

        simulerTokenAdmin(
                authorization,
                "admin-global",
                "GLOBAL",
                null
        );

        when(administrateurRepository.findByEmailOuLogin("admin-global"))
                .thenReturn(Optional.of(admin));

        adminAuthorizationService.verifierAccesAdminSite(authorization, null);
    }

    @Test
    void verifierAccesAdminSite_shouldAcceptSiteAdminOnOwnSite() {
        String authorization = "Bearer jwt-admin-site";
        Site site = creerSite(1001L);

        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                site,
                true
        );

        simulerTokenAdmin(
                authorization,
                "admin-bruxelles",
                "SITE",
                1001L
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        adminAuthorizationService.verifierAccesAdminSite(authorization, 1001L);
    }

    @Test
    void verifierAccesAdminSite_shouldRejectSiteAdminWithoutSiteId() {
        String authorization = "Bearer jwt-admin-site";
        Site site = creerSite(1001L);

        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                site,
                true
        );

        simulerTokenAdmin(
                authorization,
                "admin-bruxelles",
                "SITE",
                1001L
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        AutorisationException exception = assertThrows(
                AutorisationException.class,
                () -> adminAuthorizationService.verifierAccesAdminSite(authorization, null)
        );

        assertEquals(
                "Un administrateur SITE ne peut pas accéder à une vue globale.",
                exception.getMessage()
        );
    }

    @Test
    void verifierAccesAdminSite_shouldRejectSiteAdminOnOtherSite() {
        String authorization = "Bearer jwt-admin-site";
        Site site = creerSite(1001L);

        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                site,
                true
        );

        simulerTokenAdmin(
                authorization,
                "admin-bruxelles",
                "SITE",
                1001L
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        AutorisationException exception = assertThrows(
                AutorisationException.class,
                () -> adminAuthorizationService.verifierAccesAdminSite(authorization, 1002L)
        );

        assertEquals(
                "Un administrateur SITE ne peut agir que sur son propre site.",
                exception.getMessage()
        );
    }

    @Test
    void verifierAccesFermeture_shouldRejectSiteAdminForGlobalClosure() {
        String authorization = "Bearer jwt-admin-site";
        Site site = creerSite(1001L);

        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                site,
                true
        );

        simulerTokenAdmin(
                authorization,
                "admin-bruxelles",
                "SITE",
                1001L
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        AutorisationException exception = assertThrows(
                AutorisationException.class,
                () -> adminAuthorizationService.verifierAccesFermeture(
                        authorization,
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
        String authorization = "Bearer jwt-admin-site";
        Site site = creerSite(1001L);

        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                site,
                true
        );

        simulerTokenAdmin(
                authorization,
                "admin-bruxelles",
                "SITE",
                1001L
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        adminAuthorizationService.verifierAccesFermeture(
                authorization,
                PorteeFermeture.LOCALE,
                1001L
        );
    }

    @Test
    void verifierAdminGlobal_shouldRejectLegacyRawLogin() {
        when(jwtService.extraireUtilisateurDepuisAuthorization(
                "admin-global"
        )).thenThrow(new AuthentificationException(
                "Token JWT obligatoire."
        ));

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> adminAuthorizationService.verifierAdminGlobal(
                        "admin-global"
                )
        );

        assertEquals(
                "Token JWT obligatoire.",
                exception.getMessage()
        );
    }

    @Test
    void verifierAdminGlobal_shouldRejectMissingJwt() {
        when(jwtService.extraireUtilisateurDepuisAuthorization(null))
                .thenThrow(new AuthentificationException(
                        "Token JWT obligatoire."
                ));

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> adminAuthorizationService.verifierAdminGlobal(null)
        );

        assertEquals(
                "Token JWT obligatoire.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectUnknownAdmin() {
        String authorization = "Bearer jwt-admin-inconnu";

        simulerTokenAdmin(
                authorization,
                "admin-inconnu",
                "GLOBAL",
                null
        );

        when(administrateurRepository.findByEmailOuLogin("admin-inconnu"))
                .thenReturn(Optional.empty());

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> adminAuthorizationService.verifierAdminGlobal(authorization)
        );

        assertEquals(
                "Administrateur introuvable ou non authentifié.",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectInactiveAdmin() {
        String authorization = "Bearer jwt-admin-global";

        Administrateur admin = creerAdmin(
                "admin-global",
                RoleAdministrateur.GLOBAL,
                null,
                false
        );

        simulerTokenAdmin(
                authorization,
                "admin-global",
                "GLOBAL",
                null
        );

        when(administrateurRepository.findByEmailOuLogin("admin-global"))
                .thenReturn(Optional.of(admin));

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> adminAuthorizationService.verifierAdminGlobal(authorization)
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

    private void simulerTokenAdmin(
            String authorization,
            String login,
            String role,
            Long siteId
    ) {
        when(jwtService.extraireUtilisateurDepuisAuthorization(
                authorization
        )).thenReturn(new JwtUtilisateur(
                login,
                JwtService.TYPE_UTILISATEUR_ADMIN,
                role,
                siteId
        ));
    }
}
