package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.parametre.ParametresMetierResponse;
import com.padelMarius.backend.service.ParametresMetierService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParametresMetierController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class ParametresMetierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParametresMetierService parametresMetierService;

    @Test
    void shouldReturnBusinessParameters() throws Exception {
        when(parametresMetierService.consulterParametresMetier())
                .thenReturn(new ParametresMetierResponse(
                        90,
                        15,
                        4,
                        new BigDecimal("60.00"),
                        new BigDecimal("15.00"),
                        new BigDecimal("100.00")
                ));

        mockMvc.perform(get("/api/parametres-metier"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dureeMatchMinutes").value(90))
                .andExpect(jsonPath("$.pauseEntreMatchesMinutes").value(15))
                .andExpect(jsonPath("$.nombreJoueursMaximum").value(4))
                .andExpect(jsonPath("$.prixTotalMatch").value(60.00))
                .andExpect(jsonPath("$.montantParticipationStandard").value(15.00))
                .andExpect(jsonPath("$.soldeInitialJoueur").value(100.00));
    }
}
