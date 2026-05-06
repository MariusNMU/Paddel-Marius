package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.disponibilite.CreneauDisponibiliteResponse;
import com.padelMarius.backend.dto.disponibilite.DisponibilitesResponse;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.service.DisponibiliteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DisponibiliteController.class)
@Import(ApiExceptionHandler.class)
class DisponibiliteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DisponibiliteService disponibiliteService;

    @Test
    void shouldReturnDisponibilites() throws Exception {
        LocalDate date = LocalDate.of(2026, 5, 8);

        DisponibilitesResponse response = new DisponibilitesResponse(
                1L,
                date,
                false,
                null,
                List.of(
                        new CreneauDisponibiliteResponse(
                                10L,
                                "1",
                                LocalDateTime.of(2026, 5, 8, 9, 0),
                                LocalDateTime.of(2026, 5, 8, 10, 30)
                        )
                )
        );

        when(disponibiliteService.consulterDisponibilites(1L, date))
                .thenReturn(response);

        mockMvc.perform(get("/api/disponibilites")
                        .param("siteId", "1")
                        .param("date", "2026-05-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.siteId").value(1))
                .andExpect(jsonPath("$.date").value("2026-05-08"))
                .andExpect(jsonPath("$.ferme").value(false))
                .andExpect(jsonPath("$.creneaux[0].terrainId").value(10))
                .andExpect(jsonPath("$.creneaux[0].terrainNumero").value("1"))
                .andExpect(jsonPath("$.creneaux[0].dateHeureDebut").value("2026-05-08T09:00:00"))
                .andExpect(jsonPath("$.creneaux[0].dateHeureFin").value("2026-05-08T10:30:00"));
    }

    @Test
    void shouldReturnNotFoundWhenSiteDoesNotExist() throws Exception {
        LocalDate date = LocalDate.of(2026, 5, 8);

        when(disponibiliteService.consulterDisponibilites(999L, date))
                .thenThrow(new RessourceIntrouvableException("Site introuvable avec l'id 999"));

        mockMvc.perform(get("/api/disponibilites")
                        .param("siteId", "999")
                        .param("date", "2026-05-08"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"))
                .andExpect(jsonPath("$.message").value("Site introuvable avec l'id 999"));
    }
}