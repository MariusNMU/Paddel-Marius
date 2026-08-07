package com.padelMarius.backend.security;

import com.padelMarius.backend.exception.AuthentificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXE_ROLE = "ROLE_";

    private final JwtService jwtService;
    private final SecurityErrorWriter securityErrorWriter;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authorization)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtUtilisateur utilisateur = jwtService
                    .extraireUtilisateurDepuisAuthorization(authorization);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            utilisateur,
                            null,
                            authorities(utilisateur)
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (AuthentificationException exception) {
            SecurityContextHolder.clearContext();
            securityErrorWriter.write(
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "AUTHENTIFICATION_INVALIDE",
                    exception.getMessage()
            );
        }
    }

    private List<SimpleGrantedAuthority> authorities(JwtUtilisateur utilisateur) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority(
                PREFIXE_ROLE + utilisateur.typeUtilisateur()
        ));

        if (StringUtils.hasText(utilisateur.role())) {
            authorities.add(new SimpleGrantedAuthority(
                    PREFIXE_ROLE
                            + utilisateur.typeUtilisateur()
                            + "_"
                            + utilisateur.role()
            ));
        }

        return authorities;
    }
}
