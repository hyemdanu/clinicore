package com.clinicore.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password Encoder Configuration
 * Configures Argon2 password hashing for the application
 *

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
     * These parameters provide strong security while maintaining
     * reasonable performance for user authentication.
     *
     * @return PasswordEncoder bean for use throughout the application
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    /*
     * Alternative: Custom parameters if you need more control
     *
     * Uncomment and modify if you need to tune performance vs security:
     *
     * @Bean
     * public PasswordEncoder passwordEncoder() {
     *     return new Argon2PasswordEncoder(
     *         16,     // saltLength - 16 bytes is standard
     *         32,     // hashLength - 32 bytes provides 256-bit security
     *         1,      // parallelism - number of threads (1 = sequential)
     *         65536,  // memory - 64 MB (increase for more security, decrease for performance)
     *         3       // iterations - number of passes (increase for more security)
     *     );
     * }
     */
}