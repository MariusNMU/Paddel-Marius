package com.padelMarius.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApiExceptionHandlerTest.TestApiController.class)
@Import({
        ApiExceptionHandler.class,
        ApiExceptionHandlerTest.TestApiController.class
})
class ApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnApiErrorResponseWhenRequestBodyValidationFails()
            throws Exception {

        mockMvc.perform(post("/test-api/validation-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_INVALIDE"))
                .andExpect(jsonPath("$.message")
                        .value(containsString("nom")));
    }

    @Test
    void shouldReturnApiErrorResponseWhenJsonBodyIsMalformed()
            throws Exception {

        mockMvc.perform(post("/test-api/validation-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom":
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("JSON_INVALIDE"))
                .andExpect(jsonPath("$.message")
                        .value("Le corps JSON de la requête est invalide ou illisible."));
    }

    @Test
    void shouldReturnApiErrorResponseWhenQueryParamHasWrongType()
            throws Exception {

        mockMvc.perform(get("/test-api/query-param")
                        .param("siteId", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("REQUETE_INVALIDE"))
                .andExpect(jsonPath("$.message")
                        .value(containsString("siteId")));
    }

    @Test
    void shouldReturnApiErrorResponseWhenRequiredQueryParamIsMissing()
            throws Exception {

        mockMvc.perform(get("/test-api/required-param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("REQUETE_INVALIDE"))
                .andExpect(jsonPath("$.message")
                        .value(containsString("matricule")));
    }

    @Test
    void shouldReturnApiErrorResponseWhenQueryParamValidationFails()
            throws Exception {

        mockMvc.perform(get("/test-api/positive-param")
                        .param("siteId", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_INVALIDE"))
                .andExpect(jsonPath("$.message")
                        .exists());
    }

    @RestController
    @Validated
    public static class TestApiController {

        @PostMapping("/test-api/validation-body")
        public Map<String, String> validerBody(
                @Valid
                @RequestBody
                TestRequest request
        ) {
            return Map.of("status", "OK");
        }

        @GetMapping("/test-api/query-param")
        public Map<String, Object> queryParam(
                @RequestParam
                Long siteId
        ) {
            return Map.of("siteId", siteId);
        }

        @GetMapping("/test-api/required-param")
        public Map<String, String> requiredParam(
                @RequestParam
                String matricule
        ) {
            return Map.of("matricule", matricule);
        }

        @GetMapping("/test-api/positive-param")
        public Map<String, Object> positiveParam(
                @RequestParam
                @Positive
                Long siteId
        ) {
            return Map.of("siteId", siteId);
        }
    }

    public record TestRequest(
            @NotBlank(message = "Le nom est obligatoire.")
            String nom
    ) {
    }
}