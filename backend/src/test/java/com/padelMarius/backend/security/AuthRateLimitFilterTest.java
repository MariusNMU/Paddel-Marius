package com.padelMarius.backend.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AuthRateLimitFilterTest {

    private SecurityErrorWriter securityErrorWriter;
    private FilterChain filterChain;
    private AuthRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-05-20T10:00:00Z"),
                ZoneOffset.UTC
        );
        LimiteurTentativesAuthentification limiteur =
                new LimiteurTentativesAuthentification(1, 10, clock);
        securityErrorWriter = mock(SecurityErrorWriter.class);
        filterChain = mock(FilterChain.class);
        filter = new AuthRateLimitFilter(
                limiteur,
                securityErrorWriter
        );
    }

    @Test
    void shouldRejectLimitedAuthenticationRequestWithRetryAfter()
            throws Exception {
        MockHttpServletRequest premiereRequete = requetePost(
                "/api/auth/joueur"
        );
        filter.doFilter(
                premiereRequete,
                new MockHttpServletResponse(),
                filterChain
        );

        MockHttpServletResponse reponseLimitee =
                new MockHttpServletResponse();
        filter.doFilter(
                requetePost("/api/auth/joueur"),
                reponseLimitee,
                filterChain
        );

        verify(filterChain).doFilter(
                org.mockito.ArgumentMatchers.same(premiereRequete),
                org.mockito.ArgumentMatchers.any()
        );
        assertThat(reponseLimitee.getHeader(HttpHeaders.RETRY_AFTER))
                .isEqualTo("600");
        verify(securityErrorWriter).write(
                reponseLimitee,
                HttpStatus.TOO_MANY_REQUESTS,
                "TROP_DE_TENTATIVES",
                "Trop de tentatives. Réessayez plus tard."
        );
    }

    @Test
    void shouldIgnoreGetAndUnrelatedPostRequests() throws Exception {
        MockHttpServletRequest get = new MockHttpServletRequest(
                "GET",
                "/api/auth/joueur"
        );
        MockHttpServletRequest autrePost = requetePost(
                "/api/participations"
        );

        filter.doFilter(
                get,
                new MockHttpServletResponse(),
                filterChain
        );
        filter.doFilter(
                autrePost,
                new MockHttpServletResponse(),
                filterChain
        );

        verify(filterChain).doFilter(
                org.mockito.ArgumentMatchers.same(get),
                org.mockito.ArgumentMatchers.any()
        );
        verify(filterChain).doFilter(
                org.mockito.ArgumentMatchers.same(autrePost),
                org.mockito.ArgumentMatchers.any()
        );
        verify(securityErrorWriter, never()).write(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    private MockHttpServletRequest requetePost(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                uri
        );
        request.setRemoteAddr("127.0.0.1");
        return request;
    }
}
