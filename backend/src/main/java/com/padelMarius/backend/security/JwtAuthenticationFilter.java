package com.padelMarius.backend.security;

import com.padelMarius.backend.exception.AuthentificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
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

            UserDetails userDetails = userDetailsService.loadUserByUsername(
                    IdentiteAuthentification.depuis(utilisateur)
            );

            verifierCompteUtilisable(userDetails);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            utilisateur,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (AuthentificationException exception) {
            ecrireErreur(response, exception.getMessage());
        } catch (AuthenticationException exception) {
            ecrireErreur(response, "Token JWT invalide.");
        }
    }

    private void verifierCompteUtilisable(UserDetails userDetails) {
        if (!userDetails.isEnabled()
                || !userDetails.isAccountNonExpired()
                || !userDetails.isAccountNonLocked()
                || !userDetails.isCredentialsNonExpired()) {
            throw new AuthentificationException("Token JWT invalide.");
        }
    }

    private void ecrireErreur(
            HttpServletResponse response,
            String message
    ) throws IOException {
        SecurityContextHolder.clearContext();
        securityErrorWriter.write(
                response,
                HttpStatus.UNAUTHORIZED,
                "AUTHENTIFICATION_INVALIDE",
                message
        );
    }
}
