package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.etatoperationnel.EtatOperationnelAdminResponse;
import com.padelMarius.backend.dto.etatoperationnel.EtatTerrainOperationnel;
import com.padelMarius.backend.dto.etatoperationnel.MatchEtatAdminResponse;
import com.padelMarius.backend.dto.etatoperationnel.OccupationHebdomadaireAdminResponse;
import com.padelMarius.backend.dto.etatoperationnel.TerrainEtatAdminResponse;
import com.padelMarius.backend.entity.EtatCycleMatch;
import com.padelMarius.backend.entity.VisibiliteMatch;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.service.EtatOperationnelAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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

@WebMvcTest(EtatOperationnelAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class EtatOperationnelAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EtatOperationnelAdminService etatOperationnelAdminService;

    @Test
    void shouldReturnOperationalStateForSelectedSiteAndDate() throws Exception {
        EtatOperationnelAdminResponse response =
                new EtatOperationnelAdminResponse(
                        LocalDate.of(2026, 7, 20),
                        1001L,
                        "Padel Bruxelles",
                        true,
                        false,
                        null,
                        List.of(
                                new TerrainEtatAdminResponse(
                                        2001L,
                                        "T1",
                                        true,
                                        EtatTerrainOperationnel.RESERVE,
                                        List.of(
                                                new MatchEtatAdminResponse(
                                                        3001L,
                                                        LocalDateTime.of(
                                                                2026,
                                                                7,
                                                                20,
                                                                10,
                                                                0
                                                        ),
                                                        LocalDateTime.of(
                                                                2026,
                                                                7,
                                                                20,
                                                                11,
                                                                30
                                                        ),
                                                        VisibiliteMatch.PUBLIC,
                                                        EtatCycleMatch.A_VENIR,
                                                        3
                                                )
                                        )
                                )
                        )
                );

        when(etatOperationnelAdminService.consulterEtatOperationnel(
                LocalDate.of(2026, 7, 20),
                1001L
        )).thenReturn(response);

        mockMvc.perform(get("/api/admin/etat-operationnel")
                        .param("date", "2026-07-20")
                        .param("siteId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-07-20"))
                .andExpect(jsonPath("$.siteId").value(1001))
                .andExpect(jsonPath("$.nomSite").value("Padel Bruxelles"))
                .andExpect(jsonPath("$.siteActif").value(true))
                .andExpect(jsonPath("$.ferme").value(false))
                .andExpect(jsonPath("$.terrains[0].numeroTerrain").value("T1"))
                .andExpect(jsonPath("$.terrains[0].etatTerrain").value("RESERVE"))
                .andExpect(jsonPath("$.terrains[0].matches[0].matchId").value(3001))
                .andExpect(jsonPath("$.terrains[0].matches[0].nombreParticipants").value(3));
    }

    @Test
    void shouldReturn404WhenSiteDoesNotExist() throws Exception {
        when(etatOperationnelAdminService.consulterEtatOperationnel(
                LocalDate.of(2026, 7, 20),
                999L
        )).thenThrow(new RessourceIntrouvableException(
                "Site introuvable avec l'id 999"
        ));

        mockMvc.perform(get("/api/admin/etat-operationnel")
                        .param("date", "2026-07-20")
                        .param("siteId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESSOURCE_INTROUVABLE"))
                .andExpect(jsonPath("$.message").value(
                        "Site introuvable avec l'id 999"
                ));
    }

    @Test
    void shouldReturnWeeklyOccupationForSelectedSite() throws Exception {
        EtatOperationnelAdminResponse lundi =
                new EtatOperationnelAdminResponse(
                        LocalDate.of(2026, 7, 20),
                        1001L,
                        "Padel Bruxelles",
                        true,
                        false,
                        null,
                        List.of()
                );

        OccupationHebdomadaireAdminResponse response =
                new OccupationHebdomadaireAdminResponse(
                        LocalDate.of(2026, 7, 20),
                        LocalDate.of(2026, 7, 26),
                        1001L,
                        "Padel Bruxelles",
                        true,
                        List.of(lundi)
                );

        when(etatOperationnelAdminService
                .consulterOccupationHebdomadaire(
                        LocalDate.of(2026, 7, 22),
                        1001L
                )).thenReturn(response);

        mockMvc.perform(get("/api/admin/etat-operationnel/semaine")
                        .param("date", "2026-07-22")
                        .param("siteId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateDebut").value("2026-07-20"))
                .andExpect(jsonPath("$.dateFin").value("2026-07-26"))
                .andExpect(jsonPath("$.siteId").value(1001))
                .andExpect(jsonPath("$.nomSite").value("Padel Bruxelles"))
                .andExpect(jsonPath("$.jours[0].date").value("2026-07-20"));
    }

    @Test
    void shouldReturn409ForWeeklyOccupationOfInactiveSite() throws Exception {
        when(etatOperationnelAdminService
                .consulterOccupationHebdomadaire(
                        LocalDate.of(2026, 7, 22),
                        1003L
                )).thenThrow(new ConfigurationMetierException(
                "Le planning hebdomadaire n'est pas disponible pour un site inactif."
        ));

        mockMvc.perform(get("/api/admin/etat-operationnel/semaine")
                        .param("date", "2026-07-22")
                        .param("siteId", "1003"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(
                        "CONFIGURATION_METIER_INVALIDE"
                ))
                .andExpect(jsonPath("$.message").value(
                        "Le planning hebdomadaire n'est pas disponible pour un site inactif."
                ));
    }

    @Test
    void shouldReturn400WhenDateIsMissing() throws Exception {
        mockMvc.perform(get("/api/admin/etat-operationnel")
                        .param("siteId", "1001"))
                .andExpect(status().isBadRequest());
    }
}
