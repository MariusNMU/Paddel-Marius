package com.padelMarius.backend.security;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

public final class IdentiteAuthentification {

    private static final String SEPARATEUR = ":";

    private IdentiteAuthentification() {
    }

    public static String joueur(String matricule) {
        return construire(JwtService.TYPE_UTILISATEUR_JOUEUR, matricule);
    }

    public static String admin(String login) {
        return construire(JwtService.TYPE_UTILISATEUR_ADMIN, login);
    }

    public static String depuis(JwtUtilisateur utilisateur) {
        if (utilisateur == null) {
            throw utilisateurInconnu();
        }

        return construire(
                utilisateur.typeUtilisateur(),
                utilisateur.sujet()
        );
    }

    public static Identite parser(String nomUtilisateur) {
        if (!StringUtils.hasText(nomUtilisateur)) {
            throw utilisateurInconnu();
        }

        int positionSeparateur = nomUtilisateur.indexOf(SEPARATEUR);

        if (positionSeparateur <= 0
                || positionSeparateur == nomUtilisateur.length() - 1) {
            throw utilisateurInconnu();
        }

        String typeUtilisateur = nomUtilisateur.substring(
                0,
                positionSeparateur
        );
        String sujet = nomUtilisateur.substring(positionSeparateur + 1);

        if (!JwtService.TYPE_UTILISATEUR_JOUEUR.equals(typeUtilisateur)
                && !JwtService.TYPE_UTILISATEUR_ADMIN.equals(typeUtilisateur)) {
            throw utilisateurInconnu();
        }

        return new Identite(typeUtilisateur, sujet);
    }

    private static String construire(
            String typeUtilisateur,
            String sujet
    ) {
        if (!StringUtils.hasText(sujet)) {
            throw utilisateurInconnu();
        }

        if (!JwtService.TYPE_UTILISATEUR_JOUEUR.equals(typeUtilisateur)
                && !JwtService.TYPE_UTILISATEUR_ADMIN.equals(typeUtilisateur)) {
            throw utilisateurInconnu();
        }

        return typeUtilisateur + SEPARATEUR + sujet.trim();
    }

    private static UsernameNotFoundException utilisateurInconnu() {
        return new UsernameNotFoundException("Utilisateur inconnu.");
    }

    public record Identite(
            String typeUtilisateur,
            String sujet
    ) {
    }
}
