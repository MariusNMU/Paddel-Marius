package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.PadelMatch;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.entity.VisibiliteMatch;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PadelMatchRepository extends JpaRepository<PadelMatch, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select padelMatch
            from PadelMatch padelMatch
            where padelMatch.id = :matchId
            """)
    Optional<PadelMatch> findByIdForUpdate(
            @Param("matchId") Long matchId
    );

    List<PadelMatch> findByTerrain(Terrain terrain);

    List<PadelMatch> findByTerrainId(Long terrainId);

    List<PadelMatch> findByTerrainInAndDateHeureDebutBetween(
            List<Terrain> terrains,
            LocalDateTime debut,
            LocalDateTime fin
    );

    List<PadelMatch> findByDateHeureDebutGreaterThanEqualAndDateHeureDebutBefore(
            LocalDateTime debut,
            LocalDateTime fin
    );

    List<PadelMatch> findByVisibiliteCouranteAndEtatCycleAndDateHeureDebutGreaterThanEqualAndDateHeureDebutBefore(
            VisibiliteMatch visibiliteCourante,
            EtatCycleMatch etatCycle,
            LocalDateTime debut,
            LocalDateTime fin
    );

    List<PadelMatch> findByEtatCycleAndDateHeureDebutLessThanEqual(
            EtatCycleMatch etatCycle,
            LocalDateTime dateHeureDebut
    );

    List<PadelMatch> findByTerrainInAndDateHeureDebutGreaterThanEqualAndDateHeureDebutBeforeAndEtatCycle(
            List<Terrain> terrains,
            LocalDateTime debut,
            LocalDateTime fin,
            EtatCycleMatch etatCycle
    );

    List<PadelMatch> findByEtatCycleAndDateHeureFinLessThanEqual(
            EtatCycleMatch etatCycle,
            LocalDateTime dateHeureFin
    );
}
