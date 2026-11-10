# University Viva Voce Preparation & Defense Guide
## Antigravity Vehicle Rental Service Platform

This document prepares each team member to defend their specific component during the oral viva session. It covers technical rationales, exact code references, and answers to common examiner questions.

---

### GENERAL SYSTEM & ARCHITECTURE QUESTIONS (For All Members)

**Q1: Why did you use file handling (.txt) instead of a relational database like MySQL or PostgreSQL?**
> **Answer:** "The university rubric required proving competence in fundamental Java I/O streams and file-based data structures without relying on database drivers or ORM automation. We engineered our custom `FileStorageUtil` class using `ReentrantReadWriteLock` to guarantee thread safety during concurrent HTTP requests, and utilized atomic file swapping (`StandardCopyOption.ATOMIC_MOVE`) to prevent file corruption during server failures."

**Q2: How is Information Hiding implemented across the project?**
> **Answer:** 
> 1. Fields in all model classes are declared `private`, accessible only via validated getters and setters.
> 2. `FileStorageUtil` hides the low-level OS file handles, buffers, and lock objects; repositories interact only through high-level CRUD primitives (`readAllLines`, `appendLine`, `updateLine`, `deleteLine`).
> 3. Repositories hide the underlying delimiter formatting (pipe `|`) and parsing logic from the services.

---

### COMPONENT 1: User Management (Defense for Member 1)

**Key Files Owned:**
- `User.java` (Abstract Base Class)
- `AdminUser.java` & `RegularUser.java` (Derived Classes)
- `UserRepository.java`, `UserService.java`, `UserController.java`

#### Viva Questions & Answers:
**Q1: Where is Encapsulation demonstrated in your User module?**
> **Answer:** "In `User.java`, all fields (`userId`, `username`, `email`, `password`, `role`) are marked `private`. In their mutator methods (e.g., `setUsername()`, `setEmail()`), I enforce data integrity validations—rejecting usernames shorter than 3 characters and validating email syntax. `setPassword()` validates length, ensuring sensitive credentials cannot be illegally altered."

**Q2: How does your component implement Inheritance?**
> **Answer:** "`AdminUser` and `RegularUser` extend the abstract base class `User` via the `extends` keyword. They inherit common state variables (`userId`, `username`, etc.) and constructor chaining via `super(...)`, while extending functionality: `AdminUser` adds `adminKey` and `department`, while `RegularUser` adds `isVip`, `loyaltyPoints`, and `phoneNumber`."

**Q3: Where and why is Polymorphism used in User Management?**
> **Answer:** "Polymorphism is demonstrated in the `authenticate(String password, String extraToken)` method. 
> - In `RegularUser`, single-factor authentication checks only the password.
> - In `AdminUser`, Two-Factor Authentication (2FA) is enforced; the method overrides the base signature to verify both the password AND an administrative security token.
> When `UserService.authenticate()` is called, Java uses dynamic method dispatch at runtime to execute the correct verification routine based on the actual object type in memory."

**Q4: How do the 4 CRUD operations work with `users.txt`?**
> **Answer:**
> - **Create:** `save(User)` appends a pipe-delimited record (`ADMIN|...` or `REGULAR|...`) via `FileStorageUtil.appendLine`.
> - **Read:** `findAll()`, `findById()`, and `findByUsername()` parse tokens and dynamically instantiate the appropriate concrete subclass (`AdminUser` or `RegularUser`).
> - **Update:** `update(User)` searches the file by `userId` and replaces the line.
> - **Delete:** `deleteById(userId)` removes the line matching the predicate.

---

### COMPONENT 2: Antigravity Vehicle Management (Defense for Member 2)

**Key Files Owned:**
- `AntigravityVehicle.java` (Abstract Base Class)
- `Hoverboard.java` & `FlyingCar.java` (Derived Classes)
- `VehicleRepository.java`, `VehicleService.java`, `VehicleController.java`

#### Viva Questions & Answers:
**Q1: Why is `AntigravityVehicle` an abstract class instead of an interface?**
> **Answer:** "`AntigravityVehicle` contains shared state attributes (`vehicleId`, `modelName`, `baseHourlyRate`, `stockQuantity`, `gravDampenerEfficiency`, `status`) and shared concrete behaviors (e.g., `getEffectiveHourlyRate()`, `isRentable()`). Abstract classes allow sharing both state and common implementation across subclasses while enforcing contracts for vehicle-specific physics via abstract methods."

**Q2: How do you demonstrate Polymorphism between `Hoverboard` and `FlyingCar`?**
> **Answer:** "We override two key calculation methods:
> 1. `calculatePerformanceScore()`: In `Hoverboard`, performance is calculated from rider agility, quantum stabilization, and weight tolerance. In `FlyingCar`, it is calculated from hyperdrive rating, passenger capacity, and deflector shielding.
> 2. `calculateMaxAltitudeMeters()`: `Hoverboard` returns a low-altitude hover ceiling (8m to 15m), whereas `FlyingCar` calculates high-altitude atmospheric flight levels (2,000m to 8,000m)."

**Q3: How does your code maintain vehicle stock during rentals?**
> **Answer:** "`VehicleRepository.updateStock(vehicleId, delta)` reads the vehicle record, updates the stock count (e.g. `-1` on rent, `+1` on return), and safely writes back to `vehicles.txt`. If stock reaches `0`, `isRentable()` returns `false`, preventing overselling."

---

### COMPONENT 3: Rental & Return Transactions (Defense for Member 3)

**Key Files Owned:**
- `RentalTransaction.java`, `RentalStatus.java`
- `FineCalculator.java` (Interface)
- `RegularFineCalculator.java` & `VIPFineCalculator.java` (Strategy Implementations)
- `RentalRepository.java`, `RentalService.java`, `RentalController.java`

#### Viva Questions & Answers:
**Q1: How did you implement Polymorphism in fine calculations?**
> **Answer:** "I implemented the **Strategy Pattern** through the `FineCalculator` interface.
> - `RegularFineCalculator`: Imposes a strict penalty consisting of a \$25 fixed administrative charge plus 2.0x hourly surcharge for overdue hours.
> - `VIPFineCalculator`: Grants a 2-hour free grace window, waives the fixed fee, and applies a discounted 1.25x surcharge.
> In `RentalService.processReturn()`, the user's tier is evaluated, and the fine calculation is dispatched polymorphically without branching complex nested `if-else` formulas in the transaction logic."

**Q2: How does the Rental module interact with Component 1 and Component 2?**
> **Answer:** "During `rentVehicle()`, `RentalService` queries `UserRepository` to verify the user exists, and `VehicleRepository` to check availability (`isRentable()`). It decrements the vehicle's inventory in `vehicles.txt`. Upon return, it calculates the fine, restores the stock (`+1`), and awards loyalty points to `RegularUser` instances."

---

### COMPONENT 4: Feedback & Review Management (Defense for Member 4)

**Key Files Owned:**
- `Review.java` (Abstract Base Class)
- `PublicReview.java` & `VerifiedReview.java` (Derived Classes)
- `ReviewRepository.java`, `ReviewService.java`, `ReviewController.java`

#### Viva Questions & Answers:
**Q1: How does Inheritance structure your Review module?**
> **Answer:** "`Review.java` serves as the abstract parent class encapsulating common attributes: `reviewId`, `vehicleId`, `userId`, `rating` (validated 1-5), `comment`, and `timestamp`. `PublicReview` extends it with community source data (`sourcePlatform`), while `VerifiedReview` extends it with validated rental proof (`rentalTransactionId` and `flightComfortScore`)."

**Q2: How is Polymorphism used in calculating sentiment scores?**
> **Answer:** "Each subclass overrides `calculateCredibilityWeight()`. 
> - A `PublicReview` carries a weight of `0.45` because no rental was verified.
> - A `VerifiedReview` carries a full credibility weight of `1.0`.
> In `ReviewService.calculateWeightedRating()`, the platform calculates a weighted average:
> $$\text{Weighted Rating} = \frac{\sum (\text{rating}_i \times \text{weight}_i)}{\sum \text{weight}_i}$$
> This ensures reviews backed by verified flight transactions have greater influence on the fleet's rating."

**Q3: How are inappropriate reviews moderated?**
> **Answer:** "`ReviewService.deleteReview(reviewId)` executes the Delete CRUD operation by delegating to `ReviewRepository.deleteById()`, which atomically cleanses the record from `reviews.txt`."

