package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.disponibilite.CreneauDisponibiliteResponse;
import com.padelMarius.backend.dto.disponibilite.DisponibilitesResponse;
import com.padelMarius.backend.dto.match.CreerMatchRequest;
import com.padelMarius.backend.dto.match.MatchResponse;
import com.padelMarius.backend.entity.*;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.padelMarius.backend.config.ReglesMetier.DUREE_MATCH;
import static com.padelMarius.backend.config.ReglesMetier.PAUSE_ENTRE_MATCHES;
import static com.padelMarius.backend.config.ReglesMetier.PRIX_TOTAL_MATCH;

@Service
@RequiredArgsConstructor
public class MatchCreationService {

    private final TerrainRepository terrainRepository;
    private final MembreRepository membreRepository;
    private final DetteRepository detteRepository;
    private final PenaliteRepository penaliteRepository;
    private final PadelMatchRepository padelMatchRepository;
    private final ParticipationRepository participationRepository;
    private final DisponibiliteService disponibiliteService;
    private final ReglesReservationMembreService reglesReservationMembreService;
    private final Clock clock;

    @Transactional
    public MatchResponse creerMatch(CreerMatchRequest request) {
        Terrain terrain = terrainRepository.findById(request.terrainId())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Terrain introuvable avec l'id " + request.terrainId()
                ));

        verifierTerrainReservable(terrain);

        String matricule = request.matriculeOrganisateur().trim();

        Membre organisateur = membreRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Membre introuvable avec le matricule " + matricule
                ));

        verifierOrganisateurReservable(organisateur);

        LocalDateTime dateHeureDebut = request.dateHeureDebut();
        verifierMatchDansLeFutur(dateHeureDebut);

        reglesReservationMembreService.verifierReglesCreationMatch(
                organisateur,
                terrain,
                dateHeureDebut
        );

        verifierAbsenceDetteOuverte(organisateur);
        verifierAbsencePenaliteActive(organisateur);
        verifierAbsenceParticipationOrganisateurEnAttentePaiement(
                organisateur
        );

        LocalDateTime dateHeureFin = dateHeureDebut.plus(DUREE_MATCH);

        verifierDisponibiliteTerrain(terrain, dateHeureDebut, dateHeureFin);
        verifierOrganisateurSansMatchChevauchant(organisateur, dateHeureDebut, dateHeureFin);

        LocalDateTime maintenant = LocalDateTime.now(clock);

        PadelMatch match = PadelMatch.builder()
                .terrain(terrain)
                .dateHeureDebut(dateHeureDebut)
                .dateHeureFin(dateHeureFin)
                .modeCreation(request.modeCreation())
                .visibiliteCourante(convertirVisibilite(request.modeCreation()))
                .prixTotal(PRIX_TOTAL_MATCH)
                .dateCreation(maintenant)
                .etatCycle(EtatCycleMatch.A_VENIR)
                .build();

        PadelMatch matchEnregistre = padelMatchRepository.save(match);

        Participation participationOrganisateur = Participation.builder()
                .match(matchEnregistre)
                .membre(organisateur)
                .roleParticipation(RoleParticipation.ORGANISATEUR)
                .modeEntree(ModeEntreeParticipation.CREATION)
                .statutParticipation(StatutParticipation.EN_ATTENTE_PAIEMENT)
                .dateAffectation(maintenant)
                .build();

        Participation participationEnregistree = participationRepository.save(participationOrganisateur);

        return new MatchResponse(
                matchEnregistre.getId(),
                matchEnregistre.getTerrain().getId(),
                matchEnregistre.getTerrain().getNumero(),
                matchEnregistre.getTerrain().getSite().getId(),
                matchEnregistre.getTerrain().getSite().getNom(),
                organisateur.getMatricule(),
                matchEnregistre.getDateHeureDebut(),
                matchEnregistre.getDateHeureFin(),
                matchEnregistre.getModeCreation(),
                matchEnregistre.getVisibiliteCourante(),
                matchEnregistre.getPrixTotal(),
                matchEnregistre.getEtatCycle(),
                participationEnregistree.getId()
        );
    }

    private void verifierMatchDansLeFutur(LocalDateTime dateHeureDebut) {
        LocalDateTime maintenant = LocalDateTime.now(clock);

        if (!dateHeureDebut.isAfter(maintenant)) {
            throw new ConfigurationMetierException(
                    "Impossible d'organiser un match dans le passé ou à l'heure courante."
            );
        }
    }

    private void verifierTerrainReservable(Terrain terrain) {
        if (!terrain.isActif()) {
            throw new ConfigurationMetierException("Le terrain demandé est inactif.");
        }

        if (terrain.getSite() == null) {
            throw new ConfigurationMetierException("Le terrain demandé n'est rattaché à aucun site.");
        }

        if (!terrain.getSite().isActif()) {
            throw new ConfigurationMetierException("Le site du terrain demandé est inactif.");
        }
    }

    private void verifierOrganisateurReservable(Membre organisateur) {
        if (!organisateur.isActif()) {
            throw new ConfigurationMetierException("L'organisateur est inactif.");
        }
    }

    private void verifierAbsenceDetteOuverte(Membre organisateur) {
        boolean detteOuverte = detteRepository.existsByMembreResponsableIdAndStatutDette(
                organisateur.getId(),
                StatutDette.OUVERTE
        );

        if (detteOuverte) {
            throw new ConfigurationMetierException(
                    "L'organisateur a une dette ouverte et ne peut pas créer un nouveau match."
            );
        }
    }

    private void verifierAbsencePenaliteActive(Membre organisateur) {
        LocalDateTime maintenant = LocalDateTime.now(clock);

        List<Penalite> penalitesActives = penaliteRepository.findByMembreIdAndStatutPenalite(
                organisateur.getId(),
                StatutPenalite.ACTIVE
        );

        boolean penaliteEncoreBloquante = false;

        for (Penalite penalite : penalitesActives) {
            if (penalite.getDateFin() != null && !penalite.getDateFin().isAfter(maintenant)) {
                penalite.setStatutPenalite(StatutPenalite.TERMINEE);
            } else {
                penaliteEncoreBloquante = true;
            }
        }

        if (penaliteEncoreBloquante) {
            throw new ConfigurationMetierException(
                    "L'organisateur a une pénalité active et ne peut pas créer un nouveau match."
            );
        }
    }

    private void verifierAbsenceParticipationOrganisateurEnAttentePaiement(
            Membre organisateur
    ) {
        boolean participationOrganisateurEnAttentePaiement =
                participationRepository.findByMembreId(organisateur.getId())
                        .stream()
                        .filter(participation ->
                                participation.getMatch() != null
                        )
                        .filter(participation ->
                                participation.getMatch().getEtatCycle()
                                        != EtatCycleMatch.ANNULE
                        )
                        .anyMatch(participation ->
                                participation.getRoleParticipation()
                                        == RoleParticipation.ORGANISATEUR
                                && participation.getStatutParticipation()
                                        == StatutParticipation.EN_ATTENTE_PAIEMENT
                        );

        if (participationOrganisateurEnAttentePaiement) {
            throw new ConfigurationMetierException(
                    "L'organisateur doit payer sa participation au match "
                            + "déjà organisé avant d'en créer un nouveau."
            );
        }
    }

    private void verifierDisponibiliteTerrain(
            Terrain terrain,
            LocalDateTime dateHeureDebut,
            LocalDateTime dateHeureFin
    ) {
        DisponibilitesResponse disponibilites = disponibiliteService.consulterDisponibilites(
                terrain.getSite().getId(),
                dateHeureDebut.toLocalDate()
        );

        boolean creneauDisponible = disponibilites.creneaux().stream()
                .anyMatch(creneau -> correspondAuCreneauDemande(creneau, terrain, dateHeureDebut, dateHeureFin));

        if (!creneauDisponible) {
            throw new ConfigurationMetierException(
                    "Le terrain n'est pas disponible sur le créneau demandé."
            );
        }
    }

    private boolean correspondAuCreneauDemande(
            CreneauDisponibiliteResponse creneau,
            Terrain terrain,
            LocalDateTime dateHeureDebut,
            LocalDateTime dateHeureFin
    ) {
        return Objects.equals(creneau.terrainId(), terrain.getId())
                && creneau.dateHeureDebut().equals(dateHeureDebut)
                && creneau.dateHeureFin().equals(dateHeureFin);
    }

    private void verifierOrganisateurSansMatchChevauchant(
            Membre organisateur,
            LocalDateTime dateHeureDebut,
            LocalDateTime dateHeureFin
    ) {
        List<Participation> participations = participationRepository.findByMembreId(organisateur.getId());

        boolean conflit = participations.stream()
                .filter(participation -> participation.getStatutParticipation() != StatutParticipation.LIBEREE)
                .map(Participation::getMatch)
                .filter(Objects::nonNull)
                .anyMatch(match -> chevaucheAvecPauseObligatoire(dateHeureDebut, dateHeureFin, match));

        if (conflit) {
            throw new ConfigurationMetierException(
                    "L'organisateur participe déjà à un autre match sur ce créneau."
            );
        }
    }

    private boolean chevaucheAvecPauseObligatoire(
            LocalDateTime debutNouveauMatch,
            LocalDateTime finNouveauMatch,
            PadelMatch matchExistant
    ) {
        LocalDateTime debutMatchExistant = matchExistant.getDateHeureDebut();

        if (debutMatchExistant == null) {
            return false;
        }

        LocalDateTime finMatchExistant = matchExistant.getDateHeureFin();

        if (finMatchExistant == null) {
            finMatchExistant = debutMatchExistant.plus(DUREE_MATCH);
        }

        boolean nouveauMatchFinitAssezTot =
                !finNouveauMatch.plus(PAUSE_ENTRE_MATCHES).isAfter(debutMatchExistant);

        boolean nouveauMatchCommenceAssezTard =
                !debutNouveauMatch.isBefore(finMatchExistant.plus(PAUSE_ENTRE_MATCHES));

        return !(nouveauMatchFinitAssezTot || nouveauMatchCommenceAssezTard);
    }

    private VisibiliteMatch convertirVisibilite(ModeCreation modeCreation) {
        if (modeCreation == ModeCreation.PRIVE) {
            return VisibiliteMatch.PRIVE;
        }

        return VisibiliteMatch.PUBLIC;
    }
}
