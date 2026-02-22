package com.clinicore.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // configuration object for CORS
        // btw CORS stands for Cross-Origin Resource Sharing
        // CORS tells the browser to allow requests from other domains to make requests to our API
        // CORS is not a part of spring boot or react; it's the web browser itself (browsers enforce CORS for security reasons)
        // which is why we need to configure CORS in our API so it tells browsers our security & permissions
        CorsConfiguration config = new CorsConfiguration();

        // which port origins are allowed to make requests to the API
        // we'll add our deployed frontend port here later...
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174", "http://localhost:5175"));


        // OPTIONS for preflight means "can this request be made?"
        // when frontend makes a request, browser automatically sends an OPTIONS request to check if the request is valid
        // preflight is triggered when request uses other HTTP methods or cross-origin reqs (which wes doing)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // allow all request headers (what browser is allowed to send)
        config.addAllowedHeader("*");

        // allow cookies and authentication headers (allows browser to send tokens/credentials)
        config.setAllowCredentials(true);

        // cache preflight response for 1 hour
        // basically saying, how long the browser is allowed to cache the preflight response
        // so it wont send OPTIONS check to the server every time the browser makes a request
        config.setMaxAge(3600L);

        // apply these CORS rules to all API paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
