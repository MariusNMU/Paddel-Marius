package com.padelMarius.backend.security;

import com.padelMarius.backend.entity.JetonRafraichissement;
import com.padelMarius.backend.exception.AuthentificationException;
import com.padelMarius.backend.repository.JetonRafraichissementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JetonRafraichissementService {

    private final JetonRafraichissementRepository repository;
    private final Clock clock;

    @Transactional
    public void enregistrer(
            JwtService.TokenGenere token,
            String sujet,
            String typeUtilisateur
    ) {
        if (token == null
                || !StringUtils.hasText(token.identifiantToken())
                || token.expiration() == null
                || !StringUtils.hasText(sujet)
                || !StringUtils.hasText(typeUtilisateur)) {
            throw new IllegalArgumentException(
                    "Un refresh token identifié et daté est obligatoire."
            );
        }

        repository.deleteByDateExpirationBefore(LocalDateTime.now(clock));
        repository.save(new JetonRafraichissement(
                token.identifiantToken(),
                token.expiration(),
                sujet,
                typeUtilisateur
        ));
    }

    @Transactional
    public void consommer(JwtUtilisateur utilisateur) {
        if (utilisateur == null) {
            throw refreshTokenInvalide();
        }

        JetonRafraichissement jeton = trouverPourMiseAJour(
                utilisateur.identifiantToken()
        );
        LocalDateTime maintenant = LocalDateTime.now(clock);

        if (!jeton.estActif(maintenant)
                || !jeton.correspondA(
                        utilisateur.sujet(),
                        utilisateur.typeUtilisateur()
                )) {
            throw refreshTokenInvalide();
        }

        jeton.revoquer(maintenant);
    }

    @Transactional
    public void revoquerSiPresent(String identifiantToken) {
        if (!StringUtils.hasText(identifiantToken)) {
            return;
        }

        repository.findByIdentifiantForUpdate(identifiantToken)
                .filter(jeton -> !jeton.isRevoque())
                .ifPresent(jeton -> jeton.revoquer(
                        LocalDateTime.now(clock)
                ));
    }

    private JetonRafraichissement trouverPourMiseAJour(
            String identifiantToken
    ) {
        if (!StringUtils.hasText(identifiantToken)) {
            throw refreshTokenInvalide();
        }

        return repository.findByIdentifiantForUpdate(identifiantToken)
                .orElseThrow(this::refreshTokenInvalide);
    }

    private AuthentificationException refreshTokenInvalide() {
        return new AuthentificationException(
                "Refresh token invalide."
        );
    }
}
