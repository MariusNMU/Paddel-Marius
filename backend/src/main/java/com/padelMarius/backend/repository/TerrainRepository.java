package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.Terrain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TerrainRepository extends JpaRepository<Terrain, Long> {

    List<Terrain> findBySite(Site site);

    List<Terrain> findBySiteId(Long siteId);

    List<Terrain> findBySiteAndActifTrue(Site site);

    List<Terrain> findBySiteIdAndActifTrue(Long siteId);
}
