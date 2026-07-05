package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.terrain.TerrainResponse;
import com.padelMarius.backend.service.TerrainConsultationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TerrainController.class)
@Import(ApiExceptionHandler.class)
class TerrainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TerrainConsultationService terrainConsultationService;

    @Test
    void shouldReturnActiveTerrains() throws Exception {
        List<TerrainResponse> response = List.of(
                new TerrainResponse(
                        1101L,
                        "T1",
                        1001L,
                        "Padel Bruxelles"
                ),
                new TerrainResponse(
                        1201L,
                        "T1",
                        1002L,
                        "Padel Namur"
                )
        );

        when(terrainConsultationService.listerTerrainsActifs())
                .thenReturn(response);

        mockMvc.perform(get("/api/terrains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].terrainId").value(1101))
                .andExpect(jsonPath("$[0].numeroTerrain").value("T1"))
                .andExpect(jsonPath("$[0].siteId").value(1001))
                .andExpect(jsonPath("$[0].nomSite").value("Padel Bruxelles"))
                .andExpect(jsonPath("$[1].terrainId").value(1201))
                .andExpect(jsonPath("$[1].numeroTerrain").value("T1"))
                .andExpect(jsonPath("$[1].siteId").value(1002))
                .andExpect(jsonPath("$[1].nomSite").value("Padel Namur"));
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveTerrainExists() throws Exception {
        when(terrainConsultationService.listerTerrainsActifs())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/terrains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}