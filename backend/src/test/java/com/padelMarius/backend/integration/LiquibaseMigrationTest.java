package com.padelMarius.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = "spring.liquibase.contexts=demo"
)
class LiquibaseMigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void liquibase_doit_creer_le_schema_h2_et_charger_la_demo() {
        Integer nombreMigrations = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM databasechangelog
                WHERE id = '001-create-initial-schema'
                """,
                Integer.class
        );

        List<String> tables = jdbcTemplate.queryForList(
                """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'PUBLIC'
                """,
                String.class
        );

        Integer nombreSites = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM site",
                Integer.class
        );

        assertThat(nombreMigrations).isEqualTo(1);
        assertThat(tables).contains(
                "SITE",
                "TERRAIN",
                "HORAIRE_ANNUEL_SITE",
                "FERMETURE",
                "MEMBRE",
                "ADMINISTRATEUR",
                "PADEL_MATCH",
                "PARTICIPATION",
                "DETTE",
                "PENALITE",
                "PAIEMENT"
        );
        assertThat(nombreSites).isEqualTo(2);
    }
}
