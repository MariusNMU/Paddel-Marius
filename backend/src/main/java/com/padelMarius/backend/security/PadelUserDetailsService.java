package com.padelMarius.backend.security;

import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.repository.AdministrateurRepository;
import com.padelMarius.backend.repository.MembreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PadelUserDetailsService implements UserDetailsService {

    private final MembreRepository membreRepository;
    private final AdministrateurRepository administrateurRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        IdentiteAuthentification.Identite identite =
                IdentiteAuthentification.parser(username);

        if (JwtService.TYPE_UTILISATEUR_JOUEUR.equals(
                identite.typeUtilisateur()
        )) {
            return chargerJoueur(identite.sujet());
        }

        return chargerAdmin(identite.sujet());
    }

    private UserDetails chargerJoueur(String matricule) {
        Membre membre = membreRepository.findByMatricule(matricule)
                .filter(this::motDePasseConfigure)
                .orElseThrow(this::utilisateurInconnu);

        List<String> authorities = new ArrayList<>();
        authorities.add("ROLE_JOUEUR");

        if (membre.getCategorieMembre() != null) {
            authorities.add(
                    "ROLE_JOUEUR_" + membre.getCategorieMembre().name()
            );
        }

        return User.withUsername(
                        IdentiteAuthentification.joueur(
                                membre.getMatricule()
                        )
                )
                .password(membre.getMotDePasseHash())
                .disabled(!membre.isActif())
                .authorities(authorities.toArray(String[]::new))
                .build();
    }

    private UserDetails chargerAdmin(String login) {
        Administrateur administrateur = administrateurRepository
                .findByEmailOuLogin(login)
                .filter(this::motDePasseConfigure)
                .orElseThrow(this::utilisateurInconnu);

        List<String> authorities = new ArrayList<>();
        authorities.add("ROLE_ADMIN");

        if (administrateur.getRoleAdministrateur() != null) {
            authorities.add(
                    "ROLE_ADMIN_"
                            + administrateur.getRoleAdministrateur().name()
            );
        }

        return User.withUsername(
                        IdentiteAuthentification.admin(
                                administrateur.getEmailOuLogin()
                        )
                )
                .password(administrateur.getMotDePasseHash())
                .disabled(!administrateur.isActif())
                .authorities(authorities.toArray(String[]::new))
                .build();
    }

    private boolean motDePasseConfigure(Membre membre) {
        return StringUtils.hasText(membre.getMotDePasseHash());
    }

    private boolean motDePasseConfigure(Administrateur administrateur) {
        return StringUtils.hasText(administrateur.getMotDePasseHash());
    }

    private UsernameNotFoundException utilisateurInconnu() {
        return new UsernameNotFoundException("Utilisateur inconnu.");
    }
}
