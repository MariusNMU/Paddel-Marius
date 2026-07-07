package com.padelMarius.backend.service;

import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Dette;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.Participation;
import com.padelMarius.backend.entity.RoleParticipation;
import com.padelMarius.backend.entity.StatutDette;
import com.padelMarius.backend.entity.StatutParticipation;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JoueurAuthorizationServiceTest {

    @Mock
    private MembreRepository membreRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private DetteRepository detteRepository;

    @Mock
    private PadelMatchRepository padelMatchRepository;

    @InjectMocks
    private JoueurAuthorizationService service;

    @Test
    void estJoueurActif_shouldReturnTrueForActivePlayerPrincipal() {
        Membre joueur = creerMembre(
                20L,
                "G1001",
                true
        );

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(joueur));

        assertThat(service.estJoueurActif(
                authenticationJoueur("G1001")
        )).isTrue();
    }

    @Test
    void estJoueurActif_shouldReturnFalseForAdminPrincipal() {
        assertThat(service.estJoueurActif(
                authenticationAdmin()
        )).isFalse();

        verifyNoInteractions(membreRepository);
    }

    @Test
    void peutAgirPourMatricule_shouldReturnTrueForOwnMatricule() {
        Membre joueur = creerMembre(
                20L,
                "G1001",
                true
        );

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(joueur));

        assertThat(service.peutAgirPourMatricule(
                authenticationJoueur("G1001"),
                "G1001"
        )).isTrue();
    }

    @Test
    void peutAgirPourMatricule_shouldReturnFalseForOtherMatricule() {
        Membre joueur = creerMembre(
                20L,
                "G1001",
                true
        );

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(joueur));

        assertThat(service.peutAgirPourMatricule(
                authenticationJoueur("G1001"),
                "G1002"
        )).isFalse();
    }

    @Test
    void peutAccederParticipation_shouldReturnTrueForParticipationOwner() {
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

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(joueur));

        when(participationRepository.findById(300L))
                .thenReturn(Optional.of(participation));

        assertThat(service.peutAccederParticipation(
                authenticationJoueur("G1001"),
                300L
        )).isTrue();
    }

    @Test
    void peutAccederParticipation_shouldReturnFalseForOtherPlayer() {
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

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(joueurConnecte));

        when(participationRepository.findById(300L))
                .thenReturn(Optional.of(participation));

        assertThat(service.peutAccederParticipation(
                authenticationJoueur("G1001"),
                300L
        )).isFalse();
    }

    @Test
    void peutAccederDette_shouldReturnTrueForDebtOwner() {
        Membre joueur = creerMembre(
                20L,
                "G1001",
                true
        );

        Dette dette = creerDette(500L, joueur);

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(joueur));

        when(detteRepository.findById(500L))
                .thenReturn(Optional.of(dette));

        assertThat(service.peutAccederDette(
                authenticationJoueur("G1001"),
                500L
        )).isTrue();
    }

    @Test
    void peutAccederDette_shouldReturnFalseForOtherPlayer() {
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

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(joueurConnecte));

        when(detteRepository.findById(500L))
                .thenReturn(Optional.of(dette));

        assertThat(service.peutAccederDette(
                authenticationJoueur("G1001"),
                500L
        )).isFalse();
    }

    @Test
    void estOrganisateurDuMatch_shouldReturnTrueForOrganizer() {
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

        when(padelMatchRepository.existsById(100L))
                .thenReturn(true);

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(joueur));

        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(organisateur));

        assertThat(service.estOrganisateurDuMatch(
                authenticationJoueur("G1001"),
                100L
        )).isTrue();
    }

    @Test
    void estOrganisateurDuMatch_shouldReturnFalseForNonOrganizer() {
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

        when(padelMatchRepository.existsById(100L))
                .thenReturn(true);

        when(membreRepository.findByMatricule("G1001"))
                .thenReturn(Optional.of(joueurConnecte));

        when(participationRepository.findByMatchId(100L))
                .thenReturn(List.of(organisateur));

        assertThat(service.estOrganisateurDuMatch(
                authenticationJoueur("G1001"),
                100L
        )).isFalse();
    }

    private Authentication authenticationJoueur(String matricule) {
        JwtUtilisateur utilisateur = new JwtUtilisateur(
                matricule,
                JwtService.TYPE_UTILISATEUR_JOUEUR,
                CategorieMembre.GLOBAL.name(),
                null
        );

        return new UsernamePasswordAuthenticationToken(
                utilisateur,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_JOUEUR"))
        );
    }

    private Authentication authenticationAdmin() {
        JwtUtilisateur utilisateur = new JwtUtilisateur(
                "admin-global",
                JwtService.TYPE_UTILISATEUR_ADMIN,
                CategorieMembre.GLOBAL.name(),
                null
        );

        return new UsernamePasswordAuthenticationToken(
                utilisateur,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
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
