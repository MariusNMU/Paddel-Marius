package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.paiement.PaiementResponse;
import com.padelMarius.backend.dto.paiement.PayerParticipationRequest;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.Paiement;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.PaiementRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaiementService {

    private static final BigDecimal MONTANT_PARTICIPATION_STANDARD = new BigDecimal("15.00");

    private final ParticipationRepository participationRepository;
    private final PaiementRepository paiementRepository;
    private final Clock clock;

    @Transactional
    public PaiementResponse payerParticipation(Long participationId, PayerParticipationRequest request) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Participation introuvable avec l'id " + participationId
                ));

        verifierPaiementPossible(participation, request);

        LocalDateTime datePaiement = LocalDateTime.now(clock);

        participation.setStatutParticipation(StatutParticipation.CONFIRMEE);
        participation.setDateConfirmation(datePaiement);

        Paiement paiement = Paiement.builder()
                .membre(participation.getMembre())
                .naturePaiement(NaturePaiement.PARTICIPATION)
                .montant(MONTANT_PARTICIPATION_STANDARD)
                .dateHeurePaiement(datePaiement)
                .statutPaiement(StatutPaiement.PAYE)
                .participation(participation)
                .dette(null)
                .build();

        Paiement paiementSauvegarde = paiementRepository.save(paiement);
        participationRepository.save(participation);

        return convertirEnResponse(paiementSauvegarde, participation);
    }

    private void verifierPaiementPossible(Participation participation, PayerParticipationRequest request) {
        if (request == null || request.montant() == null) {
            throw new ConfigurationMetierException("Le montant du paiement est obligatoire.");
        }

        if (request.montant().compareTo(MONTANT_PARTICIPATION_STANDARD) != 0) {
            throw new ConfigurationMetierException("Le montant d'une participation doit être de 15.00 euros.");
        }

        Membre membre = participation.getMembre();

        if (membre == null) {
            throw new ConfigurationMetierException("La participation doit être liée à un membre.");
        }

        if (!membre.isActif()) {
            throw new ConfigurationMetierException("Un membre inactif ne peut pas payer une participation.");
        }

        if (participation.getStatutParticipation() == StatutParticipation.LIBEREE) {
            throw new ConfigurationMetierException("Une participation libérée ne peut pas être payée.");
        }

        if (participation.getStatutParticipation() == StatutParticipation.CONFIRMEE) {
            throw new ConfigurationMetierException("Cette participation est déjà confirmée.");
        }

        if (paiementRepository.existsByParticipationId(participation.getId())) {
            throw new ConfigurationMetierException("Cette participation possède déjà un paiement.");
        }
    }

    private PaiementResponse convertirEnResponse(Paiement paiement, Participation participation) {
        Membre membre = participation.getMembre();

        return new PaiementResponse(
                paiement.getId(),
                participation.getId(),
                membre.getId(),
                membre.getMatricule(),
                paiement.getNaturePaiement(),
                paiement.getMontant(),
                paiement.getStatutPaiement(),
                participation.getStatutParticipation(),
                paiement.getDateHeurePaiement(),
                participation.getDateConfirmation()
        );
    }
}