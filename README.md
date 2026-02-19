# Vend Application

A Spring Boot application for vending machine management.

## Overview

This application provides a backend API for managing vending machines, including device management, transactions, and payment processing.

## Technology Stack

- Java 17
- Spring Boot 3.2.4
- Spring Data JPA
- Spring Security
- PostgreSQL
- WebSocket
- Maven

## CI/CD Setup

This project uses GitHub Actions for continuous integration and continuous deployment to a Digital Ocean Ubuntu server.

### CI/CD Pipeline

The CI/CD pipeline automatically:
1. Builds the application
2. Runs tests
3. Deploys to a Digital Ocean Ubuntu server (on push to main branch)

### Deployment Configuration

For detailed instructions on setting up the CI/CD pipeline and configuring the deployment, see the [CI/CD Setup Instructions](.github/workflows/README.md).

## Development Setup

### Prerequisites

- Java 17
- Maven
- PostgreSQL

### Building the Application

```bash
mvn clean install
```

### Running the Application Locally

```bash
mvn spring-boot:run
```

### Running Tests

```bash
mvn test
```

## API Documentation

The API documentation is available through Springdoc OpenAPI UI. When the application is running, you can access it at:

```
http://localhost:8080/swagger-ui.html
```

## Project Structure

- `src/main/java/com/eazybrew/vend/controller` - REST controllers
- `src/main/java/com/eazybrew/vend/service` - Business logic
- `src/main/java/com/eazybrew/vend/repository` - Data access
- `src/main/java/com/eazybrew/vend/model` - Domain models
- `src/main/java/com/eazybrew/vend/dto` - Data transfer objects
- `src/main/java/com/eazybrew/vend/config` - Configuration classes
- `src/main/java/com/eazybrew/vend/websocket` - WebSocket functionality
- `src/main/java/com/eazybrew/vend/paystack` - Payment integration

## Security Features

This project uses GitHub Advanced Security to enhance code security:

- **Dependency Scanning**: Automatically identifies vulnerable dependencies
- **Secret Scanning**: Prevents accidental exposure of credentials
- **Code Scanning**: Uses CodeQL to identify security vulnerabilities in code
- **Dependabot**: Automatically updates dependencies with security issues

For more information on security features:
- [GitHub Advanced Security Setup Guide](GITHUB-ADVANCED-SECURITY.md)
- [Security Policy](SECURITY.md)

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request
