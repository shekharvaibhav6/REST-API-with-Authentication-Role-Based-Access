# Scalability Notes

## Current Architecture

Right now this runs as a monolith — single Spring Boot app, single Postgres instance. That's fine for a small project but here's how I'd evolve it as the system grows.

---

## Database

**Connection pooling** is the first thing. Spring Boot uses HikariCP by default, but tuning the pool size based on expected concurrency matters. For a read-heavy task app, adding a read replica for GET queries would reduce load on the primary.

**Indexing:** The `tasks.user_id` column should be indexed (Hibernate adds it via the FK constraint, but worth confirming in production). If filtering by status/priority is added later, composite indexes help.

---

## Caching

For endpoints like `/admin/users` or `/tasks` (read-heavy, doesn't change every second), Redis caching via Spring Cache makes sense:

```java
@Cacheable("tasks")
public PageResponse<TaskResponse> getMyTasks(...) { ... }
```

Session/token blacklisting for logout can also be handled in Redis — right now tokens stay valid until expiry, which is a known limitation.

---

## Microservices (when it makes sense)

I wouldn't break this into microservices on day one — the overhead isn't worth it at small scale. But natural split points exist:
- **Auth Service** — handles registration, login, token issuance
- **Task Service** — CRUD on tasks, business rules
- **Notification Service** — email/push when task status changes

Each service would have its own DB schema. Services communicate via REST or a message broker (Kafka/RabbitMQ) for async events like "task completed → send email".

---

## Load Balancing

The API is stateless (no session, JWT only) so horizontal scaling is straightforward. Multiple instances behind Nginx or an AWS ALB would work without any code changes. Just run:

```
Instance 1: taskflow-api:8080
Instance 2: taskflow-api:8081
           ↕
        Nginx LB
           ↕
        Clients
```

---

## Docker

A basic `docker-compose.yml` would wire the API + Postgres together:

```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: taskflow_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: yourpassword

  api:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - db
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/taskflow_db
```

For production I'd push the image to ECR and deploy via ECS or Kubernetes.

---

## Logging & Monitoring

Currently using SLF4J for structured logging. At scale I'd add:
- **Centralized logging:** ELK stack (Elasticsearch + Logstash + Kibana) or CloudWatch
- **Tracing:** Spring Sleuth + Zipkin for distributed tracing across services
- **Metrics:** Micrometer + Prometheus + Grafana for API latency, error rates, DB pool usage

---

## Rate Limiting

To prevent abuse on `/auth/login` and `/auth/register`, a simple bucket4j filter or an API gateway-level rate limit would prevent brute-force and signup spam.

---

## Summary

| Concern | Short-term | Long-term |
|---|---|---|
| DB load | Read replicas + indexes | Sharding / separate DBs per service |
| Caching | Redis for hot queries | CDN for static + distributed cache |
| Scaling | Stateless instances + LB | Kubernetes autoscaling |
| Reliability | Docker Compose | ECS/K8s with health checks |
| Observability | Structured logs | ELK + Prometheus + Grafana |
