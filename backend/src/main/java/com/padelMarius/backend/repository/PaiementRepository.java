package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.Paiement;
import com.padelMarius.backend.entity.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findByMembreId(Long membreId);

    List<Paiement> findByMembreIdAndStatutPaiement(
            Long membreId,
            StatutPaiement statutPaiement
    );

    List<Paiement> findByNaturePaiement(NaturePaiement naturePaiement);

    Optional<Paiement> findByParticipationId(Long participationId);

    Optional<Paiement> findByDetteId(Long detteId);

    boolean existsByParticipationId(Long participationId);

    boolean existsByDetteId(Long detteId);

    List<Paiement> findByParticipation_Match_IdAndNaturePaiementAndStatutPaiement(
            Long matchId,
            NaturePaiement naturePaiement,
            StatutPaiement statutPaiement
    );
}