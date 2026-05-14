package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import com.padelMarius.backend.entity.ModeEntreeParticipation;
import com.padelMarius.backend.entity.StatutParticipation;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

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