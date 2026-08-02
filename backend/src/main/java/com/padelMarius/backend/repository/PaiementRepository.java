package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.NaturePaiement;
import com.padelMarius.backend.entity.Paiement;
import com.padelMarius.backend.entity.StatutPaiement;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    List<Paiement> findByMembreId(Long membreId);

    List<Paiement> findByMembreIdOrderByDateHeurePaiementDesc(Long membreId);

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select paiement
            from Paiement paiement
            where paiement.participation.match.id = :matchId
              and paiement.naturePaiement = :naturePaiement
              and paiement.statutPaiement = :statutPaiement
            order by paiement.membre.id, paiement.id
            """)
    List<Paiement> findPayesDuMatchForUpdate(
            @Param("matchId") Long matchId,
            @Param("naturePaiement") NaturePaiement naturePaiement,
            @Param("statutPaiement") StatutPaiement statutPaiement
    );

    List<Paiement> findByDateHeurePaiementGreaterThanEqualAndDateHeurePaiementBeforeAndStatutPaiement(
            LocalDateTime debut,
            LocalDateTime fin,
            StatutPaiement statutPaiement
    );
}
