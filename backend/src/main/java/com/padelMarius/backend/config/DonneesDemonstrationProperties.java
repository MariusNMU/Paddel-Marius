package com.padelMarius.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "padel.demo")
@Getter
@Setter
public class DonneesDemonstrationProperties {

    private boolean enabled;
    private String motDePasseJoueur;
    private List<Joueur> joueurs = new ArrayList<>();
    private List<Administrateur> administrateurs = new ArrayList<>();

    @Getter
    @Setter
    public static class Joueur {

        private String matricule;
        private String description;
    }

    @Getter
    @Setter
    public static class Administrateur {

        private String login;
        private String motDePasse;
        private String description;
    }
}
