# TASK 1: Understand the Problem and Identify the Objects

**Course:** Programming in Java (CO1)  
**Assignment Title:** Design, Implement and Validate an Object-Oriented Model for a Campus Smart-Parking System  
**Evaluation Component:** Task 1 (4 Marks)  
**Bloom's Taxonomy Level:** K4 – Analyse  

---

## 1. Problem Analysis

### 1.1 Context and Domain Overview
The problem requires modeling and implementing a multi-level campus parking management system designed for a university environment. The facility serves three distinct user categories:
1. **Students** (may hold monthly passes or pay as visitors)
2. **Staff** (faculty and administrative personnel, typically holding monthly passes)
3. **Visitors** (guest vehicles paying per-hour standard tariffs)

The facility accommodates multiple vehicle categories:
- **Two-Wheelers** (motorcycles, scooters)
- **Cars** (internal combustion engine passenger cars)
- **Electric Vehicles (EVs)** (may require standard parking or specialized charging slots with supplementary metering)

### 1.2 Core Operational Workflows
The system orchestrates two primary workflows and one supplementary service:
1. **Vehicle Entry Workflow:**
   - Detect vehicle arrival, capture registration number, identify vehicle category, and record user category / pass credentials.
   - Search the parking facility for an appropriate, unoccupied parking slot matching the vehicle type and charging requirements.
   - If a suitable slot is found: mark the slot as occupied, generate an immutable parking ticket with a unique ticket number and timestamp, and admit the vehicle.
   - If no suitable slot is available across all floors: notify the driver that the facility is full and deny entry.
2. **Vehicle Exit and Billing Workflow:**
   - On departure, look up the active parking ticket using the ticket number or vehicle registration number.
   - Record the exit timestamp and compute the total parking duration in minutes/hours.
   - Compute the parking fee based on vehicle category fee slabs, user category waivers (e.g., free parking for valid monthly pass holders), and any EV charging consumption.
   - Release the occupied slot back to the pool of free slots.
   - Issue a detailed billing receipt and mark the ticket as closed.
3. **EV Charging Management:**
   - Monitor electricity consumption or charging duration for electric vehicles parked in charging-enabled slots, applying a separate charging tariff.

### 1.3 Scalability Requirements
The object-oriented design must seamlessly scale through the three phases specified in the briefing:
$$\text{Phase 1: } 1 \text{ floor} \times 20 \text{ slots} \longrightarrow \text{Phase 2: } 3 \text{ floors} \times 50 \text{ slots} \longrightarrow \text{Phase 3: } 5 \text{ floors} \times 100 \text{ slots}$$
Furthermore, the design must anticipate the Day-4 Design Challenge (Task 12), which introduces new vehicle categories (e-bikes, delivery vans) and novel pricing schemes (day passes, peak-hour surcharges) without requiring modifications to existing, tested classes (adhering to the Open-Closed Principle).

---

## 2. System Inputs and Outputs

```
+--------------------------------------------------------------------------------+
|                                INPUTS RECEIVED                                 |
+--------------------------------------------------------------------------------+
|  Entry Request:                                                                |
|    - Vehicle Registration Number (String, e.g., "TN01AB1234")                  |
|    - Vehicle Type (TwoWheeler / Car / ElectricVehicle)                         |
|    - User Category (Student / Staff / Visitor)                                 |
|    - Monthly Pass Identifier (Optional String, e.g., "PASS-STF-042")           |
|    - Charging Required Flag (Boolean, EV-specific)                             |
|    - Entry Timestamp (LocalDateTime / Epoch minutes)                           |
|                                                                                |
|  Exit Request:                                                                 |
|    - Ticket Number (Long / String, e.g., "TKT-10001")                          |
|    - Exit Timestamp (LocalDateTime / Epoch minutes)                            |
|    - Energy Consumed (kWh, EV-specific, if metered)                            |
|                                                                                |
|  Administrative & Query Requests:                                              |
|    - Floor query (floor number), Slot query, Vehicle lookup (by regNo)         |
+---------------------------------------+----------------------------------------+
                                        |
                                        v
                    +---------------------------------------+
                    |      Campus Smart-Parking System      |
                    +---------------------------------------+
                                        |
                                        v
+--------------------------------------------------------------------------------+
|                                OUTPUTS PRODUCED                                |
+--------------------------------------------------------------------------------+
|  Entry Confirmation / Parking Ticket:                                          |
|    - Unique Ticket Number (e.g., "TKT-10001")                                  |
|    - Vehicle Registration Number                                               |
|    - Vehicle Category & User Category                                          |
|    - Allocated Location: Floor Number, Slot Number                             |
|    - Entry Date & Time                                                         |
|                                                                                |
|  Exit Billing Receipt:                                                         |
|    - Ticket Number & Vehicle Registration Number                               |
|    - Entry Time, Exit Time, Total Duration (hours/minutes)                     |
|    - Base Parking Fee (calculated from tiered rate slabs)                      |
|    - Charging Surcharge (for EVs using charging infrastructure)                |
|    - Discount / Pass Waiver Applied (100% waiver for valid pass holders)       |
|    - Net Payable Amount (INR)                                                  |
|    - Slot Release Confirmation (Floor X, Slot Y marked VACANT)                 |
|                                                                                |
|  System Status / Alerts:                                                       |
|    - "PARKING FULL: No vacant slot available for vehicle category"             |
|    - "INVALID TICKET / VEHICLE NOT FOUND"                                      |
|    - Daily Collection & Occupancy Summary Reports                              |
+--------------------------------------------------------------------------------+
```

---

## 3. Candidate Classes and Methods (Noun-Verb Analysis)

In Object-Oriented Analysis (OOA), problem descriptions are systematically parsed: **nouns** represent candidate classes, attributes, or entities, while **verbs** represent operations, responsibilities, and member methods.

### 3.1 Noun Extraction (Candidate Classes & Attributes)
- **Problem Nouns:** Campus, Parking Facility / Parking Lot, Floor, Slot / Parking Slot, Vehicle, Two-Wheeler, Car, Electric Vehicle (EV), Charging Slot, Ticket / Parking Ticket, Entry Time, Exit Time, Duration, Fee / Tariff, Staff, Student, Visitor, Monthly Pass.
- **Filtering & Abstraction:**
  - *Campus* is the outer domain environment; modeled as the hosting application context.
  - *Parking Facility / Parking Lot* represents the central aggregate root entity.
  - *Floor* and *Slot* represent structural containment entities.
  - *Vehicle* represents an abstract entity with specialized subclasses (*TwoWheeler*, *Car*, *ElectricVehicle*).
  - *Ticket* encapsulates a transactional parking session.
  - *User / Monthly Pass* represents user roles and access entitlements.

### 3.2 Verb Extraction (Candidate Methods & Operations)
- **Problem Verbs:** Enter, Allocate slot, Issue ticket, Record entry time, Leave / Exit, Release slot, Compute duration, Calculate fee, Check pass / verify validity, Charge EV, Find vehicle.
- **Method Mapping:**
  - `allocateSlot(Vehicle v)` $\rightarrow$ `ParkingLot` / `ParkingFloor`
  - `issueTicket(Vehicle v, ParkingSlot s)` $\rightarrow$ `ParkingLot` / `TicketService`
  - `releaseSlot()` $\rightarrow$ `ParkingSlot`
  - `calculateFee(long durationMinutes)` $\rightarrow$ `Vehicle` / `BillingService`
  - `isValid()` $\rightarrow$ `MonthlyPass`
  - `chargeVehicle(double kWh)` $\rightarrow$ `ChargingSlot` / `Chargeable`

### 3.3 Candidate Class Summary Table

| Candidate Class | Classification / Role | Key Attributes | Key Methods (Derived from Verbs) |
|---|---|---|---|
| **`Vehicle`** *(abstract)* | Core Domain Entity (Superclass) | `regNo: String`<br>`type: VehicleType`<br>`owner: User` | `getRegNo(): String`<br>`getType(): VehicleType`<br>`calculateBaseFee(long minutes): double`<br>`equals(Object): boolean` |
| **`TwoWheeler`** | Specialised Entity (Subclass) | Inherited from `Vehicle` | `calculateBaseFee(long minutes): double` *(overridden)* |
| **`Car`** | Specialised Entity (Subclass) | Inherited from `Vehicle` | `calculateBaseFee(long minutes): double` *(overridden)* |
| **`ElectricVehicle`** | Specialised Entity (Subclass) | `batteryCapacity: double`<br>`isChargingRequested: boolean` | `calculateBaseFee(long minutes): double` *(overridden)*<br>`getChargingFee(double units): double` |
| **`ParkingSlot`** | Structural Entity | `slotId: int`<br>`floorNumber: int`<br>`isOccupied: boolean`<br>`currentVehicle: Vehicle`<br>`supportedType: VehicleType` | `isAvailable(): boolean`<br>`assignVehicle(Vehicle v): boolean`<br>`vacate(): Vehicle`<br>`getSlotDetails(): String` |
| **`ChargingSlot`** | Specialised Slot Entity (Subclass of `ParkingSlot`) | `chargingRatePerUnit: double`<br>`energyConsumedKwh: double` | `connectCharger(ElectricVehicle ev): void`<br>`disconnectCharger(): double`<br>`computeEnergyCost(): double` |
| **`ParkingFloor`** | Structural Aggregate | `floorNumber: int`<br>`slots: ParkingSlot[]`<br>`capacity: int`<br>`freeCount: int` | `findAvailableSlot(VehicleType type): ParkingSlot`<br>`getOccupancyRate(): double`<br>`displayFloorStatus(): void` |
| **`ParkingTicket`** | Transactional Value Object | `ticketNumber: String`<br>`vehicle: Vehicle`<br>`allocatedSlot: ParkingSlot`<br>`entryTime: LocalDateTime`<br>`isActive: boolean` | `getDurationMinutes(LocalDateTime exitTime): long`<br>`markCompleted(): void`<br>`getTicketNumber(): String` |
| **`User`** | Domain Entity | `userId: String`<br>`name: String`<br>`role: UserRole`<br>`pass: MonthlyPass` | `hasValidPass(): boolean`<br>`getUserRole(): UserRole` |
| **`MonthlyPass`** | Entitlement Value Object | `passId: String`<br>`holderId: String`<br>`expiryDate: LocalDate`<br>`isActive: boolean` | `isValid(): boolean`<br>`renew(int months): void` |
| **`ParkingLot`** | Aggregate Root / Orchestrator | `name: String`<br>`floors: ParkingFloor[]`<br>`activeTickets: ParkingTicket[]`<br>`ticketSequence: long` | `parkVehicle(Vehicle v): ParkingTicket`<br>`unparkVehicle(String ticketId): Bill`<br>`findVehicle(String regNo): ParkingSlot`<br>`getTotalCollection(): double` |
| **`Bill`** | Transactional Value Object | `billId: String`<br>`ticket: ParkingTicket`<br>`exitTime: LocalDateTime`<br>`duration: long`<br>`baseFee: double`<br>`chargingFee: double`<br>`netPayable: double` | `generateInvoiceSummary(): String`<br>`getNetPayable(): double` |

---

## 4. Class Responsibilities and Necessity Justification

To satisfy academic rigour and object-oriented design standards, every candidate class must have a defined, cohesive responsibility (Single Responsibility Principle) and a clear justification:

### 4.1 `Vehicle` (and Subclasses: `TwoWheeler`, `Car`, `ElectricVehicle`)
- **Responsibilities:** Encapsulates intrinsic vehicle state (registration number, category) and provides polymorphic behaviour for vehicle-specific fee calculations.
- **Justification:** Without a polymorphic `Vehicle` hierarchy, vehicle properties and tariffs would degrade into error-prone procedural conditional statements (`if-else` chains on raw strings) scattered across the codebase. Specialized subclasses allow new vehicle types (e.g., e-bikes, delivery vans in Task 12) to be plugged in without modifying existing code.

### 4.2 `ParkingSlot` (and `ChargingSlot`)
- **Responsibilities:** Models a single parking bay on a specific floor. Manages its own occupancy state, associates with the vehicle currently occupying it, and validates vehicle-slot compatibility.
- **Justification:** A slot is not merely a boolean flag in an array; it holds state (slot number, floor index, supported vehicle size, current occupant) and behaviour (occupancy verification, atomic allocation and release). `ChargingSlot` extends this with energy metering and specialized EV charging rates.

### 4.3 `ParkingFloor`
- **Responsibilities:** Represents an entire floor level. Contains a collection of `ParkingSlot` objects, tracks floor-level capacity and availability, and performs localized slot allocation searches.
- **Justification:** Encapsulating slots within a `ParkingFloor` abstracts floor-level dimensions from the outer system. When different floors have different capacities or slot configurations (as introduced in the 3-floor and 5-floor facility extensions), the floor encapsulates its specific topology without impacting top-level parking logic.

### 4.4 `ParkingTicket`
- **Responsibilities:** Serves as an immutable legal and temporal record linking a specific vehicle, an assigned slot, and an exact entry timestamp.
- **Justification:** The ticket decouples the vehicle and slot from the billing transaction. It prevents race conditions and ensures that billing relies on an unalterable audit trail. When a vehicle leaves, the ticket provides the ground truth for duration and fee calculations.

### 4.5 `MonthlyPass` and `User`
- **Responsibilities:** Models the driver's role (Student, Staff, Visitor) and holds pass validity dates.
- **Justification:** Separates the vehicle (a physical object) from the user's institutional entitlement (a business privilege). A single staff member may own different vehicles or carpool; associating fee waivers with the user/pass rather than hardcoding it into the vehicle ensures clean separation of concerns.

### 4.6 `ParkingLot`
- **Responsibilities:** Acts as the Aggregate Root (Façade) coordinating system-wide entry and exit operations, delegating slot searches across floors, generating unique ticket sequences, and tracking facility-wide metrics.
- **Justification:** Provides a single, clean API for client interactions (UI, console runners, or automated tests). It prevents clients from having to navigate raw floor and slot arrays directly.

---

## 5. System Business Rules

Every valid transaction executed by the system must satisfy the following invariant business rules:

1. **Rule 1: Slot Exclusivity**  
   A single parking slot can hold at most one vehicle at any given instant. A slot marked as occupied cannot be allocated to another vehicle under any circumstance.
2. **Rule 2: Unique, Non-Reusable Ticket Sequence**  
   Every issued parking ticket must have a strictly unique ticket number generated sequentially (e.g., using a monotonic counter). Ticket identifiers must never be recycled or reused across sessions, even after a vehicle has departed.
3. **Rule 3: Vehicle-Slot Type Compatibility**  
   Vehicles must only be assigned compatible slots:
   - A `Car` cannot be allocated to a compact `TwoWheeler` slot.
   - An `ElectricVehicle` requiring charging must be assigned to an available `ChargingSlot`; if charging is not required or no charging slots are vacant, it may use a standard car slot.
4. **Rule 4: One-to-One Active Parking Session**  
   A vehicle with a given registration number cannot be issued a new parking ticket if it already holds an active, unclosed ticket in the facility (prevents ghost/duplicate admissions).
5. **Rule 5: Monthly Pass Exemption & Verification**  
   Staff and student vehicles associated with a verified, active monthly pass are charged ₹0.00 for standard parking duration. If a pass has expired, the vehicle is subject to standard visitor tariff rates.
6. **Rule 6: Atomic Slot Release on Settlement**  
   A parking slot is released and returned to the pool of free slots if and only if the exit settlement is completed (ticket marked closed and fee accounted for).
7. **Rule 7: Non-Negative Duration and Slab Rounding**  
   Exit timestamp must be greater than or equal to entry timestamp ($t_{\text{exit}} \ge t_{\text{entry}}$). Duration calculation rounds up partial hours according to university fee slabs (e.g., any fraction of an hour is billed as a full hour).
8. **Rule 8: Non-Negative Fee Guarantee**  
   The computed parking fee must satisfy $\text{Fee} \ge 0.00$ under all circumstances. Discounts or pass waivers cannot result in negative billing.

---

## 6. Facility Assumptions

To establish bounded, realistic scope for the model, the following reasonable assumptions are adopted:

1. **Physical Layout:**
   - Floors are indexed sequentially from $0$ to $F-1$ (Ground Floor = Floor 0).
   - Each floor has a fixed contiguous numbering of slots from $0$ to $S_i - 1$.
   - Slot allocation follows a deterministic **"Nearest Slot First"** strategy: search begins at Floor 0, Slot 0 and proceeds sequentially upward.
2. **Temporal Representation:**
   - Time is measured in discrete minutes (either via integer simulation minutes or `java.time.LocalDateTime` truncated to minutes).
   - Vehicles park for non-negative durations within a single operational semester.
3. **Operational Mechanics:**
   - Entry and exit occur through single-lane entry/exit gates processed sequentially (thread concurrency synchronization can be layered subsequently if needed).
   - The ticket issued at entry is retained by the user and accurately presented at the exit gate.
4. **EV Charging Constraints:**
   - EV charging slots are located on designated floors (e.g., Ground Floor / Floor 0) for electrical infrastructure convenience.
   - Charging tariffs are computed per kilowatt-hour (kWh) or per hour plugged in, added on top of any non-waived parking fee.

---

## 7. Alternative Concept and Complexity Analysis

The assignment briefing requires evaluating the Object-Oriented Design (where each vehicle is modeled as a polymorphic `Vehicle` object) against a procedural alternative using **Parallel Arrays**:
```java
String regNo[];
String type[];
String entryTime[];
```

### 7.1 How Vehicle Search Operates in Both Designs

#### A. Parallel Arrays Approach
In the parallel array design, data for the $i$-th vehicle is distributed across three separate arrays at identical index $i$.
```java
// Parallel Arrays: Finding a vehicle by registration number
public int findVehicleParallel(String[] regNo, int count, String targetRegNo) {
    if (targetRegNo == null) return -1;
    for (int i = 0; i < count; i++) {
        if (targetRegNo.equalsIgnoreCase(regNo[i])) {
            return i; // Index gives vehicle location across all parallel arrays
        }
    }
    return -1; // Vehicle not found
}
```
To obtain the vehicle's attributes, the calling program must dereference the found index across all arrays: `regNo[idx]`, `type[idx]`, and `entryTime[idx]`.

#### B. Object-Oriented Design Approach
In the OOP design, each vehicle is an encapsulated instance of `Vehicle` (or stored inside a `ParkingTicket` / `ParkingSlot`).
```java
// Object-Oriented Design: Finding a vehicle in an array of objects
public Vehicle findVehicleOOP(Vehicle[] vehicles, int count, String targetRegNo) {
    if (targetRegNo == null) return null;
    for (int i = 0; i < count; i++) {
        if (vehicles[i] != null && targetRegNo.equalsIgnoreCase(vehicles[i].getRegNo())) {
            return vehicles[i]; // Returns cohesive, fully-typed domain object
        }
    }
    return null; // Not found
}
```

### 7.2 Impact of Adding a New Vehicle Attribute
Suppose the university introduces an attribute for electric vehicle tracking: `boolean isCharging` (or `double batteryCapacity`):

- **In the Parallel Array Design:**
  1. A new array `boolean isCharging[]` must be allocated alongside the existing arrays.
  2. Every method signature accepting vehicle data must be updated to accept the new array:  
     `park(String[] regNo, String[] type, String[] entryTime, boolean[] isCharging, ...)`
  3. Every insertion, deletion, compaction, or sorting operation must explicitly manipulate the new array in lockstep.
  4. If an insertion updates three arrays but crashes before updating the fourth, the arrays fall **out of synchronization**, causing catastrophic data corruption (index $i$ in array A no longer corresponds to index $i$ in array B).
- **In the Object-Oriented Design:**
  1. The new attribute is simply added as a private field in `Vehicle` (or strictly inside the `ElectricVehicle` subclass):
     ```java
     public class ElectricVehicle extends Vehicle {
         private boolean isCharging;
         // Constructor, getter, setter
     }
     ```
  2. Existing method signatures (`park(Vehicle v)`, `unpark(Vehicle v)`) remain completely untouched.
  3. Encapsulation prevents partial or desynchronized updates.

### 7.3 Time Complexity Analysis

Let $n$ denote the number of currently parked vehicles.

| Operation | Parallel Arrays Design | Object-Oriented Design | Justification / Analysis |
|---|---|---|---|
| **Search by `regNo` (Unordered)** | $\mathcal{O}(n)$ | $\mathcal{O}(n)$ | Both approaches perform a sequential linear scan checking up to $n$ elements in the worst case ($\frac{n}{2}$ on average). |
| **Search by `regNo` (Sorted)** | $\mathcal{O}(\log n)$ | $\mathcal{O}(\log n)$ | If maintained in sorted order of `regNo`, binary search takes $\mathcal{O}(\log n)$. However, maintaining sorted parallel arrays requires shifting $k$ arrays on every insertion/deletion ($\mathcal{O}(k \cdot n)$). |
| **Insert New Vehicle** | $\mathcal{O}(1)$ amortized | $\mathcal{O}(1)$ amortized | Appending at index `count` takes constant time in both, assuming pre-allocated capacity. |
| **Delete / Release Vehicle** | $\mathcal{O}(n)$ or $\mathcal{O}(1)$ | $\mathcal{O}(1)$ | In parallel arrays, removing index $i$ requires shifting $k$ arrays left to maintain contiguous indexing ($\mathcal{O}(k \cdot n)$), or setting entries to `null`. In OOP, nulling the slot reference is an immediate $\mathcal{O}(1)$ operation. |

### 7.4 Space Complexity and Memory Overhead Analysis

Both representations have an asymptotic space complexity of $\mathcal{O}(n)$ for storing $n$ vehicles. However, their actual **JVM heap footprint per vehicle** differs significantly due to object headers and reference structures.

Assume a standard 64-bit JVM with Compressed OOPs (Ordinary Object Pointers enabled, which is default for heaps $< 32\text{ GB}$):
- Pointer size: 4 bytes (with Compressed OOPs)
- Standard Object Header: 12 bytes (8-byte Mark Word + 4-byte Klass Word, padded to 8-byte boundary $\rightarrow$ 16 bytes)

#### Memory Layout per Vehicle (3 Attributes: `regNo`, `type`, `entryTime`):

```
PARALLEL ARRAYS HEAP BREAKDOWN (for n vehicles across 3 arrays):
  - Array header per array: 16 bytes x 3 = 48 bytes (fixed)
  - 3 reference slots in arrays: 3 x 4 bytes = 12 bytes per vehicle
  - Plus the referenced String objects on heap.

OBJECT-ORIENTED DESIGN HEAP BREAKDOWN (per Vehicle instance):
  - Vehicle Object Header: 12 bytes
  - 3 reference fields (regNo, type, entryTime): 3 x 4 bytes = 12 bytes
  - Padding to 8-byte boundary: 0 bytes (12 + 12 = 24 bytes, exact multiple of 8)
  - 1 pointer in ParkingLot's Vehicle[] array: 4 bytes
  - Total per vehicle (excluding String payload): 28 bytes
```

- **Analysis:**  
  While Parallel Arrays consume approximately 16 bytes less raw object overhead per entry ($12\text{ bytes}$ vs $28\text{ bytes}$), this negligible memory saving is completely overshadowed by the catastrophic loss of safety, modularity, and maintainability.

### 7.5 Design and Maintainability Comparison Matrix

| Criteria | Parallel Arrays Design | Object-Oriented Design (`Vehicle`) |
|---|---|---|
| **Encapsulation** | **Violated:** All arrays are exposed. Any code can modify `regNo[i]` without updating `type[i]`. | **Enforced:** Fields are `private`. Invariants are validated inside constructors and mutator methods. |
| **Type Safety** | **None:** Everything is stored as primitive `String`. Invalid types like `"Rocket"` can be inserted without compile-time checks. | **Strong:** Strong static typing via Java enums (`VehicleType`) or specialized subclasses (`TwoWheeler`, `Car`, `ElectricVehicle`). |
| **Extensibility (Open-Closed Principle)** | **Extremely Poor:** Adding an attribute or vehicle type requires editing every method handling the arrays. | **High:** New vehicle types subclass `Vehicle` and override specific behaviours without modifying existing code. |
| **Risk of Data Inconsistency** | **Severe:** Sorting, inserting, or deleting in one array without identically modifying the others corrupts the whole database. | **Zero:** An object is an atomic unit in memory. Moving, passing, or deleting the object moves all its fields together. |
| **Readability & Academic Value** | Procedural/Primitive obsession antipattern; difficult to trace and maintain. | Follows industry-standard SOLID principles and idiomatic Java clean code guidelines. |

---

## 8. Summary and Next Steps

Task 1 establishes the foundational object model for the **Campus Smart-Parking System**:
- The problem domain has been dissected into clear inputs, outputs, and candidate abstractions.
- Business invariants (slot exclusivity, non-reusable ticket sequence, type compatibility) are formalized.
- The Object-Oriented paradigm has been rigorously defended against the parallel-arrays anti-pattern using formal Big-O analysis and JVM memory layout considerations.

This document serves as the specification baseline for **Task 2 (Choose the Data Representation)**.

