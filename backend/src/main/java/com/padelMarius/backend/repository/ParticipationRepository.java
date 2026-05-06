package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    List<Participation> findByMatchId(Long matchId);

    List<Participation> findByMembreId(Long membreId);

    long countByMatchId(Long matchId);

    boolean existsByMatchIdAndMembreId(Long matchId, Long membreId);
}