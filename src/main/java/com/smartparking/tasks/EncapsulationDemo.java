package com.smartparking.tasks;

import com.smartparking.model.Car;
import com.smartparking.model.ElectricCar;
import com.smartparking.model.ParkingTicket;
import com.smartparking.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Task 10 Demonstration: Packages, Access Modifiers, and Encapsulation.
 *
 * Demonstrates:
 * 1. Accessing public methods of existing model classes across package boundaries.
 * 2. Reading private state strictly through public getters.
 * 3. Updating mutable state only through validated public business methods.
 * 4. Validation guards preventing corrupted or invalid object states.
 * 5. Autoboxing and unboxing in parking operations.
 * 6. The critical difference between reference identity (==) and value equality (.equals()) on Integer wrappers.
 * 7. Practical parking-domain examples (slot numbers, durations, battery levels).
 */
public class EncapsulationDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("       Task 10: Encapsulation, Packages, and Access Modifiers Demonstration     ");
        System.out.println("================================================================================");

        // ---------------------------------------------------------------------
        // 1. Reading Encapsulated State via Public Getters
        // ---------------------------------------------------------------------
        System.out.println("\n--- 1. Reading Encapsulated State via Public Getters ---");
        // EncapsulationDemo is in package com.smartparking.tasks;
        // Vehicle, Car, and ElectricCar are in package com.smartparking.model.
        // Private fields (registrationNumber, seatingCapacity, batteryCapacityKWh)
        // are fully protected and accessible only through validated public accessors.
        ElectricCar ev = new ElectricCar("TN03CC3333", 60.0, 20.0);
        Car car = new Car("TN02BB2222", 5);

        System.out.println("Vehicle Registration : " + ev.getRegistrationNumber() + " (Immutable private final field)");
        System.out.println("Vehicle Category     : " + ev.getVehicleType());
        System.out.println("Battery Capacity     : " + ev.getBatteryCapacityKWh() + " kWh");
        System.out.println("Initial Battery Level: " + ev.getCurrentChargeKWh() + " kWh");
        System.out.println("Car Seating Capacity : " + car.getSeatingCapacity() + " seats");

        // ---------------------------------------------------------------------
        // 2. Controlled State Mutation via Public Business Methods
        // ---------------------------------------------------------------------
        System.out.println("\n--- 2. Controlled State Mutation via Public Business Methods ---");
        // Direct field mutation (e.g. ev.currentBatteryLevelKWh = 50.0) is prohibited by the compiler.
        // Mutations must flow through validated domain operations:
        System.out.println("Connecting EV to charger and delivering 25.0 kWh...");
        ev.startCharging();
        System.out.println("Charging Active      : " + ev.isCharging());

        double acceptedEnergy = ev.charge(25.0);
        System.out.println("Accepted Energy      : " + acceptedEnergy + " kWh");
        System.out.println("Updated Battery Level: " + ev.getCurrentChargeKWh() + " / " + ev.getBatteryCapacityKWh()
                + " kWh (" + String.format("%.1f", ev.getBatteryPercentage()) + "% SoC)");

        ev.stopCharging();
        System.out.println("Charging Active      : " + ev.isCharging());

        // ---------------------------------------------------------------------
        // 3. Validation Guards Protecting Object Invariants
        // ---------------------------------------------------------------------
        System.out.println("\n--- 3. Validation Guards Protecting Object Invariants ---");
        // Demonstrate fail-fast constructor and method validations that prevent invalid state:

        // A. Fee duration validation
        try {
            ev.calculateFee(-15);
        } catch (IllegalArgumentException e) {
            System.out.println("[REJECTED] Negative stay duration (-15 min): " + e.getMessage());
        }

        // B. Charging energy validation
        try {
            ev.charge(-10.0);
        } catch (IllegalArgumentException e) {
            System.out.println("[REJECTED] Non-positive energy (-10.0 kWh) : " + e.getMessage());
        }

        // C. Seating capacity validation
        try {
            new Car("TN01A1", -4);
        } catch (IllegalArgumentException e) {
            System.out.println("[REJECTED] Invalid seat count (-4 seats)   : " + e.getMessage());
        }

        // D. Registration number validation
        try {
            new Car("", 4);
        } catch (IllegalArgumentException e) {
            System.out.println("[REJECTED] Blank registration number (\"\")  : " + e.getMessage());
        }

        // ---------------------------------------------------------------------
        // 4. Autoboxing, Unboxing, and Widening in Parking Operations
        // ---------------------------------------------------------------------
        System.out.println("\n--- 4. Autoboxing and Unboxing in Parking Operations ---");
        // Autoboxing: Automatic conversion of primitive int to Integer wrapper object (Integer.valueOf(primitiveDuration))
        int primitiveDuration = 120;
        Integer boxedDuration = primitiveDuration;
        System.out.println("Primitive stay minutes (" + primitiveDuration + ") autoboxed to Integer: " + boxedDuration);

        // Storing primitive slot numbers into a generic collection (requires wrappers)
        List<Integer> assignedSlots = new ArrayList<>();
        assignedSlots.add(101); // Primitive 101 autoboxed to Integer
        assignedSlots.add(102);

        // Unboxing and Widening:
        // When passing an Integer duration directly to calculateFee(long):
        // 1. Unboxing conversion (JLS §5.1.8): Integer is unboxed to int (via boxedDuration.intValue())
        // 2. Widening primitive conversion (JLS §5.1.2): int is widened to long
        // The calculateFee(long) method executes and returns the fee as a primitive double.
        double calculatedFee = ev.calculateFee(boxedDuration);
        System.out.println("Integer duration unboxed to int, widened to long, returning double fee: Rs " + calculatedFee);

        // ---------------------------------------------------------------------
        // 5. Wrapper Class Comparison: Integer '==' vs. '.equals()'
        // ---------------------------------------------------------------------
        System.out.println("\n--- 5. Wrapper Class Comparison: Integer '==' vs. '.equals()' ---");

        // Sub-case A: Values within the Java Integer Cache (-128 to 127)
        // Autoboxing calls Integer.valueOf(int), which returns pre-allocated flyweights from the cache.
        Integer slotLowA = 100;
        Integer slotLowB = 100;
        boolean lowIdentity = (slotLowA == slotLowB); // Reference comparison (same heap object)
        boolean lowEquality = slotLowA.equals(slotLowB); // Value comparison

        System.out.println("Cached Range (-128 to 127) - Low-floor Slot IDs:");
        System.out.println("  Integer slotA = 100, Integer slotB = 100");
        System.out.println("  slotA == slotB      : " + lowIdentity + "  (Same cached flyweight reference)");
        System.out.println("  slotA.equals(slotB) : " + lowEquality + "  (Equal numeric value)");

        // Sub-case B: Values OUTSIDE the Java Integer Cache (> 127)
        // Autoboxing allocates two distinct Integer instances on the heap.
        Integer slotHighA = 200;
        Integer slotHighB = 200;
        boolean highIdentity = (slotHighA == slotHighB); // Reference comparison (DIFFERENT heap objects!)
        boolean highEquality = slotHighA.equals(slotHighB); // Value comparison

        System.out.println("\nOutside Cached Range (> 127) - High-capacity Slot IDs:");
        System.out.println("  Integer slotC = 200, Integer slotD = 200");
        System.out.println("  slotC == slotD      : " + highIdentity + " (CRITICAL BUG RISK: Distinct heap objects!)");
        System.out.println("  slotC.equals(slotD) : " + highEquality + "  (CORRECT PRACTICE: Compares numeric value)");

        System.out.println("================================================================================");
    }
}

