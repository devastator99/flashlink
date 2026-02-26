# demoflashlink-url-service

This repository contains the **Flashlink URL shortener service**. It is a full-stack application
with a Spring Boot backend (Java 17), a React/Vite frontend written in TypeScript, and
a Docker Compose development stack including MySQL, Redis, Kafka, Prometheus and Grafana.

---

## 🧱 Project Structure

```
├── Dockerfile                       # Backend multi-stage build
├── docker-compose.yml               # Development stack
├── build.gradle.kts                 # Gradle build script
├── settings.gradle.kts
├── src/                             # Backend source code
│   ├── main/java/com/flashlink/...  # Controllers, services, repositories, config
│   ├── main/resources/              # application.yml, Flyway migrations
│   └── test/java/...                # Unit/integration tests
├── frontend/                        # React frontend (Vite/TypeScript)
│   ├── src/                         # components, styles
│   ├── package.json
│   ├── Dockerfile                   # Frontend multi-stage build
│   └── nginx.conf
├── k8s/                             # Kubernetes manifests (deployment.yml)
├── gradle/                          # Gradle wrapper
├── HELP.md                          # Misc helpers
└── DETAILS.md                       # This file
```

---

## ⚙️ Backend (Spring Boot)

### Technologies
- Java 17
- Spring Boot 4.1 (M1), Spring 7
- Spring Data JPA, Hibernate with H2 (dev) / MySQL (prod)
- Spring Data Redis
- Spring Kafka
- Resilience4j rate limiter
- Micrometer (Prometheus metrics)
- Lombok

### Key Packages
- `controller` – REST APIs and redirect controller
- `service` – business logic (URL shortening, rate limiting, analytics)
- `repository` – JPA repositories
- `config` – Spring configuration classes (DB, metrics, etc.)
- `model` – DTOs and JPA entities

### Configuration
All settings live in `src/main/resources/application.yml` with profile-specific overrides.
Most values are injected via `@Value` or Resilience4j auto-configuration.

### Rate Limiting
`RateLimitService` builds a local rate limiter and also a Redis-backed token bucket script.
The service uses constructor injection to ensure configuration values are available during
initialization.

### Auditing
JPA auditing is enabled via `DatabaseConfig` with an `auditorProvider` bean that returns
"system" by default.

### Metrics
`MetricsConfig` defines `Timer` beans and a Redis template for metrics.
Prometheus config (in `monitoring/prometheus.yml`) scrapes the backend's actuator endpoint.

### Database Migrations
Flyway migrations are under `src/main/resources/db/migration`.

### Running
#### Local (Gradle)
```bash
./gradlew bootRun        # starts with embedded H2; see application-dev.yml
./gradlew test           # run unit/integ tests
```

#### Docker Compose
```bash
docker-compose up -d     # builds images and starts services
# backend listens on localhost:8080
```

Environment variables configure datasources, Redis, Kafka, etc. See `docker-compose.yml`.

### Error Handling
- Rate limiting exceptions default to allow requests (fail-open).
- Missing beans or configuration problems surface as startup exceptions –
  fix by adjusting constructors or config.

---

## 🧩 Frontend (React + Vite)

### Technologies
- React 18
- Vite 4
- TypeScript
- Tailwind CSS
- React Query for data fetching
- Sonner for toast notifications

### Components
- `UrlShortener` – form to create short links
- `UrlManagement` – user interface for managing existing links
- `AnalyticsDashboard` – display event counts (not shown above)
- `Navigation` – top nav bar

### Building
```bash
cd frontend
npm ci              # install dependencies
npm run build       # produces `dist` folder
```

### Dockerization
The Dockerfile builds with all dev dependencies and copies the resulting `dist` to an
`nginx:alpine` image. A `.dockerignore` file avoids copying host `node_modules`.

In Compose the frontend is available on port 80, proxying API requests to the backend.

---

## 🐳 Docker Compose Development Stack
Services defined:
- `mysql` 8.0 (URL database)
- `redis` 7-alpine (rate limiting/cache)
- `zookeeper` + `kafka` 7.6.1 (analytics events)
- `backend` (built from root Dockerfile)
- `frontend` (built from `frontend/Dockerfile`)
- `prometheus`, `grafana` (monitoring)

**Kafka notes:** recent Confluent images require `KAFKA_PROCESS_ROLES` and `KAFKA_LISTENERS`.

Commands:
```bash
docker-compose up -d                            # start all
docker-compose up -d backend frontend           # restart app only
docker-compose logs -f backend                  # view logs
```

---

## 🧪 Testing
- Backend uses JUnit 5 and Spring Boot test support; Kafka tests use `spring-kafka-test`.
- Run with `./gradlew test`.
- No explicit frontend tests yet.

---

## 🛠 Troubleshooting
- **Startup errors**: check stacktrace; often missing constructor args or mis‑configured `@Value`.
- **Docker build failures**: adjust Dockerfile, ensure `node_modules` excluded, install dev deps.
- **Kafka container exit code 1**: add `KAFKA_PROCESS_ROLES` as shown above.
- **Missing Prometheus config**: ensure `monitoring/prometheus.yml` exists and is mounted.

---

## ☁️ Deployment
- For k8s, manifests are located under `k8s/deployment.yml` (basic example).
- Build images via Gradle or CI pipelines and push to registry.

---

## 📚 Additional Notes
- The project uses Lombok; IDE support requires annotation processing.
- The backend exposes standard Actuator endpoints (health, metrics) on `/actuator`.
- Configuration is profile-driven (`dev`, `prod`).
- Rate limiter configuration supports per-instance overrides via `resilience4j.ratelimiter` in YAML.

---

Please refer to this document whenever you need an overview or to onboard new developers. Feel free to expand with diagrams or usage examples as the project evolves.