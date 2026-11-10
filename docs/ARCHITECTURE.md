# Antigravity Vehicle Rental Service Platform - System Architecture

## 1. Architectural Overview
The Antigravity Vehicle Rental Service Platform is structured using a **Modular Layered Architecture (N-Tier)** implemented in Java 17+ with Spring Boot Web technologies. The design strictly prioritizes Object-Oriented Programming (OOP) paradigms and decouples data access via a dedicated, thread-safe File Storage Engine (`.txt` persistence) rather than a relational database.

```
+-----------------------------------------------------------------------------------+
|                            PRESENTATION TIER (UI)                                 |
|  - index.html (Fleet Catalog & Booking)    - rentals.html (Transaction & Return)  |
|  - reviews.html (Feedback & Ratings)       - admin.html (2FA Auth & Management)   |
|  - Bootstrap 5 + Custom Glassmorphism CSS  - Vanilla JS Fetch API Async Calls     |
+-----------------------------------------------------------------------------------+
                                       |
                                       | HTTP JSON REST API
                                       v
+-----------------------------------------------------------------------------------+
|                          APPLICATION / CONTROLLER TIER                            |
|  UserController | VehicleController | RentalController | ReviewController        |
+-----------------------------------------------------------------------------------+
                                       |
                                       | Method Calls (DTO / Domain Invocations)
                                       v
+-----------------------------------------------------------------------------------+
|                             BUSINESS LOGIC (SERVICE TIER)                         |
|  - UserService (Polymorphic Auth, Registration)                                   |
|  - VehicleService (Performance Scoring, Inventory, Pricing)                       |
|  - RentalService (Stock Check, Fine Calculation via Strategy Pattern)             |
|  - ReviewService (Credibility Weighting, Sentiment Aggregation)                   |
+-----------------------------------------------------------------------------------+
                                       |
                                       | Domain Entities
                                       v
+-----------------------------------------------------------------------------------+
|                                DOMAIN MODEL TIER                                  |
|  - User (AdminUser, RegularUser)                                                  |
|  - AntigravityVehicle (Hoverboard, FlyingCar)                                     |
|  - RentalTransaction, RentalStatus, FineCalculator (Regular, VIP)                |
|  - Review (PublicReview, VerifiedReview)                                          |
+-----------------------------------------------------------------------------------+
                                       |
                                       | Abstract CRUD Operations
                                       v
+-----------------------------------------------------------------------------------+
|                        DATA ACCESS / REPOSITORY TIER                              |
|  UserRepository | VehicleRepository | RentalRepository | ReviewRepository        |
+-----------------------------------------------------------------------------------+
                                       |
                                       | Synchronized Atomic I/O
                                       v
+-----------------------------------------------------------------------------------+
|                         PERSISTENCE ENGINE / STORAGE TIER                         |
|  - FileStorageUtil (ReentrantReadWriteLock, Atomic Move, UTF-8 File Stream)       |
|  - Flat Storage Files: users.txt, vehicles.txt, rentals.txt, reviews.txt          |
+-----------------------------------------------------------------------------------+
```

---

## 2. Workload Modularization Across 4 Team Members

| Component | Module Name | Team Member Responsibility | Key OOP Pillars Demonstrated |
| :--- | :--- | :--- | :--- |
| **Component 1** | User Management | Member 1 | Encapsulation in `User`, Inheritance (`AdminUser`, `RegularUser`), Polymorphic 2FA Authentication, File CRUD (`users.txt`). |
| **Component 2** | Antigravity Vehicle Fleet | Member 2 | Abstraction (`AntigravityVehicle`), Inheritance (`Hoverboard`, `FlyingCar`), Polymorphic Performance & Altitude Calculations, File CRUD (`vehicles.txt`). |
| **Component 3** | Rental & Return Transactions | Member 3 | Encapsulation (`RentalTransaction`), Polymorphic Strategy (`FineCalculator`, `RegularFineCalculator`, `VIPFineCalculator`), Stock Management, File CRUD (`rentals.txt`). |
| **Component 4** | Feedback & Review Management | Member 4 | Inheritance (`PublicReview`, `VerifiedReview` from `Review`), Polymorphic Credibility Weighting, Sentiment Scoring, File CRUD (`reviews.txt`). |

---

## 3. Concurrency and File Handling Architecture
Web containers handle requests concurrently across worker threads. Writing naively to flat files causes race conditions and file corruption.
To address this:
1. **ReentrantReadWriteLock Fair Locks**:
   - Multiple read requests execute concurrently with shared `ReadLock`.
   - Mutative writes acquire exclusive `WriteLock`.
2. **Atomic Temp File Swapping**:
   - `writeAllLines` writes complete records to a temporary buffer file (`.tmp`) first and replaces the main `.txt` file atomically via `Files.move(..., ATOMIC_MOVE)`, preventing partial data loss during system interruptions.

