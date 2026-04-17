# Microservices Demo Project

A demonstration project showcasing a microservices architecture with Spring Cloud components.

## Architecture Overview

```
                    +------------------+
                    |   API Gateway    |
                    |    (Port 8080)    |
                    +--------+---------+
                             |
      +----------------------+----------------------+
      |                      |                      |
+----v----+           +------v------+          +-----v-----+
| User    |           | Post        |          | Comment   |
| Service |           | Service     |          | Service   |
| (8081)  |           | (8082)     |          | (8083)   |
+---------+           +------+------+          +-----------+
      |                      |                      |
      +----------+-----------+----------------------+
                 |           |                      |
           +-----v-----------v------+      +--------v---------+
           |    Kafka Topics      |      |   Notification    |
           |  post-events         |<-----|   Service         |
           |  comment-events     |      |   (8084)           |
           +---------------------+      +-------------------+
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
| Notification Service | 8084 | Kafka consumer for events |

## Features

- **Service Discovery**: Netflix Eureka for service registration and discovery
- **API Gateway**: Spring Cloud Gateway for routing and load balancing
- **Inter-Service Communication**: OpenFeign for declarative REST clients
- **Database**: MongoDB for data persistence
- **Circuit Breaker**: Resilience4j for fault tolerance
- **Messaging**: Apache Kafka for event-driven communication
- **Monitoring**: Prometheus metrics, Zipkin tracing, Grafana dashboards

## Prerequisites

- Java 21+
- Maven 3.8+
- MongoDB running on localhost:27017
- Prometheus running on localhost:9090
- Zipkin running on localhost:9411
- Grafana running on localhost:3000

## Infrastructure Setup

### Kafka Setup

Start Zookeeper and Kafka locally:

```bash
./kafka_2.13-3.9.1/bin/zookeeper-server-start.sh ./kafka_2.13-3.9.1/config/zookeeper.properties
sleep 20 && ./kafka_2.13-3.9.1/bin/kafka-server-start.sh ./kafka_2.13-3.9.1/config/server.properties
```

### Auxiliary Services

Ensure these are running locally:
- MongoDB (localhost:27017)
- Prometheus (localhost:9090)
- Zipkin (localhost:9411)
- Grafana (localhost:3000)

Or use Docker Compose (to run in container instead):
```bash
docker compose up -d
```

## Main services: Build & Run

### Resolve dependencies and Compile all services

```bash
mvn dependency:resolve
mvn clean compile
```

### Run Services Locally (in order)

1. **Start Eureka Server** (must start first, then others can start):
```bash
cd eureka-server
mvn spring-boot:run
```

2. **Start API Gateway**:
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

6. **Start Notification Service**:
```bash
cd notification-service
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

# Search users by name content (case-insensitive)
curl "http://localhost:8080/users/search?name=John"

# Delete user by ID
curl -X DELETE http://localhost:8080/users/{userId}

# Delete user by exact name match (case-insensitive)
curl -X DELETE http://localhost:8080/users/delete/John%20Doe
```

### Post Service (Port 8082)

```bash
# Create post (requires valid userId)
curl -X POST http://localhost:8080/posts \
  -H "Content-Type: application/json" \
  -d '{"userId": "{userId}", "title": "My First Post", "content": "Hello World!"}'

# Get all posts
curl http://localhost:8080/posts

# Get post by ID
curl http://localhost:8080/posts/{postId}

# Get post with user info (OpenFeign call)
curl http://localhost:8080/posts/{postId}/with-user

# Search posts by title content (case-insensitive)
curl "http://localhost:8080/posts/search?title=First"

# Search posts by author's name (case-insensitive)
curl "http://localhost:8080/posts/search?author=John"

# Delete post by ID
curl -X DELETE http://localhost:8080/posts/{postId}

# Delete post by exact title match (case-insensitive)
curl -X DELETE http://localhost:8080/posts/delete/My%20First%20Post
```

### Comment Service (Port 8083)

```bash
# Create comment (requires valid postId and userId)
curl -X POST http://localhost:8080/comments \
  -H "Content-Type: application/json" \
  -d '{"postId": "{postId}", "userId": "{userId}", "content": "Great post!"}'

# Get all comments
curl http://localhost:8080/comments

# Get comments for a post
curl http://localhost:8080/comments/post/{postId}

# Delete comment by ID
curl -X DELETE http://localhost:8080/comments/{commentId}

# Get comment with post via openfeign
curl http://localhost:8080/comments/{commentId}/detailed

# Search comments by post's title content (case-insensitive)
curl "http://localhost:8080/comments/search?title=First"

# Search comments by user's name (case-insensitive)
curl "http://localhost:8080/comments/search?commenter=John"

# Test circuit breaker (by passing fake IDs)
curl http://localhost:8080/comments/test-circuit-breaker/user/{userId}
curl http://localhost:8080/comments/test-circuit-breaker/post/{postId}
```

### Notification Service (Port 8084)

```bash
# Get latest post notifications
curl http://localhost:8080/notifications/latest/post

# Get latest comment notifications
curl http://localhost:8080/notifications/latest/comment
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

- API Gateway: http://localhost:8080/actuator/prometheus
- User Service: http://localhost:8081/actuator/prometheus
- Post Service: http://localhost:8082/actuator/prometheus
- Comment Service: http://localhost:8083/actuator/prometheus
- Notification Service: http://localhost:8084/actuator/prometheus

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
curl http://localhost:8084/actuator/health
```

## Kafka Topics

Topics are created automatically:
- `post-events` - Published by Post Service, consumed by Notification Service
- `comment-events` - Published by Comment Service, consumed by Notification Service

The Notification Service listens to both topics and logs received events.

## Project Structure

```
microservices-demo/
├── pom.xml                    # Parent POM
├── docker-compose.yml         # Mongo,Zipkin,Prometheus,Grafana docker setup
├── common/                    # Shared code
├── eureka-server/            # Service Registry
├── api-gateway/              # API Gateway
├── user-service/             # User microservice
├── post-service/             # Post microservice
├── comment-service/          # Comment microservice
└── notification-service/      # Notification microservice
```

## Tech Stack

- Spring Boot 3.2.0
- Spring Cloud 2023.0.0
- Spring Data MongoDB
- Spring Cloud OpenFeign
- Spring Cloud Gateway
- Resilience4j (Circuit Breaker)
- Apache Kafka (Producer + Consumer)
- Netflix Eureka
- Micrometer + Prometheus
- Zipkin Distributed Tracing
