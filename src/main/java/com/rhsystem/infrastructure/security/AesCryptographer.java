package com.rhsystem.infrastructure.security;

import com.rhsystem.domain.model.security.ValueDecoder;
import com.rhsystem.domain.model.security.ValueEncoder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class AesCryptographer implements ValueEncoder, ValueDecoder {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private SecretKeySpec keySpec;


    @Value("${rh-system.aes.key}")
    private String key;

    @PostConstruct
    void construct() {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalStateException("AES key must be 128, 192 or 256 bits");
        }
        keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    @Override
    public String decode(String value) {
        String[] parts = value.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid value format");
        }
        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] encrypted = Base64.getDecoder().decode(parts[1]);
        var cipher = createCipher(Cipher.DECRYPT_MODE, iv);
        return new String(execute(cipher, encrypted), StandardCharsets.UTF_8);
    }

    @Override
    public String encode(String value) {
        byte[] iv = generateIv();
        var cipher = createCipher(Cipher.ENCRYPT_MODE, iv);
        byte[] encrypted = execute(cipher, value.getBytes(StandardCharsets.UTF_8));
        String ivBase64 = Base64.getEncoder().encodeToString(iv);
        String encryptedBase64 = Base64.getEncoder().encodeToString(encrypted);
        return ivBase64 + ":" + encryptedBase64;
    }

    private byte[] execute(Cipher cipher, byte[] value) {
        try {
            return cipher.doFinal(value);
        } catch (Exception e) {
            log.error("Error executing cipher", e);
            throw new RuntimeException("Error executing cipher", e);
        }
    }

    private Cipher createCipher(int mode, byte[] iv) {
        try {
            if (iv == null || iv.length != IV_SIZE_BYTES) {
                throw new IllegalArgumentException("Invalid IV size. Expected 12 bytes");
            }
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);

            cipher.init(mode, keySpec, parameterSpec);
            return cipher;
        } catch (Exception e) {
            log.error("Error creating cipher", e);
            throw new RuntimeException("Error creating cipher", e);
        }
    }


    private static byte[] generateIv() {
        byte[] iv = new byte[IV_SIZE_BYTES];
        SECURE_RANDOM.nextBytes(iv);
        return iv;
    }

}
