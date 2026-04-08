package com.clinicore.project.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JwtServiceTest {

    private JwtService jwtService;

    // HS256 needs at least 32 bytes
    private static final String TEST_SECRET = "test-secret-key-at-least-32-chars-long-for-hs256!";
    private static final long ONE_HOUR_MS = 60L * 60L * 1000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, ONE_HOUR_MS);
    }

    @Test
    @Order(1)
    @DisplayName("TEST 1: Generated token parses back to same claims")
    void testGenerateAndValidateToken() {
        System.out.println("\n=== TEST 1: Generate & Validate Token ===");

        String token = jwtService.generateToken(1L, "knguyen", "ADMIN");
        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = jwtService.validateToken(token);
        assertNotNull(claims);
        assertEquals("1", claims.getSubject());

        assertEquals(1L, jwtService.getUserId(token));
        assertEquals("knguyen", jwtService.getUsername(token));
        assertEquals("ADMIN", jwtService.getRole(token));

        System.out.println("PASSED TEST 1");
    }

    @Test
    @Order(2)
    @DisplayName("TEST 2: Tampered token throws on validate")
    void testTamperedToken() {
        System.out.println("\n=== TEST 2: Tampered Token Rejected ===");

        String token = jwtService.generateToken(1L, "knguyen", "ADMIN");

        // swap the signature for a fake one
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
        String tampered = parts[0] + "." + parts[1] + ".ZmFrZS1zaWduYXR1cmUtZm9yLXRlc3Rpbmc";

        assertThrows(Exception.class, () -> jwtService.validateToken(tampered));

        System.out.println("PASSED TEST 2");
    }

    @Test
    @Order(3)
    @DisplayName("TEST 3: Caregiver role round-trips")
    void testCaregiverRole() {
        System.out.println("\n=== TEST 3: Caregiver Role ===");

        String token = jwtService.generateToken(2L, "caregiver1", "CAREGIVER");
        assertEquals("CAREGIVER", jwtService.getRole(token));
        assertEquals(2L, jwtService.getUserId(token));
        assertEquals("caregiver1", jwtService.getUsername(token));

        System.out.println("PASSED TEST 3");
    }

    @Test
    @Order(4)
    @DisplayName("TEST 4: Resident role round-trips")
    void testResidentRole() {
        System.out.println("\n=== TEST 4: Resident Role ===");

        String token = jwtService.generateToken(99L, "resident1", "RESIDENT");
        assertEquals("RESIDENT", jwtService.getRole(token));
        assertEquals(99L, jwtService.getUserId(token));

        System.out.println("PASSED TEST 4");
    }

    @Test
    @Order(5)
    @DisplayName("TEST 5: Random garbage string throws on validate")
    void testGarbageTokenRejected() {
        System.out.println("\n=== TEST 5: Garbage Token Rejected ===");

        assertThrows(Exception.class, () -> jwtService.validateToken("not.a.real.token"));
        assertThrows(Exception.class, () -> jwtService.validateToken("totally-bogus"));

        System.out.println("PASSED TEST 5");
    }

    @Test
    @Order(6)
    @DisplayName("TEST 6: Expired token throws on validate")
    void testExpiredTokenRejected() {
        System.out.println("\n=== TEST 6: Expired Token Rejected ===");

        // 1ms expiry so the token is dead on arrival
        JwtService shortLived = new JwtService(TEST_SECRET, 1L);
        String token = shortLived.generateToken(1L, "knguyen", "ADMIN");

        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        assertThrows(Exception.class, () -> shortLived.validateToken(token));

        System.out.println("PASSED TEST 6");
    }

    @Test
    @Order(7)
    @DisplayName("TEST 7: Token signed with different secret is rejected")
    void testWrongSecretRejected() {
        System.out.println("\n=== TEST 7: Wrong Secret Rejected ===");

        JwtService other = new JwtService("completely-different-secret-that-is-also-32-bytes-long!", ONE_HOUR_MS);
        String otherToken = other.generateToken(1L, "evil", "ADMIN");

        assertThrows(Exception.class, () -> jwtService.validateToken(otherToken));

        System.out.println("PASSED TEST 7");
    }
}
