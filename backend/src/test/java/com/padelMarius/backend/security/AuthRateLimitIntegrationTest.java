package com.padelMarius.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.HttpHeaders.RETRY_AFTER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "padel.demo.enabled=true",
        "padel.security.auth-rate-limit.max-attempts=2",
        "padel.security.auth-rate-limit.window-minutes=10"
})
@AutoConfigureMockMvc
@Sql(scripts = "/data.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthRateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldLimitLoginAndRefreshInIndependentBuckets()
            throws Exception {
        for (int tentative = 0; tentative < 2; tentative++) {
            mockMvc.perform(post("/api/auth/joueur")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "matricule": "G1001",
                                      "motDePasse": "incorrect"
                                    }
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/joueur")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "matricule": "G1001",
                                  "motDePasse": "incorrect"
                                }
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(RETRY_AFTER))
                .andExpect(jsonPath("$.code")
                        .value("TROP_DE_TENTATIVES"));

        for (int tentative = 0; tentative < 2; tentative++) {
            mockMvc.perform(post("/api/auth/refresh"))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(RETRY_AFTER))
                .andExpect(jsonPath("$.message")
                        .value("Trop de tentatives. Réessayez plus tard."));
    }
}
