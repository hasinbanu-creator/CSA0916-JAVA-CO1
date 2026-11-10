# TASK 10 – Packages, Access Modifiers, and Encapsulation (3 Marks)
**Module:** Programming in Java (CO1)  
**Academic Level:** Bloom’s Taxonomy Level K4 – Analyse  
**System Context:** Campus Smart-Parking System  
**Deliverables:**
- Documentation: `docs/tasks/task10-packages-access-encapsulation.md`
- PlantUML Model: `docs/uml/task10-packages-access-encapsulation.puml`
- Demonstration Program: `src/main/java/com/smartparking/tasks/EncapsulationDemo.java`
- Comprehensive Unit Tests: `src/test/java/com/smartparking/tasks/EncapsulationDemoTest.java`

---

## 1. Executive Summary and The Encapsulation Principle

**Encapsulation** is the foundational object-oriented mechanism of bundling data (state) and the methods that operate on that data into a single cohesive unit, while strictly restricting direct external access to the object's internal components.

In the **Campus Smart-Parking System**, encapsulation operates across two complementary architectural tiers:
1. **Macro-Level Encapsulation (Packages):** Groups functionally related classes into bounded namespaces (`model`, `service`, `app`, `tasks`), establishing clear architectural tiers and controlling cross-package visibility.
2. **Micro-Level Encapsulation (Access Modifiers & Mutators):** Employs access modifiers (`private`, package-private, `protected`, `public`) to conceal object representations, enforcing domain invariants through validated constructors, getters, and business operations.

Task 10 examines how packages, access modifiers, data validation, and modern Java wrapper types interact to guarantee object validity, prevent state corruption, and maintain clean software boundaries.

---

## 2. Package Architecture and Namespace Management

### 2.1 Why Packages Are Essential in Java
A Java **package** is a grouping mechanism that encapsulates related classes, interfaces, enumerations, and records. Packages address four vital software engineering needs:

1. **Namespace Collision Prevention:** In large software ecosystems, distinct modules frequently declare classes with identical simple names (e.g., `java.util.Date` vs. `java.sql.Date`, or `com.smartparking.model.ParkingTicket` vs. a municipal `gov.transit.ParkingTicket`). Packages provide Fully Qualified Class Names (FQCN), eliminating name collisions.
2. **Controlled Access Boundaries:** Packages establish a distinct access level—**default (package-private)**. Package-private members can collaborate freely within their common package while remaining completely invisible to external callers.
3. **Architectural Tiering and Cohesion:** Packages structure code into logical architectural tiers (Domain Model, Service Layer, Application Controller), promoting high cohesion and low coupling.
4. **Maintainability and Discoverability:** Developers can quickly locate classes and reason about system responsibilities without navigating an unorganized flat directory of source files.

### 2.2 The Campus Smart-Parking System Package Structure
The system establishes a clean, four-package hierarchy rooted under the standard reverse-domain namespace `com.smartparking`:

```
com.smartparking
├── model       -> Core domain entities, immutable value objects, and business contracts
├── service     -> Business logic, allocation workflows, and billing calculation engines
├── app         -> Application entry points, CLI runners, and system orchestration
└── tasks       -> Standalone academic demonstrations and deliverable verification suites
```

#### Detailed Package Responsibilities:

| Package | Intended Architectural Responsibility | Key Contained Types |
| :--- | :--- | :--- |
| **`com.smartparking.model`** | Encapsulates domain entities, state invariants, and behavioral contracts. Classes here represent core parking concepts. | `Vehicle`, `TwoWheeler`, `Car`, `ElectricCar`, `ParkingTicket`, `ParkingTariff`, `Chargeable` |
| **`com.smartparking.service`** | Encapsulates workflow controllers, allocation algorithms, fee computation services, and cross-entity coordination. | `ParkingService`, slot search engines, ticketing coordinators |
| **`com.smartparking.app`** | Encapsulates application entry points, configuration readers, and top-level execution launchers. | `CampusParkingApp`, system runner entry points |
| **`com.smartparking.tasks`** | Encapsulates academic task demonstrations, empirical experiments, and executable deliverables. | `ParkingFacility`, `InheritanceDemo`, `OverrideDemo`, `FeeDemo`, `AbstractionDemo`, `EncapsulationDemo` |

### 2.3 Package Declarations and Import Mechanics
In Java, package membership and import declarations follow strict compiler rules:
- **Package Declaration:** The first non-comment statement in a compilation unit must declare its package:
  ```java
  package com.smartparking.model;
  ```
- **Explicit Single-Type Import:** Imports a specific public type from another package:
  ```java
  import com.smartparking.model.ElectricCar;
  ```
- **On-Demand (Wildcard) Import:** Makes all public types in a package available without importing subpackages:
  ```java
  import com.smartparking.model.*;
  ```
- **Static Import:** Imports static members directly into the local namespace:
  ```java
  import static com.smartparking.model.ParkingTariff.CAR_RATE_PER_HOUR;
  ```

---

## 3. Access Modifiers: The Four Visibility Tiers

Java provides four distinct access levels to govern the visibility of classes, interfaces, constructors, methods, and fields.

### 3.1 The Four Access Levels Defined

1. **`private` (Class-Only):**
   - Visible **only within the exact class** where it is declared.
   - Private members cannot be viewed or modified by any other class—including subclasses and classes within the same package.
   - *Primary Purpose:* Maximum data hiding. Used for all mutable and immutable instance state (`registrationNumber`, `seatingCapacity`, `currentBatteryLevelKWh`, `slots[][]`).
2. **Default / Package-Private (No Modifier Specified):**
   - Visible to **any class residing within the same package**, but completely invisible outside the package.
   - Subclasses in different packages cannot access default members.
   - *Primary Purpose:* Package-internal collaboration. Used for test-reset hooks (`ParkingTicket.resetCounterForTesting()`) or package-internal factory helpers.
3. **`protected` (Package + Subclasses):**
   - Visible to **all classes within the same package**, plus **subclasses residing in external packages**.
   - Enables superclasses to expose internal extension points or helper utilities to derived classes without making them publicly accessible to unrelated callers.
   - *Primary Purpose:* Inheritance hooks. Used for `Vehicle.computeStartedHours()`, allowing `TwoWheeler` and `Car` to compute started hours while shielding the calculation from arbitrary external callers.
4. **`public` (Unrestricted Access):**
   - Visible to **all classes everywhere** (across all packages and modules).
   - *Primary Purpose:* Exposing the stable public API of the system (`Vehicle.getRegistrationNumber()`, `Vehicle.calculateFee()`, `Chargeable.charge()`).

### 3.2 Accessibility Comparison Matrix and UML Notation

| Access Modifier | UML Symbol | Within Same Class | Within Same Package | Subclass Outside Package | Unrelated Class Outside Package |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **`private`** | `-` | **YES** | NO | NO | NO |
| **Default** (package-private) | `~` | **YES** | **YES** | NO | NO |
| **`protected`** | `#` | **YES** | **YES** | **YES** | NO |
| **`public`** | `+` | **YES** | **YES** | **YES** | **YES** |

#### PlantUML Visibility Symbols Explained:
- **`-` (Minus / Private):** Accessible strictly within the declaring class body. Conveys total data hiding (e.g. `- registrationNumber`, `- slots`).
- **`~` (Tilde / Package-Private):** Accessible to all classes residing in the identical package, but completely concealed from external packages (e.g. `~ resetCounterForTesting()`).
- **`#` (Hash / Protected):** Accessible within the package and to derived subclasses across package boundaries (e.g. `# computeStartedHours()`).
- **`+` (Plus / Public):** Universally visible across all packages and modules (e.g. `+ calculateFee()`, `+ getTicketId()`).

### 3.3 Parking System Access Level Examples

```
+---------------------------------------------------------------------------------------+
| com.smartparking.model.Vehicle                                                        |
|  - private final String registrationNumber;           <-- Only Vehicle can access     |
|  # protected static long computeStartedHours(long m); <-- Subclasses can access       |
|  + public final String getRegistrationNumber();       <-- Everyone can access         |
+---------------------------------------------------------------------------------------+
       ^                                                ^
       | (extends in same package)                      | (imported from com.smartparking.tasks)
+-------------------------------+              +----------------------------------------+
| com.smartparking.model.Car    |              | com.smartparking.tasks.EncapsulationDemo|
|  - private int seatingCapacity|              |                                        |
|  + public int getSeating...() |              |  CAN access: public methods            |
+-------------------------------+              |  CANNOT access: private / protected    |
                                               +----------------------------------------+
```

---

## 4. Encapsulation: Data Hiding, Invariants, and Mutator Validation

### 4.1 The Peril of Public Fields (Why Direct Access Is Broken)
If domain entities declared public fields (e.g. `public int seatingCapacity;` or `public double currentBatteryLevelKWh;`):

```java
// ANTI-PATTERN: Direct public field access (No Encapsulation)
Car car = new Car();
car.registrationNumber = "";       // CORRUPTION: Blank license plate accepted!
car.seatingCapacity = -5;          // CORRUPTION: Physically impossible negative seats!

ElectricCar ev = new ElectricCar();
ev.batteryCapacityKWh = -100.0;    // CORRUPTION: Negative battery capacity!
ev.currentBatteryLevelKWh = 999.0; // CORRUPTION: Battery charged to 1000% capacity!
```

#### Catastrophic Consequences of Public Fields:
1. **Invariant Destruction:** External code can bypass all business rules, placing objects into corrupt, nonsensical states that trigger crashes downstream.
2. **Brittle Coupling:** Callers become tightly bound to internal storage representations. If `seatingCapacity` were changed from an `int` to a `byte` or an `Enum`, every caller in the entire project would break.
3. **No Auditability or Read-Only Guarantees:** A client could silently overwrite a vehicle's license plate or ticket timestamp after issue.

### 4.2 Data Hiding and Fail-Fast Validation
True encapsulation guarantees that **an object is valid upon construction and remains valid throughout its entire lifecycle**:

```java
// BEST PRACTICE: Private state protected by fail-fast constructor validation
public class Car extends Vehicle {
    private final int seatingCapacity; // Data Hiding: Inaccessible externally

    public Car(String registrationNumber, int seatingCapacity) {
        super(registrationNumber, "CAR"); // Delegates superclass validation
        if (seatingCapacity <= 0) {
            // Fail-Fast: Rejects corrupted state immediately at the boundary
            throw new IllegalArgumentException("Seating capacity must be strictly positive.");
        }
        this.seatingCapacity = seatingCapacity;
    }

    // Controlled Access: Read-only query
    public int getSeatingCapacity() {
        return seatingCapacity;
    }
}
```

### 4.3 Encapsulation Case Studies in the Parking System

1. **`Vehicle` (Immutable Identity):**
   - Fields `registrationNumber` and `vehicleType` are declared `private final`.
   - Normalizes strings to uppercase and rejects null/blank values during construction.
   - Because no setters exist and `String` is immutable, the legal identity of a vehicle cannot be altered once instantiated.
2. **`ParkingTicket` (Audit Trail Invariants):**
   - State fields (`ticketId`, `allocatedFloor`, `allocatedSlot`, `entryTimestamp`) are `private final`.
   - The ticket ID is generated sequentially from a private static counter (`TKT-1001`).
   - Callers can inspect the allocated slot via public getters, but cannot modify the timestamp or slot number, preventing ticket fraud.
3. **`ElectricCar` (Bounded State Transitions):**
   - Mutable field `currentBatteryLevelKWh` is declared `private`.
   - It cannot be modified directly; state changes occur strictly through `charge(double energyKWh)`.
   - `charge()` validates that input energy is positive, finite, and non-NaN, and uses `Math.min(capacity, current + energy)` to prevent overcharging beyond physical capacity.

---

## 5. Wrapper Classes and Autoboxing: Mechanisms and Pitfalls

### 5.1 Primitives vs. Wrapper Classes
Java maintains a dual type system comprising primitive value types and reference wrapper classes:

| Primitive Type | Size | Default Value | Wrapper Class | Immutable? | Heap Overhead |
| :--- | :---: | :---: | :--- | :---: | :---: |
| `byte` | 1 byte | `0` | `java.lang.Byte` | Yes | 16–24 bytes |
| `short` | 2 bytes | `0` | `java.lang.Short` | Yes | 16–24 bytes |
| `int` | 4 bytes | `0` | `java.lang.Integer` | Yes | 16–24 bytes |
| `long` | 8 bytes | `0L` | `java.lang.Long` | Yes | 24 bytes |
| `float` | 4 bytes | `0.0f` | `java.lang.Float` | Yes | 16–24 bytes |
| `double` | 8 bytes | `0.0d` | `java.lang.Double` | Yes | 24 bytes |
| `boolean` | 1 bit (effective 1 byte) | `false` | `java.lang.Boolean` | Yes | 16 bytes |
| `char` | 2 bytes | `'\u0000'` | `java.lang.Character` | Yes | 16–24 bytes |

### 5.2 Autoboxing and Unboxing Mechanics
- **Autoboxing:** The automatic conversion that the Java compiler makes between the primitive types and their corresponding object wrapper classes (e.g., converting an `int` to an `Integer`).
  ```java
  Integer boxedSlot = 101; // Compiler generates: Integer.valueOf(101)
  ```
- **Unboxing:** The automatic conversion of wrapper objects back to primitive values:
  ```java
  int slot = boxedSlot;    // Compiler generates: boxedSlot.intValue()
  ```

#### The Null Unboxing Hazard (`NullPointerException`):
If an uninitialized wrapper object is unboxed, the compiler attempts to invoke `.intValue()` on a `null` reference, triggering an instant `NullPointerException`:
```java
Integer unassignedSlot = null;
int slotId = unassignedSlot; // THROWS java.lang.NullPointerException!
```

### 5.3 Method Invocation Conversion: Unboxing Followed by Widening Primitive Conversion
In the parking system, a stay duration may be stored or passed as an `Integer` wrapper object (e.g., when retrieved from a collection `List<Integer>` or an external payload), whereas the fee calculation method `Vehicle.calculateFee(long durationMinutes)` formally requires a primitive `long`.

When passing an `Integer` object to `calculateFee(long)`:
```java
Integer boxedDuration = Integer.valueOf(120);
double fee = vehicle.calculateFee(boxedDuration);
```

Under the Java Language Specification (JLS §5.3 Method Invocation Conversion, §5.1.8 Unboxing Conversion, and §5.1.2 Widening Primitive Conversion), the Java compiler performs two sequential conversions:
1. **Unboxing Conversion (JLS §5.1.8):** The `Integer` object is first unboxed to a primitive `int` via `boxedDuration.intValue()`. Java cannot unbox an `Integer` directly to a primitive `long`, nor can a wrapper reference of type `Integer` be directly widened to a reference of type `Long`.
2. **Widening Primitive Conversion (JLS §5.1.2):** The unboxed primitive `int` is widened to primitive `long` without any loss of magnitude or precision.
3. **Fee Return as Primitive `double`:** The method `calculateFee(long)` processes the tariff calculation on the primitive `long` duration and returns the calculated parking fee as a primitive `double`.

---

## 6. The `Integer` Comparison Trap: Identity (`==`) vs. Equality (`.equals()`)

One of the most dangerous bugs in Java software engineering arises from using the `==` operator on wrapper objects.

### 6.1 The Java Language Specification Integer Cache (JLS §5.1.7)
Under the Java Language Specification, boxing conversions to `Integer` are required to return the **same object reference** for values strictly in the range **`-128` to `127` inclusive**. The HotSpot JVM implements this specification via an internal flyweight pool called `IntegerCache`.

> [!CAUTION]
> **The Integer Cache Boundary Trap (Crucially: The JVM Does NOT Cache All Integers):**  
> - **Cached Range (`[-128, 127]`):** For integer values between **`-128` and `127`**, `Integer.valueOf(x)` returns a pre-allocated reference from the flyweight pool. Comparing two boxed integers with `==` evaluates to **`true`** purely because both references happen to point to the exact same heap memory address.
> - **Non-Cached Range ($< -128$ or $> 127$):** **The JVM does NOT cache all integer values.** For integer values **$\ge 128$ or $\le -129$** (such as high slot number `200` or ticket ID counter `1001`), `Integer.valueOf(x)` allocates a **brand-new `Integer` instance on the heap** every single time. Comparing them with `==` evaluates to **`false`**, even though their numeric values are identical!
> - Relying on `==` for wrapper comparison creates brittle code that succeeds on small test inputs (`100 == 100` -> `true`) but silently fails in production with larger values (`200 == 200` -> `false`). Always use `.equals()` or compare primitives directly.

### 6.2 Demonstrating the Phenomenon with Parking Slot IDs

```java
// CASE A: Low-floor slot IDs within the cached range (-128 to 127)
Integer slotA = 100;
Integer slotB = 100;
System.out.println(slotA == slotB);      // PRINTS true (Coincidental reference identity!)
System.out.println(slotA.equals(slotB)); // PRINTS true (Value equality)

// CASE B: High-capacity slot IDs OUTSIDE the cached range (> 127)
Integer slotC = 200;
Integer slotD = 200;
System.out.println(slotC == slotD);      // PRINTS false! (CRITICAL BUG: Distinct heap objects!)
System.out.println(slotC.equals(slotD)); // PRINTS true  (Correct numeric equality)
```

### 6.3 Technical Root Cause Analysis

```
Integer Cache Pool (-128 to 127):
[Memory Address 0x10A0] -> Holds int value: 100
  ^              ^
  |              |
slotA          slotB  ==> (slotA == slotB) evaluates to TRUE (0x10A0 == 0x10A0)

General Heap (> 127):
[Memory Address 0x24C0] -> Holds int value: 200  <-- slotC points here
[Memory Address 0x38F0] -> Holds int value: 200  <-- slotD points here
                      ==> (slotC == slotD) evaluates to FALSE (0x24C0 != 0x38F0)
```

### 6.4 Recommended Engineering Best Practices
1. **Never use `==` to compare wrapper objects:** The `==` operator tests reference identity (memory addresses), which produces erratic behavior dependent on the cache boundary.
2. **Always use `.equals()`:** `Integer.equals()` extracts the primitive `intValue()` and compares numeric content:
   ```java
   if (slotA.equals(slotB)) { ... } // Safe, correct, and portable across all values
   ```
3. **Prefer primitive types for domain identifiers:** Unless a value must be stored in a generic collection (`List<Integer>`) or represent absence (`null`), always prefer primitive `int` or `long`. Comparing primitives with `==` evaluates numeric value directly without object allocation overhead.

---

## 7. Design Evaluation and Complexity Analysis

### 7.1 Architecture Trade-Off Analysis

| Architectural Quality | Encapsulated Design (Private Fields + Validated Accessors) | Procedural Design (Public Fields + Caller-Side Validation) |
| :--- | :--- | :--- |
| **Data Integrity** | **Guaranteed:** Objects can never be placed into an invalid state; validation is centralized. | **Fragile:** Any caller can bypass validation or forget checks, causing silent state corruption. |
| **Coupling** | **Loose:** Internal field types and representations can change without breaking clients. | **Tight:** Every client is directly bound to field names, types, and physical memory layout. |
| **Code Redundancy** | **Zero:** Validation logic exists in exactly 1 location (constructor or setter). | **Extreme:** The same `if (val > 0)` check must be duplicated across hundreds of call sites. |
| **Maintainability** | **High:** Modifying a business rule requires editing 1 class. | **Low:** Modifying a business rule requires hunting down and updating every caller across the codebase. |

### 7.2 Time and Space Complexity Analysis

#### Time Complexity:
- **Validation Overhead per Mutation:** Localized validation checks (e.g. `if (energy <= 0)`) execute in **$\mathcal{O}(1)$ constant time**, involving only a single branch condition.
- **Maintenance Complexity across $m$ Call Sites:**
  - With encapsulation: Centralized check requires $\mathcal{O}(1)$ maintenance effort when rules change.
  - Without encapsulation: Updating validation logic requires updating $m$ scattered call sites, incurring $\mathcal{O}(m)$ maintenance overhead and significant regression risk.

#### Space Complexity:
- **Primitive Storage:** A primitive `int` occupies **4 bytes** directly inside the object's memory layout.
- **Wrapper Object Overhead:** Storing an `Integer` object reference requires:
  - 4 bytes (Compressed OOP reference)
  - 16–24 bytes on the heap (Object Header + 4-byte `int` field + padding)
  - Autoboxing large numbers of integers in collections increases memory consumption by **$4\times$ to $6\times$** compared to primitive arrays (`int[]`).

---

## 8. Verification and Demonstration Output (`EncapsulationDemo.java`)

Execution via Maven:
```bash
mvn exec:java -Dexec.mainClass="com.smartparking.tasks.EncapsulationDemo"
```

Console Output:
```text
================================================================================
       Task 10: Encapsulation, Packages, and Access Modifiers Demonstration     
================================================================================

--- 1. Reading Encapsulated State via Public Getters ---
Vehicle Registration : TN03CC3333 (Immutable private final field)
Vehicle Category     : ELECTRIC_CAR
Battery Capacity     : 60.0 kWh
Initial Battery Level: 20.0 kWh
Car Seating Capacity : 5 seats

--- 2. Controlled State Mutation via Public Business Methods ---
Connecting EV to charger and delivering 25.0 kWh...
Charging Active      : true
Accepted Energy      : 25.0 kWh
Updated Battery Level: 45.0 / 60.0 kWh (75.0% SoC)
Charging Active      : false

--- 3. Validation Guards Protecting Object Invariants ---
[REJECTED] Negative stay duration (-15 min): Parking duration cannot be negative: -15
[REJECTED] Non-positive energy (-10.0 kWh) : Charging energy must be finite and strictly positive: -10.0
[REJECTED] Invalid seat count (-4 seats)   : Seating capacity must be strictly positive.
[REJECTED] Blank registration number ("")  : Vehicle registration number cannot be null or blank.

--- 4. Autoboxing and Unboxing in Parking Operations ---
Primitive stay minutes (120) autoboxed to Integer: 120
Integer duration unboxed to int, widened to long, returning double fee: Rs 60.0

--- 5. Wrapper Class Comparison: Integer '==' vs. '.equals()' ---
Cached Range (-128 to 127) - Low-floor Slot IDs:
  Integer slotA = 100, Integer slotB = 100
  slotA == slotB      : true  (Same cached flyweight reference)
  slotA.equals(slotB) : true  (Equal numeric value)

Outside Cached Range (> 127) - High-capacity Slot IDs:
  Integer slotC = 200, Integer slotD = 200
  slotC == slotD      : false (CRITICAL BUG RISK: Distinct heap objects!)
  slotC.equals(slotD) : true  (CORRECT PRACTICE: Compares numeric value)
================================================================================
```

---

## 9. Summary and Conclusion

Task 10 demonstrates that encapsulation is not merely about writing getters and setters; it is an architectural discipline of establishing strict boundaries:
1. **Packages** organize the Campus Smart-Parking System into clear, maintainable architectural tiers, shielding internal components from external interference.
2. **Access Modifiers** enforce least privilege, ensuring fields remain strictly `private`, inheritance hooks remain `protected`, and only deliberate contracts are made `public`.
3. **Data Validation** guarantees that objects fail fast at construction boundaries, preserving system-wide invariants.
4. **Wrapper Class Discipline** eliminates subtle identity bugs by mandating `.equals()` for numeric comparison and warning against null unboxing hazards.

