package com.padelMarius.backend.controller;

import com.padelMarius.backend.dto.auth.AuthAdminResponse;
import com.padelMarius.backend.dto.auth.AuthJoueurResponse;
import com.padelMarius.backend.dto.auth.ConnexionAdminRequest;
import com.padelMarius.backend.dto.auth.ConnexionJoueurRequest;
import com.padelMarius.backend.dto.auth.RafraichissementTokenResponse;
import com.padelMarius.backend.security.RefreshTokenCookieService;
import com.padelMarius.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping("/api/auth/joueur")
    public ResponseEntity<AuthJoueurResponse> authentifierJoueur(
            @Valid @RequestBody ConnexionJoueurRequest request
    ) {
        AuthService.ResultatAuthentification<AuthJoueurResponse> resultat =
                authService.authentifierJoueur(request);

        return reponseAvecCookie(resultat);
    }

    @PostMapping("/api/auth/admin")
    public ResponseEntity<AuthAdminResponse> authentifierAdmin(
            @Valid @RequestBody ConnexionAdminRequest request
    ) {
        AuthService.ResultatAuthentification<AuthAdminResponse> resultat =
                authService.authentifierAdmin(request);

        return reponseAvecCookie(resultat);
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<RafraichissementTokenResponse> rafraichir(
            @CookieValue(
                    name = RefreshTokenCookieService.NOM_COOKIE,
                    required = false
            ) String refreshToken
    ) {
        AuthService.ResultatAuthentification<
                RafraichissementTokenResponse
                > resultat = authService.rafraichir(refreshToken);

        return reponseAvecCookie(resultat);
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> deconnecter(
            @CookieValue(
                    name = RefreshTokenCookieService.NOM_COOKIE,
                    required = false
            ) String refreshToken
    ) {
        authService.deconnecter(refreshToken);

        return ResponseEntity.noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshTokenCookieService.supprimer().toString()
                )
                .build();
    }

    private <T> ResponseEntity<T> reponseAvecCookie(
            AuthService.ResultatAuthentification<T> resultat
    ) {
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshTokenCookieService
                                .creer(resultat.refreshToken())
                                .toString()
                )
                .body(resultat.reponse());
    }
}
