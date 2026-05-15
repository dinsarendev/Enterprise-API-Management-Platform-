package com.enterprise.apim.gateway.filter;

import com.enterprise.apim.gateway.security.ApiKeyAuthenticator;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ApiKeyPrincipalHeaderFilter implements GlobalFilter, Ordered {

    public static final String CLIENT_ID_HEADER = "X-Client-Id";
    public static final String TENANT_ID_HEADER = "X-Tenant-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().getDetails())
                .filter(ApiKeyAuthenticator.AuthenticatedApiKey.class::isInstance)
                .cast(ApiKeyAuthenticator.AuthenticatedApiKey.class)
                .map(principal -> withPrincipalHeaders(exchange, principal))
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 30;
    }

    private static ServerWebExchange withPrincipalHeaders(
            ServerWebExchange exchange,
            ApiKeyAuthenticator.AuthenticatedApiKey principal
    ) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(CLIENT_ID_HEADER);
                    headers.remove(TENANT_ID_HEADER);
                    headers.add(CLIENT_ID_HEADER, principal.clientId());
                    headers.add(TENANT_ID_HEADER, principal.tenantId());
                })
                .build();
        return exchange.mutate().request(request).build();
    }
}
