package com.padelMarius.backend.repository;

import com.padelMarius.backend.entity.Terrain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TerrainRepository extends JpaRepository<Terrain, Long> {

    List<Terrain> findBySiteId(Long siteId);

    List<Terrain> findBySiteIdAndActifTrue(Long siteId);
}