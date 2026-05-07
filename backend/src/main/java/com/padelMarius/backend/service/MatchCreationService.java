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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MatchCreationService {

    private static final Duration DUREE_MATCH = Duration.ofMinutes(90);
    private static final Duration DELAI_ENTRE_MATCHES = Duration.ofMinutes(15);
    private static final BigDecimal PRIX_TOTAL_MATCH = new BigDecimal("60.00");

    private final TerrainRepository terrainRepository;
    private final MembreRepository membreRepository;
    private final DetteRepository detteRepository;
    private final PenaliteRepository penaliteRepository;
    private final PadelMatchRepository padelMatchRepository;
    private final ParticipationRepository participationRepository;
    private final DisponibiliteService disponibiliteService;
    private final ReglesReservationMembreService reglesReservationMembreService;

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

        reglesReservationMembreService.verifierReglesCreationMatch(
                organisateur,
                terrain,
                dateHeureDebut
        );

        verifierAbsenceDetteOuverte(organisateur);
        verifierAbsencePenaliteActive(organisateur);

        LocalDateTime dateHeureFin = dateHeureDebut.plus(DUREE_MATCH);

        verifierDisponibiliteTerrain(terrain, dateHeureDebut, dateHeureFin);
        verifierOrganisateurSansMatchChevauchant(organisateur, dateHeureDebut, dateHeureFin);

        LocalDateTime maintenant = LocalDateTime.now();

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
                matchEnregistre.getTerrain().getSite().getId(),
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
        boolean penaliteActive = penaliteRepository.existsByMembreIdAndStatutPenalite(
                organisateur.getId(),
                StatutPenalite.ACTIVE
        );

        if (penaliteActive) {
            throw new ConfigurationMetierException(
                    "L'organisateur a une pénalité active et ne peut pas créer un nouveau match."
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
                !finNouveauMatch.plus(DELAI_ENTRE_MATCHES).isAfter(debutMatchExistant);

        boolean nouveauMatchCommenceAssezTard =
                !debutNouveauMatch.isBefore(finMatchExistant.plus(DELAI_ENTRE_MATCHES));

        return !(nouveauMatchFinitAssezTot || nouveauMatchCommenceAssezTard);
    }

    private VisibiliteMatch convertirVisibilite(ModeCreation modeCreation) {
        if (modeCreation == ModeCreation.PRIVE) {
            return VisibiliteMatch.PRIVE;
        }

        return VisibiliteMatch.PUBLIC;
    }
}
