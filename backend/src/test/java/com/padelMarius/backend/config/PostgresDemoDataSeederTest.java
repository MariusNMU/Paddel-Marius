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

import java.util.List;

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

        seeder = new PostgresDemoDataSeeder(jdbcTemplate, passwordEncoder);
    }

    @Test
    void run_insereLesDonneesDemoAvecLesIdsCompatiblesFrontend() throws Exception {
        seeder.run();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        verify(jdbcTemplate, atLeastOnce()).update(
                sqlCaptor.capture(),
                org.mockito.ArgumentMatchers.any(SqlParameterSource.class)
        );

        List<String> requetes = sqlCaptor.getAllValues();
        String toutesLesRequetes = String.join("\n", requetes);

        assertThat(toutesLesRequetes).contains("INSERT INTO site");
        assertThat(toutesLesRequetes).contains("INSERT INTO terrain");
        assertThat(toutesLesRequetes).contains("INSERT INTO membre");
        assertThat(toutesLesRequetes).contains("INSERT INTO administrateur");
        assertThat(toutesLesRequetes).contains("INSERT INTO padel_match");
        assertThat(toutesLesRequetes).contains("INSERT INTO participation");
        assertThat(toutesLesRequetes).contains("INSERT INTO paiement");

        verify(passwordEncoder).encode("password");
        verify(passwordEncoder).encode("secret");
        verify(passwordEncoder).encode("secret-site");

        verify(jdbcOperations, atLeastOnce()).execute(anyString());
    }
}