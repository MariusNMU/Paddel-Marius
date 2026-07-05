package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.terrain.TerrainResponse;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.entity.Terrain;
import com.padelMarius.backend.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TerrainConsultationService {

    private final TerrainRepository terrainRepository;

    public List<TerrainResponse> listerTerrainsActifs() {
        return terrainRepository.findByActifTrueAndSiteActifTrueOrderBySiteNomAscNumeroAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TerrainResponse toResponse(Terrain terrain) {
        Site site = terrain.getSite();

        return new TerrainResponse(
                terrain.getId(),
                terrain.getNumero(),
                site.getId(),
                site.getNom()
        );
    }
}
