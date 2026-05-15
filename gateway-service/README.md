# gateway-service

## Module purpose
Reactive edge gateway for platform ingress. Provides service-discovery routing, request context propagation, API key authentication, JWT resource-server support, Redis-backed rate limiting, actuator health probes, Prometheus metrics, and OpenTelemetry tracing.

## Folder structure
```text
gateway-service/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── src/main/
    ├── java/com/enterprise/apim/gateway/
    │   ├── GatewayServiceApplication.java
    │   ├── config/
    │   │   ├── RateLimitingConfig.java
    │   │   └── SecurityConfig.java
    │   ├── filter/
    │   │   ├── ApiKeyPrincipalHeaderFilter.java
    │   │   └── RequestContextFilter.java
    │   └── security/
    │       ├── ApiKeyAuthenticator.java
    │       └── ApiKeyProperties.java
    └── resources/application.yml
```

## Maven dependencies
- `spring-cloud-starter-gateway`
- `spring-cloud-starter-netflix-eureka-client`
- `spring-boot-starter-data-redis-reactive`
- `spring-boot-starter-oauth2-resource-server`
- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus`
- `micrometer-tracing-bridge-otel`
- `opentelemetry-exporter-otlp`

## Configuration
See `src/main/resources/application.yml`.

## Docker
Builds a Java 21 runtime image from the packaged Spring Boot jar.

## docker-compose snippet
See `docker-compose.yml`; includes Redis for `RequestRateLimiter`.

## Database schema
Not required.

## Core Java classes
- `GatewayServiceApplication`
- `SecurityConfig`
- `RateLimitingConfig`
- `ApiKeyAuthenticator`
- `ApiKeyProperties`
- `ApiKeyPrincipalHeaderFilter`
- `RequestContextFilter`

## Security configuration
- `/actuator/health/**` is public for probes.
- `/actuator/**` requires `OPS` role.
- All proxied API traffic requires JWT bearer token or API key.
- API keys are configured as SHA-256 hashes under `gateway.security.api-key.clients`.
- API key principals are forwarded downstream as `X-Client-Id` and `X-Tenant-Id`.

## Example request/response
Default local API key plaintext is `local-api-key`.

```bash
curl -H 'X-API-Key: local-api-key' \
     -H 'X-Request-Id: demo-request-1' \
     http://localhost:8080/api/config-service/actuator/health
```

```json
{
  "status": "UP"
}
```

## Run instructions
```bash
mvn -pl gateway-service spring-boot:run
```

```bash
mvn -pl gateway-service package
docker network create apim-platform || true
docker compose -f gateway-service/docker-compose.yml up --build
```
