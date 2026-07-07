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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXE_ROLE = "ROLE_";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<RoutePublique> ROUTES_PUBLIQUES = List.of(
            new RoutePublique(null, "/api/auth/**"),
            new RoutePublique("POST", "/api/membres/inscription"),
            new RoutePublique(null, "/api/health/**"),
            new RoutePublique("GET", "/api/sites"),
            new RoutePublique("GET", "/api/terrains"),
            new RoutePublique("GET", "/api/parametres-metier"),
            new RoutePublique(null, "/v3/api-docs/**"),
            new RoutePublique(null, "/swagger-ui.html"),
            new RoutePublique(null, "/swagger-ui/**"),
            new RoutePublique(null, "/h2-console/**")
    );

    private final JwtService jwtService;
    private final SecurityErrorWriter securityErrorWriter;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String chemin = extraireCheminSansContexte(request);

        return ROUTES_PUBLIQUES.stream()
                .anyMatch(route -> route.correspond(
                        request.getMethod(),
                        chemin
                ));
    }

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

    private String extraireCheminSansContexte(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (StringUtils.hasText(contextPath)
                && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }

        return requestUri;
    }

    private record RoutePublique(
            String methode,
            String chemin
    ) {

        private boolean correspond(
                String methodeRequete,
                String cheminRequete
        ) {
            boolean methodeCorrespond = methode == null
                    || methode.equalsIgnoreCase(methodeRequete);

            return methodeCorrespond
                    && PATH_MATCHER.match(chemin, cheminRequete);
        }
    }
}