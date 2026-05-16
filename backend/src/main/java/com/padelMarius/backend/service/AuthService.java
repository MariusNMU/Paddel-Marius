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
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.AdministrateurRepository;
import com.padelMarius.backend.repository.MembreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String MESSAGE_IDENTIFIANTS_ADMIN_INVALIDES =
            "Identifiants administrateur invalides.";

    private static final String MESSAGE_IDENTIFIANTS_JOUEUR_INVALIDES =
            "Identifiants joueur invalides.";

    private final MembreRepository membreRepository;
    private final AdministrateurRepository administrateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthJoueurResponse authentifierJoueur(ConnexionJoueurRequest request) {
        if (request == null
                || !StringUtils.hasText(request.matricule())
                || !StringUtils.hasText(request.motDePasse())) {
            throw new ConfigurationMetierException(
                    "Le matricule et le mot de passe sont obligatoires."
            );
        }

        String matricule = request.matricule().trim();

        Membre membre = membreRepository.findByMatricule(matricule)
                .orElseThrow(() -> new AuthentificationException(
                        MESSAGE_IDENTIFIANTS_JOUEUR_INVALIDES
                ));

        if (!membre.isActif()) {
            throw new ConfigurationMetierException("Le membre est inactif.");
        }

        if (!StringUtils.hasText(membre.getMotDePasseHash())) {
            throw new ConfigurationMetierException(
                    "Le mot de passe joueur n'est pas configuré."
            );
        }

        if (!passwordEncoder.matches(request.motDePasse(), membre.getMotDePasseHash())) {
            throw new AuthentificationException(
                    MESSAGE_IDENTIFIANTS_JOUEUR_INVALIDES
            );
        }

        return convertirJoueur(membre);
    }

    @Transactional(readOnly = true)
    public AuthAdminResponse authentifierAdmin(ConnexionAdminRequest request) {
        if (request == null
                || !StringUtils.hasText(request.login())
                || !StringUtils.hasText(request.motDePasse())) {
            throw new ConfigurationMetierException(
                    "Le login et le mot de passe administrateur sont obligatoires."
            );
        }

        String login = request.login().trim();

        Administrateur administrateur = administrateurRepository.findByEmailOuLogin(login)
                .orElseThrow(() -> new AuthentificationException(
                        MESSAGE_IDENTIFIANTS_ADMIN_INVALIDES
                ));

        if (!administrateur.isActif()) {
            throw new ConfigurationMetierException("L'administrateur est inactif.");
        }

        if (!StringUtils.hasText(administrateur.getMotDePasseHash())) {
            throw new ConfigurationMetierException(
                    "Le mot de passe administrateur n'est pas configuré."
            );
        }

        if (!passwordEncoder.matches(request.motDePasse(), administrateur.getMotDePasseHash())) {
            throw new AuthentificationException(
                    MESSAGE_IDENTIFIANTS_ADMIN_INVALIDES
            );
        }

        return convertirAdmin(administrateur);
    }

    private AuthJoueurResponse convertirJoueur(Membre membre) {
        Site siteRattachement = membre.getSiteRattachement();

        return new AuthJoueurResponse(
                membre.getId(),
                membre.getMatricule(),
                membre.getNom(),
                membre.getPrenom(),
                membre.getCategorieMembre(),
                siteRattachement == null ? null : siteRattachement.getId(),
                siteRattachement == null ? null : siteRattachement.getNom(),
                membre.isActif(),
                membre.getSoldeCredit()
        );
    }

    private AuthAdminResponse convertirAdmin(Administrateur administrateur) {
        Site site = administrateur.getSite();

        return new AuthAdminResponse(
                administrateur.getId(),
                administrateur.getEmailOuLogin(),
                administrateur.getNom(),
                administrateur.getPrenom(),
                administrateur.getRoleAdministrateur(),
                site == null ? null : site.getId(),
                site == null ? null : site.getNom(),
                administrateur.isActif()
        );
    }
}