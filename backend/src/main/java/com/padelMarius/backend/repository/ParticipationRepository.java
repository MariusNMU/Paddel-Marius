package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Participation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import com.padelMarius.backend.entity.ModeEntreeParticipation;
import com.padelMarius.backend.entity.StatutParticipation;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select participation
            from Participation participation
            where participation.id = :participationId
            """)
    Optional<Participation> findByIdForUpdate(
            @Param("participationId") Long participationId
    );

    List<Participation> findByMatchId(Long matchId);

    List<Participation> findByMembreId(Long membreId);

    List<Participation> findByMembreIdAndModeEntreeAndStatutParticipation(
            Long membreId,
            ModeEntreeParticipation modeEntree,
            StatutParticipation statutParticipation
    );

    long countByMatchIdAndStatutParticipationNot(
            Long matchId,
            StatutParticipation statutParticipation
    );

    long countByMatchId(Long matchId);

    boolean existsByMatchIdAndMembreId(Long matchId, Long membreId);
}
