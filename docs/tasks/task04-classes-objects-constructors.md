# TASK 4: Design Classes, Objects and Constructors

**Course:** Programming in Java (CO1)  
**Assignment Title:** Design, Implement and Validate an Object-Oriented Model for a Campus Smart-Parking System  
**Evaluation Component:** Task 4 (4 Marks)  
**Bloom's Taxonomy Level:** K4 – Analyse  

---

## 1. Class Definitions, Fields, and Methods

In Task 1, a noun-verb analysis identified candidate classes, their responsibilities, and key behaviors. In Task 4, we formalize these abstractions into cohesive Java class designs, specifying their attributes, data types, and method signatures while distinguishing **core domain classes** from **optional supporting types**.

### 1.1 Core Domain Classes

#### 1. `ParkingTicket`
- **Primary Responsibility:** Represents the transactional, audit-compliant parking record issued when a vehicle enters the facility. Decouples the physical vehicle and slot from the billing transaction.
- **Fields:**
  - `ticketNumber : String` — Unique alphanumeric ticket sequence.
  - `vehicleRegNo : String` — Alphanumeric license plate of the entering vehicle.
  - `vehicleType : String` — Category of the vehicle (`TWO_WHEELER`, `CAR`, `ELECTRIC_VEHICLE`).
  - `floorNumber : int` — Assigned facility floor index ($0 \dots F-1$).
  - `slotNumber : int` — Assigned slot index on the floor ($0 \dots S_f-1$).
  - `entryTime : LocalDateTime` — Point-in-time timestamp recorded upon admission.
  - `exitTime : LocalDateTime` — Departure timestamp recorded upon exit.
  - `isActive : boolean` — Binary session state (`true` while parked; `false` once closed).
- **Methods:**
  - `closeTicket(LocalDateTime exitTime) : void` — Finalizes the session and records the exit timestamp.
  - `getDurationMinutes() : long` — Computes elapsed stay in minutes using `Duration.between(entryTime, exitTime).toMinutes()`.
  - Getter accessors for all fields.

#### 2. `ParkingSlot`
- **Primary Responsibility:** Models an individual physical parking space, managing occupancy state and tracking the parked vehicle.
- **Fields:**
  - `slotNumber : int` — Zero-based index within the floor.
  - `floorNumber : int` — Level identifier.
  - `isOccupied : boolean` — True if occupied, false if vacant.
  - `currentVehicleRegNo : String` — Identifier of the parked vehicle (`null` if empty).
- **Methods:**
  - `isAvailable() : boolean` — Returns `!isOccupied`.
  - `assignVehicle(String vehicleNo) : boolean` — Claims the slot if free.
  - `vacate() : void` — Resets occupancy and clears the vehicle identifier.

#### 3. `ParkingFloor`
- **Primary Responsibility:** Encapsulates an array of `ParkingSlot` instances on a specific level, managing capacity and localized availability queries.
- **Fields:**
  - `floorNumber : int` — Floor level index.
  - `slots : ParkingSlot[]` — Array of slots specific to this floor.
  - `capacity : int` — Total slot count on this floor (`slots.length`).
- **Methods:**
  - `findFirstFreeSlot() : ParkingSlot` — Scans the floor for the first vacant slot.
  - `getFreeSlotCount() : int` — Counts currently vacant slots on this floor.

#### 4. `ParkingFacility`
- **Primary Responsibility:** The aggregate root controller orchestrating multi-floor slot allocation, ticketing, and exit release.
- **Fields:**
  - `slots : String[][]` (or `ParkingFloor[]`) — Multi-level slot structure.
- **Methods:**
  - `allocate(String vehicleNo) : int[]` — Locates the first free slot across floors in ascending order.
  - `release(int floor, int slot) : boolean` — Validates coordinates and frees the slot.

#### 5. `Vehicle` (and Subclasses `TwoWheeler`, `Car`, `ElectricVehicle`)
- **Primary Responsibility:** Represents the entering vehicle entity. Establishes the foundation for polymorphic fee computation in subsequent tasks.
- **Fields:**
  - `registrationNumber : String` — Unique alphanumeric license plate.
  - `vehicleType : String` — Vehicle category.
- **Methods:**
  - `getRegistrationNumber() : String`, `getVehicleType() : String`.
  - *(Subclass `ElectricVehicle` additionally holds `isChargingRequested : boolean`).*

#### 6. `MonthlyPass`
- **Primary Responsibility:** Represents an institutional parking entitlement held by eligible staff or students, verifying fee waiver eligibility.
- **Fields:**
  - `passId : String` — Unique pass identifier.
  - `holderRole : String` — Designation (`STUDENT` or `STAFF`).
  - `isValid : boolean` — Active entitlement status.
- **Methods:**
  - `isValid() : boolean`.

### 1.2 Optional Supporting Types
- **`VehicleType` & `UserRole` Enumerations:** Standard Java `enum` types providing compile-time type safety over raw strings, preventing typos during category comparisons.

---

## 2. Constructor Strategies

Constructors are specialized methods executed during object instantiation to allocate state, enforce mandatory invariants, and transition an object from uninitialized memory into a valid state.

### 2.1 Default / No-Argument Constructors
A **no-argument constructor** takes no parameters and initializes fields to language defaults (`0`, `null`, `false`):
```java
// Default / No-Argument Constructor Example
public class DefaultSlotDemo {
    private int slotNumber;
    private boolean isOccupied;

    public DefaultSlotDemo() {
        this.slotNumber = 0;
        this.isOccupied = false;
    }
}
```
- **When Appropriate:** Useful for generic frameworks, serialization, or simple containers where a meaningful default state exists (e.g., an unassigned slot).
- **When Inappropriate:** Dangerous for domain entities like `ParkingTicket` where identity attributes (`ticketNumber`, `vehicleRegNo`) are strictly mandatory. Leaving them `null` produces invalid ("half-baked") objects that violate business invariants.

### 2.2 Parameterized Constructors
A **parameterized constructor** accepts values for all required fields, validating them before assigning state:
```java
public ParkingTicket(String ticketNumber, String vehicleRegNo, String vehicleType,
                     int floorNumber, int slotNumber, LocalDateTime entryTime) {
    if (ticketNumber == null || ticketNumber.isBlank()) {
        throw new IllegalArgumentException("Ticket number cannot be null or blank.");
    }
    if (vehicleRegNo == null || vehicleRegNo.isBlank()) {
        throw new IllegalArgumentException("Vehicle registration number cannot be null or blank.");
    }
    if (floorNumber < 0 || slotNumber < 0) {
        throw new IllegalArgumentException("Floor and slot coordinates must be non-negative.");
    }
    this.ticketNumber = ticketNumber.trim();
    this.vehicleRegNo = vehicleRegNo.trim().toUpperCase();
    this.vehicleType = vehicleType.trim().toUpperCase();
    this.floorNumber = floorNumber;
    this.slotNumber = slotNumber;
    this.entryTime = (entryTime != null) ? entryTime : LocalDateTime.now();
    this.isActive = true;
}
```
- **Benefit:** Guarantees that no `ParkingTicket` object can ever be instantiated without valid mandatory attributes.

### 2.3 Overloaded Constructors and Constructor Chaining (`this(...)`)
Overloading provides multiple constructor signatures with different parameter lists to support common default scenarios.

#### The `this` Reference:
1. **Field Disambiguation:** Resolves shadowing when parameter names match instance field names:
   ```java
   this.floorNumber = floorNumber; // 'this.floorNumber' is the instance field; 'floorNumber' is the parameter
   ```
2. **Constructor Chaining (`this(...)`):** Invokes another constructor within the same class.

#### Why Constructor Chaining Avoids Repeated Logic:
Without chaining, validation rules (e.g., non-null checks, range validation) must be duplicated across every overloaded constructor. If a rule changes, every constructor must be modified. 

With constructor chaining, overloaded constructors delegate to the canonical constructor using `this(...)`. All validation and initialization logic is centralized in one place:

```java
// Overloaded Constructor 1: Defaults entryTime to current timestamp
public ParkingTicket(String ticketNumber, String vehicleRegNo, String vehicleType,
                     int floorNumber, int slotNumber) {
    this(ticketNumber, vehicleRegNo, vehicleType, floorNumber, slotNumber, LocalDateTime.now());
}

// Overloaded Constructor 2: Defaults vehicleType to "CAR" and entryTime to current timestamp
public ParkingTicket(String ticketNumber, String vehicleRegNo, int floorNumber, int slotNumber) {
    this(ticketNumber, vehicleRegNo, "CAR", floorNumber, slotNumber, LocalDateTime.now());
}
```
*Rule:* In Java, `this(...)` must strictly be the **first statement** in the constructor body.

---

## 3. Copy Constructors

### 3.1 Definition and Mechanics
A **copy constructor** is a constructor that creates a new object by copying the fields of an existing object of the same class:
```java
// General Copy Constructor Pattern
public ParkingTicket(ParkingTicket source) {
    if (source == null) {
        throw new IllegalArgumentException("Source ticket cannot be null.");
    }
    this.ticketNumber = source.ticketNumber;
    this.vehicleRegNo = source.vehicleRegNo;
    this.vehicleType = source.vehicleType;
    this.floorNumber = source.floorNumber;
    this.slotNumber = source.slotNumber;
    this.entryTime = source.entryTime;
    this.exitTime = source.exitTime;
    this.isActive = source.isActive;
}
```
> [!NOTE]
> Unlike C++, **Java does not provide an automatic default copy constructor**. If a copy constructor is needed, it must be explicitly declared and implemented by the developer.

### 3.2 Shallow Copy vs. Deep Copy
- **Shallow Copy:** Copies primitive field values directly and copies reference fields as memory addresses. If a reference points to a mutable object, both the original and copy share the same mutable instance.
- **Deep Copy:** Recursively clones or re-instantiates referenced objects so that the new object possesses completely independent copies of nested data structures.
- **Application to `ParkingTicket`:**  
  Because `String` and `LocalDateTime` are **immutable** in Java, copying their references is completely thread-safe and behaves with the safety of a deep copy without incurring the performance penalty of cloning.

### 3.3 Domain Analysis: Is Copying an Active Ticket Appropriate?
In a parking management system, blind copying of an active ticket violates **Rule 2 (Unique, Non-Reusable Ticket Sequence)** and **Rule 4 (One Active Ticket per Session)**:
- If a client copies an active ticket, two distinct ticket objects with the identical `ticketNumber` would exist simultaneously in memory.
- When vehicle departure is processed, the system could suffer duplicate fee charges or inconsistent slot release states.

#### Distinguishing Archival Snapshots from Reissued Tickets:
1. **Archival Snapshotting:**  
   Copying an existing ticket into an audit log or historical database after departure is valid, preserving historical identity for record-keeping.
2. **Reissuing a Lost Ticket:**  
   If a physical ticket is lost and must be re-issued, the new ticket must **never reuse the original ticket number**. The system must assign a **new unique ticket number** while transferring the session data:
   ```java
   // Reissue Constructor: Preserves session data while assigning a NEW unique ticket ID
   public ParkingTicket(String newTicketNumber, ParkingTicket original) {
       this(newTicketNumber,
            original != null ? original.vehicleRegNo : null,
            original != null ? original.vehicleType : null,
            original != null ? original.floorNumber : -1,
            original != null ? original.slotNumber : -1,
            original != null ? original.entryTime : null);
       if (original == null) {
           throw new IllegalArgumentException("Original ticket cannot be null.");
       }
       this.exitTime = original.exitTime;
       this.isActive = original.isActive;
   }
   ```

---

## 4. Object Memory and Lifecycle

### 4.1 JVM Conceptual Memory Model: Stack Frames and the Heap

In the standard JVM conceptual model:
- **Stack Memory:** Each execution thread maintains a call stack organized into stack frames. A stack frame stores local primitive variables, method arguments, and reference variables (pointers to heap objects).
- **Heap Memory:** A shared runtime data area where all class instances and arrays are dynamically allocated.

```
+------------------------------------------+        +-------------------------------------------------+
|               CALL STACK                 |        |                    JVM HEAP                     |
+------------------------------------------+        +-------------------------------------------------+
| [Stack Frame: main()]                    |        |                                                 |
|   ParkingTicket ticketRef --------------+-------> |  ParkingTicket Object Instance                  |
|   int floor = 0                          |        |  (Object Header: Mark Word + Klass Word)        |
|   int slot  = 1                          |        |  - ticketNumber  -----> "TKT-1001" (String)     |
|                                          |        |  - vehicleRegNo  -----> "TN01AB1234" (String)   |
|                                          |        |  - floorNumber   = 0                            |
|                                          |        |  - slotNumber    = 1                            |
|                                          |        |  - entryTime     -----> LocalDateTime Object    |
|                                          |        |  - isActive      = true                         |
+------------------------------------------+        +-------------------------------------------------+
```

> [!NOTE]
> **Conceptual Model vs. JVM Optimizations:**  
> While the conceptual model distinguishes stack-allocated references from heap-allocated objects, modern JVM execution engines (e.g., HotSpot with C2 JIT) perform advanced optimizations like **Escape Analysis**. If the JIT compiler proves that an object does not escape the allocating method, it may perform **Scalar Replacement**, breaking the object into primitive fields stored directly in CPU registers or on the stack frame without performing a physical heap allocation.

### 4.2 Object Allocation and Initialization Sequence
When `new ParkingTicket("TKT-1001", "TN01AB1234", "CAR", 0, 1)` executes, the JVM proceeds through a strict, deterministic lifecycle:

```
[1. Class Loading] 
       |---> Load ParkingTicket bytecode into Metaspace / Method Area if not already loaded.
[2. Heap Allocation]
       |---> Allocate memory block on the heap sized for object header + instance fields.
[3. Zero-Initialization]
       |---> Clear memory block: primitives set to 0/false, reference fields set to null.
[4. Superclass Construction]
       |---> Invoke superclass constructor (java.lang.Object constructor).
[5. Field Initializers & Instance Initializer Blocks]
       |---> Inline initializers execute in textual order (e.g., exitTime = null).
[6. Constructor Body Execution]
       |---> Chained this(...) executes, argument validation runs, fields assigned.
[7. Reference Publication]
       |---> Address of initialized heap object returned and assigned to local reference variable.
```

---

## 5. Alternative Concept and Complexity Analysis

The assignment briefing requires evaluating constructor chaining against two alternative instantiation patterns for creating `ParkingTicket` instances:
- **Approach A:** Parameterized Constructors with Constructor Chaining (`this(...)`)
- **Approach B:** Default (No-Arg) Constructor + JavaBeans-style Setter Calls
- **Approach C:** Static Factory Methods

---

### 5.1 Code Demonstrations for the Three Approaches

#### Approach A: Parameterized Constructors with Chaining
```java
public class TicketApproachA {
    private final String ticketId;
    private final String regNo;

    public TicketApproachA(String ticketId, String regNo) {
        if (ticketId == null || regNo == null) throw new IllegalArgumentException("Cannot be null");
        this.ticketId = ticketId;
        this.regNo = regNo;
    }

    public TicketApproachA(String ticketId) {
        this(ticketId, "UNREGISTERED");
    }
}
```

#### Approach B: Default Constructor + JavaBeans Setters
```java
public class TicketApproachB {
    private String ticketId; // Cannot be final!
    private String regNo;    // Cannot be final!

    public TicketApproachB() { } // Leaves fields null initially

    public void setTicketId(String ticketId) {
        if (ticketId == null) throw new IllegalArgumentException("Cannot be null");
        this.ticketId = ticketId;
    }

    public void setRegNo(String regNo) {
        if (regNo == null) throw new IllegalArgumentException("Cannot be null");
        this.regNo = regNo;
    }
}
```

#### Approach C: Static Factory Methods
```java
public class TicketApproachC {
    private final String ticketId;
    private final String regNo;

    // Private constructor enforces creation through factory methods
    private TicketApproachC(String ticketId, String regNo) {
        this.ticketId = ticketId;
        this.regNo = regNo;
    }

    public static TicketApproachC createTicket(String ticketId, String regNo) {
        if (ticketId == null || regNo == null) throw new IllegalArgumentException("Cannot be null");
        return new TicketApproachC(ticketId, regNo);
    }

    public static TicketApproachC createDefaultTicket(String ticketId) {
        return createTicket(ticketId, "UNREGISTERED");
    }
}
```

---

### 5.2 Architectural and Invariant Comparison

| Evaluation Feature | Approach A: Chained Constructors | Approach B: Default + Setters | Approach C: Static Factory Methods |
|---|---|---|---|
| **Object Validity Guarantee** | **Guaranteed:** Validation occurs prior to reference publication; an object cannot exist in an uninitialized state. | **Vulnerable:** Object is instantiated in an invalid empty state. If a setter is omitted or deferred, a "half-baked" object is exposed. | **Guaranteed:** Factory method performs validation before calling the private constructor. |
| **Immutability Support** | **Full Support:** All fields can be declared `final`, ensuring thread safety without synchronization. | **Impossible:** Fields cannot be `final` because mutator methods must alter state after construction. | **Full Support:** Fields can be declared `final`, guaranteeing immutability. |
| **Encapsulation & Mutability** | High: Object state is locked after creation. | Low: Exposed setters allow arbitrary state mutation at any point during runtime. | High: Private constructor shields instantiation logic from external callers. |
| **Readability & Expressiveness** | Standard positional arguments; can be confusing if multiple parameters share the same type. | Verbose; requires multiple method statements per ticket creation. | **Highest:** Factory methods have distinct descriptive names (e.g., `createTicket()`, `reissueTicket()`). |

---

### 5.3 Time and Space Complexity for Creating $n$ `ParkingTicket` Objects

Let $n$ denote the number of ticket instances created, each having a fixed number of fields $k$.

| Complexity Metric | Approach A: Chained Constructors | Approach B: Default + Setters | Approach C: Static Factory Methods | Technical Explanation |
|---|---|---|---|---|
| **Time per Ticket Creation** | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ | For a fixed number of fields $k$, all three approaches execute a finite, bounded sequence of instructions. |
| **Total Time for $n$ Tickets** | $\mathcal{O}(n)$ | $\mathcal{O}(n)$ | $\mathcal{O}(n)$ | Creating $n$ ticket instances requires strictly linear time across all three designs. |
| **Constant-Factor Call Overhead** | **Lowest:** 1 or 2 stack frames for constructor delegation. | **Higher Constant Factor:** Requires 1 constructor call plus $k$ distinct setter method calls ($k+1$ frames per ticket). | **Low:** 1 static method frame plus 1 constructor frame per ticket. |
| **Space per Ticket Instance** | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ | Fixed heap allocation size based on object header and $k$ field slots. |
| **Total Heap Space for $n$ Tickets** | $\mathcal{O}(n)$ | $\mathcal{O}(n)$ | $\mathcal{O}(n)$ | Total memory consumption on the heap scales linearly with the number of instances $n$. |

---

## 6. Conclusion and Justified Recommendation

For the **Campus Smart-Parking System**:
1. **Parameterized Constructors with Constructor Chaining (`this(...)`)** are recommended as the primary instantiation pattern. They enforce mandatory business invariants at the precise moment of creation, prevent uninitialized states, and support field immutability (`final`).
2. **Setter-based instantiation (JavaBeans)** is rejected for core transactional records like `ParkingTicket` because it permits partially initialized objects and prevents immutability.
3. **Copy constructors** should be strictly used for archival audit snapshots or paired with new ticket identifiers for reissuance, preventing ticket sequence collisions.

---

## 7. Verification and Test Results

A focused test suite was implemented in [`src/test/java/com/smartparking/model/ParkingTicketTest.java`](file:///Users/hasinnn/Documents/Java%20Assignment%201/src/test/java/com/smartparking/model/ParkingTicketTest.java) to verify all Task 4 constructor concepts.

### Maven Test Execution:
```bash
mvn test
```

### Actual Execution Output:
```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.smartparking.model.ParkingTicketTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.141 s -- in com.smartparking.model.ParkingTicketTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] [BUILD SUCCESS]
```

All 7 test cases passed without failure:
- `testCanonicalConstructorValidData`: Verified complete field initialization and normalization.
- `testOverloadedConstructorChaining`: Verified `this(...)` delegation with default timestamp.
- `testOverloadedConstructorDefaultingVehicleType`: Verified multi-tier chaining with default vehicle category.
- `testConstructorValidationRejectsInvalidInputs`: Verified rejection of null/blank identifiers and negative coordinates.
- `testArchivalCopyConstructor`: Verified independent heap reference and state preservation for snapshots.
- `testReissueCopyConstructorDistinctIdentity`: Verified session data reuse while assigning a new unique ticket ID.
- `testCloseTicketValidatesChronology`: Verified exit timestamp validation and duration computation.

