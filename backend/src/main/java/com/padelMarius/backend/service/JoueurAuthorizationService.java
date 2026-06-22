package com.padelMarius.backend.service;

import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.exception.AutorisationException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.DetteRepository;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import com.padelMarius.backend.security.JwtService;
import com.padelMarius.backend.security.JwtUtilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoueurAuthorizationService {

    private static final String JOUEUR_INTROUVABLE =
            "Joueur introuvable ou non authentifié.";

    private final MembreRepository membreRepository;
    private final ParticipationRepository participationRepository;
    private final DetteRepository detteRepository;
    private final PadelMatchRepository padelMatchRepository;
    private final JwtService jwtService;

    public void verifierJoueurConnecte(String authorizationHeader) {
        chargerJoueurActif(authorizationHeader);
    }

    public void verifierAccesMatricule(
            String authorizationHeader,
            String matriculeDemande
    ) {
        Membre joueurConnecte = chargerJoueurActif(authorizationHeader);

        String matriculeNormalise = matriculeDemande == null
                ? ""
                : matriculeDemande.trim();

        if (!joueurConnecte.getMatricule().equals(matriculeNormalise)) {
            throw new AutorisationException(
                    "Un joueur ne peut agir que pour son propre compte."
            );
        }
    }

    public void verifierParticipationDuJoueur(
            String authorizationHeader,
            Long participationId
    ) {
        Membre joueurConnecte = chargerJoueurActif(authorizationHeader);

        Participation participation = participationRepository
                .findById(participationId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Participation introuvable avec l'id "
                                + participationId
                ));

        if (!estMemeMembre(
                joueurConnecte,
                participation.getMembre()
        )) {
            throw new AutorisationException(
                    "Cette participation n'appartient pas "
                            + "au joueur connecté."
            );
        }
    }

    public void verifierDetteDuJoueur(
            String authorizationHeader,
            Long detteId
    ) {
        Membre joueurConnecte = chargerJoueurActif(authorizationHeader);

        Dette dette = detteRepository.findById(detteId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Dette introuvable avec l'id " + detteId
                ));

        if (!estMemeMembre(
                joueurConnecte,
                dette.getMembreResponsable()
        )) {
            throw new AutorisationException(
                    "Cette dette n'appartient pas au joueur connecté."
            );
        }
    }

    public void verifierOrganisateurDuMatch(
            String authorizationHeader,
            Long matchId
    ) {
        Membre joueurConnecte = chargerJoueurActif(authorizationHeader);

        if (!padelMatchRepository.existsById(matchId)) {
            throw new RessourceIntrouvableException(
                    "Match introuvable avec l'id " + matchId
            );
        }

        boolean estOrganisateur = participationRepository
                .findByMatchId(matchId)
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
                        estMemeMembre(joueurConnecte, membre)
                );

        if (!estOrganisateur) {
            throw new AutorisationException(
                    "Seul l'organisateur du match "
                            + "peut réaliser cette opération."
            );
        }
    }

    private Membre chargerJoueurActif(String authorizationHeader) {
        JwtUtilisateur utilisateur = jwtService
                .extraireUtilisateurDepuisAuthorization(
                        authorizationHeader
                );

        if (!JwtService.TYPE_UTILISATEUR_JOUEUR.equals(
                utilisateur.typeUtilisateur()
        )) {
            throw new AuthentificationException(
                    "Token joueur requis."
            );
        }

        Membre joueur = membreRepository
                .findByMatricule(utilisateur.sujet())
                .orElseThrow(() ->
                        new AuthentificationException(
                                JOUEUR_INTROUVABLE
                        )
                );

        if (!joueur.isActif()) {
            throw new AuthentificationException(
                    JOUEUR_INTROUVABLE
            );
        }

        return joueur;
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