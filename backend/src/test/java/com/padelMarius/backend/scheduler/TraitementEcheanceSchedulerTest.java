package com.padelMarius.backend.scheduler;

import com.padelMarius.backend.dto.traitement.TraitementEcheanceResponse;
import com.padelMarius.backend.service.TraitementEcheanceService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TraitementEcheanceSchedulerTest {

    @Test
    void traiterEcheances_shouldDelegateToBusinessService() {
        TraitementEcheanceService service = mock(
                TraitementEcheanceService.class
        );
        TraitementEcheanceScheduler scheduler =
                new TraitementEcheanceScheduler(service);

        when(service.traiterMatchesArrivesAEcheance())
                .thenReturn(new TraitementEcheanceResponse(
                        LocalDateTime.of(2026, 8, 2, 12, 0),
                        2,
                        1,
                        1
                ));

        scheduler.traiterEcheances();

        verify(service).traiterMatchesArrivesAEcheance();
    }
}
