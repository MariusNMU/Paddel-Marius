package com.padelMarius.backend.scheduler;

import com.padelMarius.backend.dto.traitement.TraitementVeilleResponse;
import com.padelMarius.backend.service.TraitementVeilleService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TraitementVeilleSchedulerTest {

    @Test
    void traiterVeilleDuJour_shouldDelegateCurrentDateToBusinessService() {
        TraitementVeilleService service = mock(
                TraitementVeilleService.class
        );

        Clock clock = Clock.fixed(
                Instant.parse("2026-08-03T08:00:00Z"),
                ZoneId.of("Europe/Brussels")
        );

        TraitementVeilleScheduler scheduler =
                new TraitementVeilleScheduler(service, clock);

        LocalDate dateTraitement = LocalDate.of(2026, 8, 3);

        when(service.traiterVeille(dateTraitement))
                .thenReturn(new TraitementVeilleResponse(
                        dateTraitement,
                        dateTraitement.plusDays(1),
                        2,
                        1,
                        1,
                        0
                ));

        scheduler.traiterVeilleDuJour();

        verify(service).traiterVeille(dateTraitement);
    }
}
