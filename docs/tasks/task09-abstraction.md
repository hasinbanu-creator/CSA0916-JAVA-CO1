# TASK 9 – Introduce Abstraction (4 Marks)
**Module:** Programming in Java (CO1)  
**Academic Level:** Bloom’s Taxonomy Level K4 – Analyse  
**System Context:** Campus Smart-Parking System  
**Deliverables:**
- Documentation: `docs/tasks/task09-abstraction.md`
- PlantUML Model: `docs/uml/task09-abstraction.puml`
- Core Interface: `src/main/java/com/smartparking/model/Chargeable.java`
- Subclass Integration: `src/main/java/com/smartparking/model/ElectricCar.java`
- Demonstration Program: `src/main/java/com/smartparking/tasks/AbstractionDemo.java`
- Comprehensive Unit Tests:
  - `src/test/java/com/smartparking/model/ChargeableTest.java`
  - `src/test/java/com/smartparking/tasks/AbstractionDemoTest.java`

---

## 1. Executive Summary and The Abstraction Principle

**Abstraction** is the foundational object-oriented principle of hiding internal implementation mechanics and presenting only essential, high-level behavioral contracts to client subsystems. It establishes a clean separation between:
- **What** an entity does (its public specification / contract), and
- **How** it executes that responsibility (its internal algorithms, data structures, and state transitions).

In Java, abstraction is realized through two distinct language constructs:
1. **Abstract Classes (`abstract class`):** Incomplete blueprint classes designed to capture shared state, constructors, invariant methods, and abstract hooks within an **"is-a"** inheritance hierarchy.
2. **Interfaces (`interface`):** Behavioral contracts specifying capabilities or roles that a class can perform (**"can-do"**), completely decoupled from the class's physical inheritance tree.

In the **Campus Smart-Parking System**, Task 9 introduces formal abstraction by refining the abstract superclass `Vehicle` and introducing the `Chargeable` interface. This architectural separation guarantees that core vehicle operations (identification, parking slot allocation, baseline occupancy tariffs) remain distinct from specialized energy-delivery services (EV charging, state-of-charge tracking, and electricity billing).

---

## 2. Why Abstraction is Essential in the Campus Smart-Parking System

A real-world university parking facility manages diverse vehicle categories—ranging from lightweight two-wheelers and passenger sedans to electric automobiles and commercial delivery vans. Without abstraction, software systems suffer from severe architectural degradation:

### 2.1 The Hazard of Incomplete Base Instances
A generic "Vehicle" does not physically exist in isolation. A facility never parks an abstract concept; it parks a physical *motorcycle*, a *car*, or an *electric car*. 
- If `Vehicle` were a concrete class, client code could accidentally execute `new Vehicle("TN01A1", "UNKNOWN")`.
- Such an object lacks specific dimensions, passenger capacities, helmet storage attributes, or battery specifications. 
- Declaring `Vehicle` as `abstract` prevents direct instantiation via the compiler, guaranteeing that only fully defined, concrete vehicle categories can enter the parking system.

### 2.2 Decoupling High-Level Facility Logic from Subclass Details
Parking controllers, gate ticketing algorithms, and floor allocation services must process vehicles uniformly without being tightly coupled to subtype-specific details:
- The allocation engine only needs to know that a vehicle has a registration number and can calculate its parking fee.
- By programming against the abstract type `Vehicle` rather than concrete subclasses, high-level business logic is shielded from low-level mechanical differences.

### 2.3 Modular Separation of Orthogonal Capabilities
Not all vehicles possess electrical batteries, and not all electrical devices parked on campus are vehicles (e.g., campus maintenance golf carts, portable battery power banks, or electric lawnmowers).
- If battery charging methods were forced into `Vehicle`, standard combustion cars and two-wheelers would inherit nonsensical operations such as `charge()` or `getBatteryCapacityKWh()`.
- Abstraction via interfaces allows the `Chargeable` capability to be attached strictly to classes that genuinely possess electrical charging hardware.

---

## 3. Abstract Class `Vehicle` and Its Abstract Methods

### 3.1 Architectural Responsibilities of `Vehicle`
The abstract class [`Vehicle`](file:///Users/hasinnn/Documents/Java%20Assignment%201/src/main/java/com/smartparking/model/Vehicle.java) represents the generalized root of all vehicular entities in the parking system:

```java
public abstract class Vehicle {
    private final String registrationNumber;
    private final String vehicleType;

    public Vehicle(String registrationNumber, String vehicleType) {
        // Enforces fail-fast validation across all subtypes
        if (registrationNumber == null || registrationNumber.isBlank()) {
            throw new IllegalArgumentException("Registration number cannot be null or blank.");
        }
        if (vehicleType == null || vehicleType.isBlank()) {
            throw new IllegalArgumentException("Vehicle type cannot be null or blank.");
        }
        this.registrationNumber = registrationNumber.trim().toUpperCase();
        this.vehicleType = vehicleType.trim().toUpperCase();
    }

    public final String getRegistrationNumber() { return registrationNumber; }
    public final String getVehicleType() { return vehicleType; }

    // Abstract contract: Subclasses MUST implement category-specific pricing
    public abstract double calculateFee(long durationMinutes);

    // Final Object contracts: Identity strictly governed by registration number
    @Override public final boolean equals(Object obj) { ... }
    @Override public final int hashCode() { ... }
}
```

### 3.2 Why `Vehicle` Must Be an Abstract Class Rather Than an Interface
1. **State Encapsulation:** Every vehicle in the physical world has an immutable state identifier (`registrationNumber` and `vehicleType`). Interfaces cannot declare mutable or instance fields; they can only declare `public static final` constants.
2. **Constructor Enforcement:** `Vehicle` declares a parameterized constructor that normalizes license plates to uppercase and enforces non-blank validation. Interfaces cannot declare constructors, meaning validation logic would have to be duplicated across every subclass constructor.
3. **Identity Contract Invariance:** The `equals(Object)` and `hashCode()` methods are declared `final` in `Vehicle` to protect the cross-subclass identity contract. Interfaces cannot override or mark `Object` methods as final.
4. **Abstract Method Contract:** By declaring `public abstract double calculateFee(long durationMinutes);`, the superclass establishes a mandatory contract that the Java compiler forces all concrete subclasses to implement.

---

## 4. Concrete Subclasses Implementing Abstract Methods

Each concrete vehicle subclass specializes the abstract fee calculation contract using the baseline tariff constants defined in [`ParkingTariff.java`](file:///Users/hasinnn/Documents/Java%20Assignment%201/src/main/java/com/smartparking/model/ParkingTariff.java):

```
                       Abstract Class: Vehicle
                     [ calculateFee(long) : double ]
                                   ^
         +-------------------------+-------------------------+
         |                         |                         |
    TwoWheeler                    Car                   ElectricCar
[ calculateFee(long) ]    [ calculateFee(long) ]    [ calculateFee(long) ]
 (Rs 10.0 / started hr)    (Rs 30.0 / started hr)    (Rs 30.0 / started hr)
```

1. **`TwoWheeler`:** Overrides `calculateFee()` to multiply started hours by `ParkingTariff.TWO_WHEELER_RATE_PER_HOUR` (Rs 10.0/hr).
2. **`Car`:** Overrides `calculateFee()` to multiply started hours by `ParkingTariff.CAR_RATE_PER_HOUR` (Rs 30.0/hr).
3. **`ElectricCar`:** Overrides `calculateFee()` to multiply started hours by `ParkingTariff.ELECTRIC_CAR_RATE_PER_HOUR` (Rs 30.0/hr).

---

## 5. The Interface Concept and the `Chargeable` Interface

### 5.1 Interface Principles in Java
An interface is a pure reference type in Java that encapsulates a contract of public abstract methods, default methods, static utility methods, and constant values.
- **Role Decoupling:** While a class defines *what an object is* (identity and internal representation), an interface defines *a capability the object possesses* (a role or behavior).
- **Multiple Realization:** Java strictly prohibits multiple class inheritance (a class can extend at most one superclass to prevent the Diamond Problem). However, Java permits a class to implement **multiple interfaces** (`implements Chargeable, Payable, Serializable`), allowing rich, multi-faceted behavior without multiple-inheritance ambiguities.

### 5.2 The `Chargeable` Interface Definition and Energy Billing Policy
In [`Chargeable.java`](file:///Users/hasinnn/Documents/Java%20Assignment%201/src/main/java/com/smartparking/model/Chargeable.java), the charging capability is encapsulated with full validation, default algorithms, static utilities, and a clearly defined energy billing contract:

#### The Relationship Between Requested Energy, Accepted Energy, and Billing Cost
In an automated EV charging station, three distinct energy quantities must be rigorously distinguished:
1. **Requested Charging Energy ($E_{\text{requested}}$):** The volume of energy requested by the driver or charging session (in kWh).
2. **Accepted Battery Energy ($E_{\text{accepted}}$):** The electrical energy actually absorbed and stored by the vehicle's battery pack. Bounded by remaining battery space:
   $$E_{\text{accepted}} = \min(E_{\text{requested}}, \text{remainingCapacity}) = \min(E_{\text{requested}}, \text{batteryCapacity} - \text{currentCharge})$$
3. **Surplus / Rejected Energy ($E_{\text{surplus}}$):**
   $$E_{\text{surplus}} = E_{\text{requested}} - E_{\text{accepted}} = \max(0.0, E_{\text{requested}} - \text{remainingCapacity})$$

#### Established Campus Billing Policy
> [!IMPORTANT]
> **Energy Billed on Accepted Consumption Policy:**  
> When requested energy exceeds the battery's remaining capacity ($E_{\text{requested}} > \text{remainingCapacity}$), the charging hardware safely terminates the session upon reaching 100% capacity, and the billing engine charges **strictly for the accepted energy ($E_{\text{accepted}}$)**:
> $$\text{Charging Fee} = E_{\text{accepted}} \times \text{ratePerKWh}$$
> Drivers are **never billed** for unabsorbed surplus energy ($E_{\text{surplus}}$). Charging a customer for energy their battery could not physically absorb would violate commercial metering standards and university fairness rules.

To guarantee this contract, `Chargeable.charge(double energyKWh)` returns the actual energy accepted in kWh:

```java
public interface Chargeable {

    /** Standard baseline electricity rate for EV charging in INR per kWh (Slide 14). */
    double STANDARD_CHARGING_RATE_PER_KWH = 12.0;

    // Abstract contract: What every chargeable entity must provide
    double charge(double energyKWh); // Returns actual accepted energy in kWh
    double getBatteryCapacityKWh();
    double getCurrentChargeKWh();

    // Default methods: Reusable algorithms derived from abstract getters
    default double getRemainingCapacityKWh() {
        return Math.max(0.0, getBatteryCapacityKWh() - getCurrentChargeKWh());
    }

    default double getBatteryPercentage() {
        double capacity = getBatteryCapacityKWh();
        return (capacity <= 0.0) ? 0.0 : (getCurrentChargeKWh() / capacity) * 100.0;
    }

    default double estimateChargingTimeMinutes(double chargingPowerKW, double efficiency) {
        validatePower(chargingPowerKW);
        if (Double.isNaN(efficiency) || Double.isInfinite(efficiency) || efficiency <= 0.0 || efficiency > 1.0) {
            throw new IllegalArgumentException("Charging efficiency must be strictly between 0.0 and 1.0: " + efficiency);
        }
        return (getRemainingCapacityKWh() / (chargingPowerKW * efficiency)) * 60.0;
    }

    default double estimateChargingTimeMinutes(double chargingPowerKW) {
        return estimateChargingTimeMinutes(chargingPowerKW, 1.0); // Default to ideal linear model (efficiency = 1.0)
    }
```

    // Static utility methods: Namespace-bound helper functions
    static double calculateChargingCost(double energyKWh, double ratePerKWh) {
        validateEnergy(energyKWh);
        if (Double.isNaN(ratePerKWh) || Double.isInfinite(ratePerKWh) || ratePerKWh < 0.0) {
            throw new IllegalArgumentException("Charging rate cannot be negative or invalid: " + ratePerKWh);
        }
        return energyKWh * ratePerKWh;
    }

    static void validateEnergy(double energyKWh) {
        if (Double.isNaN(energyKWh) || Double.isInfinite(energyKWh) || energyKWh <= 0.0) {
            throw new IllegalArgumentException("Charging energy must be finite and strictly positive: " + energyKWh);
        }
    }

    static void validatePower(double powerKW) {
        if (Double.isNaN(powerKW) || Double.isInfinite(powerKW) || powerKW <= 0.0) {
            throw new IllegalArgumentException("Charging power must be finite and strictly positive: " + powerKW);
        }
    }
}
```

---

## 6. Default and Static Interface Methods (Java 8+ Modern Architecture)

Historically (prior to Java 8), interfaces were strictly limited to public abstract method declarations and static constants. Java 8 introduced **default methods** and **static methods**, fundamentally transforming interface design:

### 6.1 Default Interface Methods (`default` keyword)
A **default method** provides a concrete, executable implementation directly within the interface declaration:
- **Binary Compatibility:** Allows API designers to introduce new functionality to existing interfaces without breaking third-party classes that already implement the interface.
- **Reusable Behavioral Templates:** Derived metrics can be computed directly from abstract getters without forcing every implementing class to duplicate identical mathematical formulas.

#### Examples in `Chargeable`:
1. **`getRemainingCapacityKWh()`:**
   $$\text{Remaining} = \max(0.0, \text{capacity} - \text{currentCharge})$$
   Enables any implementing class (whether an `ElectricCar`, an `EBike`, or a `SolarChargingStation`) to automatically report remaining energy space.
2. **`getBatteryPercentage()`:**
   Computes State of Charge (SoC):
   $$\text{SoC} = \left(\frac{\text{currentCharge}}{\text{capacity}}\right) \times 100.0$$
3. **`estimateChargingTimeMinutes(double chargingPowerKW, double efficiency)`:**
   Calculates estimated minutes to reach 100% capacity given charger output power and system efficiency:
   $$\text{Time (min)} = \left(\frac{\text{Remaining Capacity (kWh)}}{\text{Power Output (kW)} \times \eta}\right) \times 60.0$$

#### Explicit Engineering Assumptions of `estimateChargingTimeMinutes()`
The duration estimation algorithm relies upon three stated technical assumptions:
1. **Constant Charging Power (Linear Delivery Model):**  
   The calculation assumes the charging station supplies a constant power level ($\text{chargingPowerKW}$) across the entire session. In reality, lithium-ion battery management systems (BMS) employ Constant Current / Constant Voltage (CC/CV) protocols, where charging current tapers off non-linearly once the battery exceeds $\approx 80\%$ State of Charge (SoC). The linear formula provides a useful engineering baseline.
2. **Charging Efficiency Factor ($\eta \in (0.0, 1.0]$):**  
   Power transfer involves physical conversion losses (AC-to-DC onboard rectification, inverter losses, and thermal dissipation inside battery cells). In commercial systems, $\eta$ typically ranges between **$0.85$ and $0.92$**. 
   - The overloaded method `estimateChargingTimeMinutes(powerKW, efficiency)` allows callers to incorporate measured efficiency.
   - The single-argument method `estimateChargingTimeMinutes(powerKW)` defaults to $\eta = 1.0$ (ideal, lossless charging baseline).
3. **Ambient Temperature Stability:**  
   The model assumes battery cell temperatures remain within the optimal operational window ($\approx 20^\circ\text{C}$ to $25^\circ\text{C}$), without thermal throttling or cold-weather preconditioning delays.

*Subclass Override Capability:* If an advanced EV model incorporates non-linear battery charging curves (e.g., thermal throttling above 80% SoC), it can override `estimateChargingTimeMinutes()` with a specialized logarithmic model while other classes continue using the default linear calculation.

### 6.2 Static Interface Methods (`static` keyword)
A **static method** belongs directly to the interface type namespace rather than to an individual implementing instance:
- Cannot be overridden by implementing classes.
- Invoked directly via the interface name: `Chargeable.calculateChargingCost(...)`.
- Encapsulates domain utility functions and input validation rules directly alongside the contract they govern, preventing the clutter of separate utility classes.

#### Examples in `Chargeable`:
- **`calculateChargingCost(double energyKWh, double ratePerKWh)`:** Computes electricity charges safely:
  $$\text{Cost} = \text{energyKWh} \times \text{ratePerKWh}$$
- **`validateEnergy(double energyKWh)` & `validatePower(double powerKW)`:** Enforces fail-fast boundary validation, ensuring that `NaN`, $\pm\infty$, zero, and negative values are rejected immediately.

---

## 7. Concrete Integration: `ElectricCar` Implementing `Chargeable`

In [`ElectricCar.java`](file:///Users/hasinnn/Documents/Java%20Assignment%201/src/main/java/com/smartparking/model/ElectricCar.java), the class integrates both inheritance and interface implementation:

```java
public class ElectricCar extends Vehicle implements Chargeable {
    private final double batteryCapacityKWh;
    private double currentBatteryLevelKWh;
    private boolean isCharging;

    public ElectricCar(String registrationNumber, double batteryCapacityKWh, double initialBatteryLevelKWh) {
        super(registrationNumber, "ELECTRIC_CAR");
        validateBatteryCapacity(batteryCapacityKWh);
        // Validates initial charge level
        this.batteryCapacityKWh = batteryCapacityKWh;
        this.currentBatteryLevelKWh = initialBatteryLevelKWh;
        this.isCharging = false;
    }

    @Override
    public double getBatteryCapacityKWh() {
        return batteryCapacityKWh;
    }

    @Override
    public double getCurrentChargeKWh() {
        return currentBatteryLevelKWh;
    }

    @Override
    public double charge(double energyKWh) {
        Chargeable.validateEnergy(energyKWh);
        double remaining = getRemainingCapacityKWh();
        double accepted = Math.min(energyKWh, remaining);
        this.currentBatteryLevelKWh += accepted;
        return accepted; // Returns actual accepted energy for fair billing
    }

    @Override
    public double calculateFee(long durationMinutes) {
        // Preserves Task 7 & 8 slot occupancy tariff (Rs 30.0/hr)
        long billableHours = ParkingTariff.calculateStartedHours(durationMinutes);
        return billableHours * ParkingTariff.ELECTRIC_CAR_RATE_PER_HOUR;
    }
}
```

### Architectural Separation of Parking and Charging Fees
A critical design requirement is that **parking fees** (slot occupancy) and **charging fees** (electricity consumption) must remain decoupled:
- Calling `v.calculateFee(durationMinutes)` computes solely the physical parking bay occupancy fee governed by `Vehicle` and `ParkingTariff`.
- Calling `Chargeable.calculateChargingCost(kWh, rate)` computes solely the electrical utility fee for energy transferred into the battery.
- This preserves the exact behavior required by Tasks 7 and 8 while providing full support for metered energy billing.

---

## 8. Comprehensive Comparison: Abstract Classes vs. Interfaces

| Feature / Dimension | Abstract Class (`abstract class`) | Interface (`interface`) |
| :--- | :--- | :--- |
| **Architectural Role** | Models **identity** and core state ("is-a" relationship). | Models **capabilities** or roles ("can-do" contract). |
| **Inheritance Multiplicity** | Single class inheritance only (`extends BaseClass`). | Multiple interface implementation (`implements A, B, C`). |
| **Instance Fields** | Can declare **mutable, protected, private, and final instance fields**. | **No instance state**. Can only declare `public static final` constants. |
| **Constructors** | Can declare **constructors** (called via `super(...)` during subclass creation). | **No constructors allowed**. Cannot participate in object instantiation directly. |
| **Method Types Permitted** | Abstract, concrete instance, final, static, protected, private methods. | Abstract, `default`, `static`, and `private` helper methods. |
| **Access Modifiers** | Can use `public`, `protected`, package-private, or `private`. | Methods are `public` by default; private methods permitted for internal helper reuse. |
| **Object-Class Overrides** | Can override `equals()`, `hashCode()`, and `toString()`, and declare them `final`. | Cannot override or mark `Object` methods as default or final. |
| **Bytecode Dispatch** | Invoked via `invokevirtual` bytecode instruction. | Invoked via `invokeinterface` bytecode instruction. |
| **When to Choose** | When classes share **common state, code, identity, and constructors**. | When disparate classes share **common behavioral contracts or roles**. |

---

## 9. Architectural Decision Guide: When to Use Each in the Parking System

```
                         Architectural Decision Matrix
                                       |
                   Does the concept represent core identity
                   with shared instance state and constructors?
                                      / \
                                     /   \
                                   YES    NO
                                   /       \
                        Abstract Class      Is it a behavioral role
                        (e.g., Vehicle)     or orthogonal capability?
                                                  / \
                                                 /   \
                                               YES    NO
                                               /       \
                                          Interface    Not an abstraction
                                      (e.g., Chargeable,
                                           Payable)
```

1. **Use an Abstract Class (`Vehicle`):**
   - When classes represent physical specializations of a single domain concept.
   - When common immutable identifiers (`registrationNumber`, `vehicleType`) must be stored in memory and validated at construction time.
   - When core identity methods (`equals`, `hashCode`) must be sealed across all subclasses.
2. **Use an Interface (`Chargeable`, `Payable`):**
   - When a behavior is optional or orthogonal to the main inheritance tree.
   - When unrelated classes might share the capability (e.g., an `ElectricCar` and an `ElectricMaintenanceCart` can both be `Chargeable`).
   - When a class requires multiple behavioral capabilities (e.g., an `ElectricCar` is simultaneously a `Vehicle`, a `Chargeable` device, and a `Payable` entity).

---

## 10. Polymorphism via Abstract Class and Interface References

In Java, polymorphism operates seamlessly through both abstract class references and interface references:

```java
// 1. Abstract Class Polymorphism
Vehicle v = new ElectricCar("TN03C3", 60.0, 20.0);
double parkingFee = v.calculateFee(120); // Dispatches to ElectricCar.calculateFee()

// 2. Interface Polymorphism
Chargeable c = (ElectricCar) v;
c.charge(25.0);                          // Dispatches to ElectricCar.charge()
double remaining = c.getRemainingCapacityKWh(); // Dispatches to Chargeable default method
```

### Dual vtable and itable Dispatch Internals
- When invoked via `Vehicle v`, the JVM uses `invokevirtual`, resolving the call via the class's **Virtual Method Table (vtable)** at a fixed offset.
- When invoked via `Chargeable c`, the JVM uses `invokeinterface`. Because a class can implement multiple interfaces in arbitrary order, the JVM utilizes an **Interface Table (itable)** or inline-caching dispatch mechanism to resolve the method pointer.
- Modern JVMs (HotSpot 64-Bit Server VM) optimize interface calls using **Monomorphic Inline Caching**, achieving performance parity ($\mathcal{O}(1)$) with standard virtual method calls in steady-state operation.

---

## 11. Alternative Concept and Complexity Analysis (Slide 16 Inversion)

Slide 16 of the assignment PPT poses the architectural design challenge:
> *Vehicle is an abstract class and Chargeable is an interface. If Vehicle were changed to an interface with default methods, and Chargeable to an abstract class, what would the solution be? Identify what can no longer be expressed, and analyse any difference in time per method call and space per object.*

### 11.1 What Can No Longer Be Expressed
If `Vehicle` were inverted into an interface and `Chargeable` into an abstract class:
1. **Constructors and Fail-Fast Validation Lost:**
   - An interface cannot declare constructors. 
   - The validation rules that ensure `registrationNumber` is non-null, non-blank, and normalized to uppercase could no longer be centralized in `Vehicle(String, String)`.
   - Every concrete class (`TwoWheeler`, `Car`, `DeliveryVan`) would be forced to duplicate validation code in its own constructors, severely violating the DRY (Don't Repeat Yourself) principle.
2. **State Storage Prohibited:**
   - Interfaces cannot hold instance fields.
   - `private final String registrationNumber` and `private final String vehicleType` could not exist in `Vehicle`.
   - Every subclass would have to redundantly declare its own instance variables for registration number and vehicle type, increasing boilerplate and memory misalignment risk.
3. **Single Inheritance Bottleneck:**
   - If `Chargeable` were an abstract class, Java's single inheritance rule (`extends`) would dictate that a class can only extend `Chargeable` or another single class.
   - An `ElectricCar` could extend `Chargeable`, but could not extend any other class. If a new base class (e.g., `MotorVehicle`) or capability (e.g., `CommercialTransport`) were introduced as an abstract class, `ElectricCar` could not inherit both.
4. **Sealed Identity Contract Compromised:**
   - An interface cannot declare `final` overrides of `Object.equals()` and `Object.hashCode()`. Subclasses could inadvertently override them with incompatible logic, breaking cross-subclass equality.

### 11.2 Time and Space Complexity Comparison

| Metric | Implemented Design (`Vehicle` Abstract Class, `Chargeable` Interface) | Inverted Design (`Vehicle` Interface, `Chargeable` Abstract Class) |
| :--- | :--- | :--- |
| **Method Dispatch Time** | `Vehicle` calls use `invokevirtual` ($\mathcal{O}(1)$ fixed-offset vtable). `Chargeable` calls use `invokeinterface` ($\mathcal{O}(1)$ via inline cache). | Primary vehicle operations use `invokeinterface`. Slightly higher initial JIT stub generation overhead before warm-up. |
| **Object Memory Footprint** | Optimal: Fields declared once in `Vehicle`. No redundant field descriptors in subclasses. | Higher: Registration and type fields declared independently in each subclass, increasing class metadata size. |
| **Code Redundancy** | Zero: Validation and state management centralized in superclass constructor. | High: Duplicate constructor validation logic across all vehicle subclasses. |
| **Extensibility Impact** | High: Any vehicle can implement multiple future capabilities (`Payable`, `Inspected`). | Restricted: Non-EV vehicles cannot benefit from common state inheritance if single inheritance slot is consumed by `Chargeable`. |

---

## 12. Verification and Demonstration Output (`AbstractionDemo.java`)

When [`AbstractionDemo.java`](file:///Users/hasinnn/Documents/Java%20Assignment%201/src/main/java/com/smartparking/tasks/AbstractionDemo.java) executes, it provides clear, deterministic verification of abstract class and interface mechanisms:

```text
--- 1. Abstract Vehicle References ---
Car: Registration No: TN02B2, Type: CAR
EV:  Registration No: TN03C3, Type: ELECTRIC_CAR
EV Parking Fee (120 min): Rs 60.0

--- 2. Chargeable Interface Polymorphism ---
Battery Capacity : 60.0 kWh
Initial Charge   : 20.0 kWh
State of Charge  : 33.3%
Remaining Space  : 40.0 kWh

--- 3. Static Utility & Charging Execution ---
Energy Rate      : Rs 12.0 / kWh
Delivering Energy: 25.0 kWh (Cost: Rs 300.0)

--- 4. Post-Charging Verification ---
Updated Charge   : 45.0 / 60.0 kWh
Updated SoC      : 75.0%
Remaining Space  : 15.0 kWh
Est. Time to Full (22 kW): 40.9 mins

--- 5. Billing Policy When Energy Exceeds Capacity ---
Requested Energy : 30.0 kWh (Remaining Space: 15.0 kWh)
Accepted Energy  : 15.0 kWh (Battery safely capped at 100%)
Billed Amount    : Rs 180.0 (Billed on 15.0 kWh accepted, NOT requested 30.0 kWh)
Final Charge     : 60.0 / 60.0 kWh
```

### Trace of Demonstrated Steps:
1. **Abstract Class Reference:** `Vehicle evAsVehicle = new ElectricCar("TN03C3", 60.0, 20.0);` demonstrates that abstract `Vehicle` references store concrete objects and dispatch `calculateFee(120)` to compute the Rs 60.0 slot fee (2 started hours $\times$ Rs 30/hr).
2. **Interface Polymorphism:** `Chargeable evAsChargeable = (ElectricCar) evAsVehicle;` accesses EV methods through the interface reference.
3. **Default Methods:** `evAsChargeable.getBatteryPercentage()` (33.3%) and `evAsChargeable.getRemainingCapacityKWh()` (40.0 kWh) compute derived metrics from abstract getters.
4. **Static Utility Method:** `Chargeable.calculateChargingCost(25.0, 12.0)` computes the electricity bill (Rs 300.0) safely.
5. **State Mutation:** `evAsChargeable.charge(25.0)` increases battery charge from 20.0 kWh to 45.0 kWh (75.0% SoC).
6. **Charging Time Estimation:** `evAsChargeable.estimateChargingTimeMinutes(22.0)` calculates $15.0 / 22.0 \times 60 = 40.9\text{ minutes}$ remaining to reach 100% capacity.
7. **Billing Policy on Capacity Overflow:** `evAsChargeable.charge(30.0)` demonstrates that when 30.0 kWh is requested but only 15.0 kWh remaining capacity is available, the battery absorbs exactly 15.0 kWh, capping at 60.0 kWh (100%), and the billed charge is strictly $15.0 \times \text{Rs } 12.0 = \text{Rs } 180.0$ (never Rs 360.0).

---

## 13. Summary and Conclusion

Task 9 establishes a mature, extensible abstraction architecture for the Campus Smart-Parking System:
1. **Abstract Class `Vehicle`** encapsulates common physical identity, fail-fast constructor validation, and the abstract fee calculation hook.
2. **Interface `Chargeable`** cleanly separates the optional EV charging capability from core vehicular identity, avoiding hierarchy pollution.
3. **Default and Static Methods** eliminate boilerplate code, provide reusable mathematical algorithms, and centralize input validation.
4. **Separation of Tariffs** guarantees that physical slot occupancy fees and electrical utility costs are calculated independently and accurately.
5. **The test suite (53 tests)** validates all boundary conditions, mathematical invariants, and backward compatibility across Tasks 1–9.

