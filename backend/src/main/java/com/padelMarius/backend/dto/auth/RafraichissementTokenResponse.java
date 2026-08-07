package com.padelMarius.backend.dto.auth;

import java.time.LocalDateTime;

public record RafraichissementTokenResponse(
        String token,
        LocalDateTime expirationToken
) {
}
