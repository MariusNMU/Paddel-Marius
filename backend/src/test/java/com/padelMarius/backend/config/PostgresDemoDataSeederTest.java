package com.padelMarius.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgresDemoDataSeederTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private JdbcOperations jdbcOperations;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PostgresDemoDataSeeder seeder;

    @BeforeEach
    void setUp() {
        when(jdbcTemplate.getJdbcOperations()).thenReturn(jdbcOperations);
        when(passwordEncoder.encode("password")).thenReturn("HASH_JOUEUR");
        when(passwordEncoder.encode("secret")).thenReturn("HASH_ADMIN_GLOBAL");
        when(passwordEncoder.encode("secret-site")).thenReturn("HASH_ADMIN_SITE");

        Clock clock = Clock.fixed(
                Instant.parse("2026-08-20T10:00:00Z"),
                ZoneId.of("Europe/Brussels")
        );

        seeder = new PostgresDemoDataSeeder(jdbcTemplate, passwordEncoder, clock);
    }

    @Test
    void run_insereLesDonneesDemoCompatiblesAvecLeFrontend() throws Exception {
        seeder.run();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SqlParameterSource> parametresCaptor =
                ArgumentCaptor.forClass(SqlParameterSource.class);

        verify(jdbcTemplate, atLeastOnce()).update(
                sqlCaptor.capture(),
                parametresCaptor.capture()
        );

        List<String> requetes = sqlCaptor.getAllValues();
        String toutesLesRequetes = String.join("\n", requetes);

        String requeteMembre = requetes.stream()
                .filter(requete ->
                        requete.contains("INSERT INTO membre (")
                )
                .findFirst()
                .orElseThrow();

        String requeteDette = requetes.stream()
                .filter(requete ->
                        requete.contains("INSERT INTO dette (")
                )
                .findFirst()
                .orElseThrow();

        List<SqlParameterSource> parametresHoraires =
                parametresCaptor.getAllValues().stream()
                        .filter(parametres ->
                                parametres.hasValue("anneeCivile")
                        )
                        .toList();

        List<Object> anneesHoraires =
                parametresHoraires.stream()
                        .map(parametres ->
                                parametres.getValue("anneeCivile")
                        )
                        .toList();

        List<SqlParameterSource> tousLesParametres =
                parametresCaptor.getAllValues();

        assertThat(toutesLesRequetes).contains("INSERT INTO site");
        assertThat(toutesLesRequetes).contains("INSERT INTO terrain");
        assertThat(toutesLesRequetes).contains("INSERT INTO membre");
        assertThat(toutesLesRequetes).contains("INSERT INTO administrateur");
        assertThat(toutesLesRequetes).contains("INSERT INTO padel_match");
        assertThat(toutesLesRequetes).contains("INSERT INTO participation");
        assertThat(toutesLesRequetes).contains("INSERT INTO paiement");
        assertThat(anneesHoraires).contains(2026, 2027);
        assertThat(toutesLesRequetes).contains(
                "ON CONFLICT (site_id, annee_civile) DO UPDATE SET"
        );

        assertThat(parametresHoraires)
                .hasSize(6)
                .allSatisfy(parametres ->
                        assertThat(
                                parametres.hasValue("id")
                        ).isFalse()
                );

        assertThat(requeteMembre).doesNotContain(
                "solde_credit = EXCLUDED.solde_credit"
        );

        assertThat(requeteDette).contains(
                "ON CONFLICT DO NOTHING"
        );

        assertThat(parametresParId(tousLesParametres, 1003L)).anySatisfy(parametres -> {
            assertThat(parametres.getValue("actif")).isEqualTo(false);
        });

        assertThat(parametresParId(tousLesParametres, 1104L)).anySatisfy(parametres -> {
            assertThat(parametres.getValue("actif")).isEqualTo(false);
        });

        assertThat(parametresParId(tousLesParametres, 4001L)).anySatisfy(parametres -> {
            assertThat(parametres.getValue("matchId")).isEqualTo(3004L);
            assertThat(parametres.getValue("membreResponsableId")).isEqualTo(2002L);
            assertThat(parametres.getValue("montantRestant"))
                    .isEqualTo(new BigDecimal("45.00"));
        });

        assertThat(parametresParId(tousLesParametres, 5001L)).anySatisfy(parametres -> {
            assertThat(parametres.getValue("matchSourceId")).isEqualTo(3004L);
            assertThat(parametres.getValue("membreId")).isEqualTo(2002L);
        });

        verify(passwordEncoder).encode("password");
        verify(passwordEncoder).encode("secret");
        verify(passwordEncoder).encode("secret-site");

        verify(jdbcOperations, atLeastOnce()).execute(anyString());
    }

    private List<SqlParameterSource> parametresParId(
            List<SqlParameterSource> parametres,
            Long id
    ) {
        return parametres.stream()
                .filter(source -> source.hasValue("id"))
                .filter(source -> id.equals(source.getValue("id")))
                .toList();
    }

    @Test
    void run_reconcilieLesDettesDejaPayeesApresInsertionDesPaiements()
            throws Exception {
        seeder.run();

        ArgumentCaptor<String> sqlCaptor =
                ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SqlParameterSource> parametresCaptor =
                ArgumentCaptor.forClass(SqlParameterSource.class);

        verify(jdbcTemplate, atLeastOnce()).update(
                sqlCaptor.capture(),
                parametresCaptor.capture()
        );

        List<String> requetes = sqlCaptor.getAllValues();
        List<SqlParameterSource> parametres =
                parametresCaptor.getAllValues();

        int indexDerniereInsertionPaiement =
                IntStream.range(0, requetes.size())
                        .filter(index ->
                                requetes.get(index)
                                        .contains("INSERT INTO paiement (")
                        )
                        .max()
                        .orElseThrow();

        int indexReconciliationDette =
                IntStream.range(0, requetes.size())
                        .filter(index ->
                                requetes.get(index)
                                        .contains("UPDATE dette d")
                        )
                        .findFirst()
                        .orElseThrow();

        String requeteReconciliation =
                requetes.get(indexReconciliationDette);
        SqlParameterSource parametresReconciliation =
                parametres.get(indexReconciliationDette);

        assertThat(indexReconciliationDette)
                .isGreaterThan(indexDerniereInsertionPaiement);

        assertThat(requeteReconciliation).contains(
                "UPDATE dette d",
                "FROM paiement p",
                "p.dette_id = d.id",
                "p.statut_paiement = :statutPaiementPaye",
                "date_reglement = COALESCE(",
                "statut_dette = :statutDetteReglee"
        );

        assertThat(
                parametresReconciliation.getValue("montantRestant")
        ).isEqualTo(BigDecimal.ZERO);

        assertThat(
                parametresReconciliation.getValue("statutDetteReglee")
        ).isEqualTo("REGLEE");

        assertThat(
                parametresReconciliation.getValue("statutPaiementPaye")
        ).isEqualTo("PAYE");
    }
}
