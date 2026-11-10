# Tweety Rentals - Antigravity Vehicle Platform

A university-grade Object-Oriented Programming (OOP) web application engineered in **Java 17+ / Spring Boot** and designed for seamless execution in **IntelliJ IDEA**.

---

## 💡 Plain English Guide: Explain Everything Very Simply

*Use this section to understand the entire project in 5 minutes and explain it easily to your professor or examiner.*

### 1. What does this project do in simple words?
Imagine a futuristic version of Hertz or Uber for sci-fi vehicles:
- Customers can browse and rent **Hoverboards** (lightweight, low-flying boards) and **Flying Cars** (high-altitude cruisers).
- Users can book a vehicle for a few hours. The system checks if the vehicle is in stock and locks it.
- When returning the vehicle, the system checks if the user is late. **VIP members** get special perks (grace period, zero extra admin fee), while **Regular members** pay a standard late penalty.
- Customers can write reviews. If a customer actually rented the vehicle, their review gets a **"Verified Pilot"** gold badge and carries more weight than a random public comment.
- Admins have a special dashboard where they must provide a **2FA Security Key** to log in, manage inventory, and add new futuristic vehicles.

---

### 2. How does the data work without a traditional database?
Instead of MySQL, Oracle, or MongoDB, everything is saved in **flat text files** inside the `data/` folder:
- `data/users.txt` — stores all user and admin accounts.
- `data/vehicles.txt` — stores all hoverboards and flying cars.
- `data/rentals.txt` — stores rental bookings and return history.
- `data/reviews.txt` — stores customer feedback and ratings.

#### How CRUD (Create, Read, Update, Delete) works on text files:
| Operation | What happens in plain English? |
| :--- | :--- |
| **Create** | Appends a new line of text at the end of the `.txt` file (separated by `\|` pipes). |
| **Read** | Opens the file, reads each line, splits it by `\|`, and turns each line into a Java object. |
| **Update** | Finds the line with the matching ID, replaces that whole line with the new data, and saves the file. |
| **Delete** | Reads all lines, filters out the line matching the ID you want to erase, and saves the rest back. |

#### Why do we have `FileStorageUtil.java`?
If two users click "Rent" at the exact same millisecond, two threads might try to write to `vehicles.txt` at once, which could corrupt the file. 
`FileStorageUtil` acts like a **traffic controller**:
- It uses **Read/Write Locks**: Multiple people can *read* the catalog at the same time, but when someone *writes*, it locks the file for a split second so nobody interferes.
- It uses **Atomic File Writing**: It writes to a temporary file (`.tmp`) first, and then renames it in one instant step, ensuring zero chance of half-written corrupted data.

---

### 3. The 4 Pillars of OOP Explained with Everyday Examples

#### A. Encapsulation (Protecting Data)
- **Analogy:** Think of a capsule pill or a bank ATM. You cannot reach inside the machine to change your bank balance directly; you must use a keypad with rules.
- **Where in Code?** In [`User.java`](file:///e:/ai%20agent/oop/src/main/java/com/antigravity/rental/model/user/User.java) and [`AntigravityVehicle.java`](file:///e:/ai%20agent/oop/src/main/java/com/antigravity/rental/model/vehicle/AntigravityVehicle.java):
  - Every variable is `private` (`private String password;`, `private double baseHourlyRate;`).
  - To change a value, you must go through a `setter` method that checks rules:
    - *Example:* `setUsername()` throws an error if the name has fewer than 3 characters.
    - *Example:* `setGravDampenerEfficiency()` throws an error if the efficiency is outside 0.1 to 1.0.

#### B. Inheritance (Reusing Shared Traits)
- **Analogy:** Children inherit traits from parents. A human parent has eyes and a name; the child gets those automatically and adds their own unique hobbies.
- **Where in Code?**
  - [`User`](file:///e:/ai%20agent/oop/src/main/java/com/antigravity/rental/model/user/User.java) is the parent. [`AdminUser`](file:///e:/ai%20agent/oop/src/main/java/com/antigravity/rental/model/user/AdminUser.java) and [`RegularUser`](file:///e:/ai%20agent/oop/src/main/java/com/antigravity/rental/model/user/RegularUser.java) are the children. They inherit `userId`, `username`, and `password` without rewriting them. `AdminUser` adds `adminKey` (2FA), while `RegularUser` adds `isVip` and `loyaltyPoints`.
  - [`AntigravityVehicle`](file:///e:/ai%20agent/oop/src/main/java/com/antigravity/rental/model/vehicle/AntigravityVehicle.java) is the parent. [`Hoverboard`](file:///e:/ai%20agent/oop/src/main/java/com/antigravity/rental/model/vehicle/Hoverboard.java) and [`FlyingCar`](file:///e:/ai%20agent/oop/src/main/java/com/antigravity/rental/model/vehicle/FlyingCar.java) are the children.

#### C. Polymorphism (One Command, Different Actions)
- **Analogy:** Think of the "Play" button on your computer. If you click Play on a video, it plays video. If you click Play on a song, it plays sound. Same button name, different behavior.
- **Where in Code?**
  1. **Authentication:** Both classes have `authenticate()`. When an admin logs in, it checks both password *and* 2FA security key. When a regular user logs in, it only checks the password.
  2. **Fine Calculation (Strategy Pattern):** Both fine calculators implement `calculateFine()`. If the user is VIP, it gives a **2-hour free grace period** and waives the $25 flat fine. If the user is Regular, it charges the $25 flat fine plus 200% hourly rate.
  3. **Vehicle Specs:** Both vehicles have `calculateMaxAltitudeMeters()`. Hoverboards return 8–15 meters (ground skimming), while Flying Cars return 2,000–8,000 meters (cloud cruising).

#### D. Abstraction (Hiding Complex Machinery)
- **Analogy:** When you drive a car, you push the gas pedal. You don't need to know the chemistry of the fuel injection or the math of the transmission.
- **Where in Code?**
  - `AntigravityVehicle` and `User` are marked `abstract`. You cannot create a generic "Vehicle" without specifying whether it is a `Hoverboard` or `FlyingCar`.
  - The rest of the app just calls `vehicle.calculatePerformanceScore()` without worrying about whether it's calculating quantum agility or warp engine shield formulas.

---

### 4. Cheat Sheet for the 4 Team Members (Viva Defense)

| Team Member | Module Owned | How to Explain Your Part in 30 Seconds |
| :--- | :--- | :--- |
| **Member 1** | **User Management** | *"I built the User module with `User`, `AdminUser`, and `RegularUser`. I implemented **Encapsulation** by making all fields private with strict validation setters. I used **Polymorphism** in `authenticate()` so Admin users require 2FA security tokens while Regular users use passwords, and I built full CRUD saving to `users.txt`."* |
| **Member 2** | **Vehicle Fleet** | *"I built the Vehicle module with the abstract `AntigravityVehicle` parent and `Hoverboard` and `FlyingCar` subclasses. I demonstrated **Abstraction** and **Polymorphism** by overriding `calculatePerformanceScore()` and `calculateMaxAltitudeMeters()`. My repository handles stock decrementing on rent and incrementing on return in `vehicles.txt`."* |
| **Member 3** | **Rental & Returns** | *"I built the Rental module with `RentalTransaction` and the **Strategy Pattern** for fines. I used **Polymorphism** with `FineCalculator`: VIP members get a 2-hour grace period and no flat fees, while standard users pay full penalties. I handle active booking status and purge completed logs in `rentals.txt`."* |
| **Member 4** | **Reviews & Ratings** | *"I built the Feedback module with `Review`, `PublicReview`, and `VerifiedReview`. I demonstrated **Polymorphism** with `calculateCredibilityWeight()`: verified renters get 100% weight and special badges, while public visitors get 45% weight. I calculate the mathematical weighted average rating and handle review deletion in `reviews.txt`."* |

---

## 🚀 Quick Start in IntelliJ IDEA

1. **Open in IntelliJ IDEA**:
   - Open IntelliJ IDEA -> `File` -> `Open...` -> Select folder `e:\ai agent\oop`
   - IntelliJ IDEA will automatically recognize the `pom.xml` and download dependencies.
2. **Run the Application**:
   - Navigate to `src/main/java/com/antigravity/rental/AntigravityRentalApplication.java`
   - Right-click and select **`Run 'AntigravityRentalApplication.main()'`**
3. **Open the Web Application**:
   - **Fleet Catalog & Booking**: [http://localhost:8080](http://localhost:8080)
   - **Rentals & Return Dispatch**: [http://localhost:8080/rentals.html](http://localhost:8080/rentals.html)
   - **Reviews & Ratings**: [http://localhost:8080/reviews.html](http://localhost:8080/reviews.html)
   - **System Administration Console**: [http://localhost:8080/admin.html](http://localhost:8080/admin.html)

---

## 📂 Project Structure

```
oop/
├── pom.xml                                   # Maven Build Configuration
├── data/                                     # Flat File Storage Database
│   ├── users.txt                             # Component 1 Storage
│   ├── vehicles.txt                          # Component 2 Storage
│   ├── rentals.txt                           # Component 3 Storage
│   └── reviews.txt                           # Component 4 Storage
├── docs/
│   ├── CLASS_DIAGRAM.puml                    # PlantUML Class Diagram
│   ├── ARCHITECTURE.md                       # System Architecture Document
│   └── VIVA_PREP_GUIDE.md                    # Viva Defense Questions & Answers
└── src/
    └── main/
        ├── java/com/antigravity/rental/
        │   ├── AntigravityRentalApplication.java # Spring Boot Entry Point
        │   ├── util/
        │   │   └── FileStorageUtil.java      # Thread-Safe File CRUD Engine
        │   ├── model/
        │   │   ├── user/                     # Component 1 Models (User, AdminUser, RegularUser)
        │   │   ├── vehicle/                  # Component 2 Models (AntigravityVehicle, Hoverboard, FlyingCar)
        │   │   ├── rental/                   # Component 3 Models (RentalTransaction, FineCalculator, VIPFineCalculator)
        │   │   └── review/                   # Component 4 Models (Review, PublicReview, VerifiedReview)
        │   ├── repository/                   # File-based CRUD Repositories
        │   ├── service/                      # Business & Polymorphic Logic Services
        │   └── controller/                   # REST API Controllers
        └── resources/
            ├── application.properties
            └── static/                       # Web Presentation Layer
                ├── index.html                # UI 1: Fleet Catalog & Booking Modal
                ├── rentals.html              # UI 2: Transaction History & Fine Preview
                ├── reviews.html              # UI 3: Sentiment & Trust Badges
                ├── admin.html                # UI 4: Back-Office 2FA & Fleet Management
                └── css/styles.css            # Futuristic Antigravity Theme
```

---

## 🎯 4-Way Team Division & Responsibilities

| Component | Member | Responsibilities | Key OOP Pillars |
| :--- | :--- | :--- | :--- |
| **Component 1** | Member 1 | User Management, 2FA Admin Authentication, Regular & VIP Users | Encapsulation, Inheritance, Polymorphic Auth |
| **Component 2** | Member 2 | Antigravity Fleet Management, Hoverboards vs Flying Cars | Abstraction, Inheritance, Dynamic Flight & Performance Formulas |
| **Component 3** | Member 3 | Rental & Return Transactions, Fine Strategy Pattern (VIP vs Regular) | Encapsulation, Strategy Pattern Polymorphism |
| **Component 4** | Member 4 | Customer Reviews & Feedback, Sentiment Aggregation, Moderation | Inheritance, Polymorphic Credibility Weighting |

# Tweetyrentals
