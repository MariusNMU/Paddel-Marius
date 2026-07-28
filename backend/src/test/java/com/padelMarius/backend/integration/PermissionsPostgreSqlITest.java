package com.padelMarius.backend.integration;

import com.padelMarius.backend.config.PostgresDemoDataSeeder;
import com.padelMarius.backend.entity.Site;
import com.padelMarius.backend.repository.SiteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@SpringBootTest(
        webEnvironment =
                SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "padel.demo.enabled=false",
                "spring.sql.init.mode=never",
                "spring.jpa.hibernate.ddl-auto=validate"
        }
)
@ActiveProfiles("postgres")
@Testcontainers
class PermissionsPostgreSqlITest {

    private static final String BASE =
            "padel_db";

    private static final String ADMIN =
            "padel_admin";

    private static final String ADMIN_PASSWORD =
            "padel_admin_password";

    private static final String MIGRATION =
            "padel_migration";

    private static final String MIGRATION_PASSWORD =
            "padel_migration_password";

    private static final String APPLICATION =
            "padel_app";

    private static final String APPLICATION_PASSWORD =
            "padel_app_password";

    private static final String LECTURE_SEULE =
            "padel_readonly";

    private static final String LECTURE_SEULE_PASSWORD =
            "padel_readonly_password";

    @Container
    private static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(
                    "postgres:16-alpine"
            )
                    .withDatabaseName(BASE)
                    .withUsername(ADMIN)
                    .withPassword(ADMIN_PASSWORD)
                    .withCopyFileToContainer(
                            MountableFile.forHostPath(
                                    trouverScriptInitialisation()
                                            .toString()
                            ),
                            "/docker-entrypoint-initdb.d/"
                                    + "01-create-users-and-rights.sql"
                    );

    @DynamicPropertySource
    static void configurerPostgreSql(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                () -> APPLICATION
        );

        registry.add(
                "spring.datasource.password",
                () -> APPLICATION_PASSWORD
        );

        registry.add(
                "spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver"
        );

        registry.add(
                "spring.liquibase.url",
                POSTGRES::getJdbcUrl
        );

        registry.add(
                "spring.liquibase.user",
                () -> MIGRATION
        );

        registry.add(
                "spring.liquibase.password",
                () -> MIGRATION_PASSWORD
        );
    }

    @MockitoBean
    private PostgresDemoDataSeeder postgresDemoDataSeeder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SiteRepository siteRepository;

    @Test
    void liquibase_et_hibernate_doivent_utiliser_des_roles_distincts() {
        String utilisateurApplication =
                jdbcTemplate.queryForObject(
                        "SELECT current_user",
                        String.class
                );

        String proprietaireTableSite =
                jdbcTemplate.queryForObject(
                        """
                        SELECT tableowner
                        FROM pg_tables
                        WHERE schemaname = 'public'
                          AND tablename = 'site'
                        """,
                        String.class
                );

        assertThat(utilisateurApplication)
                .isEqualTo(APPLICATION);

        assertThat(proprietaireTableSite)
                .isEqualTo(MIGRATION);

        assertThat(
                possedePrivilegeSchema(
                        MIGRATION,
                        "CREATE"
                )
        ).isTrue();

        assertThat(
                possedePrivilegeSchema(
                        APPLICATION,
                        "CREATE"
                )
        ).isFalse();

        assertThat(
                possedePrivilegeSchema(
                        LECTURE_SEULE,
                        "CREATE"
                )
        ).isFalse();
    }

    @Test
    void padel_app_doit_pouvoir_effectuer_un_crud_repository() {
        Site site = Site.builder()
                .code("ROLE-CRUD")
                .nom("Site test CRUD")
                .adresse("Adresse test CRUD")
                .actif(true)
                .build();

        Site siteCree =
                siteRepository.saveAndFlush(site);

        assertThat(siteCree.getId())
                .isNotNull();

        Site siteLu =
                siteRepository.findById(
                        siteCree.getId()
                ).orElseThrow();

        assertThat(siteLu.getCode())
                .isEqualTo("ROLE-CRUD");

        siteLu.setNom("Site CRUD modifié");

        siteRepository.saveAndFlush(siteLu);

        Site siteModifie =
                siteRepository.findById(
                        siteCree.getId()
                ).orElseThrow();

        assertThat(siteModifie.getNom())
                .isEqualTo("Site CRUD modifié");

        siteRepository.deleteById(
                siteCree.getId()
        );

        siteRepository.flush();

        assertThat(
                siteRepository.existsById(
                        siteCree.getId()
                )
        ).isFalse();
    }

    @Test
    void padel_app_ne_doit_pas_pouvoir_creer_une_table() {
        DataAccessException erreur =
                catchThrowableOfType(
                        () -> jdbcTemplate.execute(
                                """
                                CREATE TABLE
                                    table_interdite_padel_app (
                                        id BIGINT PRIMARY KEY
                                    )
                                """
                        ),
                        DataAccessException.class
                );

        assertThat(erreur)
                .isNotNull();

        Throwable cause =
                erreur.getMostSpecificCause();

        assertThat(cause)
                .isInstanceOf(SQLException.class);

        assertThat(
                ((SQLException) cause)
                        .getSQLState()
        ).isEqualTo("42501");
    }

    @Test
    void padel_readonly_doit_lire_sans_pouvoir_inserer()
            throws SQLException {
        Site site = Site.builder()
                .code("ROLE-READONLY")
                .nom("Site visible en lecture")
                .adresse("Adresse lecture seule")
                .actif(true)
                .build();

        siteRepository.saveAndFlush(site);

        try (
                Connection connexion =
                        DriverManager.getConnection(
                                POSTGRES.getJdbcUrl(),
                                LECTURE_SEULE,
                                LECTURE_SEULE_PASSWORD
                        );

                PreparedStatement lecture =
                        connexion.prepareStatement(
                                """
                                SELECT nom
                                FROM site
                                WHERE code = ?
                                """
                        )
        ) {
            lecture.setString(
                    1,
                    "ROLE-READONLY"
            );

            try (
                    ResultSet resultat =
                            lecture.executeQuery()
            ) {
                assertThat(resultat.next())
                        .isTrue();

                assertThat(
                        resultat.getString("nom")
                ).isEqualTo(
                        "Site visible en lecture"
                );
            }
        }

        try (
                Connection connexion =
                        DriverManager.getConnection(
                                POSTGRES.getJdbcUrl(),
                                LECTURE_SEULE,
                                LECTURE_SEULE_PASSWORD
                        );

                PreparedStatement insertion =
                        connexion.prepareStatement(
                                """
                                INSERT INTO site (
                                    code,
                                    nom,
                                    adresse,
                                    actif
                                )
                                VALUES (?, ?, ?, ?)
                                """
                        )
        ) {
            insertion.setString(
                    1,
                    "ROLE-RO-INSERT"
            );

            insertion.setString(
                    2,
                    "Insertion interdite"
            );

            insertion.setString(
                    3,
                    "Adresse interdite"
            );

            insertion.setBoolean(
                    4,
                    true
            );

            SQLException erreur =
                    catchThrowableOfType(
                            () -> insertion.executeUpdate(),
                            SQLException.class
                    );

            assertThat((Throwable) erreur)
                    .isNotNull();

            assertThat(erreur.getSQLState())
                    .isEqualTo("42501");
        }
    }

    private boolean possedePrivilegeSchema(
            String role,
            String privilege
    ) {
        Boolean resultat =
                jdbcTemplate.queryForObject(
                        """
                        SELECT has_schema_privilege(
                            ?,
                            'public',
                            ?
                        )
                        """,
                        Boolean.class,
                        role,
                        privilege
                );

        return Boolean.TRUE.equals(resultat);
    }

    private static Path trouverScriptInitialisation() {
        Path dossier =
                Path.of(
                                System.getProperty("user.dir")
                        )
                        .toAbsolutePath()
                        .normalize();

        while (dossier != null) {
            Path candidat =
                    dossier.resolve(
                            Path.of(
                                    "docker",
                                    "postgres",
                                    "init",
                                    "01-create-users-and-rights.sql"
                            )
                    );

            if (Files.isRegularFile(candidat)) {
                return candidat;
            }

            dossier = dossier.getParent();
        }

        throw new IllegalStateException(
                "Script PostgreSQL Docker introuvable : "
                        + "docker/postgres/init/"
                        + "01-create-users-and-rights.sql"
        );
    }
}
