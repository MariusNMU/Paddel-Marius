package com.padelMarius.backend.service;

import static com.padelMarius.backend.config.ReglesMetier.MONTANT_PARTICIPATION_STANDARD;

import com.padelMarius.backend.dto.paiement.HistoriquePaiementResponse;
import com.padelMarius.backend.dto.paiement.PaiementResponse;
import com.padelMarius.backend.dto.paiement.PayerParticipationRequest;
import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.Paiement;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.DetteRepository;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.PaiementRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaiementService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final ParticipationRepository participationRepository;
    private final PaiementRepository paiementRepository;
    private final DetteRepository detteRepository;
    private final MembreRepository membreRepository;
    private final DetteService detteService;
    private final Clock clock;

    @Transactional
    public PaiementResponse payerParticipationStandard(Long participationId) {
        return payerParticipation(
                participationId,
                new PayerParticipationRequest(MONTANT_PARTICIPATION_STANDARD)
        );
    }

    @Transactional
    public PaiementResponse payerParticipation(Long participationId, PayerParticipationRequest request) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Participation introuvable avec l'id " + participationId
                ));

        verifierPaiementPossible(participation, request);

        Membre membre = participation.getMembre();

        detteService.actualiserDettesOrganisateur(membre);

        Long matchCourantId = participation.getMatch() != null
                ? participation.getMatch().getId()
                : null;

        List<Dette> dettesOuvertes = detteRepository.findByMembreResponsableIdAndStatutDette(
                        membre.getId(),
                        StatutDette.OUVERTE
                )
                .stream()
                .filter(dette -> dette.getMatch() == null
                        || dette.getMatch().getId() == null
                        || !Objects.equals(dette.getMatch().getId(), matchCourantId))
                .toList();

        BigDecimal montantDettesReglees = calculerMontantDettesOuvertes(dettesOuvertes);
        BigDecimal montantTotalDebite = normaliserMontant(
                MONTANT_PARTICIPATION_STANDARD.add(montantDettesReglees)
        );

        debiterSolde(membre, montantTotalDebite);

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

        reglerDettesOuvertes(dettesOuvertes, membre, datePaiement);

        participationRepository.save(participation);

        if (participation.getMatch() != null) {
            detteService.actualiserDettePourMatch(participation.getMatch());
        }

        return convertirEnResponse(
                paiementSauvegarde,
                participation,
                montantDettesReglees,
                montantTotalDebite
        );
    }

    @Transactional(readOnly = true)
    public List<HistoriquePaiementResponse> consulterHistoriquePaiements(String matricule) {
        String matriculeNormalise = normaliserMatricule(matricule);

        Membre membre = membreRepository.findByMatricule(matriculeNormalise)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Membre introuvable avec le matricule " + matriculeNormalise
                ));

        return paiementRepository.findByMembreIdOrderByDateHeurePaiementDesc(membre.getId())
                .stream()
                .map(this::convertirEnHistoriquePaiementResponse)
                .toList();
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
    private void debiterSolde(Membre membre, BigDecimal montant) {
        if (membre.getSoldeCredit() == null) {
            throw new ConfigurationMetierException("Le solde du membre n'est pas initialisé.");
        }

        if (membre.getSoldeCredit().compareTo(montant) < 0) {
            throw new ConfigurationMetierException("Solde insuffisant pour effectuer ce paiement.");
        }

        membre.setSoldeCredit(membre.getSoldeCredit().subtract(montant));
    }

    private BigDecimal calculerMontantDettesOuvertes(List<Dette> dettesOuvertes) {
        return dettesOuvertes.stream()
                .map(Dette::getMontantRestant)
                .map(this::normaliserMontant)
                .reduce(ZERO, BigDecimal::add);
    }

    private void reglerDettesOuvertes(
            List<Dette> dettesOuvertes,
            Membre membre,
            LocalDateTime datePaiement
    ) {
        for (Dette dette : dettesOuvertes) {
            BigDecimal montantDette = normaliserMontant(dette.getMontantRestant());

            if (montantDette.compareTo(ZERO) <= 0) {
                continue;
            }

            if (paiementRepository.existsByDetteId(dette.getId())) {
                continue;
            }

            dette.setMontantRestant(ZERO);
            dette.setDateReglement(datePaiement);
            dette.setStatutDette(StatutDette.REGLEE);

            Paiement paiementDette = Paiement.builder()
                    .membre(membre)
                    .naturePaiement(NaturePaiement.REGLEMENT_DETTE)
                    .montant(montantDette)
                    .dateHeurePaiement(datePaiement)
                    .statutPaiement(StatutPaiement.PAYE)
                    .participation(null)
                    .dette(dette)
                    .build();

            paiementRepository.save(paiementDette);
            detteRepository.save(dette);
        }
    }

    private BigDecimal normaliserMontant(BigDecimal montant) {
        if (montant == null) {
            return ZERO;
        }

        return montant.setScale(2, RoundingMode.HALF_UP);
    }

    private HistoriquePaiementResponse convertirEnHistoriquePaiementResponse(Paiement paiement) {
        Participation participation = paiement.getParticipation();
        Dette dette = paiement.getDette();

        Long participationId = participation != null ? participation.getId() : null;
        Long detteId = dette != null ? dette.getId() : null;

        Long matchId = null;

        if (participation != null && participation.getMatch() != null) {
            matchId = participation.getMatch().getId();
        } else if (dette != null && dette.getMatch() != null) {
            matchId = dette.getMatch().getId();
        }

        Membre membre = paiement.getMembre();

        return new HistoriquePaiementResponse(
                paiement.getId(),
                membre.getId(),
                membre.getMatricule(),
                paiement.getNaturePaiement(),
                paiement.getMontant(),
                paiement.getStatutPaiement(),
                paiement.getDateHeurePaiement(),
                participationId,
                detteId,
                matchId
        );
    }

    private String normaliserMatricule(String matricule) {
        if (matricule == null || matricule.isBlank()) {
            throw new ConfigurationMetierException("Le matricule est obligatoire.");
        }

        return matricule.trim();
    }

    private PaiementResponse convertirEnResponse(
            Paiement paiement,
            Participation participation,
            BigDecimal montantDettesReglees,
            BigDecimal montantTotalDebite
    ) {
        Membre membre = participation.getMembre();

        return new PaiementResponse(
                paiement.getId(),
                participation.getId(),
                membre.getId(),
                membre.getMatricule(),
                paiement.getNaturePaiement(),
                paiement.getMontant(),
                montantDettesReglees,
                montantTotalDebite,
                paiement.getStatutPaiement(),
                participation.getStatutParticipation(),
                paiement.getDateHeurePaiement(),
                participation.getDateConfirmation()
        );
    }
}
