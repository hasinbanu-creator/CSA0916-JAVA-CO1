# Task 5: Apply Static Members and the Object Life Cycle

---

## 1. Static Variables

In Java, class definitions govern two distinct categories of state: **instance variables** and **class (static) variables**.

```
+-----------------------------------------------------------------------------------+
| JVM Metaspace / Method Area (Loaded Classes)                                      |
|   class TicketCounterDemo / ParkingTicket                                         |
|     +-----------------------------------------------------------------------+     |
|     | static int sharedTicketCounter = 1003;                                |     |
|     | static final String DEFAULT_PREFIX = "TKT-";                          |     |
|     +-----------------------------------------------------------------------+     |
+-----------------------------------------------------------------------------------+
                                      ^
                                      | (Shared Class State)
      +-------------------------------+-------------------------------+
      |                                                               |
+-----|-------------------------+                               +-----|-------------------------+
| JVM Heap (Instance 1)         |                               | JVM Heap (Instance 2)         |
|   FacilityGate: "North-Gate"  |                               |   FacilityGate: "South-Gate"  |
|     instanceCounter = 1       |                               |     instanceCounter = 1       |
+-------------------------------+                               +-------------------------------+
```

### 1.1 Class-Level vs. Object-Level Information

- **Instance Variables (Per-Object State):**
  Instance fields belong to an individual instance of a class. Every time `new` is invoked, a distinct block of memory is allocated on the JVM Heap. The fields within that block represent the specific state of that single entity.
  - Examples in the smart-parking domain: `vehicleRegNo`, `vehicleType`, `floorNumber`, `slotNumber`, `entryTime`, `exitTime`, and `isActive`. Each parked car has its own registration number, entry timestamp, and physical parking slot. If these were made class-level, every vehicle entry would overwrite the data of previously parked vehicles.

- **Static Variables (Class-Level State):**
  Static fields are declared with the `static` keyword and belong to the class as a whole, rather than to any individual object. The JVM allocates memory for static fields once when the class is loaded into memory (in the Metaspace / Method Area). All instances of the class share this single memory location.
  - Examples: A sequence counter for issuing monotonically increasing ticket identifiers, shared configuration constants (e.g., ticket prefixes), or system-wide counters.

### 1.2 Demonstration of a Static Ticket Counter

To guarantee that each newly issued ticket receives a strictly increasing, non-reused identifier, a static counter is maintained at the class level:

```java
public class TicketCounterDemo {
    // Shared class-level variable: exactly one copy exists across all instances
    private static int sharedTicketCounter;
    private static final String DEFAULT_PREFIX;

    static {
        DEFAULT_PREFIX = "TKT-";
        sharedTicketCounter = 1000;
    }

    public static synchronized String generateNextTicketId() {
        sharedTicketCounter++;
        return DEFAULT_PREFIX + sharedTicketCounter;
    }
}
```

When tickets are issued from anywhere in the application, each call to `generateNextTicketId()` increments the exact same counter, yielding `TKT-1001`, `TKT-1002`, `TKT-1003`, and so forth.

### 1.3 Why an Instance Counter Inside `ParkingTicket` Would Fail

If the counter were declared as an instance variable inside `ParkingTicket`:

```java
// INCORRECT DESIGN
public class FlawedParkingTicket {
    private int ticketCounter = 1000; // Instance variable!
    private String ticketNumber;

    public FlawedParkingTicket(...) {
        this.ticketCounter++;
        this.ticketNumber = "TKT-" + this.ticketCounter;
    }
}
```

When `new FlawedParkingTicket(...)` executes:
1. A fresh heap object is allocated.
2. Its instance field `ticketCounter` is initialized to `1000`.
3. The constructor increments `this.ticketCounter` to `1001` and assigns `this.ticketNumber = "TKT-1001"`.
4. When the next vehicle enters and `new FlawedParkingTicket(...)` is called again, a completely new object is allocated with its own independent `ticketCounter` field, which again starts at `1000` and increments to `1001`.

Every single ticket issued would receive the identical identifier `TKT-1001`. This causes immediate identity collisions and directly violates **Business Rule 2 ("A ticket number must never be reused")**. An instance variable cannot coordinate a sequence across multiple instances because each instance is completely isolated from all other instances on the heap.

### 1.4 Total Occupancy: Facility-Level vs. Campus-Wide System State

The concept of "total occupancy" must be evaluated based on the boundary of the entity:

| Scope | Appropriate Representation | Justification |
| :--- | :--- | :--- |
| **Individual Facility Occupancy** | **Instance Variable** in `ParkingFacility` | Each physical parking facility (e.g., North Multi-Level Lot vs. South Surface Lot) has its own structural layout, slot capacity, and active vehicles. Facility A might be 95% full while Facility B is 20% full. Storing this as a static variable inside `ParkingFacility` would erroneously merge the counts of all buildings into one number, losing per-facility tracking. |
| **Campus-Wide Aggregated Occupancy** | **System Aggregation** or Class-Level Coordinator | If campus administration requires a single view of all parked vehicles across all facilities, this represents global system state. It can be computed dynamically by summing the instance occupancies ($\sum_{i=1}^k \text{facility}_i.\text{getOccupancy()}$) or tracked via a centralized campus coordinator. |

### 1.5 Consequences of Multiple Facility Instances

When the university operates multiple facility instances (such as a North Gate facility and a South Gate facility):
- If the ticket counter is a **static variable** shared across facilities, both gates draw from the same sequence. North Gate issues `TKT-1001`, South Gate issues `TKT-1002`, North Gate issues `TKT-1003`.
- If the ticket counter is an **instance variable** inside each facility object, North Gate maintains its own sequence (1, 2, 3...) and South Gate maintains its own sequence (1, 2, 3...). The architectural trade-offs of both options are formally analysed in Section 5.

---

## 2. Static Methods and Static Blocks

### 2.1 Appropriate Uses of Static Methods

A method should be declared `static` when its execution does not require access to the instance state (`this`) of any specific object.

Appropriate uses in the smart-parking system include:
1. **Sequence Generation / Factory Helpers:** Methods like `generateNextTicketId()` that only access static variables.
2. **Pure Mathematical / Conversion Utilities:** Methods that perform calculations strictly based on arguments passed to them (e.g., converting minutes to fractional hours, formatting currency).
3. **Class-Level Inspection / Maintenance:** Methods like `getSharedTicketCounter()` and `resetSharedCounter()` to query or reset global class state.

Constraints of static methods:
- A static method has **no `this` reference**.
- A static method **cannot directly access instance variables or call instance methods** without explicitly instantiating or receiving an object reference.
- A static method cannot be overridden polymorphically (it is resolved at compile time via static binding, though it can be hidden).

### 2.2 Java Example: Static Utility Method

```java
/**
 * Static utility method to generate unique sequential ticket identifiers.
 * Synchronized to ensure atomic increments across concurrent operations.
 */
public static synchronized String generateNextTicketId() {
    sharedTicketCounter++;
    return DEFAULT_PREFIX + sharedTicketCounter;
}

/**
 * Static accessor returning the current sequence value.
 */
public static synchronized int getSharedTicketCounter() {
    return sharedTicketCounter;
}
```

### 2.3 Static Initialization Blocks (`static { ... }`)

A **static initialization block** is a block of code enclosed in `static { ... }` inside a class body. It executes **exactly once**, when the class is first loaded and initialized by the JVM ClassLoader, before:
- Any instance of the class is created.
- Any static method of the class is executed.
- Any static variable of the class is read.

#### When Static Blocks Are Useful
Static blocks are essential when static variables require complex, multi-step initialization, such as:
- Initializing constants from external environment variables or configuration files.
- Computing pre-calculated lookup tables (e.g., trigonometric or fee-rate lookup tables).
- Handling checked exceptions during class initialization (which cannot be done in inline static field declarations).

```java
public class TicketCounterDemo {
    private static int sharedTicketCounter;
    private static final String DEFAULT_PREFIX;

    // Static Initialization Block
    static {
        // Enforces baseline prefix and initial sequence offset upon class loading
        DEFAULT_PREFIX = "TKT-";
        sharedTicketCounter = 1000;
    }
    // ...
}
```

### 2.4 Distinguishing Static Members from Instance Members

| Dimension | Static Member (`class`) | Instance Member (`object`) |
| :--- | :--- | :--- |
| **Declaration** | Marked with `static` keyword | Declared without `static` |
| **Memory Allocation** | Metaspace / Method Area (loaded once) | JVM Heap (allocated per `new`) |
| **Lifetime** | Loaded when class is initialized; persists until class is unloaded | Created on `new`; destroyed when garbage-collected |
| **Invocation** | `ClassName.member` (e.g., `TicketCounterDemo.getSharedTicketCounter()`) | `objectReference.member` (e.g., `ticket.getTicketNumber()`) |
| **Receiver Reference** | No `this` reference exists | Bound to the executing object instance (`this`) |
| **Copies in Memory** | Exactly **1** copy per JVM ClassLoader | **$n$** copies for $n$ instantiated objects |

> [!NOTE]
> Do not introduce unnecessary static methods or static state. Business entities such as `ParkingTicket`, `ParkingSlot`, and `Vehicle` must remain distinct objects on the heap. Only shared sequences, global constants, and stateless utility functions should be declared `static`.

---

## 3. Object Lifecycle and Garbage Collection

The lifecycle of an object in Java consists of four distinct stages:

```
[ 1. Allocation & Construction ] ──> [ 2. Active Session ] ──> [ 3. Session Completed ] ──> [ 4. Dereferencing & GC ]
  new ParkingTicket(...)               ticket.isActive() == true      ticket.closeTicket()            ref = null;
  Memory allocated on Heap             Referenced by slot/caller      isActive() == false             Object unreachable
  Constructors execute                                                Still strongly reachable!       Eligible for GC
```

### 3.1 What Happens to a `ParkingTicket` After a Vehicle Exits

When a vehicle leaves the facility:
1. The gate or facility invokes `ticket.closeTicket(departureTime)`.
2. The ticket's internal state updates:
   - `this.exitTime` is assigned the departure timestamp.
   - `this.isActive` is set to `false`.
3. The parking fee is computed using `ticket.getDurationMinutes()` and the vehicle type.
4. **Physical parking session is completed**, but the Java object itself **still resides in memory**.

### 3.2 When an Object Becomes Eligible for Garbage Collection

An object on the JVM Heap becomes eligible for garbage collection (GC) **if and only if it becomes unreachable from any GC Root**.

**GC Roots** in the Java Virtual Machine include:
- Local variables currently referenced in active thread stack frames.
- Active Java `Thread` objects.
- Static reference variables in loaded classes.
- JNI (Java Native Interface) global or local references.

If there exists any path of strong references from any GC Root to the `ParkingTicket` object, the JVM garbage collector considers the object **live** and will not reclaim its memory.

### 3.3 Distinguishing "Session Completed" from "Eligible for Garbage Collection"

A common misconception among beginner Java programmers is that closing a ticket or marking it inactive removes it from memory:

```java
// Step 1: Vehicle departs and ticket is closed
ParkingTicket t1 = northGate.issueTicketShared("TN01AA1111", "CAR", 0, 1);
t1.closeTicket(LocalDateTime.now()); // State change: isActive = false

// IS t1 ELIGIBLE FOR GARBAGE COLLECTION HERE?
// ANSWER: NO!
```

- **Domain State Change (`isActive = false`):** The ticket simply reflects that the vehicle has departed and payment has been processed. The reference variable `t1` on the current thread's stack still points directly to the `ParkingTicket` object on the heap. Because it is reachable from a live stack frame, it is **strongly reachable**.
- **Garbage Collection Eligibility:** For the object to become eligible for garbage collection, all references pointing to it must be severed:

```java
t1 = null; // Dereferenced!
// Now the ParkingTicket object on the heap has NO incoming references from any GC Root.
// ONLY NOW does it become eligible for garbage collection.
```

Similarly, if completed tickets are stored in an audit log (e.g., `List<ParkingTicket> departureHistory`), the objects remain strongly reachable through that collection and will **not** be garbage-collected, regardless of whether their sessions are completed.

### 3.4 Garbage Collection Timing Is Not Guaranteed

Garbage collection in Java is **automatic, asynchronous, and non-deterministic**:
- An object becoming *eligible* for GC does **not** mean it is *immediately collected*.
- The garbage collector runs when the JVM deems it necessary—typically when memory thresholds in the Young Generation (Eden space) are exceeded or allocation pressure increases.
- If an application has abundant heap space and terminates quickly, unreferenced objects may never be collected during the process lifetime.

### 3.5 Clarification on `System.gc()`

> [!WARNING]
> Calling `System.gc()` or `Runtime.getRuntime().gc()` does **NOT** guarantee that garbage collection will run.

According to the Java Language Specification (JLS §9.9) and JVM Specification:
1. `System.gc()` is purely a **hint or suggestion** to the JVM that it might be an opportune time to exert effort toward recycling unused objects.
2. The JVM is entirely free to ignore this request, defer it, or perform only a partial collection.
3. In production environments, JVM flags such as `-XX:+DisableExplicitGC` frequently disable `System.gc()` calls entirely to prevent unpredictable stop-the-world pauses.
4. Professional Java systems must never rely on `System.gc()` for correctness, resource management, or testing.

---

## 4. Passing and Returning Objects

### 4.1 Java Passes Everything by Value (Pass-by-Value)

> [!IMPORTANT]
> **Java is strictly pass-by-value.** There is no pass-by-reference in Java.

When an object reference is passed to a method:
- The parameter variable receives a **copy of the reference value** (the 32-bit or 64-bit memory pointer address).
- Both the caller's variable and the callee's parameter variable hold identical reference values pointing to the **exact same object on the heap**.

```
Stack Frame: main()                     JVM Heap
+--------------------+                  +-----------------------------------------+
| t1: 0x4A10 --------+----------------->| ParkingTicket Object at 0x4A10          |
+--------------------+                  |   ticketNumber = "TKT-1001"             |
                                        |   isActive = true -> mutated to false   |
Stack Frame: completeParkingSession()   |   exitTime = null -> mutated to now     |
+--------------------+                  +-----------------------------------------+
| ticket: 0x4A10 ----+-----------------> (Exact same heap object)
+--------------------+
```

### 4.2 Demonstrating In-Place Mutation via Object References

Because both references point to the same heap object, modifying the object's internal fields through methods on the parameter mutates the underlying heap object. The caller observes these changes immediately upon method return:

```java
public static void completeParkingSession(ParkingTicket ticket, LocalDateTime departureTime) {
    // Both caller and callee reference the same heap instance
    ticket.closeTicket(departureTime); // Mutates fields on the heap object
}
```

```java
// Caller demonstration:
ParkingTicket myTicket = northGate.issueTicketShared("TN01A1000", "CAR", 0, 1);
System.out.println(myTicket.isActive()); // true

completeParkingSession(myTicket, LocalDateTime.now());
System.out.println(myTicket.isActive()); // false (Caller sees the modification!)
```

### 4.3 Demonstrating Reference Reassignment Immunity

When a method reassigns its parameter variable, it only changes the local copy of the reference on its own stack frame. The caller's variable remains completely unaffected:

```java
public static void attemptReferenceReassignment(ParkingTicket ticket, ParkingTicket replacement) {
    // Reassigns the local parameter variable on this method's stack frame only
    ticket = replacement;
}
```

```java
// Caller demonstration:
ParkingTicket original = northGate.issueTicketShared("TN01ORIG", "CAR", 0, 0);
ParkingTicket replacement = new ParkingTicket("TKT-9999", "TN01REPL", "CAR", 1, 1);

attemptReferenceReassignment(original, replacement);

// The caller's reference variable 'original' still points to the original ticket!
System.out.println(original.getTicketNumber()); // Still "TKT-1001", NOT "TKT-9999"
```

If Java had true pass-by-reference (like C++ `&` references or C# `ref`), `original` would have been redirected to `replacement`. Because Java passes the reference *value*, `original` remains unaffected.

### 4.4 Demonstrating Returning an Object Reference

When a method returns an object, it pushes a copy of the heap reference address onto the caller's stack:

```java
public static ParkingTicket issueArchivalCopy(ParkingTicket source) {
    if (source == null) {
        throw new IllegalArgumentException("Source ticket cannot be null.");
    }
    // Allocates a new distinct object on the heap and returns its reference
    return new ParkingTicket(source);
}
```

```java
ParkingTicket copy = issueArchivalCopy(myTicket);
// 'copy' and 'myTicket' have identical field contents, but occupy distinct heap locations
System.out.println(copy == myTicket); // false (Different heap references)
```

---

## 5. Alternative Concept and Complexity Analysis

### 5.1 Problem Scenario: The North & South Gates

Suppose the university operates two distinct parking facilities:
1. **North Gate Facility**
2. **South Gate Facility**

We evaluate two architectural options for ticket counter placement:
- **Design A:** Global `static` counter shared across all facilities.
- **Design B:** Facility-`instance` counter maintained inside each `ParkingFacility` object.

```
DESIGN A: Shared Class Static Counter
+------------------------------------+
| class TicketCounterDemo            |
|   static int sharedCounter = 1003; |
+------------------------------------+
           ^              ^
           |              |
    [ North Gate ]  [ South Gate ]
       TKT-1001        TKT-1002
       TKT-1003

DESIGN B: Per-Facility Instance Counter
+----------------------------+      +----------------------------+
| Facility: "North-Gate"     |      | Facility: "South-Gate"     |
|   instanceCounter = 2      |      |   instanceCounter = 1      |
+----------------------------+      +----------------------------+
       North-Gate-1                        South-Gate-1
       North-Gate-2
```

### 5.2 Architectural Comparison: Static vs. Instance Counter

#### Design A: Global Static Counter
- **Mechanism:** Both `FacilityGate` objects invoke a single shared class-level method `TicketCounterDemo.generateNextTicketId()`.
- **Output:** North Gate issues `TKT-1001`, South Gate issues `TKT-1002`, North Gate issues `TKT-1003`.
- **Behavior:** Both facilities contribute to a single, continuous, monotonically increasing sequence across the entire campus.

#### Design B: Facility-Instance Counter
- **Mechanism:** Each `FacilityGate` holds an instance variable `private int instanceTicketCounter`.
- **Output:**
  - North Gate issues: `North-Gate-1`, `North-Gate-2`, `North-Gate-3`...
  - South Gate issues: `South-Gate-1`, `South-Gate-2`...
- **Behavior:** Each facility maintains an independent sequence starting from 1.

### 5.3 Which Design Is Appropriate?

The choice between Design A and Design B depends strictly on the **system-wide requirement for ticket number uniqueness**:

1. **If Ticket Numbers Must Be Globally Unique Across the Entire Campus:**
   - **Design A (Static Counter)** inherently guarantees campus-wide uniqueness without needing to know which facility issued the ticket.
   - **Design B (Instance Counter)** can only guarantee campus-wide uniqueness if a unique facility identifier prefix (e.g., `"NORTH-"`, `"SOUTH-"`) is prepended to the ticket number. Without such prefixing, North Gate would issue `1` and South Gate would issue `1`, causing a catastrophic duplicate ID collision at campus exit booths.

2. **If Ticket Numbers Only Need to Be Unique Within Each Facility:**
   - **Design B (Instance Counter)** is superior because it fully decouples the two facilities. If North Gate operates at high volume, its counter increments without altering South Gate's sequence.

3. **Multi-Facility Independence & Concurrency:**
   - A static counter introduces a shared synchronization point between facilities. In multi-threaded environments, every gate must acquire a lock on the static counter. An instance counter isolates gate operations from one another.

### 5.4 Space Complexity Analysis for $k$ Facilities and $n$ Tickets

Let:
- $k$ be the number of `ParkingFacility` objects (e.g., $k = 2$ for North and South gates).
- $n$ be the total number of `ParkingTicket` objects issued across all facilities over time.

#### 1. Space Used by the Ticket Counter

| Counter Placement | Stored In | Space Complexity | Exact Memory Cost |
| :--- | :--- | :---: | :--- |
| **Static Counter (Design A)** | Metaspace / Class Data | $\mathcal{O}(1)$ | Exactly **1 integer** (4 bytes) allocated once for the class, regardless of how many facilities ($k$) or tickets ($n$) are created. |
| **Instance Counter (Design B)** | Heap (embedded in each `ParkingFacility` object) | $\mathcal{O}(k)$ | Exactly **$k$ integers** ($4 \times k$ bytes). Each facility instance reserves 4 bytes for its internal counter field. |

Because the number of parking facilities on a campus is very small ($k \ll n$, typically $k \le 10$), both $\mathcal{O}(1)$ and $\mathcal{O}(k)$ consume negligible memory.

#### 2. Space Used by the Ticket Objects ($n$ Tickets)

The tickets themselves must be analysed separately from the counter:
- Each `ParkingTicket` is an independent object on the heap containing object headers, primitives, and object references (`ticketNumber`, `vehicleRegNo`, `vehicleType`, `entryTime`, `exitTime`, `floorNumber`, `slotNumber`, `isActive`).
- Storing $n$ tickets requires $\mathcal{O}(n)$ total heap memory:
  $$\text{Memory}_{\text{tickets}} = n \times S_{\text{ticket}} = \mathcal{O}(n)$$
  where $S_{\text{ticket}}$ is the memory footprint of a single `ParkingTicket` object (typically $\approx 48\text{--}64$ bytes including object header and field alignments on a 64-bit JVM with compressed OOPs).
- The choice of counter representation ($\mathcal{O}(1)$ vs. $\mathcal{O}(k)$) does **not** alter the $\mathcal{O}(n)$ storage requirement for the $n$ issued tickets.

### 5.5 Trade-Off Summary

| Attribute | Design A: Static Counter | Design B: Facility-Instance Counter |
| :--- | :--- | :--- |
| **Counter Memory** | $\mathcal{O}(1)$ (one integer in Metaspace) | $\mathcal{O}(k)$ ($k$ integers on Heap) |
| **Ticket Memory** | $\mathcal{O}(n)$ on Heap | $\mathcal{O}(n)$ on Heap |
| **Global Uniqueness** | Guaranteed by design | Requires facility prefix to prevent collisions |
| **Facility Decoupling** | High coupling (shared class state) | Decoupled (each facility is self-contained) |
| **Concurrency Impact** | Contention on global counter across gates | Contention isolated to individual gate |
| **Campus Querying** | Immediate total ticket count via static query | Requires summing all $k$ facility counters |

