package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.traitement.TraitementVeilleResponse;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.VisibiliteMatch;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.padelMarius.backend.config.ReglesMetier.NOMBRE_JOUEURS_MAXIMUM;

@Service
@RequiredArgsConstructor
public class TraitementVeilleService {

    private final PadelMatchRepository padelMatchRepository;
    private final ParticipationRepository participationRepository;
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
                .findPourVeilleForUpdate(
                        debutJour,
                        finJourExclue
                );

        LocalDateTime maintenant = LocalDateTime.now(clock);

        int matchesPassesPublics = 0;
        int participationsLiberees = 0;

        for (PadelMatch match : matches) {
            if (match.getEtatCycle() != EtatCycleMatch.A_VENIR) {
                continue;
            }

            List<Participation> participations = participationRepository
                    .findByMatchIdForUpdate(match.getId());

            participationsLiberees += libererParticipationsJoueurNonPayees(
                    participations,
                    maintenant
            );

            if (match.getVisibiliteCourante() == VisibiliteMatch.PRIVE
                    && matchEstIncomplet(participations)) {

                passerMatchEnPublic(match, maintenant);
                matchesPassesPublics++;
            }
        }

        return new TraitementVeilleResponse(
                dateTraitement,
                dateMatchTraitee,
                matches.size(),
                matchesPassesPublics,
                participationsLiberees
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

        return nombreParticipantsActifs < NOMBRE_JOUEURS_MAXIMUM;
    }

    private void passerMatchEnPublic(PadelMatch match, LocalDateTime maintenant) {
        match.setVisibiliteCourante(VisibiliteMatch.PUBLIC);

        if (match.getDatePassagePublic() == null) {
            match.setDatePassagePublic(maintenant);
        }

        padelMatchRepository.save(match);
    }

}
