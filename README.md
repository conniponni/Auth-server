# Auth Service

## Overview

Auth Service is the authentication and security component of the WebShop application. It provides endpoints for user authentication, registration, JWT handling, and access to authenticated user information.

The service is used by the Gateway to authenticate users and verify their identity when accessing protected resources.

## Responsibilities

Auth Service is responsible for:

* User registration and login.
* Authentication and authorization using Spring Security.
* JWT creation and configuration.
* JWT issuer and signing-key configuration.
* Providing authenticated user information.
* Supporting secure access to protected API endpoints.

## Technologies

* Java 17
* Spring Boot 4.0.6
* Spring WebMVC
* Spring Security
* OAuth 2.0 Authorization Server
* OAuth 2.0 Resource Server
* Spring Data JPA
* Hibernate
* PostgreSQL
* H2 database dependency
* Jakarta Bean Validation
* Lombok
* Springdoc OpenAPI

## Database

Auth Service is configured to use PostgreSQL.

The database connection is configured through environment variables:

* `DB_URL`
* `DB_USERNAME`
* `DB_PASSWORD`

Hibernate is configured with `ddl-auto=update`, which allows it to update the database schema based on the application's JPA entities.

The project also contains an H2 database dependency. Check the active configuration before assuming that H2 is used in a particular environment.

## Port

Auth Service runs locally on port `8080`.

The default local base URL is:

```text
http://localhost:8080
```

## Environment Variables

Configure the following environment variables before starting the application.

| Variable                 | Description                           | Default                 |
| ------------------------ | ------------------------------------- | ----------------------- |
| `DB_URL`                 | PostgreSQL connection URL             | Required                |
| `DB_USERNAME`            | PostgreSQL username                   | Required                |
| `DB_PASSWORD`            | PostgreSQL password                   | Required                |
| `JWT_PUBLIC_KEY`         | Public key used for JWT configuration | Required                |
| `JWT_PRIVATE_KEY`        | Private key used for JWT signing      | Required                |
| `JWT_ISSUER`             | JWT issuer identifier                 | `http://localhost:8080` |
| `JWT_KEY_ID`             | Identifier for the JWT signing key    | `test-key`              |
| `JWT_EXPIRATION_MINUTES` | JWT lifetime in minutes               | `60`                    |
| `AUTH_SERVER_URL`        | Auth Service base URL                 | `http://localhost:8080` |

**Security:** Never commit private keys, passwords, or other secrets to the repository. Configure them through environment variables or an appropriate local secret-management mechanism.

## Prerequisites

Before running Auth Service, ensure that you have:

* Java 17 installed.
* A configured PostgreSQL database.
* The required environment variables configured.
* Access to the repository's Maven Wrapper.

## Running Locally

### Windows

Open a terminal in the Auth Service repository root and run:

```powershell
mvnw.cmd spring-boot:run
```

### Linux and macOS

Open a terminal in the Auth Service repository root and run:

```bash
./mvnw spring-boot:run
```

Ensure that the required environment variables are configured in the terminal or development environment before starting the application.

## API Endpoints

The service exposes authentication-related endpoints used by the WebShop application.

The exact endpoint mappings should be checked in the controller classes. Common operations include:

* User registration.
* User login.
* Retrieving the current authenticated user's information.
* Retrieving public keys used for JWT verification.

The Gateway provides the frontend with access to the relevant authentication endpoints.

## Related Repositories

The WebShop system consists of the following repositories:

* **Auth Service:** [Auth-server](https://github.com/conniponni/Auth-server) — authentication, user registration, and JWT handling.
* **Product Service:** [Product_Service](https://github.com/philipwmitilinos-max/Product_Service) — product information and stock management.
* **Order Service:** [order-service-webshop](https://github.com/sedina-imamovic/order-service-webshop) — order management.
* **WebShop Gateway:** [webshop-gateway](https://github.com/conniponni/webshop-gateway) — routes frontend requests to the backend services.
* **Frontend:** [agil-frontend](https://github.com/Stefanvill/agil-frontend) — the WebShop user interface.
* **Email Service:** [email-service](https://github.com/Stefanvill/email-service) — email-related functionality.
