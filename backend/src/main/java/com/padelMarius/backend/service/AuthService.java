package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.auth.AuthAdminResponse;
import com.padelMarius.backend.dto.auth.AuthJoueurResponse;
import com.padelMarius.backend.dto.auth.ConnexionAdminRequest;
import com.padelMarius.backend.dto.auth.ConnexionJoueurRequest;
import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.repository.AdministrateurRepository;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.security.IdentiteAuthentification;
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

    @Transactional(readOnly = true)
    public AuthJoueurResponse authentifierJoueur(
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

        return convertirJoueur(membre);
    }

    @Transactional(readOnly = true)
    public AuthAdminResponse authentifierAdmin(
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

        return convertirAdmin(administrateur);
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
}
