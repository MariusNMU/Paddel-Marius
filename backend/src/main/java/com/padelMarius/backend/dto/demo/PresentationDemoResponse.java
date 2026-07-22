package com.padelMarius.backend.dto.demo;

import com.padelMarius.backend.dto.site.SiteResponse;

import java.util.List;

public record PresentationDemoResponse(
        List<CategorieMembreDemoResponse> categoriesMembres,
        List<SiteResponse> sites,
        List<CompteJoueurDemoResponse> joueurs,
        List<CompteAdministrateurDemoResponse> administrateurs
) {
}
