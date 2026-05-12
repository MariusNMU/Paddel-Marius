package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.membre.SoldeJoueurResponse;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.MembreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembreSoldeServiceTest {

    @Mock
    private MembreRepository membreRepository;

    private MembreSoldeService membreSoldeService;

    @BeforeEach
    void setUp() {
        membreSoldeService = new MembreSoldeService(membreRepository);
    }

    @Test
    void consulterSolde_shouldReturnMemberBalance() {
        Membre membre = Membre.builder()
                .matricule("G1001")
                .nom("Dupont")
                .prenom("Marie")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .soldeCredit(new BigDecimal("85.00"))
                .build();

        ReflectionTestUtils.setField(membre, "id", 10L);

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(membre));

        SoldeJoueurResponse response = membreSoldeService.consulterSolde("G1001");

        assertEquals(10L, response.membreId());
        assertEquals("G1001", response.matricule());
        assertEquals(new BigDecimal("85.00"), response.soldeCredit());
    }

    @Test
    void consulterSolde_shouldRejectUnknownMember() {
        when(membreRepository.findByMatricule("G9999"))
                .thenReturn(Optional.empty());

        RessourceIntrouvableException exception = assertThrows(
                RessourceIntrouvableException.class,
                () -> membreSoldeService.consulterSolde("G9999")
        );

        assertEquals(
                "Membre introuvable avec le matricule G9999",
                exception.getMessage()
        );
    }
}