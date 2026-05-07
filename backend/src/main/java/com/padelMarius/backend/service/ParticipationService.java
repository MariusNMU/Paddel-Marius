package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.participation.AjouterParticipantPriveRequest;
import com.padelMarius.backend.dto.participation.InscriptionPubliqueRequest;
import com.padelMarius.backend.dto.participation.ParticipationResponse;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.ModeEntreeParticipation;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.VisibiliteMatch;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private static final int NOMBRE_MAX_PARTICIPANTS = 4;
    private static final Duration DUREE_MATCH = Duration.ofMinutes(90);
    private static final Duration DELAI_ENTRE_MATCHES = Duration.ofMinutes(15);

    private final PadelMatchRepository padelMatchRepository;
    private final MembreRepository membreRepository;
    private final ParticipationRepository participationRepository;

    @Transactional
    public ParticipationResponse ajouterParticipantPrive(
            Long matchId,
            AjouterParticipantPriveRequest request
    ) {
        PadelMatch match = recupererMatch(matchId);

        verifierMatchAVenir(match);
        verifierMatchPrive(match);

        String matricule = normaliserMatricule(request.matriculeJoueur());
        Membre membre = recupererMembre(matricule);

        return creerParticipation(
                match,
                membre,
                ModeEntreeParticipation.INVITATION_PRIVEE
        );
    }

    @Transactional
    public ParticipationResponse inscrireParticipantPublic(
            Long matchId,
            InscriptionPubliqueRequest request
    ) {
        PadelMatch match = recupererMatch(matchId);

        verifierMatchAVenir(match);
        verifierMatchPublic(match);

        String matricule = normaliserMatricule(request.matriculeJoueur());
        Membre membre = recupererMembre(matricule);

        return creerParticipation(
                match,
                membre,
                ModeEntreeParticipation.INSCRIPTION_PUBLIQUE
        );
    }

    private PadelMatch recupererMatch(Long matchId) {
        return padelMatchRepository.findById(matchId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Match introuvable avec l'id " + matchId
                ));
    }

    private Membre recupererMembre(String matricule) {
        return membreRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Membre introuvable avec le matricule " + matricule
                ));
    }

    private String normaliserMatricule(String matricule) {
        if (matricule == null) {
            return "";
        }

        return matricule.trim();
    }

    private void verifierMatchAVenir(PadelMatch match) {
        if (match.getEtatCycle() != EtatCycleMatch.A_VENIR) {
            throw new ConfigurationMetierException(
                    "Le match n'accepte plus de nouvelle participation."
            );
        }
    }

    private void verifierMatchPrive(PadelMatch match) {
        if (match.getVisibiliteCourante() != VisibiliteMatch.PRIVE) {
            throw new ConfigurationMetierException(
                    "L'ajout prive est autorise uniquement sur un match prive."
            );
        }
    }

    private void verifierMatchPublic(PadelMatch match) {
        if (match.getVisibiliteCourante() != VisibiliteMatch.PUBLIC) {
            throw new ConfigurationMetierException(
                    "L'inscription publique est autorisee uniquement sur un match public."
            );
        }
    }

    private ParticipationResponse creerParticipation(
            PadelMatch match,
            Membre membre,
            ModeEntreeParticipation modeEntree
    ) {
        verifierMembreActif(membre);
        verifierNombreParticipants(match);
        verifierMembrePasDejaParticipant(match, membre);
        verifierMembreSansMatchChevauchant(membre, match);

        Participation participation = Participation.builder()
                .match(match)
                .membre(membre)
                .roleParticipation(RoleParticipation.JOUEUR)
                .modeEntree(modeEntree)
                .statutParticipation(StatutParticipation.EN_ATTENTE_PAIEMENT)
                .dateAffectation(LocalDateTime.now())
                .build();

        Participation participationEnregistree = participationRepository.save(participation);

        return convertirEnResponse(participationEnregistree);
    }

    private void verifierMembreActif(Membre membre) {
        if (!membre.isActif()) {
            throw new ConfigurationMetierException(
                    "Le membre est inactif et ne peut pas rejoindre un match."
            );
        }
    }

    private void verifierNombreParticipants(PadelMatch match) {
        List<Participation> participations = participationRepository.findByMatchId(match.getId());

        long nombreParticipantsActifs = participations.stream()
                .filter(participation -> participation.getStatutParticipation() != StatutParticipation.LIBEREE)
                .count();

        if (nombreParticipantsActifs >= NOMBRE_MAX_PARTICIPANTS) {
            throw new ConfigurationMetierException(
                    "Le match contient deja 4 participants."
            );
        }
    }

    private void verifierMembrePasDejaParticipant(PadelMatch match, Membre membre) {
        boolean dejaParticipant = participationRepository.existsByMatchIdAndMembreId(
                match.getId(),
                membre.getId()
        );

        if (dejaParticipant) {
            throw new ConfigurationMetierException(
                    "Le membre participe deja a ce match."
            );
        }
    }

    private void verifierMembreSansMatchChevauchant(Membre membre, PadelMatch nouveauMatch) {
        List<Participation> participations = participationRepository.findByMembreId(membre.getId());

        boolean conflit = participations.stream()
                .filter(participation -> participation.getStatutParticipation() != StatutParticipation.LIBEREE)
                .map(Participation::getMatch)
                .filter(Objects::nonNull)
                .filter(matchExistant -> !Objects.equals(matchExistant.getId(), nouveauMatch.getId()))
                .anyMatch(matchExistant -> chevaucheAvecPauseObligatoire(nouveauMatch, matchExistant));

        if (conflit) {
            throw new ConfigurationMetierException(
                    "Le membre participe deja a un autre match sur ce creneau."
            );
        }
    }

    private boolean chevaucheAvecPauseObligatoire(
            PadelMatch nouveauMatch,
            PadelMatch matchExistant
    ) {
        LocalDateTime debutNouveauMatch = nouveauMatch.getDateHeureDebut();
        LocalDateTime finNouveauMatch = determinerFinMatch(nouveauMatch);

        LocalDateTime debutMatchExistant = matchExistant.getDateHeureDebut();
        LocalDateTime finMatchExistant = determinerFinMatch(matchExistant);

        if (
                debutNouveauMatch == null
                        || finNouveauMatch == null
                        || debutMatchExistant == null
                        || finMatchExistant == null
        ) {
            return false;
        }

        boolean nouveauMatchFinitAssezTot =
                !finNouveauMatch.plus(DELAI_ENTRE_MATCHES).isAfter(debutMatchExistant);

        boolean nouveauMatchCommenceAssezTard =
                !debutNouveauMatch.isBefore(finMatchExistant.plus(DELAI_ENTRE_MATCHES));

        return !(nouveauMatchFinitAssezTot || nouveauMatchCommenceAssezTard);
    }

    private LocalDateTime determinerFinMatch(PadelMatch match) {
        if (match.getDateHeureFin() != null) {
            return match.getDateHeureFin();
        }

        if (match.getDateHeureDebut() == null) {
            return null;
        }

        return match.getDateHeureDebut().plus(DUREE_MATCH);
    }

    private ParticipationResponse convertirEnResponse(Participation participation) {
        return new ParticipationResponse(
                participation.getId(),
                participation.getMatch().getId(),
                participation.getMembre().getId(),
                participation.getMembre().getMatricule(),
                participation.getRoleParticipation(),
                participation.getModeEntree(),
                participation.getStatutParticipation(),
                participation.getDateAffectation()
        );
    }
}