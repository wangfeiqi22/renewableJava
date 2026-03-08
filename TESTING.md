# Testing Guide

This project includes unit tests for the backend services.

## Prerequisites
- Java 17+
- Maven 3.8+

## Running Tests
To execute the full test suite, run the following command in the `backend` directory:

```bash
mvn test
```

## Test Coverage
Currently, the following core services are covered:
- **OrderService**: Verifies order creation logic (ID generation) and status updates.

## Future Work
- Add Integration Tests using `@SpringBootTest` with an in-memory H2 database or Testcontainers.
- Add Frontend Unit Tests using Vitest.
- Add End-to-End (E2E) Tests using Cypress.
