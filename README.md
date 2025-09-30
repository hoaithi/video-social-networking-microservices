# Vidsonet Microservices

## Overview
This project is a microservices-based system for video sharing and social networking, built with Java (Spring Boot), Docker, and supporting technologies. Each service is independently deployable and communicates via REST and Kafka. The AI service provides video content analysis and recommends titles for uploaded videos.

## Services
- **api-gateway**: Central entry point for client requests, routing to backend services.
- **identity-service**: Handles authentication, authorization, and user management.
- **notification-service**: Manages notifications (email, OTP, etc.).
- **profile-service**: Manages user profiles and related data.
- **post-service**: Handles posts and related operations.
- **comment-service**: Manages comments on posts and videos.
- **video-service**: Handles video uploads, streaming, and metadata.
- **file-service**: Manages file storage and retrieval.
- **ai-service**: Reads the content of uploaded videos and recommends suitable titles using AI-powered analysis.

## Technologies
- Java 21 (Spring Boot)
- Maven
- Docker & Docker Compose
- Kafka (messaging)
- MySQL, PostgreSQL, MongoDB, Redis (databases/cache)

## Prerequisites
- Docker & Docker Compose
- JDK 21
- Maven (for local builds)

## Building and Running

### 1. Build Docker Images
Each service has its own `Dockerfile`. To build all images:
```sh
# In the project root
docker-compose build
```

### 2. Start All Services
```sh
docker-compose up
```

### 3. Access Services
- **API Gateway**: http://localhost:8888
- **Identity Service**: http://localhost:8080
- **Profile Service**: http://localhost:8081
- **Notification Service**: http://localhost:8082
- **Post Service**: http://localhost:8083
- **AI Service**: http://localhost:8084
- **Video Service**: http://localhost:8085
- **Comment Service**: http://localhost:8086
- **File Service**: http://localhost:8088

### 4. Spring Documents

  ```sh
  http://localhost:{server-port}/swagger-ui/index.html
  ```


Other services use similar port mappings (see `docker-compose.yml`).

## Database Credentials
- MySQL: `root` / `123456`
- PostgreSQL: `postgres` / `123456`
- MongoDB: `root` / `root`

## Kafka
Kafka is used for asynchronous communication (notifications, events). Default port: `9094`.

## Development Notes
- Each service is in its own folder with a `Dockerfile` and `pom.xml`.
- Environment variables and ports can be configured in `docker-compose.yml`.
- For local development, you can run services individually using Maven:
  ```sh
  ./mvnw spring-boot:run
  ```

## License
MIT

## Contact
For questions or contributions, please open an issue or pull request.
