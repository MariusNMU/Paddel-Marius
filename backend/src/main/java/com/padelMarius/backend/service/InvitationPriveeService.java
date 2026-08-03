package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.invitation.DeclinerInvitationRequest;
import com.padelMarius.backend.dto.invitation.InvitationPriveeResponse;
import com.padelMarius.backend.dto.invitation.InviterJoueurPriveRequest;
import com.padelMarius.backend.dto.participation.AjouterParticipantPriveRequest;
import com.padelMarius.backend.dto.participation.ParticipationResponse;
import com.padelMarius.backend.entity.*;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvitationPriveeService {

    private final PadelMatchRepository padelMatchRepository;
    private final MembreRepository membreRepository;
    private final ParticipationRepository participationRepository;
    private final ParticipationService participationService;
    private final Clock clock;

    @Transactional
    public InvitationPriveeResponse inviterJoueur(Long matchId, InviterJoueurPriveRequest request) {
        PadelMatch match = recupererMatch(matchId);

        verifierMatchPriveDOrigine(match);
        verifierMatchEncorePrive(match);
        verifierOrganisateurDuMatch(match, request.matriculeOrganisateur());

        ParticipationResponse participationCreee = participationService.ajouterParticipantPrive(
                matchId,
                new AjouterParticipantPriveRequest(request.matriculeInvite())
        );

        Participation participation = participationRepository.findById(participationCreee.participationId())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Participation introuvable avec l'id " + participationCreee.participationId()
                ));

        return convertirEnResponse(participation);
    }

    @Transactional(readOnly = true)
    public List<InvitationPriveeResponse> listerInvitationsRecues(String matricule) {
        Membre joueur = recupererMembre(normaliserMatricule(matricule));

        return participationRepository.findByMembreIdAndModeEntreeAndStatutParticipation(
                        joueur.getId(),
                        ModeEntreeParticipation.INVITATION_PRIVEE,
                        StatutParticipation.EN_ATTENTE_PAIEMENT
                )
                .stream()
                .filter(participation -> participation.getMatch() != null)
                .filter(participation -> participation.getMatch().getEtatCycle() == EtatCycleMatch.A_VENIR)
                .map(this::convertirEnResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public int compterInvitationsRecues(String matricule) {
        return listerInvitationsRecues(matricule).size();
    }

    @Transactional
    public InvitationPriveeResponse declinerInvitation(Long participationId, DeclinerInvitationRequest request) {
        Participation participation = participationRepository.findByIdForUpdate(participationId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Participation introuvable avec l'id " + participationId
                ));

        String matriculeJoueur = normaliserMatricule(request.matriculeJoueur());

        if (!participation.getMembre().getMatricule().equals(matriculeJoueur)) {
            throw new ConfigurationMetierException(
                    "Cette invitation ne concerne pas le joueur connecté."
            );
        }

        if (participation.getModeEntree() != ModeEntreeParticipation.INVITATION_PRIVEE) {
            throw new ConfigurationMetierException(
                    "Seule une invitation privée peut être déclinée."
            );
        }

        if (participation.getStatutParticipation() != StatutParticipation.EN_ATTENTE_PAIEMENT) {
            throw new ConfigurationMetierException(
                    "Seule une invitation en attente peut être déclinée."
            );
        }

        participation.setStatutParticipation(StatutParticipation.LIBEREE);
        participation.setDateLiberation(LocalDateTime.now(clock));

        Participation sauvegardee = participationRepository.save(participation);

        return convertirEnResponse(sauvegardee);
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
        if (matricule == null || matricule.isBlank()) {
            throw new ConfigurationMetierException("Le matricule est obligatoire.");
        }

        return matricule.trim();
    }

    private void verifierMatchPriveDOrigine(PadelMatch match) {
        if (match.getModeCreation() != ModeCreation.PRIVE) {
            throw new ConfigurationMetierException(
                    "Les invitations sont autorisées uniquement pour un match créé en privé."
            );
        }
    }

    private void verifierMatchEncorePrive(PadelMatch match) {
        if (match.getVisibiliteCourante() != VisibiliteMatch.PRIVE) {
            throw new ConfigurationMetierException(
                    "Le match est déjà public. Les joueurs doivent rejoindre le match public."
            );
        }
    }

    private void verifierOrganisateurDuMatch(PadelMatch match, String matriculeOrganisateur) {
        String matriculeNormalise = normaliserMatricule(matriculeOrganisateur);

        List<Participation> participations = participationRepository.findByMatchId(match.getId());

        boolean organisateurCorrect = participations.stream()
                .filter(participation -> participation.getRoleParticipation() == RoleParticipation.ORGANISATEUR)
                .filter(participation -> participation.getStatutParticipation() != StatutParticipation.LIBEREE)
                .anyMatch(participation -> participation.getMembre().getMatricule().equals(matriculeNormalise));

        if (!organisateurCorrect) {
            throw new ConfigurationMetierException(
                    "Seul l'organisateur du match peut inviter des joueurs."
            );
        }
    }

    private InvitationPriveeResponse convertirEnResponse(Participation participationInvitee) {
        PadelMatch match = participationInvitee.getMatch();
        Terrain terrain = match.getTerrain();
        Site site = terrain.getSite();
        Membre invite = participationInvitee.getMembre();

        Participation participationOrganisateur = participationRepository.findByMatchId(match.getId())
                .stream()
                .filter(participation -> participation.getRoleParticipation() == RoleParticipation.ORGANISATEUR)
                .filter(participation -> participation.getStatutParticipation() != StatutParticipation.LIBEREE)
                .findFirst()
                .orElseThrow(() -> new ConfigurationMetierException(
                        "Le match doit avoir un organisateur actif."
                ));

        Membre organisateur = participationOrganisateur.getMembre();

        return new InvitationPriveeResponse(
                participationInvitee.getId(),
                match.getId(),
                site.getId(),
                site.getNom(),
                terrain.getId(),
                terrain.getNumero(),
                match.getDateHeureDebut(),
                match.getDateHeureFin(),
                organisateur.getId(),
                organisateur.getMatricule(),
                organisateur.getNom(),
                organisateur.getPrenom(),
                invite.getId(),
                invite.getMatricule(),
                invite.getNom(),
                invite.getPrenom(),
                participationInvitee.getStatutParticipation()
        );
    }
}
