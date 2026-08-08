package com.padelMarius.backend.security;

import com.padelMarius.backend.entity.Administrateur;
import com.padelMarius.backend.entity.CategorieMembre;
import com.padelMarius.backend.entity.Membre;
import com.padelMarius.backend.entity.RoleAdministrateur;
import com.padelMarius.backend.repository.AdministrateurRepository;
import com.padelMarius.backend.repository.MembreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PadelUserDetailsServiceTest {

    @Mock
    private MembreRepository membreRepository;

    @Mock
    private AdministrateurRepository administrateurRepository;

    private PadelUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new PadelUserDetailsService(
                membreRepository,
                administrateurRepository
        );
    }

    @Test
    void shouldLoadActivePlayerWithCurrentAuthorities() {
        Membre membre = Membre.builder()
                .matricule("G1001")
                .motDePasseHash("hash-joueur")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(true)
                .build();

        when(membreRepository.findByMatriculeIgnoreCase("g1001"))
                .thenReturn(Optional.of(membre));

        UserDetails resultat = userDetailsService.loadUserByUsername(
                IdentiteAuthentification.joueur("g1001")
        );

        assertThat(resultat.getUsername()).isEqualTo("JOUEUR:G1001");
        assertThat(resultat.getPassword()).isEqualTo("hash-joueur");
        assertThat(resultat.isEnabled()).isTrue();
        assertThat(resultat.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder(
                        "ROLE_JOUEUR",
                        "ROLE_JOUEUR_GLOBAL"
                );
        verifyNoInteractions(administrateurRepository);
    }

    @Test
    void shouldLoadSiteAdminWithCurrentAuthorities() {
        Administrateur administrateur = Administrateur.builder()
                .emailOuLogin("admin-bruxelles")
                .motDePasseHash("hash-admin")
                .roleAdministrateur(RoleAdministrateur.SITE)
                .actif(true)
                .build();

        when(administrateurRepository.findByEmailOuLoginIgnoreCase(
                "ADMIN-BRUXELLES"
        ))
                .thenReturn(Optional.of(administrateur));

        UserDetails resultat = userDetailsService.loadUserByUsername(
                IdentiteAuthentification.admin("ADMIN-BRUXELLES")
        );

        assertThat(resultat.getUsername())
                .isEqualTo("ADMIN:admin-bruxelles");
        assertThat(resultat.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder(
                        "ROLE_ADMIN",
                        "ROLE_ADMIN_SITE"
                );
        verifyNoInteractions(membreRepository);
    }

    @Test
    void shouldExposeInactiveAccountAsDisabled() {
        Membre membre = Membre.builder()
                .matricule("G1001")
                .motDePasseHash("hash-joueur")
                .categorieMembre(CategorieMembre.GLOBAL)
                .actif(false)
                .build();

        when(membreRepository.findByMatriculeIgnoreCase("G1001"))
                .thenReturn(Optional.of(membre));

        UserDetails resultat = userDetailsService.loadUserByUsername(
                IdentiteAuthentification.joueur("G1001")
        );

        assertThat(resultat.isEnabled()).isFalse();
    }

    @Test
    void shouldRejectUnknownAccountOrMissingPassword() {
        when(administrateurRepository.findByEmailOuLoginIgnoreCase(
                "admin-inconnu"
        ))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(
                IdentiteAuthentification.admin("admin-inconnu")
        )).isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shouldRejectMalformedTechnicalIdentity() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(
                "identite-sans-type"
        )).isInstanceOf(UsernameNotFoundException.class);

        verifyNoInteractions(membreRepository, administrateurRepository);
    }
}
