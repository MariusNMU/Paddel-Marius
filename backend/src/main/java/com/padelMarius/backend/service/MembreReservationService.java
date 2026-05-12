package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.reservation.ReservationJoueurResponse;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembreReservationService {

    private final MembreRepository membreRepository;
    private final ParticipationRepository participationRepository;

    @Transactional(readOnly = true)
    public List<ReservationJoueurResponse> consulterReservations(String matricule) {
        String matriculeNormalise = normaliserMatricule(matricule);

        Membre membre = membreRepository.findByMatricule(matriculeNormalise)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Membre introuvable avec le matricule " + matriculeNormalise
                ));

        return participationRepository.findByMembreId(membre.getId())
                .stream()
                .filter(participation -> participation.getStatutParticipation() != StatutParticipation.LIBEREE)
                .filter(participation -> participation.getMatch() != null)
                .sorted(Comparator.comparing(
                        participation -> dateHeureDebutOuMax(participation.getMatch())
                ))
                .map(this::convertirEnResponse)
                .toList();
    }

    private String normaliserMatricule(String matricule) {
        if (matricule == null || matricule.isBlank()) {
            throw new ConfigurationMetierException("Le matricule est obligatoire.");
        }

        return matricule.trim();
    }

    private LocalDateTime dateHeureDebutOuMax(PadelMatch match) {
        if (match == null || match.getDateHeureDebut() == null) {
            return LocalDateTime.MAX;
        }

        return match.getDateHeureDebut();
    }

    private ReservationJoueurResponse convertirEnResponse(Participation participation) {
        PadelMatch match = participation.getMatch();
        Terrain terrain = match.getTerrain();
        Site site = terrain.getSite();

        return new ReservationJoueurResponse(
                participation.getId(),
                match.getId(),
                site.getId(),
                site.getNom(),
                terrain.getId(),
                terrain.getNumero(),
                match.getDateHeureDebut(),
                match.getDateHeureFin(),
                participation.getRoleParticipation(),
                participation.getModeEntree(),
                participation.getStatutParticipation(),
                match.getModeCreation(),
                match.getVisibiliteCourante(),
                match.getEtatCycle(),
                match.getPrixTotal()
        );
    }
}