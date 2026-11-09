# TASK 3: Design the Control Logic

**Course:** Programming in Java (CO1)  
**Assignment Title:** Design, Implement and Validate an Object-Oriented Model for a Campus Smart-Parking System  
**Evaluation Component:** Task 3 (4 Marks)  
**Bloom's Taxonomy Level:** K4 – Analyse  

---

## 1. Overview and Problem Context

In Task 2, we established the data representation for the Campus Smart-Parking System, selecting a **jagged array (`String[][]`)** where each floor $f \in [0, F-1]$ contains an independently allocated array of slots with length $\text{slotsPerFloor}[f]$.

Task 3 designs, validates, and implements the fundamental **control logic** governing the facility's two core operations:
1. **Slot Allocation (`allocate`):** Deterministically locating the first free slot across floors in ascending order, placing the arriving vehicle, and terminating immediately.
2. **Slot Release (`release`):** Validating spatial boundaries, freeing occupied slots, and rejecting invalid or duplicate release attempts.
3. **Fee Slabs Decision Logic:** Designing decision structures (`if–else` and `switch`) for tiered duration billing while preserving precision and distinguishing illustrative models from final business rules.

---

## 2. Control Logic Design and Discussion

### 2.1 Search Strategy: Floor-by-Floor and Slot-by-Slot
The facility follows an ascending index search:
- The outer loop iterates through floor indices in ascending order: $f = 0, 1, \dots, F-1$.
- The inner loop iterates through slot indices on the current floor: $s = 0, 1, \dots, \text{slots}[f].\text{length}-1$.
- The algorithm returns the first free slot encountered according to array order ($f = 0$ upward, and $s = 0$ upward within each floor). This provides a predictable, deterministic allocation order matching the indices of the underlying jagged array.

### 2.2 First Available Slot Allocation and Immediate Termination
When searching for an available bay, the search must stop the moment an unoccupied slot (`slots[f][s] == null`) is encountered:
- The arriving `vehicleNo` is placed directly at `slots[f][s]`.
- An immediate `return new int[]{f, s}` exits both loops simultaneously.
- Immediate termination prevents unnecessary iteration over the remaining slots on the current floor and all subsequent floors.

### 2.3 Handling a Completely Full Parking Facility
If every slot on every floor has been inspected and no slot contains `null`, both loops complete naturally:
- Execution falls through the nested loop structure.
- The method returns `null` to signal that the facility is completely full.
- Returning `null` provides a clean sentinel value for the calling application (or gate display) to trigger a `"PARKING FULL"` notification and deny vehicle entry.

### 2.4 Control Flow Statements: Use of `return`, `break`, and `continue`
Understanding how control flow statements operate clarifies the implementation choices:
- **`return` in `allocate()`:**  
  In the `allocate()` method, `return` alone is used to immediately exit both nested loops simultaneously upon finding the first free slot (`return new int[]{f, s};`). This eliminates the need for boolean tracking flags or complex loop-breaking logic. If no slot is free, the trailing `return null;` executes after loops terminate.
- **`return` as Guard Clauses in `release()`:**  
  In `release()`, early `return false;` statements serve as guard clauses to reject invalid floor indices, invalid slot indices, or attempts to release an already-empty slot before modifying array state.
- **Roles of `break` and `continue` in Search Control:**  
  While neither `break` nor `continue` is strictly needed in the primary implementation of `allocate()` (since `return` exits directly), they serve distinct roles in looping logic:
  - `break` exits the innermost active loop early (for example, if searching within a single floor and stopping once a condition is satisfied).
  - `continue` skips the remainder of the current iteration and advances to the next iteration (for example, in a multi-tier search where a floor known to have zero free slots can be bypassed by issuing `continue` in the outer loop).

### 2.5 Fee Slabs Evaluation Using Decision Structures

Fee calculation evaluates parking duration against rate slabs according to vehicle type, user category, and optional service usage. In Java, this control logic is expressed using `if–else` decision chains or `switch` statements.

#### Illustrative Decision Structure vs. Assignment Specifications
> [!IMPORTANT]
> **Explicit Scope Boundary on Tariffs:**  
> The assignment specification explicitly states that parking fees depend on vehicle type and duration of stay, that eligible staff and students holding a valid monthly pass park free of charge, and that electric vehicles using charging slots are charged separately for charging. However, Task 3 does not establish official university tariff rates, charging billing units, or specific slab thresholds. Any started-hour rounding or tiered slab numbers presented below are strictly illustrative models used to explain the control logic.

#### Illustrative Tiered Slab Structure:
To demonstrate how decision branching manages progressive rates, consider an illustrative policy:
- **Tier 1 (First hour or part thereof):** Base rate applied
- **Tier 2 (Additional hours up to a threshold):** Incremental hourly rate applied
- **Tier 3 (Daily maximum):** Capped maximum charge

```java
// Illustrative Example of Decision Logic for Tiered Slabs
public static double calculateIllustrativeFee(int billedHours, double baseRate, 
                                              double hourlyRate, double dailyMax) {
    if (billedHours <= 0) {
        return 0.0;
    } else if (billedHours == 1) {
        // Tier 1: First hour flat base rate
        return baseRate;
    } else if (billedHours <= 8) {
        // Tier 2: Base rate plus incremental rate
        double fee = baseRate + (billedHours - 1) * hourlyRate;
        return Math.min(fee, dailyMax);
    } else {
        // Tier 3: Daily maximum cap
        return dailyMax;
    }
}
```

- **`if–else` Ladder:** Appropriate for continuous range conditions (e.g., duration thresholds like `billedHours <= 8`).
- **`switch` Expressions:** Appropriate for branching on known, discrete categorical inputs (e.g., `vehicleType`). Invalid categories should be rejected explicitly rather than assigned arbitrary default rates.

---

## 3. Algorithmic Pseudocode (SIMATS Engineering Standard)

Following the **SIMATS Engineering – Standard for Algorithmic Pseudocode**, algorithms are specified with formal headers, preconditions, postconditions, explicit data types, and structured control flow.

### 3.1 Algorithm: Slot Allocation (`AllocateSlot`)

```text
ALGORITHM AllocateSlot(slots, vehicleNo)
// Allocates the first available parking slot across floors in ascending order.
// Preconditions:
//   slots is a jagged 2D array of String representing floors and slots.
//   vehicleNo is a valid non-empty string representing vehicle registration.
// Postconditions:
//   Returns an integer array {floor, slot} and marks the slot occupied,
//   or returns NULL if all slots across all floors are occupied.

INPUT:
    slots       : ARRAY of ARRAY OF STRING
    vehicleNo   : STRING

OUTPUT:
    allocated   : ARRAY OF INTEGER [2] or NULL

VARIABLES:
    f, s, totalFloors, floorCapacity : INTEGER

BEGIN
    IF vehicleNo = NULL THEN
        RETURN NULL
    END IF

    totalFloors <- LENGTH(slots)

    // Search floor-by-floor in ascending order
    FOR f <- 0 TO totalFloors - 1 DO
        floorCapacity <- LENGTH(slots[f])
        
        // Search slot-by-slot in ascending order
        FOR s <- 0 TO floorCapacity - 1 DO
            IF slots[f][s] = NULL THEN
                // Free slot found: occupy slot and terminate immediately
                slots[f][s] <- vehicleNo
                RETURN ARRAY[f, s]
            END IF
        END FOR
    END FOR

    // Facility is completely full: no free slot available
    RETURN NULL
END
```

---

### 3.2 Algorithm: Slot Release (`ReleaseSlot`)

```text
ALGORITHM ReleaseSlot(slots, floor, slot)
// Validates coordinates and releases an occupied parking slot.
// Preconditions:
//   slots is a jagged 2D array of String.
//   floor and slot are integer coordinates.
// Postconditions:
//   Returns TRUE if the slot was occupied and successfully cleared.
//   Returns FALSE if coordinates are out of bounds or slot is already empty.

INPUT:
    slots : ARRAY of ARRAY OF STRING
    floor : INTEGER
    slot  : INTEGER

OUTPUT:
    success : BOOLEAN

VARIABLES:
    totalFloors, floorCapacity : INTEGER

BEGIN
    totalFloors <- LENGTH(slots)

    // Validate floor boundary
    IF floor < 0 OR floor >= totalFloors THEN
        RETURN FALSE
    END IF

    floorCapacity <- LENGTH(slots[floor])

    // Validate slot boundary on the specified floor
    IF slot < 0 OR slot >= floorCapacity THEN
        RETURN FALSE
    END IF

    // Check if slot is already vacant
    IF slots[floor][slot] = NULL THEN
        RETURN FALSE
    END IF

    // Free the slot
    slots[floor][slot] <- NULL
    RETURN TRUE
END
```

---

### 3.3 Algorithm: Fee Computation (`ComputeParkingFee`)

```text
ALGORITHM ComputeParkingFee(durationMinutes, vehicleType, userRole, hasMonthlyPass, isChargingSlotUsed, rates)
// Computes parking fee using duration, vehicle category, pass waiver, and separate charging status.
// Preconditions:
//   durationMinutes is a non-negative integer.
//   vehicleType is one of {"TWO_WHEELER", "CAR", "ELECTRIC_VEHICLE"}.
//   userRole is one of {"STUDENT", "STAFF", "VISITOR"}.
//   hasMonthlyPass is a boolean (true if holder has an active, valid pass).
//   isChargingSlotUsed is a boolean (true strictly if an EV occupied a charging bay).
//   rates is a configurable rates structure providing vehicle-type tariffs and charging rates.
// Postconditions:
//   Returns total non-negative fee payable (DOUBLE), or -1.0 if inputs are invalid.
// Note: Started-hour rounding and specific rate variables are illustrative models.

INPUT:
    durationMinutes    : INTEGER
    vehicleType        : STRING
    userRole           : STRING
    hasMonthlyPass     : BOOLEAN
    isChargingSlotUsed : BOOLEAN
    rates              : CONFIGURABLE_RATES

OUTPUT:
    totalFee           : DOUBLE

VARIABLES:
    billedUnits        : INTEGER
    baseRate           : DOUBLE
    parkingFee         : DOUBLE
    chargingFee        : DOUBLE

BEGIN
    // Step 1: Duration validation
    IF durationMinutes < 0 THEN
        RETURN -1.0 // Invalid negative duration: reject
    END IF

    IF durationMinutes = 0 THEN
        RETURN 0.0 // Zero duration stay: no fee incurred
    END IF

    // Step 2: Determine applicable base rate per vehicle type; reject unknown categories
    SWITCH vehicleType DO
        CASE "TWO_WHEELER":
            baseRate <- rates.twoWheelerRate
        CASE "CAR":
            baseRate <- rates.carRate
        CASE "ELECTRIC_VEHICLE":
            baseRate <- rates.electricVehicleRate
        DEFAULT:
            RETURN -1.0 // Unknown vehicle category: reject transaction
    END SWITCH

    // Step 3: Compute billable units (Illustrative assumption: started-hour ceiling rounding)
    billedUnits <- CEIL(durationMinutes / 60.0)

    // Step 4: Compute base parking fee with monthly pass exemption
    // Eligible staff and students holding a valid monthly pass park free of charge;
    // visitors and non-pass holders pay the full computed parking fee.
    IF (userRole = "STUDENT" OR userRole = "STAFF") AND hasMonthlyPass = TRUE THEN
        parkingFee <- 0.0
    ELSE
        parkingFee <- billedUnits * baseRate
    END IF

    // Step 5: Compute separate EV charging fee strictly when a charging slot is used
    chargingFee <- 0.0
    IF vehicleType = "ELECTRIC_VEHICLE" AND isChargingSlotUsed = TRUE THEN
        // Separate charging fee applies only upon active charging slot usage
        chargingFee <- billedUnits * rates.chargingRate
    END IF

    // Step 6: Return aggregate total fee
    totalFee <- parkingFee + chargingFee
    RETURN totalFee
END
```

---

## 4. Java Implementation and Execution Verification

### 4.1 Java Source Code: `ParkingFacility.java`
The program implements the jagged array structure and completes the `allocate()` and `release()` methods without modifying the assignment's exact `main()` method.

**File Location:** [`src/main/java/com/smartparking/tasks/ParkingFacility.java`](file:///Users/hasinnn/Documents/Java%20Assignment%201/src/main/java/com/smartparking/tasks/ParkingFacility.java)

```java
package com.smartparking.tasks;

/**
 * Task 3: Design the Control Logic
 * 
 * Campus Smart-Parking System - Basic Facility Model
 * Demonstrates decision and looping control logic for slot allocation
 * and release across a multi-level facility using a jagged array.
 */
public class ParkingFacility {
    // slots[floor][slot] holds a vehicle number, or null if the slot is free
    private final String[][] slots;

    public ParkingFacility(int[] slotsPerFloor) {
        slots = new String[slotsPerFloor.length][];
        for (int f = 0; f < slotsPerFloor.length; f++) {
            slots[f] = new String[slotsPerFloor[f]];
        }
    }

    /** Returns {floor, slot} of the first free slot, or null if it is full. */
    public int[] allocate(String vehicleNo) {
        // TODO 1: Search floor by floor and slot by slot; place vehicleNo in the first
        //   free slot and return {floor, slot}
        if (vehicleNo == null) {
            return null;
        }
        for (int f = 0; f < slots.length; f++) {
            for (int s = 0; s < slots[f].length; s++) {
                if (slots[f][s] == null) {
                    slots[f][s] = vehicleNo;
                    return new int[]{f, s};
                }
            }
        }
        return null;
    }

    /** Frees a slot. Returns false if the position is invalid or already free. */
    public boolean release(int floor, int slot) {
        // TODO 2: Validate floor and slot, free the slot and return true; otherwise
        //   return false
        if (floor < 0 || floor >= slots.length) {
            return false;
        }
        if (slot < 0 || slot >= slots[floor].length) {
            return false;
        }
        if (slots[floor][slot] == null) {
            return false;
        }
        slots[floor][slot] = null;
        return true;
    }

    public static void main(String[] args) {
        // floor 0 has 2 slots, floor 1 has 1 slot
        ParkingFacility p = new ParkingFacility(new int[]{2, 1});
        System.out.println(java.util.Arrays.toString(p.allocate("TN01AB1111")));
        System.out.println(java.util.Arrays.toString(p.allocate("TN02CD2222")));
        System.out.println(java.util.Arrays.toString(p.allocate("TN03EF3333")));
        System.out.println(java.util.Arrays.toString(p.allocate("TN04GH4444")));
        System.out.println(p.release(0, 1) + " " + p.release(0, 1) + " "
                + p.release(5, 0));
        System.out.println(java.util.Arrays.toString(p.allocate("TN04GH4444")));
    }
}
```

---

### 4.2 Compilation and Execution Verification

The program was compiled and executed via Maven and the standard Java runtime:
```bash
mvn test-compile && java -cp target/classes com.smartparking.tasks.ParkingFacility
```

#### Actual Console Output:
```text
[0, 0]
[0, 1]
[1, 0]
null
true false false
[0, 1]
```

#### Verification Against Assignment Specification:

| Execution Statement | Action Taken | Actual Output | Expected Output | Verification Status |
|---|---|---|---|---|
| `p.allocate("TN01AB1111")` | Allocates Floor 0, Slot 0 | `[0, 0]` | `[0, 0]` | **MATCH** |
| `p.allocate("TN02CD2222")` | Allocates Floor 0, Slot 1 | `[0, 1]` | `[0, 1]` | **MATCH** |
| `p.allocate("TN03EF3333")` | Floor 0 full; Allocates Floor 1, Slot 0 | `[1, 0]` | `[1, 0]` | **MATCH** |
| `p.allocate("TN04GH4444")` | Both floors full (all 3 slots occupied); returns null | `null` | `null` | **MATCH** |
| `p.release(0, 1)` | Frees valid occupied slot (Floor 0, Slot 1) | `true` | `true` | **MATCH** |
| `p.release(0, 1)` | Attempt duplicate release on already-free slot | `false` | `false` | **MATCH** |
| `p.release(5, 0)` | Attempt release on non-existent floor (out of bounds) | `false` | `false` | **MATCH** |
| `p.allocate("TN04GH4444")` | Re-attempts allocation; claims newly freed `[0, 1]` | `[0, 1]` | `[0, 1]` | **MATCH** |

---

## 5. Alternative Concept and Complexity Analysis

The assignment briefing requires analyzing an alternative design:
> *"Your allocation scans every floor and slot until a free one is found. If, instead, you maintained a count of free slots per floor and a stack of free slot numbers, what would the allocation and release logic become? Analyse the time complexity of allocate() and release() and the extra space needed in both approaches for F floors and S slots per floor."*

### 5.1 Baseline Design: Linear Scanning Over Jagged Array
- **Mechanism:**  
  Iterates sequentially through `slots[f][s]`. No auxiliary data structures are maintained.
- **`allocate()` Logic:**  
  Nested loops check each slot until a `null` is found.
- **`release()` Logic:**  
  Direct boundary checks and `slots[floor][slot] = null`.

### 5.2 Alternative Design: Free-Slot Counters + Per-Floor Stacks
- **Data Structures Added:**
  1. `int[] freeCount = new int[F];` — Tracks the number of free slots remaining on each floor.
  2. `Stack<Integer>[] freeSlots = new Stack[F];` — A stack for each floor storing available slot numbers on that floor.
- **Alternative `allocate(vehicleNo)` Logic:**
  1. Iterate through floors $f = 0 \dots F-1$ to find the first floor where $\text{freeCount}[f] > 0$.
  2. If all $\text{freeCount}[f] == 0$, return `null` immediately (facility full).
  3. Pop the next free slot from the floor's stack: $\text{slot} = \text{freeSlots}[f].\text{pop}()$.
  4. Decrement $\text{freeCount}[f]\text{--}$.
  5. Assign $\text{slots}[f][\text{slot}] = \text{vehicleNo}$.
  6. Return $\{f, \text{slot}\}$.
- **Alternative `release(floor, slot)` Logic:**
  1. Validate boundaries and check that `slots[floor][slot] != null`.
  2. Clear slot: `slots[floor][slot] = null`.
  3. Push slot number back onto the floor's stack: $\text{freeSlots}[\text{floor}].\text{push}(\text{slot})$.
  4. Increment $\text{freeCount}[\text{floor}]\text{++}$.
  5. Return `true`.

---

### 5.3 Time Complexity Comparison

Let $F$ denote the number of floors, $S$ denote the number of slots per floor, and $N = F \cdot S$ denote the total parking capacity across all floors.

| Operation | Baseline Linear Scan | Alternative (Count + Stacks) | Complexity Analysis & Assumptions |
|---|---|---|---|
| **`allocate()` — Best Case** | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ | **Baseline:** The very first inspected slot (`slots[0][0]`) is vacant; the loops terminate on the first iteration.<br>**Alternative:** The first floor counter ($\text{freeCount}[0] > 0$) indicates availability; a slot number is popped from the stack in $\mathcal{O}(1)$. |
| **`allocate()` — Worst Case** | $\mathcal{O}(F \times S)$ | $\mathcal{O}(F)$ | **Baseline:** The facility is completely full, or only the final slot (`slots[F-1][S-1]`) is vacant. All $F \times S$ positions must be examined.<br>**Alternative:** Inspects at most $F$ floor counters. Once an available floor is found, `pop()` retrieves a free slot in $\mathcal{O}(1)$. Reduces worst-case search time from $\mathcal{O}(F \times S)$ down to $\mathcal{O}(F)$. |
| **`allocate()` — Average Case Analysis** | $\mathcal{O}(F \times S)$ | $\mathcal{O}(F)$ | *Assumption:* If vacant slots are assumed to be uniformly distributed across the facility, linear scanning inspects a fraction of total slots proportional to $F \times S$, maintaining asymptotic $\mathcal{O}(F \times S)$ complexity. Similarly, scanning floor counters takes time proportional to $F$, yielding $\mathcal{O}(F)$. Under non-uniform empirical traffic patterns, worst-case upper bounds remain the formal benchmark. |
| **`release()`** | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ | **Baseline:** Validates coordinates and resets `slots[floor][slot] = null`. Strictly constant time.<br>**Alternative:** Validates coordinates, clears slot, pushes the slot number onto the floor's stack in $\mathcal{O}(1)$, and increments the floor counter in $\mathcal{O}(1)$. Strictly constant time, assuming valid stack operations and prevention of duplicate releases. |

---

### 5.4 Space Complexity and Auxiliary Memory Overhead

| Metric | Baseline Linear Scan | Alternative (Count + Stacks) |
|---|---|---|
| **Primary Slot Storage** | $\mathcal{O}(F \times S)$ (`String[][]`) | $\mathcal{O}(F \times S)$ (`String[][]`) |
| **Extra Auxiliary Space** | $\mathcal{O}(1)$ (Zero auxiliary tracking structures) | $\mathcal{O}(F \times S)$ auxiliary memory |
| **Auxiliary Memory Breakdown** | None. Space is limited to local loop variables $f$ and $s$ on the call stack. | - 1 array of integers for floor counters: `int[F]` $\rightarrow \mathcal{O}(F)$<br>- 1 array of Stack references: `Stack[F]` $\rightarrow \mathcal{O}(F)$<br>- $F$ `Stack` instances containing up to $F \times S$ Integer references $\rightarrow \mathcal{O}(F \times S)$ |

---

### 5.5 Trade-offs, Ordering Implications, and Design Evaluation

1. **Time vs. Space Trade-off:**  
   The alternative design trades $\mathcal{O}(F \times S)$ auxiliary memory to reduce allocation time from $\mathcal{O}(F \times S)$ to $\mathcal{O}(F)$. For a large facility with many slots per floor (e.g., $F = 5, S = 100$), searching at most 5 counters is substantially faster than scanning up to 500 slot references.
2. **Loss of Lowest-Slot-First Ordering (LIFO Behavior):**  
   - In the **baseline linear scan**, allocation always selects the first free slot according to array order (lowest floor index, lowest slot index).
   - In the **stack-based alternative**, a Stack is Last-In, First-Out (LIFO). When an occupied slot is released, its slot number is pushed onto the top of that floor's stack. The subsequent allocation will pop that most-recently released slot first—even if a lower-numbered slot on the same floor was already vacant.
   - Therefore, a stack-based approach **does not preserve lowest-slot-first ordering** once releases occur. If ascending slot order must be strictly preserved, an ordered data structure (such as a Min-Heap or an ordered bitset) would be required, which would increase release time from $\mathcal{O}(1)$ to $\mathcal{O}(\log S)$.

---

## 6. Summary of Task 3 Deliverables

1. **Control Logic Documented:** Floor-by-floor search, allocation termination, and guard-clause release validation fully specified.
2. **Algorithmic Pseudocode:** Formal SIMATS Engineering standard pseudocode developed for allocation, release, and fee computation.
3. **Working Java Program:** `ParkingFacility.java` successfully implemented, compiled, and executed with exact output verified.
4. **Alternative Concept Evaluated:** Rigorous Big-O time and space trade-off analysis completed comparing linear scanning with stack/counter caching.

