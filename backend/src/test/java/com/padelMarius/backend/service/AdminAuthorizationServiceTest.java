package com.padelMarius.backend.service;

import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.PorteeFermeture;
import com.padelMarius.backend.entity.RoleAdministrateur;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.repository.AdministrateurRepository;
import com.padelMarius.backend.security.JwtService;
import com.padelMarius.backend.security.JwtUtilisateur;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthorizationServiceTest {

    @Mock
    private AdministrateurRepository administrateurRepository;

    @InjectMocks
    private AdminAuthorizationService adminAuthorizationService;

    @Test
    void estAdminGlobal_shouldReturnTrueForActiveGlobalAdmin() {
        Administrateur admin = creerAdmin(
                "admin-global",
                RoleAdministrateur.GLOBAL,
                null,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-global"))
                .thenReturn(Optional.of(admin));

        assertThat(adminAuthorizationService.estAdminGlobal(
                authenticationAdmin("admin-global", "GLOBAL", null)
        )).isTrue();
    }

    @Test
    void estAdminGlobal_shouldReturnFalseForSiteAdmin() {
        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                creerSite(1001L),
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        assertThat(adminAuthorizationService.estAdminGlobal(
                authenticationAdmin("admin-bruxelles", "SITE", 1001L)
        )).isFalse();
    }

    @Test
    void peutAccederAuSite_shouldReturnTrueForGlobalAdmin() {
        Administrateur admin = creerAdmin(
                "admin-global",
                RoleAdministrateur.GLOBAL,
                null,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-global"))
                .thenReturn(Optional.of(admin));

        assertThat(adminAuthorizationService.peutAccederAuSite(
                authenticationAdmin("admin-global", "GLOBAL", null),
                null
        )).isTrue();
    }

    @Test
    void peutAccederAuSite_shouldReturnTrueForSiteAdminOnOwnSite() {
        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                creerSite(1001L),
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        assertThat(adminAuthorizationService.peutAccederAuSite(
                authenticationAdmin("admin-bruxelles", "SITE", 1001L),
                1001L
        )).isTrue();
    }

    @Test
    void peutAccederAuSite_shouldReturnFalseForSiteAdminOnOtherSite() {
        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                creerSite(1001L),
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        assertThat(adminAuthorizationService.peutAccederAuSite(
                authenticationAdmin("admin-bruxelles", "SITE", 1001L),
                1002L
        )).isFalse();
    }

    @Test
    void peutAccederAuSite_shouldReturnFalseForSiteAdminWithoutSiteId() {
        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                creerSite(1001L),
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        assertThat(adminAuthorizationService.peutAccederAuSite(
                authenticationAdmin("admin-bruxelles", "SITE", 1001L),
                null
        )).isFalse();
    }

    @Test
    void peutGererFermeture_shouldReturnTrueForGlobalAdmin() {
        Administrateur admin = creerAdmin(
                "admin-global",
                RoleAdministrateur.GLOBAL,
                null,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-global"))
                .thenReturn(Optional.of(admin));

        assertThat(adminAuthorizationService.peutGererFermeture(
                authenticationAdmin("admin-global", "GLOBAL", null),
                PorteeFermeture.GLOBALE,
                null
        )).isTrue();
    }

    @Test
    void peutGererFermeture_shouldReturnFalseForSiteAdminOnGlobalClosure() {
        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                creerSite(1001L),
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        assertThat(adminAuthorizationService.peutGererFermeture(
                authenticationAdmin("admin-bruxelles", "SITE", 1001L),
                PorteeFermeture.GLOBALE,
                null
        )).isFalse();
    }

    @Test
    void peutGererFermeture_shouldReturnTrueForSiteAdminOnOwnLocalClosure() {
        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                creerSite(1001L),
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        assertThat(adminAuthorizationService.peutGererFermeture(
                authenticationAdmin("admin-bruxelles", "SITE", 1001L),
                PorteeFermeture.LOCALE,
                1001L
        )).isTrue();
    }

    @Test
    void peutGererFermeture_shouldReturnFalseForSiteAdminOnOtherSite() {
        Administrateur admin = creerAdmin(
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                creerSite(1001L),
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(admin));

        assertThat(adminAuthorizationService.peutGererFermeture(
                authenticationAdmin("admin-bruxelles", "SITE", 1001L),
                PorteeFermeture.LOCALE,
                1002L
        )).isFalse();
    }

    private Authentication authenticationAdmin(
            String login,
            String role,
            Long siteId
    ) {
        JwtUtilisateur utilisateur = new JwtUtilisateur(
                login,
                JwtService.TYPE_UTILISATEUR_ADMIN,
                role,
                siteId
        );

        return new UsernamePasswordAuthenticationToken(
                utilisateur,
                null,
                List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_ADMIN_" + role)
                )
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
