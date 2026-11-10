# Task 7: Override and Specialise Behaviour

---

## 1. Task Objective

The objective of Task 7 is to implement and analyse **method overriding**, **behavioural specialisation**, the **`final` keyword**, and **`Object`-class method contracts** (`toString()`, `equals()`, `hashCode()`) within the Campus Smart-Parking System's vehicle hierarchy.

Building upon the hierarchical inheritance foundation established in Task 6, Task 7 enables each concrete vehicle subclass (`TwoWheeler`, `Car`, `ElectricCar`) to specialize fee calculation while ensuring that identity and encapsulation invariants remain strictly safeguarded across the entire object-oriented model.

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
| **2. Return Type** | Invariant (Primitive) or Covariant (Reference) | Covariant return types apply strictly to reference types (allowing a subclass method to return a subtype of the superclass return type). Primitive return types (such as `double`) cannot vary and must match exactly. In our system, all fee methods return primitive `double`. |
| **3. Access Modifier** | Cannot Reduce Visibility | An overriding method cannot make an inherited method more restrictive. A `public` method in `Vehicle` must remain `public` in `TwoWheeler`, `Car`, and `ElectricCar`. It cannot be reduced to `protected` or package-private. |
| **4. Exception Handling** | Cannot Broaden Exceptions | An overriding method cannot throw new or broader checked exceptions than those declared by the superclass method. Our `calculateFee` throws unchecked `IllegalArgumentException` for invalid durations. |
| **5. Static vs. Instance** | Instance Only | A static method cannot override an instance method, nor can an instance method override a static method. Static methods are *hidden*, not polymorphically overridden. |
| **6. Final Methods** | Cannot Be Overridden | A method marked `final` in a superclass cannot be overridden by any subclass. |

---

## 4. Fee-Calculation Implementation and Stated Tariff Assumptions

### 4.1 Tariff Assumptions and Billing Rules

The original assignment briefing does not prescribe a mandatory university fee schedule. Therefore, to make the system executable and concrete, the following **illustrative project assumptions** are established:

1. **Illustrative Tariff Rates:**
   - **Two-Wheeler Rate:** Rs 10.00 per started hour (illustrative baseline assumption).
   - **Car Rate:** Rs 30.00 per started hour (illustrative baseline assumption).
   - **Electric Car Rate:** Rs 30.00 per started hour (illustrative base parking tariff).
2. **Separation of Parking and Charging Fees:**
   - The base parking fee for an `ElectricCar` covers the physical slot occupancy at Rs 30.00/hour.
   - Any electricity consumed for battery charging is a separate service charged per kWh when a charging slot is utilized (as modeled in later tasks).
3. **Ceiling Rule (Started Hours):**
   - Parking time is billed by the commenced hour. Any fraction of an hour is charged as a full hour.
   - For example, 1 minute is charged as 1 full hour; 61 minutes is charged as 2 full hours; 130 minutes is charged as 3 full hours.
4. **Zero Duration Policy:**
   - A stay of exactly 0 minutes incurs a fee of Rs 0.00. This is a selected implementation policy (e.g. immediate drop-off grace window), rather than an explicit assignment requirement.
5. **Invalid Duration Validation:**
   - Negative durations (e.g., `-15` minutes) represent corrupted timestamps or invalid input and must immediately throw `IllegalArgumentException`.

### 4.2 Overflow-Safe Ceiling Calculation in `ParkingTariff`

In earlier revisions, computing ceiling hours via `(durationMinutes + 59) / 60` introduced an integer overflow risk when `durationMinutes` approached `Long.MAX_VALUE`.

The calculation is implemented in the final utility class `ParkingTariff` using overflow-safe integer ceiling arithmetic:

```java
public static long calculateStartedHours(long durationMinutes) {
    if (durationMinutes < 0) {
        throw new IllegalArgumentException("Parking duration cannot be negative: " + durationMinutes);
    }
    if (durationMinutes == 0) {
        return 0;
    }
    // Overflow-safe ceiling arithmetic: avoids arithmetic overflow when durationMinutes is large (e.g. Long.MAX_VALUE)
    return 1 + (durationMinutes - 1) / MINUTES_PER_HOUR;
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

The `final` keyword in Java is used to restrict reassignment, method overriding, and inheritance:

### 6.1 Final Variables (Preventing Reassignment)
- **Concept:** A `final` variable cannot be reassigned once initialized.
  > [!IMPORTANT]
  > Declaring an object reference `final` means the **reference itself cannot be redirected** to point to another object. It does **not** automatically make the referenced object immutable. If the referenced object has mutable fields or setter methods, its internal state can still change.
- **Application:**
  - `Vehicle.registrationNumber` and `Vehicle.vehicleType` are declared `private final`. Because `String` is immutable and the reference cannot be reassigned, the vehicle's legal identity is fully immutable.
  - Constants such as `ParkingTariff.CAR_RATE_PER_HOUR` are declared `public static final`.
  - Local variables (e.g., `final double flatDiscount = 5.0;`) ensure the local identifier cannot be accidentally reassigned.

### 6.2 Final Methods (Protecting Invariant Contracts)
- **Concept:** A method declared `final` cannot be overridden by any subclass. Subclasses inherit the method and can invoke it, but cannot redefine its implementation.
- **Application in `Vehicle`:**
  ```java
  public final String getRegistrationNumber() {
      return registrationNumber;
  }

  public final String getVehicleType() {
      return vehicleType;
  }

  @Override
  public final boolean equals(Object obj) {
      // Identity contract protected from subclass tampering
  }

  @Override
  public final int hashCode() {
      // Hash contract protected from subclass tampering
  }
  ```
- **Architectural Justification:** The identity contract of a `Vehicle` is governed exclusively by its unique registration number. Allowing subclasses to override `getRegistrationNumber()`, `equals()`, or `hashCode()` would introduce asymmetrical equality bugs (e.g. a subclass comparing additional fields and breaking symmetry). Marking them `final` locks down the identity contract permanently.
- *Notice:* `calculateFee()` is intentionally **not** `final`, because subclass specialization is required.

### 6.3 Final Classes (Prohibiting Extension)
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
- **Architectural Justification:** `ParkingTariff` is a pure utility class. It has no instance state and should never be instantiated or extended. Marking it `final` prevents unwanted inheritance hierarchies.
- *Notice:* `Vehicle`, `TwoWheeler`, `Car`, and `ElectricCar` are intentionally **not** `final`, ensuring the domain inheritance tree remains functional.

---

## 7. `Object`-Class Method Overrides: `toString()`, `equals()`, and `hashCode()`

### 7.1 Human-Readable Description via `toString()`

Default `Object.toString()` returns the class name followed by `@` and the hexadecimal unsigned integer produced by default hashing. Overriding it provides meaningful diagnostic output for logging and debugging:

- **`Vehicle.toString()`:**
  ```java
  @Override
  public String toString() {
      return "Vehicle[regNo=" + registrationNumber + ", type=" + vehicleType + "]";
  }
  ```
- **Subclass Implementations:**
  Subclasses override `toString()` to display their specialized attributes while incorporating superclass state:
  ```java
  // TwoWheeler:
  "TwoWheeler[" + super.toString() + ", helmetStorage=" + hasHelmetStorage + "]"
  // Car:
  "Car[" + super.toString() + ", seats=" + seatingCapacity + "]"
  // ElectricCar:
  "ElectricCar[" + super.toString() + ", battery=" + currentBatteryLevelKWh + "/" + batteryCapacityKWh + "kWh, charging=" + isCharging + "]"
  ```

### 7.2 Logical Equality via `equals(Object obj)`

Default `Object.equals(Object obj)` performs **reference equality**—evaluating whether two references refer to the exact same object instance on the heap.

In the parking domain, vehicle identity is determined by its **registration number**: two vehicle objects with the same registration number represent the same physical vehicle license plate, regardless of whether they are separate object instances or distinct subclass instances.

```java
@Override
public final boolean equals(Object obj) {
    if (this == obj) {
        return true; // Reflexive check: identical reference
    }
    if (!(obj instanceof Vehicle other)) {
        return false; // Rejects null and unrelated types safely
    }
    return this.registrationNumber.equals(other.registrationNumber);
}
```

#### Adherence to the General Contract of `equals()`
1. **Reflexive:** `x.equals(x)` returns `true`.
2. **Symmetric:** `x.equals(y)` returns `true` if and only if `y.equals(x)` returns `true`.
3. **Transitive:** If `x.equals(y)` and `y.equals(z)` are true, then `x.equals(z)` is true.
4. **Consistent:** Repeated invocations return identical results because `registrationNumber` is immutable (`final`).
5. **Non-nullity:** `x.equals(null)` safely returns `false`.

#### Cross-Subclass Equality (Slide 15 Verification)
Slide 15 poses the question:
```java
System.out.println(new Car("TN02B2").equals(new ElectricCar("TN02B2"))); // prints true
```
*Why this prints true:* Because `equals()` uses `obj instanceof Vehicle` and compares normalized `registrationNumber`. In a physical parking facility, a state license plate uniquely identifies a single motor vehicle. Whether an instance is referenced as a `Car` or an `ElectricCar`, if the registration number is identical, it represents the same vehicle entity on campus. Marking `equals()` as `final` in `Vehicle` preserves symmetry across all subclasses.

### 7.3 Consistent Hashing via `hashCode()`

The Java contract mandates that:
> **If two objects are equal according to `equals(Object)`, they MUST produce the exact same integer hash code.**

```java
@Override
public final int hashCode() {
    return Objects.hash(registrationNumber);
}
```

---

## 8. The Equality/Hash-Code Contract and `HashSet` Example

### 8.1 Why Overriding Together Is Mandatory

If `equals()` is overridden without overriding `hashCode()`:
1. Two distinct vehicle instances with the same registration number (e.g., `v1 = new Car("TN01A1", 4)` and `v2 = new Car("TN01A1", 5)`) will return `v1.equals(v2) == true`.
2. However, because default `Object.hashCode()` produces arbitrary values based on internal object references, `v1.hashCode()` and `v2.hashCode()` will differ.
3. When `v1` is inserted into a hash-based collection (`HashSet<Vehicle>` or `HashMap<Vehicle, T>`), it is stored in a hash bucket determined by `v1.hashCode()`.
4. When `set.contains(v2)` is called, the collection hashes `v2` to a different bucket, never inspecting `v1`. The lookup fails, and duplicate vehicles with identical registration numbers are stored, violating uniqueness invariants.

### 8.2 Hazard of Mutable Fields in Hash Identity

If mutable fields (such as `currentBatteryLevelKWh` or `isCharging`) were included in `hashCode()`:
- An object added to a `HashSet` would be placed in a bucket based on its initial state.
- When the field mutates, the object's hash code changes dynamically.
- Subsequent calls to `contains()` or `remove()` search a different bucket and fail to locate the object. The entry becomes an unreachable "ghost" object leaking memory.
- Using the immutable `final registrationNumber` ensures the hash code remains permanently stable.

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

The assignment specification requires comparing the implemented **Subclass Overriding** design with an alternative monolithic `calculateFee()` method using a `switch` statement on vehicle type.

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
    switch (v.getVehicleType()) {
        case "TWO_WHEELER" -> return hours * 10.0;
        case "CAR"         -> return hours * 30.0;
        case "ELECTRIC_CAR"-> return hours * 30.0;
        default            -> throw new IllegalArgumentException("Unknown type");
    }
}
```

### 9.1 Mechanism Comparison

- **Subclass Overriding:**
  Fee logic is co-located with the vehicle subclass that owns the domain data. The caller invokes `v.calculateFee(minutes)`, and the runtime environment dispatches the call to the actual target class.
- **Switch-Based Design:**
  A single centralized method inspects a type descriptor and branches to the corresponding calculation.

---

## 10. Complexity Analysis for $k$ Vehicle Types

Let $k$ be the number of distinct vehicle types registered in the system (e.g., `TwoWheeler`, `Car`, `ElectricCar`, `DeliveryVan`, `EBike`).

### 10.1 Fee-Computation Time Complexity

- **Subclass Overriding:**
  Fee computation executes in **constant time $\mathcal{O}(1)$** for the implemented mathematical calculations. Polymorphic method invocation is resolved efficiently by modern JVM runtimes (via inline caching and virtual table dispatch).
- **Switch Statement:**
  Fee computation is also effectively **constant time $\mathcal{O}(1)$** in practice. Compilers translate integer or enum switches into jump tables (`tableswitch` or `lookupswitch`), while string switches utilize hashed lookups. It is **not** automatically $\mathcal{O}(k)$ linear search unless implemented as naive sequential `if-else` chains.
- **Comparison:**
  Neither approach possesses a significant raw speed advantage; both operate in constant time. The decisive engineering difference lies in **maintainability, coupling, and extensibility**.

### 10.2 Maintenance Impact, Coupling, and OCP

| Metric | Subclass Overriding | Monolithic Switch |
| :--- | :--- | :--- |
| **Existing Code Locations Modified to Add Type** | **0** existing classes modified | **1 or more** central methods modified |
| **Classes Added** | **1** new class (e.g. `DeliveryVan`) | **0** classes (add case to switch) |
| **Coupling** | Low: Central facility only references abstract `Vehicle`. | High: Central method must know about every vehicle category in the system. |
| **Type Completeness Enforcement** | The compiler forces any new concrete subclass of `Vehicle` to implement `calculateFee()`. (Note: The application must still instantiate and register the new class appropriately). | A new type descriptor might be added elsewhere, but if developer forgets to update the switch, the method falls into `default` or throws runtime errors. |

---

## 11. Open/Closed Principle (OCP) Discussion

The **Open/Closed Principle** states:
> *Software entities should be open for extension, but closed for modification.*

### How Subclass Overriding Satisfies OCP
- When the campus introduces new vehicle categories in Task 12 (such as **Delivery Vans** or **E-Bikes**), a developer creates a new subclass:
  ```java
  public class DeliveryVan extends Vehicle {
      @Override
      public double calculateFee(long durationMinutes) {
          // specialized delivery van pricing
      }
  }
  ```
- **Existing classes (`Vehicle`, `TwoWheeler`, `Car`, `ElectricCar`) remain completely untouched.** Tested code is preserved, preventing regression errors.

### How the Switch Approach Violates OCP
- Every new vehicle type requires opening and editing the existing `calculateFee()` switch block.
- Any syntax or logic mistake introduced into the switch block risks breaking calculations for existing, previously working vehicle types.

### When Might a Switch-Based Approach Still Be Reasonable?
A switch-based approach may be acceptable when:
1. The type hierarchy is strictly closed and will never change (e.g., cardinal directions).
2. Using Java **pattern matching for `switch` on `sealed` hierarchies**, where the compiler proves compile-time exhaustiveness.
3. Dealing with external Data Transfer Objects (DTOs) whose class definitions cannot be modified to add domain methods.

---

## 12. Summary and Conclusion

1. **Method Specialisation:** Abstract `calculateFee()` in `Vehicle` forces concrete subclasses to implement their category-specific tariffs while allowing polymorphic invocation through generic `Vehicle` references.
2. **Compiler Verification:** The `@Override` annotation guards against signature errors and ensures type-safe overriding.
3. **The `final` Keyword:**
   - `final` variables protect immutable identity and constants (without implying referenced objects are immutable).
   - `final` methods (`getRegistrationNumber()`, `getVehicleType()`, `equals()`, `hashCode()`) protect identity and collection contracts from subclass distortion.
   - `final` classes (`ParkingTariff`) prohibit extension of utility classes.
4. **Contract Fidelity:** Overriding `equals()` and `hashCode()` based on normalized `registrationNumber` ensures correct logical identity and reliable deduplication in hash-based collections (`HashSet`).
5. **Architectural Scalability:** Subclass overriding adheres to the Open/Closed Principle, ensuring the Campus Smart-Parking System scales smoothly without modifying tested classes.
