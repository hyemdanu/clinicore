package com.clinicore.project.service;

import org.junit.jupiter.api.*;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        // same encoder as prod
        PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        passwordService = new PasswordService(encoder);
    }

    @Test
    @Order(1)
    @DisplayName("TEST 1: Hash and verify works for matching password")
    void testHashAndVerify() {
        System.out.println("\n=== TEST 1: Hash & Verify ===");

        String password = "MySecretPass123!";
        String hash = passwordService.hashPassword(password);

        assertNotNull(hash);
        assertNotEquals(password, hash);
        assertTrue(hash.startsWith("$argon2"));
        assertTrue(passwordService.verifyPassword(password, hash));

        System.out.println("PASSED TEST 1");
    }

    @Test
    @Order(2)
    @DisplayName("TEST 2: Wrong password fails verification")
    void testWrongPasswordFails() {
        System.out.println("\n=== TEST 2: Wrong Password Fails ===");

        String hash = passwordService.hashPassword("correct-password");
        assertFalse(passwordService.verifyPassword("wrong-password", hash));
        assertFalse(passwordService.verifyPassword("", hash));

        System.out.println("PASSED TEST 2");
    }

    @Test
    @Order(3)
    @DisplayName("TEST 3: Same password produces different hashes (salt)")
    void testSaltedHashes() {
        System.out.println("\n=== TEST 3: Salted Hashes ===");

        String a = passwordService.hashPassword("test");
        String b = passwordService.hashPassword("test");

        assertNotEquals(a, b);
        assertTrue(passwordService.verifyPassword("test", a));
        assertTrue(passwordService.verifyPassword("test", b));

        System.out.println("PASSED TEST 3");
    }

    @Test
    @Order(4)
    @DisplayName("TEST 4: needsRehash returns false for a fresh hash")
    void testNeedsRehashFresh() {
        System.out.println("\n=== TEST 4: Fresh Hash Needs No Rehash ===");

        String hash = passwordService.hashPassword("password");
        assertFalse(passwordService.needsRehash(hash));

        System.out.println("PASSED TEST 4");
    }

    @Test
    @Order(5)
    @DisplayName("TEST 5: Null/empty password throws on hash")
    void testNullHashRejected() {
        System.out.println("\n=== TEST 5: Null/Empty Hash Rejected ===");

        assertThrows(IllegalArgumentException.class, () -> passwordService.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> passwordService.hashPassword(""));
        assertThrows(IllegalArgumentException.class, () -> passwordService.hashPassword("   "));

        System.out.println("PASSED TEST 5");
    }

    @Test
    @Order(6)
    @DisplayName("TEST 6: Null arguments to verify return false (no throw)")
    void testNullVerifyReturnsFalse() {
        System.out.println("\n=== TEST 6: Null Verify Returns False ===");

        assertFalse(passwordService.verifyPassword(null, "somehash"));
        assertFalse(passwordService.verifyPassword("somepass", null));
        assertFalse(passwordService.verifyPassword(null, null));

        System.out.println("PASSED TEST 6");
    }

    @Test
    @Order(7)
    @DisplayName("TEST 7: needsRehash returns true for a null hash")
    void testNeedsRehashNull() {
        System.out.println("\n=== TEST 7: Null Hash Needs Rehash ===");

        assertTrue(passwordService.needsRehash(null));

        System.out.println("PASSED TEST 7");
    }

    @Test
    @Order(8)
    @DisplayName("TEST 8: Corrupt hash format fails gracefully on verify")
    void testCorruptHashGraceful() {
        System.out.println("\n=== TEST 8: Corrupt Hash Graceful ===");

        assertFalse(passwordService.verifyPassword("anything", "not-a-real-argon2-hash"));

        System.out.println("PASSED TEST 8");
    }
}
