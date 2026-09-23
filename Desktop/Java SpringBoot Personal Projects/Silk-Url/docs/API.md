# Silk-URL API Documentation

**Silk-URL** is a high-throughput URL shortening service that allows authenticated customers to create, manage, and rate-limit shortened URLs.

The application consists of two major services:

1. **Generate URL Service** — Authenticated APIs used by customers to create and manage their shortened URLs.
2. **Access URL Service** — Public service used by visitors to access a shortened URL and get redirected to the original destination.

---

# Table of Contents

- [Base URL](#base-url)
- [Authentication](#authentication)
- [User APIs](#user-apis)
  - [Create User](#1-create-user)
  - [Generate JWT Token](#2-generate-jwt-token)

- [Generate URL Service](#generate-url-service)
  - [Create Short URL](#1-create-short-url)
  - [Get All URLs](#2-get-all-urls)
  - [Get URL by ID](#3-get-url-by-id)
  - [Update URL](#4-update-url)
  - [Delete URL](#5-delete-url)
  - [Index / Health Endpoint](#6-index-endpoint)

- [Access URL Service](#access-url-service)
- [Rate Limiting](#rate-limiting)
- [HTTP Status Codes](#http-status-codes)
- [Request Flow](#request-flow)
- [Examples](#examples)

---

# Base URL

For local development, the application can be accessed through the Spring Cloud Gateway using:

```text
http://localhost:3000
```

The Gateway acts as the entry point to the application and provides global traffic protection before requests reach the core Silk-URL application.

For a deployed environment, replace the base URL with the application's public API domain.

Throughout this documentation:

```text
BASE_URL = http://localhost:8000
```

---

# Authentication

Silk-URL uses **JWT Bearer Authentication** for authenticated APIs.

After successfully registering or logging in, the API provides a JWT bearer token.

Authenticated requests must include:

```http
Authorization: Bearer <JWT_TOKEN>
```

Example:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

The JWT token is currently valid for **24 hours** from the time of generation.

After the token expires, the customer can generate a new token using the token-generation endpoint.

---

# User APIs

User APIs are used to register customers and obtain JWT authentication tokens.

## 1. Create User

Creates/registers a new Silk-URL user.

### Endpoint

```http
POST /user/create
```

### Authentication

**Public endpoint**

A JWT token is not required.

### Request Body

```json
{
  "email": "example@email.com",
  "username": "example",
  "password": "Example@123"
}
```

### Parameters

| Parameter  | Type   | Required | Description                                      |
| ---------- | ------ | -------- | ------------------------------------------------ |
| `email`    | String | Yes      | Valid email address                              |
| `username` | String | Yes      | Username chosen by the customer                  |
| `password` | String | Yes      | Password satisfying the required password policy |

### Password Requirements

The password must:

- Contain at least one digit: `[0-9]`
- Contain at least one lowercase Latin character: `[a-z]`
- Contain at least one uppercase Latin character: `[A-Z]`
- Contain at least one special character such as `!`, `@`, `#`, `&`, `(`, `)`
- Contain at least 8 characters
- Contain no more than 20 characters

Example of a valid password:

```text
Example@123
```

### Successful Response

A successful registration returns user information together with a JWT bearer token and its expiration time.

Example:

```json
{
  "data": [
    {
      "email": "example@email.com",
      "username": "example",
      "bearerToken": "randomtoken",
      "tokenExpireTime": "2026-09-24T10:30:00Z"
    }
  ]
}
```

The `tokenExpireTime` is returned in UTC time format.

### Token Validity

The generated JWT token is valid for:

```text
24 hours
```

---

# 2. Generate JWT Token

Generates a new JWT bearer token for an existing user.

### Endpoint

```http
POST /user/get-token
```

### Authentication

**Public endpoint**

A JWT token is not required.

### Request Body

```json
{
  "username": "example",
  "password": "Example@123"
}
```

### Successful Response

The response contains a new JWT bearer token and its expiration time.

Example:

```json
{
  "data": [
    {
      "bearerToken": "randomtoken",
      "tokenExpireTime": "2026-09-24T10:30:00Z"
    }
  ]
}
```

The returned token can then be used to authenticate requests to the Generate URL Service.

---

# Generate URL Service

The Generate URL Service provides authenticated APIs for customers to create and manage their shortened URLs.

All Generate URL Service endpoints require a valid JWT Bearer Token.

### Authentication Header

```http
Authorization: Bearer <JWT_TOKEN>
```

The general endpoint structure is:

```text
BASE_URL/url/ACTION
```

For example:

```text
POST /url/create-url
```

---

# 1. Create Short URL

Creates a new shortened URL from a valid long URL.

### Endpoint

```http
POST /url/create-url
```

### Authentication

**Required**

```http
Authorization: Bearer <JWT_TOKEN>
```

### Request Body

```json
{
  "longUrl": "https://developers.facebook.com/docs/facebook-login/web/login-button",
  "ipAddressTokens": 10,
  "urlTokens": 20000
}
```

### Request Parameters

| Parameter         | Type    | Required | Description                                                                  |
| ----------------- | ------- | -------- | ---------------------------------------------------------------------------- |
| `longUrl`         | String  | Yes      | Valid destination URL                                                        |
| `ipAddressTokens` | Integer | No       | Maximum requests per minute allowed from an individual IP for this Short URL |
| `urlTokens`       | Integer | No       | Maximum total requests per minute allowed for this Short URL                 |

### Default Rate Limits

If the customer does not provide the rate-limit values, Silk-URL applies the following defaults:

```text
ipAddressTokens = 60 requests/minute/IP
urlTokens       = 50,000 requests/minute
```

The configured tokens are refilled every minute.

### Rate-Limiting Semantics

The two values represent separate limits:

**IP Address Tokens**

Controls how many requests a particular IP address can make to the Short URL during a refill period.

**URL Tokens**

Controls the total number of requests that can be made to that Short URL during a refill period, regardless of the requesting IP address.

For example:

```text
Short URL: abc123

IP limit:
100 requests/minute/IP

Overall URL limit:
20,000 requests/minute
```

Multiple IP addresses can access the URL, but:

```text
Individual IP → maximum 100 requests/minute
All IPs combined → maximum 20,000 requests/minute
```

### Successful Response

The response contains the generated Short URL and its database ID.

Example:

```json
{
  "code": 201,
  "data": [
    {
      "id": 1,
      "longUrl": "https://example.com/long/url",
      "shortUrl": "http://localhost:3000/00000000001",
      "ipAddressTokens": 60,
      "urlTokens": 50000,
      "lastChanged": "2026-09-23T11:10:51.619733"
    }
  ],
  "error": {
    "details": [],
    "type": ""
  },
  "message": "Short URL Created Successfully",
  "status": "Created"
}
```

### Important

The returned `id` identifies the URL record in Silk-URL.

Customers should retain this ID because it is required when retrieving, updating, or deleting a specific URL.

---

# 2. Get All URLs

Returns all shortened URLs generated by the authenticated customer.

### Endpoint

```http
GET /url/get-all-url
```

### Authentication

**Required**

```http
Authorization: Bearer <JWT_TOKEN>
```

### Request Body

None.

### Example

```bash
curl -X GET "http://localhost:3000/url/get-all-url" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

The response contains the URLs associated with the authenticated user.

---

# 3. Get URL by ID

Retrieves information about a specific shortened URL using its database ID.

### Endpoint

```http
GET /url/get-url/{id}
```

### Authentication

**Required**

```http
Authorization: Bearer <JWT_TOKEN>
```

### Path Parameter

| Parameter | Type    | Description                      |
| --------- | ------- | -------------------------------- |
| `id`      | Integer | Database ID of the shortened URL |

### Example

```http
GET /url/get-url/1
```

Or:

```bash
curl -X GET "http://localhost:3000/url/get-url/1" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

# 4. Update URL

Updates an existing shortened URL and/or its rate-limit configuration.

### Endpoint

```http
PUT /url/update-url
```

### Authentication

**Required**

```http
Authorization: Bearer <JWT_TOKEN>
```

### Request Body

```json
{
  "id": 1,
  "longUrl": "https://example.com/long/url",
  "ipAddressTokens": 103,
  "urlTokens": 30000
}
```

### Parameters

| Parameter         | Type    | Required | Description                   |
| ----------------- | ------- | -------- | ----------------------------- |
| `id`              | Integer | Yes      | ID of the URL record          |
| `longUrl`         | String  | Yes      | New destination URL           |
| `ipAddressTokens` | Integer | No       | New per-IP request limit      |
| `urlTokens`       | Integer | No       | New overall URL request limit |

`ipAddressTokens` and `urlTokens` are optional.

If the customer does not want to modify a particular rate limit, that parameter can be omitted.

For example, to update only the destination:

```json
{
  "id": 1,
  "longUrl": "https://example.com/new-destination"
}
```

---

# 5. Delete URL

Deletes a shortened URL belonging to the authenticated customer.

### Endpoint

```http
DELETE /url/delete-url/{id}
```

### Authentication

**Required**

```http
Authorization: Bearer <JWT_TOKEN>
```

### Path Parameter

| Parameter | Type    | Description                      |
| --------- | ------- | -------------------------------- |
| `id`      | Integer | Database ID of the URL to delete |

### Example

```http
DELETE /url/delete-url/2
```

Or:

```bash
curl -X DELETE "http://localhost:3000/url/delete-url/2" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

# 6. Application Greeting Endpoint

Provides a basic Hello to the Application to test the Application is running Successfully

### Endpoint

```http
GET /index
```

### Authentication

No Authentication

### Example

```http
GET /index
```

---

# Access URL Service

The Access URL Service is responsible for serving publicly accessible Short URLs.

Unlike the Generate URL Service, customers do not need to authenticate merely to access a public Short URL.

### Endpoint

```http
GET /{shortKey}
```

### Example

If Silk-URL generates:

```text
http://localhost:3000/abc123
```

the visitor can access:

```http
GET /abc123
```

The service resolves the `shortKey` to the corresponding long URL.

### Successful Request

The service responds with:

```http
HTTP/1.1 302 Found
Location: https://example.com
```

The client can then follow the redirect to the original destination.

### Request Flow

```text
Client
  │
  │ GET /abc123
  ▼
Spring Cloud Gateway
  │
  ▼
Global Rate Limiter
  │
  ▼
Access URL Service
  │
  ├── Validate ShortKey
  │
  ├── Apply ShortKey Rate Limit
  │
  ├── Apply IP + ShortKey Rate Limit
  │
  └── Resolve Original URL
          │
          ▼
       HTTP 302
          │
          ▼
    Original URL
```

---

# Rate Limiting

Silk-URL uses multiple layers of rate limiting to protect both the overall application and individual shortened URLs.

The system uses a **Token Bucket-based approach** with Redis for high-frequency rate-limit state.

---

## 1. Global Application Rate Limit

All requests entering Silk-URL pass through the Spring Cloud Gateway.

The Gateway applies a global application-level rate limit.

The application is designed/configured for a global limit of:

```text
100,000 requests/second
```

This is an application-level traffic protection limit.

The actual sustainable throughput depends on the deployment infrastructure, Redis performance, database workload, network capacity, and application configuration. Benchmark results should therefore be evaluated separately from the configured rate limit.

---

## 2. Per-Short URL Rate Limit

Every generated Short URL can have its own overall request limit.

For example:

```text
urlTokens = 20,000
```

means the Short URL can receive up to:

```text
20,000 requests/minute
```

before the URL-level rate limit is exceeded.

The tokens are refilled every minute.

---

## 3. Per-IP + Short URL Rate Limit

A separate rate limit applies to an individual IP address accessing a specific Short URL.

For example:

```text
ipAddressTokens = 100
```

means:

```text
IP Address A → maximum 100 requests/minute
```

for that particular Short URL.

Another IP address receives its own bucket.

---

# Rate-Limit Evaluation

For an Access URL request, Silk-URL applies the rate limits in layers.

Conceptually:

```text
Incoming Request
       │
       ▼
Global Application Limit
       │
       ▼
Per-ShortKey Limit
       │
       ▼
Per-IP + ShortKey Limit
       │
       ▼
Resolve Short URL
       │
       ▼
302 Redirect
```

If a request exceeds an applicable rate limit, Silk-URL returns:

```http
HTTP/1.1 429 Too Many Requests
```

---

# HTTP Status Codes

The following HTTP status codes may be returned by Silk-URL APIs.

| Status Code                 | Meaning                                                      |
| --------------------------- | ------------------------------------------------------------ |
| `200 OK`                    | Request successfully processed                               |
| `201 Created`               | Resource successfully created, where applicable              |
| `417 Expectation Failed`    | Server didn't responded as expected                          |
| `400 Bad Request`           | Invalid request or request parameters                        |
| `401 Unauthorized`          | Authentication is missing or invalid                         |
| `403 Forbidden`             | Authenticated user is not permitted to perform the operation |
| `404 Not Found`             | Requested resource or ShortKey does not exist                |
| `429 Too Many Requests`     | Applicable rate limit has been exceeded                      |
| `500 Internal Server Error` | Unexpected server-side error                                 |

---

# Examples for CURL Commands

## Register a User

```bash
curl -X POST "http://localhost:3000/user/create" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "example@email.com",
    "username": "example",
    "password": "Example@123"
  }'
```

---

## Generate a JWT Token

```bash
curl -X POST "http://localhost:3000/user/get-token" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "example",
    "password": "Example@123"
  }'
```

---

## Create a Short URL

```bash
curl -X POST "http://localhost:3000/url/create-url" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "longUrl": "https://example.com",
    "ipAddressTokens": 100,
    "urlTokens": 20000
  }'
```

---

## Get All URLs

```bash
curl -X GET "http://localhost:3000/url/get-all-url" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## Get a URL by ID

```bash
curl -X GET "http://localhost:3000/url/get-url/1" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## Update a URL

```bash
curl -X PUT "http://localhost:3000/url/update-url" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "longUrl": "https://example.com/new-url",
    "ipAddressTokens": 100,
    "urlTokens": 30000
  }'
```

---

## Delete a URL

```bash
curl -X DELETE "http://localhost:3000/url/delete-url/1" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## Access a Short URL

```bash
curl -i "http://localhost:3000/abc123"
```

A successful response will contain:

```http
HTTP/1.1 302 Found
Location: https://example.com
```

---

# Load Testing

Silk-URL can be load tested using tools such as `hey`, ApacheBench (`ab`), or other HTTP benchmarking tools.

Example using `hey`:

```bash
hey -n 50000 -c 100 \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  http://localhost:3000/url/get-all-url
```

Where:

```text
-n 50000 → 50,000 total requests
-c 100    → maximum 100 concurrent requests
```

For Access URL testing:

```bash
hey -n 50000 -c 100 \
  http://localhost:3000/abc123
```

> Note: Total request count and requests-per-second are different measurements. `-n 50000` means 50,000 total requests; it does not mean 50,000 requests per second.

When benchmarking Silk-URL, useful measurements include:

- Requests per second
- Average latency
- P95 latency
- P99 latency
- HTTP 2xx/3xx responses
- HTTP 429 responses
- HTTP 4xx responses
- HTTP 5xx responses
- Connection failures
- Gateway CPU and memory
- Redis CPU and memory
- Database performance

---
