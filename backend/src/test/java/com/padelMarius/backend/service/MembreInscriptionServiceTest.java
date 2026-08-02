package com.padelMarius.backend.service;

import com.padelMarius.backend.dto.membre.InscriptionMembreRequest;
import com.padelMarius.backend.dto.membre.MembreResponse;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.exception.ConfigurationMetierException;
import com.padelMarius.backend.exception.RessourceIntrouvableException;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembreInscriptionServiceTest {

    @Mock
    private MembreRepository membreRepository;

    @Mock
    private SiteRepository siteRepository;

    private MembreInscriptionService membreInscriptionService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();

        membreInscriptionService = new MembreInscriptionService(
                membreRepository,
                siteRepository,
                passwordEncoder
        );
    }

    @Test
    void inscrireMembre_shouldCreateGlobalMemberWithGeneratedMatricule() {
        InscriptionMembreRequest request = new InscriptionMembreRequest(
                "Durand",
                "Alice",
                CategorieMembre.GLOBAL,
                null,
                "MotDePasse2026!",
                "MotDePasse2026!"
        );

        when(membreRepository.findByMatriculeStartingWith("G"))
                .thenReturn(List.of(
                        creerMembre(1L, "G1001", CategorieMembre.GLOBAL, null),
                        creerMembre(2L, "G1002", CategorieMembre.GLOBAL, null)
                ));

        when(membreRepository.existsByMatricule("G1003"))
                .thenReturn(false);

        when(membreRepository.save(any(Membre.class)))
                .thenAnswer(invocation -> {
                    Membre membre = invocation.getArgument(0);
                    ReflectionTestUtils.setField(membre, "id", 10L);
                    return membre;
                });

        MembreResponse response = membreInscriptionService.inscrireMembre(request);
        ArgumentCaptor<Membre> membreCaptor = ArgumentCaptor.forClass(Membre.class);
        verify(membreRepository).save(membreCaptor.capture());
        assertTrue(passwordEncoder.matches(
                "MotDePasse2026!",
                membreCaptor.getValue().getMotDePasseHash()
        ));
        assertTrue(membreCaptor.getValue().getMotDePasseHash().startsWith("$2"));

        assertEquals(10L, response.membreId());
        assertEquals("G1003", response.matricule());
        assertEquals("Durand", response.nom());
        assertEquals("Alice", response.prenom());
        assertEquals(CategorieMembre.GLOBAL, response.categorieMembre());
        assertEquals(null, response.siteRattachementId());
        assertEquals(true, response.actif());
    }

    @Test
    void inscrireMembre_shouldCreateLibreMemberWithGeneratedMatricule() {
        InscriptionMembreRequest request = new InscriptionMembreRequest(
                "Petit",
                "Nina",
                CategorieMembre.LIBRE,
                null,
                "MotDePasse2026!",
                "MotDePasse2026!"
        );

        when(membreRepository.findByMatriculeStartingWith("L"))
                .thenReturn(List.of(
                        creerMembre(1L, "L1001", CategorieMembre.LIBRE, null)
                ));

        when(membreRepository.existsByMatricule("L1002"))
                .thenReturn(false);

        when(membreRepository.save(any(Membre.class)))
                .thenAnswer(invocation -> {
                    Membre membre = invocation.getArgument(0);
                    ReflectionTestUtils.setField(membre, "id", 11L);
                    return membre;
                });

        MembreResponse response = membreInscriptionService.inscrireMembre(request);

        assertEquals(11L, response.membreId());
        assertEquals("L1002", response.matricule());
        assertEquals(CategorieMembre.LIBRE, response.categorieMembre());
        assertEquals(true, response.actif());
    }

    @Test
    void inscrireMembre_shouldCreateSiteMemberWithSiteRattachement() {
        Site site = creerSite(1001L, "Padel Bruxelles");

        InscriptionMembreRequest request = new InscriptionMembreRequest(
                "Martin",
                "Luc",
                CategorieMembre.SITE,
                1001L,
                "MotDePasse2026!",
                "MotDePasse2026!"
        );

        when(siteRepository.findById(1001L))
                .thenReturn(Optional.of(site));

        when(membreRepository.findByMatriculeStartingWith("S"))
                .thenReturn(List.of(
                        creerMembre(1L, "S1001", CategorieMembre.SITE, site)
                ));

        when(membreRepository.existsByMatricule("S1002"))
                .thenReturn(false);

        when(membreRepository.save(any(Membre.class)))
                .thenAnswer(invocation -> {
                    Membre membre = invocation.getArgument(0);
                    ReflectionTestUtils.setField(membre, "id", 12L);
                    return membre;
                });

        MembreResponse response = membreInscriptionService.inscrireMembre(request);

        assertEquals(12L, response.membreId());
        assertEquals("S1002", response.matricule());
        assertEquals("Martin", response.nom());
        assertEquals("Luc", response.prenom());
        assertEquals(CategorieMembre.SITE, response.categorieMembre());
        assertEquals(1001L, response.siteRattachementId());
        assertEquals("Padel Bruxelles", response.nomSiteRattachement());
        assertEquals(true, response.actif());
    }

    @Test
    void inscrireMembre_shouldRejectSiteMemberWithoutSiteRattachement() {
        InscriptionMembreRequest request = new InscriptionMembreRequest(
                "Martin",
                "Luc",
                CategorieMembre.SITE,
                null,
                "MotDePasse2026!",
                "MotDePasse2026!"
        );

        ConfigurationMetierException exception = assertThrows(
                ConfigurationMetierException.class,
                () -> membreInscriptionService.inscrireMembre(request)
        );

        assertEquals(
                "Un membre SITE doit avoir un site de rattachement.",
                exception.getMessage()
        );
    }

    @Test
    void inscrireMembre_shouldRejectUnknownSite() {
        InscriptionMembreRequest request = new InscriptionMembreRequest(
                "Martin",
                "Luc",
                CategorieMembre.SITE,
                9999L,
                "MotDePasse2026!",
                "MotDePasse2026!"
        );

        when(siteRepository.findById(9999L))
                .thenReturn(Optional.empty());

        RessourceIntrouvableException exception = assertThrows(
                RessourceIntrouvableException.class,
                () -> membreInscriptionService.inscrireMembre(request)
        );

        assertEquals(
                "Site introuvable avec l'id 9999",
                exception.getMessage()
        );
    }

    @Test
    void inscrireMembre_shouldRejectInactiveSite() {
        Site site = creerSite(1001L, "Padel Bruxelles");
        site.setActif(false);

        InscriptionMembreRequest request = new InscriptionMembreRequest(
                "Martin",
                "Luc",
                CategorieMembre.SITE,
                1001L,
                "MotDePasse2026!",
                "MotDePasse2026!"
        );

        when(siteRepository.findById(1001L))
                .thenReturn(Optional.of(site));

        ConfigurationMetierException exception = assertThrows(
                ConfigurationMetierException.class,
                () -> membreInscriptionService.inscrireMembre(request)
        );

        assertEquals(
                "Le site demandé est inactif.",
                exception.getMessage()
        );

        verify(membreRepository, never()).save(any(Membre.class));
    }

    @Test
    void inscrireMembre_shouldRejectDifferentPasswords() {
        InscriptionMembreRequest request =
                new InscriptionMembreRequest(
                        "Durand",
                        "Alice",
                        CategorieMembre.GLOBAL,
                        null,
                        "MotDePasse2026!",
                        "AutreMotDePasse2026!"
                );

        ConfigurationMetierException exception =
                assertThrows(
                        ConfigurationMetierException.class,
                        () -> membreInscriptionService
                                .inscrireMembre(request)
                );

        assertEquals(
                "Les mots de passe ne correspondent pas.",
                exception.getMessage()
        );

        verify(
                membreRepository,
                never()
        ).save(any(Membre.class));
    }

    private Site creerSite(Long id, String nom) {
        Site site = Site.builder()
                .code("SITE-" + id)
                .nom(nom)
                .adresse("Adresse " + id)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(site, "id", id);

        return site;
    }

    private Membre creerMembre(
            Long id,
            String matricule,
            CategorieMembre categorieMembre,
            Site siteRattachement
    ) {
        Membre membre = Membre.builder()
                .matricule(matricule)
                .nom("Nom " + id)
                .prenom("Prenom " + id)
                .motDePasseHash(passwordEncoder.encode("password"))
                .categorieMembre(categorieMembre)
                .siteRattachement(siteRattachement)
                .actif(true)
                .build();

        ReflectionTestUtils.setField(membre, "id", id);

        return membre;
    }
}
