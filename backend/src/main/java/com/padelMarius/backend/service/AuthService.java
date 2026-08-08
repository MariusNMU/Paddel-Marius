package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.auth.AuthAdminResponse;
import com.padelMarius.backend.dto.auth.AuthJoueurResponse;
import com.padelMarius.backend.dto.auth.ConnexionAdminRequest;
import com.padelMarius.backend.dto.auth.ConnexionJoueurRequest;
import com.padelMarius.backend.dto.auth.RafraichissementTokenResponse;
import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.repository.AdministrateurRepository;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.security.IdentiteAuthentification;
import com.padelMarius.backend.security.JetonRafraichissementService;
import com.padelMarius.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String MESSAGE_IDENTIFIANTS_INVALIDES =
            "Identifiant ou mot de passe invalide.";

    private final MembreRepository membreRepository;
    private final AdministrateurRepository administrateurRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JetonRafraichissementService jetonRafraichissementService;

    @Transactional
    public ResultatAuthentification<AuthJoueurResponse> authentifierJoueur(
            ConnexionJoueurRequest request
    ) {
        if (request == null
                || !StringUtils.hasText(request.matricule())
                || !StringUtils.hasText(request.motDePasse())) {
            throw new ConfigurationMetierException(
                    "Le matricule et le mot de passe sont obligatoires."
            );
        }

        String matricule = request.matricule().trim();

        authentifier(
                IdentiteAuthentification.joueur(matricule),
                request.motDePasse()
        );

        Membre membre = membreRepository.findByMatricule(matricule)
                .filter(Membre::isActif)
                .orElseThrow(() -> new AuthentificationException(
                        MESSAGE_IDENTIFIANTS_INVALIDES
                ));

        JwtService.TokenGenere refreshToken =
                jwtService.genererRefreshTokenJoueur(membre);

        return resultatAvecRefreshToken(
                convertirJoueur(membre),
                refreshToken,
                membre.getMatricule(),
                JwtService.TYPE_UTILISATEUR_JOUEUR
        );
    }

    @Transactional
    public ResultatAuthentification<AuthAdminResponse> authentifierAdmin(
            ConnexionAdminRequest request
    ) {
        if (request == null
                || !StringUtils.hasText(request.login())
                || !StringUtils.hasText(request.motDePasse())) {
            throw new ConfigurationMetierException(
                    "Le login et le mot de passe administrateur sont obligatoires."
            );
        }

        String login = request.login().trim();

        authentifier(
                IdentiteAuthentification.admin(login),
                request.motDePasse()
        );

        Administrateur administrateur =
                administrateurRepository.findByEmailOuLogin(login)
                        .filter(Administrateur::isActif)
                        .orElseThrow(() -> new AuthentificationException(
                                MESSAGE_IDENTIFIANTS_INVALIDES
                        ));

        JwtService.TokenGenere refreshToken =
                jwtService.genererRefreshTokenAdmin(administrateur);

        return resultatAvecRefreshToken(
                convertirAdmin(administrateur),
                refreshToken,
                administrateur.getEmailOuLogin(),
                JwtService.TYPE_UTILISATEUR_ADMIN
        );
    }

    @Transactional
    public ResultatAuthentification<RafraichissementTokenResponse> rafraichir(
            String refreshToken
    ) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new AuthentificationException(
                    "Refresh token obligatoire."
            );
        }

        var utilisateur = jwtService.validerRefreshToken(refreshToken);
        jetonRafraichissementService.consommer(utilisateur);

        if (JwtService.TYPE_UTILISATEUR_JOUEUR.equals(
                utilisateur.typeUtilisateur()
        )) {
            Membre membre = membreRepository
                    .findByMatricule(utilisateur.sujet())
                    .filter(Membre::isActif)
                    .orElseThrow(this::refreshTokenInvalide);

            return renouvelerJoueur(membre);
        }

        if (JwtService.TYPE_UTILISATEUR_ADMIN.equals(
                utilisateur.typeUtilisateur()
        )) {
            Administrateur administrateur = administrateurRepository
                    .findByEmailOuLogin(utilisateur.sujet())
                    .filter(Administrateur::isActif)
                    .orElseThrow(this::refreshTokenInvalide);

            return renouvelerAdmin(administrateur);
        }

        throw refreshTokenInvalide();
    }

    @Transactional
    public void deconnecter(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return;
        }

        try {
            var utilisateur = jwtService.validerRefreshToken(refreshToken);

            jetonRafraichissementService.revoquerSiPresent(
                    utilisateur.identifiantToken()
            );
        } catch (AuthentificationException exception) {
            // Le logout reste idempotent, même avec un cookie expiré ou invalide.
        }
    }

    private void authentifier(
            String identite,
            String motDePasse
    ) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            identite,
                            motDePasse
                    )
            );
        } catch (AuthenticationException exception) {
            throw new AuthentificationException(
                    MESSAGE_IDENTIFIANTS_INVALIDES
            );
        }
    }

    private AuthJoueurResponse convertirJoueur(Membre membre) {
        Site siteRattachement = membre.getSiteRattachement();
        JwtService.TokenGenere token = jwtService.genererTokenJoueur(membre);

        return new AuthJoueurResponse(
                membre.getId(),
                membre.getMatricule(),
                membre.getNom(),
                membre.getPrenom(),
                membre.getCategorieMembre(),
                siteRattachement == null ? null : siteRattachement.getId(),
                siteRattachement == null ? null : siteRattachement.getNom(),
                membre.isActif(),
                membre.getSoldeCredit(),
                token.valeur(),
                token.expiration()
        );
    }

    private AuthAdminResponse convertirAdmin(Administrateur administrateur) {
        Site site = administrateur.getSite();
        JwtService.TokenGenere token = jwtService.genererTokenAdmin(administrateur);

        return new AuthAdminResponse(
                administrateur.getId(),
                administrateur.getEmailOuLogin(),
                administrateur.getNom(),
                administrateur.getPrenom(),
                administrateur.getRoleAdministrateur(),
                site == null ? null : site.getId(),
                site == null ? null : site.getNom(),
                administrateur.isActif(),
                token.valeur(),
                token.expiration()
        );
    }

    private ResultatAuthentification<RafraichissementTokenResponse>
    renouvelerJoueur(Membre membre) {
        JwtService.TokenGenere accessToken =
                jwtService.genererTokenJoueur(membre);
        JwtService.TokenGenere refreshToken =
                jwtService.genererRefreshTokenJoueur(membre);

        return resultatRafraichissement(
                accessToken,
                refreshToken,
                membre.getMatricule(),
                JwtService.TYPE_UTILISATEUR_JOUEUR
        );
    }

    private ResultatAuthentification<RafraichissementTokenResponse>
    renouvelerAdmin(Administrateur administrateur) {
        JwtService.TokenGenere accessToken =
                jwtService.genererTokenAdmin(administrateur);
        JwtService.TokenGenere refreshToken =
                jwtService.genererRefreshTokenAdmin(administrateur);

        return resultatRafraichissement(
                accessToken,
                refreshToken,
                administrateur.getEmailOuLogin(),
                JwtService.TYPE_UTILISATEUR_ADMIN
        );
    }

    private ResultatAuthentification<RafraichissementTokenResponse>
    resultatRafraichissement(
            JwtService.TokenGenere accessToken,
            JwtService.TokenGenere refreshToken,
            String sujet,
            String typeUtilisateur
    ) {
        return resultatAvecRefreshToken(
                new RafraichissementTokenResponse(
                        accessToken.valeur(),
                        accessToken.expiration()
                ),
                refreshToken,
                sujet,
                typeUtilisateur
        );
    }

    private <T> ResultatAuthentification<T> resultatAvecRefreshToken(
            T reponse,
            JwtService.TokenGenere refreshToken,
            String sujet,
            String typeUtilisateur
    ) {
        jetonRafraichissementService.enregistrer(
                refreshToken,
                sujet,
                typeUtilisateur
        );

        return new ResultatAuthentification<>(
                reponse,
                refreshToken.valeur()
        );
    }

    private AuthentificationException refreshTokenInvalide() {
        return new AuthentificationException(
                "Refresh token invalide."
        );
    }

    public record ResultatAuthentification<T>(
            T reponse,
            String refreshToken
    ) {
    }
}
