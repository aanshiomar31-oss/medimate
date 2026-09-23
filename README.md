# MediMate - Pharmaceutical Supply Chain and Custody Verification System

[![Java](https://img.shields.io/badge/Java-11%20LTS-orange.svg?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Hibernate](https://img.shields.io/badge/ORM-Hibernate%20%2F%20JPA-59666C.svg?logo=hibernate&logoColor=white)](https://hibernate.org/)
[![Database](https://img.shields.io/badge/Database-H2%20(PostgreSQL--ready)-yellow.svg)](https://www.h2database.com/)
[![OpenAPI](https://img.shields.io/badge/API%20Docs-Swagger%203%20%2F%20OpenAPI-85EA2D.svg?logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui.html)


MediMate is an enterprise pharmaceutical supply chain tracking and audit service built with Java 11 and Spring Boot. It monitors drug batches from manufacturing through hospital delivery, validates custody transfers, tracks cold-chain storage temperatures, and detects distribution delays using process mining principles.

---

## Overview

In pharmaceutical distribution, medicines must be tracked accurately to prevent counterfeit drugs from entering pharmacies, prevent temperature-sensitive biologics (like insulin or vaccines) from spoiling, and ensure defective batches can be recalled quickly.

MediMate provides:
- A validated custody handover workflow for each drug batch.
- Temperature monitoring to detect cold-chain violations during transit.
- Anti-counterfeit verification to confirm genuine provenance.
- Process event logging to analyze transit times and operational bottlenecks.

---

## Core Supply Chain Workflow

Every medicine batch moves through five main stages before reaching the patient. If a safety defect is discovered, the batch can be quarantined immediately at any point.

```mermaid
flowchart LR
    A[1. Manufactured] --> B[2. Quality Tested]
    B --> C[3. Central Depot]
    C --> D[4. In Transit]
    D --> E[5. Delivered to Pharmacy]
    E --> F[6. Dispensed to Patient]

    A -. Defect .-> Q[Quarantine / Recall]
    B -. Defect .-> Q
    C -. Defect .-> Q
    D -. Defect .-> Q
    E -. Defect .-> Q
```

### Stage Explanations
1. **Manufactured**: The licensed pharmaceutical plant formulates and packages the batch.
2. **Quality Tested**: Laboratory testing verifies chemical purity and sterility before release.
3. **Central Depot**: The batch is stored in regional warehouses under controlled conditions.
4. **In Transit**: Refrigerated vehicles transport the batch; temperature dataloggers monitor conditions.
5. **Delivered to Pharmacy**: The hospital or retail pharmacy receives and inspects the shipment.
6. **Dispensed**: The medication is safely prescribed and dispensed to the patient.
7. **Quarantine / Recall**: Emergency hold triggered if safety defects or temperature violations occur.

---

## System Architecture

The application uses a standard three-tier architecture:

```mermaid
flowchart TD
    subgraph Users["Clients and Integrations"]
        UI["Web Dashboard"]
        Swagger["OpenAPI Swagger UI"]
        Clients["External Hospital Systems"]
    end

    subgraph Backend["Spring Boot Backend Service"]
        Controller["REST Controllers\n(Batch, Analytics, Medicine)"]
        Service["Service Layer\n(Custody, Analytics, Validation)"]
        Repository["Spring Data JPA Repositories"]
    end

    subgraph Data["Storage and Audit"]
        DB[("Relational Database\n(Batches, Medicines, Shipments)")]
        AuditLog[("Immutable Event Log\n(Custody History, Dwell Times)")]
    end

    Users --> Controller
    Controller --> Service
    Service --> Repository
    Repository --> DB
    Service --> AuditLog
```

---

## Anti-Counterfeit Verification Flow

When a pharmacist receives a medication batch, they query MediMate to verify its authenticity and safety before dispensing:

```mermaid
flowchart TD
    Start[Scan or Enter Batch Number] --> Query[Search Database for Batch]
    Query --> Exists{Batch Found?}
    
    Exists -- No --> Fake[Flag as Potential Counterfeit]
    Exists -- Yes --> CheckRecall{Is Batch Recalled?}
    
    CheckRecall -- Yes --> StopRecall[Reject: Batch Quarantined by Recall]
    CheckRecall -- No --> CheckTemp{Did Temperature Exceed Limits?}
    
    CheckTemp -- Yes --> AlertTemp[Warning: Temperature Excursion in Transit]
    CheckTemp -- No --> Safe[Approved: Authentic and Cold-Chain Compliant]
```

---

## Process Mining and Bottleneck Analysis

MediMate applies process mining concepts to help operations teams detect delays in the distribution pipeline.

### Event Log Structure
Each time custody of a batch changes, an immutable event record is created:
- **Case ID**: The batch number (for example, `MED-2026-PF01`).
- **Activity**: The stage change (for example, `IN_TRANSIT` to `DELIVERED_TO_PHARMACY`).
- **Timestamp**: The exact date and time of the handover.
- **Resource**: The custodian or carrier accepting responsibility.
- **Metrics**: Recorded temperature and dwell time in hours.

### Bottleneck Identification
By analyzing historical event logs across all batches, the analytics service automatically calculates:
- Average and maximum dwell hours spent at each stage.
- Service Level Agreement (SLA) breach rates per carrier or warehouse.
- Stalled shipments currently overdue for delivery.

---

## Application Interfaces

MediMate includes three built-in interfaces accessible when running locally:

| Interface | URL | Description |
| :--- | :--- | :--- |
| **Web Dashboard** | `http://localhost:8080/` | Interactive management dashboard with stage pipeline, batch verifier, and telemetry tables. |
| **OpenAPI / Swagger** | `http://localhost:8080/swagger-ui.html` | Interactive API documentation to inspect and test all REST endpoints. |
| **H2 Console** | `http://localhost:8080/h2-console` | Database management console to view relational tables (`jdbc:h2:mem:medimatedb`, user: `sa`). |

---

## REST API Reference

### Batch Custody and Verification
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/batches` | Register a new medicine production batch |
| `GET` | `/api/batches` | List batches with optional status and active filters |
| `GET` | `/api/batches/{id}` | Retrieve specific batch details by database ID |
| `POST` | `/api/batches/{id}/transfer` | Advance custody to the next stage with temperature logging |
| `POST` | `/api/batches/{id}/recall` | Trigger an emergency regulatory recall and quarantine |
| `GET` | `/api/batches/{batchNumber}/verify` | Verify batch authenticity and inspect the complete custody chain |

### Process Analytics
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/analytics/stalled-shipments` | List active shipments currently exceeding transit SLAs |
| `GET` | `/api/analytics/latency` | View average stage duration and SLA breach rates |
| `GET` | `/api/analytics/expiring-batches` | List batches approaching their expiration date |
| `GET` | `/api/analytics/dashboard` | Executive KPI health summary of the supply chain |

### Medicine and Partner Catalog
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/medicines` | List catalog medicines with cold-chain specifications |
| `POST` | `/api/medicines` | Register a new drug with required temperature thresholds |
| `GET` | `/api/manufacturers` | List licensed pharmaceutical manufacturers |
| `GET` | `/api/pharmacies` | List registered hospital and retail pharmacies |

---

## Relational Data Model

```mermaid
erDiagram
    MANUFACTURER ||--o{ MEDICINE_BATCH : produces
    MEDICINE ||--o{ MEDICINE_BATCH : defines
    MEDICINE_BATCH ||--o{ BATCH_CUSTODY_LOG : tracks
    MEDICINE_BATCH ||--o{ SUPPLY_SHIPMENT : ships_in
    PHARMACY ||--o{ SUPPLY_SHIPMENT : receives

    MEDICINE {
        Long id PK
        String name
        String genericName
        Double minTemperatureCelsius
        Double maxTemperatureCelsius
        Boolean requiresColdChain
    }

    MEDICINE_BATCH {
        Long id PK
        String batchNumber UK
        Integer initialQuantity
        LocalDate expiryDate
        BatchStatus status
        String currentLocation
        String currentHolder
        Boolean recalled
    }

    BATCH_CUSTODY_LOG {
        Long id PK
        String fromStatus
        String toStatus
        String toHolder
        String location
        Double recordedTemperature
        Double timeSpentHours
        Boolean slaBreached
        LocalDateTime timestamp
    }
```

---

## Automated Testing

The project includes unit and integration tests covering the state machine logic, custody handovers, cold-chain checks, and recall handling.

Run tests using the Maven wrapper:
```bash
./mvnw clean test
```

### Test Results
```text
Running com.medimate.MedimateApplicationTests
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

Running com.medimate.BatchStatusTest
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0

Running com.medimate.MediMateServiceIntegrationTest
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS (Total: 11 tests passed, 0 failures)
```

---

## Quickstart and Local Setup

### Prerequisites
- Java 11 LTS or higher
- Git

### 1. Clone the Repository
```bash
git clone https://github.com/aanshiomar31-oss/medimate.git
cd medimate
```

### 2. Run the Application
```bash
./mvnw spring-boot:run
```

Once started, the service is available at `http://localhost:8080/`.

---

