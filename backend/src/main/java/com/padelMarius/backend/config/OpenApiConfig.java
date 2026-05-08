package com.padelMarius.backend.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI padelMariusOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Padel Marius API")
                        .version("1.0.0")
                        .description("""
                                API REST du projet de réservation de terrains de padel.

                                Fonctionnalités principales :
                                - consultation des disponibilités
                                - création de matches privés et publics
                                - gestion des participations
                                - paiement des participations
                                - gestion des dettes organisateur
                                - traitement de veille des matches
                                - statistiques administrateur
                                - authentification simple joueur/admin
                                """)
                        .contact(new Contact()
                                .name("Projet Padel Marius")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Backend local")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentation projet")
                        .url("http://localhost:8080/swagger-ui.html"));
    }
}