package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.traitement.TraitementVeilleResponse;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.Penalite;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.StatutPenalite;
import com.padelMarius.backend.entity.VisibiliteMatch;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import com.padelMarius.backend.repository.PenaliteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TraitementVeilleService {

    private static final int NOMBRE_JOUEURS_REQUIS = 4;
    private static final long DUREE_PENALITE_JOURS = 7L;
    private static final String TYPE_PENALITE_MATCH_PRIVE_INCOMPLET = "RESERVATION_PRIVEE_INCOMPLETE";
    private static final String MOTIF_PENALITE_MATCH_PRIVE_INCOMPLET =
            "Match privé incomplet la veille du match.";

    private final PadelMatchRepository padelMatchRepository;
    private final ParticipationRepository participationRepository;
    private final PenaliteRepository penaliteRepository;
    private final Clock clock;

    @Transactional
    public TraitementVeilleResponse traiterVeille(LocalDate dateTraitement) {
        if (dateTraitement == null) {
            throw new ConfigurationMetierException("La date de traitement est obligatoire.");
        }

        LocalDate dateMatchTraitee = dateTraitement.plusDays(1);
        LocalDateTime debutJour = dateMatchTraitee.atStartOfDay();
        LocalDateTime finJourExclue = dateMatchTraitee.plusDays(1).atStartOfDay();

        List<PadelMatch> matches = padelMatchRepository
                .findByDateHeureDebutGreaterThanEqualAndDateHeureDebutBefore(
                        debutJour,
                        finJourExclue
                );

        LocalDateTime maintenant = LocalDateTime.now(clock);

        int matchesPassesPublics = 0;
        int participationsLiberees = 0;
        int penalitesCreees = 0;

        for (PadelMatch match : matches) {
            if (match.getEtatCycle() != EtatCycleMatch.A_VENIR) {
                continue;
            }

            List<Participation> participations = participationRepository.findByMatchId(match.getId());

            participationsLiberees += libererParticipationsJoueurNonPayees(
                    participations,
                    maintenant
            );

            if (match.getVisibiliteCourante() == VisibiliteMatch.PRIVE
                    && matchEstIncomplet(participations)) {

                passerMatchEnPublic(match, maintenant);
                matchesPassesPublics++;

                // La pénalité n'est plus créée à J-1.
// À J-1, le match privé incomplet devient public.
// La pénalité sera créée au moment du match si le match reste incomplet.
            }
        }

        return new TraitementVeilleResponse(
                dateTraitement,
                dateMatchTraitee,
                matches.size(),
                matchesPassesPublics,
                participationsLiberees,
                penalitesCreees
        );
    }

    private int libererParticipationsJoueurNonPayees(
            List<Participation> participations,
            LocalDateTime maintenant
    ) {
        int compteur = 0;

        for (Participation participation : participations) {
            boolean estJoueur = participation.getRoleParticipation() == RoleParticipation.JOUEUR;
            boolean estEnAttentePaiement =
                    participation.getStatutParticipation() == StatutParticipation.EN_ATTENTE_PAIEMENT;

            if (estJoueur && estEnAttentePaiement) {
                participation.setStatutParticipation(StatutParticipation.LIBEREE);
                participation.setDateLiberation(maintenant);
                participationRepository.save(participation);
                compteur++;
            }
        }

        return compteur;
    }

    private boolean matchEstIncomplet(List<Participation> participations) {
        long nombreParticipantsActifs = participations.stream()
                .filter(participation ->
                        participation.getStatutParticipation() != StatutParticipation.LIBEREE
                )
                .count();

        return nombreParticipantsActifs < NOMBRE_JOUEURS_REQUIS;
    }

    private void passerMatchEnPublic(PadelMatch match, LocalDateTime maintenant) {
        match.setVisibiliteCourante(VisibiliteMatch.PUBLIC);

        if (match.getDatePassagePublic() == null) {
            match.setDatePassagePublic(maintenant);
        }

        padelMatchRepository.save(match);
    }

    private int creerPenaliteOrganisateurSiNecessaire(
            PadelMatch match,
            List<Participation> participations,
            LocalDateTime maintenant
    ) {
        boolean penaliteExisteDeja = !penaliteRepository
                .findByMatchSourceId(match.getId())
                .isEmpty();

        if (penaliteExisteDeja) {
            return 0;
        }

        Participation participationOrganisateur = trouverParticipationOrganisateur(participations);
        Membre organisateur = participationOrganisateur.getMembre();

        if (organisateur == null) {
            throw new ConfigurationMetierException(
                    "La participation organisateur doit être liée à un membre."
            );
        }

        Penalite penalite = Penalite.builder()
                .membre(organisateur)
                .matchSource(match)
                .typePenalite(TYPE_PENALITE_MATCH_PRIVE_INCOMPLET)
                .motif(MOTIF_PENALITE_MATCH_PRIVE_INCOMPLET)
                .dateDebut(maintenant)
                .dateFin(maintenant.plusDays(DUREE_PENALITE_JOURS))
                .statutPenalite(StatutPenalite.ACTIVE)
                .build();

        penaliteRepository.save(penalite);

        return 1;
    }

    private Participation trouverParticipationOrganisateur(List<Participation> participations) {
        return participations.stream()
                .filter(participation ->
                        participation.getRoleParticipation() == RoleParticipation.ORGANISATEUR
                )
                .filter(participation ->
                        participation.getStatutParticipation() != StatutParticipation.LIBEREE
                )
                .findFirst()
                .orElseThrow(() -> new ConfigurationMetierException(
                        "Le match doit avoir une participation organisateur active."
                ));
    }
}