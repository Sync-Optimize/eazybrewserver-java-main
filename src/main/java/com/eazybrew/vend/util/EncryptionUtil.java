package com.eazybrew.vend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


@Slf4j
@Component
public class EncryptionUtil {
    @Value("${encryption.secret.key}")
    private String secretKey;

    @Value("${encryption.init.vector}")
    private String initVector;

    private static final String ALGORITHM = "AES/CBC/PKCS5PADDING";

    /**
     * Encrypt a string using AES 256 encryption
     *
     * @param value the string to encrypt
     * @return the encrypted string (Base64 encoded)
     */
    public String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Error encrypting value", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt a string using AES 256 decryption
     *
     * @param encryptedValue the encrypted string (Base64 encoded)
     * @return the decrypted string
     */
    public String decrypt(String encryptedValue) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error decrypting value", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Generate a sample encrypted API key for a client
     *
     * @param apiKey the API key to encrypt
     * @return the encrypted API key
     */
    public String generateEncryptedApiKey(String apiKey) {
        return encrypt(apiKey);
    }
}
