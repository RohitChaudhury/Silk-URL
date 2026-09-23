# 🔗 Silk-URL

### High-Throughput URL Shortener with Layered Rate Limiting

**Silk-URL** is a high-throughput URL shortening application built using **Java 21, Spring Boot, Spring Cloud Gateway, Redis, PostgreSQL, Hibernate/JPA, and JWT**.

The application allows authenticated customers to generate and manage shortened URLs while providing a public URL-access service that redirects users to the original destination.

The project focuses on backend engineering concepts such as **high-throughput request handling, layered rate limiting, Redis-backed runtime state, caching, JWT authentication, database persistence, API design, and scalable system architecture**.

---

## 📌 Overview

Silk-URL is divided into two primary services:

### 🔐 Generate URL Service

The Generate URL Service provides authenticated APIs through which customers can:

- Create Short URLs from valid Long URLs
- View all URLs created by their account
- Retrieve a specific URL
- Update a URL
- Modify URL-level rate limits
- Delete a URL

All URL-management APIs require **JWT Bearer Authentication**.

### 🌐 Access URL Service

The Access URL Service provides public access to generated Short URLs.

For example:

```text
http://localhost:3000/abc123
```

When a user accesses the Short URL, Silk-URL resolves the corresponding destination and returns an HTTP:

```text
302 Found
```

redirecting the user to the original Long URL.

---

# 🏗️ Architecture

Silk-URL uses a **Spring Cloud Gateway + Spring Boot application** architecture.

The core URL-shortening functionality remains within a single Spring Boot application, while the Gateway operates as a separate application/process in front of it.

```text
                           Internet
                               │
                               ▼
                  ┌─────────────────────────┐
                  │   Spring Cloud Gateway  │
                  │          :3000           │
                  │                         │
                  │ Global Rate Limiter     │
                  └────────────┬────────────┘
                               │
                               ▼
                  ┌─────────────────────────┐
                  │       Silk-URL Core     │
                  │          :8000          │
                  │                         │
                  │     Spring Boot         │
                  │                         │
                  │ ┌─────────────────────┐ │
                  │ │ Generate URL        │ │
                  │ │ Service             │ │
                  │ │                     │ │
                  │ │ JWT Authentication │ │
                  │ │ URL Management      │ │
                  │ └─────────────────────┘ │
                  │                         │
                  │ ┌─────────────────────┐ │
                  │ │ Access URL          │ │
                  │ │ Service             │ │
                  │ │                     │ │
                  │ │ Public Short URLs   │ │
                  │ │ 302 Redirects       │ │
                  │ └─────────────────────┘ │
                  └────────────┬────────────┘
                               │
                     ┌─────────┴─────────┐
                     │                   │
                     ▼                   ▼
              ┌─────────────┐     ┌─────────────┐
              │    Redis    │     │    PostgreSQL    │
              │             │     │             │
              │ Rate Limits │     │ Users       │
              │ Cache       │     │ URLs        │
              │ Runtime     │     │ Rate Config │
              │ State       │     │             │
              └─────────────┘     └─────────────┘
```

---

# 🔄 Request Flow

## Generate URL Flow

```text
Client
   │
   │ POST /url/create-url
   │ Authorization: Bearer <JWT>
   ▼
Spring Cloud Gateway :3000
   │
   │ Global Rate Limiter
   ▼
Silk-URL Core :8000
   │
   ▼
Generate URL Service
   │
   ├── Authenticate Customer
   ├── Validate Long URL
   ├── Generate ShortKey
   ├── Store URL Information
   └── Configure Rate Limits
   │
   ▼
Short URL Response
```

---

## Access URL Flow

```text
Client
   │
   │ GET /abc123
   ▼
Spring Cloud Gateway :3000
   │
   │ Global Rate Limiter
   ▼
Access URL Service
   │
   ├── ShortKey Rate Limit
   │
   ├── IP + ShortKey Rate Limit
   │
   ├── Resolve Long URL
   │
   ▼
HTTP 302 Found
   │
   ▼
Original Destination
```

---

# 🚦 Layered Rate Limiting

Rate limiting is one of the core design features of Silk-URL.

Instead of relying on a single rate limiter, the application implements multiple layers of traffic control.

```text
                 Incoming Request
                        │
                        ▼
             ┌─────────────────────┐
             │ Global Application   │
             │ Rate Limit           │
             │                     │
             │ Spring Cloud Gateway│
             └──────────┬──────────┘
                        │
                        ▼
             ┌─────────────────────┐
             │ Per-ShortKey        │
             │ Rate Limit           │
             └──────────┬──────────┘
                        │
                        ▼
             ┌─────────────────────┐
             │ Per-IP + ShortKey   │
             │ Rate Limit           │
             └──────────┬──────────┘
                        │
                        ▼
                  Process Request
```

---

## 1. Global Application Rate Limit

The first layer is implemented using **Spring Cloud Gateway**.

The Gateway uses a Redis-backed Token Bucket implementation.

All application traffic shares the same global rate-limit key:

```text
global-application
```

Therefore, requests to different endpoints consume tokens from the same global application bucket.

For example:

```text
POST /url/create-url
GET  /url/get-all-url
GET  /url/get-url/1
GET  /abc123
GET  /xyz789
```

all participate in the global traffic limit.

### Global Rate Limit

The application is configured/designed for a global traffic protection limit of:

```text
100,000 requests/second
```

This represents the **configured global rate-limit ceiling/design target**.

It should not be interpreted as a measured application throughput of 100,000 RPS. Actual throughput depends on factors such as:

- CPU
- JVM performance
- Redis performance
- PostgreSQL performance
- Network capacity
- Concurrent connections
- Deployment infrastructure
- Application workload

Actual throughput should therefore be determined through load testing.

---

# 2. Per-ShortKey Rate Limiting

Each Short URL can have its own overall request limit.

For example:

```text
urlTokens = 20,000
```

means that the particular Short URL has an overall configured limit of:

```text
20,000 requests/minute
```

This limit is shared by all clients accessing that Short URL.

Example:

```text
                    Short URL
                     abc123
                       │
              ┌────────┴────────┐
              │                 │
            IP A              IP B
              │                 │
              └────────┬────────┘
                       │
                Shared URL Bucket
                   20,000/min
```

The URL-level tokens are refilled every minute.

---

# 3. Per-IP + ShortKey Rate Limiting

Silk-URL also provides a separate rate limit for an individual IP address accessing a particular Short URL.

For example:

```text
ipAddressTokens = 100
```

means an individual IP can make up to:

```text
100 requests/minute
```

to that particular Short URL.

Different IP addresses have independent buckets.

```text
                 /abc123
                    │
          ┌─────────┴─────────┐
          │                   │
       IP A                 IP B
     100/min               100/min
```

This provides more granular protection against excessive traffic from a single client.

---

# 🪣 Token Bucket Rate Limiting

Silk-URL uses a **Token Bucket-based approach** for request limiting.

Conceptually:

```text
              Token Bucket
        ┌─────────────────────┐
        │ ● ● ● ● ● ● ● ● ●   │
        │                     │
        │ Tokens consumed by  │
        │ incoming requests   │
        └──────────┬──────────┘
                   │
                   ▼
                Request
```

If a token is available:

```text
Token Available
      │
      ▼
Consume Token
      │
      ▼
Allow Request
```

If no token is available:

```text
No Token
   │
   ▼
Reject Request
   │
   ▼
HTTP 429 Too Many Requests
```

Redis is used for the runtime state required by the high-frequency rate-limiting operations.

---

# 🛡️ Why Multiple Rate Limits?

Each layer protects a different part of the system.

### Global Limit

Protects the entire application:

```text
All Traffic
    ↓
Global Application Limit
```

### Per-ShortKey Limit

Protects an individual shortened URL:

```text
/abc123
   ↓
URL-Level Limit
```

### Per-IP + ShortKey Limit

Controls how much traffic one client can send to a particular Short URL:

```text
IP A → /abc123
         ↓
    IP-Level Limit
```

Together, these limits provide layered traffic protection.

---

# 💾 Data Storage Strategy

Silk-URL separates persistent application data from high-frequency runtime state.

## PostgreSQL

PostgreSQL is the persistent data store.

It stores information such as:

- Users
- URL records
- ShortKeys
- URL IDs
- Customer-configured rate limits
- Other persistent application information

Customer-configured rate limits are stored in the database as the persistent source of truth.

---

## Redis

Redis is used for high-frequency runtime operations such as:

- Rate-limit bucket state
- Runtime counters/state
- URL caching
- Other transient data

This avoids relying on PostgreSQL for every high-frequency rate-limit operation.

```text
                    Silk-URL
                       │
              ┌────────┴────────┐
              │                 │
              ▼                 ▼
           PostgreSQL              Redis
              │                 │
        Persistent Data     Runtime State
        Configuration       Rate Limits
        Users               Cache
        URLs
```

---

# ⚡ Caching

Redis can be used to cache frequently accessed URL information.

The conceptual flow is:

```text
Request
   │
   ▼
Redis Cache
   │
   ├── Cache Hit ──────► Long URL
   │
   └── Cache Miss
            │
            ▼
          PostgreSQL
            │
            ▼
       Redis Cache
```

Caching reduces repeated database lookups for frequently accessed Short URLs and helps reduce database workload.

---

# 🔐 Authentication

Silk-URL uses **JWT Bearer Authentication** for protected customer APIs.

Authenticated requests must contain:

```http
Authorization: Bearer <JWT_TOKEN>
```

JWT tokens are currently valid for:

```text
24 hours
```

A new token can be generated using:

```http
POST /user/get-token
```

---

# 🔗 API Overview

The API is divided into two major categories.

## Generate URL Service

All of these endpoints require JWT Bearer Authentication.

| Method   | Endpoint               | Description                            |
| -------- | ---------------------- | -------------------------------------- |
| `POST`   | `/url/create-url`      | Create a Short URL                     |
| `GET`    | `/url/get-all-url`     | Get all URLs generated by the customer |
| `GET`    | `/url/get-url/{id}`    | Get a specific URL                     |
| `PUT`    | `/url/update-url`      | Update a URL and/or its rate limits    |
| `DELETE` | `/url/delete-url/{id}` | Delete a URL                           |
| `GET`    | `/index`               | Application endpoint                   |

---

## Access URL Service

Public Short URLs are accessed using:

```http
GET /{shortKey}
```

Example:

```text
GET http://localhost:3000/abc123
```

A successful request results in:

```http
302 Found
Location: https://example.com
```

---

# 🛠️ Technology Stack

| Technology               | Purpose                                  |
| ------------------------ | ---------------------------------------- |
| **Java 21**              | Core programming language                |
| **Spring Boot**          | Core backend framework                   |
| **Spring Cloud Gateway** | Global traffic management                |
| **Spring Web / MVC**     | REST API and HTTP request handling       |
| **Redis**                | Rate limiting, caching and runtime state |
| **PostgreSQL**           | Persistent data storage                  |
| **Hibernate / JPA**      | ORM and database interaction             |
| **Maven**                | Build and dependency management          |
| **JWT**                  | Authentication                           |
| **Git / GitHub**         | Version control                          |
| **hey**                  | Load testing                             |

---

# ⚙️ Local Development

## Prerequisites

Install:

- Java 21
- Maven
- PostgreSQL
- Redis
- Git

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

Verify Redis:

```bash
redis-cli ping
```

Expected:

```text
PONG
```

---

# 🗄️ PostgreSQL Configuration

Create the required PostgreSQL database and configure the Silk-URL application's database connection.

Example:

```properties
spring.datasource.url=jdbc:postgreSQL://localhost:5432/<database_name>
spring.datasource.username=<username>
spring.datasource.password=<password>
```

**Do not commit real database credentials to the repository.**

Use environment variables or local configuration for sensitive values.

---

# 🔴 Redis Configuration

Silk-URL uses Redis locally on:

```text
Host: localhost
Port: 6379
```

Start Redis:

```bash
redis-server
```

Verify:

```bash
redis-cli ping
```

Expected:

```text
PONG
```

---

# ▶️ Running the Application

Silk-URL consists of two Spring Boot applications.

## 1. Start the Core Application

The core application runs on:

```text
localhost:8000
```

Start it using:

```bash
mvn spring-boot:run
```

---

## 2. Start the Gateway

The Spring Cloud Gateway runs on:

```text
localhost:3000
```

Start the Gateway using:

```bash
mvn spring-boot:run
```

Normal client traffic should go through:

```text
http://localhost:3000
```

rather than directly accessing:

```text
http://localhost:8000
```

The architecture is:

```text
Client
  │
  ▼
Gateway :3000
  │
  ▼
Core Application :8000
```

In a production deployment, the core application's port should normally remain internal and the Gateway should act as the public entry point.

---

# 🧪 Load Testing

Silk-URL can be tested using tools such as:

- `curl`
- ApacheBench (`ab`)
- `hey`

For example:

```bash
hey -n 50000 -c 100 \
  http://localhost:3000/abc123
```

For an authenticated endpoint:

```bash
hey -n 50000 -c 100 \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  http://localhost:3000/url/get-all-url
```

Where:

```text
-n 50000
```

means:

```text
50,000 total requests
```

and:

```text
-c 100
```

means:

```text
100 concurrent requests
```

### Important

The number of total requests is **not** the same as requests per second.

For example:

```text
50,000 total requests
```

does not mean:

```text
50,000 requests/second
```

Actual throughput must be measured from the load-test results.

---

# 🛡️ Abuse Prevention

Rate limiting provides an important layer of abuse prevention within Silk-URL.

Currently implemented mechanisms include:

- Global application rate limiting
- Per-ShortKey rate limiting
- Per-IP + ShortKey rate limiting
- Redis-backed runtime rate-limit state
- HTTP `429 Too Many Requests`
- Customer-configurable limits

Potential future mechanisms include:

- ShortKey enumeration detection
- Suspicious traffic detection
- IP reputation/blocklists
- Temporary IP bans
- CAPTCHA/challenge mechanisms
- Malicious URL detection
- Per-user/API-key quotas
- Anomaly detection

---

# 🔒 Security

Silk-URL uses JWT authentication for protected APIs.

---

# 🚧 Current Scope

### Implemented

- [x] User registration
- [x] JWT authentication
- [x] 24-hour JWT token validity
- [x] JWT token regeneration
- [x] Short URL generation
- [x] Public Short URL access
- [x] HTTP 302 redirection
- [x] URL retrieval
- [x] URL update
- [x] URL deletion
- [x] PostgreSQL persistence
- [x] Redis integration
- [x] Redis caching
- [x] Per-ShortKey rate limiting
- [x] Per-IP + ShortKey rate limiting
- [x] Spring Cloud Gateway
- [x] Global application rate limiting
- [x] HTTP 429 handling
- [x] Load testing with `hey`

### Protected URL Routing — Planned Enhancement

Silk-URL currently does not provide **protected URL routing**, where the destination URL can only be accessed through its generated Short URL and direct access to the original public URL is restricted.

For such a setup, users would need to place their destination behind a **private firewall/reverse-proxy layer** that only permits requests originating from the Silk-URL application infrastructure. The Short URL would then act as the controlled entry point to the protected resource.

This capability is currently considered a **future feature enhancement** for Silk-URL and is not part of the current implementation.

### Planned / Further Improvements

- [ ] ShortKey enumeration protection/Protected URL Routing.
- [ ] Prometheus/Grafana observability

---

# 📚 Documentation

## API Documentation

Detailed API documentation is available at:

**[`docs/API.md`](docs/API.md)**

It contains:

- User registration
- Authentication
- JWT token generation
- Short URL creation
- URL retrieval
- URL updates
- URL deletion
- Public Short URL access
- Rate-limit configuration
- HTTP status codes
- Request/response examples
- `curl` examples

Postman Collection for the APIs available at:

**[`docs/postman-collection/Silk URL APIs.postman_collection.json`](docs/postman-collection/Silk URL APIs.postman_collection.json)**

---

# 🎯 Project Goals

Silk-URL was built to explore practical backend engineering and scalable system-design concepts rather than simply implementing a basic URL shortener.

The project focuses on:

- High-throughput HTTP services
- URL shortening
- REST API design
- Java 21
- Spring Boot
- Spring Cloud Gateway
- Redis
- PostgreSQL
- JWT authentication
- Token Bucket rate limiting
- Distributed runtime state
- Caching
- Layered traffic protection
- Concurrency
- Load testing
- Scalable backend architecture

The primary objective is to understand how **authentication, persistence, caching, rate limiting, API Gateway infrastructure, and high-volume request handling work together in a backend system**.

---

# 👨‍💻 Author

**Rohit Chaudhury**

Backend Developer | Java | Spring Boot | Redis | PostgreSQL

Silk-URL is a personal backend engineering project focused on scalable application architecture, distributed rate limiting, high-throughput request handling, caching, and modern Java/Spring backend development.

## 📄 License

This project is licensed under the [MIT License](LICENSE).

Copyright © 2026 Rohit Chaudhury.
