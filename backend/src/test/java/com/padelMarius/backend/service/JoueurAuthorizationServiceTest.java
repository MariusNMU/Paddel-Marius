package com.padelMarius.backend.service;

import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutParticipation;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.exception.AutorisationException;
import com.padelMarius.backend.repository.DetteRepository;
import com.padelMarius.backend.repository.MembreRepository;
import com.padelMarius.backend.repository.PadelMatchRepository;
import com.padelMarius.backend.repository.ParticipationRepository;
import com.padelMarius.backend.security.JwtService;
import com.padelMarius.backend.security.JwtUtilisateur;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JoueurAuthorizationServiceTest {

    private static final String AUTHORIZATION =
            "Bearer jwt-joueur";

    @Mock
    private MembreRepository membreRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private DetteRepository detteRepository;

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JoueurAuthorizationService service;

    @Test
    void verifierJoueurConnecte_shouldAcceptActivePlayerToken() {
        Membre joueur = creerMembre(
                20L,
                "G1001",
                true
        );

        simulerJoueurConnecte(joueur);

        assertDoesNotThrow(() ->
                service.verifierJoueurConnecte(AUTHORIZATION)
        );

        verify(membreRepository)
                .findByMatricule("G1001");
    }

    @Test
    void verifierJoueurConnecte_shouldRejectAdminToken() {
        when(jwtService.extraireUtilisateurDepuisAuthorization(
                AUTHORIZATION
        )).thenReturn(new JwtUtilisateur(
                "admin-global",
                JwtService.TYPE_UTILISATEUR_ADMIN,
                "GLOBAL",
                null
        ));

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> service.verifierJoueurConnecte(
                        AUTHORIZATION
                )
        );

        assertEquals(
                "Token joueur requis.",
                exception.getMessage()
        );

        verifyNoInteractions(membreRepository);
    }

    @Test
    void verifierJoueurConnecte_shouldRejectUnknownPlayer() {
        when(jwtService.extraireUtilisateurDepuisAuthorization(
                AUTHORIZATION
        )).thenReturn(new JwtUtilisateur(
                "G9999",
                JwtService.TYPE_UTILISATEUR_JOUEUR,
                "GLOBAL",
                null
        ));

        when(membreRepository.findByMatricule("G9999"))
                .thenReturn(Optional.empty());

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> service.verifierJoueurConnecte(
                        AUTHORIZATION
                )
        );

        assertEquals(
                "Joueur introuvable ou non authentifié.",
                exception.getMessage()
        );
    }

    @Test
    void verifierJoueurConnecte_shouldRejectInactivePlayer() {
        Membre joueur = creerMembre(
                20L,
                "G1001",
                false
        );

        simulerJoueurConnecte(joueur);

        AuthentificationException exception = assertThrows(
                AuthentificationException.class,
                () -> service.verifierJoueurConnecte(
                        AUTHORIZATION
                )
        );

        assertEquals(
                "Joueur introuvable ou non authentifié.",
                exception.getMessage()
        );
    }

    @Test
    void verifierAccesMatricule_shouldAcceptOwnMatricule() {
        Membre joueur = creerMembre(
                20L,
                "G1001",
                true
        );

        simulerJoueurConnecte(joueur);

        assertDoesNotThrow(() ->
                service.verifierAccesMatricule(
                        AUTHORIZATION,
                        "G1001"
                )
        );
    }

    @Test
    void verifierAccesMatricule_shouldRejectOtherMatricule() {
        Membre joueur = creerMembre(
                20L,
                "G1001",
                true
        );

        simulerJoueurConnecte(joueur);

        AutorisationException exception = assertThrows(
                AutorisationException.class,
                () -> service.verifierAccesMatricule(
                        AUTHORIZATION,
                        "G1002"
                )
        );

        assertEquals(
                "Un joueur ne peut agir que pour son propre compte.",
                exception.getMessage()
        );
    }

    @Test
    void verifierParticipationDuJoueur_shouldAcceptOwner() {
        Membre joueur = creerMembre(
                20L,
                "G1001",
                true
        );

        Participation participation = creerParticipation(
                300L,
                joueur,
                RoleParticipation.JOUEUR
        );

        simulerJoueurConnecte(joueur);

        when(participationRepository.findById(300L))
                .thenReturn(Optional.of(participation));

        assertDoesNotThrow(() ->
                service.verifierParticipationDuJoueur(
                        AUTHORIZATION,
                        300L
                )
        );
    }

    @Test
    void verifierParticipationDuJoueur_shouldRejectOtherPlayer() {
        Membre joueurConnecte = creerMembre(
                20L,
                "G1001",
                true
        );

        Membre autreJoueur = creerMembre(
                21L,
                "G1002",
                true
        );

        Participation participation = creerParticipation(
                300L,
                autreJoueur,
                RoleParticipation.JOUEUR
        );

        simulerJoueurConnecte(joueurConnecte);

        when(participationRepository.findById(300L))
                .thenReturn(Optional.of(participation));

        AutorisationException exception = assertThrows(
                AutorisationException.class,
                () -> service.verifierParticipationDuJoueur(
                        AUTHORIZATION,
                        300L
                )
        );

        assertEquals(
                "Cette participation n'appartient pas "
                        + "au joueur connecté.",
                exception.getMessage()
        );
    }

    @Test
    void verifierDetteDuJoueur_shouldAcceptResponsiblePlayer() {
        Membre joueur = creerMembre(
                20L,
                "G1001",
                true
        );

        Dette dette = creerDette(500L, joueur);

        simulerJoueurConnecte(joueur);

        when(detteRepository.findById(500L))
                .thenReturn(Optional.of(dette));

        assertDoesNotThrow(() ->
                service.verifierDetteDuJoueur(
                        AUTHORIZATION,
                        500L
                )
        );
    }

    @Test
    void verifierDetteDuJoueur_shouldRejectOtherPlayer() {
        Membre joueurConnecte = creerMembre(
                20L,
                "G1001",
                true
        );

        Membre responsable = creerMembre(
                21L,
                "G1002",
                true
        );

        Dette dette = creerDette(500L, responsable);

        simulerJoueurConnecte(joueurConnecte);

        when(detteRepository.findById(500L))
                .thenReturn(Optional.of(dette));

        AutorisationException exception = assertThrows(
                AutorisationException.class,
                () -> service.verifierDetteDuJoueur(
                        AUTHORIZATION,
                        500L
                )
        );

        assertEquals(
                "Cette dette n'appartient pas au joueur connecté.",
                exception.getMessage()
        );
    }

    @Test
    void verifierOrganisateurDuMatch_shouldAcceptOrganizer() {
        Membre joueur = creerMembre(
                20L,
                "G1001",
                true
        );

        Participation organisateur = creerParticipation(
                300L,
                joueur,
                RoleParticipation.ORGANISATEUR
        );

        simulerJoueurConnecte(joueur);

        when(padelMatchRepository.existsById(100L))
                .thenReturn(true);

        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(organisateur));

        assertDoesNotThrow(() ->
                service.verifierOrganisateurDuMatch(
                        AUTHORIZATION,
                        100L
                )
        );
    }

    @Test
    void verifierOrganisateurDuMatch_shouldRejectNonOrganizer() {
        Membre joueurConnecte = creerMembre(
                20L,
                "G1001",
                true
        );

        Membre autreJoueur = creerMembre(
                21L,
                "G1002",
                true
        );

        Participation organisateur = creerParticipation(
                300L,
                autreJoueur,
                RoleParticipation.ORGANISATEUR
        );

        simulerJoueurConnecte(joueurConnecte);

        when(padelMatchRepository.existsById(100L))
                .thenReturn(true);

        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(organisateur));

        AutorisationException exception = assertThrows(
                AutorisationException.class,
                () -> service.verifierOrganisateurDuMatch(
                        AUTHORIZATION,
                        100L
                )
        );

        assertEquals(
                "Seul l'organisateur du match "
                        + "peut réaliser cette opération.",
                exception.getMessage()
        );
    }

    private void simulerJoueurConnecte(Membre joueur) {
        when(jwtService.extraireUtilisateurDepuisAuthorization(
                AUTHORIZATION
        )).thenReturn(new JwtUtilisateur(
                joueur.getMatricule(),
                JwtService.TYPE_UTILISATEUR_JOUEUR,
                joueur.getCategorieMembre().name(),
                null
        ));

        when(membreRepository.findByMatricule(
                joueur.getMatricule()
        )).thenReturn(Optional.of(joueur));
    }

    private Membre creerMembre(
            Long id,
            String matricule,
            boolean actif
    ) {
        Membre membre = Membre.builder()
                .matricule(matricule)
                .nom("Nom")
                .prenom("Prenom")
                .motDePasseHash("hash")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(actif)
                .soldeCredit(new BigDecimal("100.00"))
                .build();

        ReflectionTestUtils.setField(
                membre,
                "id",
                id
        );

        return membre;
    }

    private Participation creerParticipation(
            Long id,
            Membre membre,
            RoleParticipation role
    ) {
        Participation participation = Participation.builder()
                .membre(membre)
                .roleParticipation(role)
                .statutParticipation(
                        StatutParticipation.CONFIRMEE
                )
                .build();

        ReflectionTestUtils.setField(
                participation,
                "id",
                id
        );

        return participation;
    }

    private Dette creerDette(
            Long id,
            Membre responsable
    ) {
        Dette dette = Dette.builder()
                .membreResponsable(responsable)
                .statutDette(StatutDette.OUVERTE)
                .build();

        ReflectionTestUtils.setField(
                dette,
                "id",
                id
        );

        return dette;
    }
}