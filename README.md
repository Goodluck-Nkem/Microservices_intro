# Microservices Demo Project

A demonstration project showcasing a microservices architecture with Spring Cloud components.

## Architecture Overview

```
                    +------------------+
                    |   API Gateway    |
                    |    (Port 8080)    |
                    +--------+---------+
                             |
          +------------------+------------------+
          |                  |                  |
    +-----v-----+      +-----v-----+      +-----v-----+
    | User      |      | Post      |      | Comment   |
    | Service   |      | Service   |      | Service   |
    | (8081)    |      | (8082)    |      | (8083)    |
    +-----------+      +-----------+      +-----------+
          |                  |                  |
          +------------------+------------------+
                             |
                    +--------v---------+
                    |    MongoDB       |
                    +------------------+
                             |
                    +--------v---------+
                    |     Eureka      |
                    |    (8761)       |
                    +------------------+
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Routes requests to microservices |
| Eureka Server | 8761 | Service discovery |
| User Service | 8081 | User management (CRUD) |
| Post Service | 8082 | Post management with Kafka |
| Comment Service | 8083 | Comments with Circuit Breaker |

## Features

- **Service Discovery**: Netflix Eureka for service registration and discovery
- **API Gateway**: Spring Cloud Gateway for routing and load balancing
- **Inter-Service Communication**: OpenFeign for declarative REST clients
- **Database**: MongoDB for data persistence
- **Circuit Breaker**: Resilience4j for fault tolerance
- **Messaging**: Apache Kafka for event-driven communication
- **Monitoring**: Prometheus metrics, Zipkin tracing, Grafana dashboards

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for Kafka)
- MongoDB running on localhost:27017
- Prometheus running on localhost:9090
- Zipkin running on localhost:9411
- Grafana running on localhost:3000

## Infrastructure Setup

### Kafka Setup

Since you have Kafka downloaded, start it using:

**Windows:**
```batch
cd kafka_<version>
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
bin\windows\kafka-server-start.bat config\server.properties
```

**macOS/Linux:**
```bash
./kafka_2.13-<version>/bin/zookeeper-server-start.sh ./kafka_2.13-<version>/config/zookeeper.properties
./kafka_2.13-<version>/bin/kafka-server-start.sh ./kafka_2.13-<version>/config/server.properties
```

Or use Docker Compose:
```bash
docker-compose up -d
```

### Local Services

Ensure these are running:
- MongoDB (localhost:27017)
- Prometheus (localhost:9090)
- Zipkin (localhost:9411)
- Grafana (localhost:3000)

## Build & Run

### Build All Services

```bash
cd microservices-demo
mvn clean package -DskipTests
```

### Run Services (in order)

1. **Start Eureka Server** (must start first):
```bash
cd eureka-server
mvn spring-boot:run
```

2. **Start API Gateway** (or start after other services):
```bash
cd api-gateway
mvn spring-boot:run
```

3. **Start User Service**:
```bash
cd user-service
mvn spring-boot:run
```

4. **Start Post Service**:
```bash
cd post-service
mvn spring-boot:run
```

5. **Start Comment Service**:
```bash
cd comment-service
mvn spring-boot:run
```

## API Endpoints

All endpoints are accessible through the API Gateway at `http://localhost:8080`

### User Service (Port 8081)

```bash
# Create user
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name": "John Doe", "email": "john@example.com"}'

# Get all users
curl http://localhost:8080/users

# Get user by ID
curl http://localhost:8080/users/{userId}
```

### Post Service (Port 8082)

```bash
# Create post
curl -X POST http://localhost:8080/posts \
  -H "Content-Type: application/json" \
  -d '{"userId": "{userId}", "title": "My First Post", "content": "Hello World!"}'

# Get all posts
curl http://localhost:8080/posts

# Get post by ID
curl http://localhost:8080/posts/{postId}

# Get post with user info (OpenFeign call)
curl http://localhost:8080/posts/{postId}/with-user
```

### Comment Service (Port 8083)

```bash
# Create comment
curl -X POST http://localhost:8080/comments \
  -H "Content-Type: application/json" \
  -d '{"postId": "{postId}", "userId": "{userId}", "content": "Great post!"}'

# Get all comments
curl http://localhost:8080/comments

# Get comments for a post
curl http://localhost:8080/comments/post/{postId}

# Test circuit breaker
curl http://localhost:8080/comments/test-circuit-breaker/user/{userId}
```

## Testing Circuit Breaker

The Comment Service has test endpoints to trigger circuit breaker:

```bash
# This will open the circuit after 3 failed calls
curl http://localhost:8080/comments/test-circuit-breaker/user/123
curl http://localhost:8080/comments/test-circuit-breaker/user/123
curl http://localhost:8080/comments/test-circuit-breaker/user/123

# After failure threshold, circuit opens - returns fallback data
curl http://localhost:8080/comments/test-circuit-breaker/user/123
```

## Monitoring

### Prometheus Metrics

- Eureka: http://localhost:8761/actuator/prometheus
- API Gateway: http://localhost:8080/actuator/prometheus
- User Service: http://localhost:8081/actuator/prometheus
- Post Service: http://localhost:8082/actuator/prometheus
- Comment Service: http://localhost:8083/actuator/prometheus

### Zipkin Tracing

Access traces at: http://localhost:9411

### Grafana Dashboards

Access Grafana at: http://localhost:3000 (admin/admin)

Import Prometheus metrics from the actuator endpoints.

### Health Checks

```bash
curl http://localhost:8761/actuator/health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

## Kafka Topics

Topics are created automatically:
- `post-events` - Published by Post Service
- `comment-events` - Published by Comment Service

## Project Structure

```
microservices-demo/
├── pom.xml                    # Parent POM
├── docker-compose.yml         # Kafka docker setup
├── setup-kafka.bat/sh         # Kafka setup scripts
├── common/                    # Shared code
├── eureka-server/            # Service Registry
├── api-gateway/              # API Gateway
├── user-service/             # User microservice
├── post-service/             # Post microservice
└── comment-service/          # Comment microservice
```

## Tech Stack

- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- Spring Data MongoDB
- Spring Cloud OpenFeign
- Spring Cloud Gateway
- Resilience4j (Circuit Breaker)
- Apache Kafka
- Netflix Eureka
- Micrometer + Prometheus
- Zipkin Distributed Tracing
