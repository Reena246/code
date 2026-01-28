package com.accesscontrol;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Test client for encrypted endpoints
 * Usage: Run this after starting the application
 */
public class TestClient {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String ENCRYPTION_KEY = "0123456789abcdef0123456789abcdef"; // Must match application.properties
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static void main(String[] args) throws Exception {
        System.out.println("=== Access Control System Test Client ===\n");

        // Test 1: Ping
        testPing();
        
        // Test 2: Validate Access
        testValidateAccess();
        
        // Test 3: DB Sync
        testDbSync();
        
        System.out.println("\n=== All Tests Completed ===");
    }

    private static void testPing() throws Exception {
        System.out.println("1. Testing Ping Endpoint...");
        
        String requestJson = "{\"controllerMac\":\"AA:BB:CC:DD:EE:FF\"}";
        
        String response = sendEncryptedRequest(
            BASE_URL + "/controller/ping",
            requestJson
        );
        
        System.out.println("   Response: " + response);
        System.out.println("   ✓ Ping successful\n");
    }

    private static void testValidateAccess() throws Exception {
        System.out.println("2. Testing Validate Access Endpoint...");
        
        String requestJson = "{"
            + "\"controllerMac\":\"AA:BB:CC:DD:EE:FF\","
            + "\"readerUuid\":\"reader-uuid-123\","
            + "\"cardUid\":\"A1B2C3D4\","
            + "\"timestamp\":\"2026-01-27T10:30:00\""
            + "}";
        
        String response = sendEncryptedRequest(
            BASE_URL + "/access/validate",
            requestJson
        );
        
        System.out.println("   Response: " + response);
        System.out.println("   ✓ Access validation successful\n");
    }

    private static void testDbSync() throws Exception {
        System.out.println("3. Testing DB Sync Endpoint...");
        
        String requestJson = "{\"controllerMac\":\"AA:BB:CC:DD:EE:FF\"}";
        
        String response = sendEncryptedRequest(
            BASE_URL + "/controller/db-sync",
            requestJson
        );
        
        System.out.println("   Response: " + response);
        System.out.println("   ✓ DB Sync successful\n");
    }

    private static String sendEncryptedRequest(String url, String requestJson) throws Exception {
        // Generate random IV
        byte[] ivBytes = new byte[16];
        new SecureRandom().nextBytes(ivBytes);
        String ivBase64 = Base64.encodeBase64String(ivBytes);
        
        // Encrypt request
        byte[] encryptedRequest = encrypt(requestJson, ivBytes);
        
        // Send HTTP request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/octet-stream")
            .header("X-IV", ivBase64)
            .POST(HttpRequest.BodyPublishers.ofByteArray(encryptedRequest))
            .build();
        
        HttpResponse<byte[]> response = httpClient.send(
            request, 
            HttpResponse.BodyHandlers.ofByteArray()
        );
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode());
        }
        
        // Decrypt response (uses same IV)
        byte[] encryptedResponse = response.body();
        String decryptedResponse = decrypt(encryptedResponse, ivBytes);
        
        return decryptedResponse;
    }

    private static byte[] encrypt(String data, byte[] ivBytes) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(
            ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), 
            "AES"
        );
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        
        return cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String decrypt(byte[] encryptedData, byte[] ivBytes) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(
            ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), 
            "AES"
        );
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        
        byte[] decrypted = cipher.doFinal(encryptedData);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
