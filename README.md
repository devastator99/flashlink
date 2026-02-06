# Flashlink - URL Shortening Service

A modern, fast URL shortening service built with Spring Boot backend and React frontend.

## Features

- **URL Shortening**: Convert long URLs into short, shareable links
- **Modern UI**: Clean, responsive interface built with React and Tailwind CSS
- **API Endpoint**: RESTful API for programmatic access
- **Redirect Service**: Automatic redirection from short codes to original URLs
- **Validation**: Input validation for URL format
- **History**: Recent shortened URLs tracking
- **Copy to Clipboard**: Easy copying of shortened URLs

## Technology Stack

### Backend
- **Spring Boot 4.1.0-M1** - Java framework
- **Spring Data JPA** - Database access
- **H2 Database** - In-memory database for testing
- **Lombok** - Code generation
- **Jakarta Validation** - Input validation
- **Snowflake ID Generator** - Unique ID generation
- **Base62 Encoding** - URL-safe encoding

### Frontend
- **React 18** - UI framework
- **Tailwind CSS** - Utility-first CSS framework
- **Font Awesome** - Icons
- **Babel Standalone** - In-browser JSX transformation

## Quick Start

### Prerequisites
- Java 17 or higher
- Gradle 8.14 or higher

### Running the Application

1. **Clone the repository**
   ```bash
   git clone https://github.com/devastator99/flashlink.git
   cd flashlink
   ```

2. **Build the application**
   ```bash
   ./gradlew build
   ```

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

4. **Access the application**
   - Frontend: http://localhost:8080
   - API: http://localhost:8080/api/shorten

## API Documentation

### Shorten URL

**Endpoint:** `POST /api/shorten`

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

**Response:** Redirects to the original URL

## Usage Examples

### Using the Frontend

1. Open http://localhost:8080 in your browser
2. Enter a long URL in the input field
3. Click "Shorten" button
4. Copy the shortened URL or use it directly

### Using the API

```bash
# Shorten a URL
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"longUrl": "https://www.example.com"}'

# Use the shortened URL
curl http://localhost:8080/UKGuOnG2pE
```

## Project Structure

```
src/
├── main/
│   ├── java/com/flashlink/demoflashlink_url_service/
│   │   ├── controller/          # REST controllers
│   │   │   ├── UrlController.java      # API endpoint
│   │   │   └── RedirectController.java # URL redirection
│   │   ├── dto/                 # Data transfer objects
│   │   │   └── UrlRequest.java
│   │   ├── model/               # Entity classes
│   │   │   └── UrlMapping.java
│   │   ├── repository/          # Data access layer
│   │   │   └── UrlMappingRepository.java
│   │   ├── service/             # Business logic
│   │   │   └── UrlService.java
│   │   ├── util/                # Utility classes
│   │   │   ├── Base62Encoder.java
│   │   │   └── SnowflakeIdGenerator.java
│   │   └── FlashlinkUrlServiceApplication.java
│   └── resources/
│       ├── static/
│       │   └── index.html       # Frontend application
│       └── application.properties # Configuration
└── test/                         # Test classes
```

## Configuration

The application uses the following configuration in `application.properties`:

```properties
# Database configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Server configuration
server.port=8080

# Flashlink configuration
flashlink.node-id=1
```

## URL Generation Algorithm

1. **Snowflake ID**: Generate a unique 64-bit ID using timestamp, node ID, and sequence
2. **Base62 Encoding**: Convert the ID to a URL-safe string using Base62 encoding
3. **Collision Detection**: Ensure the short code doesn't already exist
4. **Storage**: Save the mapping in the database

## Testing

Run the tests using Gradle:

```bash
./gradlew test
```

## Development

### Adding New Features

1. **Backend**: Add new controllers, services, or repositories in the appropriate packages
2. **Frontend**: Modify `index.html` to add new React components
3. **Styling**: Use Tailwind CSS classes for consistent styling

### Database Schema

The `url_mapping` table has the following structure:

```sql
CREATE TABLE url_mapping (
    created_at TIMESTAMP(6) NOT NULL,
    id BIGINT NOT NULL PRIMARY KEY,
    short_code VARCHAR(10) NOT NULL UNIQUE,
    long_url VARCHAR(2048) NOT NULL
);
```

## Deployment

### Docker

A Dockerfile is included for containerized deployment:

```bash
# Build the image
docker build -t flashlink .

# Run the container
docker run -p 8080:8080 flashlink
```

### Production Considerations

- Replace H2 with a production database (MySQL, PostgreSQL)
- Configure proper logging
- Set up monitoring and metrics
- Implement rate limiting
- Add authentication/authorization if needed

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Acknowledgments

- Spring Boot team for the excellent framework
- React team for the UI library
- Tailwind CSS for the utility-first CSS framework
- All contributors and users of Flashlink
