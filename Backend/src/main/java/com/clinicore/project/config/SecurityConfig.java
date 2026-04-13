package com.clinicore.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                    // CORS pre-flight
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // public endpoints — no login required
                    .requestMatchers(HttpMethod.POST, "/api/accountCredential/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accountCredential/request-access").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accountCredential/verify-activation-code").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accountCredential/create-account").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accountCredential/forgot-userid").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accountCredential/forgot-password").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accountCredential/reset-password").permitAll()

                    // admin only
                    .requestMatchers("/api/accountCredential/account-requests/**").hasRole("ADMIN")
                    .requestMatchers("/api/inventory/**").hasAnyRole("ADMIN", "CAREGIVER")

                    // residents can read their own record (ownership check is in the controller)
                    .requestMatchers(HttpMethod.GET, "/api/residents/full/**").hasAnyRole("ADMIN", "CAREGIVER", "RESIDENT")

                    // everything else under /api/residents/** stays admin + caregiver only
                    .requestMatchers("/api/residents/**").hasAnyRole("ADMIN", "CAREGIVER")
                    .requestMatchers("/api/caregivers/**").hasAnyRole("ADMIN", "CAREGIVER")
                    .requestMatchers("/api/medicalInformation/**").hasAnyRole("ADMIN", "CAREGIVER")

                    // any authenticated user
                    .requestMatchers("/api/messages/**").authenticated()
                    .requestMatchers("/api/documents/**").authenticated()
                    .requestMatchers("/api/upload/**").authenticated()
                    .requestMatchers("/api/user/**").authenticated()

                    // deny everything else
                    .anyRequest().denyAll()
                )

                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
