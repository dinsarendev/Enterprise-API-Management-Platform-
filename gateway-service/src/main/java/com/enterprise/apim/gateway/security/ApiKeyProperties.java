package com.enterprise.apim.gateway.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.security.api-key")
public record ApiKeyProperties(
        boolean enabled,
        String headerName,
        List<Client> clients
) {
    public ApiKeyProperties {
        headerName = headerName == null || headerName.isBlank() ? "X-API-Key" : headerName;
        clients = clients == null ? List.of() : List.copyOf(clients);
    }

    public record Client(String clientId, String tenantId, String sha256) {
    }
}
