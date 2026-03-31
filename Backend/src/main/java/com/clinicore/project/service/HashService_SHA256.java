package com.clinicore.project.service;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;

@Service
public class HashService_SHA256 {

    public String hashBytes(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                String s = Integer.toHexString(0xff & b);
                if (s.length() == 1) hex.append('0');
                hex.append(s);
            }
            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error hashing file", e);
        }
    }
}
