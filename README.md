# Java Debt Tracker API 💳📊

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Data JPA](https://img.shields.io/badge/Spring%20Data-JPA-blue.svg)](https://spring.io/projects/spring-data-jpa)
[![OpenAPI / Swagger](https://img.shields.io/badge/OpenAPI-Swagger%203-green.svg)](https://swagger.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A robust, clean-architecture RESTful API built with **Java 21** and **Spring Boot 3** to manage personal debts, recurrent bills, and installment payments. Designed following best software engineering practices, SOLID principles, domain validation, and comprehensive automated testing.

---

## 🚀 Key Features

- **Debt Categorization (`DebtType`)**:
  - `FIXA`: Recurring fixed bills (e.g., electricity, water, rent, fiber internet).
  - `PARCELADA`: Purchases split into multiple installments (e.g., credit card, financing, installment slips).
- **Domain Business Rules & Validation**:
  - Validates that installment debts have valid total and current installment numbers (`current <= total`).
  - Strict input validation using Jakarta Bean Validation (`@Positive`, `@NotBlank`, `@Size`).
- **Aggregated Financial Calculations**:
  - Dedicated `/api/debts/total` endpoint that calculates and returns the **total accumulated sum** directly via optimized database aggregation (`COALESCE(SUM(d.amount), 0)`) alongside debt count statistics.
- **Clean Architecture & Separation of Concerns**:
  - Entity decoupled from client contracts using **Java Records (DTOs)** and explicit mappers.
  - Global Exception Handling returning standardized error payloads (RFC 7807 compliant structure).
- **Interactive Documentation**:
  - Integrated **Swagger UI / OpenAPI 3** for instant API inspection and testing.
- **Embedded Database & Preloaded Seed Data**:
  - Zero-configuration in-memory H2 database with preloaded sample data in `data.sql`.
- **Automated Test Coverage**:
  - Unit tests with **JUnit 5** and **Mockito**.
  - Web MVC integration tests with **MockMvc**.

---

## 🛠️ Tech Stack

| Technology | Purpose |
| :--- | :--- |
| **Java 21 (LTS)** | Core programming language (Records, Pattern Matching) |
| **Spring Boot 3.3.4** | Application framework |
| **Spring Data JPA / Hibernate 6** | Object-Relational Mapping (ORM) and persistence |
| **H2 Database** | In-memory relational database for rapid development & testing |
| **Jakarta Bean Validation** | Declarative request payload validation |
| **SpringDoc OpenAPI 2.6.0** | Swagger UI and OpenAPI 3 schema generation |
| **JUnit 5 & Mockito** | Automated unit and integration testing |

---

## 🏛️ Project Architecture

```
src/
├── main/
│   ├── java/com/portfolio/debttracker/
│   │   ├── config/              # OpenAPI and application configuration
│   │   │   └── OpenApiConfig.java
│   │   ├── controller/          # RESTful Web Controllers
│   │   │   └── DebtController.java
│   │   ├── dto/                 # Immutable Request/Response Records
│   │   │   ├── DebtRequestDTO.java
│   │   │   ├── DebtResponseDTO.java
│   │   │   └── DebtSummaryDTO.java
│   │   ├── entity/              # JPA Domain Entities & Enums
│   │   │   ├── Debt.java
│   │   │   └── DebtType.java
│   │   ├── exception/           # Custom exceptions & global handler
│   │   │   ├── BusinessException.java
│   │   │   ├── ErrorResponse.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── mapper/              # Entity-DTO mapping logic
│   │   │   └── DebtMapper.java
│   │   ├── repository/          # Spring Data JPA repositories
│   │   │   └── DebtRepository.java
│   │   ├── service/             # Business logic layer
│   │   │   ├── DebtService.java
│   │   │   └── impl/
│   │   │       └── DebtServiceImpl.java
│   │   └── DebtTrackerApiApplication.java
│   └── resources/
│       ├── application.yml      # Spring Boot configuration
│       └── data.sql             # Initial seed data for H2
└── test/
    └── java/com/portfolio/debttracker/
        ├── controller/
        │   └── DebtControllerTest.java
        └── service/
            └── DebtServiceTest.java
```

---

## 📋 API Endpoints Reference

| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/debts` | Register a new debt (Fixed or Installment) | `201 Created` |
| `GET` | `/api/debts` | List all registered debts | `200 OK` |
| `GET` | `/api/debts/{id}` | Retrieve details of a specific debt by ID | `200 OK` |
| `DELETE` | `/api/debts/{id}` | Remove a debt by ID | `204 No Content` |
| `GET` | `/api/debts/total` | **Calculate and return the total accumulated amount and stats** | `200 OK` |

---

## ⚡ Getting Started

### Prerequisites
- **JDK 21** or higher installed and configured in your `PATH`.
- **Apache Maven 3.8+** (or use your IDE's embedded Maven runner).

### 1. Clone the repository
```bash
git clone https://github.com/your-username/java-debt-tracker-api.git
cd java-debt-tracker-api
```

### 2. Build the project
```bash
mvn clean package
```

### 3. Run the application
```bash
mvn spring-boot:run
```
The server will start at: `http://localhost:8080`

---

## 📖 Interactive Documentation & Database Console

- **Swagger UI (Interactive API Tester)**:  
  [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Schema (JSON)**:  
  [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **H2 Database Web Console**:  
  [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
  - **JDBC URL**: `jdbc:h2:mem:debtdb`
  - **User Name**: `sa`
  - **Password**: *(leave blank)*

---

## 📡 cURL Request Examples

### 1. Calculate Total Accumulated Debt Amount
```bash
curl -X GET "http://localhost:8080/api/debts/total" \
     -H "Accept: application/json"
```
**Sample Response (200 OK):**
```json
{
  "totalAmount": 1065.50,
  "totalDebtsCount": 4,
  "fixedDebtsCount": 2,
  "installmentDebtsCount": 2
}
```

---

### 2. Register a Recurring Fixed Debt (`FIXA`)
```bash
curl -X POST "http://localhost:8080/api/debts" \
     -H "Content-Type: application/json" \
     -d '{
       "description": "Apartment Rent",
       "amount": 1500.00,
       "type": "FIXA"
     }'
```
**Sample Response (201 Created):**
```json
{
  "id": 5,
  "description": "Apartment Rent",
  "amount": 1500.00,
  "type": "FIXA",
  "totalInstallments": null,
  "currentInstallment": null,
  "remainingInstallments": null
}
```

---

### 3. Register an Installment Debt (`PARCELADA`)
```bash
curl -X POST "http://localhost:8080/api/debts" \
     -H "Content-Type: application/json" \
     -d '{
       "description": "Smart TV 55 Inch",
       "amount": 320.00,
       "type": "PARCELADA",
       "totalInstallments": 10,
       "currentInstallment": 2
     }'
```
**Sample Response (201 Created):**
```json
{
  "id": 6,
  "description": "Smart TV 55 Inch",
  "amount": 320.00,
  "type": "PARCELADA",
  "totalInstallments": 10,
  "currentInstallment": 2,
  "remainingInstallments": 8
}
```

---

### 4. List All Registered Debts
```bash
curl -X GET "http://localhost:8080/api/debts" \
     -H "Accept: application/json"
```

---

### 5. Retrieve Debt by ID
```bash
curl -X GET "http://localhost:8080/api/debts/1" \
     -H "Accept: application/json"
```

---

### 6. Delete a Debt by ID
```bash
curl -X DELETE "http://localhost:8080/api/debts/1"
```
**Response:** `204 No Content`

---

## 🧪 Running Automated Tests

Run the complete test suite (Unit & MockMvc Integration tests) with:

```bash
mvn test
```

### Test Coverage Highlights:
- **`DebtServiceTest`**: Validates business rules, creation logic for fixed vs installment debts, validation error handling, deletion constraints, and total sum aggregation calculations.
- **`DebtControllerTest`**: Tests HTTP endpoints, status codes (`201`, `200`, `204`, `400`, `404`), payload serialization, and Bean Validation constraints.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
