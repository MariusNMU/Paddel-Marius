package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.dette.DetteResponse;
import com.padelMarius.backend.dto.dette.PaiementDetteResponse;
import com.padelMarius.backend.dto.dette.PayerDetteRequest;
import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Paiement;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.DetteRepository;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
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
public class DetteService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal PRIX_TOTAL_PAR_DEFAUT = new BigDecimal("60.00");

    private final PadelMatchRepository padelMatchRepository;
    private final ParticipationRepository participationRepository;
    private final PaiementRepository paiementRepository;
    private final DetteRepository detteRepository;
    private final MembreRepository membreRepository;
    private final Clock clock;

    @Transactional
    public DetteResponse genererDettePourMatch(Long matchId) {
        PadelMatch match = padelMatchRepository.findById(matchId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Match introuvable avec l'id " + matchId
                ));

        if (detteRepository.findByMatchId(matchId).isPresent()) {
            throw new ConfigurationMetierException("Une dette existe déjà pour ce match.");
        }

        Participation participationOrganisateur = trouverParticipationOrganisateur(matchId);
        Membre responsable = participationOrganisateur.getMembre();

        if (responsable == null) {
            throw new ConfigurationMetierException("La participation organisateur doit être liée à un membre.");
        }

        BigDecimal prixTotal = montantOuPrixDefaut(match.getPrixTotal());
        BigDecimal totalPaye = calculerTotalPayePourMatch(matchId);
        BigDecimal montantRestant = normaliserMontant(prixTotal.subtract(totalPaye));

        if (montantRestant.compareTo(ZERO) <= 0) {
            throw new ConfigurationMetierException("Le match est entièrement payé. Aucune dette ne doit être créée.");
        }

        LocalDateTime maintenant = LocalDateTime.now(clock);

        Dette dette = Dette.builder()
                .match(match)
                .membreResponsable(responsable)
                .montantInitial(montantRestant)
                .montantRestant(montantRestant)
                .dateCreation(maintenant)
                .dateReglement(null)
                .statutDette(StatutDette.OUVERTE)
                .build();

        Dette detteSauvegardee = detteRepository.save(dette);

        return convertirEnDetteResponse(detteSauvegardee);
    }

    @Transactional(readOnly = true)
    public List<DetteResponse> consulterDettesOuvertes(String matricule) {
        String matriculeNormalise = normaliserMatricule(matricule);

        Membre membre = membreRepository.findByMatricule(matriculeNormalise)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Membre introuvable avec le matricule " + matriculeNormalise
                ));

        return detteRepository.findByMembreResponsableIdAndStatutDette(
                        membre.getId(),
                        StatutDette.OUVERTE
                )
                .stream()
                .map(this::convertirEnDetteResponse)
                .toList();
    }

    @Transactional
    public PaiementDetteResponse payerDette(Long detteId, PayerDetteRequest request) {
        Dette dette = detteRepository.findById(detteId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Dette introuvable avec l'id " + detteId
                ));

        verifierPaiementDettePossible(dette, request);

        LocalDateTime maintenant = LocalDateTime.now(clock);
        BigDecimal montantPaiement = normaliserMontant(request.montant());


        Membre responsable = dette.getMembreResponsable();
        debiterSolde(responsable, montantPaiement);


        dette.setMontantRestant(ZERO);
        dette.setDateReglement(maintenant);
        dette.setStatutDette(StatutDette.REGLEE);

        Paiement paiement = Paiement.builder()
                .membre(dette.getMembreResponsable())
                .naturePaiement(NaturePaiement.REGLEMENT_DETTE)
                .montant(montantPaiement)
                .dateHeurePaiement(maintenant)
                .statutPaiement(StatutPaiement.PAYE)
                .participation(null)
                .dette(dette)
                .build();

        Paiement paiementSauvegarde = paiementRepository.save(paiement);
        Dette detteSauvegardee = detteRepository.save(dette);

        return convertirEnPaiementDetteResponse(paiementSauvegarde, detteSauvegardee);
    }

    private Participation trouverParticipationOrganisateur(Long matchId) {
        return participationRepository.findByMatchId(matchId)
                .stream()
                .filter(participation -> participation.getRoleParticipation() == RoleParticipation.ORGANISATEUR)
                .findFirst()
                .orElseThrow(() -> new ConfigurationMetierException(
                        "Le match doit avoir une participation organisateur."
                ));
    }

    private BigDecimal calculerTotalPayePourMatch(Long matchId) {
        return paiementRepository.findByParticipation_Match_IdAndNaturePaiementAndStatutPaiement(
                        matchId,
                        NaturePaiement.PARTICIPATION,
                        StatutPaiement.PAYE
                )
                .stream()
                .map(Paiement::getMontant)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void verifierPaiementDettePossible(Dette dette, PayerDetteRequest request) {
        if (request == null || request.montant() == null) {
            throw new ConfigurationMetierException("Le montant du paiement est obligatoire.");
        }

        if (dette.getStatutDette() != StatutDette.OUVERTE) {
            throw new ConfigurationMetierException("Seule une dette ouverte peut être réglée.");
        }

        if (dette.getMembreResponsable() == null) {
            throw new ConfigurationMetierException("La dette doit être liée à un membre responsable.");
        }

        if (!dette.getMembreResponsable().isActif()) {
            throw new ConfigurationMetierException("Un membre inactif ne peut pas régler une dette.");
        }

        if (paiementRepository.existsByDetteId(dette.getId())) {
            throw new ConfigurationMetierException("Cette dette possède déjà un paiement.");
        }

        BigDecimal montantAttendu = normaliserMontant(dette.getMontantRestant());
        BigDecimal montantRecu = normaliserMontant(request.montant());

        if (montantRecu.compareTo(montantAttendu) != 0) {
            throw new ConfigurationMetierException(
                    "Le montant du paiement doit correspondre au montant restant de la dette."
            );
        }
    }
    private void debiterSolde(Membre membre, BigDecimal montant) {
        if (membre.getSoldeCredit() == null) {
            throw new ConfigurationMetierException("Le solde du membre n'est pas initialisé.");
        }

        if (membre.getSoldeCredit().compareTo(montant) < 0) {
            throw new ConfigurationMetierException("Solde insuffisant pour régler cette dette.");
        }

        membre.setSoldeCredit(membre.getSoldeCredit().subtract(montant));
    }

    private DetteResponse convertirEnDetteResponse(Dette dette) {
        Membre responsable = dette.getMembreResponsable();

        return new DetteResponse(
                dette.getId(),
                dette.getMatch().getId(),
                responsable.getId(),
                responsable.getMatricule(),
                dette.getMontantInitial(),
                dette.getMontantRestant(),
                dette.getStatutDette(),
                dette.getDateCreation(),
                dette.getDateReglement()
        );
    }

    private PaiementDetteResponse convertirEnPaiementDetteResponse(Paiement paiement, Dette dette) {
        Membre membre = dette.getMembreResponsable();

        return new PaiementDetteResponse(
                paiement.getId(),
                dette.getId(),
                membre.getId(),
                membre.getMatricule(),
                paiement.getNaturePaiement(),
                paiement.getMontant(),
                paiement.getStatutPaiement(),
                dette.getStatutDette(),
                paiement.getDateHeurePaiement(),
                dette.getDateReglement()
        );
    }

    private BigDecimal montantOuPrixDefaut(BigDecimal montant) {
        if (montant == null) {
            return PRIX_TOTAL_PAR_DEFAUT;
        }

        return normaliserMontant(montant);
    }

    private BigDecimal normaliserMontant(BigDecimal montant) {
        if (montant == null) {
            return ZERO;
        }

        return montant.setScale(2, RoundingMode.HALF_UP);
    }

    private String normaliserMatricule(String matricule) {
        if (matricule == null || matricule.isBlank()) {
            throw new ConfigurationMetierException("Le matricule est obligatoire.");
        }

        return matricule.trim();
    }
}