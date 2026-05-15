package com.enterprise.apim.gateway.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyAuthenticator {

    private final ApiKeyProperties properties;

    public ApiKeyAuthenticator(ApiKeyProperties properties) {
        this.properties = properties;
    }

    public Optional<AuthenticatedApiKey> authenticate(String presentedKey) {
        if (!properties.enabled() || presentedKey == null || presentedKey.isBlank()) {
            return Optional.empty();
        }

        byte[] presentedHash = sha256(presentedKey);
        return properties.clients().stream()
                .filter(client -> client.sha256() != null && constantTimeEquals(presentedHash, decodeHex(client.sha256())))
                .findFirst()
                .map(client -> new AuthenticatedApiKey(client.clientId(), client.tenantId()));
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private static byte[] decodeHex(String value) {
        try {
            return HexFormat.of().parseHex(value.trim());
        } catch (IllegalArgumentException ex) {
            return new byte[0];
        }
    }

    private static boolean constantTimeEquals(byte[] left, byte[] right) {
        return left.length == right.length && MessageDigest.isEqual(left, right);
    }

    public record AuthenticatedApiKey(String clientId, String tenantId) {
    }
}
