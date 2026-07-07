package com.padelMarius.backend.service;

import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.repository.DetteRepository;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import com.padelMarius.backend.security.JwtService;
import com.padelMarius.backend.security.JwtUtilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service("joueurAuthorizationService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoueurAuthorizationService {

    private final MembreRepository membreRepository;
    private final ParticipationRepository participationRepository;
    private final DetteRepository detteRepository;
    private final PadelMatchRepository padelMatchRepository;

    public boolean estJoueurActif(Authentication authentication) {
        return chargerJoueurActif(authentication).isPresent();
    }

    public boolean peutAgirPourMatricule(
            Authentication authentication,
            String matriculeDemande
    ) {
        return chargerJoueurActif(authentication)
                .map(joueur -> Objects.equals(
                        joueur.getMatricule(),
                        normaliserMatricule(matriculeDemande)
                ))
                .orElse(false);
    }

    public boolean peutAccederParticipation(
            Authentication authentication,
            Long participationId
    ) {
        if (participationId == null) {
            return false;
        }

        Optional<Membre> joueurConnecte = chargerJoueurActif(authentication);

        if (joueurConnecte.isEmpty()) {
            return false;
        }

        return participationRepository.findById(participationId)
                .map(Participation::getMembre)
                .map(membre -> estMemeMembre(
                        joueurConnecte.get(),
                        membre
                ))
                .orElse(false);
    }

    public boolean peutAccederDette(
            Authentication authentication,
            Long detteId
    ) {
        if (detteId == null) {
            return false;
        }

        Optional<Membre> joueurConnecte = chargerJoueurActif(authentication);

        if (joueurConnecte.isEmpty()) {
            return false;
        }

        return detteRepository.findById(detteId)
                .map(Dette::getMembreResponsable)
                .map(membre -> estMemeMembre(
                        joueurConnecte.get(),
                        membre
                ))
                .orElse(false);
    }

    public boolean estOrganisateurDuMatch(
            Authentication authentication,
            Long matchId
    ) {
        if (matchId == null || !padelMatchRepository.existsById(matchId)) {
            return false;
        }

        Optional<Membre> joueurConnecte = chargerJoueurActif(authentication);

        if (joueurConnecte.isEmpty()) {
            return false;
        }

        return participationRepository.findByMatchId(matchId)
                .stream()
                .filter(participation ->
                        participation.getRoleParticipation()
                                == RoleParticipation.ORGANISATEUR
                )
                .filter(participation ->
                        participation.getStatutParticipation()
                                != StatutParticipation.LIBEREE
                )
                .map(Participation::getMembre)
                .filter(Objects::nonNull)
                .anyMatch(membre ->
                        estMemeMembre(joueurConnecte.get(), membre)
                );
    }

    private Optional<Membre> chargerJoueurActif(Authentication authentication) {
        return extrairePrincipal(authentication)
                .filter(utilisateur ->
                        JwtService.TYPE_UTILISATEUR_JOUEUR.equals(
                                utilisateur.typeUtilisateur()
                        )
                )
                .flatMap(utilisateur ->
                        membreRepository.findByMatricule(utilisateur.sujet())
                )
                .filter(Membre::isActif);
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

    private String normaliserMatricule(String matricule) {
        return matricule == null ? "" : matricule.trim();
    }

    private boolean estMemeMembre(
            Membre joueurConnecte,
            Membre membreCible
    ) {
        if (membreCible == null) {
            return false;
        }

        if (joueurConnecte.getId() != null
                && membreCible.getId() != null) {
            return Objects.equals(
                    joueurConnecte.getId(),
                    membreCible.getId()
            );
        }

        return Objects.equals(
                joueurConnecte.getMatricule(),
                membreCible.getMatricule()
        );
    }
}