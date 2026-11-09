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
The facility follows a deterministic **"Nearest Available Slot First"** strategy:
- The outer loop iterates through floor numbers in ascending order: $f = 0, 1, \dots, F-1$.
- The inner loop iterates through slot indices on the current floor: $s = 0, 1, \dots, \text{slots}[f].\text{length}-1$.
- By prioritizing lower floor indices ($f = 0$ Ground Floor first) and lower slot numbers ($s = 0$ nearest entry gate first), the system minimizes travel distance and traffic congestion inside the parking structure.

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

### 2.4 Control Flow Statements: Strategic Use of `return`, `break`, and `continue`
Using appropriate control statements simplifies logic and eliminates redundant operations:
- **`return` for Immediate Exit:**  
  In `allocate()`, returning the allocated coordinates `{f, s}` immediately terminates the method from within the nested inner loop, avoiding complex boolean flag variables (`boolean found`) or labeled break statements.
- **`return` for Early Boundary Validation:**  
  In `release()`, guard clauses test for invalid floor indices, invalid slot indices, or already-free slots, returning `false` immediately before any mutation can occur.
- **`break` vs. `continue` in Multi-Tier Searching:**  
  If a floor is known to have zero available slots (e.g., using a free-slot counter), a `continue` statement in the outer loop can skip the inner loop entirely for that floor. Inside a loop, `break` terminates the nearest enclosing loop when a condition is met.

### 2.5 Fee Slabs Evaluation Using Decision Structures

Fee calculation evaluates parking duration against tiered rate slabs. In Java, this can be structured using chained `if–else if–else` ladders or modern pattern-matching `switch` statements.

#### Illustrative Fee Slab Models vs. Official Requirements
> [!IMPORTANT]
> **Distinction Between Illustration and Assignment Rules:**  
> The assignment specification explicitly states that fee slabs depend on vehicle type and duration of stay (with monthly pass holders parking free). However, Task 3 does not invent permanent university tariff figures. The example below illustrates how multi-tier slabs are structured in control logic using `if–else` vs. `switch`.

#### Illustrative Slab Structure:
Suppose a vehicle category has the following hypothetical fee policy:
- **Tier 1 (First hour or part thereof):** Flat base fee (e.g., ₹30.00)
- **Tier 2 (Each additional hour up to 8 hours):** Incremental hourly rate (e.g., ₹20.00/hour)
- **Tier 3 (Daily cap / Maximum charge beyond 8 hours):** Capped maximum fee (e.g., ₹200.00)

```java
// Illustrative Control Logic for Tiered Fee Slabs
public static double calculateIllustrativeFee(int billedHours, double baseRate, 
                                              double hourlyRate, double dailyMax) {
    if (billedHours <= 0) {
        return 0.0;
    } else if (billedHours == 1) {
        // Tier 1: First hour flat fee
        return baseRate;
    } else if (billedHours <= 8) {
        // Tier 2: First hour plus additional hours
        double fee = baseRate + (billedHours - 1) * hourlyRate;
        return Math.min(fee, dailyMax);
    } else {
        // Tier 3: Daily maximum cap
        return dailyMax;
    }
}
```

- **`if–else` Ladder:** Best suited for continuous range conditions (e.g., `billedHours <= 8`) and floating-point comparisons.
- **`switch` Expressions:** Highly effective when branching on discrete categorical inputs (e.g., `vehicleType`: `TWO_WHEELER`, `CAR`, `ELECTRIC_VEHICLE`).

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
ALGORITHM ComputeParkingFee(durationMinutes, vehicleType, hasMonthlyPass, isChargingSlot, rates)
// Computes parking fee using duration, vehicle category, pass waiver, and charging status.
// Preconditions:
//   durationMinutes is a non-negative integer.
//   vehicleType is one of {TWO_WHEELER, CAR, ELECTRIC_VEHICLE}.
//   hasMonthlyPass is a boolean.
//   isChargingSlot is a boolean indicating charging bay utilization.
//   rates is a configuration structure containing hourly tariffs and charging rates.
// Postconditions:
//   Returns total non-negative fee payable (FLOAT/DOUBLE).

INPUT:
    durationMinutes : INTEGER
    vehicleType     : STRING
    hasMonthlyPass  : BOOLEAN
    isChargingSlot  : BOOLEAN
    rates           : RATE_CONFIG

OUTPUT:
    totalFee        : DOUBLE

VARIABLES:
    billedHours     : INTEGER
    hourlyRate      : DOUBLE
    parkingFee      : DOUBLE
    chargingFee     : DOUBLE

BEGIN
    // Step 1: Base validation
    IF durationMinutes <= 0 THEN
        RETURN 0.0
    END IF

    // Step 2: Determine applicable base rate per vehicle type
    SWITCH vehicleType DO
        CASE "TWO_WHEELER":
            hourlyRate <- rates.twoWheelerRate
        CASE "CAR":
            hourlyRate <- rates.carRate
        CASE "ELECTRIC_VEHICLE":
            hourlyRate <- rates.electricVehicleRate
        DEFAULT:
            hourlyRate <- rates.defaultRate
    END SWITCH

    // Step 3: Round duration up to full started hours (Ceiling Rounding)
    billedHours <- CEIL(durationMinutes / 60.0)

    // Step 4: Compute base parking charge with monthly pass waiver
    IF hasMonthlyPass = TRUE THEN
        parkingFee <- 0.0
    ELSE
        parkingFee <- billedHours * hourlyRate
    END IF

    // Step 5: Add charging fee only if vehicle utilized a charging slot
    chargingFee <- 0.0
    IF vehicleType = "ELECTRIC_VEHICLE" AND isChargingSlot = TRUE THEN
        chargingFee <- billedHours * rates.chargingRatePerHour
    END IF

    // Step 6: Return total aggregate fee
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

Let $F$ denote the number of floors, $S$ denote the number of slots per floor, and $N = F \cdot S$ denote the total capacity.

| Operation | Baseline Linear Scan | Alternative (Count + Stacks) | Complexity Analysis & Assumptions |
|---|---|---|---|
| **`allocate()` — Best Case** | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ | **Baseline:** First inspected slot (`slots[0][0]`) is free; loop exits immediately.<br>**Alternative:** Floor 0 has $\text{freeCount}[0] > 0$; pops slot from stack in $\mathcal{O}(1)$. |
| **`allocate()` — Worst Case** | $\mathcal{O}(N)$ or $\mathcal{O}(F \cdot S)$ | $\mathcal{O}(F)$ | **Baseline:** Facility is completely full (or only the very last slot `slots[F-1][S-1]` is free). Must inspect all $N$ slots.<br>**Alternative:** Inspects at most $F$ floor counters. Once a non-empty floor is found, `pop()` is $\mathcal{O}(1)$. Reduces worst-case time from $\mathcal{O}(F \cdot S)$ to $\mathcal{O}(F)$. |
| **`allocate()` — Average Case** | $\mathcal{O}(N)$ | $\mathcal{O}(F)$ | On average, baseline inspects $\approx \frac{N}{2}$ slots ($\mathcal{O}(N)$). Alternative inspects $\approx \frac{F}{2}$ counters before popping ($\mathcal{O}(F)$). |
| **`release()`** | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ | **Baseline:** Validates index bounds and sets `slots[floor][slot] = null`. Strictly constant time.<br>**Alternative:** Validates bounds, clears slot, pushes to stack ($\mathcal{O}(1)$), and increments counter ($\mathcal{O}(1)$). Strictly constant time. |

---

### 5.4 Space Complexity and Auxiliary Memory Overhead

| Metric | Baseline Linear Scan | Alternative (Count + Stacks) |
|---|---|---|
| **Primary Slot Storage** | $\mathcal{O}(F \cdot S)$ (`String[][]`) | $\mathcal{O}(F \cdot S)$ (`String[][]`) |
| **Extra Auxiliary Space** | $\mathcal{O}(1)$ (Zero auxiliary structures) | $\mathcal{O}(F \cdot S)$ auxiliary space |
| **Auxiliary Heap Allocations** | None. | - 1 array of integers (`int[F]`) $\rightarrow \mathcal{O}(F)$<br>- 1 array of Stack references (`Stack[F]`) $\rightarrow \mathcal{O}(F)$<br>- $F$ `Stack` instances containing up to $N = F \cdot S$ Integer node references $\rightarrow \mathcal{O}(F \cdot S)$ |

---

### 5.5 Trade-offs, Ordering Implications, and Design Evaluation

1. **Time vs. Space Trade-off:**  
   The alternative design trades $\mathcal{O}(F \cdot S)$ extra auxiliary memory to achieve a faster allocation time of $\mathcal{O}(F)$ instead of $\mathcal{O}(F \cdot S)$. In very large facilities (e.g., 5 floors with 1,000 slots per floor), this is an effective optimization ($5$ iterations vs. $5,000$).
2. **Slot Allocation Ordering (LIFO vs. Lowest-Slot):**  
   - The **baseline linear scan** strictly guarantees that the lowest-numbered slot on the lowest-numbered floor is always allocated first (deterministic spatial locality).
   - The **stack-based alternative** is Last-In, First-Out (LIFO). When a slot is released, it is pushed to the top of the stack and will be re-allocated *before* previously released lower-numbered slots.
   - If lowest-slot allocation ordering must be preserved, a **Min-Heap (Priority Queue)** or **ordered bitset** would be required instead of a Stack, which would increase release time to $\mathcal{O}(\log S)$.

---

## 6. Summary of Task 3 Deliverables

1. **Control Logic Documented:** Floor-by-floor search, allocation termination, and guard-clause release validation fully specified.
2. **Algorithmic Pseudocode:** Formal SIMATS Engineering standard pseudocode developed for allocation, release, and fee computation.
3. **Working Java Program:** `ParkingFacility.java` successfully implemented, compiled, and executed with exact output verified.
4. **Alternative Concept Evaluated:** Rigorous Big-O time and space trade-off analysis completed comparing linear scanning with stack/counter caching.

