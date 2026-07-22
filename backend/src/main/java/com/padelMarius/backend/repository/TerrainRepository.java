package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.Terrain;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TerrainRepository extends JpaRepository<Terrain, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select terrain
            from Terrain terrain
            where terrain.id = :terrainId
            """)
    Optional<Terrain> findByIdForUpdate(
            @Param("terrainId") Long terrainId
    );

    @EntityGraph(attributePaths = "site")
    List<Terrain> findByActifTrueAndSiteActifTrueOrderBySiteNomAscNumeroAsc();

    List<Terrain> findBySite(Site site);

    List<Terrain> findBySiteId(Long siteId);

    List<Terrain> findBySiteAndActifTrue(Site site);

    List<Terrain> findBySiteIdAndActifTrue(Long siteId);
}
