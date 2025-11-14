package com.clinicore.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Java Spring by default enables CSRF protection, which is not needed for our API
        // Java Spring inserts a hidden CSRF token into every HTML form, which is used to validate the request
        // but since we're using a REST API, we disable CSRF protection completely
        http.csrf(csrf -> csrf.disable())

                // this enable CORS and uses whatever rules we defined in CorsConfig
                .cors(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // enables CORS pre-flight requests
                    .requestMatchers(HttpMethod.POST, "/api/accountCredential/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/accountCredential/register").permitAll()
                    .anyRequest().permitAll() // we have to change this later to authenticate users...
                )

                .formLogin(form -> form.disable()); // disables the default HTML login page spring provides

        return http.build();

    }

    // our pw encoder --> provided by spring has .encode(), .matches(raw,hashed), .upgradeEncoding(raw) methods
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
