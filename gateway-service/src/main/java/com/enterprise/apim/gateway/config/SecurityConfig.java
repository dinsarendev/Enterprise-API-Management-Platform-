package com.enterprise.apim.gateway.config;

import com.enterprise.apim.gateway.security.ApiKeyAuthenticator;
import com.enterprise.apim.gateway.security.ApiKeyProperties;
import java.util.List;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ApiKeyProperties properties,
            ApiKeyAuthenticator authenticator
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .authorizeExchange(auth -> auth
                        .matchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                        .pathMatchers("/actuator/prometheus", "/actuator/**").hasRole("OPS")
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .addFilterAt(apiKeyAuthenticationFilter(properties, authenticator), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private static AuthenticationWebFilter apiKeyAuthenticationFilter(
            ApiKeyProperties properties,
            ApiKeyAuthenticator authenticator
    ) {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(authentication -> Mono.just(authentication));
        filter.setServerAuthenticationConverter(apiKeyConverter(properties, authenticator));
        return filter;
    }

    private static ServerAuthenticationConverter apiKeyConverter(
            ApiKeyProperties properties,
            ApiKeyAuthenticator authenticator
    ) {
        return exchange -> {
            if (!properties.enabled() || exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return Mono.empty();
            }
            String apiKey = exchange.getRequest().getHeaders().getFirst(properties.headerName());
            return Mono.justOrEmpty(authenticator.authenticate(apiKey)
                    .map(principal -> {
                        AbstractAuthenticationToken token = UsernamePasswordAuthenticationToken.authenticated(
                                principal.clientId(),
                                "[PROTECTED]",
                                List.of(new SimpleGrantedAuthority("ROLE_GATEWAY_CLIENT")));
                        token.setDetails(principal);
                        return (Authentication) token;
                    }));
        };
    }
}
