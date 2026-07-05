package com.padelMarius.backend.config;

import com.padelMarius.backend.security.JwtAuthenticationFilter;
import com.padelMarius.backend.security.JwtService;
import com.padelMarius.backend.security.SecurityErrorWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityErrorWriter securityErrorWriter;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService
    ) {
        return new JwtAuthenticationFilter(
                jwtService,
                securityErrorWriter
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, exception) ->
                                securityErrorWriter.write(
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "AUTHENTIFICATION_INVALIDE",
                                        "Authentification JWT obligatoire."
                                )
                        )
                        .accessDeniedHandler((request, response, exception) ->
                                securityErrorWriter.write(
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "ACCES_REFUSE",
                                        "Accès refusé."
                                )
                        )
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/membres/inscription").permitAll()
                        .requestMatchers("/api/health/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/sites").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/terrains").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/parametres-metier").permitAll()

                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .requestMatchers("/api/membres/**").hasRole("JOUEUR")
                        .requestMatchers("/api/disponibilites/**").hasRole("JOUEUR")
                        .requestMatchers("/api/matches/**").hasRole("JOUEUR")
                        .requestMatchers("/api/participations/**").hasRole("JOUEUR")
                        .requestMatchers("/api/dettes/**").hasRole("JOUEUR")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }
}