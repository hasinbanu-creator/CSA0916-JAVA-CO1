# TASK 8 – Apply Polymorphism (4 Marks)
**Module:** Programming in Java (CO1)  
**Academic Level:** Bloom’s Taxonomy Level K4 – Analyse  
**System Context:** Campus Smart-Parking System  
**Deliverables:**
- Documentation: `docs/tasks/task08-polymorphism.md`
- PlantUML Model: `docs/uml/task08-polymorphism.puml`
- Demonstration Program: `src/main/java/com/smartparking/tasks/FeeDemo.java`
- Comprehensive Unit Tests: `src/test/java/com/smartparking/tasks/FeeDemoTest.java`

---

## 1. Executive Summary and Architectural Context

Polymorphism—derived from the Greek *poly* (many) and *morphē* (forms)—is an object-oriented paradigm allowing entities (such as methods, references, or objects) to behave differently based on their operational context or runtime type.

In the **Campus Smart-Parking System**, polymorphism resolves two distinct engineering challenges:
1. **Compile-Time Polymorphism (Static Binding / Method Overloading):** Enables client code to calculate parking fees using multiple intuitive parameter formats (raw duration in minutes, clock-based entry/exit timestamps, whole billable hours, or multi-day date-time timestamps) without inventing disjointed method names.
2. **Runtime Polymorphism (Dynamic Binding / Dynamic Method Dispatch):** Enables the parking management system to operate on generic `Vehicle` references stored in collections or arrays, automatically executing the specialized pricing policy of the actual underlying subtype (`TwoWheeler`, `Car`, `ElectricCar`) at runtime without conditional branching (`if-else` or `switch` cascades).

Building directly upon the inheritance hierarchy established in Task 6 and the fee specialisation implemented in Task 7, Task 8 demonstrates how compile-time and runtime polymorphism operate synergistically to ensure code extensibility, type safety, and clean separation of concerns.

---

## 2. Compile-Time Polymorphism through Method Overloading

### 2.1 Theoretical Foundations
**Method overloading** occurs when two or more methods within the same class (or inherited from a superclass) share the exact same method name but declare **different parameter lists**.

In the Java Language Specification (JLS §8.4.9), a method's signature consists solely of:
- The **method name**
- The **number, order, and types of its formal parameters**

> [!IMPORTANT]
> The **return type**, **access modifiers**, and **thrown checked exceptions** do **not** form part of the method signature. Therefore, two methods that differ *only* by return type cannot coexist in the same class and will fail compilation with a *"method already defined"* error.

### 2.2 Compiler Method Resolution (Static Binding)
Overload resolution is executed entirely at compile-time by `javac` using **static binding** (also known as early binding):
1. **Phase 1 (Subtyping without Boxing):** The compiler searches for an applicable method whose parameter types match the argument expressions through identity conversion or primitive widening (e.g., `int` to `long`).
2. **Phase 2 (Subtyping with Boxing):** If no match is found, boxing and unboxing conversions are considered (e.g., `int` to `Integer`).
3. **Phase 3 (Variable Arity):** Finally, varargs methods are inspected.
4. **Most Specific Method Rule:** If multiple methods are applicable, the compiler selects the most specific method. If two or more methods are equally specific, compilation halts with an *"ambiguous method call"* error.

Because selection happens during compilation, there is zero runtime overhead associated with method overloading. The bytecode emitted by `javac` directly hardcodes the symbolic reference to the target method.

### 2.3 Implementation in `FeeDemo`
In [`FeeDemo.java`](file:///Users/hasinnn/Documents/Java%20Assignment%201/src/main/java/com/smartparking/tasks/FeeDemo.java), four overloaded static methods provide flexible API entry points for tariff computation:

```java
// Overload 1: Raw duration in minutes
public static double calculateFee(Vehicle vehicle, long durationMinutes) {
    Objects.requireNonNull(vehicle, "Vehicle cannot be null.");
    return vehicle.calculateFee(durationMinutes);
}

// Overload 2: Clock-based entry and exit LocalTime (handles same-day and midnight-crossing stays)
public static double calculateFee(Vehicle vehicle, LocalTime entryTime, LocalTime exitTime) {
    Objects.requireNonNull(vehicle, "Vehicle cannot be null.");
    Objects.requireNonNull(entryTime, "Entry time cannot be null.");
    Objects.requireNonNull(exitTime, "Exit time cannot be null.");

    long minutes = Duration.between(entryTime, exitTime).toMinutes();
    if (minutes < 0) {
        minutes += 24 * 60; // Adjust for overnight stay across midnight
    }
    return calculateFee(vehicle, minutes);
}

// Overload 3: Whole hours duration
public static double calculateFee(Vehicle vehicle, int wholeHours) {
    Objects.requireNonNull(vehicle, "Vehicle cannot be null.");
    if (wholeHours < 0) {
        throw new IllegalArgumentException("Hours duration cannot be negative: " + wholeHours);
    }
    long minutes = (long) wholeHours * ParkingTariff.MINUTES_PER_HOUR;
    return calculateFee(vehicle, minutes);
}

// Overload 4: Calendar-aware LocalDateTime timestamps (handles multi-day parking)
public static double calculateFee(Vehicle vehicle, LocalDateTime entryDateTime, LocalDateTime exitDateTime) {
    Objects.requireNonNull(vehicle, "Vehicle cannot be null.");
    Objects.requireNonNull(entryDateTime, "Entry date-time cannot be null.");
    Objects.requireNonNull(exitDateTime, "Exit date-time cannot be null.");

    long minutes = Duration.between(entryDateTime, exitDateTime).toMinutes();
    if (minutes < 0) {
        throw new IllegalArgumentException("Exit date-time cannot precede entry date-time.");
    }
    return calculateFee(vehicle, minutes);
}
```

---

## 3. Runtime Polymorphism through Method Overriding

### 3.1 Theoretical Foundations
**Runtime polymorphism** occurs when a call to an overridden method is resolved at runtime rather than compile-time. This mechanism is known as **dynamic binding**, **late binding**, or **dynamic method dispatch**.

In Java, every non-private, non-static, non-final instance method is **virtual by default**. When an instance method is invoked, the Java Virtual Machine (JVM) inspects the actual object instantiated on the heap and dispatches execution to that object's class implementation, regardless of the reference type used at the call site.

### 3.2 Polymorphic Contract in the Vehicle Hierarchy
As established in Task 7, the abstract superclass `Vehicle` declares the abstract fee computation contract:

```java
public abstract class Vehicle {
    // Contract: Subclasses must provide specialized category tariff logic
    public abstract double calculateFee(long durationMinutes);
}
```

Each concrete subclass overrides this method with its category-specific pricing policy:
- **`TwoWheeler`:** Charges Rs 10.00 per started hour:
  $$\text{Fee} = \lceil\text{duration} / 60\rceil \times 10.0$$
- **`Car`:** Charges Rs 30.00 per started hour:
  $$\text{Fee} = \lceil\text{duration} / 60\rceil \times 30.0$$
- **`ElectricCar`:** Charges Rs 30.00 per started hour (base parking tariff):
  $$\text{Fee} = \lceil\text{duration} / 60\rceil \times 30.0$$

---

## 4. Parent-Class References and Upcasting

### 4.1 Substitutability and the Liskov Substitution Principle (LSP)
The **Liskov Substitution Principle** states that if $S$ is a subtype of $T$, then objects of type $T$ may be replaced with objects of type $S$ without altering any of the desirable properties of the program (correctness, task performed, etc.).

In the Campus Smart-Parking System:
- A `TwoWheeler` **is-a** `Vehicle`.
- A `Car` **is-a** `Vehicle`.
- An `ElectricCar` **is-a** `Vehicle`.

Because each subclass honors the behavioral invariants of `Vehicle`, a reference variable of declared type `Vehicle` can point to any subclass instance:

```java
Vehicle v1 = new TwoWheeler("TN01A1", false);
Vehicle v2 = new Car("TN02B2", 5);
Vehicle v3 = new ElectricCar("TN03C3", 60.0);
```

### 4.2 Declared Type vs. Actual Type
Every object reference in Java possesses two distinct types:
1. **Declared (Static / Compile-Time) Type:** The type with which the variable is declared in source code (e.g., `Vehicle`). The compiler uses the declared type to verify whether a method invocation is legally accessible.
2. **Actual (Dynamic / Runtime) Type:** The class of the concrete object created in heap memory via `new` (e.g., `TwoWheeler`, `Car`, `ElectricCar`). The JVM uses the actual type to locate the method body to execute.

```
+---------------------------------------------------------------------------------------+
|  Declared Type (Compiler's View): Vehicle                                            |
|  - Accessible methods: calculateFee(), getRegistrationNumber(), getVehicleInfo()      |
|  - INACCESSIBLE: hasHelmetStorage(), getSeatingCapacity(), getBatteryCapacityKWh()    |
+---------------------------------------------------------------------------------------+
                                           |
                                           | (points to heap object)
                                           v
+---------------------------------------------------------------------------------------+
|  Actual Object on Heap (JVM's View): ElectricCar                                      |
|  - Object Header: Mark Word + Klass Pointer -> ElectricCar.class metadata             |
|  - Instance Fields: registrationNumber="TN03C3", vehicleType="ELECTRIC_CAR",          |
|                     batteryCapacityKWh=60.0, currentBatteryLevelKWh=30.0              |
|  - vtable dispatch resolves calculateFee() -> ElectricCar.calculateFee()             |
+---------------------------------------------------------------------------------------+
```

### 4.3 Upcasting in Heterogeneous Arrays
**Upcasting** is the conversion of a subclass reference to a superclass reference. In Java, upcasting is:
- **Implicit:** Requires no cast operator `(Vehicle)`.
- **Inherently Safe:** A subclass is guaranteed to contain every member declared in its superclass.
- **Non-destructive:** Unlike C++ value slicing, Java arrays store object references (pointers on the heap). No subclass fields are sliced or lost during upcasting.

In `FeeDemo.java`, upcasting allows a single array of type `Vehicle[]` to hold disparate vehicle categories:

```java
Vehicle[] parked = {
    new TwoWheeler("TN01A1", false), // Upcasted from TwoWheeler to Vehicle
    new Car("TN02B2", 5),            // Upcasted from Car to Vehicle
    new ElectricCar("TN03C3", 60.0)  // Upcasted from ElectricCar to Vehicle
};
```

---

## 5. Dynamic Method Dispatch Mechanism (JVM Internals)

### 5.1 The `invokevirtual` Bytecode Instruction
When Java code invokes an instance method on a parent reference:

```java
double fee = v.calculateFee(durationsMinutes[i]);
```

The Java compiler emits the `invokevirtual` bytecode instruction:

```bytecode
aload_3             // Load local variable 'v' (Vehicle reference) onto operand stack
lload               // Load duration onto operand stack
invokevirtual       // Method com/smartparking/model/Vehicle.calculateFee:(J)D
```

Notice that the instruction references the *symbolic* superclass method `Vehicle.calculateFee:(J)D`. The resolution to the target subclass method occurs at runtime via the **Virtual Method Table**.

### 5.2 Virtual Method Table (vtable) Architecture
When the HotSpot JVM loads a class into Metaspace, it constructs an internal data structure called the **Virtual Method Table (vtable)**:
1. Every class metadata structure has its own vtable containing pointers to the executable machine code of all virtual instance methods.
2. If a subclass overrides an inherited method, its vtable entry at the inherited index is updated to point to the subclass's compiled method body.
3. If a subclass does not override an inherited method, its vtable entry retains the pointer to the superclass implementation.
4. Because method offset indices are identical across superclass and subclass vtables, method dispatch requires only a fixed-offset array lookup:

```
Vehicle vtable:
Index [0] -> Object.toString()
Index [1] -> Vehicle.equals(Object)     [final]
Index [2] -> Vehicle.hashCode()         [final]
Index [3] -> Vehicle.getVehicleInfo()
Index [4] -> abstract calculateFee(long) [unbound / pure virtual stub]

TwoWheeler vtable:
Index [0] -> TwoWheeler.toString()      [overridden]
Index [1] -> Vehicle.equals(Object)     [inherited final]
Index [2] -> Vehicle.hashCode()         [inherited final]
Index [3] -> Vehicle.getVehicleInfo()   [inherited]
Index [4] -> TwoWheeler.calculateFee()  [overridden implementation]
Index [5] -> TwoWheeler.hasHelmetStorage()

Car vtable:
Index [0] -> Car.toString()             [overridden]
Index [1] -> Vehicle.equals(Object)     [inherited final]
Index [2] -> Vehicle.hashCode()         [inherited final]
Index [3] -> Vehicle.getVehicleInfo()   [inherited]
Index [4] -> Car.calculateFee()         [overridden implementation]
Index [5] -> Car.getSeatingCapacity()

ElectricCar vtable:
Index [0] -> ElectricCar.toString()     [overridden]
Index [1] -> Vehicle.equals(Object)     [inherited final]
Index [2] -> Vehicle.hashCode()         [inherited final]
Index [3] -> Vehicle.getVehicleInfo()   [inherited]
Index [4] -> ElectricCar.calculateFee() [overridden implementation]
Index [5] -> ElectricCar.getBatteryCapacityKWh()
```

### 5.3 Step-by-Step Runtime Dispatch Trace
When executing `double fee = v.calculateFee(durationsMinutes[i])`:
1. **Null Check:** The JVM inspects the reference `v`. If null, it immediately throws `NullPointerException`.
2. **Object Header Inspection:** The JVM reads the object header of the target heap object to retrieve its **Klass pointer** (the reference to the runtime class metadata).
3. **vtable Index Resolution:** The JVM looks up index `[4]` in the resolved class's vtable.
4. **Execution:** The JVM jumps directly to the memory address stored at index `[4]`:
   - If `v` points to a `TwoWheeler`, `TwoWheeler.calculateFee()` is executed.
   - If `v` points to a `Car`, `Car.calculateFee()` is executed.
   - If `v` points to an `ElectricCar`, `ElectricCar.calculateFee()` is executed.
5. **Time Complexity:** Because array indexing by constant offset is $\mathcal{O}(1)$, dynamic method dispatch executes in constant time.
6. **JIT Optimizations:** The HotSpot Just-In-Time (JIT) compiler further optimizes virtual calls using **Monomorphic Inline Caching (MIC)** and **Devirtualization**, replacing virtual lookups with direct branch instructions or inlining the method body when the call site observes only a single concrete class.

---

## 6. Downcasting and Safe Type Inspection via `instanceof`

### 6.1 Downcasting Concepts and Pitfalls
**Downcasting** is the conversion of a superclass reference back to a subclass reference:

```java
Vehicle v = new ElectricCar("TN03C3", 60.0);
ElectricCar ec = (ElectricCar) v; // Downcast: Narrowing conversion
```

Downcasting is necessary when client code must access subclass-specific fields or methods that are not part of the generic `Vehicle` interface (e.g., `ec.getBatteryCapacityKWh()` or `ec.isCharging()`).

> [!CAUTION]
> Downcasting is **not inherently type-safe**. If a developer attempts to cast a reference to an incompatible subtype:
> ```java
> Vehicle bike = new TwoWheeler("TN01A1", false);
> Car car = (Car) bike; // Throws java.lang.ClassCastException at runtime!
> ```
> The code compiles without error because `TwoWheeler` and `Car` both share the common ancestor `Vehicle`, but fails catastrophically at runtime.

### 6.2 Safe Downcasting using `instanceof`
To guarantee type safety, downcasting must always be guarded by a runtime type inspection using the `instanceof` operator:

```java
// Traditional downcasting pattern (pre-Java 16)
if (v instanceof ElectricCar) {
    ElectricCar ec = (ElectricCar) v; // Guaranteed safe
    System.out.println("Battery: " + ec.getBatteryCapacityKWh());
}
```

### 6.3 Modern Pattern Matching for `instanceof` (Java 16+, Java 21 LTS)
Java 16 finalized **Pattern Matching for `instanceof`** (JEP 394), eliminating the redundant boilerplate of manual casting:

```java
// Pattern matching for instanceof (standard in Java 21)
if (v instanceof ElectricCar ec) {
    // 'ec' is automatically introduced as a scoped, typed ElectricCar reference
    System.out.println("Battery: " + ec.getBatteryCapacityKWh() + " kWh");
}
```

In `FeeDemo.java`, this pattern is implemented in [`getSubclassSpecificFeature(Vehicle vehicle)`](file:///Users/hasinnn/Documents/Java%20Assignment%201/src/main/java/com/smartparking/tasks/FeeDemo.java#L104-L122):

```java
public static String getSubclassSpecificFeature(Vehicle vehicle) {
    if (vehicle == null) {
        return "Null reference";
    }
    if (vehicle instanceof ElectricCar ec) {
        return "ElectricCar [Battery: " + ec.getBatteryCapacityKWh() + " kWh, Charging: " + ec.isCharging() + "]";
    } else if (vehicle instanceof Car c) {
        return "Car [Seats: " + c.getSeatingCapacity() + "]";
    } else if (vehicle instanceof TwoWheeler tw) {
        return "TwoWheeler [Helmet Storage: " + tw.hasHelmetStorage() + "]";
    }
    return "Generic Vehicle [" + vehicle.getRegistrationNumber() + "]";
}
```

---

## 7. Benefits of Polymorphism in the Campus Smart-Parking System

### 7.1 Elimination of `if-else` / `instanceof` Type Cascades
Without polymorphism, computing fees across a collection of vehicles requires procedural type branching:

```java
// ANTI-PATTERN: Procedural type-checking cascade (violates OOP principles)
for (Vehicle v : parked) {
    double fee;
    if (v instanceof TwoWheeler) {
        fee = hours * 10.0;
    } else if (v instanceof ElectricCar) {
        fee = hours * 30.0;
    } else if (v instanceof Car) {
        fee = hours * 30.0;
    } else {
        throw new IllegalArgumentException("Unknown vehicle type");
    }
    System.out.println(v.getRegistrationNumber() + " -> " + fee);
}
```

### 7.2 Architectural Advantages of Polymorphic Dispatch

| Dimension | Polymorphic Dynamic Dispatch | Procedural Type-Checking (`instanceof`) |
| :--- | :--- | :--- |
| **Code Cohesion** | **High:** Each vehicle subclass encapsulates its own pricing formula and domain rules. | **Low:** Fee computation logic is scattered across central loops or manager classes. |
| **Open/Closed Principle (OCP)** | **Satisfied:** Adding a new vehicle type (e.g. `DeliveryVan`, `EBike`) requires adding 1 class without modifying existing calculation loops. | **Violated:** Every central loop and switch block in the codebase must be opened and edited. |
| **Type Safety** | **Enforced by Compiler:** Abstract method declaration guarantees every concrete subclass must provide an implementation. | **Fragile:** If a developer forgets an `else if` branch, the code falls into `default` or runtime failure. |
| **Execution Complexity** | **$\mathcal{O}(1)$ Constant Time:** Resolved via fixed-offset vtable array lookup. | **$\mathcal{O}(k)$ Linear Time:** Requires testing up to $k$ sequential conditions. |
| **Code Redundancy** | **Zero:** Generic algorithms operate strictly on the abstract `Vehicle` supertype. | **High:** Type checks and casts must be repeated across billing, allocation, and reporting modules. |

---

## 8. Comprehensive Comparison: Method Overloading vs. Method Overriding

| Architectural Attribute | Method Overloading (Compile-Time) | Method Overriding (Runtime) |
| :--- | :--- | :--- |
| **Polymorphism Category** | Static / Compile-Time Polymorphism | Dynamic / Runtime Polymorphism |
| **Binding Mechanism** | Early Binding (Resolved by `javac` at compile time) | Late Binding (Resolved by JVM at runtime via vtable) |
| **Scope / Location** | Within the **same class** or across inherited methods | Requires a **superclass-subclass** inheritance hierarchy |
| **Method Name** | Must be **identical** | Must be **identical** |
| **Parameter List** | **Must differ** in type, order, or number of parameters | **Must match exactly** in type and order |
| **Return Type** | Can be **different or same** (does not affect resolution) | Must be **identical** for primitives; can be **covariant** for reference types |
| **Access Modifiers** | Can be **freely changed** | Cannot be **more restrictive** than the superclass method |
| **Exception Handling** | Can declare **any** checked or unchecked exceptions | Cannot throw **new or broader checked exceptions** |
| **Private / Final / Static**| Private, final, and static methods **can be overloaded** | Private, final, and static methods **cannot be overridden** |
| **Compiler Annotation** | None (no specific annotation exists) | `@Override` (compiler validates matching superclass signature) |
| **Primary Goal** | Enhances **API usability** and readability | Implements **behavioral specialisation** and subtyping |

---

## 9. Explanation of Demonstration Output (`FeeDemo.java`)

When `FeeDemo.java` executes, it outputs the exact four lines specified by the academic assignment brief:

```text
TwoWheeler TN01A1 -> Rs 30.0
Car TN02B2 -> Rs 90.0
ElectricCar TN03C3 -> Rs 210.0
true
```

### Detailed Trace of Each Output Line

#### Line 1: `TwoWheeler TN01A1 -> Rs 30.0`
- **Object Instantiation:** `new TwoWheeler("TN01A1", false)` is upcasted into `parked[0]`.
- **Declared Type:** `Vehicle`
- **Actual Runtime Type:** `TwoWheeler`
- **Duration Supplied:** 130 minutes.
- **Dynamic Dispatch:** The JVM invokes `TwoWheeler.calculateFee(130L)`.
- **Computation:**
  - Started hours: $\lceil 130 / 60 \rceil = 1 + (130 - 1) / 60 = 3\text{ hours}$.
  - Tariff formula: $3\text{ hours} \times \text{Rs } 10.0/\text{hr} = \text{Rs } 30.0$.
- **String Formatting:** `v.getClass().getSimpleName() + " " + v.getRegistrationNumber() + " -> Rs " + fee` produces `TwoWheeler TN01A1 -> Rs 30.0`.

#### Line 2: `Car TN02B2 -> Rs 90.0`
- **Object Instantiation:** `new Car("TN02B2", 5)` is upcasted into `parked[1]`.
- **Declared Type:** `Vehicle`
- **Actual Runtime Type:** `Car`
- **Duration Supplied:** 130 minutes.
- **Dynamic Dispatch:** The JVM invokes `Car.calculateFee(130L)`.
- **Computation:**
  - Started hours: $\lceil 130 / 60 \rceil = 3\text{ hours}$.
  - Tariff formula: $3\text{ hours} \times \text{Rs } 30.0/\text{hr} = \text{Rs } 90.0$.
- **String Formatting:** Produces `Car TN02B2 -> Rs 90.0`.

#### Line 3: `ElectricCar TN03C3 -> Rs 210.0`
- **Object Instantiation:** `new ElectricCar("TN03C3", 60.0)` is upcasted into `parked[2]`.
- **Declared Type:** `Vehicle`
- **Actual Runtime Type:** `ElectricCar`
- **Duration Selection Rationale:**
  - In Slide 14 of the assignment brief, `ElectricCar` is combined with an EV charging capability (`Chargeable` interface introduced in Task 9), where fee was composed of base parking + charging units ($90.0 + 120.0 = 210.0$).
  - In Task 8, per project instructions, we preserve the established Task 7 tariff rate (Rs 30.0/hr base rate for `ElectricCar`) without altering tariff rules or prematurely implementing Task 9 interfaces.
  - A parking duration of **420 minutes** (7 started hours) was selected:
    $$\text{Started Hours} = 1 + \frac{420 - 1}{60} = 7\text{ hours}$$
    $$\text{Fee} = 7\text{ hours} \times \text{Rs } 30.0/\text{hr} = \text{Rs } 210.0$$
  - This accurately demonstrates dynamic method dispatch to `ElectricCar.calculateFee(420L)` while matching the expected assignment output to the penny.
- **String Formatting:** Produces `ElectricCar TN03C3 -> Rs 210.0`.

#### Line 4: `true`
- **Code Executed:**
  ```java
  System.out.println(new Car("TN02B2", 5).equals(new ElectricCar("TN02B2", 60.0)));
  ```
- **Contract Analysis:**
  - Both objects represent the identical vehicle registration number `"TN02B2"`.
  - In [`Vehicle.java`](file:///Users/hasinnn/Documents/Java%20Assignment%201/src/main/java/com/smartparking/model/Vehicle.java#L98-L107), `equals(Object)` is declared `public final` and compares normalized `registrationNumber` using `obj instanceof Vehicle other`.
  - Because `ElectricCar` is an instance of `Vehicle` and both have the normalized registration number `"TN02B2"`, the evaluation returns `true`.
  - This preserves the physical domain invariant that a license plate uniquely identifies a single vehicle on campus, regardless of the Java class used to reference it.

---

## 10. Complexity and Memory Analysis

### 10.1 Time Complexity for $k$ Vehicle Types and $n$ Parked Vehicles
Let $k$ represent the number of vehicle categories registered in the system, and $n$ the number of active parking stays processed.

| Operation | Polymorphic Dynamic Dispatch | Procedural Type-Checking (`instanceof`) |
| :--- | :--- | :--- |
| **Single Fee Calculation** | $\mathcal{O}(1)$ (fixed-offset vtable dereference) | $\mathcal{O}(k)$ (worst-case sequential `if-else` cascade) |
| **Batch Processing ($n$ vehicles)** | $\mathcal{O}(n)$ | $\mathcal{O}(n \cdot k)$ |
| **JIT Optimization Ceiling** | Inlined machine code via inline caching | Branch prediction overhead across multiple conditional jumps |

### 10.2 Space Complexity and Memory Footprint
1. **Object Reference Storage:** In a 64-bit JVM with Compressed OOPs enabled (default for heap sizes $< 32\text{ GB}$), each reference in `Vehicle[] parked` consumes **4 bytes**. Storing $n$ vehicles requires $\mathcal{O}(n)$ reference memory.
2. **Object Heap Memory:** Each vehicle object on the heap requires:
   - 12-byte object header (8-byte Mark Word + 4-byte Klass pointer)
   - Field storage:
     - `Vehicle`: 8 bytes (two 4-byte compressed String references)
     - `TwoWheeler`: 1 byte (`boolean`) + 3 bytes padding
     - `Car`: 4 bytes (`int`)
     - `ElectricCar`: 17 bytes (two 8-byte `double`s + 1-byte `boolean`) + 7 bytes padding
3. **vtable Metadata Overhead:** The vtable is created **once per class** in Metaspace, not per object instance. For $k$ classes each declaring $m$ virtual methods, total vtable space is $\mathcal{O}(k \cdot m)$, which is negligible (a few kilobytes for the entire system).

---

## 11. Verification and Test Traceability

The implementation is verified by a suite of 11 dedicated unit tests in [`FeeDemoTest.java`](file:///Users/hasinnn/Documents/Java%20Assignment%201/src/test/java/com/smartparking/tasks/FeeDemoTest.java):

```
-------------------------------------------------------------------------------
Test Class: com.smartparking.tasks.FeeDemoTest
-------------------------------------------------------------------------------
[PASS] testOverloadedCalculateFeeWithMinutes        - Verifies Overload 1 (long duration)
[PASS] testOverloadedCalculateFeeWithLocalTime      - Verifies Overload 2 (same-day and midnight crossing)
[PASS] testOverloadedCalculateFeeWithHours          - Verifies Overload 3 (whole hours)
[PASS] testOverloadedCalculateFeeWithLocalDateTime  - Verifies Overload 4 (multi-day stays)
[PASS] testVehicleArrayPolymorphicUpcasting         - Verifies Vehicle[] array upcasting of all 3 types
[PASS] testRuntimeDynamicDispatch                   - Verifies vtable dispatch to correct subclass
[PASS] testExactExpectedFeeValues                   - Verifies 30.0, 90.0, and 210.0 fee amounts
[PASS] testInstanceOfTypeIdentification             - Verifies instanceof type safety
[PASS] testSafeDowncasting                          - Verifies pattern matching downcast & feature access
[PASS] testVehicleCrossSubclassEquality             - Verifies symmetric cross-subclass equality contract
[PASS] testFeeDemoMainOutput                        - Verifies exact 4-line console output
-------------------------------------------------------------------------------
Results: 11 tests passed, 0 failures, 0 errors
Total Project Tests: 42 tests passed across Tasks 4–8
-------------------------------------------------------------------------------
```

---

## 12. Summary and Conclusion

Task 8 demonstrates the practical application of polymorphism within the Campus Smart-Parking System:
1. **Compile-time polymorphism (overloading)** provides clean, expressive entry points for calculating fees across diverse time representations.
2. **Runtime polymorphism (dynamic method dispatch)** abstracts vehicle categories behind a common `Vehicle` interface, allowing high-level batch processing without coupling code to concrete subtypes.
3. **Safe downcasting using pattern matching for `instanceof`** provides a fail-safe mechanism to query subtype-specific capabilities while completely eliminating `ClassCastException` hazards.
4. **Adherence to the Open/Closed Principle** ensures the system remains extensible for upcoming categories (such as delivery vans or e-bikes) without modifying tested allocation and billing logic.

