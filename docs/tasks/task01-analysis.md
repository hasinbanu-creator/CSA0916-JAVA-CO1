# TASK 1: Understand the Problem and Identify the Objects

**Course:** Programming in Java (CO1)  
**Assignment Title:** Design, Implement and Validate an Object-Oriented Model for a Campus Smart-Parking System  
**Evaluation Component:** Task 1 (4 Marks)  
**Bloom's Taxonomy Level:** K4 – Analyse  

---

## 1. Problem Analysis

### 1.1 Problem Context
The university campus operates a multi-level parking facility intended for students, staff, and visitors. The system must manage parking operations cleanly as the facility scales:
$$\text{Phase 1: } 1 \text{ floor} \times 20 \text{ slots} \longrightarrow \text{Phase 2: } 3 \text{ floors} \times 50 \text{ slots} \longrightarrow \text{Phase 3: } 5 \text{ floors} \times 100 \text{ slots}$$

The facility accommodates three vehicle types:
- **Two-Wheelers**
- **Cars**
- **Electric Vehicles (EVs)** (which may use specialized charging slots charged separately; parking an EV in a regular slot does not incur a charging fee)

Users of the facility belong to distinct categories:
- **Staff and Students** holding a valid monthly pass park free of charge.
- **Visitors** pay the full parking fee.

### 1.2 Core Operations Required by the Assignment
According to the assignment briefing, the system must support two fundamental operational workflows:

1. **Vehicle Entry:**
   - Detect an arriving vehicle (capturing its registration number, vehicle type, and user credentials/pass).
   - Locate and allocate a suitable free parking slot on a floor.
   - Issue a parking ticket containing a unique ticket number.
   - Record the vehicle's entry time.
   - If no suitable slot is available, decline admission.

2. **Vehicle Exit:**
   - Locate the vehicle's parking record upon departure using its ticket or registration number.
   - Release the occupied slot so it becomes available for subsequent vehicles.
   - Compute the parking fee based on the vehicle type and duration of stay.
   - Apply fee exemptions: staff and students with a valid monthly pass park free of charge, whereas visitors pay the computed fee.
   - Charge separately for EV charging only when an EV utilizes a designated charging slot (an EV parked in a regular slot does not incur charging fees).

---

## 2. System Inputs and Outputs

The inputs and outputs strictly required by the assignment specification are structured as follows:

### 2.1 System Inputs
- **Vehicle Registration Number:** Alphanumeric identifier for the entering vehicle (e.g., `"TN01AB1234"`).
- **Vehicle Type:** Category of vehicle (`Two-Wheeler`, `Car`, or `Electric Vehicle`).
- **User Role / Pass Indicator:** Designation of the user (`Student`, `Staff`, or `Visitor`) and whether they hold a valid monthly pass.
- **Charging Slot Request (EV only):** Indication whether an entering electric vehicle requires a charging slot.
- **Timestamps:**
  - Entry time recorded when the ticket is issued.
  - Exit time recorded when the vehicle departs.

### 2.2 System Outputs
- **Parking Ticket (Issued on Entry):**
  - Unique ticket identifier (must never be reused).
  - Allocated location (floor number and slot number).
  - Vehicle registration number and vehicle type.
  - Recorded entry time.
- **Exit Summary / Fee Output (Produced on Exit):**
  - Released slot confirmation (floor number and slot number).
  - Total duration of parking.
  - Computed parking fee based on vehicle type and duration.
  - EV charging fee (applicable only if an electric vehicle occupies and utilizes a designated charging slot; ₹0.00 if parked in a standard slot or for non-EVs).
  - Net fee payable after applying monthly pass exemption (₹0.00 for valid pass holders; full fee for visitors).
- **Status / Error Notifications:**
  - Notification when no suitable free slot is available (parking full).
  - Notification when an invalid ticket or unrecorded vehicle is queried.

---

## 3. Candidate Classes and Methods (Noun-Verb Analysis)

In Object-Oriented Analysis, nouns extracted from the problem statement serve as candidates for classes and attributes, while verbs identify system operations and candidate methods.

### 3.1 Noun Extraction
- **Entities & Concepts Identified:** Campus, Parking Facility / Parking Lot, Floor, Slot, Vehicle, Two-Wheelers, Cars, Electric Vehicles (EVs), Charging Slots, Ticket / Parking Ticket, Entry Time, Exit Time, Duration, Fee, Staff, Student, Visitor, Monthly Pass.
- **Filtering:**
  - *Campus* represents the overarching environment and does not require an independent domain class.
  - *Entry Time*, *Exit Time*, *Duration*, and *Fee* represent attributes rather than separate classes.
  - The remaining nouns form our candidate classes.

### 3.2 Verb Extraction
- **Actions & Operations Identified:** Enter, Allocate slot, Issue ticket, Record entry time, Leave / Exit, Release slot, Compute parking fee, Check monthly pass, Charge EV.

### 3.3 Candidate Class Identification and Classification

We categorize candidate classes into **Essential Domain Classes** (strictly required by the core problem) and **Optional Supporting Types** (enumerations to enhance type safety).

| Candidate Class | Category | Primary Responsibilities | Key Candidate Methods (from Verbs) |
|---|---|---|---|
| **`Vehicle`** *(abstract)* | Essential Domain Class | Represents a generalized vehicle entering the campus; holds registration number and common attributes. | `getRegNo()`, `calculateFee(int durationMinutes)` |
| **`TwoWheeler`** | Essential Domain Class | Subclass of `Vehicle`; represents motorbikes/scooters with two-wheeler-specific fee calculation. | `calculateFee(int durationMinutes)` *(overridden)* |
| **`Car`** | Essential Domain Class | Subclass of `Vehicle`; represents four-wheelers with car-specific fee calculation. | `calculateFee(int durationMinutes)` *(overridden)* |
| **`ElectricVehicle`** | Essential Domain Class | Subclass of `Vehicle`; represents EVs; tracks whether charging is requested. | `calculateFee(int durationMinutes)` *(overridden)*, `isChargingRequested()` |
| **`ParkingSlot`** | Essential Domain Class | Represents an individual parking bay; tracks slot identifier, floor, occupancy state, and assigned vehicle. | `isAvailable()`, `occupy(Vehicle v)`, `vacate()` |
| **`ChargingSlot`** | Essential Domain Class | Proposed Subclass of `ParkingSlot` (Design Choice); represents a slot equipped with EV charging facilities. | `calculateChargingFee(int durationMinutes)` |
| **`ParkingFloor`** | Essential Domain Class | Represents a floor in the facility; manages an array of slots and counts available spaces. | `findFreeSlot(Vehicle v)`, `getFreeSlotCount()` |
| **`ParkingFacility`** | Essential Domain Class | The aggregate root representing the parking facility; coordinates multi-floor slot allocation, ticketing, and exit processing. | `allocate(String vehicleNo)` / `park(Vehicle v)`, `release(int floor, int slot)` / `exit(String ticketNo)` |
| **`ParkingTicket`** | Essential Domain Class | Represents the transactional record issued on entry; pairs a unique ticket number, vehicle, slot, and entry timestamp. | `getTicketNumber()`, `getEntryTime()`, `closeTicket(int exitTime)` |
| **`MonthlyPass`** | Essential Domain Class | Represents an access pass held by a student or staff member; verifies pass validity for fee exemptions. | `isValid()` |
| **`VehicleType`** | Optional Supporting Type | Enumeration of supported vehicle categories (`TWO_WHEELER`, `CAR`, `ELECTRIC_VEHICLE`). | Standard enum methods |
| **`UserRole`** | Optional Supporting Type | Enumeration of user categories (`STUDENT`, `STAFF`, `VISITOR`). | Standard enum methods |

---

## 4. Class Responsibilities and Necessity Justification

The assignment requires justifying why each candidate class is necessary, rather than merely listing names:

1. **`Vehicle` (and subclasses `TwoWheeler`, `Car`, `ElectricVehicle`):**
   - *Necessity:* Different vehicle categories incur different parking fees and have different slot requirements (e.g., EVs may require charging slots). Using an inheritance hierarchy allows polymorphic fee computation (`vehicle.calculateFee(duration)`) and encapsulates vehicle-specific data without cluttering external logic with conditional `switch` or `if-else` statements.

2. **`ParkingSlot` (and `ChargingSlot`):**
   - *Necessity:* A slot is more than an index in an array; it holds an occupancy state (free or occupied), a physical identity (floor and slot number), and a reference to the currently parked vehicle.
   - *Proposed Design Choice:* Modeling `ChargingSlot` as a specialized subclass inheriting from `ParkingSlot` is a proposed architectural design choice that encapsulates charging capabilities and separate charging fee computation without complicating the base `ParkingSlot` class.

3. **`ParkingFloor`:**
   - *Necessity:* Floors can have different numbers of slots (as explicitly highlighted in the briefing: from uniform floors to floors with different slot counts). Encapsulating slots on a per-floor basis allows the facility to manage capacity and localized slot search cleanly without exposing internal slot arrays.

4. **`ParkingFacility`:**
   - *Necessity:* Acts as the central controller for the system. It coordinates searching across multiple floors, issuing tickets, processing departures, and releasing slots, providing a unified interface for the application.

5. **`ParkingTicket`:**
   - *Necessity:* Decouples the vehicle from its temporary parking session. It provides an unalterable record tying together the unique ticket number, vehicle registration number, assigned slot, and entry time. When the vehicle exits, the ticket provides the ground truth needed to calculate duration and fees.

6. **`MonthlyPass`:**
   - *Necessity:* Separates the vehicle (a physical machine) from the user's institutional entitlement (free parking privilege). A staff member or student holds a pass; modeling `MonthlyPass` independently ensures that fee exemption logic is evaluated cleanly without hardcoding user roles directly onto the vehicle.

---

## 5. Business Rules and Stated Assumptions

### 5.1 Business Rules Explicitly Stated in the Assignment
Every valid system operation must satisfy the following invariant rules derived directly from the assignment specification:

1. **Slot Exclusivity:** A parking slot cannot hold two vehicles simultaneously. An occupied slot must not be allocated until it has been explicitly released.
2. **Ticket Identifier Non-Reuse:** Every parking ticket must have a unique ticket number, and a ticket number must never be reused across sessions.
3. **Suitable Slot Allocation:** When a vehicle enters, the system must allocate a suitable free slot matching the vehicle category (e.g., assigning EV charging slots to EVs requiring charging).
4. **Mandatory Entry Logging:** Entry requires recording the vehicle's entry time and issuing a ticket with a unique ticket number.
5. **Slot Release on Departure:** When a vehicle leaves, the system must release the assigned slot and recalculate available capacity.
6. **Fee Dependence:** Parking fees must depend directly on the vehicle type and the duration of parking.
7. **Monthly Pass Fee Exemption:** Staff and students holding a valid monthly pass park free of charge (fee = ₹0.00). Visitors pay the full computed parking fee.
8. **EV Charging Fee Application:** A separate charging fee applies only when an Electric Vehicle utilizes a designated charging slot. Electric vehicles parked in standard parking slots without using charging infrastructure do not incur this fee.

### 5.2 Stated Design Assumptions
To implement these rules cleanly, the following reasonable design assumptions are made and clearly distinguished from assignment requirements:

1. **Deterministic Slot Allocation (Assumption):** Slot allocation searches sequentially starting from Floor 0, Slot 0 upward (nearest free slot first).
2. **Time Measurement (Assumption):** Entry time and exit time are represented in elapsed minutes from a common reference point (or standard integer minutes), and exit time is always greater than or equal to entry time ($t_{\text{exit}} \ge t_{\text{entry}}$).
3. **Fee Calculation Slabs (Assumption):** While specific monetary rates are established in subsequent tasks (Tasks 3 and 7), fees are assumed to be non-negative ($\text{Fee} \ge 0.00$) and computed on an hourly/minute basis rounded up to the nearest billing unit.
4. **Ticket Presentation on Exit (Assumption):** A customer presents their valid ticket number upon departure to locate their active parking record.

---

## 6. Alternative Concept and Complexity Analysis

The assignment requires comparing the **Object-Oriented Design** (where each vehicle is modeled as a `Vehicle` object) with an alternative procedural design using **Parallel Arrays**:
```java
String regNo[];
String type[];
String entryTime[];
```

### 6.1 Finding a Vehicle Using Its Registration Number

#### In Parallel Arrays
Each vehicle's data is fragmented across three separate arrays at the same index $i$.
```java
// Linear search in parallel arrays
public int findVehicleParallel(String[] regNo, int count, String targetRegNo) {
    if (targetRegNo == null) return -1;
    for (int i = 0; i < count; i++) {
        if (targetRegNo.equalsIgnoreCase(regNo[i])) {
            return i; // Returns index i; attributes accessed via regNo[i], type[i], entryTime[i]
        }
    }
    return -1; // Not found
}
```

#### In the Object-Oriented Design
Each vehicle's attributes are encapsulated within a single cohesive `Vehicle` instance.
```java
// Linear search in an array of Vehicle objects
public Vehicle findVehicleOOP(Vehicle[] vehicles, int count, String targetRegNo) {
    if (targetRegNo == null) return null;
    for (int i = 0; i < count; i++) {
        if (vehicles[i] != null && targetRegNo.equalsIgnoreCase(vehicles[i].getRegNo())) {
            return vehicles[i]; // Returns the complete, encapsulated Vehicle object
        }
    }
    return null; // Not found
}
```

---

### 6.2 Adding a New Vehicle Attribute

Consider the requirement of adding a new attribute to vehicles, such as `hasPass` (boolean) or `isCharging` (boolean for EVs):

#### In Parallel Arrays
1. A completely new array (e.g., `boolean isCharging[]`) must be declared and instantiated alongside existing arrays.
2. Every method that manipulates vehicle data must have its signature modified to accept the new array:
   ```java
   allocate(String[] regNo, String[] type, String[] entryTime, boolean[] isCharging, ...)
   ```
3. Insertion, deletion, and compaction logic must be updated across all arrays in exact lockstep.
4. **Data Desynchronization Risk:** If an operation updates `regNo`, `type`, and `entryTime` but fails to update `isCharging` at index $i$, the arrays become misaligned. Vehicle records become permanently corrupted with no compile-time error.

#### In the Object-Oriented Design
1. The new attribute is added as a private field with getter/setter methods inside the `Vehicle` class, or strictly within the relevant subclass (e.g., `ElectricVehicle`):
   ```java
   public class ElectricVehicle extends Vehicle {
       private boolean chargingRequested;
       // Constructor and methods...
   }
   ```
2. Existing method signatures (`allocate(Vehicle v)`, `release(Vehicle v)`) remain completely unchanged.
3. Because all attributes of a vehicle are bound together within a single object on the heap, fields cannot become misaligned. Data consistency is enforced by the compiler and object encapsulation.

---

### 6.3 Time Complexity Analysis

Let $n$ denote the number of currently parked vehicles.

| Operation | Parallel Arrays Design | Object-Oriented Design | Discussion & Explanation |
|---|---|---|---|
| **Search by `regNo` (Best Case)** | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ | In both designs, the target vehicle happens to be located at the very first index ($i = 0$), requiring exactly 1 comparison. |
| **Search by `regNo` (Worst Case)** | $\mathcal{O}(n)$ | $\mathcal{O}(n)$ | In both designs, an unsorted sequential search must inspect all $n$ elements when the vehicle is at index $n-1$ or not present in the system. |
| **Search by `regNo` (Average Case)** | $\mathcal{O}(n)$ | $\mathcal{O}(n)$ | On average, an unsorted sequential search examines approximately $\frac{n}{2}$ elements before locating the target, which simplifies asymptotically to $\mathcal{O}(n)$. |
| **Vehicle Insertion** | $\mathcal{O}(1)$ amortized | $\mathcal{O}(1)$ amortized | Appending an entry at the next available index `count` takes constant time in both designs, assuming pre-allocated capacity. |
| **Vehicle Deletion / Slot Release** | $\mathcal{O}(n)$ to locate + $\mathcal{O}(1)$ or $\mathcal{O}(n)$ to clear | $\mathcal{O}(n)$ to locate + $\mathcal{O}(1)$ or $\mathcal{O}(n)$ to clear | **Important Clarification:** An $\mathcal{O}(1)$ reference nulling only applies *after* the target slot/index is already known. If the vehicle must first be searched by `regNo`, finding it takes $\mathcal{O}(n)$ time. Once located, clearing the position is $\mathcal{O}(1)$ if set to `null`, or $\mathcal{O}(n)$ if remaining elements must be shifted to keep the array contiguous. |

---

### 6.4 Space Complexity and Storage Overhead Analysis

#### Total Space Complexity
Both the parallel arrays design and the object-oriented design exhibit $\mathcal{O}(n)$ total storage complexity for $n$ parked vehicles, as both store references and data proportional to the total number of vehicle records.

#### Conceptual Storage and Overhead Comparison
Exact memory overhead in bytes depends on the specific JVM implementation, vendor, hardware architecture (32-bit vs. 64-bit), pointer compression settings (such as Compressed OOPs), and memory alignment/padding rules. Conceptually, the storage characteristics compare as follows:

- **Parallel Arrays:**
  - Requires maintaining $k$ separate array objects (for $k$ attributes: `regNo`, `type`, `entryTime`).
  - Each array holds $n$ reference slots pointing to individual `String` objects on the heap.
  - While parallel arrays avoid intermediate class instance wrappers, they introduce multiple array header structures and disperse logically related fields across disconnected arrays.
- **Object-Oriented Design:**
  - Requires a single array of references pointing to $n$ `Vehicle` instances.
  - Each `Vehicle` instance contains standard JVM object overhead (mark and class metadata words in the object header) and references to its attribute fields.
  - While the object-oriented approach incurs a modest per-object overhead depending on the JVM object layout, it organizes related attributes contiguously into a single encapsulated entity, greatly improving cohesion and cache locality.

---

### 6.5 Maintainability and Design Implications

| Quality Attribute | Parallel Arrays (`regNo[]`, `type[]`, etc.) | Object-Oriented Design (`Vehicle`) |
|---|---|---|
| **Encapsulation & Cohesion** | **Low Cohesion:** Attributes representing a single real-world vehicle are scattered across multiple independent arrays. | **High Cohesion:** All data and behaviors pertinent to a vehicle are encapsulated within a single logical unit. |
| **Type Safety** | **Weak:** Data is stored as primitive `String` arrays. Typos in vehicle types (e.g., `"Carr"` instead of `"Car"`) cannot be caught at compile time. | **Strong:** Leverages Java's static type system and inheritance to enforce correct types at compile time. |
| **Modularity & Extensibility** | **Poor:** Introducing a new vehicle category or attribute requires modifying every procedural function across the program. | **High (Open-Closed Principle):** New vehicle categories are added by introducing new subclasses without modifying existing code. |
| **Data Integrity** | **Vulnerable:** Reordering, sorting, or deleting an entry in one array without identically updating the others corrupts the system state. | **Protected:** Encapsulation helps protect object state and reduce the risk of inconsistent updates by bundling attributes together and controlling mutations through methods, preventing index desynchronization. |

---

## 7. Summary of Task 1 Findings

1. **Problem Modeling:** The campus parking system is successfully analyzed into core entities (`Vehicle`, `ParkingSlot`, `ParkingFloor`, `ParkingFacility`, `ParkingTicket`, `MonthlyPass`) and operations (allocation, release, fee computation).
2. **Business Rules:** Explicit constraints (slot exclusivity, unique non-reusable tickets, type compatibility, and pass exemptions) are identified and distinguished from implementation assumptions.
3. **Alternative Concept Defense:** While parallel arrays achieve the same asymptotic search time ($\mathcal{O}(n)$), they suffer from severe maintainability flaws, high risk of array desynchronization, and lack of type safety. The Object-Oriented design is strongly justified as the superior architecture for the Campus Smart-Parking System.
