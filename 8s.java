package com.accesscontrol.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * AES-256/CBC/PKCS5Padding encryption utility
 * Handles encryption/decryption with random IV per request
 */
@Component
public class AesEncryptionUtil {

    private static final Logger logger = LoggerFactory.getLogger(AesEncryptionUtil.class);
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_SIZE = 16; // 128 bits for AES

    private final SecretKeySpec secretKey;
    private final ObjectMapper objectMapper;

    public AesEncryptionUtil(@Value("${encryption.key}") String encryptionKey, 
                             ObjectMapper objectMapper) {
        // Ensure key is 32 bytes for AES-256
        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("Encryption key must be 32 bytes for AES-256");
        }
        this.secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        this.objectMapper = objectMapper;
    }

    /**
     * Generate a random IV (Initialization Vector)
     * @return Base64-encoded IV string
     */
    public String generateIV() {
        byte[] iv = new byte[IV_SIZE];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return Base64.encodeBase64String(iv);
    }

    /**
     * Encrypt an object to byte array using the provided IV
     * @param object Object to encrypt
     * @param ivBase64 Base64-encoded IV
     * @return Encrypted byte array
     */
    public byte[] encrypt(Object object, String ivBase64) throws Exception {
        try {
            // Convert object to JSON string
            String jsonData = objectMapper.writeValueAsString(object);
            
            // Decode IV from Base64
            byte[] ivBytes = Base64.decodeBase64(ivBase64);
            if (ivBytes.length != IV_SIZE) {
                throw new IllegalArgumentException("Invalid IV size");
            }
            
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            
            // Encrypt
            byte[] encrypted = cipher.doFinal(jsonData.getBytes(StandardCharsets.UTF_8));
            
            logger.debug("Successfully encrypted data with IV: {}", maskIV(ivBase64));
            return encrypted;
            
        } catch (Exception e) {
            logger.error("Encryption failed: {}", e.getMessage());
            throw new Exception("Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt byte array to specified class using the provided IV
     * @param encryptedData Encrypted byte array
     * @param ivBase64 Base64-encoded IV
     * @param clazz Target class type
     * @return Decrypted object
     */
    public <T> T decrypt(byte[] encryptedData, String ivBase64, Class<T> clazz) throws Exception {
        try {
            // Decode IV from Base64
            byte[] ivBytes = Base64.decodeBase64(ivBase64);
            if (ivBytes.length != IV_SIZE) {
                throw new IllegalArgumentException("Invalid IV size");
            }
            
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            
            // Decrypt
            byte[] decrypted = cipher.doFinal(encryptedData);
            String jsonData = new String(decrypted, StandardCharsets.UTF_8);
            
            // Convert JSON to object
            T result = objectMapper.readValue(jsonData, clazz);
            
            logger.debug("Successfully decrypted data with IV: {}", maskIV(ivBase64));
            return result;
            
        } catch (Exception e) {
            logger.error("Decryption failed: {}", e.getMessage());
            throw new Exception("Decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validate IV format and size
     * @param ivBase64 Base64-encoded IV
     * @return true if valid
     */
    public boolean isValidIV(String ivBase64) {
        if (ivBase64 == null || ivBase64.isEmpty()) {
            return false;
        }
        try {
            byte[] ivBytes = Base64.decodeBase64(ivBase64);
            return ivBytes.length == IV_SIZE;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Mask IV for logging (show only first 4 characters)
     * @param iv IV string
     * @return Masked IV
     */
    private String maskIV(String iv) {
        if (iv == null || iv.length() < 4) {
            return "****";
        }
        return iv.substring(0, 4) + "****";
    }

    /**
     * Mask card UID for logging (show only last 4 characters)
     * @param cardUid Card UID string
     * @return Masked card UID
     */
    public static String maskCardUid(String cardUid) {
        if (cardUid == null || cardUid.length() < 4) {
            return "****";
        }
        int length = cardUid.length();
        return "****" + cardUid.substring(length - 4);
    }
}
