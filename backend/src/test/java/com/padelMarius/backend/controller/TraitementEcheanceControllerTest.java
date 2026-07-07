package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.traitement.TraitementEcheanceResponse;
import com.padelMarius.backend.service.TraitementEcheanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TraitementEcheanceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class TraitementEcheanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TraitementEcheanceService traitementEcheanceService;

    @Test
    void shouldReturnOk_whenGlobalAdminRunsProcessing() throws Exception {
        TraitementEcheanceResponse response = new TraitementEcheanceResponse(
                LocalDateTime.of(2026, 5, 17, 17, 0),
                2,
                1,
                1
        );

        when(traitementEcheanceService.traiterMatchesArrivesAEcheance())
                .thenReturn(response);

        mockMvc.perform(post("/api/admin/matches/traitement-echeance")
                        .header(
                                "Authorization",
                                "Bearer jwt-admin-global"
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchesAnalyses").value(2))
                .andExpect(jsonPath("$.matchesDemarres").value(1))
                .andExpect(jsonPath("$.dettesCreees").value(1));
    }
}
