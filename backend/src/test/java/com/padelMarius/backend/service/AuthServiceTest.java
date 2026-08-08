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
import com.padelMarius.backend.security.IdentiteAuthentification;
import com.padelMarius.backend.security.JetonRafraichissementService;
import com.padelMarius.backend.security.JwtService;
import com.padelMarius.backend.security.JwtUtilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private JetonRafraichissementService jetonRafraichissementService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                membreRepository,
                administrateurRepository,
                authenticationManager,
                jwtService,
                jetonRafraichissementService
        );
    }

    @Test
    void authentifierJoueur_shouldUseAuthenticationManagerAndReturnPlayerInfo() {
        autoriserAuthentification();

        Site site = creerSite(1L, "Padel Bruxelles");
        Membre membre = creerMembre(
                10L,
                "S00001",
                CategorieMembre.SITE,
                site,
                true
        );

        when(membreRepository.findByMatriculeIgnoreCase("s00001"))
                .thenReturn(Optional.of(membre));
        when(jwtService.genererTokenJoueur(membre))
                .thenReturn(token("jwt-joueur"));
        when(jwtService.genererRefreshTokenJoueur(membre))
                .thenReturn(token("refresh-joueur"));

        AuthService.ResultatAuthentification<AuthJoueurResponse> resultat =
                authService.authentifierJoueur(
                        new ConnexionJoueurRequest(
                                " s00001 ",
                                "password"
                        )
                );
        AuthJoueurResponse response = resultat.reponse();

        assertEquals(10L, response.membreId());
        assertEquals("S00001", response.matricule());
        assertEquals("Nom 10", response.nom());
        assertEquals("Prenom 10", response.prenom());
        assertEquals(CategorieMembre.SITE, response.categorieMembre());
        assertEquals(1L, response.siteRattachementId());
        assertEquals("Padel Bruxelles", response.nomSiteRattachement());
        assertEquals(true, response.actif());
        assertEquals("jwt-joueur", response.token());
        assertEquals("refresh-joueur", resultat.refreshToken());
        verify(jetonRafraichissementService).enregistrer(
                any(JwtService.TokenGenere.class),
                eq("S00001"),
                eq(JwtService.TYPE_UTILISATEUR_JOUEUR)
        );

        Authentication authentication = authentificationTransmise();
        assertEquals(
                IdentiteAuthentification.joueur("s00001"),
                authentication.getPrincipal()
        );
        assertEquals("password", authentication.getCredentials());
    }

    @Test
    void authentifierJoueur_shouldPreservePasswordWithSurroundingSpaces() {
        autoriserAuthentification();

        Membre membre = creerMembre(
                10L,
                "G0001",
                CategorieMembre.GLOBAL,
                null,
                true
        );

        when(membreRepository.findByMatriculeIgnoreCase("G0001"))
                .thenReturn(Optional.of(membre));
        when(jwtService.genererTokenJoueur(membre))
                .thenReturn(token("jwt-joueur"));
        when(jwtService.genererRefreshTokenJoueur(membre))
                .thenReturn(token("refresh-joueur"));

        authService.authentifierJoueur(
                new ConnexionJoueurRequest(
                        "G0001",
                        " motdepasse-avec-espaces "
                )
        );

        assertEquals(
                " motdepasse-avec-espaces ",
                authentificationTransmise().getCredentials()
        );
    }

    @Test
    void authentifierJoueur_shouldReturnGenericError_whenManagerRejectsCredentials() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Refus Spring Security"));

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> authService.authentifierJoueur(
                        new ConnexionJoueurRequest("G9999", "mauvais")
                )
        );

        assertEquals(MESSAGE_IDENTIFIANTS_INVALIDES, exception.getMessage());
        verifyNoInteractions(membreRepository, administrateurRepository);
    }

    @Test
    void authentifierJoueur_shouldRejectMissingCredentialsBeforeManager() {
        assertThrows(
                RuntimeException.class,
                () -> authService.authentifierJoueur(
                        new ConnexionJoueurRequest(" ", "password")
                )
        );

        verifyNoInteractions(authenticationManager);
    }

    @Test
    void authentifierAdmin_shouldUseAuthenticationManagerAndReturnGlobalAdmin() {
        autoriserAuthentification();

        Administrateur administrateur = creerAdministrateur(
                20L,
                "admin-global",
                RoleAdministrateur.GLOBAL,
                null,
                true
        );

        when(administrateurRepository.findByEmailOuLoginIgnoreCase(
                "ADMIN-GLOBAL"
        ))
                .thenReturn(Optional.of(administrateur));
        when(jwtService.genererTokenAdmin(administrateur))
                .thenReturn(token("jwt-admin-global"));
        when(jwtService.genererRefreshTokenAdmin(administrateur))
                .thenReturn(token("refresh-admin"));

        AuthService.ResultatAuthentification<AuthAdminResponse> resultat =
                authService.authentifierAdmin(
                        new ConnexionAdminRequest(
                                " ADMIN-GLOBAL ",
                                "secret"
                        )
                );
        AuthAdminResponse response = resultat.reponse();

        assertEquals(20L, response.administrateurId());
        assertEquals("admin-global", response.login());
        assertEquals(RoleAdministrateur.GLOBAL, response.roleAdministrateur());
        assertEquals(null, response.siteId());
        assertEquals("jwt-admin-global", response.token());
        assertEquals("refresh-admin", resultat.refreshToken());
        verify(jetonRafraichissementService).enregistrer(
                any(JwtService.TokenGenere.class),
                eq("admin-global"),
                eq(JwtService.TYPE_UTILISATEUR_ADMIN)
        );

        Authentication authentication = authentificationTransmise();
        assertEquals(
                IdentiteAuthentification.admin("ADMIN-GLOBAL"),
                authentication.getPrincipal()
        );
        assertEquals("secret", authentication.getCredentials());
    }

    @Test
    void authentifierAdmin_shouldReturnSiteAdminInfo() {
        autoriserAuthentification();

        Site site = creerSite(1L, "Padel Bruxelles");
        Administrateur administrateur = creerAdministrateur(
                21L,
                "admin-bruxelles",
                RoleAdministrateur.SITE,
                site,
                true
        );

        when(administrateurRepository.findByEmailOuLoginIgnoreCase(
                "admin-bruxelles"
        ))
                .thenReturn(Optional.of(administrateur));
        when(jwtService.genererTokenAdmin(administrateur))
                .thenReturn(token("jwt-admin-site"));
        when(jwtService.genererRefreshTokenAdmin(administrateur))
                .thenReturn(token("refresh-admin-site"));

        AuthAdminResponse response = authService.authentifierAdmin(
                new ConnexionAdminRequest("admin-bruxelles", "secret-site")
        ).reponse();

        assertEquals(21L, response.administrateurId());
        assertEquals(RoleAdministrateur.SITE, response.roleAdministrateur());
        assertEquals(1L, response.siteId());
        assertEquals("Padel Bruxelles", response.nomSite());
        assertEquals("jwt-admin-site", response.token());
    }

    @Test
    void authentifierAdmin_shouldReturnGenericError_whenManagerRejectsCredentials() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Refus Spring Security"));

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> authService.authentifierAdmin(
                        new ConnexionAdminRequest("admin-global", "mauvais")
                )
        );

        assertEquals(MESSAGE_IDENTIFIANTS_INVALIDES, exception.getMessage());
        verifyNoInteractions(membreRepository, administrateurRepository);
    }

    @Test
    void authentifierAdmin_shouldRejectAccountMissingAfterAuthentication() {
        autoriserAuthentification();
        when(administrateurRepository.findByEmailOuLoginIgnoreCase(
                "admin-global"
        ))
                .thenReturn(Optional.empty());

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> authService.authentifierAdmin(
                        new ConnexionAdminRequest("admin-global", "secret")
                )
        );

        assertEquals(MESSAGE_IDENTIFIANTS_INVALIDES, exception.getMessage());
        verify(jwtService, never()).genererTokenAdmin(any());
    }

    @Test
    void rafraichir_shouldIssueNewPlayerTokens_whenRefreshTokenIsValid() {
        Membre membre = creerMembre(
                10L,
                "G0001",
                CategorieMembre.GLOBAL,
                null,
                true
        );

        when(jwtService.validerRefreshToken("refresh-valide"))
                .thenReturn(new JwtUtilisateur(
                        "G0001",
                        JwtService.TYPE_UTILISATEUR_JOUEUR
                ));
        when(membreRepository.findByMatriculeIgnoreCase("G0001"))
                .thenReturn(Optional.of(membre));
        when(jwtService.genererTokenJoueur(membre))
                .thenReturn(token("nouvel-access"));
        when(jwtService.genererRefreshTokenJoueur(membre))
                .thenReturn(token("nouveau-refresh"));

        var resultat = authService.rafraichir("refresh-valide");

        assertEquals("nouvel-access", resultat.reponse().token());
        assertEquals(
                LocalDateTime.of(2026, 5, 30, 12, 0),
                resultat.reponse().expirationToken()
        );
        assertEquals("nouveau-refresh", resultat.refreshToken());
        verify(jetonRafraichissementService).consommer(any());
        verifyNoInteractions(administrateurRepository);
    }

    @Test
    void rafraichir_shouldIssueNewAdminTokens_whenRefreshTokenIsValid() {
        Administrateur administrateur = creerAdministrateur(
                20L,
                "admin-global",
                RoleAdministrateur.GLOBAL,
                null,
                true
        );

        when(jwtService.validerRefreshToken("refresh-admin"))
                .thenReturn(new JwtUtilisateur(
                        "admin-global",
                        JwtService.TYPE_UTILISATEUR_ADMIN
                ));
        when(administrateurRepository.findByEmailOuLoginIgnoreCase(
                "admin-global"
        ))
                .thenReturn(Optional.of(administrateur));
        when(jwtService.genererTokenAdmin(administrateur))
                .thenReturn(token("nouvel-access-admin"));
        when(jwtService.genererRefreshTokenAdmin(administrateur))
                .thenReturn(token("nouveau-refresh-admin"));

        var resultat = authService.rafraichir("refresh-admin");

        assertEquals("nouvel-access-admin", resultat.reponse().token());
        assertEquals("nouveau-refresh-admin", resultat.refreshToken());
        verify(jetonRafraichissementService).consommer(any());
        verifyNoInteractions(membreRepository);
    }

    @Test
    void rafraichir_shouldRejectInactiveAccount() {
        Membre membre = creerMembre(
                10L,
                "G0001",
                CategorieMembre.GLOBAL,
                null,
                false
        );

        when(jwtService.validerRefreshToken("refresh-valide"))
                .thenReturn(new JwtUtilisateur(
                        "G0001",
                        JwtService.TYPE_UTILISATEUR_JOUEUR
                ));
        when(membreRepository.findByMatriculeIgnoreCase("G0001"))
                .thenReturn(Optional.of(membre));

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> authService.rafraichir("refresh-valide")
        );

        assertEquals("Refresh token invalide.", exception.getMessage());
        verify(jwtService, never()).genererTokenJoueur(any());
    }

    @Test
    void deconnecter_shouldRevokeValidRefreshToken() {
        when(jwtService.validerRefreshToken("refresh-valide"))
                .thenReturn(new JwtUtilisateur(
                        "G0001",
                        JwtService.TYPE_UTILISATEUR_JOUEUR,
                        "token-id"
                ));

        authService.deconnecter("refresh-valide");

        verify(jetonRafraichissementService)
                .revoquerSiPresent("token-id");
    }

    @Test
    void deconnecter_shouldRemainIdempotentForInvalidRefreshToken() {
        when(jwtService.validerRefreshToken("refresh-invalide"))
                .thenThrow(new AuthentificationException(
                        "Token JWT invalide."
                ));

        authService.deconnecter("refresh-invalide");

        verify(jetonRafraichissementService, never())
                .revoquerSiPresent(any());
    }

    private void autoriserAuthentification() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Authentication authentificationTransmise() {
        ArgumentCaptor<Authentication> captor =
                ArgumentCaptor.forClass(Authentication.class);

        verify(authenticationManager).authenticate(captor.capture());
        return captor.getValue();
    }

    private JwtService.TokenGenere token(String valeur) {
        return new JwtService.TokenGenere(
                valeur,
                LocalDateTime.of(2026, 5, 30, 12, 0)
        );
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
                .motDePasseHash("hash-bcrypt")
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
            RoleAdministrateur roleAdministrateur,
            Site site,
            boolean actif
    ) {
        Administrateur administrateur = Administrateur.builder()
                .nom("Nom Admin " + id)
                .prenom("Prenom Admin " + id)
                .emailOuLogin(login)
                .motDePasseHash("hash-bcrypt")
                .roleAdministrateur(roleAdministrateur)
                .site(site)
                .actif(actif)
                .build();

        ReflectionTestUtils.setField(administrateur, "id", id);
        return administrateur;
    }
}
