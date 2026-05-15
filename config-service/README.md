# config-service

## Module purpose
Centralized Spring Cloud Config Server for platform microservices. Serves externalized configuration from a mounted native repository by default, supports Git-backed configuration with the `git` profile, and registers with discovery-service.

## Folder structure
```text
config-service/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── config-repo/
│   ├── application.yml
│   └── discovery-service/default/application.yml
└── src/main/
    ├── java/com/enterprise/apim/configserver/
    │   ├── ConfigServiceApplication.java
    │   └── config/SecurityConfig.java
    └── resources/application.yml
```

## Maven dependencies
- `spring-cloud-config-server`
- `spring-cloud-starter-netflix-eureka-client`
- `spring-boot-starter-security`
- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus`
- `micrometer-tracing-bridge-otel`
- `opentelemetry-exporter-otlp`

## Configuration
See `src/main/resources/application.yml`. Default profile is `native`; set `SPRING_PROFILES_ACTIVE=git` and `CONFIG_GIT_URI` for Git-backed production configuration.

## Docker
Builds a Java 21 runtime image from the packaged Spring Boot jar.

## docker-compose snippet
See `docker-compose.yml`.

## Database schema
Not required.

## Core Java classes
- `ConfigServiceApplication`
- `SecurityConfig`

## Security
- `/actuator/health/**` is public for probes.
- `/actuator/**` requires `OPS` role.
- Config endpoints require `CONFIG` role.
- Use `CONFIG_USERNAME` and `CONFIG_PASSWORD` for clients.

## Example request/response
```bash
curl -u config_admin:change-me http://localhost:8888/discovery-service/default
```

```json
{
  "name": "discovery-service",
  "profiles": ["default"],
  "label": null,
  "propertySources": [
    {
      "name": "file [/config-repo/application.yml]",
      "source": {
        "management.endpoints.web.exposure.include": "health,info,metrics,prometheus"
      }
    }
  ]
}
```

## Run instructions
```bash
mvn -pl config-service spring-boot:run
```

```bash
mvn -pl config-service package
docker network create apim-platform || true
docker compose -f config-service/docker-compose.yml up --build
```
