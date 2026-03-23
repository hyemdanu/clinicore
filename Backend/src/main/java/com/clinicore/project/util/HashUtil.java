package com.clinicore.project.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * HashUtil
 * Provides SHA-256 hashing for medical data integrity verification.
 * Used by service layer to hash sensitive fields before saving,
 * and to verify data hasn't been tampered with when reading.
 */
public class HashUtil {

    private HashUtil() {
        // utility class — no instantiation
    }

    /**
     * Hashes a string using SHA-256.
     * Returns a 64-character lowercase hex string.
     * Null input is treated as an empty string so hashes stay consistent.
     */
    public static String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(
                    (data == null ? "" : data).getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hex = new StringBuilder(64);
            for (byte b : hashBytes) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the Java spec — this will never happen
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Returns true if the hash of the given data matches the stored hash.
     * Use this in service read methods to detect tampering.
     */
    public static boolean verify(String data, String storedHash) {
        if (storedHash == null) return false;
        return sha256(data).equals(storedHash);
    }
}
