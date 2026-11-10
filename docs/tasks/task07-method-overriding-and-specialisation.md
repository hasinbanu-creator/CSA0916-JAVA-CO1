# Task 7: Override and Specialise Behaviour

---

## 1. Task Objective

The objective of Task 7 is to implement and analyse **method overriding**, **behavioural specialisation**, the **`final` keyword**, and **`Object`-class method contracts** (`toString()`, `equals()`, `hashCode()`) within the Campus Smart-Parking System's vehicle hierarchy.

Building upon the hierarchical inheritance foundation established in Task 6, Task 7 enables each concrete vehicle subclass (`TwoWheeler`, `Car`, `ElectricCar`) to specialize fee calculation while ensuring that identity and encapsulation invariants remain safeguarded across the entire object-oriented model.

---

## 2. Method Overriding and Specialisation

### 2.1 The Need for Method Overriding in the Parking Facility

In a campus smart-parking facility, different vehicle categories occupy different physical footprints and consume distinct infrastructure resources:
- Two-wheelers occupy a fraction of a standard bay and incur lower maintenance costs.
- Passenger automobiles occupy standard parking bays.
- Electric automobiles utilize bays with potential charging access.

A single uniform fee algorithm cannot adequately represent these differing operational realities. By defining an abstract method in the base class:

```java
public abstract double calculateFee(long durationMinutes);
```

the superclass `Vehicle` establishes a formal contract that every concrete vehicle entering the campus **must** be capable of calculating its parking fee, while delegating the specific tariff calculation to each subclass.

### 2.2 Subclass Specialisation

Each subclass overrides `calculateFee(long durationMinutes)` to execute its own category-specific pricing logic:
1. **`TwoWheeler`:** Overrides `calculateFee()` to apply the two-wheeler tariff (Rs 10.0 per started hour).
2. **`Car`:** Overrides `calculateFee()` to apply the standard automobile tariff (Rs 30.0 per started hour).
3. **`ElectricCar`:** Overrides `calculateFee()` to apply the base electric car parking tariff (Rs 30.0 per started hour).

---

## 3. The `@Override` Annotation and Java Overriding Rules

The `@Override` annotation informs the Java compiler that the annotated method is intended to override a method declared in a superclass or interface.

### 3.1 Benefits of the `@Override` Annotation
1. **Compile-Time Safety:** If a developer makes a typographical error in the method name (e.g., `calculateFees`) or modifies parameter types (e.g., `int` instead of `long`), the compiler raises an immediate error (`method does not override or implement a method from a supertype`).
2. **Protection Against Fragile Base Classes:** If the superclass method signature changes during refactoring, all subclass methods marked with `@Override` will immediately flag compilation errors, preventing silent breakage.
3. **Self-Documenting Code:** Explicitly signals to code maintainers that this method participates in polymorphic dispatch.

### 3.2 Java Rules for Method Overriding

For an overriding method to be valid under the Java Language Specification (JLS §8.4.8):

| Rule | Requirement | Explanation / Parking System Context |
| :--- | :--- | :--- |
| **1. Signature Matching** | Exact Match | The method name and parameter types must be identical. `calculateFee(long durationMinutes)` in `TwoWheeler` exactly matches `calculateFee(long durationMinutes)` in `Vehicle`. |
| **2. Return Type** | Invariant or Covariant | The return type must be identical to, or a subtype (covariant) of, the return type declared in the superclass. In our system, all return types are `double`. |
| **3. Access Modifier** | Cannot Reduce Visibility | An overriding method cannot make an inherited method more restrictive. A `public` method in `Vehicle` must remain `public` in `TwoWheeler`, `Car`, and `ElectricCar`. It cannot be reduced to `protected` or package-private. |
| **4. Exception Handling** | Cannot Broaden Exceptions | An overriding method cannot throw new or broader checked exceptions than those declared by the superclass method. Our `calculateFee` throws unchecked `IllegalArgumentException` for invalid durations. |
| **5. Static vs. Instance** | Instance Only | A static method cannot override an instance method, nor can an instance method override a static method. Static methods are *hidden*, not polymorphically overridden. |
| **6. Final Methods** | Cannot Be Overridden | A method marked `final` in a superclass cannot be overridden by any subclass. |

---

## 4. Fee-Calculation Implementation and Stated Tariff Assumptions

### 4.1 Tariff Assumptions and Billing Rules

In accordance with the assignment briefing (Slide 14) and foundational business rules established in Tasks 1–3:

1. **Illustrative Tariff Rates:**
   - **Two-Wheeler Rate:** Rs 10.00 per started hour.
   - **Car Rate:** Rs 30.00 per started hour.
   - **Electric Car Rate:** Rs 30.00 per started hour (base parking tariff).
2. **Ceiling Rule (Started Hours):**
   - Parking time is charged by the *commenced hour*. Every started hour is charged in full.
   - For example, 1 minute is charged as 1 full hour. 61 minutes is charged as 2 full hours. 130 minutes is charged as 3 full hours.
3. **Zero Duration Policy:**
   - A stay of exactly 0 minutes incurs a fee of Rs 0.00 (grace drop-off / cancellation).
4. **Invalid Duration Validation:**
   - Negative durations (e.g., `-15` minutes) represent corrupted timestamps or invalid input and must immediately throw `IllegalArgumentException`.

### 4.2 Utility Calculation in `ParkingTariff`

To avoid floating-point rounding errors and duplicate integer division logic, the calculation is centralized in the final utility class `ParkingTariff`:

```java
public static long calculateStartedHours(long durationMinutes) {
    if (durationMinutes < 0) {
        throw new IllegalArgumentException("Parking duration cannot be negative: " + durationMinutes);
    }
    if (durationMinutes == 0) {
        return 0;
    }
    return (durationMinutes + (MINUTES_PER_HOUR - 1)) / MINUTES_PER_HOUR;
}
```

### 4.3 Subclass Implementations

```java
// TwoWheeler.java
@Override
public double calculateFee(long durationMinutes) {
    long billableHours = ParkingTariff.calculateStartedHours(durationMinutes);
    return billableHours * ParkingTariff.TWO_WHEELER_RATE_PER_HOUR;
}

// Car.java
@Override
public double calculateFee(long durationMinutes) {
    long billableHours = ParkingTariff.calculateStartedHours(durationMinutes);
    return billableHours * ParkingTariff.CAR_RATE_PER_HOUR;
}

// ElectricCar.java
@Override
public double calculateFee(long durationMinutes) {
    long billableHours = ParkingTariff.calculateStartedHours(durationMinutes);
    return billableHours * ParkingTariff.ELECTRIC_CAR_RATE_PER_HOUR;
}
```

---

## 5. Runtime Polymorphism Demonstration

When client code processes an array of generic `Vehicle` references, dynamic method dispatch invokes the specialized subclass implementation at runtime:

```java
Vehicle[] fleet = new Vehicle[] {
    new TwoWheeler("TN01AA1111", true),
    new Car("TN02BB2222", 5),
    new ElectricCar("TN03CC3333", 60.0, 30.0)
};

long stayDuration = 130; // 2 hours 10 mins -> 3 started hours

for (Vehicle v : fleet) {
    // Declared type is Vehicle; actual runtime type determines which calculateFee() executes
    double fee = v.calculateFee(stayDuration);
    System.out.println(v.getClass().getSimpleName() + " -> Rs " + fee);
}
```

### Expected Output
```text
TwoWheeler -> Rs 30.0
Car -> Rs 90.0
ElectricCar -> Rs 90.0
```

---

## 6. The Three Uses of the `final` Keyword

The `final` keyword in Java is used to restrict mutability and inheritance. In the Campus Smart-Parking System, all three uses of `final` are demonstrated with clear architectural justification:

### 6.1 Final Variables (Constants & Immutable State)
- **Concept:** A `final` variable cannot be reassigned once initialized.
- **Application:**
  - `Vehicle.registrationNumber` and `Vehicle.vehicleType` are declared `private final`. Once a vehicle object is constructed, its legal identity and category cannot be altered.
  - Constants such as `ParkingTariff.CAR_RATE_PER_HOUR` are declared `public static final`.
  - Local variables within methods (e.g., `final double discount = 5.0;`) ensure values remain constant throughout execution.

### 6.2 Final Methods (Protecting Core Invariants)
- **Concept:** A method declared `final` cannot be overridden by any subclass. Subclasses inherit the method and can call it, but cannot redefine its implementation.
- **Application in `Vehicle`:**
  ```java
  public final String getRegistrationNumber() {
      return registrationNumber;
  }

  public final String getVehicleType() {
      return vehicleType;
  }
  ```
- **Architectural Justification:** The retrieval of a vehicle's legal license plate is an identity invariant. Subclasses must never be allowed to override `getRegistrationNumber()` to return a spoofed, truncated, or modified string. Marking this accessor `final` protects system integrity without restricting subclass extensibility.
- *Notice:* `calculateFee()` is intentionally **not** `final`, because subclass specialization is required.

### 6.3 Final Classes (Preventing Inheritance)
- **Concept:** A class declared `final` cannot be extended by any other class.
- **Application:**
  ```java
  public final class ParkingTariff {
      private ParkingTariff() {
          throw new UnsupportedOperationException("Utility class cannot be instantiated.");
      }
      // static constants and methods...
  }
  ```
- **Architectural Justification:** `ParkingTariff` is a pure utility class containing static constants and calculation algorithms. Allowing subclasses of `ParkingTariff` would serve no design purpose and could introduce unintended state or override hazards. Marking it `final` enforces proper object-oriented boundaries.
- *Notice:* `Vehicle`, `TwoWheeler`, `Car`, and `ElectricCar` are intentionally **not** `final`, ensuring the domain inheritance tree remains fully operational.

---

## 7. `Object`-Class Method Overrides: `toString()`, `equals()`, and `hashCode()`

Every class in Java implicitly inherits from `java.lang.Object`. Overriding its core methods is essential for domain identity, logging, and data structure compatibility.

### 7.1 Human-Readable Description via `toString()`

Default `Object.toString()` returns the class name followed by `@` and the unsigned hexadecimal hash code (e.g. `com.smartparking.model.Car@7a81197d`), which is uninformative for users and logs.

- **`Vehicle.toString()`:**
  ```java
  @Override
  public String toString() {
      return "Vehicle[regNo=" + registrationNumber + ", type=" + vehicleType + "]";
  }
  ```
- **Subclass Enhancements:**
  Subclasses override `toString()` and append their specialized attributes:
  ```java
  // TwoWheeler:
  "TwoWheeler[" + super.toString() + ", helmetStorage=" + hasHelmetStorage + "]"
  // Car:
  "Car[" + super.toString() + ", seats=" + seatingCapacity + "]"
  // ElectricCar:
  "ElectricCar[" + super.toString() + ", battery=" + currentBatteryLevelKWh + "/" + batteryCapacityKWh + "kWh, charging=" + isCharging + "]"
  ```

### 7.2 Logical Equality via `equals(Object obj)`

By default, `Object.equals(Object obj)` evaluates **reference equality** (`this == obj`), returning `true` only if both references point to the exact same memory address on the heap.

In the parking domain, vehicle identity is determined by its **registration number**: two vehicle objects with the same registration number represent the exact same physical vehicle license plate.

```java
@Override
public boolean equals(Object obj) {
    if (this == obj) {
        return true; // Reflexive optimization
    }
    if (!(obj instanceof Vehicle other)) {
        return false; // Null-safe and rejects unrelated types
    }
    return this.registrationNumber.equals(other.registrationNumber);
}
```

#### Adherence to the General Contract of `equals()`
1. **Reflexive:** For any non-null reference `x`, `x.equals(x)` returns `true`.
2. **Symmetric:** For any non-null references `x` and `y`, `x.equals(y)` returns `true` if and only if `y.equals(x)` returns `true`.
3. **Transitive:** If `x.equals(y)` is true and `y.equals(z)` is true, then `x.equals(z)` is true.
4. **Consistent:** Repeated invocations return identical results, provided fields are not modified (guaranteed by immutable `final registrationNumber`).
5. **Non-nullity:** `x.equals(null)` returns `false`.

#### Cross-Subclass Equality (Slide 15 Question)
Notice that Slide 15 poses the question:
```java
System.out.println(new Car("TN02B2").equals(new ElectricCar("TN02B2"))); // prints true
```
*Why this prints true:* Because `equals()` uses `obj instanceof Vehicle` and compares normalized `registrationNumber`. In the campus parking facility, a vehicle license plate is globally unique. If a car with license plate `"TN02B2"` enters, whether it is cast as a `Vehicle`, `Car`, or `ElectricCar`, it represents the same vehicle identity on campus. Implementing identity once in `Vehicle` ensures symmetry across subclasses.

### 7.3 Consistent Hashing via `hashCode()`

The Java contract requires that:
> **If two objects are equal according to `equals(Object)`, they MUST produce the exact same integer result from `hashCode()`.**

```java
@Override
public int hashCode() {
    return Objects.hash(registrationNumber);
}
```

---

## 8. The Equality/Hash-Code Contract and `HashSet` Example

### 8.1 Why Overriding Together Is Mandatory

If `equals()` is overridden but `hashCode()` is omitted:
1. Two distinct vehicle objects with the same registration number (e.g. `v1 = new Car("TN01A1", 4)` and `v2 = new Car("TN01A1", 5)`) will return `v1.equals(v2) == true`.
2. However, because default `Object.hashCode()` produces arbitrary memory-derived integers, `v1.hashCode()` and `v2.hashCode()` will almost certainly be different numbers.
3. When `v1` is added to a `HashSet<Vehicle>` or used as a key in a `HashMap<Vehicle, ParkingTicket>`, the JVM hashes `v1` and places it in bucket $B_1$.
4. When `set.contains(v2)` is called, the JVM hashes `v2` to a different bucket $B_2$, never checking bucket $B_1$. The lookup returns `false`, and the set will erroneously store duplicate vehicles!

### 8.2 Hazard of Mutable Fields in Hash Identity

If a mutable field (such as `currentBatteryLevelKWh` or `isCharging`) were included in `hashCode()`:
- An object added to a hash set at 20% battery would be placed in bucket $A$.
- If the vehicle charges to 80%, its hash code changes dynamically.
- Subsequent calls to `set.contains(vehicle)` or `set.remove(vehicle)` search bucket $B$, failing to find the object. The object becomes an unreachable "ghost" entry leaking memory.
- Using the immutable `final registrationNumber` guarantees the hash code is invariant over the object's lifetime.

### 8.3 Demonstration with `HashSet<Vehicle>`

```java
Set<Vehicle> set = new HashSet<>();
set.add(new Car("TN02B2", 4));
set.add(new Car("TN02B2", 6));              // Duplicate registration
set.add(new ElectricCar("TN02B2", 50.0));   // Duplicate registration
set.add(new TwoWheeler("TN99ZZ9999", true)); // Unique

System.out.println(set.size()); // Prints 2
```

---

## 9. Subclass Overriding vs. Switch-Based Alternative

The assignment specification requires comparing the implemented **Subclass Overriding** approach against an alternative design using a **single monolithic `calculateFee()` method with a `switch` statement on vehicle type**.

```
DESIGN A: Subclass Method Overriding (Polymorphic Dispatch)
                  Vehicle.calculateFee() [abstract]
                                 ^
           +---------------------+---------------------+
           |                     |                     |
TwoWheeler.calculateFee()  Car.calculateFee()  ElectricCar.calculateFee()
  (Rs 10/hr)                (Rs 30/hr)          (Rs 30/hr)

DESIGN B: Monolithic Method with Switch
public double calculateFee(Vehicle v, long minutes) {
    switch (v.getType()) {
        case "TWO_WHEELER" -> return hours * 10.0;
        case "CAR"         -> return hours * 30.0;
        case "ELECTRIC_CAR"-> return hours * 30.0;
        default            -> throw new IllegalArgumentException("Unknown type");
    }
}
```

### 9.1 Mechanism Comparison

- **Subclass Overriding:**
  Fee logic is distributed across the classes that own the corresponding domain data. The caller invokes `v.calculateFee(minutes)`, and the JVM executes dynamic method dispatch via the virtual method table (vtable).
- **Switch-Based Design:**
  A single class (or utility method) centralizes all fee algorithms. The caller invokes the monolithic method, which inspects a type discriminant string or enum and jumps to the matching case.

---

## 10. Complexity Analysis for $k$ Vehicle Types

Let $k$ be the number of distinct vehicle types registered in the system (e.g. `TwoWheeler`, `Car`, `ElectricCar`, `DeliveryVan`, `EBike`).

### 10.1 Fee-Computation Time Complexity

| Approach | Execution Mechanism | Time Complexity | Performance Discussion |
| :--- | :--- | :---: | :--- |
| **Subclass Overriding** | Dynamic Method Dispatch (vtable lookup) | $\mathcal{O}(1)$ | Single pointer dereference through the object's virtual method table. Constant time regardless of $k$. |
| **Switch Statement** | `tableswitch` / `lookupswitch` / String hash comparison | $\mathcal{O}(1)$ to $\mathcal{O}(k)$ | If using an `enum` or contiguous integers, bytecode compiles to `tableswitch` ($\mathcal{O}(1)$ jump table). If using Strings, the JVM computes string hash codes followed by branch comparisons. While highly optimized, it does not outperform vtable dispatch. |

> [!NOTE]
> Do not claim that polymorphic method dispatch is automatically faster than a switch statement. Modern JIT compilers optimize both heavily. The decisive difference lies in **maintainability, extensibility, and software architecture**.

### 10.2 Maintenance Impact and Blast Radius

| Metric | Subclass Overriding | Monolithic Switch |
| :--- | :--- | :--- |
| **Classes Modified to Add Type** | **0** existing classes modified | **1 or more** existing classes modified |
| **Classes Added** | **1** new class (e.g., `DeliveryVan`) | **0** new classes (add case to switch) |
| **Risk of Missing a Case** | **Zero:** Compiler enforces implementation of abstract `calculateFee()`. | **High:** If a developer adds a new type but forgets to update the switch, the method falls into `default` or produces silent calculation errors. |
| **Coupling** | Decoupled: Each class encapsulates its own pricing rule. | Highly coupled: The central switch must know about every vehicle category in the system. |

---

## 11. Open/Closed Principle (OCP) Discussion

The **Open/Closed Principle** (the "O" in SOLID) states:
> *Software entities should be open for extension, but closed for modification.*

### How Subclass Overriding Satisfies OCP
- When the campus introduces new vehicle categories in Task 12 (such as **Delivery Vans** with peak-hour tariffs or **E-Bikes** with flat fees), developers create a new class:
  ```java
  public class DeliveryVan extends Vehicle {
      @Override
      public double calculateFee(long durationMinutes) {
          // specialized delivery van pricing
      }
  }
  ```
- **Existing classes (`Vehicle`, `TwoWheeler`, `Car`, `ElectricCar`) remain completely untouched.** Existing, thoroughly tested code is never opened for editing, eliminating the risk of regression bugs.

### How the Switch Approach Violates OCP
- Every new vehicle type forces modification of the existing `calculateFee()` method.
- A bug introduced while editing the switch statement can break fee calculation for existing, previously working vehicle types (e.g., accidentally breaking `Car` fees while adding `DeliveryVan`).

### When Might a Switch-Based Approach Still Be Reasonable?
A switch-based approach may be appropriate when:
1. The set of types is strictly closed and will never change (e.g., compass directions `NORTH`, `SOUTH`, `EAST`, `WEST`).
2. Using modern Java **pattern matching on `sealed` classes**, where the compiler proves exhaustiveness and prevents missing cases.
3. Dealing with external, third-party DTOs that cannot be subclassed or modified.

---

## 12. Summary and Conclusion

1. **Method Specialisation:** Defining `calculateFee()` as an abstract contract in `Vehicle` and overriding it in `TwoWheeler`, `Car`, and `ElectricCar` cleanly encapsulates category-specific billing policies.
2. **Compiler Verification:** The `@Override` annotation guards against subtle typographical errors and ensures strict adherence to signature, return type, and visibility rules.
3. **The `final` Keyword:**
   - `final` variables protect immutable identity (`registrationNumber`).
   - `final` methods (`getRegistrationNumber()`, `getVehicleType()`) safeguard identity accessors from subclass alteration.
   - `final` classes (`ParkingTariff`) prevent inappropriate subclassing of utility structures.
4. **Contract Fidelity:** Overriding `equals()` and `hashCode()` based on vehicle `registrationNumber` ensures correct logical equality and seamless integration with hash-based collections (`HashSet`, `HashMap`).
5. **Architectural Excellence:** Subclass overriding upholds the Open/Closed Principle, ensuring the Campus Smart-Parking System scales smoothly into future tasks without fragile monolithic code modifications.

