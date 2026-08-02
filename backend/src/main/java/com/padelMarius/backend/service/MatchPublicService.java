package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.matchpublic.MatchPublicResponse;
import com.padelMarius.backend.dto.matchpublic.RejoindreMatchPublicRequest;
import com.padelMarius.backend.dto.matchpublic.RejoindreMatchPublicResponse;
import com.padelMarius.backend.dto.paiement.PaiementResponse;
import com.padelMarius.backend.dto.paiement.PayerParticipationRequest;
import com.padelMarius.backend.dto.participation.InscriptionPubliqueRequest;
import com.padelMarius.backend.dto.participation.ParticipationResponse;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.entity.VisibiliteMatch;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.padelMarius.backend.config.ReglesMetier.MONTANT_PARTICIPATION_STANDARD;
import static com.padelMarius.backend.config.ReglesMetier.NOMBRE_JOUEURS_MAXIMUM;

@Service
@RequiredArgsConstructor
public class MatchPublicService {

    private final PadelMatchRepository padelMatchRepository;
    private final ParticipationRepository participationRepository;
    private final ParticipationService participationService;
    private final PaiementService paiementService;
    private final MembreRepository membreRepository;
    private final ReglesReservationMembreService
            reglesReservationMembreService;

    @Transactional(readOnly = true)
    public List<MatchPublicResponse> listerMatchesPublicsDisponibles(
            Long siteId,
            LocalDate date,
            String matriculeJoueur
    ) {
        if (siteId == null) {
            throw new ConfigurationMetierException(
                    "Le site est obligatoire."
            );
        }

        if (date == null) {
            throw new ConfigurationMetierException(
                    "La date est obligatoire."
            );
        }

        if (matriculeJoueur == null || matriculeJoueur.isBlank()) {
            throw new ConfigurationMetierException(
                    "Le joueur connecté est obligatoire."
            );
        }

        Membre joueurConnecte = membreRepository
                .findByMatricule(matriculeJoueur.trim())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Joueur connecté introuvable."
                ));

        List<Participation> participationsJoueur =
                participationRepository.findByMembreId(
                        joueurConnecte.getId()
                );

        LocalDateTime debutJour = date.atStartOfDay();
        LocalDateTime finJour = date.plusDays(1).atStartOfDay();

        return padelMatchRepository
                .findByVisibiliteCouranteAndEtatCycleAndDateHeureDebutGreaterThanEqualAndDateHeureDebutBefore(
                        VisibiliteMatch.PUBLIC,
                        EtatCycleMatch.A_VENIR,
                        debutJour,
                        finJour
                )
                .stream()
                .filter(this::estSurTerrainEtSiteActifs)
                .filter(match -> appartientAuSite(match, siteId))
                .map(match -> convertirEnMatchPublicResponse(
                        match,
                        joueurConnecte,
                        participationsJoueur
                ))
                .filter(response -> response.placesDisponibles() > 0)
                .toList();
    }

    private boolean estSurTerrainEtSiteActifs(PadelMatch match) {
        Terrain terrain = match.getTerrain();

        return terrain != null
                && terrain.isActif()
                && terrain.getSite() != null
                && terrain.getSite().isActif();
    }

    @Transactional
    public RejoindreMatchPublicResponse rejoindreEtPayer(
            Long matchId,
            RejoindreMatchPublicRequest request
    ) {
        if (request == null || request.matriculeJoueur() == null || request.matriculeJoueur().isBlank()) {
            throw new ConfigurationMetierException("Le matricule du joueur est obligatoire.");
        }

        String matricule = request.matriculeJoueur().trim();

        ParticipationResponse participation = participationService.inscrireParticipantPublic(
                matchId,
                new InscriptionPubliqueRequest(matricule)
        );

        PaiementResponse paiement = paiementService.payerParticipation(
                participation.participationId(),
                new PayerParticipationRequest(MONTANT_PARTICIPATION_STANDARD)
        );

        Membre membre = membreRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Membre introuvable avec le matricule " + matricule
                ));

        return new RejoindreMatchPublicResponse(
                participation.matchId(),
                participation.participationId(),
                paiement.paiementId(),
                matricule,
                paiement.montantTotalDebite(),
                paiement.statutParticipation(),
                membre.getSoldeCredit()
        );
    }

    private boolean appartientAuSite(PadelMatch match, Long siteId) {
        Terrain terrain = match.getTerrain();

        if (terrain == null || terrain.getSite() == null) {
            return false;
        }

        return Objects.equals(terrain.getSite().getId(), siteId);
    }

    private MatchPublicResponse convertirEnMatchPublicResponse(
            PadelMatch match,
            Membre joueurConnecte,
            List<Participation> participationsJoueur
    ) {
        Terrain terrain = match.getTerrain();
        Site site = terrain.getSite();

        int nombreParticipantsActifs =
                compterParticipantsActifs(match);
        int placesDisponibles =
                NOMBRE_JOUEURS_MAXIMUM
                        - nombreParticipantsActifs;

        boolean participeDeja =
                participationRepository
                        .existsByMatchIdAndMembreId(
                                match.getId(),
                                joueurConnecte.getId()
                        );

        boolean conflitHoraire =
                participationService.aUnConflitHoraire(
                        match,
                        participationsJoueur
                );

        String motifRestrictionReservation =
                determinerMotifRestrictionReservation(
                        joueurConnecte,
                        terrain,
                        match.getDateHeureDebut()
                );

        boolean peutRejoindre =
                placesDisponibles > 0
                        && !participeDeja
                        && !conflitHoraire
                        && motifRestrictionReservation == null;

        String motifNonEligibilite =
                determinerMotifNonEligibilite(
                        participeDeja,
                        conflitHoraire,
                        motifRestrictionReservation
                );

        return new MatchPublicResponse(
                match.getId(),
                site.getId(),
                site.getNom(),
                terrain.getId(),
                terrain.getNumero(),
                match.getDateHeureDebut(),
                match.getDateHeureFin(),
                nombreParticipantsActifs,
                placesDisponibles,
                match.getPrixTotal(),
                MONTANT_PARTICIPATION_STANDARD,
                peutRejoindre,
                motifNonEligibilite
        );
    }

    private String determinerMotifRestrictionReservation(
            Membre joueur,
            Terrain terrain,
            LocalDateTime dateHeureDebut
    ) {
        try {
            reglesReservationMembreService
                    .verifierReglesReservation(
                            joueur,
                            terrain,
                            dateHeureDebut
                    );

            return null;
        } catch (ConfigurationMetierException exception) {
            return exception.getMessage();
        }
    }

    private String determinerMotifNonEligibilite(
            boolean participeDeja,
            boolean conflitHoraire,
            String motifRestrictionReservation
    ) {
        if (participeDeja) {
            return "Tu participes déjà à ce match.";
        }

        if (conflitHoraire) {
            return "Tu participes déjà à un autre match sur ce créneau.";
        }

        return motifRestrictionReservation;
    }

    private int compterParticipantsActifs(PadelMatch match) {
        List<Participation> participations = participationRepository.findByMatchId(match.getId());

        return (int) participations.stream()
                .filter(participation -> participation.getStatutParticipation() != StatutParticipation.LIBEREE)
                .count();
    }
}
