package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.auth.AuthAdminResponse;
import com.padelMarius.backend.dto.auth.AuthJoueurResponse;
import com.padelMarius.backend.dto.auth.ConnexionAdminRequest;
import com.padelMarius.backend.dto.auth.ConnexionJoueurRequest;
import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.RoleAdministrateur;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.repository.AdministrateurRepository;
import com.padelMarius.backend.repository.MembreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import com.padelMarius.backend.security.JwtService;
import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String MESSAGE_IDENTIFIANTS_INVALIDES =
            "Identifiant ou mot de passe invalide.";

    @Mock
    private MembreRepository membreRepository;

    @Mock
    private AdministrateurRepository administrateurRepository;

    @Mock
    private JwtService jwtService;

    private AuthService authService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        authService = new AuthService(
                membreRepository,
                administrateurRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void authentifierJoueur_shouldReturnPlayerInfo_whenMatriculeExistsAndMemberIsActive() {
        Site site = creerSite(1L, "Padel Bruxelles");

        Membre membre = creerMembre(
                10L,
                "S00001",
                CategorieMembre.SITE,
                site,
                true
        );

        when(membreRepository.findByMatricule("S00001"))
                .thenReturn(Optional.of(membre));
        when(jwtService.genererTokenJoueur(membre))
                .thenReturn(new JwtService.TokenGenere(
                        "jwt-joueur",
                        LocalDateTime.of(2026, 5, 30, 12, 0)
                ));

        AuthJoueurResponse response = authService.authentifierJoueur(
                new ConnexionJoueurRequest("S00001", "password")
        );

        assertEquals(10L, response.membreId());
        assertEquals("S00001", response.matricule());
        assertEquals("Nom 10", response.nom());
        assertEquals("Prenom 10", response.prenom());
        assertEquals(CategorieMembre.SITE, response.categorieMembre());
        assertEquals(1L, response.siteRattachementId());
        assertEquals("Padel Bruxelles", response.nomSiteRattachement());
        assertEquals(true, response.actif());
        assertEquals("jwt-joueur", response.token());
        assertEquals(LocalDateTime.of(2026, 5, 30, 12, 0), response.expirationToken());
    }

    @Test
    void authentifierJoueur_shouldRejectUnknownMatricule() {
        when(membreRepository.findByMatricule("G9999"))
                .thenReturn(Optional.empty());

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> authService.authentifierJoueur(
                        new ConnexionJoueurRequest("G9999", "password")
                )
        );

        assertEquals(
                MESSAGE_IDENTIFIANTS_INVALIDES,
                exception.getMessage()
        );

        verifyNoInteractions(administrateurRepository);
    }

    @Test
    void authentifierJoueur_shouldRejectInactiveMember() {
        Membre membre = creerMembre(
                10L,
                "G0001",
                CategorieMembre.GLOBAL,
                null,
                false
        );

        when(membreRepository.findByMatricule("G0001"))
                .thenReturn(Optional.of(membre));

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> authService.authentifierJoueur(
                        new ConnexionJoueurRequest(
                                "G0001",
                                "password"
                        )
                )
        );

        assertEquals(
                MESSAGE_IDENTIFIANTS_INVALIDES,
                exception.getMessage()
        );

        verifyNoInteractions(administrateurRepository);
    }

    @Test
    void authentifierJoueur_shouldRejectWrongPassword() {
        Membre membre = creerMembre(
                10L,
                "G0001",
                CategorieMembre.GLOBAL,
                null,
                true
        );

        when(membreRepository.findByMatricule("G0001"))
                .thenReturn(Optional.of(membre));

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> authService.authentifierJoueur(
                        new ConnexionJoueurRequest("G0001", "mauvais")
                )
        );

        assertEquals(
                MESSAGE_IDENTIFIANTS_INVALIDES,
                exception.getMessage()
        );

        verifyNoInteractions(administrateurRepository);
    }

    @Test
    void authentifierAdmin_shouldReturnGlobalAdminInfo_whenCredentialsAreValid() {
        Administrateur administrateur = creerAdministrateur(
                20L,
                "admin-global",
                "secret",
                RoleAdministrateur.GLOBAL,
                null,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-global"))
                .thenReturn(Optional.of(administrateur));
        when(jwtService.genererTokenAdmin(administrateur))
                .thenReturn(new JwtService.TokenGenere(
                        "jwt-admin-global",
                        LocalDateTime.of(2026, 5, 30, 12, 0)
                ));

        AuthAdminResponse response = authService.authentifierAdmin(
                new ConnexionAdminRequest("admin-global", "secret")
        );

        assertEquals(20L, response.administrateurId());
        assertEquals("admin-global", response.login());
        assertEquals("Nom Admin 20", response.nom());
        assertEquals("Prenom Admin 20", response.prenom());
        assertEquals(RoleAdministrateur.GLOBAL, response.roleAdministrateur());
        assertEquals(null, response.siteId());
        assertEquals(null, response.nomSite());
        assertEquals(true, response.actif());
        assertEquals("jwt-admin-global", response.token());
    }

    @Test
    void authentifierAdmin_shouldReturnSiteAdminInfo_whenCredentialsAreValid() {
        Site site = creerSite(1L, "Padel Bruxelles");

        Administrateur administrateur = creerAdministrateur(
                21L,
                "admin-bruxelles",
                "secret-site",
                RoleAdministrateur.SITE,
                site,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-bruxelles"))
                .thenReturn(Optional.of(administrateur));
        when(jwtService.genererTokenAdmin(administrateur))
                .thenReturn(new JwtService.TokenGenere(
                        "jwt-admin-site",
                        LocalDateTime.of(2026, 5, 30, 12, 0)
                ));

        AuthAdminResponse response = authService.authentifierAdmin(
                new ConnexionAdminRequest("admin-bruxelles", "secret-site")
        );

        assertEquals(21L, response.administrateurId());
        assertEquals("admin-bruxelles", response.login());
        assertEquals(RoleAdministrateur.SITE, response.roleAdministrateur());
        assertEquals(1L, response.siteId());
        assertEquals("Padel Bruxelles", response.nomSite());
        assertEquals(true, response.actif());
        assertEquals("jwt-admin-site", response.token());
    }

    @Test
    void authentifierAdmin_shouldRejectUnknownLogin() {
        when(administrateurRepository.findByEmailOuLogin("admin-inconnu"))
                .thenReturn(Optional.empty());

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> authService.authentifierAdmin(
                        new ConnexionAdminRequest("admin-inconnu", "secret")
                )
        );

        assertEquals(
                MESSAGE_IDENTIFIANTS_INVALIDES,
                exception.getMessage()
        );

        verifyNoInteractions(membreRepository);
    }

    @Test
    void authentifierAdmin_shouldRejectWrongPassword() {
        Administrateur administrateur = creerAdministrateur(
                20L,
                "admin-global",
                "secret",
                RoleAdministrateur.GLOBAL,
                null,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-global"))
                .thenReturn(Optional.of(administrateur));

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> authService.authentifierAdmin(
                        new ConnexionAdminRequest("admin-global", "mauvais")
                )
        );

        assertEquals(
                MESSAGE_IDENTIFIANTS_INVALIDES,
                exception.getMessage()
        );

        verifyNoInteractions(membreRepository);
    }

    @Test
    void authentifierAdmin_shouldRejectInactiveAdmin() {
        Administrateur administrateur = creerAdministrateur(
                20L,
                "admin-global",
                "secret",
                RoleAdministrateur.GLOBAL,
                null,
                false
        );

        when(administrateurRepository.findByEmailOuLogin("admin-global"))
                .thenReturn(Optional.of(administrateur));

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> authService.authentifierAdmin(
                        new ConnexionAdminRequest("admin-global", "secret")
                )
        );

        assertEquals(
                MESSAGE_IDENTIFIANTS_INVALIDES,
                exception.getMessage()
        );

        verifyNoInteractions(membreRepository);
    }

    @Test
    void authentifierAdmin_shouldRejectAdminWithoutConfiguredPassword() {
        Administrateur administrateur = creerAdministrateur(
                20L,
                "admin-global",
                null,
                RoleAdministrateur.GLOBAL,
                null,
                true
        );

        when(administrateurRepository.findByEmailOuLogin("admin-global"))
                .thenReturn(Optional.of(administrateur));

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> authService.authentifierAdmin(
                        new ConnexionAdminRequest("admin-global", "secret")
                )
        );

        assertEquals(
                MESSAGE_IDENTIFIANTS_INVALIDES,
                exception.getMessage()
        );

        verifyNoInteractions(membreRepository);
    }

    private Site creerSite(Long id, String nom) {
        Site site = Site.builder()
                .code("SITE-" + id)
                .nom(nom)
                .adresse("Adresse " + id)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(site, "id", id);

        return site;
    }

    private Membre creerMembre(
            Long id,
            String matricule,
            CategorieMembre categorieMembre,
            Site siteRattachement,
            boolean actif
    ) {
        Membre membre = Membre.builder()
                .matricule(matricule)
                .nom("Nom " + id)
                .prenom("Prenom " + id)
                .motDePasseHash(passwordEncoder.encode("password"))
                .categorieMembre(categorieMembre)
                .siteRattachement(siteRattachement)
                .actif(actif)
                .build();

        ReflectionTestUtils.setField(membre, "id", id);

        return membre;
    }

    private Administrateur creerAdministrateur(
            Long id,
            String login,
            String motDePasse,
            RoleAdministrateur roleAdministrateur,
            Site site,
            boolean actif
    ) {
        Administrateur administrateur = Administrateur.builder()
                .nom("Nom Admin " + id)
                .prenom("Prenom Admin " + id)
                .emailOuLogin(login)
                .motDePasseHash(motDePasse == null ? null : passwordEncoder.encode(motDePasse))
                .roleAdministrateur(roleAdministrateur)
                .site(site)
                .actif(actif)
                .build();

        ReflectionTestUtils.setField(administrateur, "id", id);

        return administrateur;
    }
}
