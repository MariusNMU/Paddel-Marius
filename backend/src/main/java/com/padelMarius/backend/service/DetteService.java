package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.dette.DetteResponse;
import com.padelMarius.backend.dto.dette.PaiementDetteResponse;
import com.padelMarius.backend.dto.dette.PayerDetteRequest;
import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Paiement;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutPaiement;
import com.padelMarius.backend.entity.StatutParticipation;
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
import java.util.Optional;

import static com.padelMarius.backend.config.ReglesMetier.PRIX_TOTAL_MATCH;

@Service
@RequiredArgsConstructor
public class DetteService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

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

        verifierMatchArriveAEcheance(match);

        return actualiserDettePourMatch(match)
                .orElseThrow(() -> new ConfigurationMetierException(
                        "Le match est entièrement payé. Aucune dette ne doit être créée."
                ));
    }

    @Transactional
    public Optional<DetteResponse> actualiserDettePourMatch(PadelMatch match) {
        if (match == null
                || match.getId() == null
                || !matchPeutGenererUneDette(match)) {
            return Optional.empty();
        }

        Optional<Dette> detteExistante = detteRepository.findByMatchId(match.getId());

        if (detteExistante.isPresent() && dettePossedeDejaUnPaiement(detteExistante.get())) {
            Dette dette = detteExistante.get();

            dette.setMontantRestant(ZERO);

            if (dette.getDateReglement() == null) {
                dette.setDateReglement(LocalDateTime.now(clock));
            }

            dette.setStatutDette(StatutDette.REGLEE);
            detteRepository.save(dette);

            return Optional.empty();
        }

        Participation participationOrganisateur = trouverParticipationOrganisateur(match.getId());
        Membre responsable = participationOrganisateur.getMembre();

        if (responsable == null) {
            throw new ConfigurationMetierException("La participation organisateur doit être liée à un membre.");
        }

        BigDecimal prixTotal = montantOuPrixDefaut(match.getPrixTotal());
        BigDecimal totalPaye = calculerTotalPayePourMatch(match.getId());
        BigDecimal montantRestant = normaliserMontant(prixTotal.subtract(totalPaye));


        if (montantRestant.compareTo(ZERO) <= 0) {
            detteExistante.ifPresent(dette -> {
                dette.setMontantRestant(ZERO);
                dette.setDateReglement(LocalDateTime.now(clock));
                dette.setStatutDette(StatutDette.REGLEE);
                detteRepository.save(dette);
            });

            return Optional.empty();
        }

        Dette dette = detteExistante.orElseGet(() -> Dette.builder()
                .match(match)
                .membreResponsable(responsable)
                .dateCreation(LocalDateTime.now(clock))
                .build()
        );

        dette.setMembreResponsable(responsable);
        dette.setMontantRestant(montantRestant);
        dette.setDateReglement(null);
        dette.setStatutDette(StatutDette.OUVERTE);

        BigDecimal montantInitialActuel = normaliserMontant(dette.getMontantInitial());

        if (montantInitialActuel.compareTo(montantRestant) < 0) {
            dette.setMontantInitial(montantRestant);
        } else if (dette.getMontantInitial() == null) {
            dette.setMontantInitial(montantRestant);
        }

        return Optional.of(convertirEnDetteResponse(detteRepository.save(dette)));
    }

    @Transactional
    public void actualiserDettesOrganisateur(Membre organisateur) {
        if (organisateur == null || organisateur.getId() == null) {
            return;
        }

        participationRepository.findByMembreId(organisateur.getId())
                .stream()
                .filter(participation ->
                        participation.getRoleParticipation()
                                == RoleParticipation.ORGANISATEUR
                )
                .filter(participation ->
                        participation.getStatutParticipation()
                                != StatutParticipation.LIBEREE
                )
                .map(Participation::getMatch)
                .filter(Objects::nonNull)
                .filter(this::matchPeutGenererUneDette)
                .forEach(this::actualiserDettePourMatch);
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
    private boolean dettePossedeDejaUnPaiement(Dette dette) {
        return dette != null
                && dette.getId() != null
                && paiementRepository.existsByDetteId(dette.getId());
    }

    private void verifierMatchArriveAEcheance(PadelMatch match) {
        if (match.getEtatCycle() == EtatCycleMatch.ANNULE) {
            throw new ConfigurationMetierException(
                    "Un match annulé ne peut pas générer de dette."
            );
        }

        if (match.getDateHeureDebut() == null) {
            throw new ConfigurationMetierException(
                    "La date et l'heure de début du match sont obligatoires "
                            + "pour calculer une dette."
            );
        }

        if (match.getDateHeureDebut().isAfter(LocalDateTime.now(clock))) {
            throw new ConfigurationMetierException(
                    "La dette ne peut être calculée qu'à partir "
                            + "de l'heure de début du match."
            );
        }
    }

    private boolean matchPeutGenererUneDette(PadelMatch match) {
        return match.getEtatCycle() != EtatCycleMatch.ANNULE
                && match.getDateHeureDebut() != null
                && !match.getDateHeureDebut()
                        .isAfter(LocalDateTime.now(clock));
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
            return PRIX_TOTAL_MATCH;
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
