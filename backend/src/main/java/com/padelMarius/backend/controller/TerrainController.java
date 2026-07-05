package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.terrain.TerrainResponse;
import com.padelMarius.backend.service.TerrainConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/terrains")
@RequiredArgsConstructor
public class TerrainController {

    private final TerrainConsultationService terrainConsultationService;

    @GetMapping
    public List<TerrainResponse> listerTerrainsActifs() {
        return terrainConsultationService.listerTerrainsActifs();
    }
}
