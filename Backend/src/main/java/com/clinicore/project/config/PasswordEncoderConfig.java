package com.clinicore.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password Encoder Configuration
 * Configures Argon2 password hashing for the application

 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Creates an Argon2 password encoder bean
     *
     * Using Spring Security's recommended defaults:
     * - saltLength: 16 bytes (128 bits)
     * - hashLength: 32 bytes (256 bits)
     * - parallelism: 1 (number of threads)
     * - memory: 65536 KB (64 MB) - amount of memory used
     * - iterations: 3 - number of iterations
     *

     *
     * @return PasswordEncoder bean for use throughout the application
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }


}