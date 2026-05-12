package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.reservation.ReservationJoueurResponse;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.ModeCreation;
import com.padelMarius.backend.entity.ModeEntreeParticipation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.entity.VisibiliteMatch;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.service.MembreReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MembreReservationController.class)
@Import(ApiExceptionHandler.class)
class MembreReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MembreReservationService membreReservationService;

    @Test
    void consulterReservations_shouldReturnReservations() throws Exception {
        ReservationJoueurResponse response = new ReservationJoueurResponse(
                3101L,
                3001L,
                1001L,
                "Padel Bruxelles",
                1101L,
                "T1",
                LocalDateTime.of(2026, 6, 20, 9, 0),
                LocalDateTime.of(2026, 6, 20, 10, 30),
                RoleParticipation.JOUEUR,
                ModeEntreeParticipation.INSCRIPTION_PUBLIQUE,
                StatutParticipation.CONFIRMEE,
                ModeCreation.PUBLIC,
                VisibiliteMatch.PUBLIC,
                EtatCycleMatch.A_VENIR,
                new BigDecimal("60.00")
        );

        when(membreReservationService.consulterReservations("G1001"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/membres/G1001/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].participationId").value(3101))
                .andExpect(jsonPath("$[0].matchId").value(3001))
                .andExpect(jsonPath("$[0].siteId").value(1001))
                .andExpect(jsonPath("$[0].nomSite").value("Padel Bruxelles"))
                .andExpect(jsonPath("$[0].terrainId").value(1101))
                .andExpect(jsonPath("$[0].numeroTerrain").value("T1"))
                .andExpect(jsonPath("$[0].roleParticipation").value("JOUEUR"))
                .andExpect(jsonPath("$[0].statutParticipation").value("CONFIRMEE"))
                .andExpect(jsonPath("$[0].modeCreation").value("PUBLIC"))
                .andExpect(jsonPath("$[0].visibiliteCourante").value("PUBLIC"))
                .andExpect(jsonPath("$[0].etatCycle").value("A_VENIR"))
                .andExpect(jsonPath("$[0].prixTotal").value(60.00));
    }

    @Test
    void consulterReservations_shouldReturnNotFound_whenMemberDoesNotExist() throws Exception {
        when(membreReservationService.consulterReservations("G9999"))
                .thenThrow(new RessourceIntrouvableException(
                        "Membre introuvable avec le matricule G9999"
                ));

        mockMvc.perform(get("/api/membres/G9999/reservations"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"))
                .andExpect(jsonPath("$.message").value("Membre introuvable avec le matricule G9999"));
    }
}