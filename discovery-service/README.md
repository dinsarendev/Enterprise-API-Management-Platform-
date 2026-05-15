# discovery-service

## Module purpose
Service registry for platform microservices. Provides Eureka registration and discovery with HTTP Basic authentication, actuator health probes, Prometheus metrics, and OpenTelemetry trace export.

## Folder structure
```text
discovery-service/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── src/main/
    ├── java/com/enterprise/apim/discovery/
    │   ├── DiscoveryServiceApplication.java
    │   └── config/SecurityConfig.java
    └── resources/application.yml
```

## Maven dependencies
- `spring-cloud-starter-netflix-eureka-server`
- `spring-boot-starter-security`
- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus`
- `micrometer-tracing-bridge-otel`
- `opentelemetry-exporter-otlp`

## Configuration
See `src/main/resources/application.yml`.

## Docker
Builds a Java 21 runtime image from the packaged Spring Boot jar.

## docker-compose snippet
See `docker-compose.yml`.

## Database schema
Not required.

## Core Java classes
- `DiscoveryServiceApplication`
- `SecurityConfig`

## Security
- `/actuator/health/**` is public for probes.
- `/actuator/prometheus` requires `OPS` role.
- Eureka and dashboard endpoints require `DISCOVERY` role.
- CSRF is disabled only for `/eureka/**` registration APIs.

## Example request/response
```bash
curl -u discovery_admin:change-me http://localhost:8761/eureka/apps
```

```xml
<applications>
  <versions__delta>1</versions__delta>
  <apps__hashcode></apps__hashcode>
</applications>
```

## Run instructions
```bash
mvn -pl discovery-service spring-boot:run
```

```bash
mvn -pl discovery-service package
docker compose -f discovery-service/docker-compose.yml up --build
```
