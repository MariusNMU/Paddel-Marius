package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.PadelMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PadelMatchRepository extends JpaRepository<PadelMatch, Long> {

    List<PadelMatch> findByTerrainId(Long terrainId);

    List<PadelMatch> findByTerrainIdAndDateHeureDebutLessThanAndDateHeureFinGreaterThan(
            Long terrainId,
            LocalDateTime finRecherchee,
            LocalDateTime debutRecherchee
    );
}