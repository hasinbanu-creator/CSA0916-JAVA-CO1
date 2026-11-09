# TASK 2: Choose the Data Representation

**Course:** Programming in Java (CO1)  
**Assignment Title:** Design, Implement and Validate an Object-Oriented Model for a Campus Smart-Parking System  
**Evaluation Component:** Task 2 (3 Marks)  
**Bloom's Taxonomy Level:** K4 – Analyse  

---

## 1. Data Type Selection and Technical Justification

Selecting appropriate data types is fundamental to ensuring numeric correctness, memory efficiency, and type safety in the Campus Smart-Parking System.

### 1.1 Field Justification Table

| System Field | Selected Java Type | Type Category | Allowable Range / Domain | Technical Justification |
|---|---|---|---|---|
| **Slot Number** | `int` | Primitive | $0$ to $S_f - 1$ (e.g., $0$ to $99$) | Represents a discrete zero-based physical index within a floor. Primitive `int` provides direct array indexing without object wrapper allocation or unboxing overhead. |
| **Vehicle Registration Number** | `String` | Reference (Immutable) | Standard alphanumeric registration codes (e.g., `"TN01AB1234"`) | Alphanumeric string containing state codes, district codes, and unique vehicle identifiers. Immutable `String` provides thread-safe identity, case-insensitive comparison (`equalsIgnoreCase`), and regular expression format validation. |
| **Entry Timestamp / Exit Timestamp** | `java.time.LocalDateTime` | Reference (Temporal) | ISO-8601 calendar date and time | Recommended for representing the actual date and time when a vehicle enters or exits the facility. It captures the year, month, day, hour, minute, and second without ambiguity. A timestamp represents a specific point in time, distinguishing it from elapsed duration. |
| **Parking Duration** | `long` | Primitive | $\ge 0$ minutes | Measures the calculated elapsed stay in minutes ($\Delta t$). Computed from the difference between exit and entry timestamps (e.g., using `java.time.Duration.between(entry, exit).toMinutes()`). Using `long` prevents integer overflow when accumulating durations across extended periods. |
| **Parking Fee** | `double` | Primitive | $\ge 0.0$ (64-bit IEEE 754 floating point) | Practical choice for representing monetary amounts in this academic assignment (e.g., ₹30.00, ₹45.50). Primitive `double` provides 53 bits of precision (~15–17 decimal digits), offering higher precision than 32-bit `float`, though exact decimal arithmetic requires `BigDecimal` in enterprise systems. |

### 1.2 Distinguishing Timestamps from Elapsed Duration
It is essential to distinguish between **point-in-time timestamps** and **elapsed duration**:
- **Timestamps (`java.time.LocalDateTime`):**  
  An entry timestamp records *when* a vehicle arrived (e.g., `2026-11-09T09:15:00`). It is a calendar-aware temporal reference. `LocalDateTime` from Java's modern Date-Time API (`java.time`) is strongly recommended for modeling real-world admission and exit timestamps because it is immutable, thread-safe, and avoids legacy flaws found in `java.util.Date`.
- **Elapsed Duration (`long` in minutes):**  
  Duration measures *how long* the vehicle remained parked:
  $$\Delta t = \text{exitTime} - \text{entryTime}$$
  Using `Duration.between(entry, exit).toMinutes()` produces a discrete `long` scalar representing elapsed whole minutes. Storing duration as a primitive `long` simplifies numeric slab evaluation, eliminates calendar complexity from tariff logic, and completely avoids numeric overflow.

### 1.3 Primitive vs. Object Wrapper Considerations
- **Storage and Performance:**  
  In Java, primitive variables (`int`, `long`, `double`, `boolean`) store raw binary values directly without reference indirection. Local primitive variables reside on the thread call stack, whereas primitive instance fields and primitive array elements reside directly within the heap memory of their containing object or array. In contrast, wrapper classes (`Integer`, `Long`, `Double`) encapsulate primitives inside separate heap objects containing standard JVM object headers.
- **Null Safety and Unboxing:**  
  Using primitives for slot indices and duration avoids unexpected `NullPointerException` errors that can occur when auto-unboxing wrapper instances during arithmetic or array subscripting.
- **Reference Types (`String`) for Identifiers:**  
  Registration numbers and ticket identifiers contain alphanumeric text, requiring string methods (`equalsIgnoreCase`, formatting). `java.lang.String` is immutable, making it a robust reference type for keys and identification.

### 1.4 Monetary Precision: `double` vs. `float` vs. `BigDecimal`
- **Why `double` Over `float`:**  
  Java's 32-bit `float` has only 24 bits of significand precision (~7 decimal digits). In iterative calculations or multiplication with fractional hours, `float` accumulates noticeable binary rounding drift. Primitive `double` (64-bit IEEE 754) provides 53 bits of precision (~15–17 decimal digits), offering substantially higher precision and making it a practical choice for this academic assignment.
- **Binary Floating-Point Limitations:**  
  It is important to emphasize that `double` **does not eliminate** binary floating-point representation errors. Because computers represent numbers in base-2, common decimal fractions (such as $0.1$ or $0.7$) have non-terminating repeating binary expansions and cannot be represented exactly in binary floating point.
- **Enterprise Alternative (`java.math.BigDecimal`):**  
  For production enterprise banking and mission-critical financial applications, `java.math.BigDecimal` is the industry standard because it performs exact decimal arithmetic with configurable rounding modes (e.g., `RoundingMode.HALF_UP`), eliminating binary floating-point inaccuracies entirely. For this academic assignment, `double` remains a suitable and practical choice when combined with proper rounding techniques.

---

## 2. Array Representation of Floors and Slots

A core design challenge is modeling the multi-level layout of the campus parking facility as it scales:
$$\text{Phase 1: } 1 \text{ floor} \times 20 \text{ slots} \longrightarrow \text{Phase 2: } 3 \text{ floors} \times 50 \text{ slots} \longrightarrow \text{Phase 3: } 5 \text{ floors} \times 100 \text{ slots}$$
Furthermore, the Day-4 Design Challenge introduces facilities where **different floors have different numbers of slots**.

### 2.1 Understanding Array Layouts in Java

#### A. One-Dimensional (1D) Arrays
A 1D array is a single contiguous sequence of elements indexed from $0$ to $N-1$:
```java
String[] slots = new String[totalSlots];
```
- **Pros:** Minimal reference overhead (only one array object header).
- **Cons:** Flattens multi-dimensional structures. Representing multi-level floors requires explicit mathematical index mapping.

#### B. Two-Dimensional (2D) Rectangular Arrays
In Java, a 2D array is technically an "array of arrays". A rectangular array is instantiated when all rows share an identical length:
```java
String[][] slots = new String[floors][slotsPerFloor]; // e.g. new String[3][50]
```
- **Pros:** Natural row-column indexing: `slots[floor][slot]`.
- **Cons:** Rigid topology. If Floor 0 has 20 slots, Floor 1 has 30, and Floor 2 has 50, a rectangular array must be dimensioned to `new String[3][50]`. This creates 50 unused `null` slots across Floors 0 and 1, wasting memory and making `slots[f].length` inaccurate for floor-specific bounds validation.

#### C. Jagged (Ragged) Arrays
A jagged array is an array of arrays where each row is an independently allocated array object with its own distinct length:
```java
String[][] slots = new String[floors][];
for (int f = 0; f < floors; f++) {
    slots[f] = new String[slotsPerFloor[f]]; // Each floor has its own capacity
}
```
- **Pros:** Perfectly models real-world physical structures where floors have different architectural dimensions.
- **Memory Efficiency:** Zero wasted slots. Exactly $\sum S_f$ elements are allocated.
- **Accurate Bounds:** `slots[f].length` returns the exact physical capacity of floor `f`.

---

### 2.2 Selected Representation: Jagged Array (`String[][]`)

For the Campus Smart-Parking System, the **Jagged Array (`String[][]`)** is chosen as the optimal representation.

#### Technical Justification:
1. **Architectural Realism:** Real multi-level parking facilities often have different capacities per floor (e.g., ground floor accommodates entrance lobbies, ramps, and charging bays, leaving fewer regular slots than upper floors).
2. **Zero Memory Waste:** No phantom slots or sentinel values are needed.
3. **Natural Bounds Checking:** `floor < slots.length` validates the floor, and `slot < slots[floor].length` validates the slot index on that specific floor.
4. **Direct Specification Alignment:** Slide 8 of the assignment specification explicitly defines this exact jagged array pattern:
   ```java
   public class ParkingFacility {
       private final String[][] slots;

       public ParkingFacility(int[] slotsPerFloor) {
           slots = new String[slotsPerFloor.length][];
           for (int f = 0; f < slotsPerFloor.length; f++) {
               slots[f] = new String[slotsPerFloor[f]];
           }
       }
   }
   ```

### 2.3 Java Example: Jagged Array with Different Capacities per Floor

```java
public class JaggedParkingDemo {
    public static void main(String[] args) {
        // Floor 0 has 3 slots, Floor 1 has 2 slots, Floor 2 has 4 slots
        int[] capacityPerFloor = {3, 2, 4};
        
        // 1. Allocate the outer array (number of floors)
        String[][] parkingSlots = new String[capacityPerFloor.length][];
        
        // 2. Allocate each floor independently
        for (int f = 0; f < capacityPerFloor.length; f++) {
            parkingSlots[f] = new String[capacityPerFloor[f]];
        }
        
        // 3. Park vehicles into specific slots
        parkingSlots[0][0] = "TN01AA1111"; // Floor 0, Slot 0
        parkingSlots[0][1] = "TN01BB2222"; // Floor 0, Slot 1
        parkingSlots[1][0] = "TN02CC3333"; // Floor 1, Slot 0
        parkingSlots[2][3] = "TN03DD4444"; // Floor 2, Slot 3
        
        // 4. Inspect layout and occupants
        for (int f = 0; f < parkingSlots.length; f++) {
            System.out.print("Floor " + f + " (Capacity " + parkingSlots[f].length + "): ");
            for (int s = 0; s < parkingSlots[f].length; s++) {
                String occupant = (parkingSlots[f][s] == null) ? "[Empty]" : parkingSlots[f][s];
                System.out.print(occupant + " ");
            }
            System.out.println();
        }
    }
}
```

---

## 3. Type Conversion, Casting, and the Integer Division Risk

### 3.1 Widening vs. Narrowing Type Conversions
- **Widening Conversion (Implicit / Safe):**  
  Occurs automatically when converting a type with smaller range/precision to a larger type (e.g., `int` $\rightarrow$ `double`). No data or magnitude is lost:
  ```java
  int duration = 90;
  double durationDouble = duration; // Widening conversion: 90 -> 90.0
  ```
- **Narrowing Conversion (Explicit / Risky):**  
  Requires an explicit cast operator `(targetType)` because it may discard fractional bits or cause numeric overflow (e.g., `double` $\rightarrow$ `int` truncates the decimal portion toward zero):
  ```java
  double billedUnits = 1.85;
  int wholeUnits = (int) billedUnits; // Narrowing cast: truncates 1.85 to 1
  ```

---

### 3.2 The Integer Division Problem When Converting Minutes into Hours

In Java, when both operands of the division operator `/` are integers, Java strictly performs **integer division**. The quotient is truncated toward zero, and any fractional remainder is discarded:
$$\frac{45}{60} = 0 \quad (\text{remainder } 45), \qquad \frac{65}{60} = 1 \quad (\text{remainder } 5)$$

#### Demonstration: The Buggy Implementation
Suppose a parking facility bills at an hourly rate of ₹30.00.

```java
public class FeeCalculationBugDemo {
    public static void main(String[] args) {
        int durationMinutes = 65; // 1 hour and 5 minutes
        double hourlyRate = 30.0;

        // BUG 1: Integer division truncates 65 / 60 to integer 1
        int hoursWrong1 = durationMinutes / 60; 
        double feeWrong1 = hoursWrong1 * hourlyRate;
        System.out.println("Bug 1 Result: Rs " + feeWrong1); // Output: Rs 30.0

        // BUG 2: Narrowing cast applied AFTER integer division has already truncated
        double feeWrong2 = ((double) (durationMinutes / 60)) * hourlyRate;
        System.out.println("Bug 2 Result: Rs " + feeWrong2); // Output: Rs 30.0

        // BUG 3: Short stay under 60 minutes
        int shortStay = 45;
        double shortFeeWrong = (shortStay / 60) * hourlyRate; // 45 / 60 evaluates to 0
        System.out.println("Bug 3 Result (45 mins): Rs " + shortFeeWrong); // Output: Rs 0.0!
    }
}
```

#### Risk and Financial Impact:
1. **Under-Billing:** For a 65-minute stay, the user is charged for only 1 hour instead of 2 hours, causing lost revenue for the university.
2. **Free Parking Anomaly:** For any stay under 60 minutes (e.g., 45 minutes), `45 / 60 = 0`, resulting in a fee of ₹0.00. A visitor parks for three-quarters of an hour without paying anything.

---

### 3.3 Distinguishing Billing Models: Proportional vs. Rounding Up to Started Hour

Depending on business policy, parking systems implement one of two distinct mathematical fee models:

#### Model A: Rounding Up to a Full Started Hour (Ceiling Slabs)
Under this policy, any started hour (even 1 minute into the hour) is billed as a full hour.
- **Floating-Point Formulation:**  
  Promote operand to `double` before division, apply `Math.ceil()`, and cast back:
  ```java
  // Correct ceiling calculation
  int billedHours = (int) Math.ceil((double) durationMinutes / 60.0);
  double fee = billedHours * hourlyRate;
  ```
- **Integer-Arithmetic Formulation (Alternative):**  
  For non-negative durations, ceiling division can also be computed entirely using integer arithmetic:
  ```java
  int billedHours = (durationMinutes <= 0) ? 0 : (durationMinutes + 59) / 60;
  double fee = billedHours * hourlyRate;
  ```
  *(Example: for $61\text{ mins}$, $(61 + 59) / 60 = 120 / 60 = 2\text{ hours}$)*.

#### Model B: Proportional Hourly Billing (Fractional Billing)
Under this policy, parking is charged strictly proportional to the exact fraction of time parked:
```java
// Correct proportional calculation
double exactHours = (double) durationMinutes / 60.0; // 90 mins -> 1.5 hours
double fee = exactHours * hourlyRate;                // 1.5 * 30.0 = Rs 45.0
```
- In this model, integer division would have truncated 90 minutes to 1.0 hour (₹30.00), losing ₹15.00 of legitimate fractional charge.

#### Corrected Java Implementation Example:
```java
public class CorrectFeeCalculation {
    // Ceiling Billing Model
    public static double computeCeilingFee(int durationMinutes, double hourlyRate) {
        if (durationMinutes <= 0) return 0.0;
        int billedHours = (int) Math.ceil(durationMinutes / 60.0); // 60.0 forces double division
        return billedHours * hourlyRate;
    }

    // Proportional Billing Model
    public static double computeProportionalFee(int durationMinutes, double hourlyRate) {
        if (durationMinutes <= 0) return 0.0;
        double exactHours = durationMinutes / 60.0; // 60.0 forces double division
        return exactHours * hourlyRate;
    }

    public static void main(String[] args) {
        int duration = 65; // 1 hour 5 minutes
        double rate = 30.0;
        System.out.println("Ceiling Fee (65 mins): Rs " + computeCeilingFee(duration, rate)); // Rs 60.0
        System.out.println("Proportional Fee (65 mins): Rs " + computeProportionalFee(duration, rate)); // Rs 32.5
    }
}
```

> [!IMPORTANT]
> **Explicit Scope Boundary:** Task 2 does not establish the final fee policy or specific tariff slabs for the campus parking facility. The concrete pricing slabs, rate tiers, vehicle-type fee formulas, and monthly pass waivers are analyzed and implemented in subsequent tasks (specifically Task 3, Task 6, and Task 7). The purpose of this section in Task 2 is strictly to analyze data representation, demonstrate the arithmetic risks of integer division, and show how floating-point promotion and casting preserve numerical accuracy under different billing models.

---

## 4. Input Handling: Scanner vs. Command-Line Arguments

The system requires input data for facility configuration (floor capacities) and transactional operations (allocating a vehicle, releasing a slot).

### 4.1 Comparison Table

| Attribute | `java.util.Scanner(System.in)` | Command-Line Arguments (`String[] args`) |
|---|---|---|
| **Interaction Style** | Synchronous, interactive prompt-and-response loop during runtime. | Batch parameters passed at program invocation before `main()` executes. |
| **Input Mechanism** | Reads standard input stream via console prompts (`System.out.print`). | Populates the `args` array passed into `public static void main(String[] args)`. |
| **Error Modes** | Throws `InputMismatchException` if non-numeric characters are typed; vulnerable to orphaned newline buffer bugs. | Throws `ArrayIndexOutOfBoundsException` if parameters are omitted; throws `NumberFormatException` during parsing. |
| **Automation & Testing** | Difficult to automate in non-interactive grading scripts and CI/CD pipelines without mock stream redirection. | Easily scripted in terminal batch commands (e.g. `java App 3 20 50 100`). |

---

### 4.2 Code Examples

#### Example 1: Interactive Console Input with `Scanner`
```java
import java.util.Scanner;

public class ScannerInputDemo {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Enter vehicle registration number: ");
        String regNo = scanner.nextLine().trim();
        
        System.out.print("Enter duration in minutes: ");
        int duration = scanner.nextInt();
        // Consume the trailing newline left by nextInt()
        scanner.nextLine(); 
        
        System.out.println("Received: Vehicle " + regNo + ", Duration: " + duration + " mins");
        scanner.close();
    }
}
```

#### Example 2: Command-Line Arguments (`args[]`)
```java
public class CommandLineArgsDemo {
    public static void main(String[] args) {
        // Usage: java CommandLineArgsDemo <regNo> <durationMinutes>
        if (args.length < 2) {
            System.err.println("Usage: java CommandLineArgsDemo <regNo> <durationMinutes>");
            return;
        }
        
        String regNo = args[0];
        try {
            int duration = Integer.parseInt(args[1]);
            System.out.println("Parsed from CLI: Vehicle " + regNo + ", Duration: " + duration + " mins");
        } catch (NumberFormatException e) {
            System.err.println("Error: Duration must be a valid integer.");
        }
    }
}
```

---

### 4.3 Recommended Approach for this Project

For the Campus Smart-Parking System, a **hybrid, programmatic driver architecture** is recommended:
1. **Core Domain Methods:**  
   Methods such as `allocate(String vehicleNo)` and `release(int floor, int slot)` must accept typed parameters directly, decoupling business logic from any specific I/O library.
2. **Standalone Task Verification:**  
   Programmatic execution within `main()` (as shown in the assignment briefing on Slide 8) and automated JUnit 5 tests provide deterministic, repeatable execution without typing errors during evaluation.
3. **Interactive Menu (Optional Layer):**  
   If an interactive terminal interface is required for end-user demonstration, `Scanner` can wrap the domain methods with safe input validation (`scanner.hasNextInt()`).

---

## 5. Alternative Concept and Complexity Analysis

The assignment briefing requires comparing three distinct structural approaches for modeling parking slots across floors:
- **Design A:** Jagged Array: `String[floor][slot]`
- **Design B:** Single Flattened 1D Array using index formula: `index = floor * slotsPerFloor + slot`
- **Design C:** Dynamic List of Arrays: `ArrayList<String[]>`

---

### 5.1 Case Analysis: What Changes When Floors Have Different Slot Counts?

#### Design A: Jagged Array (`String[][]`)
- **Adaptability:** Naturally supports varying floor capacities because each floor row is an independently allocated array object.
- **Behavior:** `slots[0] = new String[20]`, `slots[1] = new String[50]`.
- **Change Required:** None. The structure handles unequal floors without modification.

#### Design B: Flattened 1D Array (`String[]`)
- **When Floors are Equal-Sized ($S$):**  
  Index mapping is straightforward and uniform:
  $$\text{index} = \text{floor} \times S + \text{slot}$$
  Reverse mapping from linear index to coordinates:
  $$\text{floor} = \frac{\text{index}}{S}, \qquad \text{slot} = \text{index} \pmod S$$
- **When Floors Have Different Capacities ($S_0 \neq S_1 \neq \dots$):**
  - **The formula breaks:** A constant scalar $S$ does not exist.
  - **Approach 1 (Padding to Maximum Capacity $S_{\max}$):**  
    Dimension the 1D array to $F \times S_{\max}$. The index formula remains $\text{floor} \times S_{\max} + \text{slot}$, but substantial memory is wasted for all floors with capacity $< S_{\max}$. Unused slots must hold sentinel markers to prevent invalid parking.
  - **Approach 2 (Prefix-Offset Array):**  
    Pack only valid slots contiguously into an array of size $N = \sum S_f$. Maintain an auxiliary prefix-sum array `int offset[]` where:
    $$\text{offset}[0] = 0, \quad \text{offset}[f] = \sum_{i=0}^{f-1} S_i$$
    Then direct index lookup is restored to:
    $$\text{index} = \text{offset}[\text{floor}] + \text{slot}$$
    *(Example: If capacities are $\{20, 15, 25\}$, `offset = {0, 20, 35}`. Slot 4 on Floor 2 maps to index $35 + 4 = 39$).*

#### Design C: Dynamic List of Arrays (`ArrayList<String[]>`)
- **Adaptability:** The outer structure is a resizable `java.util.ArrayList`, where each element is an independently sized `String[]` array representing a floor.
- **Behavior & Operation Complexities:**
  - **Random Access:** `ArrayList.get(index)` is backed by an internal array, guaranteeing strictly $\mathcal{O}(1)$ random access by index.
  - **Accessing a Known Slot:** Locating a known slot via `list.get(floor)[slot]` requires an $\mathcal{O}(1)$ retrieval of the floor array followed by an $\mathcal{O}(1)$ array element access, remaining strictly $\mathcal{O}(1)$ overall.
  - **Adding a Floor:** Appending a new floor at the end (`list.add(new String[capacity])`) runs in **amortized $\mathcal{O}(1)$** time. It requires an occasional $\mathcal{O}(F)$ array copy operation only when the internal capacity buffer of the `ArrayList` is exhausted and must resize.
- **Change Required:** None. Floors of any capacity can be added dynamically at runtime without manually re-allocating an outer array.

---

### 5.2 Time Complexity Analysis

Let:
- $F =$ number of floors in the facility
- $S_f =$ number of slots on floor $f$
- $N = \sum_{f=0}^{F-1} S_f =$ total number of parking slots across all floors

| Operation | Design A: Jagged Array (`String[][]`) | Design B: Flattened 1D Array (`String[]`) | Design C: `ArrayList<String[]>` | Technical Discussion |
|---|---|---|---|---|
| **Locating a Known Slot** *(given coordinates `floor`, `slot`)* | $\mathcal{O}(1)$ | $\mathcal{O}(1)$ *(with uniform $S$ or with `offset[]`)*<br>$\mathcal{O}(F)$ *(without `offset[]`)* | $\mathcal{O}(1)$ | **Jagged Array:** Two pointer dereferences (`slots[f]` then `[s]`), taking constant $\mathcal{O}(1)$ time.<br>**1D Array:** Constant-time arithmetic and single array access ($\mathcal{O}(1)$) when using the uniform formula or prefix offset. If non-uniform without a prefix table, summing floor lengths takes $\mathcal{O}(F)$.<br>**ArrayList:** `list.get(floor)` is an $\mathcal{O}(1)$ array lookup into the internal backing buffer, followed by `[slot]` array indexing ($\mathcal{O}(1)$). Locating a known slot is strictly $\mathcal{O}(1)$. (Appending a new floor at the end is amortized $\mathcal{O}(1)$). |
| **Search Across All Slots** *(e.g., finding first free slot or vehicle by regNo)* | $\mathcal{O}(N)$ worst-case | $\mathcal{O}(N)$ worst-case | $\mathcal{O}(N)$ worst-case | In the worst case (facility full, or vehicle at the very last slot / absent), any sequential search algorithm must examine all $N$ slots regardless of data representation. Best case is $\mathcal{O}(1)$ if available at `(0, 0)`. |

---

### 5.3 Space Complexity and Memory Overhead Analysis

| Metric | Design A: Jagged Array (`String[][]`) | Design B: Flattened 1D Array (`String[]`) | Design C: `ArrayList<String[]>` |
|---|---|---|---|
| **Total Asymptotic Space** | $\mathcal{O}(N)$ | $\mathcal{O}(N)$ *(contiguous)* or $\mathcal{O}(F \cdot S_{\max})$ *(padded)* | $\mathcal{O}(N)$ |
| **Number of Heap Objects** | $F + 1$ array objects (1 master array + $F$ floor arrays) | 1 array object (or 2 if auxiliary `offset[]` array is used) | $F + 2$ objects (`ArrayList` wrapper + internal `Object[]` buffer + $F$ floor arrays) |
| **Memory Waste on Unequal Floors** | **Zero:** Exactly $N$ slot references allocated. | **Zero** if using prefix offset; **Severe waste** if padded to $S_{\max}$ ($F \cdot S_{\max} - N$ empty slots). | **Zero:** Exactly $N$ slot references allocated (plus small unused capacity in `ArrayList` growth buffer). |
| **Structural Overhead** | Modest: $F$ inner array headers. | Lowest: Only 1 array header. | Highest: `ArrayList` instance fields + growth buffer + inner arrays. |

---

## 6. Summary and Final Design Decision

1. **Primitive Data Types:**  
   `int` is justified for slot and floor indices; `int`/`long` for elapsed minute calculations; `double` for currency fees to avoid precision drift.
2. **Preventing Precision Loss:**  
   Integer division errors (which discard fractional hours and cause under-billing) are strictly prevented by promoting operands to floating-point (`durationMinutes / 60.0`) and applying ceiling functions (`Math.ceil()`) or integer ceiling arithmetic (`(t + 59) / 60`).
3. **Selected Structure:**  
   The **Jagged Array (`String[][]`)** is chosen because it accurately models multi-level facilities with varying floor capacities, eliminates memory waste, provides $\mathcal{O}(1)$ direct coordinate indexing, and directly fulfills the assignment architecture.
4. **Input Strategy:**  
   Typed method parameters with programmatic test drivers ensure reliable, repeatable academic evaluation.

This data representation establishes the technical foundation for **Task 3: Design the Control Logic**.
