package com.clinicore.project.service;

import org.junit.jupiter.api.*;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        // 32 bytes base64 for AES-256
        byte[] rawKey = "0123456789abcdef0123456789abcdef".getBytes();
        String base64Key = Base64.getEncoder().encodeToString(rawKey);
        encryptionService = new EncryptionService(base64Key);
    }

    @Test
    @Order(1)
    @DisplayName("TEST 1: Encrypt then decrypt returns original plaintext")
    void testRoundTrip() {
        System.out.println("\n=== TEST 1: Round Trip ===");

        String plaintext = "Hello, this is a confidential medical note";
        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);
        assertEquals(plaintext, decrypted);

        System.out.println("PASSED TEST 1");
    }

    @Test
    @Order(2)
    @DisplayName("TEST 2: Same plaintext produces different ciphertext (random IV)")
    void testRandomIv() {
        System.out.println("\n=== TEST 2: Random IV ===");

        String plaintext = "same input every time";
        String a = encryptionService.encrypt(plaintext);
        String b = encryptionService.encrypt(plaintext);

        assertNotEquals(a, b);
        assertEquals(plaintext, encryptionService.decrypt(a));
        assertEquals(plaintext, encryptionService.decrypt(b));

        System.out.println("PASSED TEST 2");
    }

    @Test
    @Order(3)
    @DisplayName("TEST 3: Empty string round-trips")
    void testEmptyString() {
        System.out.println("\n=== TEST 3: Empty String ===");

        String encrypted = encryptionService.encrypt("");
        assertNotNull(encrypted);
        assertEquals("", encryptionService.decrypt(encrypted));

        System.out.println("PASSED TEST 3");
    }

    @Test
    @Order(4)
    @DisplayName("TEST 4: Unicode characters round-trip")
    void testUnicode() {
        System.out.println("\n=== TEST 4: Unicode ===");

        String plaintext = "Hello 你好 🏥 résumé — mixed scripts & emoji";
        assertEquals(plaintext, encryptionService.decrypt(encryptionService.encrypt(plaintext)));

        System.out.println("PASSED TEST 4");
    }

    @Test
    @Order(5)
    @DisplayName("TEST 5: Long plaintext round-trips")
    void testLongPlaintext() {
        System.out.println("\n=== TEST 5: Long Plaintext ===");

        String plaintext = "The quick brown fox jumps over the lazy dog. ".repeat(1000);
        assertEquals(plaintext, encryptionService.decrypt(encryptionService.encrypt(plaintext)));

        System.out.println("PASSED TEST 5");
    }

    @Test
    @Order(6)
    @DisplayName("TEST 6: Tampered ciphertext fails to decrypt (GCM auth tag)")
    void testTamperedCiphertextRejected() {
        System.out.println("\n=== TEST 6: Tampered Ciphertext Rejected ===");

        String encrypted = encryptionService.encrypt("secret");
        // flip one char, GCM should catch it
        String tampered = encrypted.substring(0, encrypted.length() - 2)
                + (encrypted.charAt(encrypted.length() - 2) == 'A' ? 'B' : 'A')
                + encrypted.charAt(encrypted.length() - 1);

        assertThrows(RuntimeException.class, () -> encryptionService.decrypt(tampered));

        System.out.println("PASSED TEST 6");
    }

    @Test
    @Order(7)
    @DisplayName("TEST 7: Constructor rejects invalid-length key")
    void testInvalidKeyLengthRejected() {
        System.out.println("\n=== TEST 7: Invalid Key Length Rejected ===");

        // 16 bytes isn't enough, needs 32
        byte[] tooShort = "0123456789abcdef".getBytes();
        String base64Short = Base64.getEncoder().encodeToString(tooShort);

        assertThrows(IllegalArgumentException.class, () -> new EncryptionService(base64Short));

        System.out.println("PASSED TEST 7");
    }
}
