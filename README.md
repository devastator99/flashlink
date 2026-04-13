# 🚀 Flashlink - Modern URL Shortening Service

<div align="center">

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat&logo=java&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0--M1-6DB33F?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?style=flat&logo=react&logoColor=white)](https://react.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-3.x-38B2AC?style=flat&logo=tailwind-css&logoColor=white)
[![H2 Database](https://img.shields.io/badge/H2-Database-005C7A?style=flat&logoColor=white)](https://www.h2database.com/)
[![License](https://img.shields.io/badge/License-MIT-FF6B6B?style=flat&logoColor=white)](https://opensource.org/licenses/MIT)
[![Gradle](https://img.shields.io/badge/Gradle-8.14-02303A?style=flat&logo=gradle&logoColor=white)](https://gradle.org/)

*A blazing fast, modern URL shortening service with a sleek React frontend*

</div>

---

## 📋 Overview

Flashlink is a production-ready URL shortening service built with Spring Boot and React. It transforms long, unwieldy URLs into concise, shareable short links using Snowflake ID generation and Base62 encoding. Perfect for developers seeking a lightweight, self-hostable solution for URL management.

---

## 🛠️ Technology Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 4.1.0-M1 | Application framework |
| Spring Data JPA | - | Database access |
| H2 Database | - | In-memory database |
| Lombok | - | Code generation |
| Jakarta Validation | - | Input validation |
| Snowflake ID Generator | - | Unique ID generation |
| Base62 Encoder | - | URL-safe encoding |

### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18 | UI framework |
| Tailwind CSS | 3.x | Styling |
| Font Awesome | 6.x | Icons |
| Babel Standalone | - | In-browser JSX |

### Build Tools

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 17+ | Runtime |
| Gradle | 8.14 | Build system |
| Node.js | 18+ | Frontend build |

---

## ✨ Features

| Feature | Status |
|---------|--------|
| URL Shortening | ✅ Complete |
| Modern UI with React & Tailwind | ✅ Complete |
| RESTful API Endpoint | ✅ Complete |
| Automatic Redirect Service | ✅ Complete |
| URL Format Validation | ✅ Complete |
| Recent URLs History | ✅ Complete |
| Copy to Clipboard | ✅ Complete |
| H2 Console Access | ✅ Complete |
| Docker Deployment Ready | ✅ Complete |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Flashlink Architecture                │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   ┌─────────┐     ┌─────────────┐     ┌──────────────┐     │
│   │ Client  │────▶│  Controller │────▶│   Service    │     │
│   │ (React) │     │   (REST)    │     │   (Logic)    │     │
│   └─────────┘     └─────────────┘     └──────┬───────┘     │
│         │                                    │              │
│         │                           ┌────────▼────────┐      │
│         │                           │    Repository   │      │
│         │                           │   (JPA/H2)      │      │
│         │                           └────────┬────────┘      │
│         │                                    │              │
│         ▼                           ┌────────▼────────┐      │
│   ┌─────────────────────────────────┴─────────────────┐     │
│   │                   H2 Database                    │     │
│   │               (url_mapping table)                │     │
│   └──────────────────────────────────────────────────┘     │
│                                                              │
└─────────────────────────────────────────────────────────────┘

URL Generation Flow:
┌──────────────┐    ┌─────────────┐    ┌──────────────┐
│  Snowflake   │───▶│   Base62    │───▶│  Collision  │
│  ID Gen (64b)│    │  Encoder    │    │   Check     │
└──────────────┘    └─────────────┘    └──────────────┘
                                                   │
                                                   ▼
                                           ┌──────────────┐
                                           │   Database   │
                                           │    Storage   │
                                           └──────────────┘
```

---

## 📂 Project Structure

```
flashlink/
├── src/
│   ├── main/
│   │   ├── java/com/flashlink/demoflashlink_url_service/
│   │   │   ├── controller/
│   │   │   │   ├── UrlController.java          # REST API endpoint
│   │   │   │   └── RedirectController.java     # URL redirection
│   │   │   ├── dto/
│   │   │   │   └── UrlRequest.java             # Request DTO
│   │   │   ├── model/
│   │   │   │   └── UrlMapping.java             # Entity class
│   │   │   ├── repository/
│   │   │   │   └── UrlMappingRepository.java   # Data access
│   │   │   ├── service/
│   │   │   │   └── UrlService.java             # Business logic
│   │   │   ├── util/
│   │   │   │   ├── Base62Encoder.java          # URL encoding
│   │   │   │   └── SnowflakeIdGenerator.java   # ID generation
│   │   │   └── FlashlinkUrlServiceApplication.java
│   │   ├── resources/
│   │   │   ├── static/
│   │   │   │   └── index.html                  # React frontend
│   │   │   └── application.properties          # Configuration
│   │   └── test/
│   │       └── java/                           # Test classes
├── Dockerfile                                   # Container config
├── build.gradle                                 # Gradle config
└── README.md                                    # This file
```

---

## 🏁 Getting Started

### Prerequisites

| Tool | Minimum Version | Check Command |
|------|-----------------|---------------|
| Java | 17+ | `java -version` |
| Gradle | 8.14 | `gradle --version` |
| Git | 2.0+ | `git --version` |

### Environment Setup

```bash
# Clone the repository
git clone https://github.com/devastator99/flashlink.git
cd flashlink
```

### Frontend Setup

The frontend is bundled directly in the Spring Boot application via `index.html`. No separate build process required - it serves automatically when the application runs.

### Backend Setup

```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun
```

### Configuration

Default `application.properties` configuration:

```properties
# Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Server Configuration
server.port=8080

# Flashlink Configuration
flashlink.node-id=1
```

---

## 🚦 Running the Application

### Option 1: Native Run

```bash
# Build and run
./gradlew build
./gradlew bootRun
```

### Option 2: Docker

```bash
# Build the Docker image
docker build -t flashlink .

# Run the container
docker run -p 8080:8080 flashlink
```

### Access Points

| Service | URL |
|---------|-----|
| Frontend | http://localhost:8080 |
| API Endpoint | http://localhost:8080/api/shorten |
| H2 Console | http://localhost:8080/h2-console |

---

## 📡 API Documentation

### Shorten URL

**Endpoint:** `POST /api/shorten`

```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"longUrl": "https://www.example.com/very/long/url"}'
```

**Request Body:**
```json
{
  "longUrl": "https://www.example.com/very/long/url"
}
```

**Response:**
```json
{
  "shortCode": "UKGuOnG2pE",
  "createdAt": "2026-02-07T02:25:12.885033"
}
```

### Redirect

**Endpoint:** `GET /{shortCode}`

Redirects to the original URL with HTTP 302.

```bash
# Using the shortened URL
curl -v http://localhost:8080/UKGuOnG2pE
```

---

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests UrlServiceTest

# Run with coverage
./gradlew test jacocoTestReport
```

---

## 🔐 URL Generation Algorithm

```
┌─────────────────────────────────────────────────────────────┐
│                    ID Generation Process                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. SNOWFLAKE ID GENERATION                                  │
│     - 64-bit unique ID                                       │
│     - Timestamp (41 bits)                                   │
│     - Node ID (10 bits)                                     │
│     - Sequence (12 bits)                                    │
│                                                              │
│  2. BASE62 ENCODING                                          │
│     - Characters: 0-9, a-z, A-Z                             │
│     - URL-safe, no special characters                       │
│     - Output length: ~10 characters                         │
│                                                              │
│  3. COLLISION DETECTION                                      │
│     - Check if short code exists in database                │
│     - Regenerate if collision found                          │
│                                                              │
│  4. STORAGE                                                  │
│     - Save mapping to H2 database                           │
│     - Index short_code for fast lookup                      │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Database Schema

```sql
CREATE TABLE url_mapping (
    id BIGINT NOT NULL PRIMARY KEY,
    short_code VARCHAR(10) NOT NULL UNIQUE,
    long_url VARCHAR(2048) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL
);

CREATE INDEX idx_short_code ON url_mapping(short_code);
```

---

## 🔧 Production Considerations

> **Important:** Before deploying to production, consider the following:

- [ ] Replace H2 with a production database (MySQL, PostgreSQL)
- [ ] Configure proper logging (ELK stack, Splunk)
- [ ] Set up monitoring and metrics (Prometheus, Grafana)
- [ ] Implement rate limiting to prevent abuse
- [ ] Add authentication/authorization
- [ ] Enable HTTPS/SSL
- [ ] Configure environment-specific properties
- [ ] Set up automated backups
- [ ] Implement caching layer (Redis)

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| Port 8080 in use | Change port in `application.properties`: `server.port=8081` |
| H2 Console not loading | Enable: `spring.h2.console.enabled=true` |
| Build fails | Run `./gradlew clean build` |
| Java version error | Ensure Java 17+: `java -version` |
| CORS errors | Configure in `application.properties` |

---

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Branch Naming
- `feature/description` - New features
- `bugfix/description` - Bug fixes
- `hotfix/description` - Urgent fixes
- `docs/description` - Documentation updates

### Commit Conventions
```
<type>(<scope>): <description>

Types: feat, fix, docs, style, refactor, test, chore
Example: feat(api): add rate limiting support
```

### Pull Request Template
```markdown
## Description
[Description of changes]

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change

## Testing
[How to test this change]

## Checklist
- [ ] Code follows style guidelines
- [ ] Tests pass
- [ ] Documentation updated
```

---

## 📜 Code of Conduct

By participating in this project, you agree to abide by the terms of our Code of Conduct. We are committed to providing a welcoming and inclusive environment for everyone.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

---

## 🙌 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - The excellent Java framework
- [React](https://react.dev/) - The UI library
- [Tailwind CSS](https://tailwindcss.com/) - The utility-first CSS framework
- [H2 Database](https://www.h2database.com/) - The in-memory database
- All contributors and users of Flashlink

---

## 📬 Contact

- GitHub Issues: [Report a bug](https://github.com/devastator99/flashlink/issues)
- Discussions: [Ask questions](https://github.com/devastator99/flashlink/discussions)

---

<div align="center">

**Made with ❤️ by the Flashlink Team**

*Star us on [GitHub](https://github.com/devastator99/flashlink) if you find this useful!*

</div>